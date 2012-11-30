package ro.isdc.wro.extensions.processor.support.dustjs;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileOutputStream;

import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.util.WroUtil;


/**
 * @author Alex Objelean
 */
public class TestDustJs {
  private DustJs victim;

  @Before
  public void setUp() {
    victim = new DustJs();
  }

  @Test
  public void shouldCompileNullContent() {
    assertEquals("(function(){dust.register(null,body_0);function body_0(chk,ctx){return chk;}return body_0;})();",
        victim.compile(null, null));
  }

  @Test
  public void shouldUseConfiguredCompiler()
      throws Exception {
    final File temp = WroUtil.createTempFile();
    IOUtils.copy(victim.getDefaultCompilerStream(), new FileOutputStream(temp));
    System.setProperty(DustJs.PARAM_COMPILER_PATH, temp.getPath());
    assertEquals("(function(){dust.register(null,body_0);function body_0(chk,ctx){return chk;}return body_0;})();",
        victim.compile(null, null));
  }

  @Test(expected = WroRuntimeException.class)
  public void cannotCompileUsingWrongPathCompiler()
      throws Exception {
    System.setProperty(DustJs.PARAM_COMPILER_PATH, "/invalid/path/to/dust.js");
    victim.compile(null, null);
  }

  @After
  public void tearDown() {
    System.setProperty(DustJs.PARAM_COMPILER_PATH, "");
  }
}
