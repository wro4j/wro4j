/**
 * Copyright wro4j@2011
 */
package ro.isdc.wro.extensions.processor.support.linter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.apache.commons.lang3.ArrayUtils;
import org.junit.Test;

import ro.isdc.wro.WroRuntimeException;


/**
 * @author Alex Objelean
 */
public class TestOptionsBuilder {
  private final OptionsBuilder optionsBuilder = new OptionsBuilder();


  @Test
  public void shouldBuildOptions()
    throws Exception {
    assertEquals("{}", optionsBuilder.build(""));
  }


  @Test
  public void shouldBuildOptionsFromNullArray()
    throws Exception {
    final String[] options = null;
    assertEquals("{}", optionsBuilder.build(options));
  }

  @Test
  public void shouldBuildOptionsFromNullCSV() {
    assertEquals("{}", optionsBuilder.buildFromCsv(null));
  }

  @Test
  public void shouldBuildOptionsFromCSV() {
    final String actual = optionsBuilder.buildFromCsv("a=1,b=2");
    assertEquals("{\"a\": 1,\"b\": 2}", actual);
  }

  @Test
  public void testOptionWithNoValue()
    throws Exception {
    assertEquals("{\"devel\": true}", optionsBuilder.build("devel"));
  }


  @Test
  public void testOptionWithValue()
    throws Exception {
    assertEquals("{\"maxerr\": 100}", optionsBuilder.build("maxerr=100"));
  }


  @Test
  public void predefOption()
    throws Exception {
    assertEquals("{\"predef\": ['YUI']}", optionsBuilder.build("predef=['YUI']"));
  }


  @Test
  public void predefOptionWithQuotes()
    throws Exception {
    assertEquals("{\"predef\": \"['YUI']\"}", optionsBuilder.build("predef=\"['YUI']\""));
  }


  @Test
  public void predefOptionWithManyOptions()
    throws Exception {
    assertEquals("{\"predef\": ['YUI','window','document','OnlineOpinion','xui']}",
      optionsBuilder.build("predef=['YUI','window','document','OnlineOpinion','xui']"));
  }


  @Test
  public void testOptionWithValueAndSpaces()
    throws Exception {
    assertEquals("{\"maxerr\": 100}", optionsBuilder.build("maxerr =  100"));
  }


  @Test(expected = IllegalArgumentException.class)
  public void testOptionWithEmptyValue()
    throws Exception {
    assertEquals("{\"maxerr\": 100}", optionsBuilder.build("maxerr="));
  }


  @Test
  public void splitingNullOptionProduceEmptyArray() {
    assertTrue(Arrays.equals(ArrayUtils.EMPTY_STRING_ARRAY, optionsBuilder.splitOptions(null)));
  }


  @Test
  public void splitingEmptyOption() {
    assertTrue(Arrays.equals(new String[] { "" }, optionsBuilder.splitOptions("")));
  }


  @Test
  public void splitingOneOption() {
    assertTrue(Arrays.equals(new String[] { "o1" }, optionsBuilder.splitOptions("o1")));
  }

  @Test
  public void splitingTwoOptions() {
    assertTrue(Arrays.equals(new String[] { "o1", "o2" }, optionsBuilder.splitOptions("o1,o2")));
  }

  @Test
  public void splitingComplexOption() {
    final String option = "predef=['YUI','window','document','OnlineOpinion','xui']";
    final String[] result = optionsBuilder.splitOptions(option);
    assertEquals(1, result.length);
    assertTrue(Arrays.equals(new String[] { option }, result));
  }

  @Test
  public void splitingComplexOptions() {
    final String option = "option1,option2,option3=['YUI','window','document','xui'],option4,option5=['YUI','xui'],option6";
    final String[] result = optionsBuilder.splitOptions(option);
    assertEquals(6, result.length);
    assertEquals(
      Arrays.toString(new String[] { "option1", "option2", "option3=['YUI','window','document','xui']", "option4",
          "option5=['YUI','xui']", "option6" }), Arrays.toString(result));
  }

  @Test
  public void splitOptionsWithHiphen() {
    final String option = "ids,adjoining-classes,box-model,box-sizing,compatible-vendor-prefixes,display-property-grouping,duplicate-background-images,duplicate-properties,empty-rules,errors,fallback-colors,floats,font-faces,font-sizes,gradients,import,important,known-properties,outline-none,overqualified-elements,qualified-headings,regex-selectors,rules-count,shorthand,text-indent,unique-headings,universal-selector,unqualified-attributes";
    final String[] result = optionsBuilder.splitOptions(option);
    assertEquals(28, result.length);
  }

  @Test(expected = WroRuntimeException.class)
  public void cannotBuildCSVWheInvalidOptionsFormatProvided() {
    optionsBuilder.buildFromCsv("var:unused");
  }
}
