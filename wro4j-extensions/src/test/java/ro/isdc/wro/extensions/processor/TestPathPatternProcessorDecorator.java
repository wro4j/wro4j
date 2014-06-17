package ro.isdc.wro.extensions.processor;

import static org.junit.Assert.assertEquals;
import static org.mockito.MockitoAnnotations.initMocks;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.config.Context;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;


/**
 * @author Alex Objelean
 */
public class TestPathPatternProcessorDecorator {
  private PathPatternProcessorDecorator victim;
  @Mock
  private Reader mockReader;
  @Mock
  private Writer mockWriter;
  @Mock
  private ResourcePreProcessor mockProcessor;

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
    initMocks(this);
    mockReader = new StringReader("") {
      @Override
      public void close() {
        // make reader uncloseable
      }
    };
  }

  @Test(expected = NullPointerException.class)
  public void cannotIncludeNullPatterns() {
    final String[] patterns = null;
    PathPatternProcessorDecorator.include(mockProcessor, patterns);
  }

  @Test(expected = NullPointerException.class)
  public void cannotIncludeNullProcessor() {
    PathPatternProcessorDecorator.include(null, "");
  }

  @Test(expected = NullPointerException.class)
  public void cannotExclusdeNullPatterns() {
    final String[] patterns = null;
    PathPatternProcessorDecorator.exclude(mockProcessor, patterns);
  }

  @Test(expected = NullPointerException.class)
  public void cannotExcludeNullProcessor() {
    PathPatternProcessorDecorator.include(null, "");
  }

  @Test(expected = WroRuntimeException.class)
  public void cannotProcessWithIncludeWhenResourceIsUnknown()
      throws Exception {
    victim = PathPatternProcessorDecorator.include(mockProcessor, "");
    victim.process(mockReader, mockWriter);
  }

  @Test(expected = WroRuntimeException.class)
  public void cannotProcessWithExcludeWhenResourceIsUnknown()
      throws Exception {
    victim = PathPatternProcessorDecorator.exclude(mockProcessor, "");
    victim.process(mockReader, mockWriter);
  }

  @Test
  public void shouldApplyPatternsWhenIncludeMatches()
      throws Exception {
    checkThatInclusionMatches("/a/path/to.less", "/**/*.less");
    checkThatInclusionMatches("/a/path/to.css", "/a/*/to.*");
    checkThatInclusionMatches("/a/path/inner/to.css", "/a/**/to.*");
    checkThatInclusionMatches("/a/b/c/name.css", "/a/**/n?me.css");
    checkThatInclusionMatches("/a/b/c/name.css", "/a/**/n?me.js", "/a/**/n?me.c?s");
  }

  @Test
  public void shouldNotApplyPatternsWhenExcludeMatches()
      throws Exception {
    checkThatExclusionMatches("/a/path/to.less", "/**/*.less");
    checkThatExclusionMatches("/a/path/to.css", "/a/*/to.*");
    checkThatExclusionMatches("/a/path/inner/to.css", "/a/**/to.*");
    checkThatExclusionMatches("/a/b/c/name.css", "/a/**/n?me.css");
    checkThatExclusionMatches("/a/b/c/name.css", "/a/**/n?me.js", "/a/**/n?me.c?s");
  }

  @Test
  public void shouldNotApplyPatternsWhenIncludeMatches()
      throws Exception {
    checkThatInclusionNotMatches("/a/path/to.js", "/**/*.less");
    checkThatInclusionNotMatches("/b/path/to.css", "/a/*/to.*");
    checkThatInclusionNotMatches("/a/path/inner/bo.css", "/a/**/to.*");
    checkThatInclusionNotMatches("/a/b/c/aame.css", "/a/**/n?me.css");
    checkThatInclusionNotMatches("/a/b/c/name.less", "/a/**/n?me.js", "/a/**/n?me.c?s");
  }

  @Test
  public void shouldApplyPatternsWhenExcludeNotMatches()
      throws Exception {
    checkThatExclusionNotMatches("/a/path/to.js", "/**/*.less");
    checkThatExclusionNotMatches("/b/path/to.css", "/a/*/to.*");
    checkThatExclusionNotMatches("/a/path/inner/bo.css", "/a/**/to.*");
    checkThatExclusionNotMatches("/a/b/c/aame.css", "/a/**/n?me.css");
    checkThatExclusionNotMatches("/a/b/c/name.less", "/a/**/n?me.js", "/a/**/n?me.c?s");
  }

  private void checkThatInclusionMatches(final String resourceUri, final String... patterns)
      throws IOException {
    victim = PathPatternProcessorDecorator.include(mockProcessor, patterns);
    final Resource resource = Resource.create(resourceUri, ResourceType.CSS);
    victim.process(resource, mockReader, mockWriter);
    Mockito.verify(mockProcessor, Mockito.times(1)).process(resource, mockReader, mockWriter);
    // reset for correct times computation
    Mockito.reset(mockProcessor);
  }

  private void checkThatExclusionMatches(final String resourceUri, final String... patterns)
      throws IOException {
    victim = PathPatternProcessorDecorator.exclude(mockProcessor, patterns);
    final Resource resource = Resource.create(resourceUri, ResourceType.CSS);
    victim.process(resource, mockReader, mockWriter);
    Mockito.verify(mockProcessor, Mockito.never()).process(resource, mockReader, mockWriter);
  }

  private void checkThatInclusionNotMatches(final String resourceUri, final String... patterns)
      throws IOException {
    victim = PathPatternProcessorDecorator.include(mockProcessor, patterns);
    final Resource resource = Resource.create(resourceUri, ResourceType.CSS);
    victim.process(resource, mockReader, mockWriter);
    Mockito.verify(mockProcessor, Mockito.never()).process(resource, mockReader, mockWriter);
  }

  private void checkThatExclusionNotMatches(final String resourceUri, final String... patterns)
      throws IOException {
    victim = PathPatternProcessorDecorator.exclude(mockProcessor, patterns);
    final Resource resource = Resource.create(resourceUri, ResourceType.CSS);
    victim.process(resource, mockReader, mockWriter);
    Mockito.verify(mockProcessor, Mockito.times(1)).process(resource, mockReader, mockWriter);
    // reset for correct times computation
    Mockito.reset(mockProcessor);
  }

}
