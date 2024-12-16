package com.wolfcode.MikrotikNetwork;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MikrotikHotspotBillingSystem {

	public static void main(String[] args) {
		SpringApplication.run(MikrotikHotspotBillingSystem.class, args);
	}

}
