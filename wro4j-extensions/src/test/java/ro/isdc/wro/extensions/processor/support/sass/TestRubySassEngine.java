package ro.isdc.wro.extensions.processor.support.sass;

import java.io.IOException;

import junit.framework.Assert;

import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;

import ro.isdc.wro.WroRuntimeException;


/**
 * @author Dmitry Erman
 */
public class TestRubySassEngine {
  private RubySassEngine engine;


  @Before
  public void setUp() {
    engine = new RubySassEngine();
  }


  @Test
  public void shouldReturnEmptyStringWhenNullContentIsProcessed() {
    Assert.assertEquals(StringUtils.EMPTY, engine.process(null));
  }


  @Test
  public void shouldReturnEmptyStringWhenEmptyContentIsProcessed() {
    Assert.assertEquals(StringUtils.EMPTY, engine.process(""));
  }


  @Test(expected = WroRuntimeException.class)
  public void cannotProcessInvalidCss() {
    Assert.assertEquals(StringUtils.EMPTY, engine.process("invalidCss"));
  }


  @Test
  public void shouldProcessValidCss()
    throws IOException {
    Assert.assertNotNull(engine.process("#element {color: red;}"));
  }

  @Test
  public void shouldProcessValidSass()
    throws IOException {
    Assert.assertNotNull(engine.process("#element { #child {color: red;}}"));
  }
}
