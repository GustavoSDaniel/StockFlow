import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ErrorDocResponse } from '../models/domain.models';

@Injectable({ providedIn: 'root' })
export class ErrorDocService {
  private api = `${environment.apiUrl}/api/v1/errors`;

  constructor(private http: HttpClient) {}

  getAll(): Observable<Record<string, ErrorDocResponse>> {
    return this.http.get<Record<string, ErrorDocResponse>>(this.api);
  }

  getByKey(errorKey: string): Observable<ErrorDocResponse> {
    return this.http.get<ErrorDocResponse>(`${this.api}/${errorKey}`);
  }
}
