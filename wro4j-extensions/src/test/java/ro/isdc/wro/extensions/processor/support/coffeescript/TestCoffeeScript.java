package ro.isdc.wro.extensions.processor.support.coffeescript;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

/**
 * @author Alex Objelean
 */
public class TestCoffeeScript {
  private CoffeeScript victim;
  @Before
  public void setUp() {
    victim = new CoffeeScript();
  }

  @Test
  public void shouldCompileNullScript() {
    assertEquals("(function() {\n\n\n\n}).call(this);\n", victim.compile(null));
  }

  @Test
  public void shouldCompileSimpleAlert() {
    assertEquals("(function() {\n\n  alert('I knew it!');\n\n}).call(this);\n", victim.compile("alert 'I knew it!'"));
  }
}
