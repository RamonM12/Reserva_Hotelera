import { Component, Inject, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, AbstractControl, ValidationErrors } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ESTADO_HABITACION_LABELS, HabitacionRequest, HabitacionResponse, TIPOS_HABITACION_CATALOGO } from '../../Core/models/habitacion.model';
import {HabitacionService} from '../habitacion.service';
import {mensajeHabitacion} from '../habitacion-error';

function enteroPositivo(control: AbstractControl): ValidationErrors | null {
  return Number.isSafeInteger(control.value) && control.value > 0 ? null : { enteroPositivo: true };
}
function precioPositivo(control: AbstractControl): ValidationErrors | null {
  return Number.isFinite(control.value) && control.value > 0 ? null : { precioPositivo: true };
}

@Component({ selector: 'app-habitacion-form', standalone: false,
  templateUrl: './habitacion-form.component.html', styleUrl: './habitacion-form.component.scss' })
export class HabitacionFormComponent implements OnInit {
  form: FormGroup;
  guardando = false;
  readonly tipos = TIPOS_HABITACION_CATALOGO;
  readonly estadoLabels = ESTADO_HABITACION_LABELS;
  get esEdicion(): boolean { return !!this.data.habitacion; }

  constructor(private fb: FormBuilder, private habitacionService: HabitacionService,
              private snackBar: MatSnackBar, private dialogRef: MatDialogRef<HabitacionFormComponent>,
              @Inject(MAT_DIALOG_DATA) public data: { habitacion: HabitacionResponse | null; soloLectura: boolean }) {
    // Mismas validaciones que el HabitacionRequest del backend (Notion 4.3)
    this.form = this.fb.group({
      numero: [null, [Validators.required, enteroPositivo]],
      tipoHabitacion: [null, [Validators.required]],
      precio: [null, [Validators.required, precioPositivo]],
      capacidad: [null, [Validators.required, enteroPositivo]]
    });
  }

  ngOnInit(): void {
    if (this.data.habitacion) this.form.patchValue(this.data.habitacion);
    if (this.data.soloLectura) this.form.disable();
  }

  guardar(): void {
    if (this.data.soloLectura || this.guardando) return;
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.guardando = true;
    const request: HabitacionRequest = this.form.getRawValue();
    const obs = this.esEdicion ? this.habitacionService.actualizar(this.data.habitacion!.id, request) : this.habitacionService.registrar(request);
    obs.subscribe({
      next: () => { this.guardando = false; this.dialogRef.close(true); this.mostrarMensaje('Habitación guardada correctamente'); },
      error: err => {
        this.guardando = false;
        if (err.status === 400 && Array.isArray(err.error?.errores)) {
          for (const error of err.error.errores) {
            if (typeof error !== 'string') continue;
            const i = error.indexOf(':');
            if (i < 0) continue;
            const control = this.form.get(error.slice(0, i).trim());
            if (control) { control.setErrors({ ...control.errors, servidor: error.slice(i + 1).trim() }); control.markAsTouched(); }
          }
        }
        this.mostrarMensaje(mensajeHabitacion(err));
      }
    });
  }

  cancelar(): void { this.dialogRef.close(false); }
  private mostrarMensaje(mensaje: string): void { this.snackBar.open(mensaje, 'Cerrar', { duration: 3000 }); }
}
