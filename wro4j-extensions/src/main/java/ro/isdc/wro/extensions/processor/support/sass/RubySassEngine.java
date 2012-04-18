package ro.isdc.wro.extensions.processor.support.sass;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.util.StopWatch;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.PrintWriter;
import java.io.StringWriter;


/**
 * A Sass processor using ruby gems.
 *
 * @author Dmitry Erman
 * @created 12 Feb 2012
 * @since 1.4.4
 */
public class RubySassEngine {
    private static final Logger LOG = LoggerFactory.getLogger(RubySassEngine.class);

    /**
     * Transforms a sass content into css using Sass ruby engine.
     *
     * @param content the Sass content to process.
     */
    public String process(final String content) {
        if (StringUtils.isEmpty(content)) {
            return StringUtils.EMPTY;
        }
        try {
            final StopWatch stopWatch = new StopWatch();
            stopWatch.start("process SCSS");
            ScriptEngine rubyEngine = new ScriptEngineManager().getEngineByName("jruby");
            ScriptContext context = rubyEngine.getContext();
            String result = rubyEngine.eval(buildUpdateScript(content)).toString();
            stopWatch.stop();
            LOG.debug(stopWatch.prettyPrint());
            return result;
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
