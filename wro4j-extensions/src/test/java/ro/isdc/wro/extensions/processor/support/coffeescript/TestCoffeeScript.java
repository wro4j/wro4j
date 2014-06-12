package ro.isdc.wro.extensions.processor.support.coffeescript;

import static org.junit.Assert.assertEquals;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import ro.isdc.wro.config.Context;


/**
 * @author Alex Objelean
 */
public class TestCoffeeScript {
  private CoffeeScript victim;

  @BeforeClass
  public static void onBeforeClass() {
    assertEquals(0, Context.countActive());
  }

  @AfterClass
  public static void onAfterClass() {
    assertEquals(0, Context.countActive());
  }

  @Before
  public void setUp() {
    victim = new CoffeeScript();
  }

  @Test
  public void shouldCompileNullScript() {
    assertEquals("(function() {\n\n\n}).call(this);\n", victim.compile(null));
  }

  @Test
  public void shouldCompileSimpleAlert() {
    assertEquals("(function() {\n  alert('I knew it!');\n\n}).call(this);\n", victim.compile("alert 'I knew it!'"));
  }
}
