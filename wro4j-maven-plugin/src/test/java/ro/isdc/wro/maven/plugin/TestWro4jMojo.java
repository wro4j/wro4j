/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.maven.plugin;

import java.io.File;
import java.net.URL;
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Test class for {@link Wro4jMojo}
 *
 * @author Alex Objelean
 */
public class TestWro4jMojo {
  private Wro4jMojo mojo;
  private File destinationFolder;
  @Before
  public void setUp() throws Exception {
    mojo = new Wro4jMojo();
    final URL url = getClass().getClassLoader().getResource("wro.xml");
    final File file = new File(url.toURI());
    mojo.setWroFile(file);
    destinationFolder = new File("wroTemp-" + new Date().getTime());
    destinationFolder.mkdir();
    mojo.setDestinationFolder(destinationFolder);
  }

  @Test
  public void first() throws Exception {
    mojo.setTargetGroups("g1");
    mojo.execute();
  }
  @After
  public void tearDown() {
    FileUtils.deleteQuietly(destinationFolder);
  }
}
