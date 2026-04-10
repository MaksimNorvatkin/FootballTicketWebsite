// match-detail.js
function filterBySector(element) {
    const sectorId = element.getAttribute('data-sector-id');
    const matchId = window.location.pathname.split('/').pop();
    window.location.href = '/match/' + matchId + '?sectorId=' + sectorId;
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
    }).catch(error => console.error('Error adding to cart:', error));
}

function highlightActiveSector() {
    const urlParams = new URLSearchParams(window.location.search);
    const selectedSectorId = urlParams.get('sectorId');

    if (selectedSectorId) {
        document.querySelectorAll('.simple-sector').forEach(sector => {
            if (sector.getAttribute('data-sector-id') === selectedSectorId) {
                sector.classList.add('active');
            }
        });
    }
}

document.addEventListener('DOMContentLoaded', function() {
    highlightActiveSector();
});