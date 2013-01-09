package ro.isdc.wro.util;

import java.util.Comparator;

public interface Ordered {

  static final int HIGHEST = Integer.MAX_VALUE;
  static final int LOWEST = Integer.MIN_VALUE;
  int getOrder();
  public static final Comparator<Object> COMPARATOR = new Comparator<Object>() {

    public int compare(Object provider1, Object provider2) {
      int priority1 = LOWEST;
      int priority2 = LOWEST;
      
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
