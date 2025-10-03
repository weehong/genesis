package com.resetrix.genesis;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@EnableAspectJAutoProxy
public class GenesisApplication {

  public static void main(String[] args) {
    SpringApplication.run(GenesisApplication.class, args);
  }

}
