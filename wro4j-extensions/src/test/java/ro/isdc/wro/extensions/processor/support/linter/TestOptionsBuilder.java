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
    Assert.assertEquals("{}", optionsBuilder.build(null));
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
  public void splitingMoreOptions() {
    Assert.assertTrue(Arrays.equals(new String[] { "o1", "o2" }, optionsBuilder.splitOptions("o1,o2")));
  }

  @Test
  public void splitingComplexOption() {
    final String option = "predef=['YUI','window','document','OnlineOpinion','xui']";
    final String[] result = optionsBuilder.splitOptions(option);

    System.out.println("result.length: " + result.length);
    System.out.println("result: " + Arrays.toString(result));
    Assert.assertEquals(1, result.length);
    Assert.assertTrue(Arrays.equals(new String[] { option }, result));
  }
}
