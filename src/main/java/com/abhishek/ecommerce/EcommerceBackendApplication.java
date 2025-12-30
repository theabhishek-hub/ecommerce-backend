package com.abhishek.ecommerce;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing(auditorAwareRef = "auditorAware")
@SpringBootApplication
public class EcommerceBackendApplication
{

	public static void main(String[] args)
	{
		SpringApplication.run(EcommerceBackendApplication.class, args);
	}

}
