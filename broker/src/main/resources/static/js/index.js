// Initialize the tracker at the very top of the file
let lastSelected = {
    selectedVenue: null,
    selectedCatering: null
};

function filterOptions() {
    const input = document.getElementById('guestCount');
    if (!input) return;

    const count = parseInt(input.value) || 0;

    // 1. Filter Venues
    const venues = document.querySelectorAll('.venue-card');
    let visibleVenues = 0;
    venues.forEach(card => {
        const capacity = parseInt(card.getAttribute('data-capacity'));
        if (isNaN(count) || capacity >= count) {
            card.style.display = "flex";
            visibleVenues++;
        } else {
            card.style.display = "none";
        }
    });

    // 2. Filter Catering
    const caterings = document.querySelectorAll('.catering-card');
    let visibleCaterings = 0;
    caterings.forEach(card => {
        const maxGuests = parseInt(card.getAttribute('data-max'));
        if (isNaN(count) || maxGuests >= count) {
            card.style.display = "flex";
            visibleCaterings++;
        } else {
            card.style.display = "none";
        }
    });

    // Toggle Messages
    document.getElementById('venue-empty-msg').style.display = (visibleVenues === 0 && count > 0) ? "block" : "none";
    document.getElementById('catering-empty-msg').style.display = (visibleCaterings === 0 && count > 0) ? "block" : "none";
}

function handleRadioClick(radio) {
    const name = radio.name;

    // Toggle logic: If clicking the same one again, uncheck it
    if (lastSelected[name] === radio) {
        radio.checked = false;
        lastSelected[name] = null;

        // Reset filters because we unselected something
        filterOptions();
    } else {
        lastSelected[name] = radio;
        syncSelections(); // Only sync if something is actually selected
    }
}

function syncSelections() {
    const selectedVenue = document.querySelector('input[name="selectedVenue"]:checked');
    const selectedCatering = document.querySelector('input[name="selectedCatering"]:checked');

    // If a Venue is picked, hide Catering that doesn't match the capacity EXACTLY
    if (selectedVenue) {
        const venueCap = selectedVenue.closest('.venue-card').getAttribute('data-capacity');
        document.querySelectorAll('.catering-card').forEach(card => {
            if (card.getAttribute('data-max') !== venueCap) {
                card.style.display = "none";
            }
        });
    }

    // If a Catering is picked, hide Venues that don't match the max guests EXACTLY
    if (selectedCatering) {
        const catMax = selectedCatering.closest('.catering-card').getAttribute('data-max');
        document.querySelectorAll('.venue-card').forEach(card => {
            if (card.getAttribute('data-capacity') !== catMax) {
                card.style.display = "none";
            }
        });
    }
}