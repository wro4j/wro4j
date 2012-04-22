/*
 * Copyright (C) 2010.
 * All rights reserved.
 */
package ro.isdc.wro.extensions.processor.js;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.extensions.processor.support.requirejs.RJS;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.SupportedResourceType;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;

import com.google.common.collect.Lists;



/**
 * Uses {@link <a href="http://jrburke.github.com/r.js/">r.js</a> }
 * to combine dependencies of a script into one file.
 * 
 */
@SupportedResourceType(ResourceType.JS)
public class RJSProcessor implements ResourcePreProcessor {
  private static final Logger LOG = LoggerFactory.getLogger(RJSProcessor.class);
  public static final String ALIAS = "rjs";
  private final String baseUrl;
  private final List<String> paths;
  private final List<String> additionalOptions = new ArrayList<String>(1);


  /**
   * @param baseUrl parent directory from which all dependencies are based
   * @param pathsMap aliases to dependencies kept in directories
   * @param additionalOptions additional options passed directly as r.js arguments
   */
  public RJSProcessor(String baseUrl, Map<String,String> pathsMap, String ... additionalOptions) {
    this.baseUrl = baseUrl;
    // parse map into commandline format for r.js
    this.paths = new ArrayList<String>(pathsMap.size());
    for (String key : pathsMap.keySet()) {
      paths.add("paths." + key + "=" + pathsMap.get(key));
    }
    // raw additional options
    if (additionalOptions != null) {
      for (String option : additionalOptions) {
        this.additionalOptions.add(option);
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  public void process(final Resource resource, final Reader reader, final Writer writer)
    throws IOException {
    final String content = IOUtils.toString(reader);
    File out = File.createTempFile("rjsout", ".js");
    List<String> args = Lists.newArrayList("-o", "name=" + FilenameUtils.getBaseName(resource.getUri()),
        "baseUrl=" + baseUrl, "out=" + out.getPath(),
        "optimize=none"
    );
    args.addAll(paths);
    args.addAll(additionalOptions);
    try {
      new RJS().compile(args.toArray(new Object[args.size()]));
      writer.write(FileUtils.readFileToString(out));
    } catch (final Exception e) {
      onException(e);
      writer.write(content);
      final String resourceUri = resource == null ? StringUtils.EMPTY : "[" + resource.getUri() + "]";
      LOG.warn("Exception while applying " + getClass().getSimpleName() + " processor on the " + resourceUri
          + " resource, no processing applied...", e);
    } finally {
      reader.close();
      writer.close();
      if (out != null) {
        out.delete();
      }
    }
  }

  /**
   * Invoked when a processing exception occurs.
   */
  protected void onException(final Exception e) {
  }
}
