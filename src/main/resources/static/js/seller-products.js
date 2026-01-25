/**
 * Seller Products Management - Wire UI to APIs
 * Handles CRUD operations on seller products with modal dialog
 */

let currentEditProductId = null;

/**
 * Load and display seller products
 */
function loadProducts() {
    const container = document.getElementById('products-container');
    if (!container) return;
    
    container.innerHTML = '<div class="loading">Loading products...</div>';
    
    // Get current seller ID from page
    const sellerId = document.body.getAttribute('data-seller-id') || 
                     document.querySelector('[data-seller-id]')?.getAttribute('data-seller-id');
    
    if (!sellerId) {
        container.innerHTML = '<div class="error-message">Seller ID not found</div>';
        return;
    }
    
    // Fetch products for this seller
    fetchWithAuth(`/api/v1/products?sellerId=${sellerId}&page=1&size=100`, {
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
        renderProducts(products);
    })
    .catch(error => {
        console.error('Error loading products:', error);
        showAlert('Failed to load products: ' + error.message, 'error');
        container.innerHTML = '<div class="error-message">Error loading products. Please refresh the page.</div>';
    });
}

/**
 * Render products as cards
 */
function renderProducts(products) {
    const container = document.getElementById('products-container');
    let html = '';
    
    products.forEach(product => {
        const stockQty = product.stockQuantity || 0;
        const stockLabel = stockQty > 10 ? 'HIGH' : (stockQty > 0 ? 'LOW' : 'OUT');
        const stockColor = stockQty > 10 ? '#d4edda' : (stockQty > 0 ? '#fff3cd' : '#f8d7da');
        const stockColorText = stockQty > 10 ? '#155724' : (stockQty > 0 ? '#856404' : '#721c24');
        
        const statusColor = product.status === 'ACTIVE' ? '#d4edda' : '#f8d7da';
        const statusTextColor = product.status === 'ACTIVE' ? '#155724' : '#721c24';
        
        html += `
            <div class="product-card">
                <div class="product-header">
                    <h4 class="product-name">${escapeHtml(product.name)}</h4>
                    <p class="product-sku">SKU: ${escapeHtml(product.sku)}</p>
                </div>
                <div style="padding: 15px;">
                    <div class="product-price">$${parseFloat(product.priceAmount || 0).toFixed(2)}</div>
                    <div style="display: flex; gap: 8px; margin-bottom: 12px; flex-wrap: wrap;">
                        <span class="badge" style="background: ${stockColor}; color: ${stockColorText}; padding: 6px 12px; border-radius: 4px; font-size: 11px; font-weight: 600;">
                            ${stockQty} units (${stockLabel})
                        </span>
                        <span class="badge" style="background: ${statusColor}; color: ${statusTextColor}; padding: 6px 12px; border-radius: 4px; font-size: 11px; font-weight: 600;">
                            ${product.status}
                        </span>
                    </div>
                    <div style="font-size: 13px; color: #666; margin-bottom: 12px; line-height: 1.4; max-height: 60px; overflow: hidden;">
                        ${escapeHtml(product.description || 'No description')}
                    </div>
                </div>
                <div style="padding: 12px 15px; border-top: 1px solid #eee; display: flex; gap: 8px;">
                    <button onclick="openEditProductModal(${product.id})" class="btn btn-primary btn-sm" style="flex: 1;">✏️ Edit</button>
                    <button onclick="deleteProduct(${product.id})" class="btn btn-danger btn-sm" style="flex: 1;">🗑️ Delete</button>
                </div>
            </div>
        `;
    });
    container.innerHTML = html;
}

/**
 * Open add product modal
 */
function openAddProductModal() {
    currentEditProductId = null;
    document.getElementById('modalTitle').textContent = 'Add Product';
    document.getElementById('productForm').reset();
    document.getElementById('productForm').dataset.mode = 'create';
    document.getElementById('productModal').style.display = 'flex';
}

/**
 * Open edit product modal
 */
function openEditProductModal(productId) {
    // Load product details
    fetchWithAuth(`/api/v1/products/${productId}`, {
        method: 'GET',
        headers: { 'Accept': 'application/json' }
    })
    .then(response => {
        if (!response.ok) throw new Error(`HTTP ${response.status}`);
        return response.json();
    })
    .then(product => {
        currentEditProductId = productId;
        const form = document.getElementById('productForm');
        
        // Populate form with product data
        form.querySelector('[name="name"]').value = product.name || '';
        form.querySelector('[name="description"]').value = product.description || '';
        form.querySelector('[name="priceAmount"]').value = product.priceAmount || '';
        form.querySelector('[name="currency"]').value = product.currency || 'USD';
        form.querySelector('[name="sku"]').value = product.sku || '';
        form.querySelector('[name="categoryId"]').value = product.categoryId || '';
        form.querySelector('[name="brandId"]').value = product.brandId || '';

        document.getElementById('modalTitle').textContent = 'Edit Product';
        form.dataset.mode = 'edit';
        document.getElementById('productModal').style.display = 'flex';
    })
    .catch(error => {
        console.error('Error loading product:', error);
        showAlert('Failed to load product details', 'error');
    });
}

/**
 * Close product modal
 */
function closeProductModal() {
    document.getElementById('productModal').style.display = 'none';
    currentEditProductId = null;
}

/**
 * Submit product form (create or update)
 */
function submitProduct(event) {
    event.preventDefault();

    const form = event.target;
    const formData = new FormData(form);

    const productData = {
        name: formData.get('name'),
        description: formData.get('description'),
        priceAmount: parseFloat(formData.get('priceAmount')),
        currency: formData.get('currency') || 'USD',
        sku: formData.get('sku'),
        categoryId: formData.get('categoryId') ? parseInt(formData.get('categoryId')) : null,
        brandId: formData.get('brandId') ? parseInt(formData.get('brandId')) : null
    };

    // Validation
    if (!productData.name || !productData.sku || !productData.priceAmount) {
        showAlert('Please fill in all required fields', 'error');
        return;
    }

    if (productData.priceAmount <= 0) {
        showAlert('Price must be greater than 0', 'error');
        return;
    }

    const method = currentEditProductId ? 'PUT' : 'POST';
    const url = `/api/v1/products${currentEditProductId ? '/' + currentEditProductId : ''}`;

    fetchWithAuth(url, {
        method: method,
        headers: {
            'Content-Type': 'application/json',
            'Accept': 'application/json'
        },
        body: JSON.stringify(productData)
    })
    .then(response => {
        if (!response.ok) throw new Error(`HTTP ${response.status}`);
        return response.json();
    })
    .then(product => {
        showAlert(currentEditProductId ? 'Product updated successfully' : 'Product created successfully', 'success');
        closeProductModal();
        loadProducts();
    })
    .catch(error => {
        console.error('Error saving product:', error);
        showAlert('Failed to save product: ' + error.message, 'error');
    });
}

/**
 * Delete product
 */
function deleteProduct(productId) {
    if (!confirm('Are you sure you want to delete this product?')) {
        return;
    }

    fetchWithAuth(`/api/v1/products/${productId}`, {
        method: 'DELETE',
        headers: { 'Accept': 'application/json' }
    })
    .then(response => {
        if (!response.ok) throw new Error(`HTTP ${response.status}`);
        showAlert('Product deleted successfully', 'success');
        loadProducts();
    })
    .catch(error => {
        console.error('Error deleting product:', error);
        showAlert('Failed to delete product: ' + error.message, 'error');
    });
}

/**
 * Load categories for dropdown
 */
function loadCategories() {
    fetchWithAuth('/api/v1/categories', {
        method: 'GET',
        headers: { 'Accept': 'application/json' }
    })
    .then(response => {
        if (!response.ok) throw new Error(`HTTP ${response.status}`);
        return response.json();
    })
    .then(categories => {
        const select = document.getElementById('productCategory');
        const data = categories.content || categories || [];
        data.forEach(cat => {
            const option = document.createElement('option');
            option.value = cat.id;
            option.textContent = cat.name;
            select.appendChild(option);
        });
    })
    .catch(error => console.error('Error loading categories:', error));
}

/**
 * Load brands for dropdown
 */
function loadBrands() {
    fetchWithAuth('/api/v1/brands', {
        method: 'GET',
        headers: { 'Accept': 'application/json' }
    })
    .then(response => {
        if (!response.ok) throw new Error(`HTTP ${response.status}`);
        return response.json();
    })
    .then(brands => {
        const select = document.getElementById('productBrand');
        const data = brands.content || brands || [];
        data.forEach(brand => {
            const option = document.createElement('option');
            option.value = brand.id;
            option.textContent = brand.name;
            select.appendChild(option);
        });
    })
    .catch(error => console.error('Error loading brands:', error));
}

/**
 * Escape HTML special characters
 */
function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}
