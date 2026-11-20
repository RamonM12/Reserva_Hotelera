import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { EstadoReserva, ESTADO_RESERVA_ID, ReservaRequest, ReservaResponse } from '../Core/models/reserva.model';

@Injectable({
  providedIn: 'root'
})
export class ReservaService {

  // Pasa por el Gateway (8090) hacia el microservicio de reservas
  private readonly baseUrl = `${environment.apiUrl}/api/reservas`;

  constructor(private http: HttpClient) { }

  listar(): Observable<ReservaResponse[]> {
    return this.http.get<ReservaResponse[]>(this.baseUrl);
  }

  obtenerPorId(id: number): Observable<ReservaResponse> {
    return this.http.get<ReservaResponse>(`${this.baseUrl}/${id}`);
  }

  registrar(request: ReservaRequest): Observable<ReservaResponse> {
    return this.http.post<ReservaResponse>(this.baseUrl, request);
  }

  // Solo cambian fechas; el backend recalcula el precioTotal
  actualizar(id: number, request: ReservaRequest): Observable<ReservaResponse> {
    return this.http.put<ReservaResponse>(`${this.baseUrl}/${id}`, request);
  }

  // Check-in (EN_CURSO), check-out (FINALIZADA) o cancelación (CANCELADA)
  cambiarEstado(id: number, estado: EstadoReserva): Observable<ReservaResponse> {
    return this.http.patch<ReservaResponse>(`${this.baseUrl}/${id}/estado/${ESTADO_RESERVA_ID[estado]}`, {});
  }

  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
