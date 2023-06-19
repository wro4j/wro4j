/*
 * Copyright (c) 2008. All rights reserved.
 */
package ro.isdc.wro.model.group;

import ro.isdc.wro.WroRuntimeException;

/**
 * This exception is thrown when the group a recursively referring each-other.
 *
 * @author Alex Objelean
 */
public class RecursiveGroupDefinitionException extends WroRuntimeException {
  /**
   * serialVersionUID
   */
  private static final long serialVersionUID = 1L;

  /**
   * @param message
   */
  public RecursiveGroupDefinitionException(final String message) {
    super(message);
  }
}
