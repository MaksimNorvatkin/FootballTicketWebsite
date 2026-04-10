// stadium.js - функции для стадиона
function filterBySector(element) {
    const sectorId = element.getAttribute('data-sector-id');
    const matchId = window.location.pathname.split('/').pop();
    window.location.href = '/match/' + matchId + '?sectorId=' + sectorId;
}

function highlightActiveSector() {
    const urlParams = new URLSearchParams(window.location.search);
    const selectedSectorId = urlParams.get('sectorId');
    if (selectedSectorId) {
        document.querySelectorAll('.sector').forEach(sector => {
            if (sector.getAttribute('data-sector-id') === selectedSectorId) {
                sector.classList.add('active');
            }
        });
    }
}

document.addEventListener('DOMContentLoaded', highlightActiveSector);