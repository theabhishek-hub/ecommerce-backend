/**
 * Product Details - Placeholder JavaScript
 * Handles UI state management for product detail view
 * 
 * TODO: Implement actual API calls to /api/v1/products/{id}
 */

document.addEventListener('DOMContentLoaded', function() {
    initializeProductDetails();
});

/**
 * Initialize the product details view
 * Extracts product ID from URL and prepares for data loading
 */
function initializeProductDetails() {
    const productId = extractProductIdFromUrl();
    
    if (productId && !isNaN(productId)) {
        console.log('Product details initialized for product ID:', productId);
        // TODO: Call fetchProductDetails(productId) when API integration is ready
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
 * TODO: Replace with actual API endpoint call
 */
function fetchProductDetails(productId) {
    const loadingSpinner = document.getElementById('loadingSpinner');
    const productDetails = document.getElementById('productDetails');
    const emptyState = document.getElementById('emptyState');
    const errorState = document.getElementById('errorState');

    // Reset all states
    showLoadingState(loadingSpinner, productDetails, emptyState, errorState);

    // TODO: Implement API call
    // Example structure:
    // fetch(`/api/v1/products/${productId}`)
    //     .then(handleProductDetailResponse)
    //     .catch(handleProductDetailError)
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
    // TODO: Implement details population
    // Example:
    // document.getElementById('productName').textContent = escapeHtml(product.name || '');
    // document.getElementById('productSku').textContent = escapeHtml(product.sku || 'N/A');
    // document.getElementById('productBrand').textContent = escapeHtml(product.brand?.name || 'N/A');
    // document.getElementById('productCategory').textContent = escapeHtml(product.category?.name || 'N/A');
    // document.getElementById('productStatus').textContent = escapeHtml(product.status || 'N/A');
    // document.getElementById('productPrice').textContent = `$${formatPrice(product.price?.amount || 0)}`;
    // document.getElementById('productDescription').textContent = escapeHtml(product.description || 'No description available');
    // setProductImage(product.imageUrl);
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
