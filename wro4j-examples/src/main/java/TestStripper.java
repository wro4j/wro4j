import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.Writer;

import org.apache.commons.io.IOUtils;

import ro.isdc.wro.model.resource.processor.algorithm.ResourceContentStripper;

/*
 * Copyright (c) 2008 ISDC! Romania. All rights reserved.
 */

/**
 * TestStripper.java.
 * 
 * @author alexandru.objelean / ISDC! Romania
 * @version $Revision: $
 * @date $Date: $
 * @created Created on Nov 25, 2008
 */
public class TestStripper {
  public static void main(final String[] args) throws IOException {
    final ClassLoader classLoader = Thread.currentThread()
        .getContextClassLoader();
    final InputStream is = classLoader.getResourceAsStream("js/jquery.js");
    final Writer writer = new StringWriter();
    IOUtils.copy(is, writer);
    final String result = ResourceContentStripper
        .stripCommentsAndWhitespace(writer.toString());
    System.out.println(result);
  }
}
