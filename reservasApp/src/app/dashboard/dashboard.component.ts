import { Component, OnInit } from '@angular/core';
import { UsuarioService } from '../usuarios/usuario.service';
import { AuthService } from '../Core/services/auth.service';
import { ROLES } from '../Core/models/usuario.model';
import { ReservaService } from '../reservas/reserva.service';
import { HabitacionService } from '../habitaciones/habitacion.service';
import { HuespedService } from '../huespedes/huesped.service';

@Component({
  selector: 'app-dashboard',
  standalone: false,
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css'
})
export class DashboardComponent implements OnInit {
  totalUsuarios: number | null = null;
  reservasActivas: number | null = null;
  habitacionesDisponibles: number | null = null;
  totalHuespedes: number | null = null;
  isAdmin = false;

  constructor(
    private usuarioService: UsuarioService,
    private authService: AuthService,
    private reservaService: ReservaService,
    private habitacionService: HabitacionService,
    private huespedService: HuespedService
  ) {}

  ngOnInit(): void {
    this.isAdmin = this.authService.hasRole(ROLES[0]);
    if (this.isAdmin) {
      this.listarUsuarios();
    }

    this.contarReservasActivas();
    this.contarHabitacionesDisponibles();
    this.contarHuespedes();
  }

  listarUsuarios(): void {
    this.usuarioService.listar().subscribe({
      next: (data) => this.totalUsuarios = data.length,
      error: () => this.totalUsuarios = null
    });
  }

  // Activas = CONFIRMADA o EN_CURSO
  contarReservasActivas(): void {
    this.reservaService.listar().subscribe({
      next: (data) => this.reservasActivas =
        data.filter(r => r.estado === 'CONFIRMADA' || r.estado === 'EN_CURSO').length,
      error: () => this.reservasActivas = null
    });
  }

  contarHabitacionesDisponibles(): void {
    this.habitacionService.listar().subscribe({
      next: (data) => this.habitacionesDisponibles =
        data.filter(h => h.estadoHabitacion === 'DISPONIBLE').length,
      error: () => this.habitacionesDisponibles = null
    });
  }

  contarHuespedes(): void {
    this.huespedService.listar().subscribe({
      next: (data) => this.totalHuespedes = data.length,
      error: () => this.totalHuespedes = null
    });
  }
}
