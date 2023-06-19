/*
 * Copyright (C) 2011.
 * All rights reserved.
 */
package ro.isdc.wro.util;

/**
 * Classic factory interface.
 *
 * @author Alex Objelean
 * @since 1.3.8
 */
public interface ObjectFactory<T> {
  T create();
}
