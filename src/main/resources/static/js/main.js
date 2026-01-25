// main.js

document.addEventListener('DOMContentLoaded', function() {
    console.log("UI layout loaded successfully");
    console.log("Thymeleaf templates rendered");
    
    // Check if elements are loaded
    const navbar = document.querySelector('.navbar');
    const mainContent = document.querySelector('.main-content');
    const footer = document.querySelector('.footer');
    
    if (navbar && mainContent && footer) {
        console.log("✓ Navbar loaded");
        console.log("✓ Main content loaded");
        console.log("✓ Footer loaded");
    }
});
