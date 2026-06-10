/**
 * AI Chat Manager — 管理聊天面板的完整交互逻辑
 */
var ChatManager = {
  context: '',
  isOpen: false,

  init: function () {
    var self = this;

    document.getElementById('aiFab').addEventListener('click', function () { self.toggle(); });
    document.getElementById('chatClose').addEventListener('click', function () { self.close(); });
    document.getElementById('chatOverlay').addEventListener('click', function () { self.close(); });

    document.getElementById('chatSend').addEventListener('click', function () {
      var input = document.getElementById('chatInput');
      var msg = input.value.trim();
      if (msg) { self.send(msg); input.value = ''; }
    });

    document.getElementById('chatInput').addEventListener('keydown', function (e) {
      if (e.key === 'Enter') document.getElementById('chatSend').click();
    });

    document.querySelectorAll('#quickChips .chip').forEach(function (chip) {
      chip.addEventListener('click', function () {
        self.sendQuick(this.getAttribute('data-prompt'));
      });
    });

    var iframe = document.getElementById('contentFrame');
    if (iframe) {
      iframe.addEventListener('load', function () { self.onIframeChange(); });
      self.onIframeChange();
    }
  },

  detectContext: function () {
    var iframe = document.getElementById('contentFrame');
    if (!iframe) return '';
    try {
      var src = iframe.contentWindow.location.pathname;
      if (src.indexOf('/page/product') !== -1) return 'product';
      if (src.indexOf('/page/user') !== -1) return 'user';
      if (src.indexOf('/page/article') !== -1) return 'article';
    } catch (e) { /* 跨域或未加载 */ }
    return '';
  },

  onIframeChange: function () {
    var ctx = this.detectContext();
    if (ctx && ctx !== this.context) {
      this.context = ctx;
      var labels = { product: '商品', user: '用户', article: '文章' };
      document.getElementById('chatContext').textContent = labels[ctx] || ctx;
    }
  },

  open: function () {
    this.isOpen = true;
    document.getElementById('chatDrawer').classList.add('active');
    document.getElementById('chatOverlay').classList.add('active');
    this.autoSend();
  },

  close: function () {
    this.isOpen = false;
    document.getElementById('chatDrawer').classList.remove('active');
    document.getElementById('chatOverlay').classList.remove('active');
  },

  toggle: function () {
    this.isOpen ? this.close() : this.open();
  },

  send: function (message) {
    var self = this;
    self.renderMessage({ role: 'user', content: message });

    var loadingEl = self.renderLoading();
    var token = localStorage.getItem('token');

    fetch('/api/chat/send', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': 'Bearer ' + token
      },
      body: JSON.stringify({ message: message, context: self.context })
    })
      .then(function (res) {
        if (res.status === 401) {
          localStorage.removeItem('token');
          localStorage.removeItem('username');
          location.href = '/login';
          throw new Error('Unauthorized');
        }
        return res.json();
      })
      .then(function (res) {
        if (loadingEl) loadingEl.remove();
        if (res.code === 200) {
          self.renderMessage({ role: 'ai', content: res.data });
        } else {
          self.renderMessage({ role: 'ai', content: { reply: '❌ ' + (res.message || '服务异常') } });
        }
      })
      .catch(function (err) {
        if (loadingEl) loadingEl.remove();
        if (err.message !== 'Unauthorized') {
          self.renderMessage({ role: 'ai', content: { reply: '❌ AI 服务暂时不可用，请稍后重试' } });
        }
      });
  },

  autoSend: function () {
    var labels = { product: '商品', user: '用户', article: '文章' };
    var label = labels[this.context] || '系统';
    this.send('罗列' + label + '的异常情况');
  },

  renderMessage: function (msg) {
    var container = document.getElementById('chatMessages');

    if (msg.role === 'user') {
      var el = document.createElement('div');
      el.className = 'msg-user';
      el.textContent = msg.content;
      container.appendChild(el);
    } else {
      var data = msg.content;
      var reply = typeof data === 'string' ? data : (data.reply || '');

      if (reply) {
        var el = document.createElement('div');
        el.className = 'msg-ai';
        el.textContent = reply;
        container.appendChild(el);
      }

      if (data && data.anomalies && data.anomalies.length > 0) {
        var self = this;
        data.anomalies.forEach(function (a) {
          container.appendChild(self.createAnomalyCard(a));
        });
      }

      if (data && data.imagePlaceholder) {
        var ph = document.createElement('div');
        ph.className = 'chart-placeholder';
        ph.textContent = '📊 图表区域（接入真实 AI 后启用）';
        container.appendChild(ph);
      }
    }

    container.scrollTop = container.scrollHeight;
  },

  renderLoading: function () {
    var container = document.getElementById('chatMessages');
    var el = document.createElement('div');
    el.className = 'msg-ai';
    el.textContent = '思考中...';
    el.id = 'chatLoading';
    container.appendChild(el);
    container.scrollTop = container.scrollHeight;
    return el;
  },

  createAnomalyCard: function (a) {
    var card = document.createElement('div');
    var sevClass = a.severity === '警告' ? 'severity-warning' : (a.severity === '提示' ? 'severity-info' : '');

    card.className = 'anomaly-card ' + sevClass;
    card.innerHTML =
      '<div class="anomaly-card-header">' +
        '<span class="severity-badge severity-' + a.severity + '">' + a.severity + '</span>' +
        '<span class="anomaly-type">' + ChatManager.escapeHtml(a.type) + '</span>' +
      '</div>' +
      '<div class="anomaly-card-body">' +
        '字段: ' + ChatManager.escapeHtml(a.field) +
        ' | 实际值: ' + ChatManager.escapeHtml(a.actualValue) +
        ' | 期望值: ' + ChatManager.escapeHtml(a.expectedValue) +
      '</div>' +
      '<div class="anomaly-card-actions">' +
        '<button class="btn-detail">详情</button>' +
        '<button class="btn-dismiss">忽略</button>' +
      '</div>';

    card.querySelector('.btn-detail').addEventListener('click', function () { ChatManager.showDetail(a); });
    card.querySelector('.btn-dismiss').addEventListener('click', function () {
      card.style.opacity = '0.5';
      card.querySelector('.btn-detail').disabled = true;
      card.querySelector('.btn-dismiss').disabled = true;
      card.querySelector('.btn-dismiss').textContent = '已忽略';
    });

    return card;
  },

  showDetail: function (a) {
    document.getElementById('detailTitle').textContent = a.type + ' — ' + (a.recordName || '');
    document.getElementById('detailBody').innerHTML =
      '<div class="detail-row"><span class="detail-label">异常类型</span><span class="detail-value">' + ChatManager.escapeHtml(a.type) + '</span></div>' +
      '<div class="detail-row"><span class="detail-label">严重程度</span><span class="detail-value">' + ChatManager.escapeHtml(a.severity) + '</span></div>' +
      '<div class="detail-row"><span class="detail-label">涉及字段</span><span class="detail-value">' + ChatManager.escapeHtml(a.field) + '</span></div>' +
      '<div class="detail-row"><span class="detail-label">实际值</span><span class="detail-value">' + ChatManager.escapeHtml(a.actualValue) + '</span></div>' +
      '<div class="detail-row"><span class="detail-label">期望值</span><span class="detail-value">' + ChatManager.escapeHtml(a.expectedValue) + '</span></div>' +
      '<div class="detail-row"><span class="detail-label">关联记录</span><span class="detail-value">' + ChatManager.escapeHtml(a.recordName || '') + ' (ID: ' + (a.recordId || '-') + ')</span></div>' +
      '<div class="detail-row"><span class="detail-label">修复建议</span><span class="detail-value">' + ChatManager.escapeHtml(a.suggestion || '') + '</span></div>';

    var overlay = document.getElementById('detailOverlay');
    if (!overlay) {
      overlay = document.createElement('div');
      overlay.id = 'detailOverlay';
      overlay.className = 'detail-overlay';
      overlay.addEventListener('click', function () { ChatManager.closeDetail(); });
      document.body.appendChild(overlay);
    }

    document.getElementById('anomalyDetail').classList.add('active');
    overlay.classList.add('active');
  },

  closeDetail: function () {
    document.getElementById('anomalyDetail').classList.remove('active');
    var overlay = document.getElementById('detailOverlay');
    if (overlay) overlay.classList.remove('active');
  },

  sendQuick: function (prompt) { this.send(prompt); },

  escapeHtml: function (text) {
    if (!text) return '';
    var div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
  }
};

document.addEventListener('DOMContentLoaded', function () {
  if (localStorage.getItem('token')) {
    ChatManager.init();
  }
});
