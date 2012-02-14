package ro.isdc.wro.extensions.processor.css;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.jruby.embed.LocalVariableBehavior;
import org.jruby.embed.ScriptingContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.SupportedResourceType;
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;
import ro.isdc.wro.util.StopWatch;

import java.io.*;

@SupportedResourceType(ResourceType.CSS)
public class RubySassProcessor implements ResourcePreProcessor, ResourcePostProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(RubySassProcessor.class);


    public Options getOpt() {
        return opt;
    }


    protected Options opt = new Options();

    public RubySassProcessor(){
    }

    /*** Pre Processor Method ***/
    public void process( final Resource resource, final Reader reader, final Writer writer)
            throws IOException {
        final String content = IOUtils.toString(reader);
        ScriptingContainer sc = new ScriptingContainer(LocalVariableBehavior.PERSISTENT);

        try {
            LOG.info("[RubySassProcessor] Processing stream reader.");
            sc.runScriptlet(BuildUpdateScript(content, this.opt));
            String result = (String)sc.get("result");
            writer.write(result);

        } catch (final WroRuntimeException e) {
            onException(e);
            //still write the content we already have to output stream w/o procesing in SASS
            writer.write(content);
            final String resourceUri = resource == null ? StringUtils.EMPTY : "[" + resource.getUri() + "]";
            LOG.warn("Exception while applying " + getClass().getSimpleName() + " processor on the " + resourceUri
                    + " resource, no processing applied...", e);

        } finally {
            reader.close();
            writer.close();
        }

    }

    /*** Post Processor Method ***/
    public void process( Reader reader, Writer writer) throws IOException {
        process(null, reader, writer);
    }

    /**
     * Invoked when a processing exception occurs.
     */
    protected void onException(final WroRuntimeException e) {
        LOG.warn("[RubySassProcessor] Error processing SASS stream: " + e.getMessage());
    }
    

    private String BuildUpdateScript(String content, Options opt) {
        final StopWatch stopWatch = new StopWatch();
        StringWriter raw = new StringWriter();
        PrintWriter script = new PrintWriter(raw);
        StringBuilder sb = new StringBuilder();
        sb.append(":syntax => :scss");
        if(!opt.getFilePath().isEmpty()) sb.append(",load_paths => '" + opt.getFilePath() + "'");

        LOG.info("[RubySassProcessor] START SCSS translation time: ");
        stopWatch.start("process SCSS");
        script.println("  require 'rubygems'                                            ");
        script.println("  require 'sass/plugin'                                         ");
        script.println("  require 'sass/engine'                                         ");
        script.println("  source = '" + content.replace("'","\"") + "'                  ");
        script.println("  engine = Sass::Engine.new(source, {" + sb.toString() + "})    ");
        script.println("  result = engine.render                                        ");
        script.flush();
        stopWatch.stop();
        LOG.info("[RubySassProcessor] Finished processing SASS stream conversion.");
        LOG.debug(stopWatch.prettyPrint());

        return raw.toString();        
    }
    
    private String BuildUpdateScript(String content){
        return BuildUpdateScript(content, new Options());
    }

    /**
     * Created by IntelliJ IDEA.
     * User: Dmitry.Erman
     * Date: 1/4/12
     * Time: 8:39 PM
     * To change this template use File | Settings | File Templates.
     */
    public static class Options {
        public String getFilePath() {
            return filePath;
        }

        public void setFilePath(String filePath) {
            this.filePath = filePath;
        }

        protected String filePath = "";
    }
}



