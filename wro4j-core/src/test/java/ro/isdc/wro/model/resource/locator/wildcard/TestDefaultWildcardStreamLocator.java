/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.model.resource.locator.wildcard;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import ro.isdc.wro.config.Context;
import ro.isdc.wro.model.resource.locator.ClasspathUriLocator;
import ro.isdc.wro.model.resource.locator.UriLocator;
import ro.isdc.wro.util.WroUtil;


/**
 * @author Alex Objelean
 */
public class TestDefaultWildcardStreamLocator {
  private WildcardStreamLocator locator;
  
  @BeforeClass
  public static void onBeforeClass() {
    assertEquals(0, Context.countActive());
  }
  
  @Before
  public void setUp() {
    locator = new DefaultWildcardStreamLocator();
  }
  
  @Test
  public void testNoWildcardPresent1() {
    assertFalse(locator.hasWildcard("test/resource.css"));
  }
  
  @Test
  public void testNoWildcardPresent2() {
    assertFalse(locator.hasWildcard("test/resource[a].css"));
  }
  
  @Test
  public void testWildcardPresent1() {
    assertTrue(locator.hasWildcard("test/*.css"));
  }
  
  @Test
  public void testWildcardPresent2() {
    assertTrue(locator.hasWildcard("test/test.?ss"));
  }
  
  @Test
  public void testWildcardPresent3() {
    assertTrue(locator.hasWildcard("test/**.???"));
  }
  
  @Test
  public void testWildcardPresent4() {
    assertFalse(locator.hasWildcard("http://yui.yahooapis.com/combo?2.7.0/build/reset-fonts-grids/reset-fonts-grids.css&2.7.0/build/base/base-min.css&2.7.0/build/assets/skins/sam/skin.css"));
  }
  
  @Test(expected = IOException.class)
  public void cannotPassNullArgument()
      throws IOException {
    locator.locateStream(null, null);
  }
  
  @Test(expected = IOException.class)
  public void cannotPassNullFolder()
      throws IOException {
    locator.locateStream("/resource/*.css", null);
  }
  
  @Test(expected = IOException.class)
  public void cannotProcessUriWithoutWildcard()
      throws IOException {
    final File folder = new File(ClassLoader.getSystemResource("").getFile());
    locator.locateStream("/resource/noWildcard.css", folder);
  }
  
  @Test
  public void testWithValidFolder()
      throws IOException {
    final File folder = new File(ClassLoader.getSystemResource("").getFile());
    locator.locateStream("/resource/*.css", folder);
  }
  
  @Test(expected = IOException.class)
  public void testWithInvalidFolder()
      throws IOException {
    final File folder = new File(ClassLoader.getSystemResource("1.css").getFile());
    locator.locateStream("/resource/*.css", folder);
  }
  
  @Test
  public void testWildcardResourcesOrderedAlphabetically()
      throws IOException {
    locator = new DefaultWildcardStreamLocator() {
      @Override
      void triggerWildcardExpander(final Collection<File> allFiles, final WildcardContext wildcardContext)
          throws IOException {
        final Collection<String> filenameList = new ArrayList<String>();
        for (final File file : allFiles) {
          filenameList.add(file.getName());
        }
        assertEquals(
            Arrays.toString(new String[] {
              "tools.expose-1.0.5.js", "tools.overlay-1.1.2.js", "tools.overlay.apple-1.0.1.js",
              "tools.overlay.gallery-1.0.0.js"
            }), Arrays.toString(filenameList.toArray()));
      };
    };
    final UriLocator uriLocator = new ClasspathUriLocator() {
      @Override
      public WildcardStreamLocator newWildcardStreamLocator() {
        return locator;
      }
    };
    uriLocator.locate("classpath:" + WroUtil.toPackageAsFolder(getClass()) + "/*.js");
  }
  
  @Test
  public void testWildcardLocator()
      throws IOException {
    locator = new DefaultWildcardStreamLocator() {
      @Override
      void triggerWildcardExpander(final Collection<File> allFiles, final WildcardContext wildcardContext)
          throws IOException {
        assertEquals(2, allFiles.size());
      };
    };
    final UriLocator uriLocator = new ClasspathUriLocator() {
      @Override
      public WildcardStreamLocator newWildcardStreamLocator() {
        return locator;
      }
    };
    uriLocator.locate("classpath:" + WroUtil.toPackageAsFolder(getClass()) + "/*.css");
  }
}
