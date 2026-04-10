// header.js
function updateCartCount() {
    fetch('/cart/count')
        .then(response => response.json())
        .then(data => {
            const cartCountSpan = document.getElementById('cartCount');
            if (data && data.count > 0) {
                cartCountSpan.textContent = data.count;
                cartCountSpan.style.display = 'inline-block';
            } else {
                cartCountSpan.style.display = 'none';
            }
        })
        .catch(error => console.error('Error fetching cart count:', error));
}

// Эффект тени при скролле
function addStickyScrollEffect() {
    const navbar = document.querySelector('.navbar-custom');
    if (navbar) {
        window.addEventListener('scroll', function() {
            if (window.scrollY > 50) {
                navbar.classList.add('sticky-scroll');
            } else {
                navbar.classList.remove('sticky-scroll');
            }
        });
    }
}

document.addEventListener('DOMContentLoaded', function() {
    updateCartCount();
    addStickyScrollEffect();
});