package ro.isdc.wro.extensions.processor.support.sass;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.jruby.embed.EvalFailedException;
import org.jruby.embed.LocalVariableBehavior;
import org.jruby.embed.ScriptingContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.util.DestroyableLazyInitializer;
import ro.isdc.wro.util.StopWatch;


/**
 * A Sass processor using ruby gems.
 *
 * @author Dmitry Erman
 * @since 1.4.4
 * @created 12 Feb 2012
 */
public class RubySassEngine {
  private static final Logger LOG = LoggerFactory.getLogger(RubySassEngine.class);
  private final DestroyableLazyInitializer<ScriptingContainer> scriptingContainerInitializer = new DestroyableLazyInitializer<ScriptingContainer>() {
    @Override
    protected ScriptingContainer initialize() {
      return new ScriptingContainer(LocalVariableBehavior.PERSISTENT);
    }
  };

  /**
   * Transforms a sass content into css using Sass ruby engine.
   * @param content the Sass content to process.
   */
  public String process(final String content) {
    if (StringUtils.isEmpty(content)) {
      return StringUtils.EMPTY;
    }
    try {
      final StopWatch stopWatch = new StopWatch();
      stopWatch.start("process SCSS");
      scriptingContainerInitializer.get().runScriptlet(buildUpdateScript(content));
      stopWatch.stop();
      LOG.debug(stopWatch.prettyPrint());
      return (String)scriptingContainerInitializer.get().get("result");
    } catch (final EvalFailedException e) {
      throw new WroRuntimeException(e.getMessage(), e);
    }
  }


  private String buildUpdateScript(final String content) {
    Validate.notNull(content);
    final StringWriter raw = new StringWriter();
    final PrintWriter script = new PrintWriter(raw);
    final StringBuilder sb = new StringBuilder();
    sb.append(":syntax => :scss");
    script.println("  require 'rubygems'                                            ");
    script.println("  require 'sass/plugin'                                         ");
    script.println("  require 'sass/engine'                                         ");
    script.println("  source = '" + content.replace("'", "\"") + "'                  ");
    script.println("  engine = Sass::Engine.new(source, {" + sb.toString() + "})    ");
    script.println("  result = engine.render                                        ");
    script.flush();
    return raw.toString();
  }
}
