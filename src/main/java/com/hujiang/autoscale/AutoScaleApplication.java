package com.hujiang.autoscale;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableAutoConfiguration
public class AutoScaleApplication {

	public static void main(String[] args) {
		SpringApplication.run(AutoScaleApplication.class, args);
	}
}
