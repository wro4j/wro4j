/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.processor.impl;

import java.io.IOException;

import ro.isdc.wro.resource.Resource;

public interface PreProcessorExecutor {
  String execute(Resource resource) throws IOException;
}