import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Page } from '../models/page.model';
import { NotificationResponse, NotificationFilter } from '../models/domain.models';
import { NotificationType, NotificationPriority } from '../models/enums';

@Injectable({ providedIn: 'root' })
export class NotificationService {
  private api = `${environment.apiUrl}/api/v1/notifications`;

  constructor(private http: HttpClient) {}

  getAll(page = 0, size = 10): Observable<Page<NotificationResponse>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get<Page<NotificationResponse>>(this.api, { params });
  }

  getFiltered(filter: NotificationFilter, page = 0, size = 10): Observable<Page<NotificationResponse>> {
    let params = new HttpParams().set('page', page).set('size', size);
    if (filter.from) params = params.set('from', filter.from);
    if (filter.to) params = params.set('to', filter.to);
    if (filter.type) params = params.set('type', filter.type);
    if (filter.priority) params = params.set('priority', filter.priority);
    if (filter.read !== undefined) params = params.set('read', filter.read);
    if (filter.resolved !== undefined) params = params.set('resolved', filter.resolved);
    return this.http.get<Page<NotificationResponse>>(`${this.api}/filter`, { params });
  }

  getUnread(page = 0, size = 10): Observable<Page<NotificationResponse>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get<Page<NotificationResponse>>(`${this.api}/unread`, { params });
  }

  getByPriority(priority: NotificationPriority, page = 0, size = 10): Observable<Page<NotificationResponse>> {
    const params = new HttpParams().set('priority', priority).set('page', page).set('size', size);
    return this.http.get<Page<NotificationResponse>>(`${this.api}/priority`, { params });
  }

  getByType(type: NotificationType, page = 0, size = 10): Observable<Page<NotificationResponse>> {
    const params = new HttpParams().set('type', type).set('page', page).set('size', size);
    return this.http.get<Page<NotificationResponse>>(`${this.api}/type`, { params });
  }

  getByProduct(productId: string, page = 0, size = 10): Observable<Page<NotificationResponse>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get<Page<NotificationResponse>>(`${this.api}/${productId}/product`, { params });
  }

  getResolved(page = 0, size = 10): Observable<Page<NotificationResponse>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get<Page<NotificationResponse>>(`${this.api}/resolved`, { params });
  }

  getUnresolved(page = 0, size = 10): Observable<Page<NotificationResponse>> {
    const params = new HttpParams().set('page', page).set('size', size);
    return this.http.get<Page<NotificationResponse>>(`${this.api}/unresolved`, { params });
  }

  markAsRead(id: string): Observable<void> {
    return this.http.patch<void>(`${this.api}/${id}/read`, {});
  }

  markAsResolved(id: string): Observable<void> {
    return this.http.patch<void>(`${this.api}/${id}/resolved`, {});
  }
}
