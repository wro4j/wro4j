package ro.isdc.wro.extensions.processor;

import static org.apache.commons.lang3.Validate.notEmpty;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.AntPathMatcher;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.processor.decorator.ProcessorDecorator;
import ro.isdc.wro.util.WroUtil;


/**
 * <p>A {@link ProcessorDecorator} with Ant path style support. This processor requires AntPathMatcher provided by spring
 * framework.</p>
 *
 * <p>The implementation is inspired from <a href="https://github.com/jknack/modern-web-app">mwa</a> project created by
 * Edgar Espina.</p>
 * 
 * @author Alex Objelean
 * @since 1.5.0
 */
public final class PathPatternProcessorDecorator
    extends ProcessorDecorator {
  private static final Logger LOG = LoggerFactory.getLogger(PathPatternProcessorDecorator.class);
  /**
   * The ant path patterns.
   */
  private final String[] patterns;

  /**
   * The path matcher.
   */
  private final AntPathMatcher matcher;

  /**
   * Shall we include paths?
   */
  private final boolean includes;
  
  /**
   * Decorates the processor which should be applied or not (based on includes parameter) on resources which matches the
   * provided patterns.
   * 
   * @param processor The processor to decorate.
   * @param includes the flag indicating if the patterns should be used for inclusion or exclusion.
   * @param patterns an array of patterns.
   */
  private PathPatternProcessorDecorator(final Object processor, final boolean includes, final String... patterns) {
    super(processor);
    notEmpty(patterns, "A pattern set is required.");
    this.includes = includes;
    matcher = new AntPathMatcher();
    this.patterns = patterns;
    LOG.debug("{} patterns {}", includes ? "include" : "exclude", Arrays.toString(patterns));
  }
  
  /**
   * Decorates a processor which will be applied on provided patterns.
   * 
   * @param processor
   *          The processor to decorate.
   * @param patterns
   *          an array of patterns on which the processor will be applied.
   * @return decorated processor.
   */
  public static PathPatternProcessorDecorator include(final Object processor, final String... patterns) {
    return new PathPatternProcessorDecorator(processor, true, patterns);
  }

  /**
   * Decorates a processor which will not be applied on provided patterns.
   * 
   * @param processor
   *          The processor to decorate.
   * @param patterns
   *          an array of patterns on which the processor will not be applied.
   * @return decorated processor.
   */
  public static PathPatternProcessorDecorator exclude(final Object processor, final String... patterns) {
    return new PathPatternProcessorDecorator(processor, false, patterns);
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public void process(final Resource resource, final Reader reader,
      final Writer writer)
      throws IOException {
    if (resource != null) {
      final String uri = resource.getUri();
      LOG.debug("matching uri: {}", uri);
      if (includes) {
        // Match (p1 OR p2 OR .. pn)
        for (String pattern : patterns) {
          if (matcher.match(pattern, uri)) {
            LOG.debug("Processing resource: {}. Match found: {}",
                uri, toString());
            getDecoratedObject().process(resource, reader, writer);
            return;
          }
        }
      } else {
        boolean process = true;
        // Match !(p1 AND p2 AND .. pn)
        for (String pattern : patterns) {
          if (matcher.match(pattern, uri)) {
            process = false;
            break;
          }
        }
        if (process) {
          LOG.debug("Processing resource: {}. Match found: {}", uri,
              toString());
          getDecoratedObject().process(resource, reader, writer);
          return;
        }
      }
      LOG.debug("Skipping {} from {}. No match found: {}", new Object[] {
          uri, getDecoratedObject(), toString() });
      WroUtil.safeCopy(reader, writer);
    } else {
      throw new WroRuntimeException("Wrong usage of "
          + toString() + ". Please use it as a pre-processor.");
    }
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    final String processorName = getOriginalDecoratedObject().getClass().getSimpleName();
    StringBuilder buffer = new StringBuilder(processorName).append(": ").append(includes ? "(" : "!(");
    String separator = includes ? " || " : " && ";
    for (String pattern : patterns) {
      buffer.append(pattern).append(separator);
    }
    buffer.setLength(buffer.length() - separator.length());
    buffer.append(")");
    return buffer.toString();
  }
}
