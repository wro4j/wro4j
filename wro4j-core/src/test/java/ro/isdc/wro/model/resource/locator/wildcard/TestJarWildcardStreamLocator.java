package ro.isdc.wro.model.resource.locator.wildcard;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import junit.framework.Assert;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.model.resource.locator.ClasspathUriLocator;
import ro.isdc.wro.model.resource.locator.UriLocator;
import ro.isdc.wro.util.WroUtil;


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


  @Test
  public void testWildcardResourcesOrderedAlphabetically() throws IOException {
    jarStreamLocator = new JarWildcardStreamLocator() {
      @Override
      protected void handleFoundResources(final Map<String, File> map, final WildcardContext wildcardContext) {
        final Collection<String> filenameList = new ArrayList<String>();
        for (final File file : map.values()) {
          filenameList.add(file.getName());
        }
        Assert.assertEquals(Arrays.toString(new String[] {
          "tools.expose-1.0.5.js", "tools.overlay-1.1.2.js", "tools.overlay.apple-1.0.1.js", "tools.overlay.gallery-1.0.0.js"
        }), Arrays.toString(filenameList.toArray()));
      };
    };
    final UriLocator uriLocator = new ClasspathUriLocator() {
      @Override
      public WildcardStreamLocator newWildcardStreamLocator() {
        return jarStreamLocator;
      }
    };
    uriLocator.locate("classpath:" + WroUtil.toPackageAsFolder(getClass()) + "/*.js");
  }

}
