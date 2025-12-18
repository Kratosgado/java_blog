
package com.kratosgado.blog.utils.interfaces;

import java.util.List;
import java.util.Optional;

public interface Repository<T> {

  /**
   * Adds the given entity to the database
   * 
   * @param model
   * @return the entity added to the database
   */
  public T add(T model);

  public T update(T model);

  /**
   * @return List of all entities in the database
   */
  public List<T> getAll();

  /**
   * Returns the entity with the given id
   * 
   * @param id
   * @return Optional containing the entity with the given id
   * @throws RuntimeException
   */
  public Optional<T> getById(String id) throws RuntimeException;

  /**
   * Removes the entity with the given id from the database
   * 
   * @param id
   * @return true if the entity was removed, false otherwise
   */
  public boolean removeById(String id);

  /**
   * Checks if the entity with the given id exists in the database
   * 
   * @param id
   * @return true if the entity exists, false otherwise
   */
  public boolean exists(String id);

  /**
   * @return number of entities in the database
   */
  public int count();

}
