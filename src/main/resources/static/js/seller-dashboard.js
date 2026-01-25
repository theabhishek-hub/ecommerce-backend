/**
 * Seller Dashboard - Load and Display Metrics
 * Wires dashboard HTML to API endpoints
 */

/**
 * Load all dashboard metrics from server-side data
 */
function loadDashboardMetrics() {
    // Check if server-side data exists
    if (window.dashboardData) {
        console.log('Loading metrics from server data:', window.dashboardData);
        
        // Update product metrics
        if (window.dashboardData.totalProducts !== undefined) {
            updateMetricValue('total-products', window.dashboardData.totalProducts);
        }
        
        // Update order metrics
        if (window.dashboardData.totalOrders !== undefined) {
            updateMetricValue('total-orders', window.dashboardData.totalOrders);
        }
        
        // Update pending orders
        if (window.dashboardData.pendingOrders !== undefined) {
            updateMetricValue('pending-orders', window.dashboardData.pendingOrders);
        }
        
        // Update revenue
        if (window.dashboardData.totalRevenue) {
            updateMetricValue('total-revenue', window.dashboardData.totalRevenue);
        }
    } else {
        console.warn('Server data not available');
    }
}


/**
 * Update metric value in DOM
 */
function updateMetricValue(elementId, value) {
    const el = document.getElementById(elementId);
    if (el) {
        el.innerHTML = `<strong>${value}</strong>`;
    }
}

/**
 * Update metric with error state
 */
function updateMetricError(elementId, value) {
    const el = document.getElementById(elementId);
    if (el) {
        el.innerHTML = `<span style="color: #999;">${value}</span>`;
    }
}

/**
 * Fetch with CSRF token
 */
function fetchWithAuth(url, options = {}) {
    const token = document.querySelector('meta[name="csrf-token"]')?.getAttribute('content');
    const header = document.querySelector('meta[name="csrf-header"]')?.getAttribute('content');
    
    const headers = { ...options.headers };
    if (token && header) {
        headers[header] = token;
    }
    
    return fetch(url, { ...options, headers });
}
