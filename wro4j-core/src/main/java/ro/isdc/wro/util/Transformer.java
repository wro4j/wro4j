package ro.isdc.wro.util;

/**
 * Transform some an object of some type to another object of the same type.
 *
 * @param <T> type of the object to transform.
 */
public interface Transformer<T> {
  /**
   * Apply a transformation on the input object.
   * 
   * @param input
   *          the object to transform.
   * @return the transformed object.
   * @throws Exception
   *           if an exception occured during transformation.
   */
  T transform(T input) throws Exception;
}