/**
 * Copyright@2010 Alex Objelean
 */
package ro.isdc.wro.extensions.processor.js;

import java.io.IOException;
import java.io.Writer;

import com.google.javascript.jscomp.CompilationLevel;
import com.google.javascript.jscomp.Compiler;

import ro.isdc.wro.config.Context;
import ro.isdc.wro.model.group.processor.Minimize;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.SupportedResourceType;

/**
 * Uses Google closure compiler for js minimization.
 * <p/>
 * In order to make this class {@link Context} agnostic, set the encoding
 * explicitly using
 * {@link GoogleClosureCompressorProcessor#setEncoding(String)}.
 *
 * @see http://blog.bolinfest.com/2009/11/calling-closure-compiler-from-java.html
 * @author Alex Objelean
 */
@Minimize
@SupportedResourceType(ResourceType.JS)
public class GoogleClosureCompressorProcessor extends AbstractGoogleClosureProcessor<GoogleClosureCompressorProcessor> {
    public static final String ALIAS_SIMPLE = "googleClosureSimple";
    public static final String ALIAS_ADVANCED = "googleClosureAdvanced";
    public static final String ALIAS_WHITESPACE_ONLY = "googleClosureWhitespace";

    public GoogleClosureCompressorProcessor() {
        super();
    }

    public GoogleClosureCompressorProcessor(CompilationLevel compilationLevel) {
        super(compilationLevel);
    }

    @Override
    protected void writeResults(Compiler compiler, String fileName, Writer writer) throws IOException {
        writer.write(compiler.toSource());
    }
}
