package com.edwin.habitaciones;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.edwin.habitaciones", "com.ramon.commons"})
public class HabitacionesApplication {

	public static void main(String[] args) {
		SpringApplication.run(HabitacionesApplication.class, args);
	}

}
