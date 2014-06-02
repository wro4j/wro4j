package ro.isdc.wro.model.resource.processor.support;

import static org.junit.Assert.assertEquals;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import ro.isdc.wro.config.Context;
import ro.isdc.wro.model.resource.locator.ClasspathUriLocator;
import ro.isdc.wro.model.resource.processor.support.ImageUrlRewriter.RewriterContext;


/**
 * @author Alex Objelean
 */
public class TestImageUrlRewriter {
  private static final String DEFAULT_PREFIX = "[prefix]";
  private static final String DEFAULT_CONTEXT_PATH = "/";

  private static final String DEFAULT_CSS_URI = "/path/to/style.css";
  private static final String PROTECTED_CSS_URI = "/WEB-INF/path/to/style.css";
  private static final String DEFAULT_IMAGE_URL = "/img/image.png";
  private static final String RELATIVE_IMAGE_URL = "img/image.png";
  private RewriterContext context;
  private ImageUrlRewriter victim;

  @BeforeClass
  public static void onBeforeClass() {
    assertEquals(0, Context.countActive());
  }

  @AfterClass
  public static void onAfterClass() {
    assertEquals(0, Context.countActive());
  }

  @Before
  public void setUp() {
    context = new RewriterContext();
    context.setProxyPrefix(DEFAULT_PREFIX);
    context.setContextPath(DEFAULT_CONTEXT_PATH);
    victim = new ImageUrlRewriter(context);
  }

  @Test(expected = NullPointerException.class)
  public void cannotAcceptNullProxyPrefix() {
    final RewriterContext context = new RewriterContext();
    context.setContextPath(DEFAULT_CONTEXT_PATH);
    new ImageUrlRewriter(context);
  }

  @Test(expected = NullPointerException.class)
  public void cannotAcceptNullCssUri() {
    victim.rewrite(null, DEFAULT_IMAGE_URL);
  }

  @Test(expected = NullPointerException.class)
  public void cannotAcceptNullImageUrl() {
    victim.rewrite(DEFAULT_CSS_URI, null);
  }

  @Test
  public void checkContextRelativeUrlInClasspathCssResource() {
    final String actual = victim.rewrite(ClasspathUriLocator.createUri(DEFAULT_CSS_URI), DEFAULT_IMAGE_URL);
    assertEquals(DEFAULT_IMAGE_URL, actual);
  }

  @Test
  public void checkRelativeImageUrlInClasspathCssResource() {
    final String actual = victim.rewrite(ClasspathUriLocator.createUri(DEFAULT_CSS_URI), RELATIVE_IMAGE_URL);
    final String expected = DEFAULT_PREFIX + "classpath:/path/to/" + RELATIVE_IMAGE_URL;
    assertEquals(expected, actual);
  }

  @Test
  public void checkRelativeImageUrlInClasspathCssResourceWhenContextPathIsNotDefault() {
    context.setContextPath("/1/2/3");
    final String actual = victim.rewrite(ClasspathUriLocator.createUri(DEFAULT_CSS_URI), RELATIVE_IMAGE_URL);
    final String expected = DEFAULT_PREFIX + "classpath:/path/to/" + RELATIVE_IMAGE_URL;
    assertEquals(expected, actual);
  }

  @Test
  public void checkRelativeWithDotsImageUrlInClasspathCssResource() {
    final String actual = victim.rewrite(ClasspathUriLocator.createUri(DEFAULT_CSS_URI), "../" + RELATIVE_IMAGE_URL);
    final String expected = DEFAULT_PREFIX + "classpath:/path/" + RELATIVE_IMAGE_URL;
    assertEquals(expected, actual);
  }

  @Test
  public void checkRelativeImageUrlInContextRelativeCssResource() {
    final String actual = victim.rewrite(DEFAULT_CSS_URI, RELATIVE_IMAGE_URL);
    final String expected = "/path/to/" + RELATIVE_IMAGE_URL;
    assertEquals(expected, actual);
  }


  @Test
  public void checkEmptyImageUrlInContextRelativeCssResource() {
    final String actual = victim.rewrite(DEFAULT_CSS_URI, "");
    final String expected = "";
    assertEquals(expected, actual);
  }

  @Test
  public void checkRelativeImageUrlInProtectedContextRelativeCssResource() {
    final String actual = victim.rewrite(PROTECTED_CSS_URI, RELATIVE_IMAGE_URL);
    final String expected = DEFAULT_PREFIX + "/WEB-INF/path/to/" + RELATIVE_IMAGE_URL;
    assertEquals(expected, actual);
  }

  @Test
  public void checkRelativeWithDotsImageUrlInContextRelativeCssResource() {
    final String actual = victim.rewrite(DEFAULT_CSS_URI, "../" + RELATIVE_IMAGE_URL);
    final String expected = "/path/" + RELATIVE_IMAGE_URL;
    assertEquals(expected, actual);
  }

  @Test
  public void shouldRewriteProperlyWhenWindowsPathSeparatorIsUsed() {
    context.setAggregatedFolderPath("\\resources\\css");
    assertRewriteWithTwoLevelAggregatedPath();
  }

  @Test
  public void shouldRewriteProperlyWhenUnixPathSeparatorIsUsed() {
    context.setAggregatedFolderPath("/resources/css");
    assertRewriteWithTwoLevelAggregatedPath();
  }

  private void assertRewriteWithTwoLevelAggregatedPath() {
    final String actual = victim.rewrite("/resources/assets/select2/select2.css", "select2.png");
    final String expected = "../../resources/assets/select2/select2.png";
    assertEquals(expected, actual);
  }
}
