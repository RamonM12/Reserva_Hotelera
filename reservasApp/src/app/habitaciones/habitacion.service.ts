import { Injectable } from '@angular/core';
import { environment } from '../../environments/environment';
import { HttpClient } from '@angular/common/http';
import { HabitacionRequest, HabitacionResponse } from '../Core/models/habitacion.model';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class HabitacionService {

  private readonly baseUrl = `${environment.apiUrl}/api/habitaciones`;

  constructor(private http: HttpClient) { }

  listar(): Observable<HabitacionResponse[]> {
    return this.http.get<HabitacionResponse[]>(this.baseUrl);
  }

  obtenerPorId(id: number): Observable<HabitacionResponse> {
    return this.http.get<HabitacionResponse>(`${this.baseUrl}/${id}`);
  }

  registrar(request: HabitacionRequest): Observable<HabitacionResponse> {
    return this.http.post<HabitacionResponse>(this.baseUrl, request);
  }

  actualizar(id: number, request: HabitacionRequest): Observable<HabitacionResponse> {
    return this.http.put<HabitacionResponse>(`${this.baseUrl}/${id}`, request);
  }

  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }

  // PATCH del ADMIN (Notion 4.2)
  cambiarEstado(id: number, idEstado: number): Observable<HabitacionResponse> {
    return this.http.patch<HabitacionResponse>(`${this.baseUrl}/${id}/estado/${idEstado}`, null);
  }
}
