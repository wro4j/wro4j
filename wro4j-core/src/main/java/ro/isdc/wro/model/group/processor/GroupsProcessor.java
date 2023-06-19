/*
 * Copyright (c) 2008. All rights reserved.
 */
package ro.isdc.wro.model.group.processor;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Collection;

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.cache.CacheKey;
import ro.isdc.wro.config.ReadOnlyContext;
import ro.isdc.wro.manager.callback.LifecycleCallbackRegistry;
import ro.isdc.wro.model.WroModel;
import ro.isdc.wro.model.WroModelInspector;
import ro.isdc.wro.model.factory.WroModelFactory;
import ro.isdc.wro.model.group.Group;
import ro.isdc.wro.model.group.Inject;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;
import ro.isdc.wro.model.resource.processor.decorator.DefaultProcessorDecorator;
import ro.isdc.wro.model.resource.processor.decorator.ProcessorDecorator;
import ro.isdc.wro.model.resource.processor.factory.ProcessorsFactory;


/**
 * Default group processor which perform preProcessing, merge and postProcessing on groups resources.
 *
 * @author Alex Objelean
 */
public class GroupsProcessor {
  private static final Logger LOG = LoggerFactory.getLogger(GroupsProcessor.class);
  @Inject
  private LifecycleCallbackRegistry callbackRegistry;
  @Inject
  private ProcessorsFactory processorsFactory;
  @Inject
  private WroModelFactory modelFactory;
  @Inject
  private ReadOnlyContext context;
  @Inject
  private Injector injector;

  /**
   * This field is transient because {@link PreProcessorExecutor} is not serializable (according to findbugs eclipse
   * plugin).
   */
  @Inject
  private transient PreProcessorExecutor preProcessorExecutor;

  /**
   * @param cacheKey
   *          to process.
   * @return processed content.
   */
  public String process(final CacheKey cacheKey) {
    Validate.notNull(cacheKey);
    try {
      LOG.debug("Starting processing group [{}] of type [{}] with minimized flag: " + cacheKey.isMinimize(),
          cacheKey.getGroupName(), cacheKey.getType());
      // find processed result for a group
      final WroModel model = modelFactory.create();
      final Group group = new WroModelInspector(model).getGroupByName(cacheKey.getGroupName());
      if (group == null) {
        throw new WroRuntimeException("No such group available in the model: " + cacheKey.getGroupName());
      }
      final Group filteredGroup = group.collectResourcesOfType(cacheKey.getType());
      if (filteredGroup.getResources().isEmpty()) {
        LOG.debug("No resources found in group: {} and resource type: {}", group.getName(), cacheKey.getType());
        if (!context.getConfig().isIgnoreEmptyGroup()) {
          throw new WroRuntimeException("No resources found in group: " + group.getName());
        }
      }
      final String result = preProcessorExecutor.processAndMerge(filteredGroup.getResources(), cacheKey.isMinimize());
      return applyPostProcessors(cacheKey, result);
    } catch (final IOException e) {
      throw new WroRuntimeException("Exception while merging resources: " + e.getMessage(), e).logError();
    } finally {
      callbackRegistry.onProcessingComplete();
    }
  }

  /**
   * Apply resourcePostProcessors.
   *
   * @param cacheKey
   *          the {@link CacheKey} being processed.
   * @param content
   *          to process with all postProcessors.
   * @return the post processed content.
   */
  private String applyPostProcessors(final CacheKey cacheKey, final String content)
      throws IOException {
    final Collection<ResourcePostProcessor> processors = processorsFactory.getPostProcessors();
    LOG.debug("appying post processors: {}", processors);
    if (processors.isEmpty()) {
      return content;
    }
    final Resource resource = Resource.create(cacheKey.getGroupName(), cacheKey.getType());

    Reader reader = new StringReader(content.toString());
    Writer writer = null;
    for (final ResourcePostProcessor processor : processors) {
      final ResourcePreProcessor decoratedProcessor = decorateProcessor(processor, cacheKey.isMinimize());
      writer = new StringWriter();
      decoratedProcessor.process(resource, reader, writer);
      reader = new StringReader(writer.toString());
    }
    return writer.toString();
  }

  /**
   * This method is synchronized to ensure that processor is injected before it is being used by other thread.
   *
   * @return a decorated processor.
   */
  private synchronized ProcessorDecorator decorateProcessor(final ResourcePostProcessor processor,
      final boolean minimize) {
    final ProcessorDecorator decorated = new DefaultProcessorDecorator(processor, minimize) {
      @Override
      public void process(final Resource resource, final Reader reader, final Writer writer)
          throws IOException {
        try {
          callbackRegistry.onBeforePostProcess();
          super.process(resource, reader, writer);
        } finally {
          callbackRegistry.onAfterPostProcess();
        }
      }
    };
    injector.inject(decorated);
    return decorated;
  }

  /**
   * @VisibleForTesting
   */
  final void setPreProcessorExecutor(final PreProcessorExecutor preProcessorExecutor) {
    this.preProcessorExecutor = preProcessorExecutor;
  }

  /**
   * Perform cleanup when taken out of service.
   */
  public void destroy() {
    preProcessorExecutor.destroy();
  }
}
