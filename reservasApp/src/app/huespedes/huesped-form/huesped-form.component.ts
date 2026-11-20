import { Component, Inject, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { HuespedRequest, HuespedResponse, TIPOS_DOCUMENTO_CATALOGO } from '../../Core/models/huesped.model';
import { HuespedService } from '../huesped.service';

@Component({
  selector: 'app-huesped-form',
  standalone: false,
  templateUrl: './huesped-form.component.html',
  styleUrl: './huesped-form.component.scss'
})
export class HuespedFormComponent implements OnInit {

  guardando = false;
  esEdicion = false;
  form: FormGroup;
  readonly tiposDocumento = TIPOS_DOCUMENTO_CATALOGO;

  constructor(
    private fb: FormBuilder,
    private huespedService: HuespedService,
    private snackBar: MatSnackBar,
    private dialogRef: MatDialogRef<HuespedFormComponent>,
    @Inject(MAT_DIALOG_DATA) public data: HuespedResponse | null
  ) {
    // Mismas validaciones que el HuespedRequest del backend (Notion 4.3 y 5.3)
    this.form = this.fb.group({
      nombre: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(50)]],
      apellido: ['', [Validators.required, Validators.minLength(2), Validators.maxLength(50)]],
      email: ['', [Validators.required, Validators.email]],
      telefono: ['', [Validators.required, Validators.pattern(/^\d{10}$/)]],
      tipoDocumento: [null, [Validators.required]],
      numeroDocumento: ['', [Validators.required, Validators.minLength(8), Validators.maxLength(20)]],
      nacionalidad: ['', [Validators.required]]
    });
  }

  ngOnInit(): void {
    // Si llega un huésped, es edición: se precargan sus datos
    if (this.data) {
      this.esEdicion = true;
      this.form.patchValue(this.data);
    }
  }

  guardar(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.guardando = true;
    const request: HuespedRequest = this.form.getRawValue();

    const obs = this.esEdicion
      ? this.huespedService.actualizar(this.data!.id, request)
      : this.huespedService.registrar(request);

    obs.subscribe({
      next: () => {
        this.guardando = false;
        this.dialogRef.close(true);
        this.mostrarMensaje(`Huésped ${request.nombre} ${this.esEdicion ? 'actualizado' : 'registrado'}`);
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

  // Mensajes por código HTTP (Notion 8.3). 401 y 403 ya los maneja el JwtInterceptor
  private manejarError(err: HttpErrorResponse): void {
    if (err.status === 400) {
      // El backend manda "errores": ["campo: mensaje"] -> se pinta en su campo
      const errores: string[] = err.error?.errores ?? [];
      errores.forEach(texto => {
        const [campo, mensaje] = texto.split(': ');
        this.form.get(campo)?.setErrors({ servidor: mensaje });
      });
      this.mostrarMensaje('Revisa los datos del formulario');
    } else if (err.status === 409) {
      this.mostrarMensaje(err.error?.detail ?? 'Ya existe un huésped con esos datos');
    } else if (err.status === 503) {
      this.mostrarMensaje('El servicio no está disponible. Intenta más tarde');
    } else if (err.status !== 401 && err.status !== 403) {
      this.mostrarMensaje('Ocurrió un error inesperado');
    }
  }

  private mostrarMensaje(mensaje: string): void {
    this.snackBar.open(mensaje, 'Cerrar', { duration: 3000 });
  }
}
