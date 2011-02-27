/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.extensions.processor.algorithm.jshint;

import org.junit.Before;
import org.junit.Test;


/**
 * @author Alex Objelean
 */
public class TestJsHint {
  private JsHint jsHint;


  @Before
  public void setUp() {
    jsHint = new JsHint();
  }


  @Test(expected=IllegalArgumentException.class)
  public void cannotSetNullOptions() throws Exception {
    jsHint.setOptions((String[])null);
    jsHint.validate("");
  }

  @Test
  public void testWithNoOptions() throws Exception {
    jsHint.validate("");
  }

  @Test
  public void testWithSeveralOptions() throws Exception {
    jsHint.setOptions("1","2");
    jsHint.validate("");
  }

  @Test(expected=JsHintException.class)
  public void testInvalidScript() throws Exception {
    jsHint.validate("al ert(1)");
  }

  @Test
  public void testValidScript() throws Exception {
    jsHint.validate("$(function(){})(jQuery);");
  }

  @Test(expected=JsHintException.class)
  public void testWithUndefOption() throws Exception {
    jsHint.setOptions("undef");
    jsHint.validate("$(function(){})(jQuery);");
  }
}
