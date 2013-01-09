package ro.isdc.wro.util;

import java.util.Comparator;

/**
 * Classes implementing this interface signal to the classes that load them that order is significant.
 *  
 * @author Moandji Ezana
 *
 */
public interface Ordered {

  int getOrder();

  static final int HIGHEST = Integer.MAX_VALUE;
  static final int MEDIUM = 0;
  static final int LOWEST = Integer.MIN_VALUE;
  
  public static final Comparator<Object> COMPARATOR = new Comparator<Object>() {

    public int compare(Object provider1, Object provider2) {
      int priority1 = MEDIUM;
      int priority2 = MEDIUM;
      
      if (provider1 instanceof Ordered) {
        Ordered loadOrderAwareProvider = (Ordered) provider1;
        priority1 = loadOrderAwareProvider.getOrder();
      }
      
      if (provider2 instanceof Ordered) {
        Ordered loadOrderAwareProvider = (Ordered) provider2;
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

}
