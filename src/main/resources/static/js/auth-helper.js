/**
 * Authentication Helper - Shared utilities for all seller pages
 */

/**
 * Fetch with JWT authentication token
 */
function fetchWithAuth(url, options = {}) {
    const token = localStorage.getItem('jwtToken');
    
    if (!options.headers) {
        options.headers = {};
    }
    
    if (token) {
        options.headers['Authorization'] = `Bearer ${token}`;
    }
    
    return fetch(url, options);
}

/**
 * Show alert message
 */
function showAlert(message, type = 'error') {
    const alertDiv = document.createElement('div');
    alertDiv.className = `alert alert-${type}`;
    alertDiv.textContent = message;
    const container = document.getElementById('alerts-container');
    if (container) {
        container.appendChild(alertDiv);
        setTimeout(() => alertDiv.remove(), 5000);
    } else {
        console.warn('Alert container not found, showing in console:', message);
    }
}

/**
 * Escape HTML special characters
 */
function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

/**
 * Get CSRF token from meta tag
 */
function getCsrfToken() {
    return document.querySelector('meta[name="csrf-token"]')?.content || '';
}

/**
 * Get CSRF header name
 */
function getCsrfHeaderName() {
    return document.querySelector('meta[name="csrf-header"]')?.content || 'X-CSRF-TOKEN';
}
