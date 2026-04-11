// match-detail.js
let currentPage = 0;
let currentSectorId = null;
let totalTickets = 0;
const pageSize = 10;

// Добавление в корзину
function addToCart(button) {
    const ticketCard = button.closest('.ticket-card');
    const ticketId = ticketCard.getAttribute('data-ticket-id');
    const matchId = window.location.pathname.split('/').pop();

    fetch('/cart/add/' + ticketId, {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: 'matchId=' + matchId
    }).then(() => {
        // Меняем на кнопку "Удалить"
        button.textContent = '🗑️ Удалить';
        button.classList.remove('btn-primary');
        button.classList.add('btn-danger');
        button.setAttribute('onclick', 'removeFromCart(this)');
        updateCartCount();
    }).catch(error => console.error('Error adding to cart:', error));
}

// Удаление из корзины
function removeFromCart(button) {
    const ticketCard = button.closest('.ticket-card');
    const ticketId = ticketCard.getAttribute('data-ticket-id');
    const matchId = window.location.pathname.split('/').pop();

    fetch('/cart/remove/' + ticketId, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' }
    }).then(response => response.json())
        .then(data => {
            if (data.success) {
                // Возвращаем кнопку "В корзину"
                button.textContent = 'В корзину';
                button.classList.remove('btn-danger');
                button.classList.add('btn-primary');
                button.setAttribute('onclick', 'addToCart(this)');
                updateCartCount();
            }
        }).catch(error => console.error('Error removing from cart:', error));
}

// Фильтр по сектору
window.filterBySector = function(element) {
    const sectorId = element.getAttribute('data-sector-id');
    const matchId = window.location.pathname.split('/').pop();

    currentPage = 0;
    currentSectorId = sectorId;

    document.querySelectorAll('.simple-sector').forEach(sector => {
        sector.classList.remove('active');
    });
    element.classList.add('active');

    loadTickets(matchId, sectorId, 0, true);
};

// Загрузка билетов
function loadTickets(matchId, sectorId, page, reset = false) {
    let url = `/match/${matchId}/tickets?page=${page}&size=${pageSize}`;
    if (sectorId) {
        url += `&sectorId=${sectorId}`;
    }

    fetch(url)
        .then(response => response.json())
        .then(data => {
            if (reset) {
                renderTickets(data.tickets, true);
                currentPage = 0;
                totalTickets = data.totalCount;
            } else {
                renderTickets(data.tickets, false);
                currentPage = page;
            }

            updatePriceRange(data.minPrice, data.maxPrice);
            document.getElementById('tickets-count').textContent = data.totalCount;

            const currentShown = document.querySelectorAll('#tickets-list .ticket-item').length;
            const loadMoreBtn = document.getElementById('load-more-btn');
            const loadMoreContainer = document.getElementById('load-more-container');

            if (data.hasMore && currentShown < data.totalCount) {
                loadMoreContainer.style.display = 'block';
                loadMoreBtn.textContent = `Показать еще билеты (${currentShown}/${data.totalCount}) ↓`;
            } else {
                loadMoreContainer.style.display = 'none';
            }

            updateAllCartButtons();
        })
        .catch(error => console.error('Error loading tickets:', error));
}

// Загрузка следующих билетов
window.loadMoreTickets = function() {
    const matchId = window.location.pathname.split('/').pop();
    const nextPage = currentPage + 1;
    loadTickets(matchId, currentSectorId, nextPage, false);
};

// Отрисовка билетов
function renderTickets(tickets, reset) {
    const ticketsList = document.getElementById('tickets-list');

    if (reset) {
        ticketsList.innerHTML = '';
    }

    if (tickets.length === 0 && reset) {
        const container = document.getElementById('tickets-container');
        if (container) {
            container.innerHTML = `
                <div class="alert alert-warning">
                    😔 Нет доступных билетов в выбранном секторе
                </div>
            `;
        }
        return;
    }

    tickets.forEach(ticket => {
        const ticketHtml = `
            <div class="ticket-item" data-ticket-id="${ticket.id}" data-ticket-price="${ticket.price}">
                <div class="card ticket-card">
                    <div class="card-body">
                        <div class="d-flex justify-content-between align-items-center">
                            <div>
                                <h6 class="mb-1">Сектор: ${ticket.sectorName}</h6>
                                <p class="mb-0 small">Ряд: ${ticket.rowNumber}, Место: ${ticket.seatNumber}</p>
                            </div>
                            <div class="text-end">
                                <span class="badge bg-success fs-6 price-badge">£${ticket.price}</span>
                                <button class="btn btn-sm btn-primary" onclick="addToCart(this)">В корзину</button>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        `;
        ticketsList.insertAdjacentHTML('beforeend', ticketHtml);
    });
}

// Обновление состояния всех кнопок корзины
function updateAllCartButtons() {
    const buttons = document.querySelectorAll('.ticket-item button');

    buttons.forEach(button => {
        const ticketItem = button.closest('.ticket-item');
        const ticketId = ticketItem.getAttribute('data-ticket-id');

        fetch(`/cart/check/${ticketId}`)
            .then(response => response.json())
            .then(data => {
                if (data.inCart) {
                    button.textContent = '🗑️ Удалить';
                    button.classList.remove('btn-primary');
                    button.classList.add('btn-danger');
                    button.setAttribute('onclick', 'removeFromCart(this)');
                } else {
                    button.textContent = 'В корзину';
                    button.classList.remove('btn-danger');
                    button.classList.add('btn-primary');
                    button.setAttribute('onclick', 'addToCart(this)');
                }
            })
            .catch(error => console.error('Error checking cart:', error));
    });
}

// Обновление диапазона цен
function updatePriceRange(minPrice, maxPrice) {
    const priceRange = document.getElementById('price-range');
    if (priceRange) {
        priceRange.innerHTML = `
            <div class="price-range-title">💰 Диапазон цен</div>
            <div class="price-range-values">
                £${minPrice} - £${maxPrice}
            </div>
        `;
    }
}

// Обновление счетчика корзины
function updateCartCount() {
    fetch('/cart/count')
        .then(response => response.json())
        .then(data => {
            const cartCountSpan = document.getElementById('cartCount');
            if (cartCountSpan) {
                if (data.count > 0) {
                    cartCountSpan.textContent = data.count;
                    cartCountSpan.style.display = 'inline-block';
                } else {
                    cartCountSpan.style.display = 'none';
                }
            }
        })
        .catch(error => console.error('Error fetching cart count:', error));
}

// Подсветка активного сектора
function highlightActiveSector() {
    const urlParams = new URLSearchParams(window.location.search);
    const selectedSectorId = urlParams.get('sectorId');

    if (selectedSectorId) {
        currentSectorId = selectedSectorId;
        document.querySelectorAll('.simple-sector').forEach(sector => {
            if (sector.getAttribute('data-sector-id') === selectedSectorId) {
                sector.classList.add('active');
            }
        });
    }
}

// Инициализация
document.addEventListener('DOMContentLoaded', function() {
    totalTickets = parseInt(document.getElementById('tickets-count')?.textContent || '0');
    highlightActiveSector();
    updateAllCartButtons();
});