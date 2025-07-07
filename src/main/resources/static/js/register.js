/**
 * ShopMaster - Register Page JavaScript
 * Handles multi-step registration form, validation, and password strength
 */

class RegisterPage {
  constructor() {
    // DOM elements - Steps
    this.form = document.getElementById("registerForm")
    this.steps = Array.from(document.querySelectorAll(".form-step"))
    this.stepIndicators = Array.from(document.querySelectorAll(".step"))
    this.currentStepIndex = 0

    // DOM elements - Navigation buttons
    this.step1NextBtn = document.getElementById("step1Next")
    this.step2PrevBtn = document.getElementById("step2Prev")
    this.step2NextBtn = document.getElementById("step2Next")
    this.step3PrevBtn = document.getElementById("step3Prev")
    this.registerBtn = document.getElementById("registerButton")

    // DOM elements - Form fields
    this.usernameField = document.getElementById("username")
    this.emailField = document.getElementById("email")
    this.passwordField = document.getElementById("password")
    this.confirmPasswordField = document.getElementById("confirmPassword")
    this.storeNameField = document.getElementById("storeName")
    this.storeAddressField = document.getElementById("storeAddress")
    this.storeRutField = document.getElementById("storeRut")
    this.storeTypeField = document.getElementById("storeType")
    this.termsCheckbox = document.getElementById("termsAccepted")

    // DOM elements - Password toggles
    this.passwordToggle = document.getElementById("password-toggle")
    this.confirmPasswordToggle = document.getElementById("confirmPassword-toggle")

    // DOM elements - Password strength
    this.strengthMeter = document.querySelector(".strength-meter-fill")
    this.strengthText = document.querySelector(".strength-text")

    // DOM elements - Summary
    this.summaryUsername = document.getElementById("summary-username")
    this.summaryEmail = document.getElementById("summary-email")
    this.summaryStoreName = document.getElementById("summary-storeName")
    this.summaryStoreType = document.getElementById("summary-storeType")

    // Configuration
    this.config = {
      minUsernameLength: 3,
      maxUsernameLength: 20,
      minPasswordLength: 8,
      validationDelay: 300,
    }

    // State
    this.isSubmitting = false
    this.validationTimeouts = {}

    this.init()
  }

  /**
   * Initialize the register page functionality
   */
  init() {
    // Check if required elements exist
    if (!this.form) {
      console.warn("RegisterPage: Form not found")
      return
    }

    console.log("Initializing RegisterPage...") // Debug
    console.log("Password field:", !!this.passwordField) // Debug
    console.log("Confirm password field:", !!this.confirmPasswordField) // Debug
    console.log("Password toggle:", !!this.passwordToggle) // Debug
    console.log("Confirm password toggle:", !!this.confirmPasswordToggle) // Debug

    this.setupEventListeners()
    this.setupValidation()
    this.setupPasswordToggles()
    this.setupPasswordStrength()
    this.setupAccessibility()
    this.showCurrentStep()
  }

  /**
   * Setup all event listeners
   */
  setupEventListeners() {
    // Form submission
    this.form.addEventListener("submit", (e) => {
      this.handleSubmit(e)
    })

    // Step navigation
    if (this.step1NextBtn) {
      this.step1NextBtn.addEventListener("click", () => this.goToNextStep())
    }
    if (this.step2PrevBtn) {
      this.step2PrevBtn.addEventListener("click", () => this.goToPrevStep())
    }
    if (this.step2NextBtn) {
      this.step2NextBtn.addEventListener("click", () => this.goToNextStep())
    }
    if (this.step3PrevBtn) {
      this.step3PrevBtn.addEventListener("click", () => this.goToPrevStep())
    }

    // Real-time validation with debouncing
    this.setupFieldValidation(this.usernameField, () => this.validateUsername())
    this.setupFieldValidation(this.emailField, () => this.validateEmail())
    this.setupFieldValidation(this.passwordField, () => {
      this.updatePasswordStrength()
      this.validatePassword()
    })
    this.setupFieldValidation(this.confirmPasswordField, () => this.validateConfirmPassword())
    this.setupFieldValidation(this.storeNameField, () => this.validateStoreName())
    this.setupFieldValidation(this.storeAddressField, () => this.validateStoreAddress())
    this.setupFieldValidation(this.storeRutField, () => this.validateStoreRut())
    this.setupFieldValidation(this.storeTypeField, () => this.validateStoreType())

    // Terms checkbox
    if (this.termsCheckbox) {
      this.termsCheckbox.addEventListener("change", () => {
        this.validateTerms()
      })
    }

    // Update summary when fields change
    this.usernameField?.addEventListener("input", () => this.updateSummary())
    this.emailField?.addEventListener("input", () => this.updateSummary())
    this.storeNameField?.addEventListener("input", () => this.updateSummary())
    this.storeTypeField?.addEventListener("change", () => this.updateSummary())

    // Handle browser back/forward
    window.addEventListener("pageshow", (e) => {
      if (e.persisted) {
        this.resetLoadingState()
      }
    })
  }

  /**
   * Setup field validation with event listeners
   */
  setupFieldValidation(field, validationFn) {
    if (!field) return

    // Input event with debounce
    field.addEventListener("input", () => {
      this.debounceValidation(field.id, validationFn)
      this.clearError(field.id)
    })

    // Blur event (immediate validation)
    field.addEventListener("blur", () => {
      validationFn()
    })
  }

  /**
   * Setup password toggle functionality
   */
  setupPasswordToggles() {
    console.log("Setting up password toggles...") // Debug
    this.setupPasswordToggle(this.passwordField, this.passwordToggle, "password")
    this.setupPasswordToggle(this.confirmPasswordField, this.confirmPasswordToggle, "confirmPassword")
  }

  /**
   * Setup individual password toggle
   */
  setupPasswordToggle(field, toggle, fieldName) {
    if (!field || !toggle) {
      console.warn(`Password toggle setup failed for ${fieldName}:`, { field: !!field, toggle: !!toggle })
      return
    }

    console.log(`Setting up toggle for ${fieldName}`) // Debug

    const showIcon = toggle.querySelector(".show-icon")
    const hideIcon = toggle.querySelector(".hide-icon")

    if (!showIcon || !hideIcon) {
      console.warn(`Icons not found for ${fieldName}:`, { showIcon: !!showIcon, hideIcon: !!hideIcon })
      return
    }

    // Click event
    toggle.addEventListener("click", (e) => {
      e.preventDefault()
      console.log(`Toggle clicked for ${fieldName}`) // Debug
      this.togglePasswordVisibility(field, showIcon, hideIcon, toggle, fieldName)
    })

    // Keyboard support
    toggle.addEventListener("keydown", (e) => {
      if (e.key === "Enter" || e.key === " ") {
        e.preventDefault()
        console.log(`Toggle keyboard activated for ${fieldName}`) // Debug
        this.togglePasswordVisibility(field, showIcon, hideIcon, toggle, fieldName)
      }
    })
  }

  /**
   * Toggle password field visibility
   */
  togglePasswordVisibility(field, showIcon, hideIcon, toggle, fieldName) {
    console.log(`Toggling visibility for ${fieldName}, current type:`, field.type) // Debug

    const isPassword = field.type === "password"

    if (isPassword) {
      // Show password
      field.type = "text"
      showIcon.classList.add("hidden")
      hideIcon.classList.remove("hidden")
      toggle.setAttribute("aria-label", "Ocultar contraseña")
      this.announceToScreenReader("Contraseña visible")
      console.log(`${fieldName} password now visible`) // Debug
    } else {
      // Hide password
      field.type = "password"
      showIcon.classList.remove("hidden")
      hideIcon.classList.add("hidden")
      toggle.setAttribute("aria-label", "Mostrar contraseña")
      this.announceToScreenReader("Contraseña oculta")
      console.log(`${fieldName} password now hidden`) // Debug
    }

    // Maintain focus on password field
    field.focus()

    // Move cursor to end
    const length = field.value.length
    field.setSelectionRange(length, length)
  }

  /**
   * Setup password strength meter
   */
  setupPasswordStrength() {
    if (!this.passwordField || !this.strengthMeter || !this.strengthText) return

    this.passwordField.addEventListener("input", () => {
      this.updatePasswordStrength()
    })
  }

  /**
   * Update password strength meter
   */
  updatePasswordStrength() {
    if (!this.passwordField || !this.strengthMeter || !this.strengthText) return

    const password = this.passwordField.value
    const strength = this.calculatePasswordStrength(password)
    const strengthClass = this.getStrengthClass(strength)
    const strengthText = this.getStrengthText(strength)

    // Remove all strength classes
    const strengthContainer = this.strengthMeter.parentElement.parentElement
    strengthContainer.classList.remove("strength-weak", "strength-medium", "strength-good", "strength-strong")

    // Add appropriate strength class
    if (password) {
      strengthContainer.classList.add(strengthClass)
    }

    // Update text
    this.strengthText.textContent = password ? strengthText : "Seguridad de la contraseña"
  }

  /**
   * Calculate password strength score (0-100)
   */
  calculatePasswordStrength(password) {
    if (!password) return 0

    let score = 0

    // Length
    score += Math.min(password.length * 4, 25)

    // Complexity
    if (/[A-Z]/.test(password)) score += 10 // Uppercase
    if (/[a-z]/.test(password)) score += 10 // Lowercase
    if (/[0-9]/.test(password)) score += 10 // Numbers
    if (/[^A-Za-z0-9]/.test(password)) score += 15 // Special chars

    // Variety
    const uniqueChars = new Set(password).size
    score += Math.min(uniqueChars * 2, 15)

    // Patterns
    if (/(.)\1\1/.test(password)) score -= 10 // Repeated characters
    if (/^[A-Za-z]+$/.test(password)) score -= 5 // Only letters
    if (/^[0-9]+$/.test(password)) score -= 15 // Only numbers

    // Common patterns
    const commonPatterns = ["123", "abc", "qwerty", "password", "admin"]
    for (const pattern of commonPatterns) {
      if (password.toLowerCase().includes(pattern)) {
        score -= 10
        break
      }
    }

    return Math.max(0, Math.min(score, 100))
  }

  /**
   * Get strength class based on score
   */
  getStrengthClass(score) {
    if (score >= 80) return "strength-strong"
    if (score >= 60) return "strength-good"
    if (score >= 30) return "strength-medium"
    return "strength-weak"
  }

  /**
   * Get strength text based on score
   */
  getStrengthText(score) {
    if (score >= 80) return "Contraseña fuerte"
    if (score >= 60) return "Contraseña buena"
    if (score >= 30) return "Contraseña media"
    return "Contraseña débil"
  }

  /**
   * Step navigation methods
   */
  goToNextStep() {
    if (this.currentStepIndex === 0 && !this.validateStep1()) {
      return
    }
    if (this.currentStepIndex === 1 && !this.validateStep2()) {
      return
    }

    if (this.currentStepIndex < this.steps.length - 1) {
      this.currentStepIndex++
      this.showCurrentStep()
      this.updateSummary()
    }
  }

  goToPrevStep() {
    if (this.currentStepIndex > 0) {
      this.currentStepIndex--
      this.showCurrentStep()
    }
  }

  showCurrentStep() {
    // Hide all steps
    this.steps.forEach((step, index) => {
      if (index === this.currentStepIndex) {
        step.classList.remove("hidden")
      } else {
        step.classList.add("hidden")
      }
    })

    // Update step indicators
    this.stepIndicators.forEach((indicator, index) => {
      indicator.classList.remove("active", "completed")

      if (index < this.currentStepIndex) {
        indicator.classList.add("completed")
        // Add checkmark for completed steps
        const circle = indicator.querySelector(".step-circle")
        if (circle && !circle.querySelector("svg")) {
          circle.innerHTML = `
            <svg fill="none" viewBox="0 0 24 24" stroke-width="1.5" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" d="M4.5 12.75l6 6 9-13.5" />
            </svg>
          `
        }
      } else if (index === this.currentStepIndex) {
        indicator.classList.add("active")
        // Restore number for active step
        const circle = indicator.querySelector(".step-circle")
        if (circle) {
          circle.textContent = index + 1
        }
      } else {
        // Restore number for future steps
        const circle = indicator.querySelector(".step-circle")
        if (circle) {
          circle.textContent = index + 1
        }
      }
    })

    // Focus first field in current step
    this.focusFirstFieldInCurrentStep()
  }

  focusFirstFieldInCurrentStep() {
    const currentStep = this.steps[this.currentStepIndex]
    if (!currentStep) return

    const firstInput = currentStep.querySelector("input, select")
    if (firstInput) {
      firstInput.focus()
    }
  }

  /**
   * Validation methods for each step
   */
  validateStep1() {
    let isValid = true

    if (!this.validateUsername()) isValid = false
    if (!this.validateEmail()) isValid = false
    if (!this.validatePassword()) isValid = false
    if (!this.validateConfirmPassword()) isValid = false

    return isValid
  }

  validateStep2() {
    let isValid = true

    if (!this.validateStoreName()) isValid = false
    if (!this.validateStoreAddress()) isValid = false
    if (!this.validateStoreRut()) isValid = false
    if (!this.validateStoreType()) isValid = false

    return isValid
  }

  validateStep3() {
    return this.validateTerms()
  }

  /**
   * Individual field validation methods
   */
  validateUsername() {
    if (!this.usernameField) return true

    const username = this.usernameField.value.trim()

    if (!username) {
      this.showError("username", "El nombre de usuario es requerido")
      return false
    }

    if (username.length < this.config.minUsernameLength) {
      this.showError("username", `El nombre de usuario debe tener al menos ${this.config.minUsernameLength} caracteres`)
      return false
    }

    if (username.length > this.config.maxUsernameLength) {
      this.showError(
        "username",
        `El nombre de usuario no puede tener más de ${this.config.maxUsernameLength} caracteres`,
      )
      return false
    }

    if (!/^[a-zA-Z0-9._-]+$/.test(username)) {
      this.showError(
        "username",
        "El nombre de usuario solo puede contener letras, números, puntos, guiones y guiones bajos",
      )
      return false
    }

    this.clearError("username")
    return true
  }

  validateEmail() {
    if (!this.emailField) return true

    const email = this.emailField.value.trim()

    if (!email) {
      this.showError("email", "El correo electrónico es requerido")
      return false
    }

    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
    if (!emailRegex.test(email)) {
      this.showError("email", "Por favor, ingresa un correo electrónico válido")
      return false
    }

    this.clearError("email")
    return true
  }

  validatePassword() {
    if (!this.passwordField) return true

    const password = this.passwordField.value

    if (!password) {
      this.showError("password", "La contraseña es requerida")
      return false
    }

    if (password.length < this.config.minPasswordLength) {
      this.showError("password", `La contraseña debe tener al menos ${this.config.minPasswordLength} caracteres`)
      return false
    }

    // Check for at least one uppercase letter
    if (!/[A-Z]/.test(password)) {
      this.showError("password", "La contraseña debe contener al menos una letra mayúscula")
      return false
    }

    // Check for at least one lowercase letter
    if (!/[a-z]/.test(password)) {
      this.showError("password", "La contraseña debe contener al menos una letra minúscula")
      return false
    }

    // Check for at least one number
    if (!/[0-9]/.test(password)) {
      this.showError("password", "La contraseña debe contener al menos un número")
      return false
    }

    this.clearError("password")
    return true
  }

  validateConfirmPassword() {
    if (!this.confirmPasswordField || !this.passwordField) return true

    const password = this.passwordField.value
    const confirmPassword = this.confirmPasswordField.value

    if (!confirmPassword) {
      this.showError("confirmPassword", "Debes confirmar tu contraseña")
      return false
    }

    if (password !== confirmPassword) {
      this.showError("confirmPassword", "Las contraseñas no coinciden")
      return false
    }

    this.clearError("confirmPassword")
    return true
  }

  validateStoreName() {
    if (!this.storeNameField) return true

    const storeName = this.storeNameField.value.trim()

    if (!storeName) {
      this.showError("storeName", "El nombre de la tienda es requerido")
      return false
    }

    if (storeName.length < 2) {
      this.showError("storeName", "El nombre de la tienda debe tener al menos 2 caracteres")
      return false
    }

    this.clearError("storeName")
    return true
  }

  validateStoreAddress() {
    if (!this.storeAddressField) return true

    const storeAddress = this.storeAddressField.value.trim()

    if (!storeAddress) {
      this.showError("storeAddress", "La dirección de la tienda es requerida")
      return false
    }

    if (storeAddress.length < 5) {
      this.showError("storeAddress", "La dirección debe tener al menos 5 caracteres")
      return false
    }

    this.clearError("storeAddress")
    return true
  }

  /**
   * Validate Colombian NIT
   */
  validateStoreRut() {
    if (!this.storeRutField) return true

    const nit = this.storeRutField.value.trim()

    if (!nit) {
      this.showError("storeRut", "El NIT de la tienda es requerido")
      return false
    }

    // Clean NIT (remove dots, hyphens, and spaces)
    const cleanNit = nit.replace(/[.\-\s]/g, "")

    // Validate NIT format (Colombian NIT)
    if (!/^\d{8,15}$/.test(cleanNit)) {
      this.showError("storeRut", "El NIT debe tener entre 8 y 15 dígitos")
      return false
    }

    // Validate NIT check digit for NITs with 9 or more digits
    if (cleanNit.length >= 9 && !this.validateNitCheckDigit(cleanNit)) {
      this.showError("storeRut", "El NIT ingresado no es válido")
      return false
    }

    this.clearError("storeRut")
    return true
  }

  /**
   * Validate Colombian NIT check digit
   */
  validateNitCheckDigit(nit) {
    try {
      const nitWithoutDv = nit.substring(0, nit.length - 1)
      const checkDigit = Number.parseInt(nit.substring(nit.length - 1))

      const factors = [71, 67, 59, 53, 47, 43, 41, 37, 29, 23, 19, 17, 13, 7, 3]
      let sum = 0

      // Calculate weighted sum
      for (let i = 0; i < nitWithoutDv.length; i++) {
        const digit = Number.parseInt(nitWithoutDv.charAt(nitWithoutDv.length - 1 - i))
        sum += digit * factors[i]
      }

      const remainder = sum % 11
      const calculatedDv = remainder < 2 ? remainder : 11 - remainder

      return calculatedDv === checkDigit
    } catch (error) {
      return false
    }
  }

  validateStoreType() {
    if (!this.storeTypeField) return true

    const storeType = this.storeTypeField.value

    if (!storeType) {
      this.showError("storeType", "Debes seleccionar el tipo de tienda")
      return false
    }

    this.clearError("storeType")
    return true
  }

  validateTerms() {
    if (!this.termsCheckbox) return true

    if (!this.termsCheckbox.checked) {
      this.showError("termsAccepted", "Debes aceptar los términos y condiciones")
      return false
    }

    this.clearError("termsAccepted")
    return true
  }

  /**
   * Validate Chilean RUT check digit
   */
  validateRutCheckDigit(rut) {
    const rutDigits = rut.slice(0, -1)
    const checkDigit = rut.slice(-1).toLowerCase()

    let sum = 0
    let multiplier = 2

    for (let i = rutDigits.length - 1; i >= 0; i--) {
      sum += Number.parseInt(rutDigits[i]) * multiplier
      multiplier = multiplier === 7 ? 2 : multiplier + 1
    }

    const remainder = sum % 11
    const calculatedCheckDigit = remainder === 0 ? "0" : remainder === 1 ? "k" : (11 - remainder).toString()

    return checkDigit === calculatedCheckDigit
  }

  /**
   * Update summary section
   */
  updateSummary() {
    if (this.summaryUsername) {
      this.summaryUsername.textContent = this.usernameField?.value || "-"
    }
    if (this.summaryEmail) {
      this.summaryEmail.textContent = this.emailField?.value || "-"
    }
    if (this.summaryStoreName) {
      this.summaryStoreName.textContent = this.storeNameField?.value || "-"
    }
    if (this.summaryStoreType) {
      const storeType = this.storeTypeField?.value
      const storeTypeText = storeType === "tienda" ? "Tienda" : storeType === "papeleria" ? "Papelería" : "-"
      this.summaryStoreType.textContent = storeTypeText
    }
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

    // Validate all steps
    if (!this.validateStep1() || !this.validateStep2() || !this.validateStep3()) {
      e.preventDefault()
      this.announceToScreenReader("Por favor, corrige los errores en el formulario")
      return
    }

    // Show loading state
    this.showLoading()
    this.isSubmitting = true

    // Let the form submit naturally to Spring Boot
    // The loading state will be cleared when the page reloads or redirects
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
    if (!this.registerBtn) return

    const spinner = this.registerBtn.querySelector(".btn-spinner")
    const text = this.registerBtn.querySelector(".btn-text")

    if (spinner && text) {
      spinner.classList.remove("hidden")
      text.textContent = "Creando cuenta..."
    }

    this.registerBtn.disabled = true
    this.registerBtn.setAttribute("aria-busy", "true")

    this.announceToScreenReader("Creando cuenta, por favor espera...")
  }

  /**
   * Reset loading state
   */
  resetLoadingState() {
    if (!this.registerBtn) return

    const spinner = this.registerBtn.querySelector(".btn-spinner")
    const text = this.registerBtn.querySelector(".btn-text")

    if (spinner && text) {
      spinner.classList.add("hidden")
      text.textContent = "Completar registro"
    }

    this.registerBtn.disabled = false
    this.registerBtn.setAttribute("aria-busy", "false")
    this.isSubmitting = false
  }

  /**
   * Setup accessibility enhancements
   */
  setupAccessibility() {
    // Add ARIA live region if it doesn't exist
    if (!document.getElementById("register-live-region")) {
      const liveRegion = document.createElement("div")
      liveRegion.id = "register-live-region"
      liveRegion.setAttribute("aria-live", "polite")
      liveRegion.setAttribute("aria-atomic", "true")
      liveRegion.className = "sr-only"
      document.body.appendChild(liveRegion)
    }

    // Improve form accessibility
    this.form.setAttribute("novalidate", "true")
    this.form.setAttribute("aria-label", "Formulario de registro")
  }

  /**
   * Announce message to screen readers
   */
  announceToScreenReader(message) {
    const liveRegion = document.getElementById("register-live-region")
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
   * Clean up resources
   */
  destroy() {
    // Clear any pending timeouts
    Object.values(this.validationTimeouts).forEach((timeout) => {
      clearTimeout(timeout)
    })
  }
}

// Initialize when DOM is loaded
document.addEventListener("DOMContentLoaded", () => {
  // Only initialize if we're on the register page
  if (document.getElementById("registerForm")) {
    window.registerPage = new RegisterPage()
  }
})

// Handle page visibility changes for better UX
document.addEventListener("visibilitychange", () => {
  if (!document.hidden && window.registerPage) {
    // Page became visible, reset any loading states
    window.registerPage.resetLoadingState()
  }
})

// Export for potential use in other scripts
if (typeof module !== "undefined" && module.exports) {
  module.exports = RegisterPage
}
