import { Component, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatTabsModule } from '@angular/material/tabs';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { SupplierService } from '../../../core/services/supplier.service';
import { AuthService } from '../../../core/auth/auth.service';
import { SupplierResponse, AddressResponse, SupplierContactResponse } from '../../../core/models/domain.models';
import { UserRole } from '../../../core/models/enums';
import { LoadingSpinnerComponent } from '../../../shared/components/loading-spinner/loading-spinner.component';
import { CurrencyBrPipe } from '../../../shared/pipes/currency.pipe';
import { EnumLabelPipe } from '../../../shared/pipes/enum-label.pipe';
import { DatePipe } from '@angular/common';

@Component({
  selector: 'app-supplier-detail',
  standalone: true,
  imports: [RouterModule, MatCardModule, MatTabsModule, MatButtonModule, MatIconModule, LoadingSpinnerComponent, CurrencyBrPipe, EnumLabelPipe, DatePipe],
  template: `
    <div class="header"><button mat-icon-button routerLink="/suppliers"><mat-icon>arrow_back</mat-icon></button><h2>{{ supplier()?.tradeName || supplier()?.name || 'Fornecedor' }}</h2></div>
    @if (loading()) { <app-loading-spinner /> }
    @else if (supplier()) {
      <mat-tab-group>
        <mat-tab label="Dados">
          <mat-card><mat-card-content class="grid">
            <div><strong>Razão Social:</strong> {{ supplier()?.name }}</div>
            <div><strong>CNPJ:</strong> {{ supplier()?.cnpj }}</div>
            <div><strong>Nome Fantasia:</strong> {{ supplier()?.tradeName }}</div>
            <div><strong>Website:</strong> {{ supplier()?.website || '-' }}</div>
            <div><strong>Pedido Mínimo:</strong> {{ supplier()?.minOrderValue | currencyBr }}</div>
            <div><strong>Observações:</strong> {{ supplier()?.notes || '-' }}</div>
          </mat-card-content></mat-card>
        </mat-tab>
        <mat-tab label="Contatos ({{ supplier()?.contacts?.length || 0 }})">
          @for (c of supplier()?.contacts; track c.id) {
            <mat-card class="sub-card"><mat-card-content><div class="grid">
              <div><strong>Nome:</strong> {{ c.contactName }}</div><div><strong>Email:</strong> {{ c.email }}</div><div><strong>Telefone:</strong> {{ c.phoneNumber }}</div>
            </div></mat-card-content></mat-card>
          }
        </mat-tab>
        <mat-tab label="Endereços ({{ supplier()?.addresses?.length || 0 }})">
          @for (a of supplier()?.addresses; track a.id) {
            <mat-card class="sub-card"><mat-card-content><div class="grid">
              <div><strong>CEP:</strong> {{ a.zipCode }}</div><div>{{ a.street }}, {{ a.streetNumber }} {{ a.complement }}</div>
              <div>{{ a.neighborhood }} - {{ a.city }}/{{ a.stateUF | enumLabel:'stateUF' }}</div>
              <div>{{ a.country }} @if(a.isMain) { <span class="badge">Principal</span> }</div>
            </div></mat-card-content></mat-card>
          }
        </mat-tab>
      </mat-tab-group>
    }
  `,
  styles: [`.header { display: flex; align-items: center; gap: 12px; margin-bottom: 24px; } .grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(250px, 1fr)); gap: 12px; } .sub-card { margin: 12px 0; } .badge { background: #dbeafe; color: #1e40af; padding: 2px 8px; border-radius: 4px; font-size: 12px; }`],
})
export class SupplierDetailComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private supplierService = inject(SupplierService);
  protected auth = inject(AuthService);
  protected UserRole = UserRole;
  supplier = signal<SupplierResponse | null>(null);
  loading = signal(true);

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id')!;
    this.supplierService.getByCnpj('').subscribe(); // fallback - in real impl, get by ID
    // For now load from list; ideally the API provides getById for supplier detail
    this.supplierService.getAll(0, 1).subscribe(); // placeholder
    this.loading.set(false);
  }
}
