/*
 * Copyright (c) 2008. All rights reserved.
 */
package ro.isdc.wro.util;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import junit.framework.AssertionFailedError;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.FalseFileFilter;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.io.input.AutoCloseInputStream;
import org.apache.commons.lang3.Validate;
import org.junit.Assert;
import org.junit.ComparisonFailure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.config.Context;
import ro.isdc.wro.manager.factory.BaseWroManagerFactory;
import ro.isdc.wro.manager.factory.WroManagerFactory;
import ro.isdc.wro.model.WroModel;
import ro.isdc.wro.model.factory.WroModelFactory;
import ro.isdc.wro.model.group.processor.Injector;
import ro.isdc.wro.model.group.processor.InjectorBuilder;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.locator.UriLocator;
import ro.isdc.wro.model.resource.locator.factory.AbstractUriLocatorFactory;
import ro.isdc.wro.model.resource.locator.factory.DefaultUriLocatorFactory;
import ro.isdc.wro.model.resource.locator.factory.UriLocatorFactory;
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;
import ro.isdc.wro.model.resource.processor.decorator.ProcessorDecorator;
import ro.isdc.wro.model.resource.processor.factory.SimpleProcessorsFactory;


/**
 * WroTestUtils.
 *
 * @author Alex Objelean
 */
public class WroTestUtils {
  private static final Logger LOG = LoggerFactory.getLogger(WroTestUtils.class);

  /**
   * @return a {@link BaseWroManagerFactory} which uses an empty model.
   */
  public static BaseWroManagerFactory simpleManagerFactory() {
    return new BaseWroManagerFactory().setModelFactory(simpleModelFactory(new WroModel()));
  }

  /**
   * Compare contents of two resources (files) by performing some sort of processing on input resource.
   *
   * @param inputResourceUri
   *          uri of the resource to process.
   * @param expectedContentResourceUri
   *          uri of the resource to compare with processed content.
   * @param processor
   *          a closure used to process somehow the input content.
   */
  public static void compareProcessedResourceContents(final String inputResourceUri,
      final String expectedContentResourceUri, final ResourcePostProcessor processor)
      throws IOException {
    final Reader resultReader = getReaderFromUri(inputResourceUri);
    final Reader expectedReader = getReaderFromUri(expectedContentResourceUri);
    compare(resultReader, expectedReader, processor);
  }

  private static Reader getReaderFromUri(final String uri)
      throws IOException {
    // wrap reader with bufferedReader for top efficiency
    return new BufferedReader(new InputStreamReader(createDefaultUriLocatorFactory().locate(uri)));
  }

  private static UriLocatorFactory createDefaultUriLocatorFactory() {
    return new DefaultUriLocatorFactory();
  }

  public static InputStream getInputStream(final String uri)
      throws IOException {
    return createDefaultUriLocatorFactory().locate(uri);
  }

  public static void init(final WroModelFactory factory) {
    final WroManagerFactory managerFactroy = new BaseWroManagerFactory().setModelFactory(factory);
    InjectorBuilder.create(managerFactroy).build().inject(factory);
  }

  /**
   * @return the injected processor.
   */
  public static ResourcePreProcessor initProcessor(final ResourcePreProcessor processor) {
    final BaseWroManagerFactory factory = new BaseWroManagerFactory();
    factory.setProcessorsFactory(new SimpleProcessorsFactory().addPreProcessor(processor));
    InjectorBuilder.create(factory).build().inject(processor);
    return processor;
  }

  public static void initProcessor(final ResourcePostProcessor processor) {
    initProcessor((ResourcePreProcessor) new ProcessorDecorator(processor));
  }

  /**
   * Compare contents of two resources (files) by performing some sort of processing on input resource.
   *
   * @param resultReader
   *          The reader of the resource to process.
   * @param expectedReader
   *          The expected reader of the resource to compare with processed content.
   * @param processor
   *          a closure used to process somehow the input content.
   */
  public static void compare(final Reader resultReader, final Reader expectedReader,
      final ResourcePostProcessor processor)
      throws IOException {
    final Writer resultWriter = new StringWriter();
    processor.process(resultReader, resultWriter);
    final Writer expectedWriter = new StringWriter();

    IOUtils.copy(expectedReader, expectedWriter);
    compare(expectedWriter.toString(), resultWriter.toString());
    expectedReader.close();
    expectedWriter.close();
  }

  public static void compare(final InputStream input, final InputStream expected, final ResourcePostProcessor processor)
      throws IOException {
    compare(new InputStreamReader(input), new InputStreamReader(expected), processor);
  }

  /**
   * Compare if content of expected stream is the same as content of the actual stream. When compared content is not
   * equal, the assertion error will be thrown.
   *
   * @param expected
   *          {@link InputStream} of the expected content.
   * @param actual
   *          {@link InputStream} of the actual content.
   */
  public static void compare(final InputStream expected, final InputStream actual)
      throws IOException {
    Assert.assertNotNull(expected);
    Assert.assertNotNull(actual);
    final String encoding = Context.get().getConfig().getEncoding();
    compare(IOUtils.toString(expected, encoding), IOUtils.toString(actual, encoding));
    expected.close();
    actual.close();
  }

  /**
   * Compares two strings by removing trailing spaces and tabs for correct comparison.
   */
  public static void compare(final String expected, final String actual) {
    try {
      final String in = replaceTabsWithSpaces(expected.trim());
      final String out = replaceTabsWithSpaces(actual.trim());

      Assert.assertEquals(in, out);
      LOG.debug("Compare.... [OK]");
    } catch (final ComparisonFailure e) {
      LOG.error("Compare.... [FAIL]", e.getMessage());
      throw e;
    }
  }

  /**
   * Replace tabs with spaces.
   *
   * @param input
   *          from where to remove tabs.
   * @return cleaned string.
   */
  private static String replaceTabsWithSpaces(final String input) {
    // replace tabs with spaces
    return input.replaceAll("\\t", "  ").replaceAll("\\r", "");
  }

	public static void compareFromSameFolder(final File sourceFolder, final IOFileFilter sourceFileFilter,
			final Transformer<String> toTargetFileName, final ResourcePreProcessor processor) {

		final Collection<File> files = FileUtils.listFiles(sourceFolder, sourceFileFilter, FalseFileFilter.INSTANCE);
		int processedNumber = 0;

		for (final File file : files) {
			LOG.debug("processing: {}", file.getName());

			File targetFile;
			try {
				targetFile = new File(sourceFolder, toTargetFileName.transform(file.getName()));
			} catch (Exception e) {
				throw WroRuntimeException.wrap(e);
			}

			try (InputStream sourceFileStream = new FileInputStream(file);
					InputStream targetFileStream = new FileInputStream(targetFile)) {

				LOG.debug("comparing with: {}", targetFile.getName());
				try {
					compare(sourceFileStream, targetFileStream, new ResourcePostProcessor() {
						public void process(final Reader reader, final Writer writer) throws IOException {
							// ResourceType doesn't matter here
							processor.process(Resource.create("file:" + file.getPath(), ResourceType.CSS), reader,
									writer);
						}
					});
				} catch (final ComparisonFailure e) {
					LOG.error("Failed comparing: {}", file.getName());
					throw e;
				}
				processedNumber++;
			} catch (final IOException e) {
				LOG.debug("Skip comparison because couldn't find the TARGET file {}. Original cause: {}", targetFile,
						e.getCause());
			} catch (final Exception e) {
				throw WroRuntimeException.wrap(e);
			}
		}

		logSuccess(processedNumber);
	}

  private static void logSuccess(final int size) {
    if (size == 0) {
      throw new IllegalStateException("No files compared. Check if there is at least one resource to compare");
    }
    LOG.debug("===============");
    LOG.debug("Successfully processed: {} files.", size);
    LOG.debug("===============");
  }

  /**
   * Process and compare all the files from the sourceFolder and compare them with the files from the targetFolder.
   */
  public static void compareFromDifferentFolders(final File sourceFolder, final File targetFolder,
      final ResourcePreProcessor processor)
      throws IOException {
    compareFromDifferentFolders(sourceFolder, targetFolder, TrueFileFilter.TRUE, Transformers.noOpTransformer(),
        processor);
  }

  public static void compareFromDifferentFoldersByExtension(final File sourceFolder, final File targetFolder,
      final String extension, final ResourcePreProcessor processor)
      throws IOException {
    compareFromDifferentFolders(sourceFolder, targetFolder, new WildcardFileFilter("*." + extension),
        Transformers.noOpTransformer(), processor);
  }

  /**
   * TODO run tests in parallel
   */
  public static void compareFromDifferentFoldersByExtension(final File sourceFolder, final File targetFolder,
      final String extension, final ResourcePostProcessor processor)
      throws IOException {
    compareFromDifferentFolders(sourceFolder, targetFolder, new WildcardFileFilter("*." + extension),
        Transformers.noOpTransformer(), processor);
  }

  /**
   * Compares files with the same name from sourceFolder against it's counterpart in targetFolder, but allows source and
   * target files to have different extensions. TODO run tests in parallel
   */
  public static void compareFromDifferentFoldersByName(final File sourceFolder, final File targetFolder,
      final String srcExtension, final String targetExtension, final ResourcePostProcessor processor)
      throws IOException {
    compareFromDifferentFolders(sourceFolder, targetFolder, new WildcardFileFilter("*." + srcExtension),
        Transformers.extensionTransformer("css"), processor);
  }

  private static void compareFromDifferentFolders(final File sourceFolder, final File targetFolder,
      final IOFileFilter fileFilter, final Transformer<String> toTargetFileName, final ResourcePostProcessor processor)
      throws IOException {
    // TODO use ProcessorsUtils
    compareFromDifferentFolders(sourceFolder, targetFolder, fileFilter, toTargetFileName, new ResourcePreProcessor() {
      public void process(final Resource resource, final Reader reader, final Writer writer)
          throws IOException {
        processor.process(reader, writer);
      }
    });
  }

  /**
   * Applies a function for each file from a folder. The folder should contain at least one file to process, otherwise
   * an exception will be thrown.
   *
   * @param folder
   *          {@link File} representing the folder where the files will be used from processing.
   * @param function
   *          {@link Function} to apply on each found file.
   */
  public static void forEachFileInFolder(final File folder, final Function<File, Void> function) {
    Validate.notNull(function);
    final Collection<File> files = FileUtils.listFiles(folder, TrueFileFilter.TRUE, FalseFileFilter.INSTANCE);
    int processedNumber = 0;
    for (final File file : files) {
      try {
        function.apply(file);
      } catch (final Exception e) {
        throw new RuntimeException("Problem while applying function on file: " + file, e);
      }
      processedNumber++;
    }
    logSuccess(processedNumber);
  }

  /**
   * Process and compare the files which a located in different folders.
   *
   * @param sourceFolder
   *          folder where the source files are located.
   * @param targetFolder
   *          folder where the target files are located.
   * @param fileFilter
   *          filter used to select files to process.
   * @param toTargetFileName
   *          {@link Transformer} used to identify the target file name based on source file name.
   * @param preProcessor
   *          {@link ResourcePreProcessor} used to process the source files.
   * @throws IOException
   */
  private static void compareFromDifferentFolders(final File sourceFolder, final File targetFolder,
      final IOFileFilter fileFilter, final Transformer<String> toTargetFileName, final ResourcePreProcessor preProcessor)
      throws IOException {
    LOG.debug("Source folder: {}", sourceFolder);
    LOG.debug("Target folder: {}", targetFolder);
    final Collection<File> files = FileUtils.listFiles(sourceFolder, fileFilter, FalseFileFilter.INSTANCE);
    int processedNumber = 0;
    // TODO use WroUtil#runInParallel for running tests faster
    for (final File file : files) {
      File targetFile = null;
      try {
        targetFile = new File(targetFolder, toTargetFileName.transform(file.getName()));
        final InputStream targetFileStream = new AutoCloseInputStream(new FileInputStream(targetFile));
        LOG.debug("=========== processing: {} ===========", file.getName());
        // ResourceType doesn't matter here
        try {
          compare(new AutoCloseInputStream(new FileInputStream(file)), targetFileStream, new ResourcePostProcessor() {
            public void process(final Reader reader, final Writer writer)
                throws IOException {
              // ResourceType doesn't matter here
              ResourceType resourceType = ResourceType.CSS;
              try {
                resourceType = ResourceType.get(FilenameUtils.getExtension(file.getPath()));
              } catch (final IllegalArgumentException e) {
                LOG.warn("Unknown resource type for file: {}, assuming resource type is: {}", file.getPath(),
                    resourceType);
              }
              try {
                preProcessor.process(Resource.create("file:" + file.getPath(), resourceType), reader, writer);
              } catch (final Exception e) {
                LOG.error("Processing failed...", e);
                throw WroRuntimeException.wrap(e, "Processing failed...");
              }
            }
          });
        } catch (final ComparisonFailure e) {
          LOG.error("Failed comparing: {}", file.getName());
          throw e;
        }
        processedNumber++;
      } catch (final IOException e) {
        LOG.debug("Skip comparison because couldn't find the TARGET file {}\n. Original exception: {}", targetFile,
            e.getCause());
      } catch (final Exception e) {
        throw WroRuntimeException.wrap(e, "A problem during transformation occured");
      }
    }
    logSuccess(processedNumber);
  }

  /**
   * Runs a task concurrently. Allows to test thread-safe behavior.
   *
   * @param task
   *          a {@link Callable} to run concurrently.
   * @throws Exception
   *           if any of the executed tasks fails.
   */
  public static void runConcurrently(final Callable<Void> task, final int times)
      throws Exception {
    final ExecutorService service = Executors.newFixedThreadPool(5);
    final List<Future<?>> futures = new ArrayList<Future<?>>();
    for (int i = 0; i < times; i++) {
      futures.add(service.submit(task));
    }
    for (final Future<?> future : futures) {
      future.get();
    }
  }

  @SuppressWarnings("unchecked")
  public static void runConcurrently(final Callable<Void>... tasks) throws Exception {
    final ExecutorService service = Executors.newFixedThreadPool(5);
    final List<Future<?>> futures = new ArrayList<Future<?>>();
    for (final Callable<Void> task : tasks) {
      futures.add(service.submit(task));
    }
    for (final Future<?> future : futures) {
      future.get();
    }
  }

  /**
   * Run the task concurrently 50 times.
   */
  public static void runConcurrently(final Callable<Void> task)
      throws Exception {
    runConcurrently(task, 50);
  }

  /**
   * A blocking operation which waits for the provided function to be true. If the function doesn't return true after a
   * timeout period (expressed in milliseconds), it will throw a {@link RuntimeException}.
   *
   * @param function
   *          a {@link Function} responsible for expressing the until expression. It must return true if the wait
   *          blocking operation should stop.
   * @param timeout
   *          number of milliseconds to wait until expression is true. If this timeout is exceeded, the exception will
   *          be thrown.
   */
  public static void waitUntil(final Function<Void, Boolean> function, final long timeout) {
    final long start = System.currentTimeMillis();
    try {
      while (!function.apply(null)) {
        if (System.currentTimeMillis() - start > timeout) {
          throw new WroRuntimeException("Timeout of " + timeout + "ms exceeded.");
        }
      }
    } catch (final Exception e) {
      throw WroRuntimeException.wrap(e);
    }
  }

  /**
   * @return a default {@link Injector} to be used by test classes.
   */
  public static Injector createInjector() {
    return InjectorBuilder.create(new BaseWroManagerFactory()).build();
  }

  /**
   * Creates a model factory for a given model.
   */
  public static WroModelFactory simpleModelFactory(final WroModel model) {
    Validate.notNull(model);
    return new WroModelFactory() {
      public WroModel create() {
        return model;
      }

      public void destroy() {
      }
    };
  }

  /**
   * Asserts that a processor supports provided resource types.
   */
  public static void assertProcessorSupportResourceTypes(final ResourcePreProcessor processor,
      final ResourceType... expectedResourceTypes) {
    final ResourceType[] actualResourceTypes = new ProcessorDecorator(processor).getSupportedResourceTypes();
    try {
      Assert.assertTrue(Arrays.equals(expectedResourceTypes, actualResourceTypes));
    } catch (final AssertionFailedError e) {
      final String message = "actual resourceTypes: " + Arrays.toString(actualResourceTypes) + ", expected are: "
          + Arrays.toString(expectedResourceTypes);
      LOG.error(message);
      Assert.fail(message);
    }
  }

  /**
   * @return an implementation of {@link UriLocatorFactory} which always return a valid stream which contains the
   *         resource uri as content.
   */
  public static UriLocatorFactory createResourceMockingLocatorFactory() {
    return new AbstractUriLocatorFactory() {
      public UriLocator getInstance(final String uri) {
        return createResourceMockingLocator();
      }
    };
  }

  /**
   * @return an implementation of {@link UriLocator} which always return a valid stream which contains the resource uri
   *         as content.
   */
  public static UriLocator createResourceMockingLocator() {
    return new UriLocator() {
      public InputStream locate(final String uri)
          throws IOException {
        return new ByteArrayInputStream(uri.getBytes());
      }

      public boolean accept(final String uri) {
        return true;
      }
    };
  }
}
