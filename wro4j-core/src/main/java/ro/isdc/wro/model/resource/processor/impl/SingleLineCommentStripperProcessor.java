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
 * SingleLineCommentStripperProcessor can be both: preProcessor and postProcessor. Remove single line comments from
 * processed resource. This processor exist only for the sake of proof of concept.
 *
 * @author Alex Objelean
 */
public class SingleLineCommentStripperProcessor
  implements ResourcePreProcessor, ResourcePostProcessor {
  /**
   * Pattern containing a regex matching singleline comments and preceding empty spaces and tabs.
   */
  public static final Pattern PATTERN = Pattern.compile(WroUtil.loadRegexpWithKey("comment.singleline"),
      Pattern.MULTILINE);
  public static final String ALIAS = "singlelineStripper";

  public void process(final Resource resource, final Reader reader, final Writer writer)
    throws IOException {
    try {
      final String content = IOUtils.toString(reader);
      String result = PATTERN.matcher(content).replaceAll(StringUtils.EMPTY);
      result = WroUtil.EMTPY_LINE_PATTERN.matcher(result).replaceAll(StringUtils.EMPTY);
      writer.write(result);
    } finally {
      reader.close();
      writer.close();
    }
  }

  public void process(final Reader reader, final Writer writer)
    throws IOException {
    // resource Uri doesn't matter.
    process(null, reader, writer);
  }
}
