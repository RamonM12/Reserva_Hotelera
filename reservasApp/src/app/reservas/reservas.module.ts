import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { ReservasRoutingModule } from './reservas-routing.module';
import { ReservaListComponent } from './reserva-list/reserva-list.component';
import { ReservaFormComponent } from './reserva-form/reserva-form.component';
import { MaterialModule } from '../material/material.module';

@NgModule({
  declarations: [
    ReservaListComponent,
    ReservaFormComponent
  ],
  imports: [
    CommonModule,
    ReservasRoutingModule,
    MaterialModule,
    FormsModule,
    ReactiveFormsModule
  ]
})
export class ReservasModule { }
