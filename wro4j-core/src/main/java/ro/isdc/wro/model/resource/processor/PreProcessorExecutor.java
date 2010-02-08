/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.model.resource.processor;

import java.io.IOException;

import ro.isdc.wro.model.resource.Resource;


/**
 * Apply all preProcessor on provided {@link Resource} and returns the result of execution as String.
 * <p>
 * This is useful when you want to preProcess a resource which is not a part of the model (css import use-case).
 *
 * @author Alex Objelean
 */
public interface PreProcessorExecutor {
  /**
   * Execute all the preProcessors on the given resource.
   *
   * @param resource {@link Resource} to preProcess.
   * @return the result of preProcessing as string content.
   * @throws IOException if {@link Resource} cannot be found or any other related errors.
   */
  String execute(Resource resource) throws IOException;
}