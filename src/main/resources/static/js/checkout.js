/**
 * Checkout Page Logic
 * 
 * Responsibilities:
 * 1. Read cart from localStorage
 * 2. Validate cart (redirect to /cart if empty)
 * 3. Check JWT auth token (redirect to /login if not authenticated)
 * 4. Populate shipping form and cart summary
 * 5. Check Razorpay availability and show/hide option
 * 6. Handle COD and Razorpay payment flows
 */

// Global state
let razorpayEnabled = false;
let razorpayKeyId = null;

document.addEventListener('DOMContentLoaded', function () {
    initCheckout();
});

/**
 * Initialize checkout page
 * Note: Authentication is handled by Spring Security - if user can access this page, they're authenticated
 */
async function initCheckout() {
    try {
        // Step 1: Load cart from API or localStorage
        let cart = await getCartData();
        console.log('Cart data loaded:', cart);
        
        // Step 2: Validate cart is not empty
        if (!cart || !cart.items || cart.items.length === 0) {
            redirectToCart('Your cart is empty');
            return;
        }

        // Step 3: Check Razorpay availability
        await checkRazorpayAvailability();

        // Step 4: Populate cart summary and form
        populateCartSummary(cart);
        enablePlaceOrderButton();
    } catch (error) {
        console.error('Error initializing checkout:', error);
        showMessage('Failed to load checkout. Please refresh the page.', 'error');
        enablePlaceOrderButton(); // Enable button anyway so user can try again or go back
    }
}

/**
 * Get cart data from API (for authenticated users) or localStorage (for anonymous)
 * Filters by selectedProductIds from sessionStorage if available
 * @returns {object} Cart object with items and totals
 */
async function getCartData() {
    try {
        // Get selected product IDs from sessionStorage
        const selectedProductIds = getSelectedProductIds();
        console.log('[Checkout] Selected product IDs from sessionStorage:', selectedProductIds);
        
        // Try to fetch from API first (for authenticated users)
        const response = await fetch('/api/v1/cart', {
            method: 'GET',
            credentials: 'include',
            headers: {
                'Content-Type': 'application/json'
            }
        });
        
        if (response.ok) {
            const data = await response.json();
            console.log('API cart response:', data);
            
            // Handle ApiResponse wrapper { success, data: {...} }
            const cartData = data.data || data;
            
            if (cartData && cartData.items && Array.isArray(cartData.items) && cartData.items.length > 0) {
                console.log('Cart items from API:', cartData.items);
                
                // Filter items if selected product IDs exist
                let filteredItems = cartData.items;
                if (selectedProductIds && selectedProductIds.length > 0) {
                    console.log('[Checkout] Filtering API items by selectedProductIds...');
                    filteredItems = cartData.items.filter(item => 
                        selectedProductIds.includes(String(item.productId))
                    );
                    console.log('Filtered API items:', filteredItems);
                } else {
                    console.log('[Checkout] No selected product IDs, using all API items');
                }
                
                // Convert API response to checkout format
                const convertedItems = filteredItems.map(item => ({
                    productId: item.productId,
                    name: item.productName,
                    price: parseFloat(item.priceAmount || 0),
                    quantity: item.quantity,
                    imageUrl: item.imageUrl
                }));
                
                if (convertedItems.length > 0) {
                    return {
                        items: convertedItems,
                        subtotal: convertedItems.reduce((sum, item) => sum + (parseFloat(item.price || 0) * item.quantity), 0)
                    };
                } else {
                    console.warn('[Checkout] No items after filtering');
                }
            } else {
                console.warn('API cart is empty or missing items');
            }
        } else {
            console.warn('Failed to fetch cart from API:', response.status, response.statusText);
        }
    } catch (error) {
        console.error('Error fetching cart from API:', error);
    }
    
    // Fall back to localStorage for anonymous users
    try {
        const cart = localStorage.getItem('ecommerce_cart');
        if (cart) {
            const parsedCart = JSON.parse(cart);
            console.log('[Checkout] Using cart from localStorage:', parsedCart);
            
            // Filter by selected product IDs if available
            const selectedProductIds = getSelectedProductIds();
            if (selectedProductIds && selectedProductIds.length > 0) {
                console.log('[Checkout] Filtering localStorage items by selectedProductIds...');
                const filteredItems = parsedCart.filter(item => 
                    selectedProductIds.includes(String(item.productId))
                );
                console.log('[Checkout] Filtered localStorage items:', filteredItems);
                
                if (filteredItems.length > 0) {
                    return {
                        items: filteredItems,
                        subtotal: filteredItems.reduce((sum, item) => sum + (item.price * item.quantity), 0)
                    };
                }
            } else {
                console.log('[Checkout] No selected product IDs, using all localStorage items');
                // Use all items if no selection
                const subtotal = parsedCart.reduce((sum, item) => sum + (item.price * item.quantity), 0);
                return {
                    items: parsedCart,
                    subtotal: subtotal
                };
            }
        }
    } catch (error) {
        console.error('Error parsing localStorage cart:', error);
    }
    
    return null;
}

/**
 * Get selected product IDs from sessionStorage
 * @returns {Array} Array of selected product IDs
 */
function getSelectedProductIds() {
    try {
        const selectedIds = sessionStorage.getItem('selectedProductIds');
        if (selectedIds) {
            return JSON.parse(selectedIds);
        }
    } catch (error) {
        console.error('Error parsing selected product IDs:', error);
    }
    return null;
}

/**
 * Check if Razorpay is enabled and update UI accordingly
 */
async function checkRazorpayAvailability() {
    try {
        const response = await fetch('/api/v1/payments/razorpay/enabled', {
            method: 'GET',
            credentials: 'include'
        });
        
        if (response.ok) {
            const data = await response.json();
            if (data.success && data.data && data.data.enabled) {
                razorpayEnabled = true;
                razorpayKeyId = data.data.keyId;
                // Show Razorpay option
                const razorpayOption = document.getElementById('razorpayOption');
                if (razorpayOption) {
                    razorpayOption.style.display = 'block';
                }
            }
        }
    } catch (error) {
        console.warn('Failed to check Razorpay availability:', error);
        // Continue without Razorpay - COD will be the only option
    }
}

/**
 * Redirect to cart page
 * @param {string} message - Optional message to display
 */
function redirectToCart(message = '') {
    if (message) {
        sessionStorage.setItem('cartMessage', message);
    }
    window.location.href = '/cart';
}

/**
 * Get cart from localStorage
 * @returns {object} Cart object with items and totals
 */
function getCart() {
    const cart = localStorage.getItem('cart');
    return cart ? JSON.parse(cart) : null;
}

/**
 * Populate cart summary in the checkout sidebar
 * @param {object} cart - Cart object from localStorage
 */
function populateCartSummary(cart) {
    try {
        const cartItemsContainer = document.getElementById('cartItems');
        const subtotalElem = document.getElementById('subtotal');
        const shippingElem = document.getElementById('shipping');
        const taxElem = document.getElementById('tax');
        const totalElem = document.getElementById('total');

        if (!cartItemsContainer || !subtotalElem || !shippingElem || !taxElem || !totalElem) {
            console.error('One or more cart summary elements not found');
            return;
        }

        // Clear loading message
        cartItemsContainer.innerHTML = '';

        // Populate each item
        const items = cart.items || [];
        items.forEach(item => {
            const itemElement = document.createElement('div');
            itemElement.className = 'summary-item';
            itemElement.innerHTML = `
                <div class="item-details">
                    <span class="item-name">${escapeHtml(item.name)}</span>
                    <span class="item-qty">Qty: ${item.quantity}</span>
                </div>
                <div class="item-price">
                    ₹${formatPrice(item.price * item.quantity)}
                </div>
            `;
            cartItemsContainer.appendChild(itemElement);
        });

        // Calculate and display totals
        const subtotal = cart.subtotal || cart.items.reduce((sum, item) => sum + (item.price * item.quantity), 0);
        const shipping = 0; // Free shipping for now
        const tax = Math.round(subtotal * 0.05 * 100) / 100; // 5% tax
        const total = subtotal + shipping + tax;

        subtotalElem.textContent = `₹${formatPrice(subtotal)}`;
        shippingElem.textContent = `₹${formatPrice(shipping)}`;
        taxElem.textContent = `₹${formatPrice(tax)}`;
        totalElem.textContent = `₹${formatPrice(total)}`;
        
        console.log('Cart summary populated successfully');
    } catch (error) {
        console.error('Error populating cart summary:', error);
    }
}

/**
 * Enable place order button and attach event listener
 */
function enablePlaceOrderButton() {
    const placeOrderBtn = document.getElementById('placeOrderBtn');
    if (!placeOrderBtn) {
        console.error('Place order button not found!');
        return;
    }
    placeOrderBtn.disabled = false;
    placeOrderBtn.addEventListener('click', handlePlaceOrder);
    console.log('Place order button enabled');
}

/**
 * Handle place order button click
 * Routes to COD or Razorpay flow based on selected payment method
 */
function handlePlaceOrder() {
    // Get form data
    const shippingForm = document.getElementById('shippingForm');
    const paymentMethodRadio = document.querySelector('input[name="paymentMethod"]:checked');

    // Validate form
    if (!shippingForm.checkValidity()) {
        shippingForm.reportValidity();
        return;
    }

    const paymentMethod = paymentMethodRadio ? paymentMethodRadio.value : 'COD';

    // Route to appropriate payment flow
    if (paymentMethod === 'COD') {
        handleCODOrder();
    } else if (paymentMethod === 'ONLINE' && razorpayEnabled) {
        handleRazorpayOrder();
    } else {
        showMessage('Online payment is not available. Please select COD.', 'error');
    }
}

/**
 * Handle COD order placement
 */
function handleCODOrder() {
    const placeOrderBtn = document.getElementById('placeOrderBtn');
    placeOrderBtn.disabled = true;
    placeOrderBtn.textContent = 'Placing Order...';

    // Get CSRF token
    const csrfToken = getCsrfToken();
    const csrfHeaderName = getCsrfHeaderName();
    const headers = {
        'Content-Type': 'application/x-www-form-urlencoded',
    };
    
    if (csrfToken) {
        headers[csrfHeaderName] = csrfToken;
    }

    // Get selected product IDs from sessionStorage
    const selectedProductIds = getSelectedProductIds();
    console.log('[Checkout] Placing COD order with selected product IDs:', selectedProductIds);

    // Place order with COD payment method
    const formData = new URLSearchParams();
    formData.append('paymentMethod', 'COD');
    
    // Append selected product IDs
    if (selectedProductIds && selectedProductIds.length > 0) {
        console.log('[Checkout] Adding', selectedProductIds.length, 'product IDs to form');
        selectedProductIds.forEach(id => {
            console.log('[Checkout] Appending product ID:', id);
            formData.append('selectedProductIds', id);
        });
        console.log('[Checkout] Form data after adding product IDs:', Array.from(formData.entries()));
    } else {
        console.warn('[Checkout] No selected product IDs found in sessionStorage');
    }

    console.log('[Checkout] Final form data string:', formData.toString());

    fetch('/checkout/place-order', {
        method: 'POST',
        headers: headers,
        body: formData,
        credentials: 'include'
    })
    .then(response => {
        console.log('[Checkout] Response status:', response.status, response.statusText);
        console.log('[Checkout] Response redirected:', response.redirected);
        if (response.redirected) {
            // Success - redirect to orders page
            localStorage.removeItem('cart');
            sessionStorage.removeItem('selectedProductIds');
            window.location.href = response.url;
        } else if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        } else {
            localStorage.removeItem('cart');
            window.location.href = '/orders';
        }
    })
    .catch(error => {
        console.error('[Checkout] Error placing COD order:', error);
        showMessage('Failed to place order. Please try again.', 'error');
        placeOrderBtn.disabled = false;
        placeOrderBtn.textContent = 'Place Order';
    });
}

/**
 * Handle Razorpay order placement flow
 */
async function handleRazorpayOrder() {
    const placeOrderBtn = document.getElementById('placeOrderBtn');
    placeOrderBtn.disabled = true;
    placeOrderBtn.textContent = 'Processing...';

    try {
        // Step 1: Create Razorpay order WITHOUT creating the database order yet
        // This prevents cart clearing and order creation if payment is cancelled
        const razorpayOrderResponse = await createRazorpayOrderWithoutDbOrder();
        if (!razorpayOrderResponse || !razorpayOrderResponse.razorpayOrderId) {
            throw new Error('Failed to prepare payment');
        }

        // Step 2: Open Razorpay checkout modal
        const options = {
            key: razorpayKeyId,
            amount: razorpayOrderResponse.amount,
            currency: razorpayOrderResponse.currency,
            name: 'E-Commerce Store',
            description: 'Order Payment',
            order_id: razorpayOrderResponse.razorpayOrderId,
            handler: function(response) {
                // Step 3: Only create order and verify payment AFTER successful Razorpay payment
                verifyAndCreateOrderAfterRazorpayPayment(response);
            },
            prefill: {
                name: document.getElementById('firstName')?.value + ' ' + document.getElementById('lastName')?.value,
                email: document.getElementById('email')?.value,
                contact: document.getElementById('phone')?.value
            },
            theme: {
                color: '#3399cc'
            },
            modal: {
                ondismiss: function() {
                    // User closed the modal without completing payment
                    // Cart is still intact - no order was created
                    console.log('Payment cancelled by user');
                    placeOrderBtn.disabled = false;
                    placeOrderBtn.textContent = 'Place Order';
                    showMessage('Payment cancelled. Your cart items are preserved. Please try again whenever you are ready.', 'info');
                }
            }
        };

        const razorpay = new Razorpay(options);
        razorpay.open();
        razorpay.on('payment.failed', function(response) {
            // Payment failed - no order was created yet
            console.log('Payment failed:', response);
            placeOrderBtn.disabled = false;
            placeOrderBtn.textContent = 'Place Order';
            showMessage('Payment failed. Please try again.', 'error');
        });

    } catch (error) {
        console.error('Error in Razorpay flow:', error);
        showMessage(error.message || 'Failed to process payment. Please try again.', 'error');
        placeOrderBtn.disabled = false;
        placeOrderBtn.textContent = 'Place Order';
    }
}

/**
 * Create Razorpay order WITHOUT creating database order
 * This prepares Razorpay for payment without committing order in DB
 * Order is created only AFTER payment verification
 * @returns {Promise<{razorpayOrderId: string, amount: number, currency: string}>}
 */
async function createRazorpayOrderWithoutDbOrder() {
    // Get selected product IDs and cart data
    const selectedProductIds = getSelectedProductIds();
    const cart = await getCartData();
    
    if (!cart || !cart.items || cart.items.length === 0) {
        throw new Error('Cart is empty');
    }

    // Calculate amount from cart
    const subtotal = cart.subtotal || cart.items.reduce((sum, item) => sum + (item.price * item.quantity), 0);
    const tax = Math.round(subtotal * 0.05 * 100) / 100;
    const total = subtotal + tax;
    
    console.log('[Checkout] Creating Razorpay order with amount:', total);
    
    // Create Razorpay order WITHOUT creating database order
    const response = await fetch('/api/v1/payments/razorpay/create-order-only', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        credentials: 'include',
        body: JSON.stringify({ 
            amount: Math.round(total * 100), // Convert to paise
            currency: 'INR'
        })
    });

    if (!response.ok) {
        const errorData = await response.json().catch(() => ({}));
        throw new Error(errorData.message || 'Failed to prepare payment');
    }

    const data = await response.json();
    if (data.success && data.data) {
        return {
            razorpayOrderId: data.data.razorpayOrderId,
            amount: data.data.amount,
            currency: data.data.currency
        };
    }

    throw new Error('Invalid response from payment API');
}

/**
 * Verify Razorpay payment and CREATE order AFTER successful payment
 * @param {object} razorpayResponse - Razorpay payment response
 */
async function verifyAndCreateOrderAfterRazorpayPayment(razorpayResponse) {
    const placeOrderBtn = document.getElementById('placeOrderBtn');
    placeOrderBtn.disabled = true;
    placeOrderBtn.textContent = 'Completing Order...';

    try {
        // Step 1: Verify payment signature
        console.log('[Checkout] Verifying payment signature...');
        const verifyResponse = await fetch('/api/v1/payments/razorpay/verify-signature', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            credentials: 'include',
            body: JSON.stringify({
                razorpayOrderId: razorpayResponse.razorpay_order_id,
                razorpayPaymentId: razorpayResponse.razorpay_payment_id,
                razorpaySignature: razorpayResponse.razorpay_signature
            })
        });

        console.log('[Checkout] Verify signature response status:', verifyResponse.status);

        if (!verifyResponse.ok) {
            const errorData = await verifyResponse.json().catch(() => ({}));
            console.error('[Checkout] Signature verification failed:', errorData);
            throw new Error(errorData.message || 'Payment verification failed');
        }

        console.log('[Checkout] Payment signature verified successfully!');

        // Step 2: Payment verified - NOW create the order
        let selectedProductIds = getSelectedProductIds();
        let orderUrl = '/checkout/place-order';
        
        console.log('[Checkout] Initial selectedProductIds from sessionStorage:', selectedProductIds);
        
        // Fallback: If no selectedProductIds, use all cart items
        if (!selectedProductIds || selectedProductIds.length === 0) {
            console.log('[Checkout] No selectedProductIds in sessionStorage, fetching cart to get product IDs...');
            try {
                const cart = await getCartData();
                if (cart && cart.items && cart.items.length > 0) {
                    selectedProductIds = cart.items.map(item => String(item.productId));
                    console.log('[Checkout] Extracted product IDs from cart:', selectedProductIds);
                }
            } catch (e) {
                console.warn('[Checkout] Could not fetch cart for fallback:', e);
            }
        }
        
        const csrfToken = getCsrfToken();
        const csrfHeaderName = getCsrfHeaderName();
        const headers = {
            'Content-Type': 'application/x-www-form-urlencoded',
        };
        
        if (csrfToken) {
            headers[csrfHeaderName] = csrfToken;
        }

        const formData = new URLSearchParams();
        formData.append('paymentMethod', 'ONLINE');
        
        console.log('[Checkout] Final selected product IDs:', selectedProductIds);
        console.log('[Checkout] Final selected product IDs length:', selectedProductIds ? selectedProductIds.length : 0);
        
        if (selectedProductIds && selectedProductIds.length > 0) {
            console.log('[Checkout] Appending selected product IDs to form...');
            selectedProductIds.forEach(id => {
                console.log('[Checkout] Adding product ID:', id);
                formData.append('selectedProductIds', id);
            });
        } else {
            console.warn('[Checkout] WARNING: No product IDs to send! This might cause order creation to fail');
        }

        console.log('[Checkout] Form data to send:');
        for (let [key, value] of formData.entries()) {
            console.log(`  ${key}: ${value}`);
        }

        console.log('[Checkout] Creating order after payment verification...');
        const createOrderResponse = await fetch('/checkout/place-order', {
            method: 'POST',
            headers: headers,
            body: formData,
            credentials: 'include',
            redirect: 'follow'  // Follow redirects
        });

        console.log('[Checkout] Order creation response status:', createOrderResponse.status);
        console.log('[Checkout] Order creation response URL:', createOrderResponse.url);
        console.log('[Checkout] Order creation response OK:', createOrderResponse.ok);
        console.log('[Checkout] Response type:', createOrderResponse.type);

        // Debug: Log response headers
        console.log('[Checkout] Response headers - content-type:', createOrderResponse.headers.get('content-type'));
        
        // Check if we got a redirect to /orders (success) or /checkout (error)
        if (createOrderResponse.url && createOrderResponse.url.includes('/orders')) {
            console.log('[Checkout] SUCCESS: Detected /orders URL in response');
            // Success - clear cart (both client and server) and show success message
            localStorage.removeItem('cart');
            sessionStorage.removeItem('selectedProductIds');
            
            // Also clear server-side cart
            try {
                await fetch('/api/v1/cart', {
                    method: 'DELETE',
                    credentials: 'include'
                }).catch(() => {
                    // Silent fail - cart might not exist or API might not support DELETE
                    console.log('[Checkout] Cart clear on server skipped');
                });
            } catch (e) {
                console.log('[Checkout] Could not clear server cart:', e);
            }
            
            showMessage('Payment successful! Order placed.', 'success');
            
            console.log('[Checkout] Order created successfully, redirecting to:', createOrderResponse.url);
            setTimeout(() => {
                window.location.href = createOrderResponse.url;
            }, 500);
        } else if (createOrderResponse.url.includes('/checkout')) {
            // Error - user was redirected back to checkout with error message
            const errorText = await createOrderResponse.text();
            console.error('[Checkout] Order creation failed. Full response:', errorText);
            
            // Extract error message from flash attributes or alert divs
            const parser = new DOMParser();
            const doc = parser.parseFromString(errorText, 'text/html');
            
            // Try to find error message from multiple sources
            let errorMessage = 'Failed to create order after payment verification';
            
            // Look for Bootstrap alert with error class
            const alertError = doc.querySelector('.alert-danger');
            if (alertError) {
                errorMessage = alertError.textContent.trim();
                console.log('[Checkout] Found error in alert-danger:', errorMessage);
            }
            
            // Look for any element with data-error attribute
            const errorElement = doc.querySelector('[data-error]');
            if (errorElement && !alertError) {
                errorMessage = errorElement.textContent.trim();
                console.log('[Checkout] Found error in data-error:', errorMessage);
            }
            
            // Look for any error or failure message in the page
            const titleTag = doc.querySelector('title');
            if (titleTag && titleTag.textContent.includes('Error')) {
                console.log('[Checkout] Page title indicates error:', titleTag.textContent);
            }
            
            console.error('[Checkout] Extracted error message:', errorMessage);
            throw new Error(errorMessage);
        } else if (createOrderResponse.ok) {
            // Fallback: HTTP OK response
            localStorage.removeItem('cart');
            sessionStorage.removeItem('selectedProductIds');
            showMessage('Payment successful! Order placed.', 'success');
            setTimeout(() => {
                window.location.href = '/orders';
            }, 500);
        } else {
            const responseText = await createOrderResponse.text();
            console.error('[Checkout] Order creation failed with status:', createOrderResponse.status);
            console.error('[Checkout] Response text:', responseText);
            throw new Error(`Failed to create order. Server returned status ${createOrderResponse.status}`);
        }

    } catch (error) {
        console.error('Error in payment verification/order creation:', error);
        console.error('Error stack:', error.stack);
        showMessage(`Payment verified but failed to create order: ${error.message}`, 'error');
        placeOrderBtn.disabled = false;
        placeOrderBtn.textContent = 'Place Order';
    }
}

/**
 * Create Razorpay order
 * @param {number} orderId - Internal order ID
 * @returns {Promise<{razorpayOrderId: string, amount: number, currency: string}>}
 */
async function createRazorpayOrder(orderId) {
    const response = await fetch('/api/v1/payments/razorpay/create-order', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        credentials: 'include',
        body: JSON.stringify({ orderId: orderId })
    });

    if (!response.ok) {
        const errorData = await response.json().catch(() => ({}));
        throw new Error(errorData.message || 'Failed to create Razorpay order');
    }

    const data = await response.json();
    if (data.success && data.data) {
        return {
            razorpayOrderId: data.data.razorpayOrderId,
            amount: data.data.amount,
            currency: data.data.currency
        };
    }

    throw new Error('Invalid response from Razorpay API');
}

/**
 * Verify Razorpay payment after successful payment
 * @param {number} orderId - Internal order ID
 * @param {object} razorpayResponse - Razorpay payment response
 */
async function verifyRazorpayPayment(orderId, razorpayResponse) {
    const placeOrderBtn = document.getElementById('placeOrderBtn');
    placeOrderBtn.disabled = true;
    placeOrderBtn.textContent = 'Verifying Payment...';

    try {
        const response = await fetch('/api/v1/payments/razorpay/verify', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            credentials: 'include',
            body: JSON.stringify({
                orderId: orderId,
                razorpayOrderId: razorpayResponse.razorpay_order_id,
                razorpayPaymentId: razorpayResponse.razorpay_payment_id,
                razorpaySignature: razorpayResponse.razorpay_signature
            })
        });

        if (!response.ok) {
            const errorData = await response.json().catch(() => ({}));
            throw new Error(errorData.message || 'Payment verification failed');
        }

        // Success - clear cart and redirect
        localStorage.removeItem('cart');
        showMessage('Payment successful! Order placed.', 'success');
        setTimeout(() => {
            window.location.href = '/orders';
        }, 1500);

    } catch (error) {
        console.error('Payment verification error:', error);
        showMessage('Payment verification failed. Please contact support.', 'error');
        placeOrderBtn.disabled = false;
        placeOrderBtn.textContent = 'Place Order';
    }
}

/**
 * Handle Razorpay payment failure
 * @param {number} orderId - Internal order ID
 * @param {object} response - Razorpay failure response
 */
function handleRazorpayFailure(orderId, response) {
    console.error('Razorpay payment failed:', response);
    const errorMessage = response.error?.description || 'Unknown error';
    showMessage(`Payment failed: ${errorMessage}. Your cart items are still saved. Please retry.`, 'error');
    const placeOrderBtn = document.getElementById('placeOrderBtn');
    placeOrderBtn.disabled = false;
    placeOrderBtn.textContent = 'Place Order';
    // Note: Cart is NOT cleared on payment failure - user can retry
}

/**
 * Get CSRF token from meta tag or cookie
 * @returns {string|null} CSRF token or null if not found
 */
function getCsrfToken() {
    // Try to get from meta tag first (Thymeleaf includes it)
    const metaTag = document.querySelector('meta[name="_csrf"]');
    if (metaTag) {
        return metaTag.getAttribute('content');
    }
    // Fallback: try to get from cookie
    const cookies = document.cookie.split(';');
    for (let cookie of cookies) {
        const [name, value] = cookie.trim().split('=');
        if (name === 'XSRF-TOKEN') {
            return decodeURIComponent(value);
        }
    }
    return null;
}

/**
 * Get CSRF header name from meta tag
 * @returns {string} CSRF header name (default: X-XSRF-TOKEN)
 */
function getCsrfHeaderName() {
    const metaTag = document.querySelector('meta[name="_csrf_header"]');
    return metaTag ? metaTag.getAttribute('content') : 'X-XSRF-TOKEN';
}

/**
 * Format price to 2 decimal places
 * @param {number} price - Price value
 * @returns {string} Formatted price
 */
function formatPrice(price) {
    return parseFloat(price).toFixed(2);
}

/**
 * Escape HTML special characters to prevent XSS
 * @param {string} text - Text to escape
 * @returns {string} Escaped text
 */
function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

/**
 * Show temporary message to user
 * @param {string} message - Message text
 * @param {string} type - Message type (success, error, info)
 */
function showMessage(message, type = 'info') {
    // Create alert element if it doesn't exist
    let alertContainer = document.getElementById('alertContainer');
    if (!alertContainer) {
        alertContainer = document.createElement('div');
        alertContainer.id = 'alertContainer';
        alertContainer.className = 'alert-container';
        alertContainer.style.cssText = 'position: fixed; top: 20px; right: 20px; z-index: 9999; max-width: 400px;';
        document.body.insertBefore(alertContainer, document.body.firstChild);
    }

    const alertElement = document.createElement('div');
    alertElement.className = `alert alert-${type}`;
    alertElement.textContent = message;
    alertElement.style.cssText = `
        padding: 15px 20px;
        margin-bottom: 10px;
        border-radius: 4px;
        background-color: ${type === 'success' ? '#d4edda' : type === 'error' ? '#f8d7da' : '#d1ecf1'};
        color: ${type === 'success' ? '#155724' : type === 'error' ? '#721c24' : '#0c5460'};
        border: 1px solid ${type === 'success' ? '#c3e6cb' : type === 'error' ? '#f5c6cb' : '#bee5eb'};
        animation: slideDown 0.3s ease-out;
    `;

    alertContainer.appendChild(alertElement);

    // Auto-remove after 5 seconds
    setTimeout(() => {
        alertElement.style.animation = 'slideUp 0.3s ease-out';
        setTimeout(() => alertElement.remove(), 300);
    }, 5000);
}
