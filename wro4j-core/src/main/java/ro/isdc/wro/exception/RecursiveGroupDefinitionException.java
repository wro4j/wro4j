/*
 * Copyright (c) 2008 ISDC! Romania. All rights reserved.
 */
package ro.isdc.wro.exception;

/**
 * This exception is thrown when the group a recursively referring each-other.
 * 
 * @author alexandru.objelean / ISDC! Romania
 * @version $Revision: $
 * @date $Date: $
 * @created Created on Nov 10, 2008
 */
public class RecursiveGroupDefinitionException extends WroRuntimeException {
  /**
   * serialVersionUID
   */
  private static final long serialVersionUID = 1L;

  /**
   * Default constructor.
   */
  public RecursiveGroupDefinitionException() {}

  /**
   * @param message
   * @param cause
   */
  public RecursiveGroupDefinitionException(final String message,
      final Throwable cause) {
    super(message, cause);
  }

  /**
   * @param message
   */
  public RecursiveGroupDefinitionException(final String message) {
    super(message);
  }

  /**
   * @param cause
   */
  public RecursiveGroupDefinitionException(final Throwable cause) {
    super(cause);
  }

}
