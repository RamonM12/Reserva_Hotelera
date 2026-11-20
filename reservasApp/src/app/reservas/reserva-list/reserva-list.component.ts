import { Component, OnInit } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ESTADO_RESERVA_LABELS, EstadoReserva, ReservaResponse } from '../../Core/models/reserva.model';
import { AuthService } from '../../Core/services/auth.service';
import { ReservaService } from '../reserva.service';
import { ReservaFormComponent } from '../reserva-form/reserva-form.component';

@Component({
  selector: 'app-reserva-list',
  standalone: false,
  templateUrl: './reserva-list.component.html',
  styleUrl: './reserva-list.component.scss'
})
export class ReservaListComponent implements OnInit {

  columnas = ['huesped', 'habitacion', 'fechas', 'personas', 'total', 'estado', 'acciones'];
  reservas: ReservaResponse[] = [];
  cargando = false;
  readonly estadoLabels = ESTADO_RESERVA_LABELS;

  get isAdmin(): boolean { return this.authService.isAdmin(); }

  constructor(
    private reservaService: ReservaService,
    private dialog: MatDialog,
    private snackBar: MatSnackBar,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.buscar();
  }

  buscar(): void {
    this.cargando = true;
    this.reservaService.listar().subscribe({
      next: (data) => { this.reservas = data; this.cargando = false; },
      error: (err: HttpErrorResponse) => { this.cargando = false; this.manejarError(err); }
    });
  }

  abrirFormulario(reserva?: ReservaResponse): void {
    this.dialog.open(ReservaFormComponent, { width: '500px', data: reserva ?? null })
      .afterClosed().subscribe(guardado => { if (guardado) this.buscar(); });
  }

  // Botones según el estado (Notion 8.2). Transiciones: 1→2, 2→3, 1→4
  puedeEditar(r: ReservaResponse): boolean { return r.estado === 'CONFIRMADA' || r.estado === 'EN_CURSO'; }
  puedeCheckIn(r: ReservaResponse): boolean { return r.estado === 'CONFIRMADA'; }
  puedeCheckOut(r: ReservaResponse): boolean { return r.estado === 'EN_CURSO'; }
  puedeCancelar(r: ReservaResponse): boolean { return r.estado === 'CONFIRMADA'; }
  puedeEliminar(r: ReservaResponse): boolean {
    return this.isAdmin && (r.estado === 'FINALIZADA' || r.estado === 'CANCELADA');
  }

  checkIn(r: ReservaResponse): void {
    if (!confirm(`¿Hacer check-in de la reserva de ${r.huesped.nombre}?`)) return;
    this.cambiarEstado(r, 'EN_CURSO', 'Check-in realizado');
  }

  checkOut(r: ReservaResponse): void {
    if (!confirm(`¿Hacer check-out de la reserva de ${r.huesped.nombre}?`)) return;
    this.cambiarEstado(r, 'FINALIZADA', 'Check-out realizado');
  }

  cancelar(r: ReservaResponse): void {
    if (!confirm(`¿Cancelar la reserva de ${r.huesped.nombre}?`)) return;
    this.cambiarEstado(r, 'CANCELADA', 'Reserva cancelada');
  }

  eliminar(r: ReservaResponse): void {
    if (!this.puedeEliminar(r)) return;
    if (!confirm('¿Eliminar esta reserva?')) return;
    this.reservaService.eliminar(r.id).subscribe({
      next: () => { this.mostrarMensaje('Reserva eliminada'); this.buscar(); },
      error: (err: HttpErrorResponse) => this.manejarError(err)
    });
  }

  etiquetaEstado(estado: EstadoReserva): string {
    return this.estadoLabels[estado];
  }

  private cambiarEstado(r: ReservaResponse, estado: EstadoReserva, mensaje: string): void {
    this.reservaService.cambiarEstado(r.id, estado).subscribe({
      next: () => { this.mostrarMensaje(mensaje); this.buscar(); },
      error: (err: HttpErrorResponse) => this.manejarError(err)
    });
  }

  // Mensajes por código HTTP (Notion 8.3). 401 y 403 los maneja el JwtInterceptor
  private manejarError(err: HttpErrorResponse): void {
    if (err.status === 404 || err.status === 409) {
      this.mostrarMensaje(err.error?.detail ?? 'No se pudo completar la operación');
    } else if (err.status === 503) {
      this.mostrarMensaje('Un servicio no está disponible. Intenta más tarde');
    } else if (err.status !== 401 && err.status !== 403) {
      this.mostrarMensaje('Ocurrió un error inesperado');
    }
  }

  private mostrarMensaje(mensaje: string): void {
    this.snackBar.open(mensaje, 'Cerrar', { duration: 3500 });
  }
}
