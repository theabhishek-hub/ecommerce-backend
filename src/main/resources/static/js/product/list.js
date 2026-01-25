/**
 * Product List - Placeholder JavaScript
 * Handles UI state management for product catalog
 * 
 * TODO: Implement actual API calls to /api/v1/products
 */

document.addEventListener('DOMContentLoaded', function() {
    initializeProductList();
});

/**
 * Initialize the product list view
 * Sets up UI elements and prepares for data loading
 */
function initializeProductList() {
    console.log('Product list initialized');
    fetchProducts();
}

/**
 * Fetch products from API and populate the grid
 */
function fetchProducts() {
    const loadingSpinner = document.getElementById('loadingSpinner');
    const productsGrid = document.getElementById('productsGrid');
    const emptyState = document.getElementById('emptyState');
    const errorState = document.getElementById('errorState');

    // Reset all states
    showLoadingState(loadingSpinner, productsGrid, emptyState, errorState);

    // Get search and sort parameters from URL
    const urlParams = new URLSearchParams(window.location.search);
    const searchQuery = urlParams.get('q') || '';
    const sortOption = urlParams.get('sort') || '';

    // Build API URL with query parameters
    // Use /filter endpoint which supports name, sort, and pagination
    let apiUrl = '/api/v1/products/filter';
    const queryParams = new URLSearchParams();
    
    // Add page and size for pagination
    queryParams.append('page', '0');
    queryParams.append('size', '100');
    
    // Map sort options to Spring Data Sort format
    if (sortOption) {
        if (sortOption === 'newest') {
            queryParams.append('sort', 'createdAt,desc');
        } else if (sortOption === 'oldest') {
            queryParams.append('sort', 'createdAt,asc');
        } else if (sortOption === 'price-low-to-high') {
            queryParams.append('sort', 'priceAmount,asc');
        } else if (sortOption === 'price-high-to-low') {
            queryParams.append('sort', 'priceAmount,desc');
        } else if (sortOption === 'name-asc') {
            queryParams.append('sort', 'name,asc');
        }
    } else {
        // Default sort by newest
        queryParams.append('sort', 'createdAt,desc');
    }
    
    if (searchQuery) {
        queryParams.append('name', searchQuery);
    }

    apiUrl += '?' + queryParams.toString();

    fetch(apiUrl)
        .then(response => {
            if (!response.ok) throw new Error('Failed to fetch products');
            return response.json();
        })
        .then(handleProductResponse)
        .catch(handleProductError);
}

/**
 * Handle successful product response from API
 * @param {Object} data - The response data
 */
function handleProductResponse(data) {
    const loadingSpinner = document.getElementById('loadingSpinner');
    const productsGrid = document.getElementById('productsGrid');
    const emptyState = document.getElementById('emptyState');

    loadingSpinner.style.display = 'none';

    // Handle paginated response format from /filter endpoint
    let products = [];
    
    if (data.data) {
        // Check if data.data is a pagination object with content property
        if (data.data.content) {
            products = data.data.content;
        } else if (Array.isArray(data.data)) {
            products = data.data;
        }
    } else if (data.content) {
        // Direct pagination object
        products = data.content;
    } else if (Array.isArray(data)) {
        products = data;
    }
    
    if (!Array.isArray(products) || products.length === 0) {
        emptyState.style.display = 'block';
        return;
    }
    
    renderProductGrid(products, productsGrid);
}

/**
 * Handle API error during product fetch
 * @param {Error} error - The error object
 */
function handleProductError(error) {
    console.error('Error fetching products:', error);
    const loadingSpinner = document.getElementById('loadingSpinner');
    const errorState = document.getElementById('errorState');

    loadingSpinner.style.display = 'none';
    errorState.style.display = 'block';
}

/**
 * Render products to the grid
 * @param {Array} products - Array of product objects
 * @param {HTMLElement} gridElement - The grid container element
 */
function renderProductGrid(products, gridElement) {
    gridElement.innerHTML = products.map(product => createProductCard(product)).join('');
    gridElement.style.display = 'grid';
}

/**
 * Create a single product card HTML element
 * @param {Object} product - Product object
 * @returns {string} HTML string for product card
 */
function createProductCard(product) {
    // Fetch stock status for the product
    const stockStatus = getStockStatus(product.id);
    
    return `
        <div class="product-list__card">
            <div class="product-list__image-wrapper">
                ${product.imageUrl ? 
                    `<img class="product-list__image" src="${escapeHtml(product.imageUrl)}" alt="${escapeHtml(product.name)}" />` : 
                    '<div class="product-list__no-image">No Image</div>'
                }
            </div>
            <div class="product-list__info">
                <h3 class="product-list__name">${escapeHtml(product.name)}</h3>
                <p class="product-list__description">${escapeHtml(product.description || 'No description')}</p>
                <div class="product-list__stock-info" id="stock-${product.id}">
                    <span class="product-list__stock-status">Loading...</span>
                </div>
                <div class="product-list__footer">
                    <span class="product-list__price">$${formatPrice(product.priceAmount || 0)}</span>
                    <a href="/products-page/${product.id}" class="product-list__btn-details">View Details</a>
                </div>
            </div>
        </div>
    `;
}

/**
 * Get stock status for a product
 * @param {number} productId - Product ID
 */
function getStockStatus(productId) {
    fetch(`/api/v1/inventory/products/${productId}/stock`)
        .then(response => response.ok ? response.json() : null)
        .then(data => {
            const stockElement = document.getElementById(`stock-${productId}`);
            if (stockElement && data && data.data) {
                const quantity = data.data.quantity || 0;
                const stockStatus = quantity > 0 ? 'In Stock' : 'Out of Stock';
                const stockClass = quantity > 0 ? 'in-stock' : 'out-of-stock';
                stockElement.innerHTML = `<span class="product-list__stock-status ${stockClass}">${stockStatus}</span>`;
            } else if (stockElement) {
                stockElement.innerHTML = '<span class="product-list__stock-status out-of-stock">Out of Stock</span>';
            }
        })
        .catch(() => {
            const stockElement = document.getElementById(`stock-${productId}`);
            if (stockElement) {
                stockElement.innerHTML = '<span class="product-list__stock-status">Stock info unavailable</span>';
            }
        });
}

/**
 * Show loading state by resetting all visibility states
 */
function showLoadingState(loadingSpinner, productsGrid, emptyState, errorState) {
    loadingSpinner.style.display = 'block';
    productsGrid.style.display = 'none';
    emptyState.style.display = 'none';
    errorState.style.display = 'none';
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
