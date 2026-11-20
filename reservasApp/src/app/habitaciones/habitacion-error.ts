import { HttpErrorResponse } from '@angular/common/http';

export function mensajeHabitacion(err: HttpErrorResponse): string {
  if (err.status === 0) return 'No se pudo conectar con el servidor.';
  if (err.status === 401) return 'Sesión expirada.';
  if (err.status === 403) return 'No tienes permisos para esta acción.';
  if (err.status === 404) return err.error?.detail ?? 'Habitación no encontrada.';
  if (err.status === 503) return err.error?.detail ?? 'Servicio no disponible. Intenta más tarde.';
  if (err.status === 500) return 'Error interno del servidor.';
  return err.error?.detail ?? 'No se pudo realizar la operación.';
}
