/*
 * Copyright (c) 2008 ISDC! Romania. All rights reserved.
 */
package ro.isdc.wro.processor.impl;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import org.apache.commons.io.IOUtils;

import ro.isdc.wro.processor.ResourcePostProcessor;
import ro.isdc.wro.processor.ResourcePreProcessor;
import ro.isdc.wro.resource.Resource;
import ro.isdc.wro.util.WroUtil;

/**
 * CommentStripperProcessor. Removes both type of comments. It uses both: multi
 * line & single line comment strippers.
 *
 * @author alexandru.objelean / ISDC! Romania
 * @version $Revision: $
 * @date $Date: $
 * @created Created on Nov 28, 2008
 */
public class CommentStripperProcessor implements ResourcePreProcessor,
    ResourcePostProcessor {
  /**
   * {@inheritDoc}
   */
  public void process(final Reader source, final Writer destination)
      throws IOException {
    final String content = IOUtils.toString(source);
    // apply single line comment stripper processor first
    String result = SingleLineCommentStripperProcessor.PATTERN.matcher(content)
        .replaceAll("");
    // apply multi line comment stripper processor after
    result = MultiLineCommentStripperProcessor.PATTERN.matcher(result)
        .replaceAll("");
    result = WroUtil.EMTPY_LINE_PATTERN.matcher(result).replaceAll("");
    destination.write(result);
  }

  /**
   * {@inheritDoc}
   */
  public void process(final Resource resource, final Reader reader,
      final Writer writer) throws IOException {
    // resource Uri doesn't matter.
    process(reader, writer);
  }
}
