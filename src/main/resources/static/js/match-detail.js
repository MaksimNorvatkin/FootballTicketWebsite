// match-detail.js
let currentPage = 0;
let currentSectorId = null;
let totalTickets = parseInt(document.getElementById('tickets-count')?.innerText || 0);
const pageSize = 10;

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
            console.log('Data received:', data);
            console.log('tickets count in data:', data.tickets.length);
            if (reset) {
                renderTickets(data.tickets);
                currentPage = 0;
                totalTickets = data.totalCount;
            } else {
                appendTickets(data.tickets);
                currentPage = page;
            }

            updatePriceRange(data.minPrice, data.maxPrice);
            const ticketsCountEl = document.getElementById('tickets-count');
            if (ticketsCountEl) ticketsCountEl.textContent = data.totalCount;

            const currentShown = document.querySelectorAll('#tickets-list .ticket-item').length;
            const loadMoreContainer = document.getElementById('load-more-container');
            const loadMoreBtn = document.getElementById('load-more-btn');

            if (loadMoreContainer) {
                if (data.hasMore && currentShown < data.totalCount) {
                    loadMoreContainer.style.display = 'block';
                    if (loadMoreBtn) {
                        loadMoreBtn.textContent = `Показать еще билеты (${currentShown}/${data.totalCount}) ↓`;
                    }
                } else {
                    loadMoreContainer.style.display = 'none';
                }
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
function renderTickets(tickets) {
    console.log('renderTickets called, tickets count:', tickets.length);

    // Получаем элементы
    let ticketsList = document.getElementById('tickets-list');
    let ticketsContainer = document.getElementById('tickets-container');
    let noTicketsMessage = document.getElementById('no-tickets-message');

    console.log('ticketsList:', ticketsList);
    console.log('ticketsContainer:', ticketsContainer);

    // Если контейнера нет - выходим
    if (!ticketsContainer) {
        console.error('tickets-container not found!');
        return;
    }

    // Если списка нет - создаём
    if (!ticketsList) {
        console.log('Creating tickets-list dynamically');
        ticketsContainer.innerHTML = '<div id="tickets-list" class="tickets-list"></div>';
        ticketsList = document.getElementById('tickets-list');
    }

    // Если после создания всё равно нет - выходим
    if (!ticketsList) {
        console.error('tickets-list not found even after creation!');
        return;
    }

    // Очищаем список
    ticketsList.innerHTML = '';

    // Если билетов нет
    if (tickets.length === 0) {
        ticketsList.style.display = 'none';
        if (noTicketsMessage) {
            noTicketsMessage.style.display = 'block';
        }
        return;
    }

    // Если билеты есть
    ticketsList.style.display = 'block';
    if (noTicketsMessage) {
        noTicketsMessage.style.display = 'none';
    }

    // Добавляем билеты
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

// Добавление билетов (для пагинации)
function appendTickets(tickets) {
    const ticketsList = document.getElementById('tickets-list');

    if (!ticketsList) return;

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
        if (!ticketItem) return;

        const ticketId = ticketItem.getAttribute('data-ticket-id');
        if (!ticketId) return;

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

// Сброс фильтра
window.resetFilter = function() {
    document.querySelectorAll('.simple-sector').forEach(sector => {
        sector.classList.remove('active');
    });

    currentSectorId = null;
    currentPage = 0;

    const matchId = window.location.pathname.split('/').pop();
    loadTickets(matchId, null, 0, true);
};

// Инициализация
document.addEventListener('DOMContentLoaded', function() {
    totalTickets = parseInt(document.getElementById('tickets-count')?.textContent || '0');
    highlightActiveSector();
    updateAllCartButtons();
});