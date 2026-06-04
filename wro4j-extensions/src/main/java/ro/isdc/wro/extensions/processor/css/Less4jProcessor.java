package ro.isdc.wro.extensions.processor.css;

import static org.apache.commons.lang3.Validate.notNull;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.model.group.Inject;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.SupportedResourceType;
import ro.isdc.wro.model.resource.locator.factory.UriLocatorFactory;
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;
import ro.isdc.wro.util.WroUtil;

import com.github.sommeri.less4j.Less4jException;
import com.github.sommeri.less4j.LessCompiler;
import com.github.sommeri.less4j.LessCompiler.CompilationResult;
import com.github.sommeri.less4j.LessCompiler.Problem;
import com.github.sommeri.less4j.LessSource;
import com.github.sommeri.less4j.core.DefaultLessCompiler;


/**
 * Yet another processor which compiles less to css. This implementation uses open source java library called less4j.
 *
 * @author Alex Objelean
 * @since 1.6.0
 */
@SupportedResourceType(ResourceType.CSS)
public class Less4jProcessor
    implements ResourcePreProcessor, ResourcePostProcessor {
  private static final Logger LOG = LoggerFactory.getLogger(Less4jProcessor.class);
  public static final String ALIAS = "less4j";

  /**
   * Required to use the less4j import mechanism.
   */
  private static class RelativeAwareLessSource
      extends LessSource.StringSource {
    private final Resource resource;
    private final UriLocatorFactory locatorFactory;

    public RelativeAwareLessSource(final Resource resource, final String content, final UriLocatorFactory locatorFactory) {
      super(content);
      this.resource = resource;
      notNull(locatorFactory);
      this.locatorFactory = locatorFactory;
    }

    @Override
    public LessSource relativeSource(final String relativePath)
        throws StringSourceException {
      return resource != null ? computeRelative(resource, relativePath) : super.relativeSource(relativePath);
    }

    private LessSource computeRelative(final Resource resource, final String relativePath) throws StringSourceException {
      try {
        final String relativeResourceUri = computeRelativeResourceUri(resource.getUri(), relativePath);
        final Resource relativeResource = Resource.create(relativeResourceUri, ResourceType.CSS);
        final String relativeResourceContent = IOUtils.toString(locatorFactory.locate(relativeResourceUri), StandardCharsets.UTF_8);
        return new RelativeAwareLessSource(relativeResource, relativeResourceContent, locatorFactory);
      } catch (final IOException e) {
        LOG.error("Failed to compute relative resource: " + resource, e);
        throw new StringSourceException();
      }
    }

    public String computeRelativeResourceUri(final String originalResourceUri, final String relativePath) {
      final String fullPath = WroUtil.getFullPath(originalResourceUri) + relativePath;
      return WroUtil.normalize(fullPath);
    }
  }
  @Inject
  private UriLocatorFactory locatorFactory;

  private final LessCompiler compiler = new DefaultLessCompiler();

  @Override
  public void process(final Reader reader, final Writer writer)
      throws IOException {
    process(null, reader, writer);
  }

  @Override
  public void process(final Resource resource, final Reader reader, final Writer writer)
      throws IOException {
    try {
      final LessSource lessSource = new RelativeAwareLessSource(resource, IOUtils.toString(reader), locatorFactory);
      final LessCompiler.Configuration configuration = new LessCompiler.Configuration();
      configuration.getSourceMapConfiguration().setLinkSourceMap(false);
      final CompilationResult result = compiler.compile(lessSource, configuration);
      logWarnings(result);
      writer.write(result.getCss());
    } catch (final Less4jException e) {
      LOG.error("Failed to compile less resource: {}.", resource);
      for (final Problem problem : e.getErrors()) {
        LOG.error(problemAsString(problem));
      }
      throw WroRuntimeException.wrap(e);
    }
  }

  private void logWarnings(final CompilationResult result) {
    if (!result.getWarnings().isEmpty()) {
      LOG.warn("Less warnings are:");
      for (final Problem problem : result.getWarnings()) {
        LOG.warn(problemAsString(problem));
      }
    }
  }

  private String problemAsString(final Problem problem) {
    return String.format("%s:%s %s.", problem.getLine(), problem.getCharacter(), problem.getMessage());
  }
}
