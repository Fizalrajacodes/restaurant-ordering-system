// Payment page JavaScript for Razorpay integration

document.addEventListener('DOMContentLoaded', function() {
    initPayment();
});

async function initPayment() {
    const payNowBtn = document.getElementById('payNowBtn');
    
    if (!RAZORPAY_KEY_ID || !RAZORPAY_ORDER_ID) {
        showError('Payment configuration missing');
        return;
    }
    
    // Enable pay button
    if (payNowBtn) {
        payNowBtn.disabled = false;
        payNowBtn.addEventListener('click', openRazorpayCheckout);
    }
}

function openRazorpayCheckout() {
    const payNowBtn = document.getElementById('payNowBtn');
    payNowBtn.disabled = true;
    payNowBtn.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>Opening...';
    
    const options = {
        key: RAZORPAY_KEY_ID,
        amount: Math.round(AMOUNT * 100), // Amount in paise
        currency: 'INR',
        name: RESTAURANT_NAME,
        description: `Order ${ORDER_NUMBER}`,
        order_id: RAZORPAY_ORDER_ID,
        handler: function(response) {
            verifyPayment(response);
        },
        prefill: {
            name: sessionStorage.getItem('customer_name') || '',
            email: sessionStorage.getItem('customer_email') || '',
            contact: sessionStorage.getItem('customer_mobile') || ''
        },
        notes: {
            order_number: ORDER_NUMBER,
            table_token: TABLE_TOKEN
        },
        theme: {
            color: '#0d6efd'
        },
        modal: {
            ondismiss: function() {
                payNowBtn.disabled = false;
                payNowBtn.innerHTML = '<i class="bi bi-credit-card me-2"></i>Pay Now';
                showToast('Payment cancelled. You can try again.', 'info');
            }
        }
    };
    
    const rzp = new Razorpay(options);
    rzp.open();
}

async function verifyPayment(response) {
    const payNowBtn = document.getElementById('payNowBtn');
    payNowBtn.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>Verifying...';
    
    try {
        const verifyResponse = await axios.post(`${API_BASE_URL}/payments/verify`, {
            razorpayOrderId: response.razorpay_order_id,
            razorpayPaymentId: response.razorpay_payment_id,
            razorpaySignature: response.razorpay_signature,
            orderNumber: ORDER_NUMBER
        });
        
        if (verifyResponse.data.success) {
            // Clear cart
            localStorage.removeItem('restaurant_cart_' + TABLE_TOKEN);
            sessionStorage.removeItem('current_order');
            sessionStorage.removeItem('current_order_amount');
            sessionStorage.removeItem('customer_name');
            sessionStorage.removeItem('customer_email');
            sessionStorage.removeItem('customer_mobile');
            
            // Redirect to success page
            window.location.href = `/success?order=${ORDER_NUMBER}&table=${TABLE_TOKEN}`;
        } else {
            payNowBtn.disabled = false;
            payNowBtn.innerHTML = '<i class="bi bi-credit-card me-2"></i>Pay Now';
            showToast(verifyResponse.data.message || 'Payment verification failed', 'error');
        }
    } catch (error) {
        console.error('Payment verification failed:', error);
        payNowBtn.disabled = false;
        payNowBtn.innerHTML = '<i class="bi bi-credit-card me-2"></i>Pay Now';
        showToast('Payment verification failed. Please contact support.', 'error');
    }
}

function showError(message) {
    document.body.innerHTML = `
        <div class="d-flex flex-column min-vh-100">
            <main class="flex-grow-1 d-flex align-items-center justify-content-center py-5">
                <div class="container">
                    <div class="row justify-content-center">
                        <div class="col-md-6">
                            <div class="card shadow">
                                <div class="card-body text-center p-5">
                                    <i class="bi bi-exclamation-triangle display-1 text-danger"></i>
                                    <h3 class="mt-3">Payment Error</h3>
                                    <p class="text-muted">${message}</p>
                                    <a href="/menu?table=${TABLE_TOKEN}" class="btn btn-primary mt-3">
                                        <i class="bi bi-arrow-left me-2"></i>Back to Menu
                                    </a>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </main>
        </div>
    `;
}

function showToast(message, type = 'info') {
    const container = document.createElement('div');
    container.className = 'toast-container position-fixed bottom-0 end-0 p-3';
    container.style.zIndex = '9999';
    
    const bgClass = type === 'success' ? 'bg-success' : type === 'error' ? 'bg-danger' : 'bg-info';
    
    container.innerHTML = `
        <div class="toast ${bgClass} text-white" role="alert">
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