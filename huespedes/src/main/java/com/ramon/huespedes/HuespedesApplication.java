package com.ramon.huespedes;

import com.ramon.commons.clients.FeignConfig;
import com.ramon.commons.exceptions.GlobalExceptionHandler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@EnableFeignClients(basePackages = "com.ramon.commons.clients")
@Import({GlobalExceptionHandler.class, FeignConfig.class})
public class HuespedesApplication {

	public static void main(String[] args) {
		SpringApplication.run(HuespedesApplication.class, args);
	}

}
