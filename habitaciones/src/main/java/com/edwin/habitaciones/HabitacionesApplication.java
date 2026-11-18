package com.edwin.habitaciones;

import com.ramon.commons.clients.FeignConfig;
import com.ramon.commons.exceptions.GlobalExceptionHandler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@EnableFeignClients(basePackages = "com.ramon.commons.clients")
@Import({GlobalExceptionHandler.class, FeignConfig.class})
public class HabitacionesApplication {

	public static void main(String[] args) {
		SpringApplication.run(HabitacionesApplication.class, args);
	}

}
