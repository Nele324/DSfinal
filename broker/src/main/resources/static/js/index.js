let lastSelected = {
    selectedVenue: null,
    selectedCatering: null
};

document.addEventListener('DOMContentLoaded', () => {
    const reviewBtn = document.getElementById('reviewButton');
    const status = document.querySelector('.status-badge span');

    // check if either supplier list is empty
    const cateringCards = document.querySelectorAll('.catering-card');
    const venueCards = document.querySelectorAll('.venue-card');

    if (cateringCards.length === 0 || venueCards.length === 0) {
        reviewBtn.disabled = true;
        reviewBtn.style.backgroundColor = '#95a5a6';
        reviewBtn.style.cursor = 'not-allowed';
        reviewBtn.textContent = '⚠️ Cannot proceed — one or more suppliers unavailable';
    }
});

function handleRadioClick(radio) {
    const name = radio.name;

    if (lastSelected[name] === radio) {
        // same radio clicked again — uncheck it
        radio.checked = false;
        lastSelected[name] = null;
    } else {
        lastSelected[name] = radio;
    }

    // always re-run the filter after any click
    syncSelections();
}

function syncSelections() {
    const selectedVenue    = document.querySelector('input[name="selectedVenue"]:checked');
    const selectedCatering = document.querySelector('input[name="selectedCatering"]:checked');

    // --- reset everything to visible first ---
    document.querySelectorAll('.catering-card').forEach(card => card.style.display = 'flex');
    document.querySelectorAll('.venue-card').forEach(card => card.style.display = 'flex');

    // --- then apply filters only if something is selected ---
    if (selectedVenue) {
        const venueCap = parseInt(selectedVenue.closest('.venue-card').getAttribute('data-capacity'));
        document.querySelectorAll('.catering-card').forEach(card => {
            if (parseInt(card.getAttribute('data-max')) > venueCap) {
                card.style.display = 'none';
            }
        });
    }

    if (selectedCatering) {
        const catMax = parseInt(selectedCatering.closest('.catering-card').getAttribute('data-max'));
        document.querySelectorAll('.venue-card').forEach(card => {
            if (parseInt(card.getAttribute('data-capacity')) < catMax) {
                card.style.display = 'none';
            }
        });
    }
}