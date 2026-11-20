export const TIPOS_DOCUMENTO = [
  'INE',
  'LICENCIA',
  'PASAPORTE'
] as const;

export type TipoDocumento = typeof TIPOS_DOCUMENTO[number];

export const TIPO_DOCUMENTO_LABELS: Record<TipoDocumento, string> = {
  INE: 'INE',
  LICENCIA: 'Licencia',
  PASAPORTE: 'Pasaporte'
};

export const TIPOS_DOCUMENTO_CATALOGO: { id: TipoDocumento; label: string }[] = [
  { id: TIPOS_DOCUMENTO[0], label: TIPO_DOCUMENTO_LABELS[TIPOS_DOCUMENTO[0]] },
  { id: TIPOS_DOCUMENTO[1], label: TIPO_DOCUMENTO_LABELS[TIPOS_DOCUMENTO[1]] },
  { id: TIPOS_DOCUMENTO[2], label: TIPO_DOCUMENTO_LABELS[TIPOS_DOCUMENTO[2]] }
];


export interface HuespedRequest {
  nombre: string;
  apellido: string;
  email: string;
  telefono: string;
  tipoDocumento: TipoDocumento;
  numeroDocumento: string;
  nacionalidad: string;
}

export interface HuespedResponse {
  id: number;
  nombre: string;
  apellido: string;
  email: string;
  telefono: string;
  tipoDocumento: TipoDocumento;
  numeroDocumento: string;
  nacionalidad: string;
  estadoRegistro: string;
}
