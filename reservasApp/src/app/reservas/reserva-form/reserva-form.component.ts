import { Component, Inject, OnInit } from '@angular/core';
import { AbstractControl, FormBuilder, FormGroup, ValidationErrors, ValidatorFn, Validators } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ReservaRequest, ReservaResponse } from '../../Core/models/reserva.model';
import { HuespedResponse } from '../../Core/models/huesped.model';
import { HabitacionResponse, TIPO_HABITACION_LABELS } from '../../Core/models/habitacion.model';
import { ReservaService } from '../reserva.service';
import { HuespedService } from '../../huespedes/huesped.service';
import {HabitacionService} from '../../habitaciones/habitacion.service';

// Siempre debe cumplirse fechaEntrada < fechaSalida (Notion 6.4)
const fechasValidas: ValidatorFn = (group: AbstractControl): ValidationErrors | null => {
  const entrada = group.get('fechaEntrada')?.value;
  const salida = group.get('fechaSalida')?.value;
  return entrada && salida && entrada >= salida ? { fechasInvalidas: true } : null;
};

@Component({
  selector: 'app-reserva-form',
  standalone: false,
  templateUrl: './reserva-form.component.html',
  styleUrl: './reserva-form.component.scss'
})
export class ReservaFormComponent implements OnInit {

  guardando = false;
  esEdicion = false;
  form: FormGroup;

  huespedes: HuespedResponse[] = [];
  habitaciones: HabitacionResponse[] = [];
  readonly tipoHabitacionLabels = TIPO_HABITACION_LABELS;

  constructor(
    private fb: FormBuilder,
    private reservaService: ReservaService,
    private huespedService: HuespedService,
    private habitacionService: HabitacionService,
    private snackBar: MatSnackBar,
    private dialogRef: MatDialogRef<ReservaFormComponent>,
    @Inject(MAT_DIALOG_DATA) public data: ReservaResponse | null
  ) {
    this.form = this.fb.group({
      idHuesped: [null, [Validators.required]],
      idHabitacion: [null, [Validators.required]],
      fechaEntrada: ['', [Validators.required]],
      fechaSalida: ['', [Validators.required]],
      numeroPersonas: [1, [Validators.required, Validators.min(1)]]
    }, { validators: fechasValidas });
  }

  ngOnInit(): void {
    if (this.data) {
      // EDICIÓN: solo fechas (Notion 6.4)
      this.esEdicion = true;
      this.form.patchValue({
        idHuesped: this.data.huesped.id,
        idHabitacion: this.data.habitacion.id,
        fechaEntrada: this.data.fechaEntrada,
        fechaSalida: this.data.fechaSalida,
        numeroPersonas: this.data.numeroPersonas
      });
      this.form.get('idHuesped')?.disable();
      this.form.get('idHabitacion')?.disable();
      this.form.get('numeroPersonas')?.disable();

      // Después del check-in solo se puede cambiar la fecha de salida
      if (this.data.estado === 'EN_CURSO') {
        this.form.get('fechaEntrada')?.disable();
      }
    } else {
      this.cargarCatalogos();
    }
  }

  // Llena los combos: huéspedes activos y solo habitaciones DISPONIBLES
  private cargarCatalogos(): void {
    this.huespedService.listar().subscribe({
      next: (data) => this.huespedes = data,
      error: () => this.mostrarMensaje('No se pudieron cargar los huéspedes')
    });
    this.habitacionService.listar().subscribe({
      next: (data) => this.habitaciones = data.filter(h => h.estadoHabitacion === 'DISPONIBLE'),
      error: () => this.mostrarMensaje('No se pudieron cargar las habitaciones')
    });
  }

  // Capacidad de la habitación elegida, para mostrarla como ayuda
  get capacidadSeleccionada(): number | null {
    const id = this.form.get('idHabitacion')?.value;
    return this.habitaciones.find(h => h.id === id)?.capacidad ?? null;
  }

  guardar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.guardando = true;
    // getRawValue incluye también los campos deshabilitados
    const request: ReservaRequest = this.form.getRawValue();

    const obs = this.esEdicion
      ? this.reservaService.actualizar(this.data!.id, request)
      : this.reservaService.registrar(request);

    obs.subscribe({
      next: () => {
        this.guardando = false;
        this.dialogRef.close(true);
        this.mostrarMensaje(`Reserva ${this.esEdicion ? 'actualizada' : 'registrada'}`);
      },
      error: (err: HttpErrorResponse) => {
        this.guardando = false;
        this.manejarError(err);
      }
    });
  }

  cancelar(): void {
    this.dialogRef.close(false);
  }

  // Mensajes por código HTTP (Notion 8.3). 401 y 403 los maneja el JwtInterceptor
  private manejarError(err: HttpErrorResponse): void {
    if (err.status === 400) {
      const errores: string[] = err.error?.errores ?? [];
      errores.forEach(texto => {
        const [campo, mensaje] = texto.split(': ');
        this.form.get(campo)?.setErrors({ servidor: mensaje });
      });
      this.mostrarMensaje(err.error?.detail ?? 'Revisa los datos del formulario');
    } else if (err.status === 404 || err.status === 409) {
      // 404: huésped o habitación inexistente. 409: regla de negocio (capacidad, no disponible, etc.)
      this.mostrarMensaje(err.error?.detail ?? 'No se pudo guardar la reserva');
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
