package com.example;

import com.blazebit.persistence.CriteriaBuilderFactory;
import com.example.model.Item;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;

@ApplicationScoped
public class BlazeRepository {

  @Inject
  EntityManager em;

  @Inject
  CriteriaBuilderFactory cbf;

  public List<Item> findAllItems() {
    return cbf.create(em, Item.class).getResultList();
  }
}
