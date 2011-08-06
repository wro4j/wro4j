/**
 * Copyright wro4j@2011
 */
package ro.isdc.wro.extensions.processor.algorithm.csslint;

import org.junit.Test;


/**
 * @author Alex Objelean
 */
public class TestCssLint {
  private CssLint jsHint = new CssLint();

  @Test
  public void testSetNullOptions()
    throws Exception {
    jsHint.setOptions((String[])null);
    jsHint.validate("");
  }


  @Test
  public void testWithNoOptions()
    throws Exception {
    jsHint.validate("");
  }


  @Test
  public void testWithSeveralOptions()
    throws Exception {
    jsHint.setOptions("1", "2");
    jsHint.validate("");
  }


  @Test(expected = CssLintException.class)
  public void testStyleWithWarning()
    throws Exception {
    jsHint.setOptions("box-model");
    jsHint.validate(".foo { width: 100px; padding: 10px; }");
  }


  @Test
  public void testValidStyle()
    throws Exception {
    jsHint.validate("h1 { left: 0; }");
  }


  @Test(expected=CssLintException.class)
  public void testStyleContainingAScript()
    throws Exception {
    jsHint.validate("$(function(){})(jQuery);");
  }

  @Test(expected=CssLintException.class)
  public void testDuplicateHeading() throws Exception {
    jsHint.setOptions("unique-headings");
    jsHint.validate("h1 { color: red;} h1 {color: blue;}");
  }

  @Test(expected=CssLintException.class)
  public void testRegexSelectors() throws Exception {
    jsHint.setOptions("regex-selectors");
    jsHint.validate("li[class*=foo]{ color: red; }");
  }

  @Test(expected=CssLintException.class)
  public void testOperaGradient() throws Exception {
    jsHint.setOptions("gradients");
    jsHint.validate(".foo { background: -o-linear-gradient(top, #1e5799 , #2989d8 , #207cca , #7db9e8 ); }");
  }

}
