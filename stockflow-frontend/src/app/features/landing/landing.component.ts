import { Component, inject, OnInit } from '@angular/core';
import { Router, RouterModule } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { AuthService } from '../../core/auth/auth.service';

@Component({
  selector: 'app-landing',
  standalone: true,
  imports: [MatButtonModule, MatIconModule, RouterModule],
  template: `
    <div class="landing-root">
      <!-- ====== NAVBAR ====== -->
      <nav class="navbar">
        <div class="nav-inner">
          <a class="nav-brand" href="#">
            <mat-icon>inventory_2</mat-icon>
            <span>StockFlow</span>
          </a>
          <div class="nav-actions">
            <button mat-stroked-button class="btn-nav-outline" (click)="login()">Entrar</button>
            <button mat-flat-button class="btn-nav-solid" (click)="register()">Demonstração</button>
          </div>
        </div>
      </nav>

      <!-- Decorative blobs -->
      <div class="blob blob-hero-1"></div>
      <div class="blob blob-hero-2"></div>

      <!-- ====== HERO ====== -->
      <section class="hero">
        <div class="hero-content">
          <div class="hero-badge">
            <span class="badge-dot"></span> Ambiente de Demonstração
          </div>
          <h1 class="hero-title">
            Explore o futuro da<br />
            <span class="gradient-text">gestão de inventário</span>
          </h1>
          <p class="hero-subtitle">
            Navegue por um módulo de testes completo: cadastre produtos, controle
            estoques, simule movimentações e visualize dashboards inteligentes.
            Sem compromisso, sem custo — apenas sua avaliação.
          </p>
          <div class="hero-actions">
            <button mat-flat-button class="btn-primary" (click)="register()">
              Testar o sistema
              <mat-icon>arrow_forward</mat-icon>
            </button>
            <button mat-stroked-button class="btn-secondary" (click)="login()">
              Fazer login
            </button>
          </div>
          <div class="hero-stats">
            <div class="stat">
              <span class="stat-number">6</span>
              <span class="stat-label">Módulos disponíveis</span>
            </div>
            <div class="stat">
              <span class="stat-number">3</span>
              <span class="stat-label">Perfis de acesso</span>
            </div>
            <div class="stat">
              <span class="stat-number">100%</span>
              <span class="stat-label">Funcionalidades liberadas</span>
            </div>
          </div>
        </div>

        <!-- Hero visual — realistic mini dashboard -->
        <div class="hero-visual">
          <div class="dashboard-mockup">
            <!-- Toolbar -->
            <div class="mockup-toolbar">
              <div class="mockup-dots"><span></span><span></span><span></span></div>
              <div class="mockup-toolbar-title">Dashboard — Visão Geral</div>
              <div class="mockup-avatar"></div>
            </div>
            <!-- Sidebar + Content -->
            <div class="mockup-body">
              <div class="mockup-sidebar">
                <div class="mockup-nav-item active"><span class="mockup-nav-icon">📦</span></div>
                <div class="mockup-nav-item"><span class="mockup-nav-icon">🏭</span></div>
                <div class="mockup-nav-item"><span class="mockup-nav-icon">📊</span></div>
                <div class="mockup-nav-item"><span class="mockup-nav-icon">🔔</span></div>
                <div class="mockup-nav-item"><span class="mockup-nav-icon">⚙️</span></div>
              </div>
              <div class="mockup-content">
                <!-- KPI cards with real data -->
                <div class="mockup-kpi-row">
                  <div class="mockup-kpi">
                    <div class="mockup-kpi-label">Total Produtos</div>
                    <div class="mockup-kpi-value">1,284</div>
                    <div class="mockup-kpi-trend up">▲ 12%</div>
                  </div>
                  <div class="mockup-kpi">
                    <div class="mockup-kpi-label">Em Estoque</div>
                    <div class="mockup-kpi-value">48,590</div>
                    <div class="mockup-kpi-trend up">▲ 8%</div>
                  </div>
                  <div class="mockup-kpi">
                    <div class="mockup-kpi-label">Alertas</div>
                    <div class="mockup-kpi-value mockup-warn">12</div>
                    <div class="mockup-kpi-trend down">▼ 3</div>
                  </div>
                </div>
                <!-- Mini bar chart -->
                <div class="mockup-chart">
                  <div class="mockup-chart-title">Movimentações — Últimos 7 dias</div>
                  <div class="mockup-bars">
                    <div class="mockup-bar-group">
                      <div class="mockup-bar" style="height:60%"></div><span>Seg</span>
                    </div>
                    <div class="mockup-bar-group">
                      <div class="mockup-bar" style="height:85%"></div><span>Ter</span>
                    </div>
                    <div class="mockup-bar-group">
                      <div class="mockup-bar" style="height:45%"></div><span>Qua</span>
                    </div>
                    <div class="mockup-bar-group">
                      <div class="mockup-bar" style="height:95%"></div><span>Qui</span>
                    </div>
                    <div class="mockup-bar-group">
                      <div class="mockup-bar" style="height:70%"></div><span>Sex</span>
                    </div>
                    <div class="mockup-bar-group">
                      <div class="mockup-bar" style="height:40%"></div><span>Sáb</span>
                    </div>
                    <div class="mockup-bar-group">
                      <div class="mockup-bar" style="height:30%"></div><span>Dom</span>
                    </div>
                  </div>
                </div>
                <!-- Mini table with status badges -->
                <div class="mockup-table">
                  <div class="mockup-table-header">
                    <span>Produto</span><span>Categoria</span><span>Status</span>
                  </div>
                  <div class="mockup-table-row">
                    <span>Parafuso M8</span><span>Fixadores</span><span class="mockup-badge green">Ativo</span>
                  </div>
                  <div class="mockup-table-row">
                    <span>Motor 5CV</span><span>Elétricos</span><span class="mockup-badge amber">Baixo</span>
                  </div>
                  <div class="mockup-table-row">
                    <span>Rolamento 6205</span><span>Mecânicos</span><span class="mockup-badge red">Falta</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- ====== FEATURES ====== -->
      <section class="features-section">
        <div class="section-header">
          <span class="section-eyebrow">O que você pode testar</span>
          <h2 class="section-title">Funcionalidades disponíveis para avaliação</h2>
          <p class="section-subtitle">
            Explore todas as funcionalidades do sistema neste ambiente de demonstração.
          </p>
        </div>

        <div class="features-grid">
          <div class="feature-card">
            <div class="fc-icon fc-icon-purple">
              <mat-icon>inventory_2</mat-icon>
            </div>
            <h3>Gestão de Produtos</h3>
            <p>Cadastre, edite e categorize produtos com SKU automático. Controle status e visibilidade.</p>
          </div>

          <div class="feature-card">
            <div class="fc-icon fc-icon-blue">
              <mat-icon>warehouse</mat-icon>
            </div>
            <h3>Controle de Estoque</h3>
            <p>Monitore níveis em tempo real. Alertas automáticos para estoque baixo, ponto de reposição e excesso.</p>
          </div>

          <div class="feature-card">
            <div class="fc-icon fc-icon-green">
              <mat-icon>swap_vert</mat-icon>
            </div>
            <h3>Movimentações</h3>
            <p>Registre entradas, saídas, ajustes e transferências com rastreabilidade completa e log de auditoria.</p>
          </div>

          <div class="feature-card">
            <div class="fc-icon fc-icon-amber">
              <mat-icon>local_shipping</mat-icon>
            </div>
            <h3>Fornecedores</h3>
            <p>Mantenha contatos, endereços e histórico de fornecimento. Relacionamento centralizado.</p>
          </div>

          <div class="feature-card">
            <div class="fc-icon fc-icon-indigo">
              <mat-icon>bar_chart</mat-icon>
            </div>
            <h3>Dashboards</h3>
            <p>Visão geral com KPIs, gráficos de movimentação e top produtos. Decisões baseadas em dados.</p>
          </div>

          <div class="feature-card">
            <div class="fc-icon fc-icon-pink">
              <mat-icon>notifications</mat-icon>
            </div>
            <h3>Notificações</h3>
            <p>Alertas em tempo real para eventos críticos, reposições e movimentações importantes.</p>
          </div>
        </div>
      </section>

      <!-- ====== HOW IT WORKS ====== -->
      <section class="how-section">
        <div class="section-header">
          <span class="section-eyebrow">Como testar</span>
          <h2 class="section-title">Comece a avaliar em 3 passos</h2>
        </div>

        <div class="steps-row">
          <div class="step">
            <div class="step-number">1</div>
            <h3>Acesse o ambiente</h3>
            <p>Registre-se ou faça login rapidamente para entrar no módulo de demonstração. Sem burocracia.</p>
          </div>
          <div class="step-connector"></div>
          <div class="step">
            <div class="step-number">2</div>
            <h3>Explore os módulos</h3>
            <p>Navegue por produtos, estoques, fornecedores e dashboards. Todos os recursos liberados para teste.</p>
          </div>
          <div class="step-connector"></div>
          <div class="step">
            <div class="step-number">3</div>
            <h3>Avalie o potencial</h3>
            <p>Visualize relatórios, simule operações e veja como o StockFlow pode otimizar sua operação real.</p>
          </div>
        </div>
      </section>

      <!-- ====== CTA ====== -->
      <section class="cta-section">
        <div class="cta-blob"></div>
        <div class="cta-card">
          <h2>Gostou do que viu?</h2>
          <p>
            Este é um ambiente de demonstração do StockFlow — um futuro SaaS
            para gestão inteligente de inventário. Explore à vontade e, se
            quiser saber mais sobre a versão completa, entre em contato.
          </p>
          <div class="cta-actions">
            <button mat-flat-button class="btn-primary btn-lg" (click)="register()">
              Explorar ambiente de demonstração
            </button>
          </div>
        </div>
      </section>

      <!-- ====== FOOTER ====== -->
      <footer class="footer">
        <div class="footer-inner">
          <div class="footer-brand">
            <div class="footer-logo">
              <mat-icon>inventory_2</mat-icon>
              <span>StockFlow</span>
            </div>
            <p>© {{ currentYear }} StockFlow. Todos os direitos reservados.</p>
          </div>
          <div class="footer-links">
            <a (click)="login()">Entrar</a>
            <a (click)="register()">Demonstração</a>
          </div>
        </div>
        <div class="blob blob-footer"></div>
      </footer>
    </div>
  `,
  styles: [`
    :host { display: block; }

    /* ===== ROOT ===== */
    .landing-root {
      font-family: 'Inter', -apple-system, sans-serif;
      background: linear-gradient(180deg, #fafbfd 0%, #f3f4f9 50%, #faf9fe 100%);
      color: #1a1a2e;
      overflow-x: hidden;
      position: relative;
    }

    /* ===== NAVBAR ===== */
    .navbar {
      position: sticky;
      top: 0;
      z-index: 100;
      background: rgba(255, 255, 255, 0.8);
      backdrop-filter: blur(20px);
      -webkit-backdrop-filter: blur(20px);
      border-bottom: 1px solid rgba(0,0,0,0.05);
    }
    .nav-inner {
      max-width: 1200px;
      margin: 0 auto;
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 0 24px;
      height: 64px;
    }
    .nav-brand {
      display: flex;
      align-items: center;
      gap: 10px;
      text-decoration: none;
      font-size: 20px;
      font-weight: 700;
      color: #1a1a2e;
      letter-spacing: -0.5px;
    }
    .nav-brand mat-icon {
      color: #7c3aed;
      font-size: 28px;
      width: 28px;
      height: 28px;
    }
    .nav-actions {
      display: flex;
      gap: 10px;
    }
    .btn-nav-outline {
      border-radius: 10px !important;
      border: 2px solid #d1d5db !important;
      color: #374151 !important;
      font-weight: 600 !important;
      height: 40px !important;
      font-size: 14px !important;
      padding: 0 20px !important;
    }
    .btn-nav-solid {
      border-radius: 10px !important;
      background: linear-gradient(135deg, #7c3aed, #6366f1) !important;
      color: #fff !important;
      font-weight: 600 !important;
      height: 40px !important;
      font-size: 14px !important;
      padding: 0 20px !important;
      box-shadow: 0 2px 8px rgba(124, 58, 237, 0.3) !important;
    }

    /* ===== ANIMATED BLOBS ===== */
    .blob {
      position: absolute;
      border-radius: 50%;
      filter: blur(80px);
      pointer-events: none;
      z-index: 0;
    }
    .blob-hero-1 {
      width: 500px; height: 500px;
      background: radial-gradient(circle, rgba(124, 58, 237, 0.12), transparent);
      top: 80px; right: -150px;
      animation: blob1 14s ease-in-out infinite;
    }
    .blob-hero-2 {
      width: 350px; height: 350px;
      background: radial-gradient(circle, rgba(99, 102, 241, 0.1), transparent);
      top: 400px; left: -100px;
      animation: blob2 12s ease-in-out infinite;
    }
    .blob-footer {
      width: 300px; height: 300px;
      background: radial-gradient(circle, rgba(124, 58, 237, 0.08), transparent);
      bottom: -100px; right: -100px;
    }
    @keyframes blob1 {
      0%,100%{transform:translate(0,0) scale(1)}
      33%{transform:translate(-40px,30px) scale(1.06)}
      66%{transform:translate(30px,-40px) scale(0.94)}
    }
    @keyframes blob2 {
      0%,100%{transform:translate(0,0) scale(1)}
      50%{transform:translate(30px,-25px) scale(1.08)}
    }

    /* ===== HERO ===== */
    .hero {
      position: relative;
      z-index: 1;
      max-width: 1200px;
      margin: 0 auto;
      padding: 80px 24px 100px;
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 60px;
      align-items: center;
    }
    .hero-badge {
      display: inline-flex;
      align-items: center;
      gap: 8px;
      background: rgba(124, 58, 237, 0.08);
      border: 1px solid rgba(124, 58, 237, 0.15);
      border-radius: 100px;
      padding: 6px 16px;
      font-size: 13px;
      font-weight: 500;
      color: #7c3aed;
      margin-bottom: 24px;
    }
    .badge-dot {
      width: 8px; height: 8px;
      border-radius: 50%;
      background: #7c3aed;
      animation: pulse-dot 2s ease-in-out infinite;
    }
    @keyframes pulse-dot {
      0%,100%{opacity:1}
      50%{opacity:.4}
    }
    .hero-title {
      font-size: 48px;
      font-weight: 800;
      line-height: 1.15;
      letter-spacing: -1.5px;
      color: #1a1a2e;
      margin: 0 0 24px;
    }
    .gradient-text {
      background: linear-gradient(135deg, #7c3aed 0%, #6366f1 50%, #a855f7 100%);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
      background-clip: text;
    }
    .hero-subtitle {
      font-size: 17px;
      line-height: 1.7;
      color: #6b7280;
      margin: 0 0 36px;
      max-width: 500px;
    }
    .hero-actions {
      display: flex;
      gap: 14px;
      margin-bottom: 48px;
    }
    .btn-primary {
      height: 52px !important;
      padding: 0 28px !important;
      font-size: 16px !important;
      font-weight: 600 !important;
      border-radius: 14px !important;
      background: linear-gradient(135deg, #7c3aed, #6366f1) !important;
      color: #fff !important;
      box-shadow: 0 4px 20px rgba(124, 58, 237, 0.35) !important;
      transition: all 0.2s ease !important;
    }
    .btn-primary:hover {
      transform: translateY(-2px);
      box-shadow: 0 8px 28px rgba(124, 58, 237, 0.5) !important;
    }
    .btn-primary mat-icon {
      margin-left: 6px;
      font-size: 20px; width: 20px; height: 20px;
    }
    .btn-secondary {
      height: 52px !important;
      padding: 0 28px !important;
      font-size: 16px !important;
      font-weight: 600 !important;
      border-radius: 14px !important;
      border: 2px solid #d1d5db !important;
      color: #374151 !important;
    }
    .btn-lg {
      height: 56px !important;
      font-size: 17px !important;
      padding: 0 36px !important;
    }

    .hero-stats {
      display: flex;
      gap: 36px;
    }
    .stat {
      display: flex;
      flex-direction: column;
      gap: 2px;
    }
    .stat-number {
      font-size: 24px;
      font-weight: 700;
      color: #1a1a2e;
      letter-spacing: -0.5px;
    }
    .stat-label {
      font-size: 13px;
      color: #6b7280;
      font-weight: 500;
    }

    /* Hero mockup — realistic mini dashboard */
    .hero-visual {
      display: flex;
      justify-content: center;
      perspective: 800px;
    }
    .dashboard-mockup {
      background: #fff;
      border-radius: 18px;
      border: 1px solid #e5e7eb;
      box-shadow:
        0 25px 60px rgba(0,0,0,0.1),
        0 8px 20px rgba(0,0,0,0.06),
        0 0 0 1px rgba(0,0,0,0.03);
      overflow: hidden;
      width: 100%;
      max-width: 520px;
      transform: rotateY(-4deg) rotateX(3deg);
      transition: transform 0.3s ease;
    }
    .dashboard-mockup:hover {
      transform: rotateY(-1deg) rotateX(1deg);
    }
    .mockup-toolbar {
      height: 38px;
      background: linear-gradient(180deg, #f9fafb 0%, #f3f4f6 100%);
      border-bottom: 1px solid #e5e7eb;
      display: flex;
      align-items: center;
      padding: 0 14px;
      gap: 14px;
    }
    .mockup-dots {
      display: flex;
      gap: 7px;
      flex-shrink: 0;
    }
    .mockup-dots span {
      width: 10px; height: 10px;
      border-radius: 50%;
    }
    .mockup-dots span:nth-child(1) { background: #ef4444; }
    .mockup-dots span:nth-child(2) { background: #f59e0b; }
    .mockup-dots span:nth-child(3) { background: #22c55e; }
    .mockup-toolbar-title {
      flex: 1;
      font-size: 11px;
      font-weight: 600;
      color: #9ca3af;
      text-align: center;
      letter-spacing: 0.3px;
    }
    .mockup-avatar {
      width: 22px; height: 22px;
      border-radius: 50%;
      background: linear-gradient(135deg, #7c3aed, #6366f1);
      flex-shrink: 0;
    }
    .mockup-body {
      display: flex;
      height: 300px;
    }
    .mockup-sidebar {
      width: 52px;
      background: linear-gradient(180deg, #fafbfc 0%, #f5f6f8 100%);
      border-right: 1px solid #e8eaed;
      padding: 10px 7px;
      display: flex;
      flex-direction: column;
      gap: 6px;
    }
    .mockup-nav-item {
      width: 38px; height: 38px;
      border-radius: 10px;
      display: flex;
      align-items: center;
      justify-content: center;
      transition: all 0.15s ease;
    }
    .mockup-nav-item.active {
      background: rgba(124, 58, 237, 0.1);
    }
    .mockup-nav-icon {
      font-size: 16px;
      opacity: 0.5;
      filter: grayscale(0.3);
    }
    .mockup-nav-item.active .mockup-nav-icon {
      opacity: 1;
      filter: none;
    }
    .mockup-content {
      flex: 1;
      padding: 14px 16px;
      display: flex;
      flex-direction: column;
      gap: 12px;
      overflow: hidden;
    }
    /* KPI cards */
    .mockup-kpi-row {
      display: flex;
      gap: 9px;
    }
    .mockup-kpi {
      flex: 1;
      padding: 10px 11px;
      background: #f9fafb;
      border-radius: 10px;
      border: 1px solid #f1f3f4;
      display: flex;
      flex-direction: column;
      gap: 3px;
    }
    .mockup-kpi-label {
      font-size: 9px;
      font-weight: 600;
      color: #9ca3af;
      text-transform: uppercase;
      letter-spacing: 0.3px;
    }
    .mockup-kpi-value {
      font-size: 20px;
      font-weight: 800;
      color: #1a1a2e;
      letter-spacing: -0.5px;
      line-height: 1;
    }
    .mockup-kpi-value.mockup-warn {
      color: #ef4444;
    }
    .mockup-kpi-trend {
      font-size: 10px;
      font-weight: 600;
      letter-spacing: 0.2px;
    }
    .mockup-kpi-trend.up { color: #16a34a; }
    .mockup-kpi-trend.down { color: #ef4444; }
    /* Bar chart */
    .mockup-chart {
      padding: 12px 10px 8px;
      background: linear-gradient(135deg, rgba(124,58,237,0.03), rgba(99,102,241,0.06));
      border-radius: 10px;
      border: 1px solid rgba(124,58,237,0.08);
    }
    .mockup-chart-title {
      font-size: 10px;
      font-weight: 600;
      color: #6b7280;
      margin-bottom: 10px;
    }
    .mockup-bars {
      display: flex;
      align-items: flex-end;
      justify-content: space-between;
      gap: 4px;
      height: 80px;
    }
    .mockup-bar-group {
      flex: 1;
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 4px;
    }
    .mockup-bar {
      width: 100%;
      max-width: 24px;
      border-radius: 4px 4px 0 0;
      background: linear-gradient(180deg, #7c3aed 0%, #a78bfa 100%);
      transition: height 0.3s ease;
    }
    .mockup-bar-group span {
      font-size: 8px;
      color: #9ca3af;
      font-weight: 500;
    }
    /* Table */
    .mockup-table {
      display: flex;
      flex-direction: column;
      gap: 0;
      border: 1px solid #f1f3f4;
      border-radius: 8px;
      overflow: hidden;
    }
    .mockup-table-header {
      display: grid;
      grid-template-columns: 1.2fr 0.9fr 0.7fr;
      gap: 6px;
      padding: 7px 10px;
      background: #f9fafb;
      border-bottom: 1px solid #f1f3f4;
    }
    .mockup-table-header span {
      font-size: 9px;
      font-weight: 700;
      color: #9ca3af;
      text-transform: uppercase;
      letter-spacing: 0.3px;
    }
    .mockup-table-row {
      display: grid;
      grid-template-columns: 1.2fr 0.9fr 0.7fr;
      gap: 6px;
      padding: 8px 10px;
      border-bottom: 1px solid #f9fafb;
      align-items: center;
    }
    .mockup-table-row:last-child {
      border-bottom: none;
    }
    .mockup-table-row span {
      font-size: 11px;
      font-weight: 500;
      color: #374151;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }
    .mockup-badge {
      display: inline-block;
      font-size: 9px !important;
      font-weight: 700 !important;
      padding: 2px 7px;
      border-radius: 100px;
      width: fit-content;
    }
    .mockup-badge.green { background: #dcfce7; color: #16a34a; }
    .mockup-badge.amber { background: #fef3c7; color: #d97706; }
    .mockup-badge.red   { background: #fee2e2; color: #dc2626; }

    /* ===== FEATURES ===== */
    .features-section {
      position: relative;
      z-index: 1;
      max-width: 1200px;
      margin: 0 auto;
      padding: 0 24px 100px;
    }
    .section-header {
      text-align: center;
      margin-bottom: 56px;
    }
    .section-eyebrow {
      display: inline-block;
      font-size: 13px;
      font-weight: 600;
      color: #7c3aed;
      text-transform: uppercase;
      letter-spacing: 1.5px;
      margin-bottom: 12px;
    }
    .section-title {
      font-size: 36px;
      font-weight: 800;
      letter-spacing: -1px;
      color: #1a1a2e;
      margin: 0 0 12px;
    }
    .section-subtitle {
      font-size: 16px;
      color: #6b7280;
      margin: 0;
      max-width: 500px;
      margin: 0 auto;
    }
    .features-grid {
      display: grid;
      grid-template-columns: repeat(3, 1fr);
      gap: 20px;
    }
    .feature-card {
      background: #fff;
      border: 1px solid #e8eaed;
      border-radius: 16px;
      padding: 28px;
      transition: all 0.2s ease;
    }
    .feature-card:hover {
      border-color: #d1d5db;
      box-shadow: 0 8px 24px rgba(0,0,0,0.06);
      transform: translateY(-2px);
    }
    .fc-icon {
      width: 44px; height: 44px;
      border-radius: 12px;
      display: flex;
      align-items: center;
      justify-content: center;
      margin-bottom: 16px;
    }
    .fc-icon mat-icon {
      font-size: 22px; width: 22px; height: 22px;
    }
    .fc-icon-purple { background: #f3e8ff; color: #7c3aed; }
    .fc-icon-blue   { background: #dbeafe; color: #3b82f6; }
    .fc-icon-green  { background: #dcfce7; color: #16a34a; }
    .fc-icon-amber  { background: #fef3c7; color: #d97706; }
    .fc-icon-indigo { background: #e0e7ff; color: #4f46e5; }
    .fc-icon-pink   { background: #fce7f3; color: #ec4899; }
    .feature-card h3 {
      font-size: 16px;
      font-weight: 700;
      color: #1a1a2e;
      margin: 0 0 8px;
    }
    .feature-card p {
      font-size: 14px;
      color: #6b7280;
      line-height: 1.6;
      margin: 0;
    }

    /* ===== HOW IT WORKS ===== */
    .how-section {
      position: relative;
      z-index: 1;
      max-width: 1200px;
      margin: 0 auto;
      padding: 0 24px 100px;
    }
    .steps-row {
      display: flex;
      align-items: flex-start;
      justify-content: center;
      gap: 0;
    }
    .step {
      text-align: center;
      flex: 1;
      max-width: 280px;
    }
    .step-number {
      width: 56px; height: 56px;
      border-radius: 16px;
      background: linear-gradient(135deg, #7c3aed, #6366f1);
      color: #fff;
      font-size: 24px;
      font-weight: 700;
      display: flex;
      align-items: center;
      justify-content: center;
      margin: 0 auto 20px;
      box-shadow: 0 6px 20px rgba(124, 58, 237, 0.25);
    }
    .step h3 {
      font-size: 18px;
      font-weight: 700;
      color: #1a1a2e;
      margin: 0 0 8px;
    }
    .step p {
      font-size: 14px;
      color: #6b7280;
      line-height: 1.6;
      margin: 0;
    }
    .step-connector {
      width: 48px;
      height: 2px;
      background: #e5e7eb;
      margin-top: 28px;
      flex-shrink: 0;
    }

    /* ===== CTA SECTION ===== */
    .cta-section {
      position: relative;
      z-index: 1;
      max-width: 1200px;
      margin: 0 auto;
      padding: 0 24px 100px;
    }
    .cta-blob {
      position: absolute;
      width: 350px; height: 350px;
      top: -80px; left: 50%;
      transform: translateX(-50%);
      background: radial-gradient(circle, rgba(124,58,237,0.1), transparent);
      filter: blur(80px);
      border-radius: 50%;
      pointer-events: none;
    }
    .cta-card {
      position: relative;
      background: linear-gradient(135deg, #1a1a2e 0%, #1e1b4b 50%, #312e81 100%);
      border-radius: 24px;
      padding: 64px 48px;
      text-align: center;
      box-shadow: 0 20px 60px rgba(124, 58, 237, 0.15);
    }
    .cta-card h2 {
      font-size: 32px;
      font-weight: 800;
      color: #fff;
      margin: 0 0 16px;
      letter-spacing: -0.5px;
    }
    .cta-card p {
      font-size: 16px;
      color: rgba(255,255,255,0.65);
      margin: 0 auto 32px;
      max-width: 520px;
      line-height: 1.6;
    }
    .cta-actions {
      display: flex;
      justify-content: center;
    }

    /* ===== FOOTER ===== */
    .footer {
      position: relative;
      border-top: 1px solid #e8eaed;
      padding: 40px 24px;
    }
    .footer-inner {
      position: relative;
      z-index: 1;
      max-width: 1200px;
      margin: 0 auto;
      display: flex;
      align-items: center;
      justify-content: space-between;
    }
    .footer-logo {
      display: flex;
      align-items: center;
      gap: 8px;
      font-size: 18px;
      font-weight: 700;
      color: #1a1a2e;
      margin-bottom: 8px;
    }
    .footer-logo mat-icon {
      color: #7c3aed;
      font-size: 22px; width: 22px; height: 22px;
    }
    .footer-brand p {
      font-size: 13px;
      color: #9ca3af;
      margin: 0;
    }
    .footer-links {
      display: flex;
      gap: 24px;
    }
    .footer-links a {
      font-size: 14px;
      color: #6b7280;
      text-decoration: none;
      font-weight: 500;
      cursor: pointer;
      transition: color 0.15s;
    }
    .footer-links a:hover {
      color: #7c3aed;
    }

    /* ===== RESPONSIVE ===== */
    @media (max-width: 900px) {
      .hero {
        grid-template-columns: 1fr;
        text-align: center;
        padding: 48px 24px 60px;
      }
      .hero-title { font-size: 32px; }
      .hero-subtitle { max-width: 100%; }
      .hero-actions { justify-content: center; flex-wrap: wrap; }
      .hero-stats { justify-content: center; }
      .hero-visual { display: none; }
      .features-grid { grid-template-columns: repeat(2, 1fr); }
      .steps-row { flex-direction: column; align-items: center; gap: 24px; }
      .step-connector {
        width: 2px; height: 32px;
        margin: 0;
      }
    }
    @media (max-width: 600px) {
      .features-grid { grid-template-columns: 1fr; }
      .section-title { font-size: 28px; }
      .cta-card { padding: 40px 24px; }
      .cta-card h2 { font-size: 24px; }
      .navbar { padding: 0 12px; }
      .btn-nav-outline { display: none; }
      .hero-stats { gap: 20px; }
      .footer-inner { flex-direction: column; gap: 16px; text-align: center; }
    }
  `]
})
export class LandingComponent implements OnInit {
  private auth = inject(AuthService);
  private router = inject(Router);
  currentYear = new Date().getFullYear();

  ngOnInit(): void {
    if (this.auth.keycloak?.authenticated) {
      this.router.navigate(['/dashboard/stocks']);
    }
  }

  login(): void {
    this.auth.login();
  }

  register(): void {
    this.auth.register();
  }
}
