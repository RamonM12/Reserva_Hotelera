import { Component, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ESTADO_HABITACION_LABELS, EstadoHabitacion, HabitacionResponse, TIPO_HABITACION_LABELS, TipoHabitacion } from '../../Core/models/habitacion.model';
import { AuthService } from '../../Core/services/auth.service';
import { HabitacionFormComponent } from '../habitacion-form/habitacion-form.component';
import { HabitacionEstadoComponent } from '../habitacion-estado/habitacion-estado.component';
import {HabitacionService} from '../habitacion.service';
import {mensajeHabitacion} from '../habitacion-error';

@Component({ selector: 'app-habitacion-list', standalone: false,
  templateUrl: './habitacion-list.component.html', styleUrl: './habitacion-list.component.scss' })
export class HabitacionListComponent implements OnInit {
  columnas = ['numero', 'tipo', 'precio', 'capacidad', 'estado', 'acciones'];
  habitaciones: HabitacionResponse[] = [];
  cargando = false;
  readonly tipoLabels = TIPO_HABITACION_LABELS;
  readonly estadoLabels = ESTADO_HABITACION_LABELS;
  get isAdmin(): boolean { return this.authService.isAdmin(); }

  constructor(private habitacionService: HabitacionService, private dialog: MatDialog,
              private snackBar: MatSnackBar, private authService: AuthService) {}

  ngOnInit(): void { this.buscar(); }

  etiquetaTipo(tipo: TipoHabitacion): string { return this.tipoLabels[tipo]; }
  etiquetaEstado(estado: EstadoHabitacion): string { return this.estadoLabels[estado]; }

  buscar(): void {
    this.cargando = true;
    this.habitacionService.listar().subscribe({
      next: data => { this.habitaciones = data; this.cargando = false; },
      error: err => { this.cargando = false; this.mostrarMensaje(mensajeHabitacion(err)); }
    });
  }

  consultar(habitacion: HabitacionResponse): void {
    this.habitacionService.obtenerPorId(habitacion.id).subscribe({
      next: data => this.dialog.open(HabitacionFormComponent, { width: '450px', data: { habitacion: data, soloLectura: true } }),
      error: err => this.mostrarMensaje(mensajeHabitacion(err))
    });
  }

  abrirFormulario(habitacion?: HabitacionResponse): void {
    if (!this.isAdmin) return;
    this.dialog.open(HabitacionFormComponent, { width: '450px', data: { habitacion: habitacion ?? null, soloLectura: false } })
      .afterClosed().subscribe(guardado => { if (guardado) this.buscar(); });
  }

  cambiarEstado(habitacion: HabitacionResponse): void {
    if (!this.isAdmin || habitacion.estadoHabitacion === 'OCUPADA') return;
    this.dialog.open(HabitacionEstadoComponent, { width: '450px', data: habitacion })
      .afterClosed().subscribe(guardado => { if (guardado) this.buscar(); });
  }

  eliminar(habitacion: HabitacionResponse): void {
    if (!this.isAdmin || habitacion.estadoHabitacion === 'OCUPADA') return;
    if (!confirm(`¿Eliminar la habitación ${habitacion.numero}?`)) return;
    this.habitacionService.eliminar(habitacion.id).subscribe({
      next: () => { this.mostrarMensaje('Habitación eliminada correctamente'); this.buscar(); },
      error: err => this.mostrarMensaje(mensajeHabitacion(err))
    });
  }

  private mostrarMensaje(mensaje: string): void { this.snackBar.open(mensaje, 'Cerrar', { duration: 3000 }); }
}
