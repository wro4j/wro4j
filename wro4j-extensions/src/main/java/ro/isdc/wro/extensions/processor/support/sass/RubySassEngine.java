package ro.isdc.wro.extensions.processor.support.sass;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.WroRuntimeException;


/**
 * A Sass processor using ruby gems.
 *
 * @author Dmitry Erman
 * @created 12 Feb 2012
 * @since 1.4.4
 */
public class RubySassEngine {
  private static final Logger LOG = LoggerFactory.getLogger(RubySassEngine.class);
  private static final String RUBY_GEM_REQUIRE = "rubygems";
  private static final String SASS_PLUGIN_REQUIRE = "sass/plugin";
  private static final String SASS_ENGINE_REQUIRE = "sass/engine";

  private final Set<String> requires;

  public RubySassEngine() {
    requires = new LinkedHashSet<String>();
    requires.add(RUBY_GEM_REQUIRE);
    requires.add(SASS_PLUGIN_REQUIRE);
    requires.add(SASS_ENGINE_REQUIRE);
  }

  /**
   * Adds a ruby require to the ruby script to be run by this RubySassEngine. It's safe to add the same require twice.
   *
   * @param require
   *          The name of the require, e.g. bourbon
   */
  public void addRequire(final String require) {
    if (require != null && require.trim().length() > 0) {
      requires.add(require.trim());
    }
  }

  /**
   * Transforms a sass content into css using Sass ruby engine. This method is synchronized because the engine itself is
   * not thread-safe.
   *
   * @param content
   *          the Sass content to process.
   */
  public synchronized String process(final String content) {
    if (StringUtils.isEmpty(content)) {
      return StringUtils.EMPTY;
    }
    try {
      final ScriptEngine rubyEngine = new ScriptEngineManager().getEngineByName("jruby");
      return rubyEngine.eval(buildUpdateScript(content)).toString();
    } catch (final ScriptException e) {
      throw new WroRuntimeException(e.getMessage(), e);
    }
  }

  private String buildUpdateScript(final String content) {
    Validate.notNull(content);
    final StringWriter raw = new StringWriter();
    final PrintWriter script = new PrintWriter(raw);
    final StringBuilder sb = new StringBuilder();
    sb.append(":syntax => :scss");

    for (final String require : requires) {
      script.println("  require '" + require + "'                                   ");
    }
    final String scriptAsString = String.format("result = Sass::Engine.new('%s', {%s}).render",
        content.replace("'", "\""),
        sb.toString());
    LOG.debug("scriptAsString: {}", scriptAsString);
    script.println(scriptAsString);
    script.flush();
    return raw.toString();
  }
}
