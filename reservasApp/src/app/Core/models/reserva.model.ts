import { HuespedResponse } from './huesped.model';
import { HabitacionResponse } from './habitacion.model';

// Catálogo EstadoReserva (Notion 4.1)
export const ESTADOS_RESERVA = [
  'CONFIRMADA',
  'EN_CURSO',
  'FINALIZADA',
  'CANCELADA'
] as const;

export type EstadoReserva = typeof ESTADOS_RESERVA[number];

export const ESTADO_RESERVA_LABELS: Record<EstadoReserva, string> = {
  CONFIRMADA: 'Confirmada',
  EN_CURSO: 'En curso',
  FINALIZADA: 'Finalizada',
  CANCELADA: 'Cancelada'
};

// Código que espera el backend en PATCH /api/reservas/{id}/estado/{idEstado}
export const ESTADO_RESERVA_ID: Record<EstadoReserva, number> = {
  CONFIRMADA: 1,
  EN_CURSO: 2,
  FINALIZADA: 3,
  CANCELADA: 4
};

// Lo que se ENVÍA al backend (ReservaRequest de commons). Fechas yyyy-MM-dd
export interface ReservaRequest {
  idHuesped: number;
  idHabitacion: number;
  fechaEntrada: string;
  fechaSalida: string;
  numeroPersonas: number;
}

// Lo que REGRESA el backend (ReservaResponse de commons)
export interface ReservaResponse {
  id: number;
  huesped: HuespedResponse;
  habitacion: HabitacionResponse;
  fechaEntrada: string;
  fechaSalida: string;
  numeroPersonas: number;
  precioNoche: number;
  precioTotal: number;
  estado: EstadoReserva;
  estadoRegistro: string;
}
