import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { guestGuard } from './Core/guards/guest.guard';
import { LayoutComponent } from './layout/layout.component';
import { authGuard } from './Core/guards/auth.guard';
import { DashboardComponent } from './dashboard/dashboard.component';
import { roleGuard } from './Core/guards/rol.guard';
import { ROLES } from './Core/models/usuario.model';


const routes: Routes = [
  {
    path: 'auth',
    canActivate: [guestGuard],
    loadChildren: () => import('./auth/auth.module').then(m => m.AuthModule)
  },
  {
    path: '',
    component: LayoutComponent,
    canActivate: [authGuard],
    children: [
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
      { path: 'dashboard', component: DashboardComponent },
      {
        path: 'usuarios',
        canActivate: [roleGuard],
        data: { roles: [ROLES[0]]},
        loadChildren: () => import('./usuarios/usuarios.module').then(m => m.UsuariosModule)
      },
      {
        path: 'huespedes',
        canActivate: [roleGuard],
        data: {roles: [ROLES[0], ROLES[1]]},
        loadChildren: () => import('./huespedes/huespedes.module').then(m => m.HuespedesModule)
      }
    ]
  },
  { path: '**', redirectTo: 'auth/login' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
