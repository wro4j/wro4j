/*
 * Copyright (c) 2008. All rights reserved.
 */
package ro.isdc.wro.model.resource.processor;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;

import org.apache.commons.io.input.ReaderInputStream;
import org.apache.commons.io.output.WriterOutputStream;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.config.Context;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.processor.impl.js.JSMinProcessor;
import ro.isdc.wro.model.resource.processor.support.JSMin;
import ro.isdc.wro.util.WroTestUtils;


/**
 * TestJsMinProcessor.
 *
 * @author Alex Objelean
 * @created Created on Nov 28, 2008
 */
public class TestJsMinProcessor {
  private ResourcePreProcessor processor;
  @Before
  public void setUp() {
    processor = new JSMinProcessor();
    Context.set(Context.standaloneContext());
    WroTestUtils.createInjector().inject(processor);
  }

  @After
  public void tearDown() {
    Context.unset();
  }

  @Test
  public void testFromFolder()
    throws IOException {
    final URL url = getClass().getResource("jsmin");

    final File testFolder = new File(url.getFile(), "test");
    final File expectedFolder = new File(url.getFile(), "expected");
    WroTestUtils.compareFromDifferentFoldersByExtension(testFolder, expectedFolder, "js", processor);
  }


  /**
   * Tests a known issue with JsMin.
   */
  @Test(expected = WroRuntimeException.class)
  public void shoudlFailWhenCompilingAnExpressionWithNewLines()
      throws Exception {
    processor.process(null, new StringReader("Math.round(4\n/3);"), new StringWriter());

  }

  @Test
  public void shouldSupportCorrectResourceTypes() {
    WroTestUtils.assertProcessorSupportResourceTypes(processor, ResourceType.JS);
  }

  @Test
  public void shouldHandleSlashAndIsolatedSingleQuoteInRegexes() throws Exception {
      final String script = "var slashOrDoubleQuote=/[/']/g;";
      assertEquals("\n" + script, jsmin(script));
  }

  @Test
  public void shouldNotMinifyInsideQuasiLiterals() throws Exception {
    assertEquals(
      "\nvar a=`x = y`;",
      jsmin("var a = `x = y`;"));
  }

  @Test
  public void shouldRemoveByteOrderMark() throws Exception {
    assertEquals(
        "\nvar a=1;",
        jsmin("\uFEFFvar a = 1;"));
  }

  @Test
  public void shouldNotRemoveLineBreakBeforeExclamationMark() throws Exception {
    assertEquals(
        "\nvar a=1\n!true\nconsole.log(a)",
        jsmin("var a = 1\n!true\nconsole.log(a)"));
  }

  @Test
  public void shouldNotRemoveLineBreakBeforeTilde() throws Exception {
    assertEquals(
      "\nvar a=1\n~true\nconsole.log(a)",
      jsmin("var a = 1\n~true\nconsole.log(a)"));
  }

  @Test
  public void shouldNotRemoveSpaceBetweenPlusSigns() throws Exception {
    assertEquals(
        "\nconsole.log(1\n+ +1)",
        jsmin("console.log(1\n+ +1)"));
  }

  @Test
  public void shouldNotRemoveSpaceBetweenMinusSigns() throws Exception {
    assertEquals(
        "\nconsole.log(1\n- -1)",
        jsmin("console.log(1\n- -1)"));
  }

  @Test
  public void shouldRemoveInlineComments() throws Exception {
    assertEquals(
        "\nvar r=1;",
        jsmin("var r = 1; // some comment"));
  }

  @Test
  public void shouldRemoveBlockComments() throws Exception {
    assertEquals(
        "\nvar r=1;",
        jsmin("var r = 1; /* some comment */"));
  }

  @Test
  public void shouldNotProcessRegexpAfterOperator() throws Exception {
    assertEquals("\n1+/a  a/;", jsmin("1 + /a  a/;"));
    assertEquals("\n1-/a  a/;", jsmin("1 - /a  a/;"));
    assertEquals("\n1* /a  a/;", jsmin("1 * /a  a/;"));
    assertEquals("\n1/ /a  a/;", jsmin("1 / /a  a/;"));
    // Not sure why this should work, ~ was added in the original JSMin but the expression below is not valid
    // Javascript.
    assertEquals("\n1~/a  a/;", jsmin("1 ~ /a  a/;"));
  }

  @Test
  public void shouldProcessRegexpContainingCurlyBraces() throws Exception {
    assertEquals(
        "\nreturn/\\d{1,2}[\\/\\-]\\d{1,2}[\\/\\-]\\d{2,4}/.test(s);",
        jsmin("return /\\d{1,2}[\\/\\-]\\d{1,2}[\\/\\-]\\d{2,4}/.test(s);"));
  }

  @Test
  public void shouldProcessRegexpContainingSemicolumn() throws Exception {
    assertEquals(
        "\nreturn/a;/.test(s);",
        jsmin("return /a;/.test(s);"));
  }

  @Test(expected = Exception.class)
  public void shouldFailOnInlineCommentAfterUnclosedRegexp() throws Exception {
    // Make this fail to be consistent with the original JSMin, although this is in fact valid Javascript if comment is
    // a defined variable.
    jsmin("var r = /a //comment");
  }

  @Test(expected = Exception.class)
  public void shouldFailOnUnclosedBlockCommentAfterUnclosedRegexp() throws Exception {
    // same comment as above
    jsmin("var r = /a/*comment");
  }

  private String jsmin(final String inputScript) throws Exception {
      final StringReader reader = new StringReader(inputScript);
      final InputStream is = new ReaderInputStream(reader, "UTF-8");
      final StringWriter writer = new StringWriter();
      final OutputStream os = new WriterOutputStream(writer, "UTF-8");
      new JSMin(is, os).jsmin();
      return writer.toString();
  }
}
