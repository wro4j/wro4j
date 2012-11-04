package ro.isdc.wro.extensions.processor.support.dustjs;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.lang3.StringUtils;

import ro.isdc.wro.extensions.processor.support.template.AbstractJsTemplateCompiler;


/**
 * Dust is a JavaScript templating engine designed to provide a clean separation between presentation and logic without
 * sacrificing ease of use. It is particularly well-suited for asynchronous and streaming applications.
 *
 * @author Eivind Barstad Waaler
 * @since 1.4.5
 * @created 8 Mar 2012
 */
public class DustJs extends AbstractJsTemplateCompiler {
  /**
   * The system property name used to specify the path of the dust compiler. If this is not specified, the default
   * compiler is used.
   */
  private static final String SYSTEM_PROPERTY_NAME = "dustJs.file.path";
  private static final String DEFAULT_DUST_JS = "dust-full-1.1.1.min.js";

  /**
   * {@inheritDoc}
   */
  @Override
  protected InputStream getCompilerAsStream() throws IOException {
    final String compilerPath = System.getProperty(SYSTEM_PROPERTY_NAME, DEFAULT_DUST_JS);
    return StringUtils.isEmpty(compilerPath) ? DustJs.class.getResourceAsStream(DEFAULT_DUST_JS) : new FileInputStream(
        compilerPath);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected String getCompileCommand() {
    return "dust.compile";
  }
}
