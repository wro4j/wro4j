package ro.isdc.wro.extensions.processor.css;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import org.w3c.css.sac.InputSource;

import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;

import com.vaadin.sass.internal.handler.SCSSDocumentHandler;
import com.vaadin.sass.internal.handler.SCSSDocumentHandlerImpl;
import com.vaadin.sass.internal.parser.Parser;


/**
 * Compiles sass resources to css using <a href="https://github.com/vaadin/sass-compiler/">vaadin sass-compiler
 * library</a>.
 *
 * @author Alex Objelean
 * @created 22 May 2015
 * @since 1.7.9
 */
public class VaadinSassProcessor
    implements ResourcePreProcessor, ResourcePostProcessor {

  private final Parser parser = createParser();

  @Override
  public void process(final Reader reader, final Writer writer)
      throws IOException {
    parser.parseStyleSheet(new InputSource(reader));
  }

  @Override
  public void process(final Resource resource, final Reader reader, final Writer writer)
      throws IOException {
    parser.parseStyleSheet(new InputSource(reader));
  }

  private Parser createParser() {
    final Parser parser = new Parser();
    final SCSSDocumentHandler handler = new SCSSDocumentHandlerImpl();
    parser.setDocumentHandler(handler);
    return parser;
  }
}
