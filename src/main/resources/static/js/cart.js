// cart.js - функции для корзины

// Обновление счетчика корзины
function updateCartCount() {
    fetch('/cart/count')
        .then(response => response.json())
        .then(data => {
            const cartCountSpan = document.getElementById('cartCount');
            if (data && data.count > 0) {
                cartCountSpan.textContent = data.count;
                cartCountSpan.style.display = 'inline-block';
            } else {
                cartCountSpan.style.display = 'none';
            }
        })
        .catch(error => console.error('Error fetching cart count:', error));
}

// Добавление в корзину
function addToCart(button) {
    const ticketItem = button.closest('.ticket-item');
    const ticketId = ticketItem.getAttribute('data-ticket-id');
    const matchId = window.location.pathname.split('/').pop();

    fetch('/cart/add/' + ticketId, {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: 'matchId=' + matchId
    }).then(() => {
        // Меняем кнопку на "Удалить"
        button.textContent = '🗑️ Удалить';
        button.classList.remove('btn-primary');
        button.classList.add('btn-danger');
        button.setAttribute('onclick', 'removeFromCart(this)');
        updateCartCount();
    }).catch(error => console.error('Error adding to cart:', error));
}

// Удаление из корзины
function removeFromCart(button) {
    const ticketItem = button.closest('.ticket-item');
    const ticketId = ticketItem.getAttribute('data-ticket-id');

    fetch('/cart/remove/' + ticketId, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' }
    }).then(response => response.json())
        .then(data => {
            if (data.success) {
                // Меняем кнопку обратно на "В корзину"
                button.textContent = 'В корзину';
                button.classList.remove('btn-danger');
                button.classList.add('btn-primary');
                button.setAttribute('onclick', 'addToCart(this)');
                updateCartCount();
            }
        }).catch(error => console.error('Error removing from cart:', error));
}

function updateQuantity(ticketId, quantity) {
    fetch('/cart/update/' + ticketId, {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: 'quantity=' + quantity
    }).then(() => location.reload());
}