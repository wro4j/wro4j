package ro.isdc.wro.extensions.processor;

import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import ro.isdc.wro.extensions.processor.js.RJSProcessor;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;


public class TestRJSProcessor {

  @Test
  public void shouldTransformSubmodules() throws Exception {
    URL baseURL = getClass().getResource("/test/requirejs");
    String filename = "/test/requirejs/testrequire.js";
    URL file = getClass().getResource(filename);
    ResourcePreProcessor processor = new RJSProcessor(baseURL.getFile(), Collections.<String,String>emptyMap());
    StringWriter resultWriter = new StringWriter();
    processor.process(
        Resource.create("file:" + file.getPath(), ResourceType.JS),
        new InputStreamReader(getClass().getResourceAsStream(filename)),
        resultWriter);
    String expected = IOUtils.toString(getClass().getResource("/test/requirejs/testrequire-expected.js"));
    Assert.assertEquals(expected, resultWriter.toString());
  }

  @Test
  public void shouldTransformSubmodulesInSubdirectories() throws Exception {
    URL baseURL = getClass().getResource("/test/requirejs");
    String filename = "/test/requirejs/views/testrequire.js";
    URL file = getClass().getResource(filename);
    ResourcePreProcessor processor = new RJSProcessor(baseURL.getFile(), Collections.<String,String>emptyMap());
    StringWriter resultWriter = new StringWriter();
    processor.process(
        Resource.create("file:" + file.getPath(), ResourceType.JS),
        new InputStreamReader(getClass().getResourceAsStream(filename)),
        resultWriter);
    String expected = IOUtils.toString(getClass().getResource("/test/requirejs/testrequire-expected.js"));
    Assert.assertEquals(expected, resultWriter.toString());
  }

  @Test
  public void shouldUsePathMappings() throws Exception {
    URL baseURL = getClass().getResource("/test/requirejs");
    String filename = "/test/requirejs/testpaths.js";
    URL file = getClass().getResource(filename);
    Map<String,String> paths = new HashMap<String,String>(){{ put("underscore", "lib/underscore"); }};
    ResourcePreProcessor processor = new RJSProcessor(baseURL.getFile(), paths);
    StringWriter resultWriter = new StringWriter();
    processor.process(
        Resource.create("file:" + file.getPath(), ResourceType.JS),
        new InputStreamReader(getClass().getResourceAsStream(filename)),
        resultWriter);
    String expected = IOUtils.toString(getClass().getResource("/test/requirejs/testpaths-expected.js"));
    Assert.assertEquals(expected, resultWriter.toString());
  }

  @Test
  public void shouldUsePlugins() throws Exception {
    URL baseURL = getClass().getResource("/test/requirejs");
    String filename = "/test/requirejs/testplugins.js";
    URL file = getClass().getResource(filename);
    Map<String,String> paths = new HashMap<String,String>(){{
      put("handlebars", "lib/handlebars");
      put("underscore", "lib/underscore");
      put("hbs", "lib/hbs");
    }};
    ResourcePreProcessor processor = new RJSProcessor(baseURL.getFile(), paths,
        "pragmasOnSave.excludeHbsParser=true",
        "pragmasOnSave.excludeHbs=true");
    StringWriter resultWriter = new StringWriter();
    processor.process(
        Resource.create("file:" + file.getPath(), ResourceType.JS),
        new InputStreamReader(getClass().getResourceAsStream(filename)),
        resultWriter);
    String expected = IOUtils.toString(getClass().getResource("/test/requirejs/testplugins-expected.js"));
    Assert.assertEquals(expected, resultWriter.toString());
  }
}
