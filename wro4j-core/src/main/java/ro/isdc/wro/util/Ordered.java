package ro.isdc.wro.util;

import java.util.Comparator;


/**
 * Classes implementing this interface signal to the classes that load them that order is significant.
 *
 * @author Moandji Ezana
 * @since 1.6.2
 */
public interface Ordered {
  /**
   * The highest order, meaning that the value will be the last when ascending comparator is used.
   */
  public static final int HIGHEST = Integer.MAX_VALUE;
  public static final int MEDIUM = 0;
  /**
   * The highest order, meaning that the value will be the first when ascending comparator is used.
   */
  public static final int LOWEST = Integer.MIN_VALUE;
  /**
   * Sort elements from {@link #HIGHEST} to {@link #LOWEST}
   */
  public static final Comparator<Object> DESCENDING_COMPARATOR = new Comparator<Object>() {
	@Override
    public int compare(final Object left, final Object right) {
      int priorityLeft = MEDIUM;
      int priorityRight = MEDIUM;

      if (left instanceof Ordered) {
        final Ordered orderedLeft = (Ordered) left;
        priorityLeft = orderedLeft.getOrder();
      }

      if (right instanceof Ordered) {
        final Ordered orderedRight = (Ordered) right;
        priorityRight = orderedRight.getOrder();
      }
      if (priorityLeft == priorityRight) {
        return 0;
      }
      return priorityLeft > priorityRight ? -1 : 1;
    }
  };

  /**
   * @return a number representing the order.
   */
  int getOrder();
}
