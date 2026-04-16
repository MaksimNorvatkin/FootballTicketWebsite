// matches.js - AJAX фильтрация

// Применение фильтров
function applyFilters() {
    const team = document.querySelector('select[name="team"]')?.value || '';
    const stadium = document.querySelector('select[name="stadium"]')?.value || '';
    const fromDate = document.querySelector('input[name="fromDate"]')?.value || '';
    const toDate = document.querySelector('input[name="toDate"]')?.value || '';

    let url = `/matches/filter?`;
    if (team) url += `team=${encodeURIComponent(team)}&`;
    if (stadium) url += `stadium=${encodeURIComponent(stadium)}&`;
    if (fromDate) url += `fromDate=${fromDate}&`;
    if (toDate) url += `toDate=${toDate}&`;

    fetch(url)
        .then(response => response.json())
        .then(data => {
            updateMatchesList(data);
        })
        .catch(error => console.error('Error:', error));
}

// Сброс фильтров
function resetFilters() {
    const teamSelect = document.querySelector('select[name="team"]');
    const stadiumSelect = document.querySelector('select[name="stadium"]');
    const fromDateInput = document.querySelector('input[name="fromDate"]');
    const toDateInput = document.querySelector('input[name="toDate"]');

    if (teamSelect) teamSelect.value = '';
    if (stadiumSelect) stadiumSelect.value = '';
    if (fromDateInput) fromDateInput.value = '';
    if (toDateInput) toDateInput.value = '';

    fetch('/matches/filter')
        .then(response => response.json())
        .then(data => {
            updateMatchesList(data);
        })
        .catch(error => console.error('Error:', error));
}

// Обновление списка матчей (только для AJAX)
function updateMatchesList(data) {
    const matchesCount = document.getElementById('matchesCount');
    const matchesList = document.getElementById('matchesList');
    const noMatchesAjaxMessage = document.getElementById('noMatchesAjaxMessage');
    const noMatchesMessage = document.getElementById('noMatchesMessage');

    // Обновляем счётчик
    if (matchesCount) {
        matchesCount.textContent = data.count;
        console.log('Updated count to:', data.count);
    } else {
        console.error('matchesCount element not found!');
    }

    // Скрываем/показываем сообщения
    if (data.matches.length === 0) {
        // Скрываем список матчей
        if (matchesList) matchesList.style.display = 'none';
        // Скрываем серверное сообщение
        if (noMatchesMessage) noMatchesMessage.style.display = 'none';
        // Показываем AJAX сообщение
        if (noMatchesAjaxMessage) noMatchesAjaxMessage.style.display = 'block';
        return;
    }

    // Показываем список, скрываем сообщения
    if (matchesList) matchesList.style.display = 'flex';
    if (noMatchesMessage) noMatchesMessage.style.display = 'none';
    if (noMatchesAjaxMessage) noMatchesAjaxMessage.style.display = 'none';


    // Рендерим матчи
    let html = '';
    data.matches.forEach(match => {
        html += `
            <div class="col-md-6 col-lg-4 mb-4">
                <div class="match-card">
                    <div class="match-date-box">
                        <div class="date-month">${match.month}</div>
                        <div class="date-day">${match.day}</div>
                        <div class="date-year">${match.year}</div>
                    </div>
                    <div class="p-3">
                        <div class="match-teams">${escapeHtml(match.homeTeam)} vs ${escapeHtml(match.awayTeam)}</div>
                        <div class="match-detail-info">
                            <span>🏟️ ${escapeHtml(match.stadium)}</span>
                            <span>🏙️ ${escapeHtml(match.city)}</span>
                            <span>⏰ ${match.time}</span>
                        </div>
                        <a href="/match/${match.id}" class="btn-buy">🎫 Выбрать билеты →</a>
                    </div>
                </div>
            </div>
        `;
    });

    if (matchesList) {
        matchesList.innerHTML = html;
        matchesList.style.display = 'flex';
    }
}

// Защита от XSS
function escapeHtml(text) {
    if (!text) return '';
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}