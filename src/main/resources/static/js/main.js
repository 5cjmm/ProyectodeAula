/**
 * ShopMaster - Main JavaScript File
 * Handles navigation, interactions, and UI enhancements
 */

class ShopMasterApp {
  constructor() {
    this.init();
  }

  init() {
    this.setupEventListeners();
    this.setupMobileMenu();
    this.setupLoadingStates();
    this.setupImageErrorHandling();
    this.setupSmoothScrolling();
    this.setupAccessibility();
  }

  /**
   * Setup all event listeners
   */
  setupEventListeners() {
    // Mobile menu toggle
    const mobileMenuButton = document.getElementById('mobile-menu-button');
    const mobileMenu = document.getElementById('mobile-menu');

    if (mobileMenuButton && mobileMenu) {
      mobileMenuButton.addEventListener('click', () => {
        this.toggleMobileMenu(mobileMenuButton, mobileMenu);
      });

      // Close mobile menu when clicking outside
      document.addEventListener('click', (e) => {
        if (!mobileMenuButton.contains(e.target) && !mobileMenu.contains(e.target)) {
          this.closeMobileMenu(mobileMenuButton, mobileMenu);
        }
      });

      // Close mobile menu on escape key
      document.addEventListener('keydown', (e) => {
        if (e.key === 'Escape') {
          this.closeMobileMenu(mobileMenuButton, mobileMenu);
        }
      });
    }

    // Button click handlers with loading states
    this.setupButtonHandlers();
  }

  /**
   * Setup mobile menu functionality
   */
  setupMobileMenu() {
    const mobileMenuLinks = document.querySelectorAll('.mobile-nav-link');
    
    mobileMenuLinks.forEach(link => {
      link.addEventListener('click', () => {
        const mobileMenuButton = document.getElementById('mobile-menu-button');
        const mobileMenu = document.getElementById('mobile-menu');
        this.closeMobileMenu(mobileMenuButton, mobileMenu);
      });
    });
  }

  /**
   * Toggle mobile menu
   */
  toggleMobileMenu(button, menu) {
    const isExpanded = button.getAttribute('aria-expanded') === 'true';
    const menuIcon = button.querySelector('.menu-icon');
    const closeIcon = button.querySelector('.close-icon');

    if (isExpanded) {
      this.closeMobileMenu(button, menu);
    } else {
      this.openMobileMenu(button, menu);
    }

    // Toggle icons
    if (menuIcon && closeIcon) {
      menuIcon.classList.toggle('hidden');
      closeIcon.classList.toggle('hidden');
    }
  }

  /**
   * Open mobile menu
   */
  openMobileMenu(button, menu) {
    button.setAttribute('aria-expanded', 'true');
    menu.classList.add('show');
    document.body.style.overflow = 'hidden';
    
    // Focus first menu item
    const firstMenuItem = menu.querySelector('.mobile-nav-link');
    if (firstMenuItem) {
      firstMenuItem.focus();
    }
  }

  /**
   * Close mobile menu
   */
  closeMobileMenu(button, menu) {
    button.setAttribute('aria-expanded', 'false');
    menu.classList.remove('show');
    document.body.style.overflow = '';
    
    const menuIcon = button.querySelector('.menu-icon');
    const closeIcon = button.querySelector('.close-icon');
    
    if (menuIcon && closeIcon) {
      menuIcon.classList.remove('hidden');
      closeIcon.classList.add('hidden');
    }
  }

  /**
   * Setup button handlers with loading states
   */
  setupButtonHandlers() {
    const buttons = document.querySelectorAll('.btn');
    
    buttons.forEach(button => {
      if (button.href && !button.href.startsWith('#')) {
        button.addEventListener('click', (e) => {
          this.handleButtonClick(button, e);
        });
      }
    });
  }

  /**
   * Handle button clicks with loading states
   */
  handleButtonClick(button, event) {
    // Don't show loading for external links or anchors
    if (button.href.startsWith('http') && !button.href.includes(window.location.hostname)) {
      return;
    }

    // Show loading state
    this.showLoadingState(button);
    
    // Show global loading indicator for navigation
    if (button.href && !button.href.startsWith('#')) {
      this.showLoadingIndicator();
    }
  }

  /**
   * Show loading state on button
   */
  showLoadingState(button) {
    const originalText = button.textContent;
    const spinner = this.createSpinner();
    
    button.disabled = true;
    button.style.opacity = '0.7';
    button.innerHTML = '';
    button.appendChild(spinner);
    button.appendChild(document.createTextNode('Cargando...'));
    
    // Store original content
    button.dataset.originalContent = originalText;
  }

  /**
   * Create loading spinner element
   */
  createSpinner() {
    const spinner = document.createElement('div');
    spinner.className = 'inline-block w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin';
    spinner.style.animation = 'spin 1s linear infinite';
    return spinner;
  }

  /**
   * Setup loading states
   */
  setupLoadingStates() {
    // Hide loading indicator on page load
    window.addEventListener('load', () => {
      this.hideLoadingIndicator();
    });

    // Show loading indicator for form submissions
    const forms = document.querySelectorAll('form');
    forms.forEach(form => {
      form.addEventListener('submit', () => {
        this.showLoadingIndicator();
      });
    });
  }

  /**
   * Show global loading indicator
   */
  showLoadingIndicator() {
    const indicator = document.getElementById('loading-indicator');
    if (indicator) {
      indicator.classList.add('show');
    }
  }

  /**
   * Hide global loading indicator
   */
  hideLoadingIndicator() {
    const indicator = document.getElementById('loading-indicator');
    if (indicator) {
      indicator.classList.remove('show');
    }
  }

  /**
   * Setup image error handling
   */
  setupImageErrorHandling() {
    const images = document.querySelectorAll('img');
    
    images.forEach(img => {
      img.addEventListener('error', () => {
        this.handleImageError(img);
      });
      
      // Add loading state
      img.addEventListener('load', () => {
        img.classList.add('loaded');
      });
    });
  }

  /**
   * Handle image loading errors
   */
  handleImageError(img) {
    // Try fallback image if specified
    if (img.dataset.fallback && img.src !== img.dataset.fallback) {
      img.src = img.dataset.fallback;
      return;
    }

    // Create placeholder
    const placeholder = this.createImagePlaceholder(img); 
if (img.parentNode) {
  img.parentNode.replaceChild(placeholder, img);
}
  }

  /**
   * Create image placeholder
   */
  createImagePlaceholder(img) {
    const placeholder = document.createElement('div');
    placeholder.className = 'bg-gray-200 flex items-center justify-center text-gray-500 rounded';
    placeholder.style.width = img.width ? `${img.width}px` : '100%';
    placeholder.style.height = img.height ? `${img.height}px` : '200px';
    placeholder.innerHTML = `
      <svg class="w-12 h-12" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z"></path>
      </svg>
    `;
    return placeholder;
  }

  /**
   * Setup smooth scrolling for anchor links
   */
  setupSmoothScrolling() {
    const anchorLinks = document.querySelectorAll('a[href^="#"]');
    
    anchorLinks.forEach(link => {
      link.addEventListener('click', (e) => {
        const targetId = link.getAttribute('href').substring(1);
        const targetElement = document.getElementById(targetId);
        
        if (targetElement) {
          e.preventDefault();
          this.smoothScrollTo(targetElement);
        }
      });
    });
  }

  /**
   * Smooth scroll to element
   */
  smoothScrollTo(element) {
    const headerHeight = document.querySelector('.header')?.offsetHeight || 0;
    const targetPosition = element.offsetTop - headerHeight - 20;
    
    window.scrollTo({
      top: targetPosition,
      behavior: 'smooth'
    });
  }

  /**
   * Setup accessibility enhancements
   */
  setupAccessibility() {
    // Keyboard navigation for mobile menu
    this.setupKeyboardNavigation();
    
    // Focus management
    this.setupFocusManagement();
    
    // ARIA live regions for dynamic content
    this.setupAriaLiveRegions();
  }

  /**
   * Setup keyboard navigation
   */
  setupKeyboardNavigation() {
    const mobileMenu = document.getElementById('mobile-menu');
    
    if (mobileMenu) {
      mobileMenu.addEventListener('keydown', (e) => {
        const focusableElements = mobileMenu.querySelectorAll('a, button');
        const firstElement = focusableElements[0];
        const lastElement = focusableElements[focusableElements.length - 1];
        
        if (e.key === 'Tab') {
          if (e.shiftKey) {
            if (document.activeElement === firstElement) {
              e.preventDefault();
              lastElement.focus();
            }
          } else {
            if (document.activeElement === lastElement) {
              e.preventDefault();
              firstElement.focus();
            }
          }
        }
      });
    }
  }

  /**
   * Setup focus management
   */
  setupFocusManagement() {
    // Skip to main content link
    const skipLink = document.querySelector('a[href="#main-content"]');
    if (skipLink) {
      skipLink.addEventListener('click', (e) => {
        e.preventDefault();
        const mainContent = document.getElementById('main-content');
        if (mainContent) {
          mainContent.focus();
          mainContent.scrollIntoView();
        }
      });
    }
  }

  /**
   * Setup ARIA live regions
   */
  setupAriaLiveRegions() {
    // Create live region for announcements
    const liveRegion = document.createElement('div');
    liveRegion.setAttribute('aria-live', 'polite');
    liveRegion.setAttribute('aria-atomic', 'true');
    liveRegion.className = 'sr-only';
    liveRegion.id = 'live-region';
    document.body.appendChild(liveRegion);
  }

  /**
   * Announce message to screen readers
   */
  announceMessage(message) {
    const liveRegion = document.getElementById('live-region');
    if (liveRegion) {
      liveRegion.textContent = message;
      setTimeout(() => {
        liveRegion.textContent = '';
      }, 1000);
    }
  }

  /**
   * Utility method to debounce function calls
   */
  debounce(func, wait) {
    let timeout;
    return function executedFunction(...args) {
      const later = () => {
        clearTimeout(timeout);
        func(...args);
      };
      clearTimeout(timeout);
      timeout = setTimeout(later, wait);
    };
  }

  /**
   * Utility method to throttle function calls
   */
  throttle(func, limit) {
    let inThrottle;
    return function() {
      const args = arguments;
      const context = this;
      if (!inThrottle) {
        func.apply(context, args);
        inThrottle = true;
        setTimeout(() => inThrottle = false, limit);
      }
    };
  }
}

// Initialize the application when DOM is loaded
document.addEventListener('DOMContentLoaded', () => {
  new ShopMasterApp();
});

// Handle page visibility changes
document.addEventListener('visibilitychange', () => {
  if (document.hidden) {
    // Page is hidden
    console.log('Page hidden');
  } else {
    // Page is visible
    console.log('Page visible');
  }
});

// Performance monitoring
if ('performance' in window) {
  window.addEventListener('load', () => {
    setTimeout(() => {
      const perfData = performance.getEntriesByType('navigation')[0];
      console.log('Page load time:', perfData.loadEventEnd - perfData.loadEventStart, 'ms');
    }, 0);
  });
}