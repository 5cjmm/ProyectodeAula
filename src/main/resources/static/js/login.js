/**
 * ShopMaster - Login Page JavaScript
 * Handles login form functionality, validation, and password visibility toggle
 */

class LoginPage {
  constructor() {
    // DOM elements
    this.form = document.getElementById("loginForm")
    this.submitButton = document.getElementById("loginButton")
    this.usernameField = document.getElementById("username")
    this.passwordField = document.getElementById("password")
    this.passwordToggle = document.getElementById("password-toggle")
    this.showIcon = this.passwordToggle?.querySelector(".show-icon")
    this.hideIcon = this.passwordToggle?.querySelector(".hide-icon")

    // Configuration
    this.config = {
      minUsernameLength: 3,
      minPasswordLength: 4,
      validationDelay: 300,
    }

    // State
    this.isSubmitting = false
    this.validationTimeouts = {}

    this.init()
  }

  /**
   * Initialize the login page functionality
   */
  init() {
    // Check if required elements exist
    if (!this.form || !this.submitButton || !this.usernameField || !this.passwordField) {
      console.warn("LoginPage: Required elements not found")
      return
    }

    this.setupEventListeners()
    this.setupValidation()
    this.setupPasswordToggle()
    this.focusFirstField()
    this.setupAccessibility()
  }

  /**
   * Setup all event listeners
   */
  setupEventListeners() {
    // Form submission
    this.form.addEventListener("submit", (e) => {
      this.handleSubmit(e)
    })

    // Real-time validation with debouncing
    this.usernameField.addEventListener("input", () => {
      this.debounceValidation("username", () => this.validateUsername())
      this.clearError("username")
    })

    this.passwordField.addEventListener("input", () => {
      this.debounceValidation("password", () => this.validatePassword())
      this.clearError("password")
    })

    // Blur validation (immediate)
    this.usernameField.addEventListener("blur", () => {
      this.validateUsername()
    })

    this.passwordField.addEventListener("blur", () => {
      this.validatePassword()
    })

    // Enter key navigation
    this.usernameField.addEventListener("keypress", (e) => {
      if (e.key === "Enter") {
        e.preventDefault()
        this.passwordField.focus()
      }
    })

    this.passwordField.addEventListener("keypress", (e) => {
      if (e.key === "Enter" && this.validateForm()) {
        this.form.submit()
      }
    })

    // Handle browser back/forward
    window.addEventListener("pageshow", (e) => {
      if (e.persisted) {
        this.resetLoadingState()
      }
    })
  }

  /**
   * Setup password visibility toggle functionality
   */
  setupPasswordToggle() {
    if (!this.passwordToggle) return

    // Click event
    this.passwordToggle.addEventListener("click", (e) => {
      e.preventDefault()
      this.togglePasswordVisibility()
    })

    // Keyboard support
    this.passwordToggle.addEventListener("keydown", (e) => {
      if (e.key === "Enter" || e.key === " ") {
        e.preventDefault()
        this.togglePasswordVisibility()
      }
    })
  }

  /**
   * Toggle password field visibility
   */
  togglePasswordVisibility() {
    const isPassword = this.passwordField.type === "password"

    if (isPassword) {
      // Show password
      this.passwordField.type = "text"
      this.showIcon?.classList.add("hidden")
      this.hideIcon?.classList.remove("hidden")
      this.passwordToggle.setAttribute("aria-label", "Ocultar contraseña")
      this.announceToScreenReader("Contraseña visible")
    } else {
      // Hide password
      this.passwordField.type = "password"
      this.showIcon?.classList.remove("hidden")
      this.hideIcon?.classList.add("hidden")
      this.passwordToggle.setAttribute("aria-label", "Mostrar contraseña")
      this.announceToScreenReader("Contraseña oculta")
    }

    // Maintain focus on password field
    this.passwordField.focus()

    // Move cursor to end
    const length = this.passwordField.value.length
    this.passwordField.setSelectionRange(length, length)
  }

  /**
   * Setup form validation
   */
  setupValidation() {
    // Custom validation messages
    this.usernameField.addEventListener("invalid", (e) => {
      e.preventDefault()
      this.showError("username", "Por favor, ingresa tu usuario")
    })

    this.passwordField.addEventListener("invalid", (e) => {
      e.preventDefault()
      this.showError("password", "Por favor, ingresa tu contraseña")
    })
  }

  /**
   * Handle form submission
   */
  handleSubmit(e) {
    // Prevent double submission
    if (this.isSubmitting) {
      e.preventDefault()
      return
    }

    // Validate form
    if (!this.validateForm()) {
      e.preventDefault()
      this.focusFirstErrorField()
      return
    }

    // Show loading state
    this.showLoading()
    this.isSubmitting = true

    // Let the form submit naturally to Spring Security
    // The loading state will be cleared when the page reloads or redirects
  }

  /**
   * Validate entire form
   */
  validateForm() {
    let isValid = true

    // Clear previous validation timeouts
    Object.values(this.validationTimeouts).forEach((timeout) => {
      clearTimeout(timeout)
    })

    if (!this.validateUsername()) {
      isValid = false
    }

    if (!this.validatePassword()) {
      isValid = false
    }

    return isValid
  }

  /**
   * Validate username field
   */
  validateUsername() {
    const username = this.usernameField.value.trim()

    if (!username) {
      this.showError("username", "El usuario es requerido")
      return false
    }

    if (username.length < this.config.minUsernameLength) {
      this.showError("username", `El usuario debe tener al menos ${this.config.minUsernameLength} caracteres`)
      return false
    }

    // Check for invalid characters (optional)
    if (!/^[a-zA-Z0-9._-]+$/.test(username)) {
      this.showError("username", "El usuario solo puede contener letras, números, puntos, guiones y guiones bajos")
      return false
    }

    this.clearError("username")
    return true
  }

  /**
   * Validate password field
   */
  validatePassword() {
    const password = this.passwordField.value

    if (!password) {
      this.showError("password", "La contraseña es requerida")
      return false
    }

    if (password.length < this.config.minPasswordLength) {
      this.showError("password", `La contraseña debe tener al menos ${this.config.minPasswordLength} caracteres`)
      return false
    }

    this.clearError("password")
    return true
  }

  /**
   * Show validation error
   */
  showError(fieldName, message) {
    const field = document.getElementById(fieldName)
    const errorDiv = document.getElementById(`${fieldName}-error`)

    if (!field || !errorDiv) return

    field.classList.add("error")
    field.setAttribute("aria-invalid", "true")
    errorDiv.textContent = message
    errorDiv.classList.remove("hidden")

    this.announceToScreenReader(message)
  }

  /**
   * Clear validation error
   */
  clearError(fieldName) {
    const field = document.getElementById(fieldName)
    const errorDiv = document.getElementById(`${fieldName}-error`)

    if (!field || !errorDiv) return

    field.classList.remove("error")
    field.setAttribute("aria-invalid", "false")
    errorDiv.classList.add("hidden")
    errorDiv.textContent = ""
  }

  /**
   * Show loading state
   */
  showLoading() {
    const spinner = this.submitButton.querySelector(".btn-spinner")
    const text = this.submitButton.querySelector(".btn-text")

    if (spinner && text) {
      spinner.classList.remove("hidden")
      text.textContent = "Iniciando sesión..."
    }

    this.submitButton.disabled = true
    this.submitButton.setAttribute("aria-busy", "true")

    this.announceToScreenReader("Iniciando sesión, por favor espera...")
  }

  /**
   * Reset loading state
   */
  resetLoadingState() {
    const spinner = this.submitButton.querySelector(".btn-spinner")
    const text = this.submitButton.querySelector(".btn-text")

    if (spinner && text) {
      spinner.classList.add("hidden")
      text.textContent = "Iniciar Sesión"
    }

    this.submitButton.disabled = false
    this.submitButton.setAttribute("aria-busy", "false")
    this.isSubmitting = false
  }

  /**
   * Focus first field or first field with error
   */
  focusFirstField() {
    if (!this.usernameField.value) {
      this.usernameField.focus()
    } else if (!this.passwordField.value) {
      this.passwordField.focus()
    }
  }

  /**
   * Focus first field with validation error
   */
  focusFirstErrorField() {
    const usernameError = !document.getElementById("username-error").classList.contains("hidden")
    const passwordError = !document.getElementById("password-error").classList.contains("hidden")

    if (usernameError) {
      this.usernameField.focus()
    } else if (passwordError) {
      this.passwordField.focus()
    }
  }

  /**
   * Setup accessibility enhancements
   */
  setupAccessibility() {
    // Add ARIA live region if it doesn't exist
    if (!document.getElementById("login-live-region")) {
      const liveRegion = document.createElement("div")
      liveRegion.id = "login-live-region"
      liveRegion.setAttribute("aria-live", "polite")
      liveRegion.setAttribute("aria-atomic", "true")
      liveRegion.className = "sr-only"
      document.body.appendChild(liveRegion)
    }

    // Improve form accessibility
    this.form.setAttribute("novalidate", "true")
    this.form.setAttribute("aria-label", "Formulario de inicio de sesión")
  }

  /**
   * Announce message to screen readers
   */
  announceToScreenReader(message) {
    const liveRegion = document.getElementById("login-live-region")
    if (liveRegion) {
      liveRegion.textContent = message
      setTimeout(() => {
        liveRegion.textContent = ""
      }, 1000)
    }
  }

  /**
   * Debounce validation to avoid excessive calls
   */
  debounceValidation(fieldName, validationFn) {
    if (this.validationTimeouts[fieldName]) {
      clearTimeout(this.validationTimeouts[fieldName])
    }

    this.validationTimeouts[fieldName] = setTimeout(() => {
      validationFn()
      delete this.validationTimeouts[fieldName]
    }, this.config.validationDelay)
  }

  /**
   * Utility method to check if element is visible
   */
  isElementVisible(element) {
    return element && element.offsetParent !== null
  }

  /**
   * Clean up resources
   */
  destroy() {
    // Clear any pending timeouts
    Object.values(this.validationTimeouts).forEach((timeout) => {
      clearTimeout(timeout)
    })

    // Remove event listeners if needed
    // (In this case, they'll be cleaned up when the page unloads)
  }
}

// Initialize when DOM is loaded
document.addEventListener("DOMContentLoaded", () => {
  // Only initialize if we're on the login page
  if (document.getElementById("loginForm")) {
    window.loginPage = new LoginPage()
  }
})

// Handle page visibility changes for better UX
document.addEventListener("visibilitychange", () => {
  if (!document.hidden && window.loginPage) {
    // Page became visible, reset any loading states
    window.loginPage.resetLoadingState()
  }
})

// Export for potential use in other scripts
if (typeof module !== "undefined" && module.exports) {
  module.exports = LoginPage
}
