/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.extensions.processor.support.linter;

import junit.framework.Assert;

import org.junit.Test;

import ro.isdc.wro.extensions.processor.support.linter.JsLint;
import ro.isdc.wro.extensions.processor.support.linter.LinterException;


/**
 * @author Alex Objelean
 */
public class TestJsLint {
  private final JsLint jsLint = new JsLint();

  @Test
  public void testSetNullOptions()
    throws Exception {
    jsLint.setOptions((String[])null);
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
    jsLint.setOptions("1", "2");
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

  @Test
  public void testEmptyOptions() throws Exception {
    testGenericOptions("{}", "");
  }

  @Test
  public void testNullOptions() throws Exception {
    testGenericOptions("{}", null);
  }

  @Test
  public void testOptionWithNoValue() throws Exception {
    testGenericOptions("{\"devel\": true}", "devel");
  }

  @Test
  public void testOptionWithValue() throws Exception {
    testGenericOptions("{\"maxerr\": 100}", "maxerr=100");
  }

  @Test
  public void predefOption() throws Exception {
    testGenericOptions("{\"predef\": ['YUI']}", "predef=['YUI']");
  }

  @Test
  public void predefOptionWithQuotes() throws Exception {
    testGenericOptions("{\"predef\": \"['YUI']\"}", "predef=\"['YUI']\"");
  }


  @Test
  public void testOptionWithValueAndSpaces() throws Exception {
    testGenericOptions("{\"maxerr\": 100}", "maxerr =  100");
  }

  @Test(expected=IllegalArgumentException.class)
  public void testOptionWithEmptyValue() throws Exception {
    testGenericOptions("{\"maxerr\": 100}", "maxerr=");
  }

  private void testGenericOptions(final String expectedOptions, final String... providedOptions) throws Exception {
    Assert.assertEquals(expectedOptions, jsLint.buildOptions(providedOptions));
  }
}
