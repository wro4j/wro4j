package ro.isdc.wro.util.provider;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import ro.isdc.wro.util.Ordered;


public class TestProviderPriority {

  private static Ordered HIGH = new Ordered() {
    public int getOrder() {
      return Ordered.HIGHEST;
    }
  };
  private static Ordered MEDIUM = new Ordered() {
    public int getOrder() {
      return 10;
    }
  };
  private static Ordered LOW = new Ordered() {
    public int getOrder() {
      return Ordered.LOWEST;
    }
  };
  
  @Test
  public void shouldSortFromLowToHigh() {
    List<Ordered> priorities = Arrays.asList(HIGH, LOW, MEDIUM, MEDIUM, HIGH, LOW, MEDIUM, HIGH);
    
    Collections.sort(priorities, Ordered.COMPARATOR);
    
    assertEquals(Arrays.asList(LOW, LOW, MEDIUM, MEDIUM, MEDIUM, HIGH, HIGH, HIGH), priorities);
  }
  
  @Test
  public void shouldCompareSamePriorityEqually() {
    assertEquals(0, Ordered.COMPARATOR.compare(LOW, LOW));
    assertEquals(0, Ordered.COMPARATOR.compare(MEDIUM, MEDIUM));
    assertEquals(0, Ordered.COMPARATOR.compare(HIGH, HIGH));
  }
}
