/**
 * ShopMaster - Punto de Venta JavaScript
 * Handles point of sale functionality, product management, and debt registration
 */

class PuntoVenta {
  constructor() {
    // DOM elements
    this.menuToggle = document.getElementById("menu-toggle")
    this.userMenu = document.getElementById("user-menu")
    this.modalDeuda = document.getElementById("modalDeuda")
    this.nombreProductoInput = document.getElementById("nombreProducto")
    this.cantidadInput = document.getElementById("cantidad")
    this.precioDisplay = document.getElementById("precioDisplay")

    // State
    this.menuOpen = false

    this.init()
  }

  /**
   * Initialize the punto de venta functionality
   */
  init() {
    this.setupEventListeners()
    this.setupAccessibility()
    this.setupKeyboardShortcuts()
    this.focusFirstField()
  }

  /**
   * Setup all event listeners
   */
  setupEventListeners() {
    // Menu toggle
    if (this.menuToggle && this.userMenu) {
      this.menuToggle.addEventListener("click", (e) => {
        e.stopPropagation()
        this.toggleMenu()
      })

      // Close menu when clicking outside
      document.addEventListener("click", (e) => {
        if (this.menuOpen && !this.userMenu.contains(e.target) && !this.menuToggle.contains(e.target)) {
          this.closeMenu()
        }
      })
    }

    // Product input events
    if (this.nombreProductoInput) {
      this.nombreProductoInput.addEventListener("input", () => {
        this.actualizarPrecio()
      })

      this.nombreProductoInput.addEventListener("change", () => {
        this.actualizarPrecio()
      })
    }

    // Quantity input events
    if (this.cantidadInput) {
      this.cantidadInput.addEventListener("input", () => {
        this.validateQuantity()
      })

      // Auto-focus to add button when quantity is entered
      this.cantidadInput.addEventListener("keypress", (e) => {
        if (e.key === "Enter") {
          e.preventDefault()
          const addButton = document.querySelector(".btn-add")
          if (addButton && !addButton.disabled) {
            addButton.click()
          }
        }
      })
    }

    // Modal events
    this.setupModalEvents()

    // Form validation
    this.setupFormValidation()

    // Keyboard navigation for table
    this.setupTableNavigation()
  }

  /**
   * Setup modal event listeners
   */
  setupModalEvents() {
    if (this.modalDeuda) {
      // Close modal when clicking overlay
      this.modalDeuda.addEventListener("click", (e) => {
        if (e.target === this.modalDeuda) {
          this.cerrarModalDeuda()
        }
      })

      // Close modal on escape key
      document.addEventListener("keydown", (e) => {
        if (e.key === "Escape" && !this.modalDeuda.classList.contains("hidden")) {
          this.cerrarModalDeuda()
        }
      })
    }
  }

  /**
   * Setup form validation
   */
  setupFormValidation() {
    const forms = document.querySelectorAll("form")
    forms.forEach((form) => {
      form.addEventListener("submit", (e) => {
        if (!this.validateForm(form)) {
          e.preventDefault()
        }
      })
    })
  }

  /**
   * Setup table keyboard navigation
   */
 setupTableNavigation() {
    const deleteButtons = document.querySelectorAll(".btn-danger")
    deleteButtons.forEach((button) => {
      button.addEventListener("keydown", (e) => {
        if (e.key === "Enter" || e.key === " ") {
          e.preventDefault()
          if (confirm("¿Estás seguro de que deseas eliminar este producto?")) {
            button.click()
          }
        }
      })
    })
  }

  /**
   * Setup keyboard shortcuts
   */
  setupKeyboardShortcuts() {
    document.addEventListener("keydown", (e) => {
      // Alt + A: Focus on product name input
      if (e.altKey && e.key === "a") {
        e.preventDefault()
        if (this.nombreProductoInput) {
          this.nombreProductoInput.focus()
        }
      }

      // Alt + Q: Focus on quantity input
      if (e.altKey && e.key === "q") {
        e.preventDefault()
        if (this.cantidadInput) {
          this.cantidadInput.focus()
        }
      }

      // Alt + F: Finalize sale
      if (e.altKey && e.key === "f") {
        e.preventDefault()
        const finalizeButton = document.querySelector('button[type="submit"]')
        if (finalizeButton && !finalizeButton.disabled) {
          finalizeButton.click()
        }
      }

      // Alt + D: Open debt modal
      if (e.altKey && e.key === "d") {
        e.preventDefault()
        const debtButton = document.querySelector('button[onclick="abrirModalDeuda()"]')
        if (debtButton && !debtButton.disabled) {
          this.abrirModalDeuda()
        }
      }
    })
  }

  /**
   * Toggle user menu
   */
  toggleMenu() {
    if (this.menuOpen) {
      this.closeMenu()
    } else {
      this.openMenu()
    }
  }

  /**
   * Open user menu
   */
  openMenu() {
    this.menuOpen = true
    this.menuToggle.setAttribute("aria-expanded", "true")
    this.userMenu.classList.remove("hidden")

    // Focus first menu item
    const firstMenuItem = this.userMenu.querySelector(".menu-item")
    if (firstMenuItem) {
      firstMenuItem.focus()
    }
  }

  /**
   * Close user menu
   */
  closeMenu() {
    this.menuOpen = false
    this.menuToggle.setAttribute("aria-expanded", "false")
    this.userMenu.classList.add("hidden")
  }

  /**
   * Update price display when product is selected
   */
  actualizarPrecio() {
    if (!this.nombreProductoInput || !this.precioDisplay) return

    const selected = this.nombreProductoInput.value.trim()
    const options = document.querySelectorAll("#productos option")
    let precio = null

    options.forEach((option) => {
      if (option.value === selected) {
        precio = option.dataset.precio
      }
    })

    if (precio) {
      const precioFormateado = new Intl.NumberFormat("es-CO", {
        style: "currency",
        currency: "COP",
        minimumFractionDigits: 0,
      }).format(precio)
      this.precioDisplay.textContent = `Precio: ${precioFormateado}`
      this.precioDisplay.classList.remove("text-gray-500")
      this.precioDisplay.classList.add("text-green-600")
    } else if (selected) {
      this.precioDisplay.textContent = "Producto no encontrado"
      this.precioDisplay.classList.remove("text-green-600")
      this.precioDisplay.classList.add("text-red-500")
    } else {
      this.precioDisplay.textContent = "Precio: Selecciona un producto"
      this.precioDisplay.classList.remove("text-green-600", "text-red-500")
      this.precioDisplay.classList.add("text-gray-500")
    }
  }

  /**
   * Validate quantity input
   */
  validateQuantity() {
    if (!this.cantidadInput) return

    const cantidad = parseInt(this.cantidadInput.value)
    if (cantidad < 1) {
      this.cantidadInput.value = 1
    }
  }

  /**
   * Validate form before submission
   */
  validateForm(form) {
    const requiredFields = form.querySelectorAll("[required]")
    let isValid = true

    requiredFields.forEach((field) => {
      if (!field.value.trim()) {
        isValid = false
        field.classList.add("border-red-500")
        field.focus()
      } else {
        field.classList.remove("border-red-500")
      }
    })

    return isValid
  }

  /**
   * Open debt registration modal
   */
  abrirModalDeuda() {
    if (!this.modalDeuda) return

    this.modalDeuda.classList.remove("hidden")
    document.body.style.overflow = "hidden"

    // Focus first input in modal
    const firstInput = this.modalDeuda.querySelector("input")
    if (firstInput) {
      setTimeout(() => {
        firstInput.focus()
      }, 100)
    }

    this.announceToScreenReader("Modal de registro de deuda abierto")
  }

  /**
   * Close debt registration modal
   */
  cerrarModalDeuda() {
    if (!this.modalDeuda) return

    this.modalDeuda.classList.add("hidden")
    document.body.style.overflow = ""

    // Return focus to debt button
    const debtButton = document.querySelector('button[onclick="abrirModalDeuda()"]')
    if (debtButton) {
      debtButton.focus()
    }

    this.announceToScreenReader("Modal de registro de deuda cerrado")
  }

  /**
   * Focus first field on page load
   */
  focusFirstField() {
    if (this.nombreProductoInput) {
      this.nombreProductoInput.focus()
    }
  }

  /**
   * Setup accessibility enhancements
   */
  setupAccessibility() {
    // Add ARIA live region if it doesn't exist
    if (!document.getElementById("pos-live-region")) {
      const liveRegion = document.createElement("div")
      liveRegion.id = "pos-live-region"
      liveRegion.setAttribute("aria-live", "polite")
      liveRegion.setAttribute("aria-atomic", "true")
      liveRegion.className = "sr-only"
      document.body.appendChild(liveRegion)
    }

    // Improve table accessibility
    const table = document.querySelector(".products-table")
    if (table) {
      table.setAttribute("role", "table")
      table.setAttribute("aria-label", "Productos seleccionados para la venta")
    }

    // Add keyboard shortcuts help
    this.addKeyboardShortcutsHelp()
  }

  /**
   * Add keyboard shortcuts help
   */
  addKeyboardShortcutsHelp() {
    const helpText = document.createElement("div")
    helpText.className = "sr-only"
    helpText.innerHTML = `
      <p>Atajos de teclado disponibles:</p>
      <ul>
        <li>Alt + A: Enfocar campo de producto</li>
        <li>Alt + Q: Enfocar campo de cantidad</li>
        <li>Alt + F: Finalizar venta</li>
        <li>Alt + D: Abrir modal de deuda</li>
        <li>Escape: Cerrar modal</li>
      </ul>
    `
    document.body.appendChild(helpText)
  }

  /**
   * Announce message to screen readers
   */
  announceToScreenReader(message) {
    const liveRegion = document.getElementById("pos-live-region")
    if (liveRegion) {
      liveRegion.textContent = message
      setTimeout(() => {
        liveRegion.textContent = ""
      }, 1000)
    }
  }

  /**
   * Format currency for display
   */
  formatCurrency(amount) {
    return new Intl.NumberFormat("es-CO", {
      style: "currency",
      currency: "COP",
      minimumFractionDigits: 0,
    }).format(amount)
  }

  /**
   * Clean up resources
   */
  destroy() {
    // Remove event listeners and clean up
    document.body.style.overflow = ""

    // Clear any timeouts
    if (this.updateTimeout) {
      clearTimeout(this.updateTimeout)
    }
  }
}

// Global functions for backward compatibility
function actualizarPrecio() {
  if (window.puntoVenta) {
    window.puntoVenta.actualizarPrecio()
  }
}

function abrirModalDeuda() {
  if (window.puntoVenta) {
    window.puntoVenta.abrirModalDeuda()
  }
}

function cerrarModalDeuda() {
  if (window.puntoVenta) {
    window.puntoVenta.cerrarModalDeuda()
  }
}

// Initialize when DOM is loaded
document.addEventListener("DOMContentLoaded", () => {
  window.puntoVenta = new PuntoVenta()
})

// Handle page visibility changes
document.addEventListener("visibilitychange", () => {
  if (document.hidden) {
    // Page is hidden, pause any operations
    console.log("POS page hidden")
  } else {
    // Page is visible, resume operations
    console.log("POS page visible")
    if (window.puntoVenta && window.puntoVenta.nombreProductoInput) {
      window.puntoVenta.nombreProductoInput.focus()
    }
  }
})

// Handle before unload
window.addEventListener("beforeunload", () => {
  if (window.puntoVenta) {
    window.puntoVenta.destroy()
  }
})

// Export for potential use in other scripts
if (typeof module !== "undefined" && module.exports) {
  module.exports = PuntoVenta
}
