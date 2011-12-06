/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.extensions.processor.support.linter;

import org.junit.Test;


/**
 * @author Alex Objelean
 */
public class TestJsLint {
  private final JsLint jsLint = new JsLint();

  @Test
  public void testSetNullOptions()
    throws Exception {
    jsLint.setOptions(null);
    jsLint.validate("");
  }


  @Test
  public void testWithNoOptions()
    throws Exception {
    jsLint.validate("");
  }


  @Test
  public void testWithSeveralOptions()
    throws Exception {
    jsLint.setOptions("1,2");
    jsLint.validate("");
  }


  @Test(expected = LinterException.class)
  public void testInvalidScript()
    throws Exception {
    jsLint.validate("al ert(1)");
  }


  @Test(expected = LinterException.class)
  public void testValidScript()
    throws Exception {
    jsLint.validate("$(function(){})(jQuery);");
  }


  @Test(expected = LinterException.class)
  public void testWithUndefOption()
    throws Exception {
    jsLint.setOptions("undef");
    jsLint.validate("$(function(){})(jQuery);");
  }
}
