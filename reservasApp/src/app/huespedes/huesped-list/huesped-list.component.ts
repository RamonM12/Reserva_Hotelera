import { Component, OnInit } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { HuespedResponse, TIPO_DOCUMENTO_LABELS, TipoDocumento } from '../../Core/models/huesped.model';
import { AuthService } from '../../Core/services/auth.service';
import { HuespedFormComponent } from '../huesped-form/huesped-form.component';
import { HuespedService } from '../huesped.service';

@Component({
  selector: 'app-huesped-list',
  standalone: false,
  templateUrl: './huesped-list.component.html',
  styleUrl: './huesped-list.component.scss'
})
export class HuespedListComponent implements OnInit {

  columnas = ['nombre', 'email', 'telefono', 'documento', 'nacionalidad', 'acciones'];
  huespedes: HuespedResponse[] = [];
  cargando = false;
  isAdmin = false;

  readonly tipoDocumentoLabels = TIPO_DOCUMENTO_LABELS;

  constructor(
    private huespedService: HuespedService,
    private authService: AuthService,
    private dialog: MatDialog,
    private snackBar: MatSnackBar
  ) { }

  ngOnInit(): void {
    // Solo el ADMIN puede eliminar huéspedes (Notion 3)
    this.isAdmin = this.authService.isAdmin();
    this.buscar();
  }

  getTipoDocumentoLabel(tipo: TipoDocumento): string { return this.tipoDocumentoLabels[tipo]; }

  buscar(): void {
    this.cargando = true;
    this.huespedService.listar().subscribe({
      next: (data) => {
        this.huespedes = data;
        this.cargando = false;
      },
      error: (err: HttpErrorResponse) => {
        this.cargando = false;
        if (err.status === 503) {
          this.mostrarMensaje('El servicio de huéspedes no está disponible. Intenta más tarde');
        } else if (err.status !== 401 && err.status !== 403) {
          this.mostrarMensaje('Error al cargar la lista de huéspedes');
        }
      }
    });
  }

  abrirFormulario(huesped?: HuespedResponse): void {
    const ref = this.dialog.open(HuespedFormComponent, {
      width: '500px',
      data: huesped ?? null
    });

    ref.afterClosed().subscribe((guardado) => {
      if (guardado) this.buscar();
    });
  }

  eliminar(huesped: HuespedResponse): void {
    if (!confirm(`¿Eliminar al huésped "${huesped.nombre} ${huesped.apellido}"?`)) return;

    this.huespedService.eliminar(huesped.id).subscribe({
      next: () => {
        this.mostrarMensaje('Huésped eliminado correctamente');
        this.buscar();
      },
      error: (err: HttpErrorResponse) => {
        if (err.status === 409) {
          // Tiene reservas CONFIRMADA o EN_CURSO (Notion 6.2)
          this.mostrarMensaje(err.error?.detail ?? 'No se puede eliminar el huésped');
        } else if (err.status === 404) {
          this.mostrarMensaje('El huésped no existe');
        } else if (err.status === 503) {
          this.mostrarMensaje('El servicio no está disponible. Intenta más tarde');
        } else if (err.status !== 401 && err.status !== 403) {
          this.mostrarMensaje('Error al eliminar el huésped');
        }
      }
    });
  }

  private mostrarMensaje(mensaje: string): void {
    this.snackBar.open(mensaje, 'Cerrar', { duration: 3000 });
  }
}
