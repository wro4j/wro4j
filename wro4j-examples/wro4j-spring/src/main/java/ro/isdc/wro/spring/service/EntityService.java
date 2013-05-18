package ro.isdc.wro.spring.service;

import org.springframework.stereotype.Service;

import ro.isdc.wro.spring.model.MyEntity;


@Service
public class EntityService {

  // @Transactional(readOnly = true)
  public MyEntity findEntity(final String id) {
    return new MyEntity();
  }

  // @Transactional
  public MyEntity save(final MyEntity entity) {
    return entity;
  }

  public boolean delete(final String entityId) {
    return true;
  }

}
