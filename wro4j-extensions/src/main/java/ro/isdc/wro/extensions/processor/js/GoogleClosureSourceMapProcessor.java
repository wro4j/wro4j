package ro.isdc.wro.extensions.processor.js;

import java.io.IOException;
import java.io.Writer;

import com.google.javascript.jscomp.CompilationLevel;
import com.google.javascript.jscomp.Compiler;

import ro.isdc.wro.model.resource.ResourceType;

public class GoogleClosureSourceMapProcessor extends AbstractGoogleClosureProcessor<GoogleClosureSourceMapProcessor> {
    public static final String ALIAS_SOURCEMAP = ResourceType.MAP_PROCESSOR;
    
    public GoogleClosureSourceMapProcessor() {
        super();
    }

    public GoogleClosureSourceMapProcessor(CompilationLevel compilationLevel) {
        super(compilationLevel);
    }

    @Override
    void writeResults(Compiler compiler, String fileName, Writer writer) throws IOException {
        // TODO enable offsets
        compiler.getSourceMap().appendTo(writer, fileName);
    }

}