package com.example.study3;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableTransactionManagement
@SpringBootApplication
public class Study3Application {

	public static void main(String[] args) {
		SpringApplication.run(Study3Application.class, args);
	}

}
