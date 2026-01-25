/**
 * Seller Orders Management - Wire UI to APIs
 * Handles listing, confirming, shipping, and delivering orders
 */

/**
 * Load and display seller orders
 */
function loadOrders(status = '') {
    const container = document.getElementById('orders-container');
    if (!container) return;
    
    container.innerHTML = '<div class="loading">Loading orders...</div>';
    
    // Get current seller ID from page
    const sellerId = document.body.getAttribute('data-seller-id') || 
                     document.querySelector('[data-seller-id]')?.getAttribute('data-seller-id');
    
    if (!sellerId) {
        container.innerHTML = '<div class="error-message">Seller ID not found</div>';
        return;
    }
    
    // Build URL with filters
    let url = `/api/v1/orders?sellerId=${sellerId}&page=1&size=100`;
    if (status) {
        url += `&status=${status}`;
    }
    
    // Fetch orders for this seller
    fetchWithAuth(url, {
        method: 'GET',
        headers: {
            'Accept': 'application/json'
        }
    })
    .then(response => {
        if (!response.ok) throw new Error(`HTTP ${response.status}`);
        return response.json();
    })
    .then(data => {
        const orders = data.content || data || [];
        if (!orders.length) {
            document.getElementById('empty-state').style.display = 'block';
            container.innerHTML = '';
            return;
        }
        
        document.getElementById('empty-state').style.display = 'none';
        renderOrders(orders);
    })
    .catch(error => {
        console.error('Error loading orders:', error);
        showAlert('Failed to load orders: ' + error.message, 'error');
        container.innerHTML = '<div class="error-message">Error loading orders. Please refresh the page.</div>';
    });
}

/**
 * Render orders as cards
 */
function renderOrders(orders) {
    const container = document.getElementById('orders-container');
    let html = '';
    
    orders.forEach(order => {
        const orderDate = new Date(order.createdAt).toLocaleDateString();
        const statusBadge = getStatusBadge(order.orderStatus);
        const actionButtons = getActionButtons(order.id, order.orderStatus);

        html += `
            <div class="order-card">
                <div class="order-info">
                    <div class="order-info-label">Order ID</div>
                    <div class="order-info-value">#${order.id}</div>
                </div>
                <div class="order-info">
                    <div class="order-info-label">Customer</div>
                    <div class="order-info-value">${order.user?.fullName || 'N/A'}</div>
                </div>
                <div>
                    ${statusBadge}
                </div>
                <div class="order-info">
                    <div class="order-info-label">Amount</div>
                    <div class="order-amount">$${parseFloat(order.totalAmount || 0).toFixed(2)}</div>
                </div>
                <div class="order-actions">
                    ${actionButtons}
                </div>
            </div>
        `;
    });
    container.innerHTML = html;
}

/**
 * Get status badge HTML
 */
function getStatusBadge(status) {
    const badges = {
        'PENDING': { color: '#fff3cd', text: '#856404' },
        'CONFIRMED': { color: '#cfe2ff', text: '#084298' },
        'SHIPPED': { color: '#d1ecf1', text: '#0c5460' },
        'DELIVERED': { color: '#d4edda', text: '#155724' },
        'CANCELLED': { color: '#f8d7da', text: '#721c24' }
    };
    
    const badge = badges[status] || { color: '#e2e3e5', text: '#383d41' };
    
    return `<div style="display: inline-block; background: ${badge.color}; color: ${badge.text}; padding: 6px 12px; border-radius: 4px; font-size: 12px; font-weight: 600;">${status}</div>`;
}

/**
 * Get action buttons based on order status
 */
function getActionButtons(orderId, status) {
    let buttons = `
        <button onclick="viewOrderDetails(${orderId})" class="btn btn-primary btn-sm">👁️ View</button>
    `;
    
    if (status === 'PENDING') {
        buttons += `<button onclick="confirmOrder(${orderId})" class="btn btn-success btn-sm">✓ Confirm</button>`;
    } else if (status === 'CONFIRMED') {
        buttons += `<button onclick="shipOrder(${orderId})" class="btn btn-info btn-sm">📦 Ship</button>`;
    } else if (status === 'SHIPPED') {
        buttons += `<button onclick="deliverOrder(${orderId})" class="btn btn-success btn-sm">🎉 Deliver</button>`;
    }
    
    return buttons;
}

/**
 * View order details
 */
function viewOrderDetails(orderId) {
    fetchWithAuth(`/api/v1/orders/${orderId}`, {
        method: 'GET',
        headers: { 'Accept': 'application/json' }
    })
    .then(response => {
        if (!response.ok) throw new Error(`HTTP ${response.status}`);
        return response.json();
    })
    .then(order => {
        alert(`Order #${order.id}\nStatus: ${order.orderStatus}\nTotal: $${parseFloat(order.totalAmount || 0).toFixed(2)}`);
    })
    .catch(error => {
        console.error('Error loading order:', error);
        showAlert('Failed to load order details', 'error');
    });
}

/**
 * Confirm order
 */
function confirmOrder(orderId) {
    if (!confirm('Confirm this order?')) {
        return;
    }
    
    fetchWithAuth(`/api/v1/orders/${orderId}/confirm`, {
        method: 'POST',
        headers: { 'Accept': 'application/json' }
    })
    .then(response => {
        if (!response.ok) throw new Error(`HTTP ${response.status}`);
        showAlert('Order confirmed', 'success');
        loadOrders();
    })
    .catch(error => {
        console.error('Error confirming order:', error);
        showAlert('Failed to confirm order: ' + error.message, 'error');
    });
}

/**
 * Ship order
 */
function shipOrder(orderId) {
    if (!confirm('Mark order as shipped?')) {
        return;
    }
    
    fetchWithAuth(`/api/v1/orders/${orderId}/ship`, {
        method: 'POST',
        headers: { 'Accept': 'application/json' }
    })
    .then(response => {
        if (!response.ok) throw new Error(`HTTP ${response.status}`);
        showAlert('Order marked as shipped', 'success');
        loadOrders();
    })
    .catch(error => {
        console.error('Error shipping order:', error);
        showAlert('Failed to ship order: ' + error.message, 'error');
    });
}

/**
 * Deliver order
 */
function deliverOrder(orderId) {
    if (!confirm('Mark order as delivered?')) {
        return;
    }
    
    fetchWithAuth(`/api/v1/orders/${orderId}/deliver`, {
        method: 'POST',
        headers: { 'Accept': 'application/json' }
    })
    .then(response => {
        if (!response.ok) throw new Error(`HTTP ${response.status}`);
        showAlert('Order marked as delivered', 'success');
        loadOrders();
    })
    .catch(error => {
        console.error('Error delivering order:', error);
        showAlert('Failed to deliver order: ' + error.message, 'error');
    });
}

/**
 * Filter orders by status
 */
function filterOrdersByStatus(status) {
    loadOrders(status);
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
    }
}
