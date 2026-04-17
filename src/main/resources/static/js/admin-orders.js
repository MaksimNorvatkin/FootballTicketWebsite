// admin-orders.js

function adminChangeStatus(orderId, status) {
    const params = new URLSearchParams();
    params.append('status', status);

    fetch(`/admin/orders/${orderId}/status`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: params
    })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                alert(data.message);
                updateAdminStats();
                location.reload();
                window.location.href = window.location.href;
            } else {
                alert('Ошибка: ' + data.message);
            }
        })
        .catch(error => console.error('Error:', error));
}

function adminShowDetails(orderId) {
    fetch(`/admin/orders/${orderId}/details`)
        .then(response => response.json())
        .then(data => {
            let message = `Заказ #${data.orderNumber}\n`;
            message += `Email: ${data.customerEmail}\n`;
            message += `Сумма: £${data.totalAmount}\n`;
            message += `Статус: ${data.status}\n`;
            message += `Дата: ${data.orderDate}\n\n`;
            message += `🎫 Билеты:\n`;

            data.tickets.forEach((ticket, index) => {
                message += `${index + 1}. ${ticket.match} - ${ticket.sector}, Ряд ${ticket.row}, Место ${ticket.seat} - £${ticket.price}\n`;
            });

            alert(message);
        })
        .catch(error => console.error('Error:', error));
}

function updateAdminStats() {
    fetch('/admin/orders/stats')
        .then(response => response.json())
        .then(data => {
            document.getElementById('admin-stat-total').textContent = data.totalOrders;
            document.getElementById('admin-stat-paid').textContent = data.totalPaid;
            document.getElementById('admin-stat-processing').textContent = data.totalProcessing;
            document.getElementById('admin-stat-shipped').textContent = data.totalShipped;
            document.getElementById('admin-stat-cancelled').textContent = data.totalCancelled;
        })
        .catch(error => console.error('Error:', error));
}