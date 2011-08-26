package ro.isdc.wro.util;

/**
 * Transform some an object of some type to another object of the same type.
 *
 * @param <T> type of the object to transform.
 */
public interface Transformer<T> {
  T transform(T input) throws Exception;
}