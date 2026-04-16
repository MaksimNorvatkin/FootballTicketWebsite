// match-detail.js
let currentPage = 0;
let currentSectorId = null;
let totalTickets = parseInt(document.getElementById('tickets-count')?.innerText || 0);
const pageSize = 10;

// Проверка, находится ли билет в корзине
function isTicketInCart(ticketId) {
    return fetch(`/cart/check/${ticketId}`)
        .then(response => response.json())
        .then(data => data.inCart)
        .catch(() => false);
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
                renderTickets(data.tickets);
                currentPage = 0;
                totalTickets = data.totalCount;
            } else {
                appendTickets(data.tickets);
                currentPage = page;
            }

            updatePriceRange(data.minPrice, data.maxPrice);
            document.getElementById('tickets-count').textContent = data.totalCount;

            const currentShown = document.querySelectorAll('#tickets-list .ticket-item').length;
            const loadMoreBtn = document.getElementById('load-more-btn');
            const loadMoreContainer = document.getElementById('load-more-container');

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
    const ticketsList = document.getElementById('tickets-list');
    const ticketsContainer = document.getElementById('tickets-container');

    if (!ticketsList) {
        console.error('tickets-list not found');
        return;
    }

    // Очищаем список
    ticketsList.innerHTML = '';

    // Находим или создаём сообщение об отсутствии билетов
    let noTicketsAlert = ticketsContainer ? ticketsContainer.querySelector('.alert-warning-custom') : null;

    if (tickets.length === 0) {
        // Скрываем список
        ticketsList.style.display = 'none';

        // Показываем сообщение
        if (!noTicketsAlert) {
            const alertDiv = document.createElement('div');
            alertDiv.className = 'alert alert-warning-custom';
            alertDiv.innerHTML = '😔 Нет доступных билетов в выбранном секторе';
            if (ticketsContainer) {
                ticketsContainer.appendChild(alertDiv);
            }
        } else {
            noTicketsAlert.style.display = 'block';
        }
        return;
    }

    // Показываем список
    ticketsList.style.display = 'block';

    // Удаляем сообщение если есть
    if (noTicketsAlert) {
        noTicketsAlert.remove();
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

    updateAllCartButtons();
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
                    button.setAttribute('data-ticket-id', ticketId);
                    button.setAttribute('onclick', 'removeFromCart(this)');
                } else {
                    button.textContent = 'В корзину';
                    button.classList.remove('btn-danger');
                    button.classList.add('btn-primary');
                    button.setAttribute('data-ticket-id', ticketId);
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

// Сброс фильтра (без перезагрузки страницы)
window.resetFilter = function() {
    // Убираем подсветку активного сектора
    document.querySelectorAll('.simple-sector').forEach(sector => {
        sector.classList.remove('active');
    });

    // Сбрасываем текущий сектор
    currentSectorId = null;
    currentPage = 0;

    // Загружаем все билеты (без фильтра по сектору)
    const matchId = window.location.pathname.split('/').pop();
    loadTickets(matchId, null, 0, true);
};

// Инициализация
document.addEventListener('DOMContentLoaded', function() {
    totalTickets = parseInt(document.getElementById('tickets-count')?.textContent || '0');
    highlightActiveSector();
    updateAllCartButtons();
});