// admin-tickets.js - управление билетами

function generateTickets(matchId, sectorId) {
    const rowsInput = document.getElementById('rows_' + sectorId);
    const seatsInput = document.getElementById('seats_' + sectorId);
    const priceInput = document.getElementById('price_' + sectorId);

    if (!rowsInput || !seatsInput) {
        console.error('Elements not found for sector:', sectorId);
        alert('Ошибка: не найдены поля для ввода');
        return;
    }

    const rows = rowsInput.value;
    const seats = seatsInput.value;
    const basePrice = priceInput ? priceInput.value : 50;

    if (!confirm(`Создать ${rows} рядов по ${seats} мест? Старые билеты будут удалены!`)) {
        return;
    }

    const params = new URLSearchParams();
    params.append('rows', rows);
    params.append('seatsPerRow', seats);
    params.append('basePrice', basePrice);

    fetch(`/admin/matches/${matchId}/sectors/${sectorId}/generate`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: params
    })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                // Обновляем счётчик билетов на карточке
                const countSpan = document.getElementById('count_' + sectorId);
                console.log('Updating count for sector:', sectorId, 'Count span:', countSpan, 'New count:', data.count);
                if (countSpan) {
                    countSpan.textContent = data.count;
                } else {
                    console.error('Count span not found for sector:', sectorId);
                }
                alert(data.message);
            } else {
                alert('Ошибка: ' + data.message);
            }
        })
        .catch(error => console.error('Error:', error));
}

function updatePrice(matchId, sectorId) {
    const priceInput = document.getElementById('price_' + sectorId);
    if (!priceInput) {
        alert('Ошибка: поле цены не найдено');
        return;
    }

    const newPrice = priceInput.value;

    if (!newPrice || newPrice <= 0) {
        alert('Введите корректную цену');
        return;
    }

    const params = new URLSearchParams();
    params.append('newPrice', newPrice);

    fetch(`/admin/matches/${matchId}/sectors/${sectorId}/update-price`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: params
    })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                alert(data.message);
            } else {
                alert('Ошибка: ' + data.message);
            }
        })
        .catch(error => console.error('Error:', error));
}