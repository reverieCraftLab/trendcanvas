package com.reveriecraftlab.trendcanvas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class TrendcanvasApplication {

	public static void main(String[] args) {
		SpringApplication.run(TrendcanvasApplication.class, args);
	}

}
