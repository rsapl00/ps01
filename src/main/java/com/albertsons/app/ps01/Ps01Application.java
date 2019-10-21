package com.albertsons.app.ps01;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
@EnableAsync
public class Ps01Application {

	public static void main(String[] args) {
		SpringApplication.run(Ps01Application.class, args);
	}

}
