import { Component, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatTabsModule } from '@angular/material/tabs';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ConfirmDialogComponent } from '../../../shared/components/confirm-dialog/confirm-dialog.component';
import { SupplierService } from '../../../core/services/supplier.service';
import { AuthService } from '../../../core/auth/auth.service';
import { SupplierResponse } from '../../../core/models/domain.models';
import { UserRole } from '../../../core/models/enums';
import { LoadingSpinnerComponent } from '../../../shared/components/loading-spinner/loading-spinner.component';
import { CurrencyBrPipe } from '../../../shared/pipes/currency.pipe';
import { EnumLabelPipe } from '../../../shared/pipes/enum-label.pipe';
import { DatePipe } from '@angular/common';
import { finalize, switchMap, catchError, of } from 'rxjs';

@Component({
  selector: 'app-supplier-detail',
  standalone: true,
  imports: [RouterModule, MatCardModule, MatTabsModule, MatButtonModule, MatIconModule, MatTooltipModule, LoadingSpinnerComponent, CurrencyBrPipe, EnumLabelPipe, DatePipe],
  template: `
    <div class="header">
      <button mat-icon-button routerLink="/suppliers"><mat-icon>arrow_back</mat-icon></button>
      <h2>{{ supplier()?.tradeName || supplier()?.name || 'Fornecedor' }}</h2>
      @if (!loading() && supplier() && auth.hasRole(UserRole.MANAGER)) {
        <a mat-icon-button [routerLink]="['/suppliers', supplier()?.id, 'edit']"><mat-icon>edit</mat-icon></a>
      }
    </div>

    @if (loading()) {
      <app-loading-spinner message="Carregando fornecedor..." />
    } @else if (supplier()) {
      <mat-tab-group>
        <mat-tab label="Dados da Empresa">
          <mat-card><mat-card-content>
            <div class="detail-grid">
              <div class="detail-field"><strong>Razão Social</strong><span>{{ supplier()?.name }}</span></div>
              <div class="detail-field"><strong>CNPJ</strong><span>{{ supplier()?.cnpj }}</span></div>
              <div class="detail-field"><strong>Nome Fantasia</strong><span>{{ supplier()?.tradeName || '-' }}</span></div>
              <div class="detail-field"><strong>Website</strong><span>{{ supplier()?.website || '-' }}</span></div>
              <div class="detail-field"><strong>Pedido Mínimo</strong><span>{{ supplier()?.minOrderValue | currencyBr }}</span></div>
              <div class="detail-field"><strong>Observações</strong><span>{{ supplier()?.notes || '-' }}</span></div>
              <div class="detail-field"><strong>Criado em</strong><span>{{ supplier()?.createdAt | date:'dd/MM/yyyy HH:mm' }}</span></div>
            </div>
          </mat-card-content></mat-card>
        </mat-tab>

        <mat-tab label="Contatos ({{ supplier()?.contacts?.length || 0 }})">
          @if (supplier()?.contacts?.length) {
            @for (c of supplier()?.contacts; track c.id) {
              <mat-card class="sub-card"><mat-card-content>
                <div class="sub-card-row">
                  <div class="detail-grid" style="flex:1">
                    <div class="detail-field"><strong>Nome</strong><span>{{ c.contactName }}</span></div>
                    <div class="detail-field"><strong>Email</strong><span>{{ c.email }}</span></div>
                    <div class="detail-field"><strong>Telefone</strong><span>{{ c.phoneNumber }}</span></div>
                  </div>
                  @if (auth.hasRole(UserRole.ADMIN)) {
                    <button mat-icon-button color="warn" (click)="onDeleteContact(c.id, c.contactName)" matTooltip="Remover contato">
                      <mat-icon>delete</mat-icon>
                    </button>
                  }
                </div>
              </mat-card-content></mat-card>
            }
          } @else {
            <mat-card class="sub-card"><mat-card-content><p class="empty-text">Nenhum contato cadastrado.</p></mat-card-content></mat-card>
          }
        </mat-tab>

        <mat-tab label="Endereços ({{ supplier()?.addresses?.length || 0 }})">
          @if (supplier()?.addresses?.length) {
            @for (a of supplier()?.addresses; track a.id) {
              <mat-card class="sub-card"><mat-card-content>
                <div class="sub-card-row">
                  <div class="detail-grid" style="flex:1">
                    <div class="detail-field"><strong>CEP</strong><span>{{ a.zipCode }}</span></div>
                    <div class="detail-field"><strong>Logradouro</strong><span>{{ a.street }}, {{ a.streetNumber }} {{ a.complement || '' }}</span></div>
                    <div class="detail-field"><strong>Bairro / Cidade</strong><span>{{ a.neighborhood }} — {{ a.city }}/{{ a.stateUF | enumLabel:'stateUF' }}</span></div>
                    <div class="detail-field"><strong>País</strong><span>{{ a.country }} @if(a.isMain) {<span class="badge">Principal</span>}</span></div>
                  </div>
                  @if (auth.hasRole(UserRole.ADMIN)) {
                    <button mat-icon-button color="warn" (click)="onDeleteAddress(a.id)" matTooltip="Remover endereço">
                      <mat-icon>delete</mat-icon>
                    </button>
                  }
                </div>
              </mat-card-content></mat-card>
            }
          } @else {
            <mat-card class="sub-card"><mat-card-content><p class="empty-text">Nenhum endereço cadastrado.</p></mat-card-content></mat-card>
          }
        </mat-tab>
      </mat-tab-group>
    } @else {
      <mat-card><mat-card-content><p class="empty-text">Fornecedor não encontrado.</p></mat-card-content></mat-card>
    }
  `,
  styles: [`
    .header { display: flex; align-items: center; gap: 12px; margin-bottom: 24px; }
    .header h2 { margin: 0; font-size: 22px; font-weight: 700; }
    .detail-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(260px, 1fr)); gap: 16px; }
    .detail-field { display: flex; flex-direction: column; gap: 4px; }
    .detail-field strong { font-size: 12px; color: #6b7280; text-transform: uppercase; letter-spacing: 0.5px; }
    .detail-field span { font-size: 15px; color: #1a1a2e; }
    .sub-card { margin: 12px 0; }
    .sub-card-row { display: flex; align-items: flex-start; gap: 8px; }
    .badge { display: inline-block; background: #dbeafe; color: #1e40af; padding: 2px 8px; border-radius: 6px; font-size: 11px; font-weight: 600; margin-left: 8px; }
    .empty-text { color: #9ca3af; font-size: 14px; text-align: center; padding: 20px 0; margin: 0; }
  `],
})
export class SupplierDetailComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private supplierService = inject(SupplierService);
  private snackBar = inject(MatSnackBar);
  private dialog = inject(MatDialog);
  protected auth = inject(AuthService);
  protected UserRole = UserRole;
  supplier = signal<SupplierResponse | null>(null);
  loading = signal(true);

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (!id) { this.loading.set(false); return; }

    this.loading.set(true);

    // Busca o fornecedor pelo ID via lista e depois pelo CNPJ para obter dados completos
    this.supplierService.getAll(0, 100).pipe(
      finalize(() => this.loading.set(false)),
      switchMap(page => {
        const found = page.content.find(s => s.id === id);
        if (!found) {
          this.snackBar.open('Fornecedor não encontrado.', 'OK', { duration: 3000 });
          return of(null);
        }
        return this.supplierService.getByCnpj(found.cnpj);
      }),
      catchError(() => {
        this.snackBar.open('Erro ao carregar fornecedor.', 'OK', { duration: 3000 });
        return of(null);
      })
    ).subscribe(detail => {
      if (detail) this.supplier.set(detail);
    });
  }

  onDeleteContact(contactId: string, contactName: string): void {
    this.dialog.open(ConfirmDialogComponent, {
      data: { title: 'Remover Contato', message: `Remover o contato "${contactName}"?`, confirmLabel: 'Remover' }
    }).afterClosed().subscribe(confirmed => {
      if (confirmed) {
        this.supplierService.deleteContact(contactId).subscribe(() => {
          this.snackBar.open('Contato removido!', 'OK', { duration: 3000 });
          this.reloadSupplier();
        });
      }
    });
  }

  onDeleteAddress(addressId: string): void {
    this.dialog.open(ConfirmDialogComponent, {
      data: { title: 'Remover Endereço', message: 'Remover este endereço?', confirmLabel: 'Remover' }
    }).afterClosed().subscribe(confirmed => {
      if (confirmed) {
        this.supplierService.deleteAddress(addressId).subscribe(() => {
          this.snackBar.open('Endereço removido!', 'OK', { duration: 3000 });
          this.reloadSupplier();
        });
      }
    });
  }

  private reloadSupplier(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (!id) return;
    this.supplierService.getAll(0, 100).pipe(
      switchMap(page => {
        const found = page.content.find(s => s.id === id);
        return found ? this.supplierService.getByCnpj(found.cnpj) : of(null);
      })
    ).subscribe(detail => {
      if (detail) this.supplier.set(detail);
    });
  }
}
