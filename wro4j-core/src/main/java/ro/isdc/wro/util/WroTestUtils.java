/*
 * Copyright (c) 2008. All rights reserved.
 */
package ro.isdc.wro.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Collection;

import javax.annotation.processing.Processor;

import junit.framework.Assert;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.FalseFileFilter;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.model.group.processor.Injector;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.factory.SimpleUriLocatorFactory;
import ro.isdc.wro.model.resource.factory.UriLocatorFactory;
import ro.isdc.wro.model.resource.locator.ClasspathUriLocator;
import ro.isdc.wro.model.resource.locator.ServletContextUriLocator;
import ro.isdc.wro.model.resource.locator.UrlUriLocator;
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;
import ro.isdc.wro.model.resource.processor.SimpleProcessorsFactory;


/**
 * WroTestUtils.
 *
 * @author Alex Objelean
 * @created Created on Nov 28, 2008
 */
public class WroTestUtils {
  private static final Logger LOG = LoggerFactory.getLogger(WroTestUtils.class);

  /**
   * Compare contents of two resources (files) by performing some sort of
   * processing on input resource.
   *
   * @param inputResourceUri
   *          uri of the resource to process.
   * @param expectedContentResourceUri
   *          uri of the resource to compare with processed content.
   * @param processor
   *          a closure used to process somehow the input content.
   */
  public static void compareProcessedResourceContents(
      final String inputResourceUri, final String expectedContentResourceUri,
      final ResourceProcessor processor) throws IOException {
    final Reader resultReader = getReaderFromUri(inputResourceUri);
    final Reader expectedReader = getReaderFromUri(expectedContentResourceUri);
    WroTestUtils.compare(resultReader, expectedReader,
        processor);
  }

  private static Reader getReaderFromUri(final String uri) throws IOException {
    // wrap reader with bufferedReader for top efficiency
    return new BufferedReader(new InputStreamReader(createDefaultUriLocatorFactory().locate(uri)));
  }

  private static UriLocatorFactory createDefaultUriLocatorFactory() {
    return new SimpleUriLocatorFactory().addUriLocator(new ServletContextUriLocator()).addUriLocator(
        new ClasspathUriLocator()).addUriLocator(new UrlUriLocator());
  }


  public static InputStream getInputStream(final String uri) throws IOException {
    return createDefaultUriLocatorFactory().locate(uri);
  }

  /**
   * @return the injector
   */
  public static void initProcessor(final ResourcePreProcessor processor) {
    final Injector injector = new Injector(createDefaultUriLocatorFactory(), new SimpleProcessorsFactory().addPreProcessor(processor));
    injector.inject(processor);
  }

  /**
   * @return the injector
   */
  public static void initProcessor(final ResourcePostProcessor processor) {
    final Injector injector = new Injector(createDefaultUriLocatorFactory(), new SimpleProcessorsFactory().addPostProcessor(processor));
    injector.inject(processor);
  }

  /**
   * Compare contents of two resources (files) by performing some sort of processing on input resource.
   *
   * @param inputResourceUri uri of the resource to process.
   * @param expectedContentResourceUri uri of the resource to compare with processed content.
   * @param processor a closure used to process somehow the input content.
   */
  public static void compare(final Reader resultReader, final Reader expectedReader, final ResourceProcessor processor)
    throws IOException {
    final Writer resultWriter = new StringWriter();
    processor.process(resultReader, resultWriter);
    final Writer expectedWriter = new StringWriter();
    IOUtils.copy(expectedReader, expectedWriter);
    compare(expectedWriter.toString(), resultWriter.toString());
    expectedReader.close();
    expectedWriter.close();
  }


  public static void compare(final InputStream input, final InputStream expected, final ResourceProcessor processor)
    throws IOException {
    compare(new InputStreamReader(input), new InputStreamReader(expected), processor);
  }


  /**
   * Compare if content of expected stream is the same as content of the actual stream.
   *
   * @param expected {@link InputStream} of the expected content.
   * @param actual {@link InputStream} of the actual content.
   * @return true if content of the expected and actual streams are equal.
   */
  public static void compare(final InputStream expected, final InputStream actual)
    throws IOException {
    Assert.assertNotNull(expected);
    Assert.assertNotNull(actual);
    compare(IOUtils.toString(expected), IOUtils.toString(actual));
    expected.close();
    actual.close();
  }


  /**
   * Compares two strings by removing trailing spaces & tabs for correct comparison.
   */
  public static void compare(final String expected, final String actual) {
    Assert.assertEquals(replaceTabsWithSpaces(expected.trim()), replaceTabsWithSpaces(actual.trim()));
  }


  /**
   * Replace tabs with spaces.
   *
   * @param input from where to remove tabs.
   * @return cleaned string.
   */
  private static String replaceTabsWithSpaces(final String input) {
    // replace tabs with spaces
    return input.replaceAll("\\t", "  ").replaceAll("\\r", "");
  }


  /**
   * A convenient way to get {@link InputStream} of some resource relative to a java class. Usage: <code>
   *   getClassRelativeResource(MyClass.class, "someFile.properties");
   * </code> or <code>
   * 	 getClassRelativeResource(MyClass.class, "subfolder/someFile.properties");
   * </code>
   *
   * @param clazz relative to which the resource stream will be returned.
   * @param relativePath path relative to the clazz. This one should not start with a '/'.
   * @return {@link InputStream} for search resource.
   */
  public static InputStream getClassRelativeResource(final Class<?> clazz, final String relativePath) {
    final String packageName = clazz.getPackage().getName().replace('.', '/');
    final String finalPath = packageName + "/" + relativePath;
    return Thread.currentThread().getContextClassLoader().getResourceAsStream(finalPath);
  }


  /**
   * Process and compare files from the same folder. Use the extension to make distinction between the source files
   * (files to process) and target files (files to compare with).
   *
   * @param sourceFolder the folder where the files to compare resides.
   * @param sourceFileExtension the extension of the files used to process.
   * @param targetFileExtension the extension of the files used to compare with the processed result.
   * @param processor {@link ResourceProcessor} to apply on input files.
   * @throws IOException
   */
  public static void compareSameFolderByExtension(final File sourceFolder, final String sourceFileExtension,
    final String targetFileExtension, final ResourceProcessor processor)
    throws IOException {
    compareFromSameFolder(sourceFolder, new WildcardFileFilter("*." + sourceFileExtension),
      Transformers.extensionTransformer(targetFileExtension), processor);
  }

  public static void compareSameFolderByExtension(final File sourceFolder, final String sourceFileExtension,
    final String targetFileExtension, final ResourcePreProcessor processor)
    throws IOException {
    compareFromSameFolder(sourceFolder, new WildcardFileFilter("*." + sourceFileExtension),
      Transformers.extensionTransformer(targetFileExtension), processor);
  }

  /**
   * @see WroTestUtils#compareFromSameFolder(File, IOFileFilter, Transformer, ResourceProcessor) Same as
   *      {@link WroTestUtils#compareSameFolderByExtension(File, String, String, ResourceProcessor)}, but let you define
   *      the way target file name is named.
   *
   * @param sourceFolder
   * @param sourceFileExtension
   * @param toTargetFileName
   * @param processor
   * @throws IOException
   */
  public static void compareSameFolderByExtension(final File sourceFolder, final String sourceFileExtension,
    final Transformer<String> toTargetFileName, final ResourceProcessor processor)
    throws IOException {
    compareFromSameFolder(sourceFolder, new WildcardFileFilter("*." + sourceFileExtension), toTargetFileName, processor);
  }


  /**
   * Process and compare files from the same folder.
   *
   * @param sourceFolder the folder where the files to compare resides.
   * @param sourceFileFilter the {@link IOFileFilter} used to select source files (files to be processed).
   * @param toTargetFileName the {@link Transformer} which creates the name of the target file used to compare with the
   *        source processed content.
   * @param processor {@link ResourceProcessor} to apply on source files.
   * @throws IOException
   */
  public static void compareFromSameFolder(final File sourceFolder, final IOFileFilter sourceFileFilter,
    final Transformer<String> toTargetFileName, final ResourceProcessor processor)
    throws IOException {
    //TODO create adaptor and use it
    final ResourcePreProcessor preProcessor = new ResourcePreProcessor() {
      public void process(final Resource resource, final Reader reader, final Writer writer)
        throws IOException {
        processor.process(reader, writer);
      }
    };
    compareFromSameFolder(sourceFolder, sourceFileFilter, toTargetFileName, preProcessor);
  }

  public static void compareFromSameFolder(final File sourceFolder, final IOFileFilter sourceFileFilter,
    final Transformer<String> toTargetFileName, final ResourcePreProcessor processor)
    throws IOException {
    final Collection<File> files = FileUtils.listFiles(sourceFolder, sourceFileFilter, FalseFileFilter.INSTANCE);
    int processedNumber = 0;
    for (final File file : files) {
      LOG.debug("processing: " + file.getName());
      File targetFile = null;
      try {
        targetFile = new File(sourceFolder, toTargetFileName.transform(file.getName()));
        final InputStream targetFileStream = new FileInputStream(targetFile);
        LOG.debug("comparing with: " + targetFile.getName());
        // ResourceType doesn't matter here
        final ResourceProcessor resourceProcessor = WroUtil.newResourceProcessor(
          Resource.create("file:" + file.getAbsolutePath(), ResourceType.CSS), processor);
        compare(new FileInputStream(file), targetFileStream, resourceProcessor);
        LOG.debug("Compare ... [OK]");
        processedNumber++;
      } catch (final IOException e) {
        LOG.warn("Skip comparison because couldn't find the TARGET file " + targetFile.getPath());
      }
    }
    logSuccess(processedNumber);
  }


  private static void logSuccess(final int size) {
    LOG.debug("===============");
    LOG.debug("Successfully compared: " + size + " files.");
    LOG.debug("===============");
  }


  /**
   * Process and compare the files which a located in different folders.
   *
   * @param sourceFolder
   * @param targetFolder
   * @param fileFilter
   * @param processor
   * @throws IOException
   */
  public static void compareFromDifferentFolders(final File sourceFolder, final File targetFolder,
    final IOFileFilter fileFilter, final ResourceProcessor processor)
    throws IOException {
    compareFromDifferentFolders(sourceFolder, targetFolder, fileFilter, Transformers.noOpTransformer(), processor);
  }


  /**
   * Process and compare the files which a located in different folders.
   *
   * @param sourceFolder folder where the source files are located.
   * @param targetFolder folder where the target files are located.
   * @param fileFilter filter used to select files to process.
   * @param toTargetFileName {@link Transformer} used to identify the target file name based on source file name.
   * @param processor {@link Processor} used to process the source files.
   * @throws IOException
   */
  public static void compareFromDifferentFolders(final File sourceFolder, final File targetFolder,
    final IOFileFilter fileFilter, final Transformer<String> toTargetFileName, final ResourceProcessor processor)
    throws IOException {
    final Collection<File> files = FileUtils.listFiles(sourceFolder, fileFilter, FalseFileFilter.INSTANCE);
    int processedNumber = 0;
    for (final File file : files) {
      File targetFile = null;
      try {
        targetFile = new File(targetFolder, toTargetFileName.transform(file.getName()));
        final InputStream targetFileStream = new FileInputStream(targetFile);
        LOG.debug("processing: " + file.getName());
        compare(new FileInputStream(file), targetFileStream, processor);
        LOG.debug("Compare ... [OK]");
        processedNumber++;
      } catch (final IOException e) {
        LOG.warn("Skip comparison because couldn't find the TARGET file " + targetFile.getPath());
      }
    }
    logSuccess(processedNumber);
  }
}
