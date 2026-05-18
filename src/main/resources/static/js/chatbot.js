/**
 * chatbot.js — ShopMaster AI Chatbot
 * =====================================
 * Chatbot flotante con arquitectura LLM + MCP + Groq + Alertas Inteligentes.
 *
 * Uso: incluir en cualquier template Thymeleaf después de obtener tiendaId:
 *   <script src="/js/chatbot.js" data-tienda-id="${tiendaId}"></script>
 *
 * O inicializar manualmente:
 *   ShopChatBot.init({ tiendaId: 'abc123' });
 */

(function () {
  'use strict';

  // ─────────────────────────────────────────────
  //  Configuración
  // ─────────────────────────────────────────────
  const CONFIG = {
    POLL_INTERVAL_MS: 60_000,       // Revisa alertas cada 60 seg
    MAX_MESSAGES: 50,               // Máximo de mensajes en el historial
    QUICK_QUESTIONS: [
      '¿Tengo alertas?',
      '¿Qué productos tienen bajo stock?',
      '¿Cuánto vendí hoy?',
      '¿Cuáles son las deudas pendientes?',
    ],
  };

  // ─────────────────────────────────────────────
  //  Estado global del chatbot
  // ─────────────────────────────────────────────
  let state = {
    open: false,
    alertPanelOpen: false,
    tiendaId: null,
    alertCount: 0,
    alertas: [],
    dismissed: new Set(), // índices de alertas descartadas por el usuario
    loading: false,
  };

  // ─────────────────────────────────────────────
  //  Estilos inyectados dinámicamente
  // ─────────────────────────────────────────────
  function injectStyles() {
    if (document.getElementById('sm-chatbot-styles')) return;
    const style = document.createElement('style');
    style.id = 'sm-chatbot-styles';
    style.textContent = `
      /* ── Contenedor raíz ── */
      #sm-chatbot-root {
        position: fixed;
        bottom: 1.5rem;
        right: 1.5rem;
        z-index: 9999;
        font-family: 'Inter', system-ui, sans-serif;
        font-size: 14px;
      }

      /* ── Botones flotantes ── */
      .sm-fab {
        width: 52px; height: 52px;
        border-radius: 50%;
        border: none;
        cursor: pointer;
        display: flex; align-items: center; justify-content: center;
        box-shadow: 0 4px 16px rgba(0,0,0,.18);
        transition: transform .15s, box-shadow .15s;
        position: relative;
      }
      .sm-fab:hover { transform: scale(1.07); box-shadow: 0 6px 20px rgba(0,0,0,.22); }
      .sm-fab:active { transform: scale(.97); }

      #sm-btn-chat {
        background: #0e7490;  /* cyan-700 */
        color: #fff;
        font-size: 22px;
        margin-bottom: 10px;
      }
      #sm-btn-bell {
        background: #fff;
        color: #374151;
        font-size: 20px;
      }

      /* ── Badge de alertas ── */
      #sm-badge {
        position: absolute;
        top: -4px; right: -4px;
        width: 20px; height: 20px;
        border-radius: 50%;
        background: #dc2626;
        color: #fff;
        font-size: 11px;
        font-weight: 600;
        display: flex; align-items: center; justify-content: center;
        border: 2px solid #fff;
        display: none;
      }

      /* ── Panel de alertas ── */
      #sm-alert-panel {
        position: absolute;
        bottom: 130px; right: 0;
        width: 300px;
        background: #fff;
        border-radius: 14px;
        box-shadow: 0 8px 30px rgba(0,0,0,.15);
        border: 1px solid #e5e7eb;
        overflow: hidden;
        display: none;
        animation: smSlideUp .2s ease;
      }
      #sm-alert-panel.open { display: block; }
      .sm-alert-header {
        padding: 12px 16px;
        background: #fef3c7;
        border-bottom: 1px solid #fde68a;
        font-weight: 600;
        font-size: 13px;
        color: #92400e;
        display: flex; align-items: center; gap: 6px;
      }
      .sm-alert-list { max-height: 240px; overflow-y: auto; }
      .sm-alert-item {
        padding: 9px 12px 9px 16px;
        border-bottom: 1px solid #f3f4f6;
        font-size: 13px;
        color: #374151;
        line-height: 1.45;
        display: flex;
        align-items: center;
        gap: 8px;
      }
      .sm-alert-item:last-child { border-bottom: none; }
      .sm-alert-item.danger  { background: #fef2f2; color: #991b1b; }
      .sm-alert-item.warning { background: #fffbeb; color: #92400e; }
      .sm-alert-text { flex: 1; }
      .sm-alert-dismiss {
        flex-shrink: 0;
        width: 20px; height: 20px;
        border-radius: 50%;
        border: none;
        background: rgba(0,0,0,.08);
        color: inherit;
        font-size: 12px;
        cursor: pointer;
        display: flex; align-items: center; justify-content: center;
        opacity: 0.6;
        transition: opacity .15s, background .15s;
        line-height: 1;
      }
      .sm-alert-dismiss:hover { opacity: 1; background: rgba(0,0,0,.15); }
      .sm-alert-empty {
        padding: 20px 16px;
        text-align: center;
        color: #6b7280;
        font-size: 13px;
      }

      /* ── Ventana del chat ── */
      #sm-chat-window {
        position: absolute;
        bottom: 130px; right: 0;
        width: 340px;
        background: #fff;
        border-radius: 16px;
        box-shadow: 0 8px 32px rgba(0,0,0,.18);
        border: 1px solid #e5e7eb;
        overflow: hidden;
        display: none;
        flex-direction: column;
        animation: smSlideUp .2s ease;
      }
      #sm-chat-window.open { display: flex; }

      /* ── Header del chat ── */
      .sm-chat-header {
        padding: 14px 16px;
        background: linear-gradient(135deg, #0e7490, #0891b2);
        color: #fff;
        display: flex; align-items: center; gap: 10px;
      }
      .sm-chat-avatar {
        width: 36px; height: 36px;
        border-radius: 50%;
        background: rgba(255,255,255,.2);
        display: flex; align-items: center; justify-content: center;
        font-size: 18px;
        flex-shrink: 0;
      }
      .sm-chat-title { font-weight: 600; font-size: 14px; }
      .sm-chat-subtitle { font-size: 11px; opacity: .8; }
      .sm-chat-close {
        margin-left: auto;
        background: none; border: none;
        color: rgba(255,255,255,.8);
        cursor: pointer; font-size: 18px;
        line-height: 1;
        padding: 2px 6px;
        border-radius: 6px;
      }
      .sm-chat-close:hover { background: rgba(255,255,255,.15); }

      /* ── Mensajes ── */
      #sm-messages {
        flex: 1;
        overflow-y: auto;
        padding: 12px;
        display: flex;
        flex-direction: column;
        gap: 8px;
        max-height: 320px;
        min-height: 200px;
        background: #f9fafb;
      }
      .sm-msg {
        max-width: 85%;
        padding: 8px 12px;
        border-radius: 12px;
        line-height: 1.5;
        font-size: 13px;
        word-break: break-word;
        white-space: pre-wrap;
      }
      .sm-msg.bot {
        background: #fff;
        color: #111827;
        border: 1px solid #e5e7eb;
        align-self: flex-start;
        border-radius: 4px 12px 12px 12px;
      }
      .sm-msg.user {
        background: #0e7490;
        color: #fff;
        align-self: flex-end;
        border-radius: 12px 4px 12px 12px;
      }
      .sm-msg.typing {
        background: #fff;
        border: 1px solid #e5e7eb;
        align-self: flex-start;
        color: #9ca3af;
        font-style: italic;
        font-size: 13px;
        border-radius: 4px 12px 12px 12px;
      }

      /* ── Preguntas rápidas ── */
      #sm-quick {
        padding: 8px 12px;
        display: flex;
        gap: 6px;
        overflow-x: auto;
        border-top: 1px solid #f3f4f6;
        background: #fff;
        scrollbar-width: none;
      }
      #sm-quick::-webkit-scrollbar { display: none; }
      .sm-quick-btn {
        white-space: nowrap;
        padding: 5px 10px;
        border-radius: 20px;
        border: 1px solid #d1fae5;
        background: #ecfdf5;
        color: #065f46;
        font-size: 11.5px;
        cursor: pointer;
        transition: background .12s;
        flex-shrink: 0;
      }
      .sm-quick-btn:hover { background: #d1fae5; }

      /* ── Input ── */
      .sm-chat-input-area {
        display: flex;
        align-items: center;
        gap: 8px;
        padding: 10px 12px;
        border-top: 1px solid #f3f4f6;
        background: #fff;
      }
      #sm-input {
        flex: 1;
        border: 1px solid #d1d5db;
        border-radius: 20px;
        padding: 7px 14px;
        font-size: 13px;
        outline: none;
        resize: none;
        transition: border-color .15s;
        font-family: inherit;
      }
      #sm-input:focus { border-color: #0e7490; }
      #sm-send-btn {
        width: 36px; height: 36px;
        border-radius: 50%;
        background: #0e7490;
        border: none;
        cursor: pointer;
        color: #fff;
        display: flex; align-items: center; justify-content: center;
        font-size: 16px;
        transition: background .12s;
        flex-shrink: 0;
      }
      #sm-send-btn:hover { background: #0891b2; }
      #sm-send-btn:disabled { background: #9ca3af; cursor: default; }

      /* ── Animación ── */
      @keyframes smSlideUp {
        from { opacity: 0; transform: translateY(12px); }
        to   { opacity: 1; transform: translateY(0); }
      }

      /* ── Dot typing animation ── */
      .sm-dot-typing {
        display: inline-flex; gap: 4px; align-items: center;
      }
      .sm-dot-typing span {
        width: 6px; height: 6px;
        border-radius: 50%;
        background: #9ca3af;
        animation: smDot 1.2s infinite;
      }
      .sm-dot-typing span:nth-child(2) { animation-delay: .2s; }
      .sm-dot-typing span:nth-child(3) { animation-delay: .4s; }
      @keyframes smDot {
        0%, 80%, 100% { transform: scale(.8); opacity: .5; }
        40%           { transform: scale(1.2); opacity: 1; }
      }
    `;
    document.head.appendChild(style);
  }

  // ─────────────────────────────────────────────
  //  HTML del chatbot
  // ─────────────────────────────────────────────
  function createHTML() {
    const root = document.createElement('div');
    root.id = 'sm-chatbot-root';
    root.setAttribute('role', 'complementary');
    root.setAttribute('aria-label', 'Asistente ShopMaster');
    root.innerHTML = `
      <!-- Panel de alertas -->
      <div id="sm-alert-panel" role="dialog" aria-label="Alertas activas">
        <div class="sm-alert-header">🔔 Alertas activas</div>
        <div class="sm-alert-list" id="sm-alert-list">
          <div class="sm-alert-empty">Cargando alertas…</div>
        </div>
      </div>

      <!-- Ventana de chat -->
      <div id="sm-chat-window" role="dialog" aria-label="Chat con asistente">
        <div class="sm-chat-header">
          <div class="sm-chat-avatar">🤖</div>
          <div>
            <div class="sm-chat-title">Asistente ShopMaster</div>
            <div class="sm-chat-subtitle">Con tecnología Groq · LLaMA 3</div>
          </div>
          <button class="sm-chat-close" id="sm-close-btn" aria-label="Cerrar chat">✕</button>
        </div>
        <div id="sm-messages" aria-live="polite" aria-label="Historial de mensajes"></div>
        <div id="sm-quick" aria-label="Preguntas rápidas"></div>
        <div class="sm-chat-input-area">
          <input id="sm-input" type="text" placeholder="Escribe tu pregunta…"
                 aria-label="Mensaje para el asistente" autocomplete="off" maxlength="300" />
          <button id="sm-send-btn" aria-label="Enviar mensaje">➤</button>
        </div>
      </div>

      <!-- FAB campanita -->
      <button class="sm-fab" id="sm-btn-bell" aria-label="Ver alertas">
        🔔
        <span id="sm-badge">0</span>
      </button>

      <!-- FAB chat -->
      <button class="sm-fab" id="sm-btn-chat" aria-label="Abrir asistente">
        💬
      </button>
    `;
    document.body.appendChild(root);
  }

  // ─────────────────────────────────────────────
  //  Lógica de mensajes
  // ─────────────────────────────────────────────
  function appendMessage(text, type) {
    const container = document.getElementById('sm-messages');
    const div = document.createElement('div');
    div.className = `sm-msg ${type}`;

    if (type === 'typing') {
      div.innerHTML = `<span class="sm-dot-typing"><span></span><span></span><span></span></span> Pensando…`;
    } else {
      div.textContent = text;
    }

    container.appendChild(div);

    // Limitar historial
    const msgs = container.querySelectorAll('.sm-msg');
    if (msgs.length > CONFIG.MAX_MESSAGES) msgs[0].remove();

    container.scrollTop = container.scrollHeight;
    return div;
  }

  async function sendMessage(texto) {
    if (!texto || state.loading) return;
    state.loading = true;

    appendMessage(texto, 'user');
    document.getElementById('sm-input').value = '';
    document.getElementById('sm-send-btn').disabled = true;

    const typingDiv = appendMessage('', 'typing');

    try {
      const res = await fetch('/api/chatbot/mensaje', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ mensaje: texto, tiendaId: state.tiendaId }),
      });

      typingDiv.remove();

      if (!res.ok) throw new Error('Error en el servidor: ' + res.status);

      const data = await res.json();
      appendMessage(data.respuesta || '⚠️ Sin respuesta del servidor.', 'bot');
    } catch (err) {
      typingDiv.remove();
      appendMessage('❌ No pude conectarme al servidor. Verifica tu conexión.', 'bot');
      console.error('[ShopMaster Chatbot]', err);
    } finally {
      state.loading = false;
      document.getElementById('sm-send-btn').disabled = false;
    }
  }

  // ─────────────────────────────────────────────
  //  Alertas — campanita 🔔
  // ─────────────────────────────────────────────
  async function fetchAlertas() {
    if (!state.tiendaId) return;
    try {
      const res = await fetch(`/api/chatbot/alertas?tiendaId=${state.tiendaId}`);
      if (!res.ok) return;
      const data = await res.json();

      // Guardar todas las alertas del servidor
      state.alertas = data.alertas || [];

      // Si el servidor trae alertas nuevas que no estaban antes, limpiar dismissed
      // (solo limpiar si el total del servidor creció respecto a lo que había)
      const visibles = state.alertas.filter((_, i) => !state.dismissed.has(i));
      state.alertCount = visibles.length;

      const badge = document.getElementById('sm-badge');
      if (state.alertCount > 0) {
        badge.style.display = 'flex';
        badge.textContent   = state.alertCount > 9 ? '9+' : state.alertCount;
      } else {
        badge.style.display = 'none';
      }

      renderAlertPanel();
    } catch (err) {
      console.warn('[ShopMaster Alertas]', err);
    }
  }

  function renderAlertPanel() {
    const list = document.getElementById('sm-alert-list');
    const visibles = state.alertas.filter((_, i) => !state.dismissed.has(i));

    if (visibles.length === 0) {
      list.innerHTML = '<div class="sm-alert-empty">✅ Sin alertas activas. ¡Todo bien!</div>';
      return;
    }

    list.innerHTML = '';
    state.alertas.forEach((a, i) => {
      if (state.dismissed.has(i)) return;

      const cls = a.startsWith('💸') ? 'danger' : 'warning';
      const item = document.createElement('div');
      item.className = `sm-alert-item ${cls}`;
      item.innerHTML = `
        <span class="sm-alert-text">${a}</span>
        <button class="sm-alert-dismiss" title="Descartar">✕</button>
      `;

      item.querySelector('.sm-alert-dismiss').addEventListener('click', (e) => {
        e.stopPropagation();
        dismissAlerta(i);
      });

      list.appendChild(item);
    });
  }

  function dismissAlerta(index) {
    state.dismissed.add(index);
    const visibles = state.alertas.filter((_, i) => !state.dismissed.has(i));
    state.alertCount = visibles.length;

    const badge = document.getElementById('sm-badge');
    if (state.alertCount > 0) {
      badge.style.display = 'flex';
      badge.textContent   = state.alertCount > 9 ? '9+' : state.alertCount;
    } else {
      badge.style.display = 'none';
    }

    renderAlertPanel();
  }

  // ─────────────────────────────────────────────
  //  Preguntas rápidas
  // ─────────────────────────────────────────────
  function renderQuickQuestions() {
    const container = document.getElementById('sm-quick');
    CONFIG.QUICK_QUESTIONS.forEach(q => {
      const btn = document.createElement('button');
      btn.className = 'sm-quick-btn';
      btn.textContent = q;
      btn.addEventListener('click', () => sendMessage(q));
      container.appendChild(btn);
    });
  }

  // ─────────────────────────────────────────────
  //  Toggle de paneles
  // ─────────────────────────────────────────────
  function toggleChat() {
    state.open = !state.open;
    state.alertPanelOpen = false;

    document.getElementById('sm-chat-window').classList.toggle('open', state.open);
    document.getElementById('sm-alert-panel').classList.remove('open');
    document.getElementById('sm-btn-chat').textContent = state.open ? '✕' : '💬';

    if (state.open) {
      const msgs = document.getElementById('sm-messages');
      if (msgs.children.length === 0) {
        appendMessage(
          '¡Hola! Soy tu asistente ShopMaster 👋\nPuedes preguntarme sobre ventas, inventario o deudas.',
          'bot'
        );
      }
      setTimeout(() => document.getElementById('sm-input').focus(), 100);
    }
  }

  function toggleAlertPanel() {
    state.alertPanelOpen = !state.alertPanelOpen;
    state.open = false;

    document.getElementById('sm-alert-panel').classList.toggle('open', state.alertPanelOpen);
    document.getElementById('sm-chat-window').classList.remove('open');
    document.getElementById('sm-btn-chat').textContent = '💬';
  }

  // ─────────────────────────────────────────────
  //  Event listeners
  // ─────────────────────────────────────────────
  function bindEvents() {
    document.getElementById('sm-btn-chat').addEventListener('click', toggleChat);
    document.getElementById('sm-btn-bell').addEventListener('click', toggleAlertPanel);
    document.getElementById('sm-close-btn').addEventListener('click', toggleChat);

    document.getElementById('sm-send-btn').addEventListener('click', () => {
      sendMessage(document.getElementById('sm-input').value.trim());
    });

    document.getElementById('sm-input').addEventListener('keydown', (e) => {
      if (e.key === 'Enter' && !e.shiftKey) {
        e.preventDefault();
        sendMessage(e.target.value.trim());
      }
    });

    // Cerrar paneles al hacer clic fuera
    document.addEventListener('click', (e) => {
      const root = document.getElementById('sm-chatbot-root');
      if (!root.contains(e.target)) {
        state.alertPanelOpen = false;
        document.getElementById('sm-alert-panel').classList.remove('open');
      }
    });
  }

  // ─────────────────────────────────────────────
  //  Inicialización
  // ─────────────────────────────────────────────
  function init(options = {}) {
    // tiendaId: desde options, desde data-attribute del script, o desde el DOM
    state.tiendaId =
      options.tiendaId ||
      document.currentScript?.dataset?.tiendaId ||
      document.getElementById('tiendaId')?.value ||
      '';

    if (!state.tiendaId) {
      console.warn('[ShopMaster Chatbot] tiendaId no encontrado. El chatbot no funcionará correctamente.');
    }

    injectStyles();
    createHTML();
    renderQuickQuestions();
    bindEvents();

    // Cargar alertas al inicio y luego periodicamente
    fetchAlertas();
    setInterval(fetchAlertas, CONFIG.POLL_INTERVAL_MS);
  }

  // ─────────────────────────────────────────────
  //  Auto-init si se incluye con data-tienda-id
  // ─────────────────────────────────────────────
  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', () => init());
  } else {
    init();
  }

  // API pública
  window.ShopChatBot = { init, fetchAlertas };
})();
