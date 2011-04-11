package ro.isdc.wro.model.resource.locator.wildcard;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Vector;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Tests the {@link JarWildcardStreamLocator} class.
 *
 * @author Matias Mirabelli &lt;matias.mirabelli@globant.com&gt;
 * @since 1.3.6
 */
public class TestJarWildcardStreamLocator {
  private static final Logger LOG = LoggerFactory.getLogger(TestJarWildcardStreamLocator.class);

  private JarWildcardStreamLocator jarStreamLocator;

  private final String testInfo = "var foo = 'Hello World';";

  private final String jarFileName = "file:///home/test/myJar.jar!";
  @Mock
  private JarFile jarFile;


  public TestJarWildcardStreamLocator() {
    MockitoAnnotations.initMocks(this);
  }


  @Before
  public void setUp()
    throws IOException {
    final Vector<JarEntry> vector = new Vector<JarEntry>();

    vector.add(new JarEntry("com/test/app/test-resource.js"));

    when(jarFile.entries()).thenReturn(vector.elements());
    when(jarFile.getInputStream(vector.get(0))).thenReturn(new ByteArrayInputStream(testInfo.getBytes()));

    jarStreamLocator = new JarWildcardStreamLocator() {
      @Override
      protected JarFile open(final File file) {
        return jarFile;
      }
    };
  }


  @Test
  public void testLocateJarStream()
    throws IOException {
    final InputStream is = jarStreamLocator.locateStream("com/test/app/*.js", new File(jarFileName));
    final List<String> lines = IOUtils.readLines(is);
    LOG.debug("lines: " + lines);
    assertEquals(testInfo, lines.get(0));

    IOUtils.closeQuietly(is);
  }


  @Test
  public void testLocateJarStreamDelegate()
    throws IOException {
    final InputStream is = jarStreamLocator.locateStream("classpath:com/test/app/*.js", new File("src/test/resources/"));
    final String content = IOUtils.readLines(is).get(0);
    assertTrue(content.contains("1.js"));
    assertTrue(content.contains("2.js"));
    assertTrue(content.contains("3.js"));
    assertTrue(!content.contains("1.css"));

    IOUtils.closeQuietly(is);
  }


  @Test(expected = IOException.class)
  public void testLocateJarStreamDelegateFail()
    throws IOException {
    jarStreamLocator.locateStream("com/test/app/*.js", new File("test.jpg"));
  }
}
