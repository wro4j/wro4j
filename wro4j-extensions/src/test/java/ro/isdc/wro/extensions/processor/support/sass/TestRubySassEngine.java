package ro.isdc.wro.extensions.processor.support.sass;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

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
    assertEquals(StringUtils.EMPTY, engine.process(null));
  }


  @Test
  public void shouldReturnEmptyStringWhenEmptyContentIsProcessed() {
    assertEquals(StringUtils.EMPTY, engine.process(""));
  }


  @Test(expected = WroRuntimeException.class)
  public void cannotProcessInvalidCss() {
    assertEquals(StringUtils.EMPTY, engine.process("invalidCss"));
  }


  @Test
  public void shouldProcessValidCss()
    throws IOException {
    assertEquals("#element {\n  color: red; }\n", engine.process("#element {color: red;}"));
  }

  @Test
  public void shouldProcessValidSass()
    throws IOException {
    assertEquals("#element #child {\n  color: red; }\n", engine.process("#element { #child {color: red;}}"));
  }

  @Test
  public void shouldProcessValidNonAsciiSass()
    throws IOException {
    assertEquals("@charset \"UTF-8\";\n#element {\n  font-family: \"\uFF2D\uFF33 \uFF30\u30B4\u30B7\u30C3\u30AF\"; }\n",
        engine.process("#element {font-family: \"\uFF2D\uFF33 \uFF30\u30B4\u30B7\u30C3\u30AF\";}"));
  }
}
