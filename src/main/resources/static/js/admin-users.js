// admin-users.js

function changeRole(userId, role) {
    if (!confirm('Изменить роль пользователя?')) {
        location.reload();
        return;
    }
    const params = new URLSearchParams();
    params.append('role', role);

    fetch(`/admin/users/${userId}/role`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: params
    })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                alert(data.message);
                location.reload();
            } else {
                alert('Ошибка: ' + data.message);
            }
        })
        .catch(error => console.error('Error:', error));
}

function deleteUser(userId) {
    if (!confirm('Удалить пользователя? Это действие нельзя отменить!')) {
        return;
    }

    fetch(`/admin/users/${userId}`, {
        method: 'DELETE',
        headers: { 'Content-Type': 'application/json' }
    })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                alert(data.message);
                location.reload();
            } else {
                alert('Ошибка: ' + data.message);
            }
        })
        .catch(error => console.error('Error:', error));
}