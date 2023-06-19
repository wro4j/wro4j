/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.model.resource.processor.impl.css;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.config.jmx.WroConfiguration;
import ro.isdc.wro.model.group.Inject;
import ro.isdc.wro.model.group.processor.PreProcessorExecutor;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.SupportedResourceType;
import ro.isdc.wro.model.resource.processor.support.CssImportInspector;
import ro.isdc.wro.model.resource.processor.support.ProcessingCriteria;
import ro.isdc.wro.model.resource.processor.support.ProcessingType;


/**
 * <p>CssImport Processor responsible for handling css <code>@import</code> statement. It is implemented as both:
 * preProcessor and postProcessor. It is necessary because preProcessor is responsible for updating model with found
 * imported resources, while post processor removes import occurrences.</p>
 *
 * <p>When processor finds an import which is not valid, it will check the
 * {@link WroConfiguration#isIgnoreMissingResources()} flag. If it is set to false, the processor will fail.</p>
 *
 * @author Alex Objelean
 */
@SupportedResourceType(ResourceType.CSS)
public class CssImportPreProcessor
  extends AbstractCssImportPreProcessor {
  private static final Logger LOG = LoggerFactory.getLogger(CssImportPreProcessor.class);

  public static final String ALIAS = "cssImport";
  @Inject
  private PreProcessorExecutor preProcessorExecutor;

  @Override
  protected String doTransform(final String cssContent, final List<Resource> foundImports)
      throws IOException {
    final StringBuilder sb = new StringBuilder();
    // for now, minimize always
    // TODO: find a way to get minimize property dynamically.
    sb.append(preProcessorExecutor.processAndMerge(foundImports,
        ProcessingCriteria.create(ProcessingType.IMPORT_ONLY, false)));
    if (!foundImports.isEmpty()) {
      LOG.debug("Imported resources found : {}", foundImports.size());
    }
    sb.append(cssContent);
    LOG.debug("importsCollector: {}", foundImports);
    return removeImportStatements(sb.toString());
  }

  /**
   * @return the css content with all css import statements removed.
   */
  protected String removeImportStatements(final String cssContent) {
    return new CssImportInspector(cssContent).removeImportStatements();
  }
}
