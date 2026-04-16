// admin-matches.js - управление матчами

// Открыть модальное окно
function openCreateModal() {
    const modal = document.getElementById('createMatchModal');
    if (modal) {
        modal.style.display = 'flex';
        document.body.style.overflow = 'hidden';
    }
}
// Закрыть модальное окно
function closeCreateModal() {
    const modal = document.getElementById('createMatchModal');
    if (modal) {
        modal.style.display = 'none';
        document.body.style.overflow = 'auto';
        const messageDiv = document.getElementById('createMatchMessage');
        if (messageDiv) messageDiv.innerHTML = '';
    }
}

// Закрытие по клику на фон
window.onclick = function(event) {
    const modal = document.getElementById('createMatchModal');
    if (modal && event.target === modal) {
        closeCreateModal();
    }
}

// Создание матча
function createMatch() {
    const homeTeamId = document.getElementById('homeTeamId').value;
    const awayTeamId = document.getElementById('awayTeamId').value;
    const stadiumId = document.getElementById('stadiumId').value;
    const matchDateTime = document.getElementById('matchDateTime').value;

    // Валидация
    if (!homeTeamId) {
        showMessage('Выберите команду хозяев', 'danger');
        return;
    }
    if (!awayTeamId) {
        showMessage('Выберите команду гостей', 'danger');
        return;
    }
    if (homeTeamId === awayTeamId) {
        showMessage('Команды должны быть разными!', 'danger');
        return;
    }
    if (!stadiumId) {
        showMessage('Выберите стадион', 'danger');
        return;
    }
    if (!matchDateTime) {
        showMessage('Выберите дату и время матча', 'danger');
        return;
    }

    // Проверка, что дата не в прошлом
    const selectedDate = new Date(matchDateTime);
    const now = new Date();
    if (selectedDate < now) {
        showMessage('Нельзя создать матч в прошлом! Выберите будущую дату и время.', 'danger');
        return;
    }

    const params = new URLSearchParams();
    params.append('homeTeamId', homeTeamId);
    params.append('awayTeamId', awayTeamId);
    params.append('stadiumId', stadiumId);
    params.append('matchDateTime', matchDateTime);

    fetch('/admin/matches/create', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: params
    })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                showMessage('Матч успешно создан!', 'success');
                setTimeout(() => location.reload(), 1500);
            } else {
                showMessage(data.message || 'Ошибка при создании матча', 'danger');
            }
        })
        .catch(error => {
            console.error('Error:', error);
            showMessage('Ошибка сервера', 'danger');
        });
}

// Удаление матча
function deleteMatch(matchId) {
    if (!confirm('Вы уверены, что хотите удалить этот матч? Будут удалены все билеты!')) {
        return;
    }

    fetch('/admin/matches/' + matchId, {
        method: 'DELETE',
        headers: { 'Content-Type': 'application/json' }
    })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                showMessage('Матч удалён', 'success');
                setTimeout(() => location.reload(), 1000);
            } else {
                alert(data.message || 'Ошибка при удалении');
            }
        })
        .catch(error => {
            console.error('Error:', error);
            alert('Ошибка сервера');
        });
}

// Устанавливаем минимальную дату (сегодня) при загрузке страницы
document.addEventListener('DOMContentLoaded', function() {
    const dateInput = document.getElementById('matchDateTime');
    if (dateInput) {
        const now = new Date();
        now.setMinutes(now.getMinutes() - now.getTimezoneOffset());
        dateInput.min = now.toISOString().slice(0, 16);
    }
});

// Показать сообщение
function showMessage(message, type) {
    const messageDiv = document.getElementById('createMatchMessage');
    if (messageDiv) {
        messageDiv.innerHTML = `<div class="alert alert-${type} alert-dismissible fade show mb-0 py-2">${message}<button type="button" class="btn-close" data-bs-dismiss="alert"></button></div>`;
        setTimeout(() => {
            const alert = messageDiv.querySelector('.alert');
            if (alert) alert.remove();
        }, 3000);
    }
}