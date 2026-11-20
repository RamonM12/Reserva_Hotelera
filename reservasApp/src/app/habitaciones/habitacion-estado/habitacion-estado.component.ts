import { Component, Inject } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ESTADOS_HABITACION_CATALOGO, ESTADO_HABITACION_LABELS, HabitacionResponse } from '../../Core/models/habitacion.model';
import {HabitacionService} from '../habitacion.service';
import {mensajeHabitacion} from '../habitacion-error';

@Component({ selector: 'app-habitacion-estado', standalone: false,
  templateUrl: './habitacion-estado.component.html', styleUrl: './habitacion-estado.component.scss' })
export class HabitacionEstadoComponent {
  readonly estados;
  readonly estadoActual: string;
  form: FormGroup;
  guardando = false;

  constructor(fb: FormBuilder, private habitacionService: HabitacionService,
              private snackBar: MatSnackBar, private dialogRef: MatDialogRef<HabitacionEstadoComponent>,
              @Inject(MAT_DIALOG_DATA) public data: HabitacionResponse) {
    this.estadoActual = ESTADO_HABITACION_LABELS[data.estadoHabitacion];
    // Notion 4.2: nunca hacia OCUPADA ni al mismo estado; una OCUPADA no cambia
    this.estados = data.estadoHabitacion === 'OCUPADA' ? [] :
      ESTADOS_HABITACION_CATALOGO.filter(e => e.estado !== 'OCUPADA' && e.estado !== data.estadoHabitacion);
    this.form = fb.group({ idEstado: [null, Validators.required] });
  }

  guardar(): void {
    if (this.guardando) return;
    const idEstado = this.form.value.idEstado;
    if (this.form.invalid || !this.estados.some(e => e.id === idEstado)) { this.form.markAllAsTouched(); return; }
    this.guardando = true;
    this.habitacionService.cambiarEstado(this.data.id, idEstado).subscribe({
      next: () => { this.guardando = false; this.dialogRef.close(true); this.snackBar.open('Estado actualizado', 'Cerrar', { duration: 3000 }); },
      error: err => { this.guardando = false; this.snackBar.open(mensajeHabitacion(err), 'Cerrar', { duration: 3000 }); }
    });
  }

  cancelar(): void { this.dialogRef.close(false); }
}
