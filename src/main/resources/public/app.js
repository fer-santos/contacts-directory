/**
 * Amber Minimal Dashboard Logic
 */

const OWNER_EMAIL = 'admin@amber.com';
const API_URL = `/api/contacts?ownerEmail=${encodeURIComponent(OWNER_EMAIL)}`;

let contacts = [];
let selectedContactIndex = -1;
let isEditing = false;
let currentView = 'all'; // 'all', 'favorites', 'trash'
let theme = localStorage.getItem('theme') || 'dark';

// DOM Elements
const contactList = document.getElementById('contactList');
const detailPanel = document.getElementById('detailPanel');
const contactCount = document.getElementById('contactCount');
const searchInput = document.getElementById('searchInput');
const viewTitle = document.getElementById('viewTitle');

// User Profile & Theme
const userProfileBtn = document.getElementById('userProfileBtn');
const themeToggleBtn = document.getElementById('themeToggleBtn');
const themeIcon = document.getElementById('themeIcon');
const themeLabel = document.getElementById('themeLabel');

// Modal Elements
const contactModal = document.getElementById('contactModal');
const addContactBtn = document.getElementById('addContactBtn');
const closeModal = document.getElementById('closeModal');
const contactForm = document.getElementById('contactForm');
const modalTitle = document.getElementById('modalTitle');

// Nav items
const navItems = document.querySelectorAll('.nav-item');

/**
 * Initialization
 */
document.addEventListener('DOMContentLoaded', () => {
    applyTheme(theme);
    lucide.createIcons();
    fetchContacts();
    setupEventListeners();
});

function applyTheme(newTheme) {
    theme = newTheme;
    localStorage.setItem('theme', theme);
    if (theme === 'dark') {
        document.documentElement.classList.add('dark');
        document.documentElement.classList.remove('light');
        if (themeIcon) {
            themeIcon.setAttribute('data-lucide', 'sun');
        }
        if (themeLabel) {
            themeLabel.textContent = 'Dark';
        }
    } else {
        document.documentElement.classList.add('light');
        document.documentElement.classList.remove('dark');
        if (themeIcon) {
            themeIcon.setAttribute('data-lucide', 'moon');
        }
        if (themeLabel) {
            themeLabel.textContent = 'Light';
        }
    }
    lucide.createIcons();
}

/**
 * Fetch contacts from backend
 */
async function fetchContacts() {
    try {
        const response = await fetch(API_URL);
        if (!response.ok) throw new Error('Failed to fetch contacts');
        
        contacts = await response.json();
        renderFilteredContacts();
        
    } catch (error) {
        console.error('Error:', error);
        contactList.innerHTML = `<div class="loading">Error loading contacts: ${error.message}</div>`;
    }
}

function renderFilteredContacts() {
    const query = searchInput.value.toLowerCase();
    
    // Map with original indices
    let filtered = contacts.map((c, i) => ({...c, originalIndex: i}));
    
    // Filter by view
    if (currentView === 'all') {
        filtered = filtered.filter(c => !c.trashed);
    } else if (currentView === 'favorites') {
        filtered = filtered.filter(c => c.favorite && !c.trashed);
    } else if (currentView === 'trash') {
        filtered = filtered.filter(c => c.trashed);
    }
    
    // Filter by query
    if (query) {
        filtered = filtered.filter(c => 
            (c.name && c.name.toLowerCase().includes(query)) || 
            (c.email && c.email.toLowerCase().includes(query)) ||
            (c.phoneNumber && c.phoneNumber.includes(query))
        );
    }
    
    renderContactList(filtered);
    
    if (filtered.length > 0) {
        // Find if currently selected contact is in the filtered list
        const isCurrentlySelectedInFiltered = selectedContactIndex !== -1 && filtered.some(c => c.originalIndex === selectedContactIndex);
        
        if (!isCurrentlySelectedInFiltered) {
             // Select the first one in the filtered list
             selectContact(filtered[0].originalIndex);
        } else {
             // Just re-render detail for selected
             renderContactDetail(contacts[selectedContactIndex]);
        }
    } else {
        selectedContactIndex = -1;
        renderEmptyDetail();
    }
}

/**
 * Render the sidebar contact list
 */
function renderContactList(list) {
    contactList.innerHTML = '';
    contactCount.textContent = `${list.length} ${list.length === 1 ? 'contact' : 'contacts'}`;

    if (list.length === 0) {
        contactList.innerHTML = '<div class="loading">No contacts found</div>';
        return;
    }

    list.forEach((contact) => {
        const index = contact.originalIndex;
        const card = document.createElement('button');
        card.className = `contact-card ${index === selectedContactIndex ? 'selected' : ''}`;
        card.onclick = () => selectContact(index);

        const initial = contact.name ? contact.name.trim().charAt(0).toUpperCase() : '?';

        card.innerHTML = `
            <div class="avatar-circle">${initial}</div>
            <div class="contact-info-mini">
                <h4>${contact.name}</h4>
            </div>
        `;
        contactList.appendChild(card);
    });
}

/**
 * Handle contact selection
 */
function selectContact(index) {
    if (index === -1) return;
    selectedContactIndex = index;
    
    // Quick re-render to update selected class visually
    renderFilteredContacts();
}

/**
 * Render the detail panel for a specific contact
 */
function renderContactDetail(contact) {
    if(!contact) return;
    const initial = contact.name ? contact.name.trim().charAt(0).toUpperCase() : '?';

    let actionButtons = '';
    let actionsClass = 'detail-actions';
    
    if (contact.trashed) {
        actionsClass = 'detail-actions two-cols';
        actionButtons = `
            <button class="btn-secondary" onclick="handleRestore()">
                <i data-lucide="rotate-ccw"></i>
                <span>Restore</span>
            </button>
            <button class="btn-secondary btn-danger" onclick="handlePermanentDelete()">
                <i data-lucide="trash-2"></i>
                <span>Delete</span>
            </button>
        `;
    } else {
        const favIcon = contact.favorite ? 'star-off' : 'star';
        const favText = contact.favorite ? 'Unfavorite' : 'Favorite';
        actionButtons = `
            <button class="btn-secondary btn-edit" onclick="handleEdit()">
                <i data-lucide="edit-2"></i>
                <span>Edit</span>
            </button>
            <button class="btn-secondary" onclick="handleToggleFavorite()">
                <i data-lucide="${favIcon}"></i>
                <span>${favText}</span>
            </button>
            <button class="btn-secondary btn-danger" onclick="handleTrash()">
                <i data-lucide="trash-2"></i>
                <span>Delete</span>
            </button>
        `;
    }

    detailPanel.innerHTML = `
        <div class="detail-card">
            <div class="detail-content">
                <div class="detail-header">
                    <div class="avatar-large">${initial}</div>
                    <h1>${contact.name}</h1>
                </div>

                <div class="detail-info-grid">
                    <div class="info-item">
                        <div class="info-label">
                            <i data-lucide="phone"></i>
                            <span>Phone</span>
                        </div>
                        <p class="info-value">${contact.phoneNumber || 'N/A'}</p>
                    </div>

                    <div class="info-item">
                        <div class="info-label">
                            <i data-lucide="mail"></i>
                            <span>Email</span>
                        </div>
                        <p class="info-value">${contact.email || 'N/A'}</p>
                    </div>
                </div>

                <div class="${actionsClass}">
                    ${actionButtons}
                </div>
            </div>
        </div>
    `;
    
    lucide.createIcons();
}

/**
 * Render empty state in detail panel
 */
function renderEmptyDetail() {
    detailPanel.innerHTML = `
        <div class="detail-card">
            <div class="empty-state">
                <div class="empty-icon">
                    <i data-lucide="mail"></i>
                </div>
                <h3>No Contact Selected</h3>
                <p class="text-secondary">Select a contact from the list to view details</p>
            </div>
        </div>
    `;
    lucide.createIcons();
}

/**
 * Setup Event Listeners
 */
function setupEventListeners() {
    // Search logic
    searchInput.addEventListener('input', () => {
        renderFilteredContacts();
    });
    
    // User Profile Dropdown
    userProfileBtn.addEventListener('click', (e) => {
        // Prevent closing immediately
        e.stopPropagation();
        userProfileBtn.classList.toggle('open');
    });
    
    document.addEventListener('click', (e) => {
        if (!userProfileBtn.contains(e.target)) {
            userProfileBtn.classList.remove('open');
        }
    });
    
    // Theme Toggle
    themeToggleBtn.addEventListener('click', (e) => {
        e.stopPropagation();
        applyTheme(theme === 'dark' ? 'light' : 'dark');
    });
    
    // Navigation
    navItems.forEach(item => {
        item.addEventListener('click', () => {
            navItems.forEach(nav => nav.classList.remove('active'));
            item.classList.add('active');
            
            currentView = item.getAttribute('data-view');
            if(currentView === 'all') viewTitle.textContent = 'All Contacts';
            if(currentView === 'favorites') viewTitle.textContent = 'Favorites';
            if(currentView === 'trash') viewTitle.textContent = 'Trash';
            
            // Re-render list
            renderFilteredContacts();
        });
    });

    // Modal logic
    addContactBtn.onclick = () => {
        isEditing = false;
        modalTitle.textContent = 'Add Contact';
        contactForm.reset();
        contactModal.classList.add('show');
    };

    closeModal.onclick = () => {
        contactModal.classList.remove('show');
    };

    contactModal.onclick = (event) => {
        if (event.target === contactModal) {
            contactModal.classList.remove('show');
        }
    };

    contactForm.onsubmit = (e) => {
        e.preventDefault();
        const formData = new FormData(contactForm);
        const data = Object.fromEntries(formData.entries());
        if (isEditing) {
            handleSaveEdit(data);
        } else {
            handleAdd(data);
        }
    };
}

/**
 * Action logic
 */
async function handleAdd(data) {
    try {
        const fullName = `${data.firstName || ''} ${data.lastName || ''}`.trim();
        const response = await fetch(API_URL, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                name: fullName,
                email: data.email,
                phoneNumber: data.phone,
                favorite: false,
                trashed: false
            })
        });

        if (response.status === 201) {
            contactModal.classList.remove('show');
            contactForm.reset();
            searchInput.value = ''; // clear search
            selectedContactIndex = contacts.length; // Will select the new one at the end
            fetchContacts();
        } else {
            console.error('Failed to add contact:', response.status);
            alert('Error adding contact. Please check your data.');
        }
    } catch (error) {
        console.error('Error during handleAdd:', error);
        alert('Network error. Could not reach the server.');
    }
}

// Attach functions to global scope if they are called from inline HTML handlers
window.handleEdit = function() {
    if (selectedContactIndex < 0 || selectedContactIndex >= contacts.length) return;
    
    isEditing = true;
    const contact = contacts[selectedContactIndex];
    modalTitle.textContent = 'Edit Contact';
    
    const nameParts = (contact.name || '').split(' ');
    contactForm.firstName.value = nameParts[0] || '';
    contactForm.lastName.value = nameParts.slice(1).join(' ') || '';
    contactForm.email.value = contact.email || '';
    contactForm.phone.value = contact.phoneNumber || '';
    
    contactModal.classList.add('show');
};

async function handleSaveEdit(data) {
    try {
        const fullName = `${data.firstName || ''} ${data.lastName || ''}`.trim();
        const existingContact = contacts[selectedContactIndex];
        const response = await fetch(`${API_URL}&contactIndex=${selectedContactIndex}`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                ...existingContact,
                name: fullName,
                email: data.email,
                phoneNumber: data.phone
            })
        });

        if (response.ok) {
            contactModal.classList.remove('show');
            contactForm.reset();
            fetchContacts();
        } else {
            const errorMsg = await response.text();
            console.error('Failed to edit contact:', response.status, errorMsg);
            alert(`Error editing contact: ${errorMsg}`);
        }
    } catch (error) {
        console.error('Error during handleSaveEdit:', error);
        alert('Network error. Could not reach the server.');
    }
}

window.handleTrash = async function() {
    if (selectedContactIndex < 0 || selectedContactIndex >= contacts.length) return;
    
    const contact = contacts[selectedContactIndex];
    if (confirm(`Are you sure you want to delete ${contact.name}?`)) {
        contact.trashed = true;
        await updateContact(selectedContactIndex, contact);
    }
};

window.handlePermanentDelete = async function() {
    if (selectedContactIndex < 0 || selectedContactIndex >= contacts.length) return;
    
    if (confirm(`Permanently delete this contact?`)) {
        try {
            const response = await fetch(`${API_URL}&contactIndex=${selectedContactIndex}`, {
                method: 'DELETE'
            });
            if (response.ok) {
                selectedContactIndex = -1;
                fetchContacts();
            } else {
                const errorMsg = await response.text();
                alert(`Error deleting contact: ${errorMsg}`);
            }
        } catch (error) {
            console.error('Error during delete:', error);
            alert('Network error.');
        }
    }
};

window.handleRestore = async function() {
    if (selectedContactIndex < 0 || selectedContactIndex >= contacts.length) return;
    
    const contact = contacts[selectedContactIndex];
    contact.trashed = false;
    await updateContact(selectedContactIndex, contact);
};

window.handleToggleFavorite = async function() {
    if (selectedContactIndex < 0 || selectedContactIndex >= contacts.length) return;
    
    const contact = contacts[selectedContactIndex];
    contact.favorite = !contact.favorite;
    await updateContact(selectedContactIndex, contact);
};

async function updateContact(index, contactData) {
    try {
        const response = await fetch(`${API_URL}&contactIndex=${index}`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(contactData)
        });

        if (response.ok) {
            fetchContacts();
        } else {
            const errorMsg = await response.text();
            console.error('Failed to update contact:', response.status, errorMsg);
            alert(`Error updating contact: ${errorMsg}`);
        }
    } catch (error) {
        console.error('Error during updateContact:', error);
        alert('Network error. Could not reach the server.');
    }
}
