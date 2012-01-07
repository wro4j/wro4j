/**
 * Copyright wro4j@2011
 */
package ro.isdc.wro.extensions.processor.support.csslint;

import org.junit.Test;


/**
 * @author Alex Objelean
 */
public class TestCssLint {
  private CssLint cssLint = new CssLint();

  @Test
  public void testSetNullOptions()
    throws Exception {
    cssLint.setOptions((String[])null);
    cssLint.validate("");
  }


  @Test
  public void testWithNoOptions()
    throws Exception {
    cssLint.validate("");
  }


  @Test
  public void testWithSeveralOptions()
    throws Exception {
    cssLint.setOptions("1", "2");
    cssLint.validate("");
  }


  @Test(expected = CssLintException.class)
  public void testStyleWithWarning()
    throws Exception {
    cssLint.setOptions("box-model");
    cssLint.validate(".foo { width: 100px; padding: 10px; }");
  }


  @Test
  public void testValidStyle()
    throws Exception {
    cssLint.validate("h1 { left: 0; }");
  }


  @Test(expected=CssLintException.class)
  public void testStyleContainingAScript()
    throws Exception {
    cssLint.validate("$(function(){})(jQuery);");
  }

  @Test(expected=CssLintException.class)
  public void testDuplicateHeading() throws Exception {
    cssLint.setOptions("unique-headings");
    cssLint.validate("h1 { color: red;} h1 {color: blue;}");
  }

  @Test(expected=CssLintException.class)
  public void testRegexSelectors() throws Exception {
    cssLint.setOptions("regex-selectors");
    cssLint.validate("li[class*=foo]{ color: red; }");
  }

  @Test(expected=CssLintException.class)
  public void testOperaGradient() throws Exception {
    cssLint.setOptions("gradients");
    cssLint.validate(".foo { background: -o-linear-gradient(top, #1e5799 , #2989d8 , #207cca , #7db9e8 ); }");
  }

}
