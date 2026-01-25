/**
 * Product Details - Placeholder JavaScript
 * Handles UI state management for product detail view
 * 
 * TODO: Implement actual API calls to /api/v1/products/{id}
 */

document.addEventListener('DOMContentLoaded', function() {
    initializeProductDetails();
    setupAddToCartButton();
});

/**
 * Initialize the product details view
 * Extracts product ID from URL and prepares for data loading
 */
function initializeProductDetails() {
    const productId = extractProductIdFromUrl();
    
    if (productId && !isNaN(productId)) {
        console.log('Product details initialized for product ID:', productId);
        fetchProductDetails(productId);
        fetchStockStatus(productId);
    } else {
        showError('Invalid product ID');
    }
}

/**
 * Extract product ID from the current URL path
 * @returns {string|null} The product ID or null if not found
 */
function extractProductIdFromUrl() {
    const pathParts = window.location.pathname.split('/');
    return pathParts[pathParts.length - 1];
}

/**
 * Fetch product details from API
 * @param {number} productId - The product ID to fetch
 */
function fetchProductDetails(productId) {
    const loadingSpinner = document.getElementById('loadingSpinner');
    const productDetails = document.getElementById('productDetails');
    const emptyState = document.getElementById('emptyState');
    const errorState = document.getElementById('errorState');

    // Reset all states
    showLoadingState(loadingSpinner, productDetails, emptyState, errorState);

    fetch(`/api/v1/products/${productId}`)
        .then(response => {
            if (!response.ok) throw new Error('Product not found');
            return response.json();
        })
        .then(data => {
            const product = data.data || data;
            if (!product || !product.id) {
                emptyState.style.display = 'block';
                loadingSpinner.style.display = 'none';
                return;
            }
            populateProductDetails(product);
            loadingSpinner.style.display = 'none';
            productDetails.style.display = 'block';
        })
        .catch(handleProductDetailError);
}

/**
 * Handle successful product detail response from API
 * @param {Object} response - The fetch response
 */
function handleProductDetailResponse(response) {
    const loadingSpinner = document.getElementById('loadingSpinner');
    const productDetails = document.getElementById('productDetails');
    const emptyState = document.getElementById('emptyState');

    loadingSpinner.style.display = 'none';

    // TODO: Parse response and validate product data
    // Example:
    // const product = response.data || response;
    // if (!product || !product.id) {
    //     emptyState.style.display = 'block';
    //     return;
    // }
    // populateProductDetails(product);
    // productDetails.style.display = 'block';
}

/**
 * Handle API error during product detail fetch
 * @param {Error} error - The error object
 */
function handleProductDetailError(error) {
    console.error('Error fetching product details:', error);
    const loadingSpinner = document.getElementById('loadingSpinner');
    const emptyState = document.getElementById('emptyState');
    const errorState = document.getElementById('errorState');

    loadingSpinner.style.display = 'none';

    if (error.message === 'Product not found') {
        emptyState.style.display = 'block';
    } else {
        showError(error.message);
    }
}

/**
 * Populate the product details view with data
 * @param {Object} product - Product object with full details
 */
function populateProductDetails(product) {
    document.getElementById('productName').textContent = product.name || '';
    document.getElementById('productSku').textContent = product.sku || 'N/A';
    document.getElementById('productBrand').textContent = product.brandName || 'N/A';
    document.getElementById('productCategory').textContent = product.categoryName || 'N/A';
    document.getElementById('productStatus').textContent = product.status || 'N/A';
    document.getElementById('productPrice').textContent = `$${formatPrice(product.priceAmount || 0)}`;
    document.getElementById('productDescription').textContent = product.description || 'No description available';
    setProductImage(product.imageUrl);
}

/**
 * Fetch stock status for a product
 * @param {number} productId - Product ID
 */
function fetchStockStatus(productId) {
    fetch(`/api/v1/inventory/products/${productId}/stock`)
        .then(response => response.ok ? response.json() : null)
        .then(data => {
            const stockElement = document.getElementById('productStockStatus');
            const addToCartBtn = document.getElementById('addToCartBtn');
            
            if (data && data.data) {
                const quantity = data.data.quantity || 0;
                const stockStatus = quantity > 0 ? 'In Stock' : 'Out of Stock';
                const stockClass = quantity > 0 ? 'stock-in' : 'stock-out';
                
                stockElement.textContent = stockStatus;
                stockElement.className = `product-details__stock-status ${stockClass}`;
                
                // Disable Add to Cart if out of stock
                if (quantity === 0) {
                    addToCartBtn.disabled = true;
                    addToCartBtn.textContent = 'Out of Stock';
                    addToCartBtn.classList.add('disabled');
                }
            } else {
                stockElement.textContent = 'Out of Stock';
                stockElement.className = 'product-details__stock-status stock-out';
                addToCartBtn.disabled = true;
                addToCartBtn.textContent = 'Out of Stock';
                addToCartBtn.classList.add('disabled');
            }
        })
        .catch(() => {
            const stockElement = document.getElementById('productStockStatus');
            const addToCartBtn = document.getElementById('addToCartBtn');
            
            stockElement.textContent = 'Stock info unavailable';
            stockElement.className = 'product-details__stock-status';
            addToCartBtn.disabled = true;
            addToCartBtn.classList.add('disabled');
        });
}

/**
 * Set the product image from URL or use placeholder
 * @param {string} imageUrl - The image URL
 */
function setProductImage(imageUrl) {
    const imageElement = document.getElementById('productImage');
    
    if (imageUrl) {
        imageElement.src = escapeHtml(imageUrl);
    } else {
        // SVG placeholder for missing images
        imageElement.src = 'data:image/svg+xml,%3Csvg xmlns="http://www.w3.org/2000/svg" width="400" height="400"%3E%3Crect fill="%23f5f5f5" width="400" height="400"/%3E%3Ctext x="50%" y="50%" text-anchor="middle" dy=".3em" fill="%23999" font-size="24"%3ENo Image%3C/text%3E%3C/svg%3E';
    }
}

/**
 * Show loading state by resetting all visibility states
 */
function showLoadingState(loadingSpinner, productDetails, emptyState, errorState) {
    loadingSpinner.style.display = 'block';
    productDetails.style.display = 'none';
    emptyState.style.display = 'none';
    errorState.style.display = 'none';
}

/**
 * Show error state with a custom message
 * @param {string} message - Error message to display
 */
function showError(message) {
    const errorState = document.getElementById('errorState');
    document.getElementById('errorMessage').textContent = message;
    errorState.style.display = 'block';
}

/**
 * Format price to 2 decimal places
 * @param {number} amount - The price amount
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
 * Setup Add to Cart button event listener
 */
function setupAddToCartButton() {
    const addToCartBtn = document.getElementById('addToCartBtn');
    const buyNowBtn = document.getElementById('buyNowBtn');
    const contentDiv = document.querySelector('[data-authenticated]');
    const isAuthenticated = contentDiv?.getAttribute('data-authenticated') === 'true';
    
    if (addToCartBtn) {
        addToCartBtn.addEventListener('click', function(e) {
            e.preventDefault();
            
            // Check if user is authenticated (via session or JWT)
            if (!isAuthenticated && !localStorage.getItem('jwtToken')) {
                window.location.href = '/login';
                return;
            }
            
            // If authenticated, add to cart
            const productId = extractProductIdFromUrl();
            addProductToCart(productId);
        });
    }
    
    if (buyNowBtn) {
        buyNowBtn.addEventListener('click', function(e) {
            e.preventDefault();
            
            // Check if user is authenticated (via session or JWT)
            if (!isAuthenticated && !localStorage.getItem('jwtToken')) {
                window.location.href = '/login';
                return;
            }
            
            // If authenticated, add to cart and redirect to checkout
            const productId = extractProductIdFromUrl();
            buyProduct(productId);
        });
    }
}

/**
 * Add product to cart via API
 * @param {number} productId - The product ID to add
 */
function addProductToCart(productId) {
    const addToCartBtn = document.getElementById('addToCartBtn');
    const originalText = addToCartBtn.textContent;
    
    // Disable button while processing
    addToCartBtn.disabled = true;
    addToCartBtn.textContent = 'Adding...';
    
    const token = localStorage.getItem('jwtToken');
    const headers = {
        'Content-Type': 'application/json'
    };
    
    // Add JWT token if available (for API authentication)
    if (token) {
        headers['Authorization'] = `Bearer ${token}`;
    }
    
    fetch('/api/v1/cart', {
        method: 'POST',
        headers: headers,
        body: JSON.stringify({
            productId: parseInt(productId),
            quantity: 1
        })
    })
    .then(response => {
        if (!response.ok) {
            return response.json().then(data => {
                throw new Error(data.message || 'Failed to add to cart');
            });
        }
        return response.json();
    })
    .then(data => {
        addToCartBtn.textContent = 'Added to Cart!';
        addToCartBtn.style.backgroundColor = '#28a745';
        
        // Reset after 2 seconds and redirect
        setTimeout(() => {
            window.location.href = '/cart';
        }, 1500);
    })
    .catch(error => {
        console.error('Error adding to cart:', error);
        addToCartBtn.disabled = false;
        addToCartBtn.textContent = originalText;
        alert('Error adding to cart: ' + error.message);
    });
}

/**
 * Buy product now - adds to cart and redirects to checkout
 * @param {number} productId - The product ID to buy
 */
function buyProduct(productId) {
    const buyNowBtn = document.getElementById('buyNowBtn');
    const originalText = buyNowBtn.textContent;
    
    // Disable button while processing
    buyNowBtn.disabled = true;
    buyNowBtn.textContent = 'Processing...';
    
    const token = localStorage.getItem('jwtToken');
    const headers = {
        'Content-Type': 'application/json'
    };
    
    if (token) {
        headers['Authorization'] = `Bearer ${token}`;
    }
    
    fetch('/api/v1/cart', {
        method: 'POST',
        headers: headers,
        body: JSON.stringify({
            productId: parseInt(productId),
            quantity: 1
        })
    })
    .then(response => {
        if (!response.ok) {
            return response.json().then(data => {
                throw new Error(data.message || 'Failed to add to cart');
            });
        }
        return response.json();
    })
    .then(data => {
        // Redirect to checkout
        window.location.href = '/checkout';
    })
    .catch(error => {
        console.error('Error during buy now:', error);
        buyNowBtn.disabled = false;
        buyNowBtn.textContent = originalText;
        alert('Error: ' + error.message);
    });
}