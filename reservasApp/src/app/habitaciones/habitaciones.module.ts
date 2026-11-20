import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { HabitacionesRoutingModule } from './habitaciones-routing.module';
import { MaterialModule } from '../material/material.module';
import { HabitacionListComponent } from './habitacion-list/habitacion-list.component';
import { HabitacionFormComponent } from './habitacion-form/habitacion-form.component';

import { HabitacionEstadoComponent } from './habitacion-estado/habitacion-estado.component';

@NgModule({
  declarations: [
    HabitacionListComponent,
    HabitacionFormComponent,
    HabitacionEstadoComponent
  ],
  imports: [
    CommonModule,
    HabitacionesRoutingModule,
    MaterialModule,
    FormsModule,
    ReactiveFormsModule
  ]
})
export class HabitacionesModule { }
