package ro.isdc.wro.util;

import java.util.Comparator;

/**
 * Classes implementing this interface signal to the classes that load them that order is significant.
 *
 * @author Moandji Ezana
 * @created 9 Jan 2013
 * @since 1.6.2
 */
public interface Ordered {
  public static final int HIGHEST = Integer.MAX_VALUE;
  public static final int MEDIUM = 0;
  public static final int LOWEST = Integer.MIN_VALUE;
  public static final Comparator<Object> DEFAULT_COMPARATOR = new Comparator<Object>() {
    /**
     * {@inheritDoc}
     */
    public int compare(final Object left, final Object right) {
      int priority1 = MEDIUM;
      int priority2 = MEDIUM;

      if (left instanceof Ordered) {
        final Ordered loadOrderAwareProvider = (Ordered) left;
        priority1 = loadOrderAwareProvider.getOrder();
      }

      if (right instanceof Ordered) {
        final Ordered loadOrderAwareProvider = (Ordered) right;
        priority2 = loadOrderAwareProvider.getOrder();
      }

      if (priority1 > priority2) {
        return 1;
      }

      if (priority1 < priority2) {
        return -1;
      }

      return 0;
    }
  };
  /**
   * @return a number representing the order.
   */
  int getOrder();
}
