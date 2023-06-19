/*
 * Copyright (c) 2008. All rights reserved.
 */
package ro.isdc.wro.model.resource.processor.impl;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;
import ro.isdc.wro.util.WroUtil;


/**
 * Removes multi line comments from processed resource.
 *
 * @author Alex Objelean
 */
public class MultiLineCommentStripperProcessor
  implements ResourcePreProcessor, ResourcePostProcessor {
  /**
   * Pattern containing a regex matching multiline comments and empty new lines.
   */
  public static final Pattern PATTERN = Pattern.compile(WroUtil.loadRegexpWithKey("comment.multiline"));
  public static final String ALIAS = "multilineStripper";

  public void process(final Resource resource, final Reader source, final Writer destination)
    throws IOException {
    try {
      final String content = IOUtils.toString(source);
      String result = PATTERN.matcher(content).replaceAll(StringUtils.EMPTY);
      result = WroUtil.EMTPY_LINE_PATTERN.matcher(result).replaceAll(StringUtils.EMPTY);
      destination.write(result);
    } finally {
      source.close();
      destination.close();
    }
  }

  public void process(final Reader reader, final Writer writer)
    throws IOException {
    // resourceUri doesn't matter
    process(null, reader, writer);
  }
}
