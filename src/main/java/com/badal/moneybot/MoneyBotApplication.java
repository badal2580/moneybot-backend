package com.badal.moneybot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MoneyBotApplication {

    public static void main(String[] args) {
        SpringApplication.run(MoneyBotApplication.class, args);
    }

}
