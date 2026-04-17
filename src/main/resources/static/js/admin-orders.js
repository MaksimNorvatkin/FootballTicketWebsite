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
            let html = `
                <div class="order-details">
                    <div class="row mb-3">
                        <div class="col-md-6">
                            <strong>📋 Номер заказа:</strong> ${data.orderNumber}
                        </div>
                        <div class="col-md-6">
                            <strong>📧 Email:</strong> ${data.customerEmail}
                        </div>
                    </div>
                    <div class="row mb-3">
                        <div class="col-md-6">
                            <strong>💰 Сумма:</strong> £${data.totalAmount}
                        </div>
                        <div class="col-md-6">
                            <strong>📅 Дата:</strong> ${data.orderDate}
                        </div>
                    </div>
                    <div class="row mb-3">
                        <div class="col-md-6">
                            <strong>📌 Статус:</strong> 
                            <span class="badge ${getAdminStatusBadgeClass(data.status)}">${getAdminStatusText(data.status)}</span>
                        </div>
                    </div>
                    <hr>
                    <h6>🎫 Билеты:</h6>
                    <div class="table-responsive">
                        <table class="table table-sm">
                            <thead>
                                <tr>
                                    <th>Матч</th>
                                    <th>Сектор</th>
                                    <th>Ряд</th>
                                    <th>Место</th>
                                    <th>Цена</th>
                                </tr>
                            </thead>
                            <tbody>
            `;

            data.tickets.forEach(ticket => {
                html += `
                    <tr>
                        <td>${ticket.match}</td>
                        <td>${ticket.sector}</td>
                        <td>${ticket.row}</td>
                        <td>${ticket.seat}</td>
                        <td>£${ticket.price}</td>
                    </tr>
                `;
            });

            html += `
                            </tbody>
                        </table>
                    </div>
                </div>
            `;

            document.getElementById('adminOrderDetailsBody').innerHTML = html;

            // Показываем модальное окно
            const modal = new bootstrap.Modal(document.getElementById('adminOrderDetailsModal'));
            modal.show();
        })
        .catch(error => console.error('Error:', error));
}

function getAdminStatusBadgeClass(status) {
    switch(status) {
        case 'PAID': return 'bg-success';
        case 'PROCESSING': return 'bg-warning text-dark';
        case 'SHIPPED': return 'bg-info';
        case 'CANCELLED': return 'bg-danger';
        default: return 'bg-secondary';
    }
}

function getAdminStatusText(status) {
    switch(status) {
        case 'PAID': return '✅ Оплачен';
        case 'PROCESSING': return '🔄 В работе';
        case 'SHIPPED': return '📦 Отправлен';
        case 'CANCELLED': return '❌ Отменён';
        default: return status;
    }
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