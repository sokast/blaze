package com.example;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.annotations.QuarkusMain;

/**
 * Lexicon Service main application start interface.
 */
@QuarkusMain
public class ServiceApp {

  public static void main(String... args) {
    Quarkus.run(ServiceRunner.class, args);
  }
}
