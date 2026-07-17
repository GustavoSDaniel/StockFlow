import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Page } from '../models/page.model';
import { UserResponse } from '../models/domain.models';
import { UserRole } from '../models/enums';

@Injectable({ providedIn: 'root' })
export class UserService {
  private api = `${environment.apiUrl}/api/v1/users`;

  constructor(private http: HttpClient) {}

  getCurrentUser(): Observable<UserResponse> {
    return this.http.get<UserResponse>(`${this.api}/me`);
  }

  getAll(page = 0, size = 10, sort = 'userName,asc'): Observable<Page<UserResponse>> {
    const params = new HttpParams().set('page', page).set('size', size).set('sort', sort);
    return this.http.get<Page<UserResponse>>(this.api, { params });
  }

  search(name: string, page = 0, size = 10): Observable<Page<UserResponse>> {
    const params = new HttpParams().set('name', name).set('page', page).set('size', size);
    return this.http.get<Page<UserResponse>>(`${this.api}/search`, { params });
  }

  promote(userId: string, newRole: UserRole): Observable<void> {
    return this.http.patch<void>(`${this.api}/${userId}/promote`, {}, { params: { newRole } });
  }

  activate(userId: string): Observable<void> {
    return this.http.patch<void>(`${this.api}/${userId}/active`, {});
  }

  disable(userId: string): Observable<void> {
    return this.http.patch<void>(`${this.api}/${userId}/disable`, {});
  }

  delete(userId: string): Observable<void> {
    return this.http.delete<void>(`${this.api}/${userId}`);
  }
}
