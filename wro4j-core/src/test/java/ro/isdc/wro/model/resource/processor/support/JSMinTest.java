package ro.isdc.wro.model.resource.processor.support;

import org.apache.commons.io.input.ReaderInputStream;
import org.apache.commons.io.output.WriterOutputStream;
import org.junit.Assert;
import org.junit.Test;

import java.io.*;

public class JSMinTest {
    @Test
    public void shouldHandleSlashAndIsolatedSingleQuoteInRegexes() throws Exception {
        String script = "var slashOrDoubleQuote=/[/']/g;";
        Assert.assertEquals("\n" + script, jsmin(script));
    }

    private String jsmin(String inputScript) throws Exception {
        StringReader reader = new StringReader(inputScript);
        InputStream is = new ReaderInputStream(reader, "UTF-8");
        StringWriter writer = new StringWriter();
        OutputStream os = new WriterOutputStream(writer, "UTF-8");
        new JSMin(is, os).jsmin();
        return writer.toString();
    }
}
