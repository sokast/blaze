package com.example;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;

/**
 * An implementation of the QuarkusApplication which allows performing logic on startup
 */
@Slf4j
public class ServiceRunner implements QuarkusApplication {

  @Inject
  Service service;

  @Override
  public int run(String... args) {
    find();
    Quarkus.waitForExit();
    return 0;
  }

  /**
   * Updates all available translations for the system. Method processes yaml files for each
   * language and creates/ updates all necessary information.
   */
  public void find() {
    service.findAllWithPanache();
    service.findAllWithBlaze();
  }
}
