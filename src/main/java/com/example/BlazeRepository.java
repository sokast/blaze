package com.example;

import com.example.model.Item;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;

/**
 * The Key Repository
 */
@ApplicationScoped
public class Repository implements PanacheRepositoryBase<Item, Integer> {

  public List<Item> findAllItems() {
    return findAll().list();
  }
}
