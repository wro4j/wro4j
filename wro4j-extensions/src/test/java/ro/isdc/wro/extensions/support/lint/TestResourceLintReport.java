package ro.isdc.wro.extensions.support.lint;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;


/**
 * @author Alex Objelean
 */
public class TestResourceLintReport {
  private ResourceLintReport<String> victim;

  @Before
  public void setUp() {
    victim = new ResourceLintReport<String>();
  }

  @Test
  public void shouldHaveNoLintsByDefault() {
    assertTrue(victim.getLints().isEmpty());
  }

  @Test(expected = NullPointerException.class)
  public void cannotSetNullLints() {
    victim.setLints(null);
  }

  @Test
  public void shouldHaveConfiguredLints() {
    victim.setLints(Arrays.asList("1", "2", "3"));
    assertEquals(3, victim.getLints().size());
  }

  @Test
  public void shouldHaveUnknownPathByDefault() {
    assertEquals(ResourceLintReport.UNKNOWN_PATH, victim.getResourcePath());
  }

  @Test
  public void shouldHaveUnknownPathWhenNullPathIsSet() {
    victim.setResourcePath(null);
    assertEquals(ResourceLintReport.UNKNOWN_PATH, victim.getResourcePath());
  }

  @Test
  public void shouldHaveCorrectPathWhenNotNullPathIsSet() {
    victim.setResourcePath("path");
    assertEquals("path", victim.getResourcePath());
  }

  @Test
  public void shouldCreateValidObjectWithFactoryMethod() {
    victim = ResourceLintReport.create("path", Arrays.asList("1", "2"));
    assertEquals("path", victim.getResourcePath());
    assertEquals(2, victim.getLints().size());
  }

  @Test
  public void shouldIgnoreNullLints() {
    final List<String> lints = new ArrayList<String>();
    lints.add(null);
    lints.add(null);
    final ResourceLintReport<String> report = ResourceLintReport.create("uri", lints);
    assertTrue(report.getLints().isEmpty());
  }
}
