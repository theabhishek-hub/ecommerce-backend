/**
 * Shopping Cart - Client-side State Management
 * Manages cart state via localStorage
 * Handles cart display, quantity updates, item removal, and total calculations
 * 
 * Cart Storage Format:
 * [
 *   {
 *     "productId": 1,
 *     "name": "Product Name",
 *     "price": 99.99,
 *     "quantity": 2,
 *     "imageUrl": "/images/product.jpg"
 *   }
 * ]
 */

const CART_KEY = 'ecommerce_cart';
const TAX_RATE = 0.10; // 10% tax

// Global variable to store current cart items (works for both API and localStorage)
let currentCartItems = [];

document.addEventListener('DOMContentLoaded', function() {
    initializeCart();
});

/**
 * Initialize cart on page load - fetch from API for authenticated users, localStorage for anonymous
 */
function initializeCart() {
    console.log('[Cart] Initializing...');
    
    // Clear any previous checkout selections to ensure clean state
    sessionStorage.removeItem('selectedProductIds');
    console.log('[Cart] Cleared sessionStorage selectedProductIds');
    
    // Check if user is authenticated via data attribute set by Thymeleaf
    const contentDiv = document.querySelector('[data-authenticated]');
    const isAuthenticated = contentDiv?.getAttribute('data-authenticated') === 'true';
    
    console.log('[Cart] Is authenticated:', isAuthenticated);
    
    if (isAuthenticated) {
        fetchCartFromAPI();
    } else {
        renderCart();
    }
    
    checkAuthStatus();
    setupGlobalEventListeners();
}

/**
 * Fetch cart from API for authenticated users
 */
function fetchCartFromAPI() {
    console.log('[Cart] Fetching from API...');
    
    const headers = {
        'Content-Type': 'application/json'
    };
    
    const token = localStorage.getItem('jwtToken');
    if (token) {
        headers['Authorization'] = `Bearer ${token}`;
    }
    
    fetch('/api/v1/cart', {
        method: 'GET',
        headers: headers,
        credentials: 'include' // Include session cookies
    })
    .then(response => {
        if (!response.ok) {
            if (response.status === 401 || response.status === 403) {
                throw new Error('Not authenticated');
            }
            throw new Error('Failed to fetch cart');
        }
        return response.json();
    })
    .then(data => {
        console.log('[Cart] Fetched from API:', data);
        const cartData = data.data || data;
        
        // Convert API response to cart format
        if (cartData && cartData.items && cartData.items.length > 0) {
            const cartItems = cartData.items.map(item => {
                console.log('[Cart] Item from API:', item);
                return {
                    productId: item.productId,
                    name: item.productName,
                    price: parseFloat(item.priceAmount || 0),
                    quantity: item.quantity,
                    imageUrl: item.imageUrl
                };
            });
            
            console.log('[Cart] Converted items:', cartItems);
            renderCartWithItems(cartItems);
        } else {
            console.log('[Cart] No items in API response');
            renderCart();
        }
    })
    .catch(error => {
        console.error('[Cart] Error fetching from API:', error);
        // Fall back to localStorage if API fails
        renderCart();
    });
}

/**
 * Render cart with specific items
 * @param {Array} cartItems - Items from API
 */
function renderCartWithItems(cartItems) {
    console.log('[Cart] Rendering API items...', cartItems);
    
    // Store items globally for use in event listeners
    currentCartItems = cartItems;
    console.log('[Cart] Stored current cart items in memory:', currentCartItems);
    
    if (cartItems.length === 0) {
        showEmptyCart();
        return;
    }
    
    showCartContent();
    renderCartItems(cartItems);
    updateCartTotals(cartItems);
}

/**
 * Render cart items and update UI
 */
function renderCart() {
    console.log('[Cart] Rendering...');
    const cartItems = getCartItems();
    
    // Store items globally for use in event listeners
    currentCartItems = cartItems;
    console.log('[Cart] Stored current cart items in memory:', currentCartItems);
    
    if (cartItems.length === 0) {
        showEmptyCart();
        return;
    }
    
    showCartContent();
    renderCartItems(cartItems);
    updateCartTotals(cartItems);
}

/**
 * Get cart items from localStorage
 * @returns {Array} Array of cart items
 */
function getCartItems() {
    try {
        const cartData = localStorage.getItem(CART_KEY);
        return cartData ? JSON.parse(cartData) : [];
    } catch (error) {
        console.error('[Cart] Error parsing cart from localStorage:', error);
        return [];
    }
}

/**
 * Save cart items to localStorage
 * @param {Array} items - Cart items to save
 */
function saveCartItems(items) {
    try {
        localStorage.setItem(CART_KEY, JSON.stringify(items));
        console.log('[Cart] Saved', items.length, 'items to localStorage');
    } catch (error) {
        console.error('[Cart] Error saving cart to localStorage:', error);
    }
}

/**
 * Render individual cart items
 * @param {Array} cartItems - Array of cart items
 */
function renderCartItems(cartItems) {
    const itemsList = document.getElementById('cartItemsList');
    itemsList.innerHTML = cartItems.map((item, index) => createCartItemHTML(item, index)).join('');
    
    // Attach event listeners to newly created elements
    attachItemEventListeners(cartItems);
}

/**
 * Create HTML for a single cart item
 * @param {Object} item - Cart item
 * @param {number} index - Item index in cart
 * @returns {string} HTML string
 */
function createCartItemHTML(item, index) {
    const subtotal = item.price * item.quantity;
    
    return `
        <div class="cart__item" data-product-id="${item.productId}" data-index="${index}">
            <div class="cart__col cart__col--select">
                <input type="checkbox" class="cart__item-checkbox" data-product-id="${item.productId}" checked />
            </div>
            <div class="cart__col cart__col--product">
                <div class="cart__product">
                    ${item.imageUrl ? 
                        `<img class="cart__product-image" src="${escapeHtml(item.imageUrl)}" alt="${escapeHtml(item.name)}" />` :
                        '<div class="cart__product-placeholder">No Image</div>'
                    }
                    <h3 class="cart__product-name">${escapeHtml(item.name)}</h3>
                </div>
            </div>
            <div class="cart__col cart__col--price">
                <span class="cart__price">$${formatPrice(item.price)}</span>
            </div>
            <div class="cart__col cart__col--quantity">
                <div class="cart__quantity">
                    <button class="cart__btn-quantity cart__btn-quantity--decrease" data-action="decrease">−</button>
                    <input type="number" class="cart__quantity-input" value="${item.quantity}" min="1" data-action="change" />
                    <button class="cart__btn-quantity cart__btn-quantity--increase" data-action="increase">+</button>
                </div>
            </div>
            <div class="cart__col cart__col--subtotal">
                <span class="cart__subtotal">$${formatPrice(subtotal)}</span>
            </div>
            <div class="cart__col cart__col--actions">
                <button class="cart__btn-remove" data-action="remove">Remove</button>
            </div>
        </div>
    `;
}

/**
 * Attach event listeners to cart item controls
 * @param {Array} cartItems - Current cart items
 */
function attachItemEventListeners(cartItems) {
    document.querySelectorAll('.cart__item').forEach(itemElement => {
        const index = parseInt(itemElement.getAttribute('data-index'));
        
        // Quantity buttons
        itemElement.querySelectorAll('.cart__btn-quantity').forEach(btn => {
            btn.addEventListener('click', function(e) {
                e.preventDefault();
                const action = this.getAttribute('data-action');
                handleQuantityChange(index, action, cartItems);
            });
        });
        
        // Quantity input
        const quantityInput = itemElement.querySelector('.cart__quantity-input');
        if (quantityInput) {
            quantityInput.addEventListener('change', function() {
                const newQuantity = parseInt(this.value) || 1;
                if (newQuantity < 1) this.value = 1;
                handleQuantityChange(index, 'set', cartItems, newQuantity);
            });
        }
        
        // Remove button
        const removeBtn = itemElement.querySelector('.cart__btn-remove');
        if (removeBtn) {
            removeBtn.addEventListener('click', function(e) {
                e.preventDefault();
                const productId = itemElement.getAttribute('data-product-id');
                handleRemoveItem(productId);
            });
        }
    });
}

/**
 * Handle quantity change for cart item
 * @param {number} index - Item index
 * @param {string} action - Action type: 'increase', 'decrease', 'set'
 * @param {Array} cartItems - Current cart items
 * @param {number} newQuantity - New quantity (for 'set' action)
 */
function handleQuantityChange(index, action, cartItems, newQuantity = null) {
    console.log(`[Cart] Quantity change - index: ${index}, action: ${action}`);
    
    // Check if authenticated
    const contentDiv = document.querySelector('[data-authenticated]');
    const isAuthenticated = contentDiv?.getAttribute('data-authenticated') === 'true';
    
    let newQty = cartItems[index].quantity;
    
    if (action === 'increase') {
        newQty += 1;
    } else if (action === 'decrease') {
        if (cartItems[index].quantity > 1) {
            newQty -= 1;
        }
    } else if (action === 'set' && newQuantity) {
        newQty = Math.max(1, newQuantity);
    }
    
    if (isAuthenticated) {
        // Use API to update quantity
        updateCartItemViaAPI(cartItems[index].productId, newQty);
    } else {
        // Use localStorage
        cartItems[index].quantity = newQty;
        saveCartItems(cartItems);
        renderCart();
    }
}

/**
 * Update cart item quantity via API
 * @param {number} productId - Product ID
 * @param {number} quantity - New quantity
 */
function updateCartItemViaAPI(productId, quantity) {
    console.log(`[Cart] Updating item ${productId} to quantity ${quantity} via API`);
    
    const headers = {
        'Content-Type': 'application/json'
    };
    
    const token = localStorage.getItem('jwtToken');
    if (token) {
        headers['Authorization'] = `Bearer ${token}`;
    }
    
    fetch(`/api/v1/cart/products/${productId}`, {
        method: 'PUT',
        headers: headers,
        credentials: 'include',
        body: JSON.stringify({
            quantity: quantity
        })
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('Failed to update cart');
        }
        return response.json();
    })
    .then(data => {
        console.log('[Cart] Updated via API, re-fetching cart');
        // Re-fetch the cart to get updated data from API
        fetchCartFromAPI();
    })
    .catch(error => {
        console.error('[Cart] Error updating item:', error);
        alert('Error updating cart: ' + error.message);
    });
}

/**
 * Handle removing item from cart
 * @param {number} productId - Product ID to remove
 */
function handleRemoveItem(productId) {
    console.log(`[Cart] Removing item with productId: ${productId}`);
    
    // Check if authenticated
    const contentDiv = document.querySelector('[data-authenticated]');
    const isAuthenticated = contentDiv?.getAttribute('data-authenticated') === 'true';
    
    if (isAuthenticated) {
        // Use API to remove item
        removeCartItemViaAPI(productId);
    } else {
        // Use localStorage - need to find index by productId
        const cartItems = getCartItems();
        const index = cartItems.findIndex(item => item.productId === parseInt(productId));
        if (index !== -1) {
            cartItems.splice(index, 1);
            saveCartItems(cartItems);
            renderCart();
        }
    }
}

/**
 * Remove cart item via API
 * @param {number} productId - Product ID to remove
 */
function removeCartItemViaAPI(productId) {
    console.log(`[Cart] Removing item ${productId} via API`);
    
    const headers = {
        'Content-Type': 'application/json'
    };
    
    const token = localStorage.getItem('jwtToken');
    if (token) {
        headers['Authorization'] = `Bearer ${token}`;
    }
    
    fetch(`/api/v1/cart/products/${productId}`, {
        method: 'DELETE',
        headers: headers,
        credentials: 'include'
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('Failed to remove item');
        }
        console.log('[Cart] Removed via API, re-rendering cart');
        // Wait a moment then re-fetch
        setTimeout(() => {
            fetchCartFromAPI();
        }, 300);
    })
    .catch(error => {
        console.error('[Cart] Error removing item:', error);
        alert('Error removing item: ' + error.message);
    });
}

/**
 * Update cart totals (subtotal, tax, total) based on selected items
 * @param {Array} cartItems - Cart items
 * @param {boolean} selectedOnly - Calculate only for selected items (default: true)
 */
function updateCartTotals(cartItems, selectedOnly = true) {
    let subtotal = 0;
    
    if (selectedOnly) {
        // Calculate only from selected items
        const selectedCheckboxes = document.querySelectorAll('.cart__item-checkbox:checked');
        const selectedProductIds = new Set(
            Array.from(selectedCheckboxes).map(checkbox => checkbox.getAttribute('data-product-id'))
        );
        
        subtotal = cartItems.reduce((sum, item) => {
            if (selectedProductIds.has(String(item.productId))) {
                return sum + (item.price * item.quantity);
            }
            return sum;
        }, 0);
    } else {
        // Calculate from all items
        subtotal = cartItems.reduce((sum, item) => sum + (item.price * item.quantity), 0);
    }
    
    const tax = subtotal * TAX_RATE;
    const total = subtotal + tax;
    
    document.getElementById('subtotalAmount').textContent = `$${formatPrice(subtotal)}`;
    document.getElementById('taxAmount').textContent = `$${formatPrice(tax)}`;
    document.getElementById('totalAmount').textContent = `$${formatPrice(total)}`;
    
    console.log(`[Cart] Totals - Subtotal: $${formatPrice(subtotal)}, Tax: $${formatPrice(tax)}, Total: $${formatPrice(total)}`);
}

/**
 * Show empty cart state
 */
function showEmptyCart() {
    document.getElementById('emptyCartState').style.display = 'block';
    document.getElementById('cartContainer').style.display = 'none';
    document.getElementById('errorState').style.display = 'none';
    console.log('[Cart] Showing empty state');
}

/**
 * Show cart content
 */
function showCartContent() {
    document.getElementById('emptyCartState').style.display = 'none';
    document.getElementById('cartContainer').style.display = 'block';
    document.getElementById('errorState').style.display = 'none';
}

/**
 * Setup global event listeners for cart controls
 */
function setupGlobalEventListeners() {
    const checkoutBtn = document.getElementById('checkoutBtn');
    if (checkoutBtn) {
        checkoutBtn.addEventListener('click', function(e) {
            e.preventDefault();
            handleCheckout();
        });
    }
    
    // Setup select all checkbox listener using event delegation
    document.addEventListener('change', function(e) {
        // Handle select all checkbox
        if (e.target.id === 'selectAllCheckbox') {
            console.log('[Cart] Select all checkbox changed:', e.target.checked);
            const isChecked = e.target.checked;
            document.querySelectorAll('.cart__item-checkbox').forEach(checkbox => {
                checkbox.checked = isChecked;
            });
            
            // Recalculate totals when select all is changed
            console.log('[Cart] Using currentCartItems for total calculation:', currentCartItems);
            updateCartTotals(currentCartItems, true);
        }
        // Handle individual item checkboxes
        else if (e.target.classList.contains('cart__item-checkbox')) {
            console.log('[Cart] Item checkbox changed for product:', e.target.getAttribute('data-product-id'));
            // Recalculate totals when any item checkbox changes
            console.log('[Cart] Using currentCartItems for total calculation:', currentCartItems);
            updateCartTotals(currentCartItems, true);
        }
    });
}

/**
 * Setup select all checkbox listener (kept for backwards compatibility)
 */
function setupSelectAllCheckbox() {
    // No longer needed - event delegation handles this
    console.log('[Cart] setupSelectAllCheckbox called (deprecated)');
}

/**
 * Check authentication status and update UI
 */
function checkAuthStatus() {
    // Check session-based authentication via data attribute
    const contentDiv = document.querySelector('[data-authenticated]');
    const isAuthenticated = contentDiv?.getAttribute('data-authenticated') === 'true';
    
    // Also check for JWT token
    const jwtToken = localStorage.getItem('jwtToken');
    const hasAuth = isAuthenticated || !!jwtToken;
    
    const authWarning = document.getElementById('authWarning');
    const checkoutBtn = document.getElementById('checkoutBtn');
    
    console.log('[Cart] Auth check - Session:', isAuthenticated, ', JWT:', !!jwtToken, ', Overall:', hasAuth);
    
    if (!hasAuth) {
        console.log('[Cart] Not authenticated');
        if (authWarning) authWarning.style.display = 'block';
        if (checkoutBtn) checkoutBtn.disabled = true;
    } else {
        console.log('[Cart] Authenticated');
        if (authWarning) authWarning.style.display = 'none';
        if (checkoutBtn) checkoutBtn.disabled = false;
    }
}

/**
 * Handle checkout action
 */
function handleCheckout() {
    console.log('[Cart] Starting checkout process...');
    
    // Check session-based or JWT authentication
    const contentDiv = document.querySelector('[data-authenticated]');
    const isAuthenticated = contentDiv?.getAttribute('data-authenticated') === 'true';
    const jwtToken = localStorage.getItem('jwtToken');
    const hasAuth = isAuthenticated || !!jwtToken;
    
    if (!hasAuth) {
        console.log('[Cart] Checkout requires authentication - redirecting to login');
        window.location.href = '/login';
        return;
    }
    
    // Check if cart has items - check the DOM for rendered items, not localStorage
    const cartItems = document.querySelectorAll('.cart__item');
    if (cartItems.length === 0) {
        alert('Your cart is empty');
        console.warn('[Cart] Cart is empty, cannot proceed to checkout');
        return;
    }
    
    // Get selected items
    const selectedCheckboxes = document.querySelectorAll('.cart__item-checkbox:checked');
    if (selectedCheckboxes.length === 0) {
        alert('Please select at least one product to proceed to checkout');
        console.warn('[Cart] No products selected, cannot proceed to checkout');
        return;
    }
    
    const selectedProductIds = Array.from(selectedCheckboxes).map(checkbox => {
        const id = checkbox.getAttribute('data-product-id');
        return id;
    });
    
    console.log('[Cart] Selected product IDs before storing:', selectedProductIds);
    console.log('[Cart] Proceeding to checkout with', selectedProductIds.length, 'selected items');
    
    // Store selected product IDs in session storage for checkout page to use
    const selectedIdsJson = JSON.stringify(selectedProductIds);
    sessionStorage.setItem('selectedProductIds', selectedIdsJson);
    
    // Verify it was stored
    const storedIds = sessionStorage.getItem('selectedProductIds');
    console.log('[Cart] Verified stored selectedProductIds:', storedIds);
    
    // Navigate to checkout page
    console.log('[Cart] Navigating to /checkout');
    window.location.href = '/checkout';
}

/**
 * Format price to 2 decimal places
 * @param {number} amount - Price amount
 * @returns {string} Formatted price
 */
function formatPrice(amount) {
    return parseFloat(amount).toFixed(2);
}

/**
 * Escape HTML special characters to prevent XSS
 * @param {string} text - Text to escape
 * @returns {string} Escaped text
 */
function escapeHtml(text) {
    if (!text) return '';
    const map = {
        '&': '&amp;',
        '<': '&lt;',
        '>': '&gt;',
        '"': '&quot;',
        "'": '&#039;'
    };
    return text.replace(/[&<>"']/g, m => map[m]);
}

/**
 * Add product to cart (can be called from other pages)
 * @param {number} productId - Product ID
 * @param {string} name - Product name
 * @param {number} price - Product price
 * @param {string} imageUrl - Product image URL
 * @param {number} quantity - Quantity to add (default: 1)
 */
function addToCart(productId, name, price, imageUrl = null, quantity = 1) {
    console.log(`[Cart] Adding to cart - productId: ${productId}, quantity: ${quantity}`);
    
    const cartItems = getCartItems();
    const existingItem = cartItems.find(item => item.productId === productId);
    
    if (existingItem) {
        existingItem.quantity += quantity;
        console.log(`[Cart] Item already in cart, updated quantity to ${existingItem.quantity}`);
    } else {
        cartItems.push({
            productId,
            name,
            price,
            quantity,
            imageUrl
        });
        console.log('[Cart] New item added to cart');
    }
    
    saveCartItems(cartItems);
    console.log('[Cart] Saved. Total items:', cartItems.length);
}

/**
 * Remove product from cart
 * @param {number} productId - Product ID to remove
 */
function removeFromCart(productId) {
    console.log(`[Cart] Removing from cart - productId: ${productId}`);
    
    const cartItems = getCartItems();
    const index = cartItems.findIndex(item => item.productId === productId);
    
    if (index > -1) {
        cartItems.splice(index, 1);
        saveCartItems(cartItems);
        console.log('[Cart] Item removed. Remaining items:', cartItems.length);
    }
}

/**
 * Clear entire cart
 */
function clearCart() {
    console.log('[Cart] Clearing entire cart');
    localStorage.removeItem(CART_KEY);
}

/**
 * Get cart item count
 * @returns {number} Number of items in cart
 */
function getCartItemCount() {
    return getCartItems().reduce((sum, item) => sum + item.quantity, 0);
}

/**
 * Get cart total amount
 * @returns {number} Total cart amount
 */
function getCartTotal() {
    const cartItems = getCartItems();
    const subtotal = cartItems.reduce((sum, item) => sum + (item.price * item.quantity), 0);
    return subtotal + (subtotal * TAX_RATE);
}