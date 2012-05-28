/**
 * Copyright wro4j@2011
 */
package ro.isdc.wro.extensions.processor.support.linter;

import java.util.Arrays;

import junit.framework.Assert;

import org.apache.commons.lang3.ArrayUtils;
import org.junit.Test;


/**
 * @author Alex Objelean
 */
public class TestOptionsBuilder {
  private final OptionsBuilder optionsBuilder = new OptionsBuilder();


  @Test
  public void testEmptyOptions()
    throws Exception {
    Assert.assertEquals("{}", optionsBuilder.build(""));
  }


  @Test
  public void testNullOptions()
    throws Exception {
    final String[] options = null;
    Assert.assertEquals("{}", optionsBuilder.build(options));
  }
  
  @Test(expected = NullPointerException.class)
  public void cannotBuildOptionsFromNullCSV() {
    optionsBuilder.buildFromCsv(null);
  }
  
  @Test
  public void shouldBuildOptionsFromCSV() {
    final String actual = optionsBuilder.buildFromCsv("a=1,b=2");
    Assert.assertEquals("{\"a\": 1,\"b\": 2}", actual);
  }

  @Test
  public void testOptionWithNoValue()
    throws Exception {
    Assert.assertEquals("{\"devel\": true}", optionsBuilder.build("devel"));
  }


  @Test
  public void testOptionWithValue()
    throws Exception {
    Assert.assertEquals("{\"maxerr\": 100}", optionsBuilder.build("maxerr=100"));
  }


  @Test
  public void predefOption()
    throws Exception {
    Assert.assertEquals("{\"predef\": ['YUI']}", optionsBuilder.build("predef=['YUI']"));
  }


  @Test
  public void predefOptionWithQuotes()
    throws Exception {
    Assert.assertEquals("{\"predef\": \"['YUI']\"}", optionsBuilder.build("predef=\"['YUI']\""));
  }


  @Test
  public void predefOptionWithManyOptions()
    throws Exception {
    Assert.assertEquals("{\"predef\": ['YUI','window','document','OnlineOpinion','xui']}",
      optionsBuilder.build("predef=['YUI','window','document','OnlineOpinion','xui']"));
  }


  @Test
  public void testOptionWithValueAndSpaces()
    throws Exception {
    Assert.assertEquals("{\"maxerr\": 100}", optionsBuilder.build("maxerr =  100"));
  }


  @Test(expected = IllegalArgumentException.class)
  public void testOptionWithEmptyValue()
    throws Exception {
    Assert.assertEquals("{\"maxerr\": 100}", optionsBuilder.build("maxerr="));
  }


  @Test
  public void splitingNullOptionProduceEmptyArray() {
    Assert.assertTrue(Arrays.equals(ArrayUtils.EMPTY_STRING_ARRAY, optionsBuilder.splitOptions(null)));
  }


  @Test
  public void splitingEmptyOption() {
    Assert.assertTrue(Arrays.equals(new String[] { "" }, optionsBuilder.splitOptions("")));
  }


  @Test
  public void splitingOneOption() {
    Assert.assertTrue(Arrays.equals(new String[] { "o1" }, optionsBuilder.splitOptions("o1")));
  }

  @Test
  public void splitingTwoOptions() {
    Assert.assertTrue(Arrays.equals(new String[] { "o1", "o2" }, optionsBuilder.splitOptions("o1,o2")));
  }

  @Test
  public void splitingComplexOption() {
    final String option = "predef=['YUI','window','document','OnlineOpinion','xui']";
    final String[] result = optionsBuilder.splitOptions(option);
    Assert.assertEquals(1, result.length);
    Assert.assertTrue(Arrays.equals(new String[] { option }, result));
  }

  @Test
  public void splitingComplexOptions() {
    final String option = "option1,option2,option3=['YUI','window','document','xui'],option4,option5=['YUI','xui'],option6";
    final String[] result = optionsBuilder.splitOptions(option);
    Assert.assertEquals(6, result.length);
    Assert.assertEquals(
      Arrays.toString(new String[] { "option1", "option2", "option3=['YUI','window','document','xui']", "option4",
          "option5=['YUI','xui']", "option6" }), Arrays.toString(result));
  }

  @Test
  public void splitOptionsWithHiphen() {
    final String option = "ids,adjoining-classes,box-model,box-sizing,compatible-vendor-prefixes,display-property-grouping,duplicate-background-images,duplicate-properties,empty-rules,errors,fallback-colors,floats,font-faces,font-sizes,gradients,import,important,known-properties,outline-none,overqualified-elements,qualified-headings,regex-selectors,rules-count,shorthand,text-indent,unique-headings,universal-selector,unqualified-attributes";
    final String[] result = optionsBuilder.splitOptions(option);
    Assert.assertEquals(28, result.length);
  }
}
