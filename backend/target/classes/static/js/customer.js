// Customer-facing JavaScript for Restaurant QR Ordering System

// Global state
let cart = {
    items: [],
    tableToken: '',
    tableNumber: '',
    tableName: ''
};

const TAX_RATE = 5.0;

// DOM Elements
const cartBtn = document.getElementById('cartBtn');
const cartCount = document.getElementById('cartCount');
const cartModal = document.getElementById('cartModal');
const cartModalBody = document.getElementById('cartModalBody');
const cartModalFooter = document.getElementById('cartModalFooter');
const emptyCart = document.getElementById('emptyCart');
const cartItemsContainer = document.getElementById('cartItemsContainer');
const cartSubtotal = document.getElementById('cartSubtotal');
const cartTax = document.getElementById('cartTax');
const cartTotal = document.getElementById('cartTotal');
const checkoutBtn = document.getElementById('checkoutBtn');
const searchInput = document.getElementById('searchInput');
const checkoutModal = document.getElementById('checkoutModal');
const checkoutForm = document.getElementById('checkoutForm');
const proceedToPaymentBtn = document.getElementById('proceedToPaymentBtn');
const loadingModal = document.getElementById('loadingModal');
const loadingText = document.getElementById('loadingText');

// Initialize
document.addEventListener('DOMContentLoaded', function() {
    initCart();
    loadMenu();
    setupEventListeners();
    loadTableInfo();
});

function initCart() {
    const savedCart = localStorage.getItem('restaurant_cart_' + TABLE_TOKEN);
    if (savedCart) {
        cart = JSON.parse(savedCart);
        updateCartUI();
    }
}

function saveCart() {
    localStorage.setItem('restaurant_cart_' + TABLE_TOKEN, JSON.stringify(cart));
}

function setupEventListeners() {
    // Search
    if (searchInput) {
        searchInput.addEventListener('input', debounce(handleSearch, 300));
    }

    // Cart button
    if (cartBtn) {
        cartBtn.addEventListener('click', () => updateCartModal());
    }

    // Checkout button
    if (checkoutBtn) {
        checkoutBtn.addEventListener('click', openCheckoutModal);
    }

    // Checkout form
    if (checkoutForm) {
        checkoutForm.addEventListener('submit', handleCheckout);
    }

    // Proceed to payment
    if (proceedToPaymentBtn) {
        proceedToPaymentBtn.addEventListener('click', proceedToPayment);
    }

    // Cart modal events
    if (cartModal) {
        cartModal.addEventListener('shown.bs.modal', updateCartModal);
    }
}

async function loadTableInfo() {
    if (!TABLE_TOKEN) return;
    
    try {
        const response = await axios.get(`${API_BASE_URL}/menu/table/${TABLE_TOKEN}`);
        if (response.data.success && response.data.data) {
            cart.tableToken = TABLE_TOKEN;
            cart.tableNumber = response.data.data.tableNumber;
            cart.tableName = response.data.data.tableName;
            saveCart();
        }
    } catch (error) {
        console.error('Failed to load table info:', error);
    }
}

async function loadMenu() {
    try {
        const response = await axios.get(`${API_BASE_URL}/menu/items`);
        if (response.data.success && response.data.data) {
            renderMenu(response.data.data);
        }
    } catch (error) {
        console.error('Failed to load menu:', error);
        showToast('Failed to load menu', 'error');
    }
}

function renderMenu(items) {
    // Group by category
    const categories = {};
    items.forEach(item => {
        const catName = item.categoryName || 'Other';
        if (!categories[catName]) {
            categories[catName] = [];
        }
        categories[catName].push(item);
    });

    // Render All tab
    const allContainer = document.getElementById('allItemsContainer');
    if (allContainer) {
        allContainer.innerHTML = items.map(item => createMenuItemCard(item)).join('');
    }

    // Render category tabs
    Object.keys(categories).forEach(catName => {
        const container = document.getElementById(catName.replace(/\s+/g, '') + 'Items');
        if (container) {
            container.innerHTML = categories[catName].map(item => createMenuItemCard(item)).join('');
        }
    });

    // Attach add to cart events
    document.querySelectorAll('.add-to-cart-btn').forEach(btn => {
        btn.addEventListener('click', (e) => {
            const itemId = e.target.closest('.add-to-cart-btn').dataset.itemId;
            const item = items.find(i => i.id == itemId);
            if (item) addToCart(item);
        });
    });
}

function createMenuItemCard(item) {
    const vegIcon = item.foodType === 'VEG' || item.foodType === 'VEGAN' 
        ? '<i class="bi bi-leaf text-success me-1" title="Vegetarian"></i>' 
        : '<i class="bi bi-circle text-danger me-1" title="Non-Vegetarian"></i>';
    
    return `
        <div class="col-12 col-sm-6 col-md-4 col-lg-3">
            <div class="card menu-item-card h-100 shadow-sm">
                ${item.imageUrl ? `
                <img src="${item.imageUrl}" class="card-img-top menu-item-image" alt="${item.name}" 
                     onerror="this.style.display='none'">
                ` : `
                <div class="card-img-top menu-item-image-placeholder d-flex align-items-center justify-content-center bg-light">
                    <i class="bi bi-egg-fried display-4 text-muted"></i>
                </div>
                `}
                <div class="card-body d-flex flex-column">
                    <div class="d-flex justify-content-between align-items-start mb-2">
                        <h6 class="card-title mb-0">${vegIcon}${item.name}</h6>
                    </div>
                    <p class="card-text text-muted small flex-grow-1">${item.description || ''}</p>
                    <div class="d-flex justify-content-between align-items-center mt-3">
                        <span class="fw-bold fs-5 text-primary">₹${item.price.toFixed(2)}</span>
                        <button class="btn btn-primary btn-sm add-to-cart-btn" data-item-id="${item.id}">
                            <i class="bi bi-plus-lg me-1"></i>Add
                        </button>
                    </div>
                    <div class="mt-2">
                        <small class="text-muted">
                            <i class="bi bi-clock me-1"></i>${item.preparationTimeMinutes || 15} min
                        </small>
                    </div>
                </div>
            </div>
        </div>
    `;
}

function handleSearch(e) {
    const query = e.target.value.trim().toLowerCase();
    const cards = document.querySelectorAll('.menu-item-card');
    
    cards.forEach(card => {
        const name = card.querySelector('.card-title').textContent.toLowerCase();
        const desc = card.querySelector('.card-text').textContent.toLowerCase();
        if (name.includes(query) || desc.includes(query)) {
            card.closest('.col-12').style.display = '';
        } else {
            card.closest('.col-12').style.display = 'none';
        }
    });
}

function addToCart(menuItem) {
    const existingItem = cart.items.find(item => item.menuItemId === menuItem.id);
    
    if (existingItem) {
        existingItem.quantity += 1;
    } else {
        cart.items.push({
            menuItemId: menuItem.id,
            itemName: menuItem.name,
            description: menuItem.description,
            unitPrice: menuItem.price,
            quantity: 1,
            foodType: menuItem.foodType,
            imageUrl: menuItem.imageUrl
        });
    }
    
    saveCart();
    updateCartUI();
    showToast(`${menuItem.name} added to cart`, 'success');
    
    // Animate cart button
    cartBtn.classList.add('animate__animated', 'animate__pulse');
    setTimeout(() => cartBtn.classList.remove('animate__animated', 'animate__pulse'), 500);
}

function removeFromCart(menuItemId) {
    cart.items = cart.items.filter(item => item.menuItemId !== menuItemId);
    saveCart();
    updateCartUI();
    updateCartModal();
}

function updateQuantity(menuItemId, change) {
    const item = cart.items.find(item => item.menuItemId === menuItemId);
    if (item) {
        item.quantity += change;
        if (item.quantity <= 0) {
            removeFromCart(menuItemId);
        } else {
            saveCart();
            updateCartUI();
            updateCartModal();
        }
    }
}

function updateCartUI() {
    const totalItems = cart.items.reduce((sum, item) => sum + item.quantity, 0);
    
    if (cartCount) {
        if (totalItems > 0) {
            cartCount.textContent = totalItems;
            cartCount.style.display = 'inline-block';
        } else {
            cartCount.style.display = 'none';
        }
    }
    
    if (checkoutBtn) {
        checkoutBtn.disabled = totalItems === 0;
    }
}

function updateCartModal() {
    if (cart.items.length === 0) {
        if (emptyCart) emptyCart.style.display = 'block';
        if (cartItemsContainer) cartItemsContainer.style.display = 'none';
        if (cartModalFooter) cartModalFooter.style.display = 'none';
        return;
    }

    if (emptyCart) emptyCart.style.display = 'none';
    if (cartItemsContainer) cartItemsContainer.style.display = 'block';
    if (cartModalFooter) cartModalFooter.style.display = 'block';

    // Render cart items
    if (cartItemsContainer) {
        cartItemsContainer.innerHTML = cart.items.map(item => `
            <div class="card mb-2 cart-item" data-item-id="${item.menuItemId}">
                <div class="card-body py-2">
                    <div class="d-flex justify-content-between align-items-center">
                        <div class="flex-grow-1">
                            <div class="d-flex justify-content-between">
                                <h6 class="mb-1">${item.itemName}</h6>
                                <span class="fw-bold">₹${(item.unitPrice * item.quantity).toFixed(2)}</span>
                            </div>
                            <small class="text-muted">₹${item.unitPrice.toFixed(2)} each</small>
                        </div>
                        <div class="d-flex align-items-center gap-2 ms-3">
                            <button class="btn btn-sm btn-outline-secondary qty-btn" data-action="decrease">
                                <i class="bi bi-dash"></i>
                            </button>
                            <span class="fw-bold qty-value">${item.quantity}</span>
                            <button class="btn btn-sm btn-outline-secondary qty-btn" data-action="increase">
                                <i class="bi bi-plus"></i>
                            </button>
                            <button class="btn btn-sm btn-outline-danger ms-2 remove-btn">
                                <i class="bi bi-trash"></i>
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        `).join('');

        // Attach quantity events
        cartItemsContainer.querySelectorAll('.qty-btn').forEach(btn => {
            btn.addEventListener('click', (e) => {
                const itemId = parseInt(e.target.closest('.cart-item').dataset.itemId);
                const action = btn.dataset.action;
                updateQuantity(itemId, action === 'increase' ? 1 : -1);
            });
        });

        cartItemsContainer.querySelectorAll('.remove-btn').forEach(btn => {
            btn.addEventListener('click', (e) => {
                const itemId = parseInt(e.target.closest('.cart-item').dataset.itemId);
                removeFromCart(itemId);
            });
        });
    }

    // Update totals
    const subtotal = getSubtotal();
    const tax = subtotal * TAX_RATE / 100;
    const total = subtotal + tax;

    if (cartSubtotal) cartSubtotal.textContent = `₹${subtotal.toFixed(2)}`;
    if (cartTax) cartTax.textContent = `₹${tax.toFixed(2)}`;
    if (cartTotal) cartTotal.textContent = `₹${total.toFixed(2)}`;
}

function getSubtotal() {
    return cart.items.reduce((sum, item) => sum + (item.unitPrice * item.quantity), 0);
}

function openCheckoutModal() {
    if (cart.items.length === 0) return;
    
    const modal = new bootstrap.Modal(checkoutModal);
    modal.show();
    
    // Pre-fill table info
    document.getElementById('customerName').focus();
}

function handleCheckout(e) {
    e.preventDefault();
    
    const customerName = document.getElementById('customerName').value.trim();
    const customerMobile = document.getElementById('customerMobile').value.trim();
    const customerEmail = document.getElementById('customerEmail').value.trim();
    const specialInstructions = document.getElementById('specialInstructions').value.trim();
    
    if (!customerName || !customerMobile) {
        showToast('Please fill in all required fields', 'error');
        return;
    }
    
    if (!/^[6-9]\d{9}$/.test(customerMobile)) {
        showToast('Please enter a valid 10-digit mobile number', 'error');
        return;
    }
    
    // Close checkout modal
    bootstrap.Modal.getInstance(checkoutModal).hide();
    
    // Show loading
    showLoading('Creating order...');
    
    // Create order
    createOrder({
        customerName,
        customerMobile,
        customerEmail,
        tableNumber: cart.tableNumber,
        items: cart.items.map(item => ({
            menuItemId: item.menuItemId,
            quantity: item.quantity,
            specialInstructions: ''
        })),
        specialInstructions
    });
}

async function createOrder(orderData) {
    try {
        const response = await axios.post(`${API_BASE_URL}/orders`, orderData);
        
        if (response.data.success) {
            hideLoading();
            // Store order number for payment
            sessionStorage.setItem('current_order', response.data.data.orderNumber);
            sessionStorage.setItem('current_order_amount', response.data.data.totalAmount);
            // Redirect to payment page
            window.location.href = `/payment?order=${response.data.data.orderNumber}&table=${cart.tableToken}`;
        } else {
            hideLoading();
            showToast(response.data.message || 'Failed to create order', 'error');
        }
    } catch (error) {
        hideLoading();
        console.error('Order creation failed:', error);
        showToast('Failed to create order. Please try again.', 'error');
    }
}

function proceedToPayment() {
    const customerName = document.getElementById('customerName').value.trim();
    const customerMobile = document.getElementById('customerMobile').value.trim();
    const customerEmail = document.getElementById('customerEmail').value.trim();
    const specialInstructions = document.getElementById('specialInstructions').value.trim();
    
    if (!customerName || !customerMobile) {
        showToast('Please fill in all required fields', 'error');
        return;
    }
    
    if (!/^[6-9]\d{9}$/.test(customerMobile)) {
        showToast('Please enter a valid 10-digit mobile number', 'error');
        return;
    }
    
    bootstrap.Modal.getInstance(checkoutModal).hide();
    showLoading('Creating order...');
    
    createOrder({
        customerName,
        customerMobile,
        customerEmail,
        tableNumber: cart.tableNumber,
        items: cart.items.map(item => ({
            menuItemId: item.menuItemId,
            quantity: item.quantity,
            specialInstructions: ''
        })),
        specialInstructions
    });
}

function showLoading(message = 'Processing...') {
    if (loadingText) loadingText.textContent = message;
    const modal = new bootstrap.Modal(loadingModal);
    modal.show();
}

function hideLoading() {
    const modal = bootstrap.Modal.getInstance(loadingModal);
    if (modal) modal.hide();
}

function showToast(message, type = 'info') {
    // Remove existing toasts
    document.querySelectorAll('.toast-container').forEach(el => el.remove());
    
    const container = document.createElement('div');
    container.className = 'toast-container position-fixed bottom-0 end-0 p-3';
    container.style.zIndex = '9999';
    
    const bgClass = type === 'success' ? 'bg-success' : type === 'error' ? 'bg-danger' : 'bg-info';
    
    container.innerHTML = `
        <div class="toast ${bgClass} text-white" role="alert" aria-live="assertive" aria-atomic="true">
            <div class="toast-header ${bgClass} text-white border-0">
                <strong class="me-auto">${type === 'success' ? 'Success' : type === 'error' ? 'Error' : 'Info'}</strong>
                <button type="button" class="btn-close btn-close-white" data-bs-dismiss="toast"></button>
            </div>
            <div class="toast-body">${message}</div>
        </div>
    `;
    
    document.body.appendChild(container);
    const toast = new bootstrap.Toast(container.querySelector('.toast'));
    toast.show();
    
    container.querySelector('.toast').addEventListener('hidden.bs.toast', () => {
        container.remove();
    });
}

function debounce(func, wait) {
    let timeout;
    return function executedFunction(...args) {
        const later = () => {
            clearTimeout(timeout);
            func(...args);
        };
        clearTimeout(timeout);
        timeout = setTimeout(later, wait);
    };
}

// Track order page functionality
async function trackOrder(orderNumber, mobile) {
    try {
        const response = await axios.get(`${API_BASE_URL}/orders/track/${orderNumber}`, {
            params: { mobile }
        });
        
        if (response.data.success) {
            displayOrderTracking(response.data.data);
        } else {
            showToast(response.data.message || 'Order not found', 'error');
        }
    } catch (error) {
        console.error('Tracking failed:', error);
        showToast('Failed to track order', 'error');
    }
}

function displayOrderTracking(order) {
    document.getElementById('trackingFormSection').style.display = 'none';
    document.getElementById('orderDetailsSection').style.display = 'block';
    
    // Update progress tracker
    const statusOrder = ['PENDING', 'CONFIRMED', 'PREPARING', 'READY', 'SERVED'];
    const currentIndex = statusOrder.indexOf(order.orderStatus);
    
    document.querySelectorAll('.progress-step').forEach((step, index) => {
        step.classList.remove('completed', 'active');
        if (index < currentIndex) {
            step.classList.add('completed');
        } else if (index === currentIndex) {
            step.classList.add('active');
        }
    });
}

function debounce(func, wait) {
    let timeout;
    return function executedFunction(...args) {
        const later = () => {
            clearTimeout(timeout);
            func(...args);
        };
        clearTimeout(timeout);
        timeout = setTimeout(later, wait);
    };
}