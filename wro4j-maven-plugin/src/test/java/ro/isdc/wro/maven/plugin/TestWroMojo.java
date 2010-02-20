/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.maven.plugin;

import java.io.File;
import java.net.URL;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

/**
 * Test class for {@link Wro4jMojo}
 *
 * @author Alex Objelean
 */
public class TestWroMojo {
  private Wro4jMojo mojo;
  @Before
  public void setUp() throws Exception {
    mojo = new Wro4jMojo();
    final URL url = getClass().getClassLoader().getResource("wro.xml");
    final File file = new File(url.toURI());
    mojo.setWroFile(file);
    mojo.setDestinationFolder(new File("c://wro"));
  }

  @Test
  public void first() throws Exception {
    mojo.setTargetGroups(Arrays.asList("g1"));
    mojo.execute();
  }
}
