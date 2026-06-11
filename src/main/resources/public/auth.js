document.addEventListener('DOMContentLoaded', () => {
    const loginForm = document.getElementById('loginForm');
    if (loginForm) {
        loginForm.addEventListener('submit', (e) => {
            e.preventDefault();
            const formData = new FormData(loginForm);
            const email = formData.get('email');
            
            // Validate basic required fields
            if (!email) {
                alert('Email is required.');
                return;
            }

            // Simulate successful POST to login and get token/email
            localStorage.setItem('ownerEmail', email);
            window.location.href = '/';
        });
    }

    const registerForm = document.getElementById('registerForm');
    if (registerForm) {
        registerForm.addEventListener('submit', (e) => {
            e.preventDefault();
            
            // Validation
            const formData = new FormData(registerForm);
            const firstName = formData.get('firstName');
            const email = formData.get('email');
            const password = formData.get('password');

            if (!firstName || !email || !password) {
                alert('Please fill in all required fields.');
                return;
            }

            if (password.length < 8) {
                alert('Password must be at least 8 characters long.');
                return;
            }

            // Simulate API /register success and redirect
            alert('Registration successful! Please log in.');
            window.location.href = 'login.html';
        });
    }
});
