/**
 * Copyright wro4j@2011
 */
package ro.isdc.wro.extensions.processor.support.csslint;

import static org.junit.Assert.assertEquals;

import java.io.FileInputStream;
import java.net.URL;

import org.apache.commons.io.IOUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import ro.isdc.wro.config.Context;


/**
 * @author Alex Objelean
 */
public class TestCssLint {
  private CssLint cssLint;

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
    cssLint = new CssLint();
  }

  @Test
  public void testSetNullOptions()
      throws Exception {
    cssLint.setOptions(null);
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
    cssLint.setOptions("1, 2");
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

  @Test(expected = CssLintException.class)
  public void testStyleContainingAScript()
      throws Exception {
    cssLint.validate("$(function(){})(jQuery);");
  }

  @Test(expected = CssLintException.class)
  public void testDuplicateHeading()
      throws Exception {
    cssLint.setOptions("unique-headings");
    cssLint.validate("h1 { color: red;} h1 {color: blue;}");
  }

  @Test(expected = CssLintException.class)
  public void testRegexSelectors()
      throws Exception {
    cssLint.setOptions("regex-selectors");
    cssLint.validate("li[class*=foo]{ color: red; }");
  }

  @Test(expected = CssLintException.class)
  public void testOperaGradient()
      throws Exception {
    cssLint.setOptions("gradients");
    cssLint.validate(".foo { background: -o-linear-gradient(top, #1e5799 , #2989d8 , #207cca , #7db9e8 ); }");
  }

  @Test
  public void shouldHaveNoErrorWhenNoOptions()
      throws Exception {
    cssLint.setOptions("");
    final URL url = getClass().getResource("sample/content.css");
    cssLint.validate(IOUtils.toString(new FileInputStream(url.getFile())));
  }

  @Test
  public void processSampleContentWithManyOptions()
      throws Exception {
    cssLint.setOptions("ids,adjoining-classes,box-model,box-sizing,compatible-vendor-prefixes,display-property-grouping,duplicate-background-images,duplicate-properties,empty-rules,errors,fallback-colors,floats,font-faces,font-sizes,gradients,import,important,known-properties,outline-none,overqualified-elements,qualified-headings,regex-selectors,rules-count,shorthand,text-indent,unique-headings,universal-selector,unqualified-attributes,vendor-prefix,zero-units");
    try {
      final URL url = getClass().getResource("sample/content.css");
      cssLint.validate(IOUtils.toString(new FileInputStream(url.getFile())));
      Assert.fail("should have failed!");
    } catch (final CssLintException e) {
      Assert.assertEquals(30, e.getErrors().size());
    }
  }
}
