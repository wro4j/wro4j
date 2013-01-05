package ro.isdc.wro.util.provider;

import java.util.Comparator;

public enum ProviderPriority {

  LOW, MEDIUM, HIGH;
  
  public static final Comparator<Object> COMPARATOR = new Comparator<Object>() {

    public int compare(Object provider1, Object provider2) {
      ProviderPriority priority1 = ProviderPriority.LOW;
      ProviderPriority priority2 = ProviderPriority.LOW;
      
      if (provider1 instanceof ProviderPriorityAware) {
        ProviderPriorityAware loadOrderAwareProvider = (ProviderPriorityAware) provider1;
        priority1 = loadOrderAwareProvider.getPriority();
      }
      
      if (provider2 instanceof ProviderPriorityAware) {
        ProviderPriorityAware loadOrderAwareProvider = (ProviderPriorityAware) provider2;
        priority2 = loadOrderAwareProvider.getPriority();
      }
      
      return priority1.compareTo(priority2);
    }
  };
}
