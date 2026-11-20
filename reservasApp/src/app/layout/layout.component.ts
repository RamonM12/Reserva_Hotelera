import { Component } from '@angular/core';
import { ThemeService } from '../Core/services/theme.service';
import { AuthService } from '../Core/services/auth.service';

interface MenuItem {
  label: string;
  icon: string;
  route: string;
  disponible: boolean;
}

@Component({
  selector: 'app-layout',
  standalone: false,
  templateUrl: './layout.component.html',
  styleUrl: './layout.component.css'
})
export class LayoutComponent {

    sidenavAbierto = true;
  isAdmin = false;
  username: string | null = null;

  menuItems: MenuItem[] = [
    { label: 'Dashboard', icon: 'dashboard', route: '/dashboard', disponible: true },
    { label: 'Reservaciones', icon: 'calendar_month', route: '/reservaciones', disponible: false },
    { label: 'Habitaciones', icon: 'bed', route: '/habitaciones', disponible: false },
    { label: 'Huéspedes', icon: 'people', route: '/huespedes', disponible: true },
    { label: 'Usuarios', icon: 'manage_accounts', route: '/usuarios', disponible: true }
  ];

    constructor(
    public themeService: ThemeService,
    private authService: AuthService
  ) {}

    ngOnInit(): void {
    this.isAdmin = this.authService.isAdmin();
    this.username = this.authService.getUsername();
  }

  toggleSidenav(): void {
    this.sidenavAbierto = !this.sidenavAbierto;
  }

  cerrarSesion(): void {
    this.authService.logout();
  }

}
