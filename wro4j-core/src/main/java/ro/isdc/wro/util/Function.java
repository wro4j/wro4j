package ro.isdc.wro.util;

/**
 * Determines an output value based on an input value. Inspired from Guava (simulates functional programming style), the
 * only difference is that the function can throw an {@link Exception}.
 * 
 * @author Alex Objelean
 * @since 1.4.4
 */
public interface Function<F, T> {
  /**
   * Returns the result of applying this function to {@code input}. This method is <i>generally
   * expected</i>, but not absolutely required, to have the following properties:
   *
   * <ul>
   * <li>Its execution does not cause any observable side effects.
   * </ul>
   *
   * @throws NullPointerException if {@code input} is null and this function does not accept null
   *     arguments
   * @throws Exception if an exception occurred during applying the function.  
   */
  T apply(F input) throws Exception;
}

