/**
 * Copyright wro4j@2011
 */
package ro.isdc.wro.model.resource.processor.decorator;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import ro.isdc.wro.model.group.processor.Minimize;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.SupportedResourceType;
import ro.isdc.wro.model.resource.processor.Destroyable;
import ro.isdc.wro.model.resource.processor.ImportAware;
import ro.isdc.wro.model.resource.processor.MinimizeAware;
import ro.isdc.wro.model.resource.processor.ResourceProcessorAware;
import ro.isdc.wro.model.resource.processor.SupportAware;
import ro.isdc.wro.model.resource.processor.SupportedResourceTypeAware;
import ro.isdc.wro.util.AbstractDecorator;


/**
 * Hides details common to all processors decorators, like ability to identify if a processor is minimize aware
 *
 * @author Alex Objelean
 * @since 1.4.6
 */
public abstract class AbstractProcessorDecoratorSupport<T>
    extends AbstractDecorator<T>
    implements ResourceProcessorAware {
  /**
   * @param decorated
   *          the decorated processor. The type of the returned object is {@link Object} because we don't really care and we
   *          need it only to check if the processor is minimize aware and get its supported type. This "hack" will e
   *          removed in 1.5.0.
   */
  public AbstractProcessorDecoratorSupport(final T decorated) {
    super(decorated);
  }

  /**
   * This method is final, because it intends to preserve the getSupportedResourceType flag of the decorated processor.
   * You still can override this behavior by implementing
   * {@link AbstractProcessorDecoratorSupport#getSupportedResourceTypeInternal()} on your own risk.
   *
   * @return the {@link SupportedResourceType} annotation of the decorated processor if one exist.
   */
  @Override
  public final SupportedResourceType getSupportedResourceType() {
    return getSupportedResourceTypeInternal();
  }

  /**
   * Allow subclass override the way getSupportedResourceType is used.
   */
  protected SupportedResourceType getSupportedResourceTypeInternal() {
    return getSupportedResourceTypeForProcessor(getDecoratedObject());
  }

  /**
   * Computes {@link SupportedResourceType} for provided processor.
   */
  final SupportedResourceType getSupportedResourceTypeForProcessor(final Object processor) {
    SupportedResourceType supportedType = processor.getClass().getAnnotation(SupportedResourceType.class);
    /**
     * This is a special case for processors which implement {@link SupportedResourceTypeProvider} interface. This is
     * useful for decorator processors which needs to "inherit" the {@link SupportedResourceType} of the decorated
     * processor.
     */
    if (processor instanceof SupportedResourceTypeAware) {
      supportedType = ((SupportedResourceTypeAware) processor).getSupportedResourceType();
    }
    return supportedType;
  }

  /**
   * This method is final, because it intends to preserve the minimize flag of the decorated processor. You still can
   * override this behavior by implementing {@link AbstractProcessorDecoratorSupport#isMinimizeInternal()} on your own
   * risk.
   */
  @Override
  public final boolean isMinimize() {
    return isMinimizeInternal();
  }

  /**
   * Allow subclass override the way isMinimized is used.
   */
  protected boolean isMinimizeInternal() {
    return isMinimizeForProcessor(getDecoratedObject());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isSupported() {
    return getDecoratedObject() instanceof SupportAware ? ((SupportAware) getDecoratedObject()).isSupported() : true;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isImportAware() {
    return getDecoratedObject() instanceof ImportAware ? ((ImportAware) getDecoratedObject()).isImportAware() : false;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void destroy()
      throws Exception {
    if (getDecoratedObject() instanceof Destroyable) {
      ((Destroyable) getDecoratedObject()).destroy();
    }
  }

  /**
   * @return true if passed processor is minimize aware.
   */
  final boolean isMinimizeForProcessor(final Object processor) {
    if (processor instanceof MinimizeAware) {
      return ((MinimizeAware) processor).isMinimize();
    }
    return processor.getClass().isAnnotationPresent(Minimize.class);
  }

  /**
   * @return the array of supported resources the processor can process.
   */
  public final ResourceType[] getSupportedResourceTypes() {
    final SupportedResourceType supportedType = getSupportedResourceType();
    return supportedType == null ? ResourceType.values() : new ResourceType[] {
      supportedType.value()
    };
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public final void process(final Reader reader, final Writer writer)
      throws IOException {
    process(null, reader, writer);
  }
}
