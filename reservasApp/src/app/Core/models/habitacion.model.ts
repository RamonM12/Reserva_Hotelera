// Catálogo TipoHabitacion (mismo enum del backend)
export const TIPOS_HABITACION = [
  'SENCILLA',
  'DOBLE',
  'SUITE'
] as const;

export type TipoHabitacion = typeof TIPOS_HABITACION[number];

export const TIPO_HABITACION_LABELS: Record<TipoHabitacion, string> = {
  SENCILLA: 'Sencilla',
  DOBLE: 'Doble',
  SUITE: 'Suite'
};

// Catálogo EstadoHabitacion (Notion 4.1)
export const ESTADOS_HABITACION = [
  'DISPONIBLE',
  'OCUPADA',
  'LIMPIEZA',
  'MANTENIMIENTO'
] as const;

export type EstadoHabitacion = typeof ESTADOS_HABITACION[number];

export const ESTADO_HABITACION_LABELS: Record<EstadoHabitacion, string> = {
  DISPONIBLE: 'Disponible',
  OCUPADA: 'Ocupada',
  LIMPIEZA: 'Limpieza',
  MANTENIMIENTO: 'Mantenimiento'
};

// Lo que REGRESA el backend (HabitacionResponse de commons)
export interface HabitacionResponse {
  id: number;
  numero: number;
  precio: number;
  capacidad: number;
  estadoHabitacion: EstadoHabitacion;
  estadoRegistro: string;
  tipoHabitacion: TipoHabitacion;
}

// Catálogo para el combo del tipo de habitación
export const TIPOS_HABITACION_CATALOGO: { id: TipoHabitacion; label: string }[] =
  TIPOS_HABITACION.map(tipo => ({ id: tipo, label: TIPO_HABITACION_LABELS[tipo] }));

// Código que espera el backend en PATCH /api/habitaciones/{id}/estado/{idEstado}
export const ESTADO_HABITACION_ID: Record<EstadoHabitacion, number> = {
  DISPONIBLE: 1,
  OCUPADA: 2,
  LIMPIEZA: 3,
  MANTENIMIENTO: 4
};

// Catálogo para el diálogo de cambiar estado
export const ESTADOS_HABITACION_CATALOGO: { id: number; estado: EstadoHabitacion; label: string }[] =
  ESTADOS_HABITACION.map(estado => ({
    id: ESTADO_HABITACION_ID[estado],
    estado,
    label: ESTADO_HABITACION_LABELS[estado]
  }));

// Lo que se ENVÍA al backend (HabitacionRequest de commons). El estado NO va: solo se cambia con PATCH
export interface HabitacionRequest {
  numero: number;
  precio: number;
  capacidad: number;
  tipoHabitacion: TipoHabitacion;
}
