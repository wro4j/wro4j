package ro.isdc.wro.util.provider;

import static org.junit.Assert.assertEquals;
import static ro.isdc.wro.util.provider.ProviderPriority.HIGH;
import static ro.isdc.wro.util.provider.ProviderPriority.LOW;
import static ro.isdc.wro.util.provider.ProviderPriority.MEDIUM;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

public class TestProviderPriority {

  @Test
  public void shouldSortFromLowToHigh() {
    List<ProviderPriority> priorities = Arrays.asList(HIGH, LOW, MEDIUM, MEDIUM, HIGH, LOW, MEDIUM, HIGH);
    
    Collections.sort(priorities);
    
    assertEquals(Arrays.asList(LOW, LOW, MEDIUM, MEDIUM, MEDIUM, HIGH, HIGH, HIGH), priorities);
  }
  
  @Test
  public void shouldCompareSamePriorityEqually() {
    assertEquals(0, LOW.compareTo(LOW));
    assertEquals(0, MEDIUM.compareTo(MEDIUM));
    assertEquals(0, HIGH.compareTo(HIGH));
  }
}
