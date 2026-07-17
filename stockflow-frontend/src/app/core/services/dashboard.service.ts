import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  DashboardOverviewResponse, DashboardStockResponse,
  DashboardMovementsResponse, DashboardSupplierResponse,
} from '../models/domain.models';

@Injectable({ providedIn: 'root' })
export class DashboardService {
  private api = `${environment.apiUrl}/api/v1/dashboards`;

  constructor(private http: HttpClient) {}

  getOverview(): Observable<DashboardOverviewResponse> {
    return this.http.get<DashboardOverviewResponse>(`${this.api}/overview`);
  }

  getStocks(): Observable<DashboardStockResponse> {
    return this.http.get<DashboardStockResponse>(`${this.api}/stocks`);
  }

  getMovements(): Observable<DashboardMovementsResponse> {
    return this.http.get<DashboardMovementsResponse>(`${this.api}/movements`);
  }

  getSuppliers(): Observable<DashboardSupplierResponse> {
    return this.http.get<DashboardSupplierResponse>(`${this.api}/suppliers`);
  }
}
