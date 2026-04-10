// cart.js - функции для корзины
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

function addToCart(button) {
    const ticketCard = button.closest('.ticket-card');
    const ticketId = ticketCard.getAttribute('data-ticket-id');
    const matchId = window.location.pathname.split('/').pop();

    fetch('/cart/add/' + ticketId, {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: 'matchId=' + matchId
    }).then(() => {
        button.textContent = '✓ Добавлено!';
        button.classList.remove('btn-primary');
        button.classList.add('btn-success');
        setTimeout(() => {
            button.textContent = 'В корзину';
            button.classList.remove('btn-success');
            button.classList.add('btn-primary');
        }, 1500);
        updateCartCount();
    }).catch(error => console.error('Error:', error));
}

function removeFromCart(ticketId) {
    fetch('/cart/remove/' + ticketId, { method: 'POST' })
        .then(() => location.reload());
}

function updateQuantity(ticketId, quantity) {
    fetch('/cart/update/' + ticketId, {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: 'quantity=' + quantity
    }).then(() => location.reload());
}