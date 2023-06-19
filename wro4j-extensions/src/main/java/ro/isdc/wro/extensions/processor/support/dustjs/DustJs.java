package ro.isdc.wro.extensions.processor.support.dustjs;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.extensions.processor.support.template.AbstractJsTemplateCompiler;


/**
 * Dust is a JavaScript templating engine designed to provide a clean separation between presentation and logic without
 * sacrificing ease of use. It is particularly well-suited for asynchronous and streaming applications.
 *
 * @author Eivind Barstad Waaler
 * @since 1.4.5
 */
public class DustJs extends AbstractJsTemplateCompiler {
  private static final Logger LOG = LoggerFactory.getLogger(DustJs.class);

  /**
   * The name of the system property used to specify the path of the dust compiler. If this is not specified, the default
   * compiler is used.
   */
  static final String PARAM_COMPILER_PATH = "dustJs.file.path";
  private static final String DEFAULT_DUST_JS = "dust-full-1.1.1.min.js";

  /**
   * {@inheritDoc}
   */
  @Override
  protected InputStream getCompilerAsStream() throws IOException {
    final String compilerPath = System.getProperty(PARAM_COMPILER_PATH);
    LOG.debug("compilerPath: {}", compilerPath);
    return StringUtils.isEmpty(compilerPath) ? getDefaultCompilerStream() : new FileInputStream(
        compilerPath);
  }

  /**
   * @return default stream of the compiler.
   */
  protected InputStream getDefaultCompilerStream() {
    return DustJs.class.getResourceAsStream(DEFAULT_DUST_JS);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected String getCompileCommand() {
    return "dust.compile";
  }
}
