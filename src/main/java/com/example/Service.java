package com.example;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@ApplicationScoped
@Slf4j
public class Service {

  @Inject
  Repository repository;
  
  @Inject
  BlazeRepository blazeRepository;
  
  @Transactional
  public void findAllWithPanache() {
    repository.findAllItems();
  }

  public void findAllWithBlaze() {
    blazeRepository.findAllItems();
  }
}
