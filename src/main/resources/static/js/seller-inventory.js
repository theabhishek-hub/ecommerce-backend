/**
 * Seller Inventory Management - Wire UI to APIs
 * Handles stock quantity updates and inventory tracking
 */

/**
 * Load and display seller inventory
 */
function loadInventory(search = '') {
    const container = document.getElementById('inventory-container');
    if (!container) return;
    
    container.innerHTML = '<div class="loading">Loading inventory...</div>';
    
    // Get current seller ID from page
    const sellerId = document.body.getAttribute('data-seller-id') || 
                     document.querySelector('[data-seller-id]')?.getAttribute('data-seller-id');
    
    if (!sellerId) {
        container.innerHTML = '<div class="error-message">Seller ID not found</div>';
        return;
    }
    
    // Build URL with filters
    let url = `/api/v1/products?sellerId=${sellerId}&page=1&size=100`;
    if (search) {
        url += `&search=${encodeURIComponent(search)}`;
    }
    
    // Fetch inventory for this seller
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
        const products = data.content || data || [];
        if (!products.length) {
            document.getElementById('empty-state').style.display = 'block';
            container.innerHTML = '';
            return;
        }
        
        document.getElementById('empty-state').style.display = 'none';
        renderInventory(products);
    })
    .catch(error => {
        console.error('Error loading inventory:', error);
        showAlert('Failed to load inventory: ' + error.message, 'error');
        container.innerHTML = '<div class="error-message">Error loading inventory. Please refresh the page.</div>';
    });
}

/**
 * Render inventory as table
 */
function renderInventory(products) {
    const container = document.getElementById('inventory-container');
    let html = '<table style="width: 100%; border-collapse: collapse; margin-top: 20px;">';
    
    html += `
        <thead>
            <tr style="background: #f5f5f5; border-bottom: 2px solid #ddd;">
                <th style="padding: 12px; text-align: left; font-weight: 600;">Product Name</th>
                <th style="padding: 12px; text-align: center; font-weight: 600;">SKU</th>
                <th style="padding: 12px; text-align: right; font-weight: 600;">Current Stock</th>
                <th style="padding: 12px; text-align: center; font-weight: 600;">Status</th>
                <th style="padding: 12px; text-align: center; font-weight: 600;">Action</th>
            </tr>
        </thead>
        <tbody>
    `;
    
    products.forEach((product, index) => {
        const bgColor = index % 2 === 0 ? '#fff' : '#f9f9f9';
        const stockQty = product.stockQuantity || 0;
        const stockColor = stockQty > 10 ? '#28a745' : (stockQty > 0 ? '#ffc107' : '#dc3545');
        const statusColor = product.status === 'ACTIVE' ? '#28a745' : '#dc3545';
        
        html += `
            <tr style="background: ${bgColor}; border-bottom: 1px solid #eee;">
                <td style="padding: 12px; text-align: left;">
                    <strong>${escapeHtml(product.name)}</strong>
                </td>
                <td style="padding: 12px; text-align: center; color: #666;">
                    ${escapeHtml(product.sku)}
                </td>
                <td style="padding: 12px; text-align: right;">
                    <span style="color: ${stockColor}; font-weight: 600; font-size: 16px;">
                        ${stockQty}
                    </span>
                </td>
                <td style="padding: 12px; text-align: center;">
                    <span style="background: ${statusColor}; color: white; padding: 4px 8px; border-radius: 4px; font-size: 12px; font-weight: 600;">
                        ${product.status}
                    </span>
                </td>
                <td style="padding: 12px; text-align: center;">
                    <button onclick="openUpdateStockModal(${product.id}, '${escapeHtml(product.name)}', ${stockQty})" class="btn btn-primary btn-sm">Update Stock</button>
                </td>
            </tr>
        `;
    });
    
    html += `
        </tbody>
    </table>
    `;
    
    container.innerHTML = html;
}

/**
 * Open update stock modal
 */
function openUpdateStockModal(productId, productName, currentStock) {
    const modal = document.getElementById('stockModal');
    if (!modal) {
        console.error('Stock modal not found');
        return;
    }
    
    document.getElementById('modalProductName').textContent = productName;
    document.getElementById('currentStock').textContent = currentStock;
    document.getElementById('newStock').value = currentStock;
    document.getElementById('newStock').dataset.productId = productId;
    
    modal.style.display = 'flex';
}

/**
 * Close stock modal
 */
function closeStockModal() {
    const modal = document.getElementById('stockModal');
    if (modal) {
        modal.style.display = 'none';
    }
}

/**
 * Update product stock
 */
function updateStock(event) {
    event.preventDefault();
    
    const newStockInput = document.getElementById('newStock');
    const productId = parseInt(newStockInput.dataset.productId);
    const newStock = parseInt(newStockInput.value);
    
    if (isNaN(newStock) || newStock < 0) {
        showAlert('Please enter a valid stock quantity', 'error');
        return;
    }
    
    const stockData = {
        stockQuantity: newStock
    };
    
    fetchWithAuth(`/api/v1/products/${productId}`, {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json',
            'Accept': 'application/json'
        },
        body: JSON.stringify(stockData)
    })
    .then(response => {
        if (!response.ok) throw new Error(`HTTP ${response.status}`);
        return response.json();
    })
    .then(product => {
        showAlert('Stock updated successfully', 'success');
        closeStockModal();
        loadInventory();
    })
    .catch(error => {
        console.error('Error updating stock:', error);
        showAlert('Failed to update stock: ' + error.message, 'error');
    });
}

/**
 * Search inventory
 */
function searchInventory() {
    const search = document.getElementById('searchInput').value;
    loadInventory(search);
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
