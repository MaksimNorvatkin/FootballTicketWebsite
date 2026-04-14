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

// Универсальное удаление из корзины
function removeFromCart(button) {
    const ticketId = button.getAttribute('data-ticket-id');
    if (!ticketId) {
        console.error('ticketId not found', button);
        return;
    }

    fetch('/cart/remove/' + ticketId, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' }
    }).then(response => response.json())
        .then(data => {
            if (data.success) {
                // Проверяем, где находимся
                if (button.closest('tr')) {
                    // На странице корзины - удаляем строку
                    const row = button.closest('tr');
                    if (row) row.remove();
                    updateCartTotal(data.totalAmount);
                    if (data.itemCount === 0) {
                        showEmptyCartMessage();
                    }
                } else {
                    // На странице матча - меняем кнопку обратно на "В корзину"
                    button.textContent = 'В корзину';
                    button.classList.remove('btn-danger');
                    button.classList.add('btn-primary');
                    button.setAttribute('onclick', 'addToCart(this)');
                }
                updateCartCount();
            }
        })
        .catch(error => console.error('Error:', error));
}

// Обновление общей суммы в корзине
function updateCartTotal(totalAmount) {
    const totalElement = document.getElementById('cart-total');
    if (totalElement) {
        totalElement.textContent = totalAmount.toFixed(2);
    }
}

// Показать сообщение о пустой корзине
function showEmptyCartMessage() {
    const cartContainer = document.querySelector('.cart-container');
    if (cartContainer) {
        cartContainer.innerHTML = `
            <div class="alert alert-info text-center">
                <h4>🛒 Корзина пуста</h4>
                <p>Добавьте билеты на матчи, чтобы продолжить.</p>
                <a href="/matches" class="btn btn-primary">Посмотреть матчи</a>
            </div>
        `;
    }
}

function updateQuantity(ticketId, quantity) {
    fetch('/cart/update/' + ticketId, {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: 'quantity=' + quantity
    }).then(() => location.reload());
}