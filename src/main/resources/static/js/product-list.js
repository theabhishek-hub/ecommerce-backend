/**
 * Product List - JavaScript
 * Handles product catalog display, filtering, and actions
 */

let currentPage = 0;
const pageSize = 12;
let currentQuery = '';
let currentSort = 'createdAt,desc';

document.addEventListener('DOMContentLoaded', function() {
    initializeProductList();
    // Auto-fetch products when page loads
    fetchProducts();
    
    // Setup search form listener
    const searchForm = document.querySelector('.search-sort-bar form');
    if (searchForm) {
        searchForm.addEventListener('submit', function(e) {
            e.preventDefault();
            currentPage = 0;
            currentQuery = document.querySelector('input[name="q"]').value;
            currentSort = document.querySelector('select[name="sort"]').value || 'createdAt,desc';
            fetchProducts();
        });
    }
});

/**
 * Initialize the product list view
 */
function initializeProductList() {
    console.log('Product list initialized');
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

    // Build query parameters
    let url = `/api/v1/products?page=${currentPage}&size=${pageSize}&sort=${currentSort}`;
    if (currentQuery) {
        url += `&q=${encodeURIComponent(currentQuery)}`;
    }

    fetch(url, {
        method: 'GET',
        headers: {
            'Accept': 'application/json',
            'Content-Type': 'application/json'
        }
    })
    .then(response => {
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        return response.json();
    })
    .then(data => handleProductResponse(data, loadingSpinner, productsGrid, emptyState))
    .catch(error => handleProductError(error, loadingSpinner, errorState));
}

/**
 * Handle successful product response from API
 */
function handleProductResponse(data, loadingSpinner, productsGrid, emptyState) {
    loadingSpinner.style.display = 'none';

    // Extract products from response wrapper
    const products = data.data || data.content || [];
    
    if (products.length === 0) {
        emptyState.style.display = 'block';
        return;
    }
    
    renderProductGrid(products, productsGrid);
}

/**
 * Handle API error during product fetch
 */
function handleProductError(error, loadingSpinner, errorState) {
    console.error('Error fetching products:', error);
    loadingSpinner.style.display = 'none';
    errorState.style.display = 'block';
}

/**
 * Render products to the grid
 */
function renderProductGrid(products, gridElement) {
    gridElement.innerHTML = products.map(product => createProductCard(product)).join('');
    gridElement.style.display = 'grid';
}

/**
 * Create a single product card HTML element
 */
function createProductCard(product) {
    const priceAmount = product.price?.amount || product.priceAmount || 0;
    const currency = product.price?.currency || product.currency || 'USD';
    const imageUrl = product.imageUrl || '/images/placeholder.jpg';
    const sellerName = product.seller?.companyName || product.seller?.name || 'Official Store';
    
    return `
        <div class="product-card">
            <div class="product-card-image-wrapper">
                <img src="${escapeHtml(imageUrl)}" alt="${escapeHtml(product.name)}" class="product-card-image" onerror="this.src='/images/placeholder.jpg'">
            </div>
            <div class="product-card-info">
                <h3 class="product-card-title">${escapeHtml(product.name)}</h3>
                <p class="product-card-seller">by ${escapeHtml(sellerName)}</p>
                <div class="product-card-rating">
                    <span class="rating-stars">★★★★★</span>
                </div>
                <div class="product-card-price">
                    <span class="price-value">${currency} ${formatPrice(priceAmount)}</span>
                </div>
                <div class="product-card-actions">
                    <a href="/product/${product.id}" class="btn-details">View Details</a>
                    <button class="btn-cart" onclick="addToCart(${product.id})">🛒 Add to Cart</button>
                </div>
            </div>
        </div>
    `;
}

/**
 * Add product to cart
 */
function addToCart(productId) {
    fetch(`/api/v1/cart/items`, {
        method: 'POST',
        headers: {
            'Accept': 'application/json',
            'Content-Type': 'application/json',
            'X-CSRF-TOKEN': getCSRFToken()
        },
        body: JSON.stringify({
            productId: productId,
            quantity: 1
        })
    })
    .then(response => {
        if (!response.ok) {
            if (response.status === 401) {
                alert('Please login first to add items to cart');
                window.location.href = '/login';
            } else {
                throw new Error('Failed to add to cart');
            }
        }
        return response.json();
    })
    .then(data => {
        alert('Product added to cart!');
    })
    .catch(error => {
        console.error('Error adding to cart:', error);
        alert('Failed to add product to cart. Please try again.');
    });
}

/**
 * Get CSRF token from page or cookie
 */
function getCSRFToken() {
    const name = '_csrf=';
    const decodedCookie = decodeURIComponent(document.cookie);
    const cookieArray = decodedCookie.split(';');
    for (let cookie of cookieArray) {
        cookie = cookie.trim();
        if (cookie.indexOf(name) === 0) {
            return cookie.substring(name.length);
        }
    }
    // Try to get from meta tag
    const token = document.querySelector('meta[name="_csrf"]');
    return token ? token.getAttribute('content') : '';
}

/**
 * Show loading state
 */
function showLoadingState(loadingSpinner, productsGrid, emptyState, errorState) {
    loadingSpinner.style.display = 'block';
    productsGrid.style.display = 'none';
    emptyState.style.display = 'none';
    errorState.style.display = 'none';
}

/**
 * Format price to 2 decimal places
 */
function formatPrice(amount) {
    return parseFloat(amount).toFixed(2);
}

/**
 * Escape HTML special characters to prevent XSS
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

