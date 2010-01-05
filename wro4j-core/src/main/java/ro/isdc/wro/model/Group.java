/*
 * Copyright (c) 2008. All rights reserved.
 */
package ro.isdc.wro.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.exception.WroRuntimeException;
import ro.isdc.wro.resource.Resource;

/**
 * A group is an entity which gather a list of resources.
 *
 * @author Alex Objelean
 * @created Created on Oct 30, 2008
 */
public final class Group {
  private static final Logger LOG = LoggerFactory.getLogger(Group.class);
  /**
   * Group name.
   */
  private String name;

  /**
   * Resources of the group.
   */
  private List<Resource> resources = new ArrayList<Resource>();

  /**
   * @return the name
   */
  public final String getName() {
    return name;
  }

  /**
   * @param name
   *          the name to set
   */
  public final void setName(final String name) {
    this.name = name;
  }

  /**
   * @return the resources
   */
  public final List<Resource> getResources() {
    return resources;
  }

  /**
   * @param resources
   *          the resources to set
   */
  public final void setResources(final List<Resource> resources) {
    this.resources = resources;
    for (final Resource resource : resources) {
      resource.setGroup(this);
    }
  }


  /**
   * Allow change of the resource list. Useful when the model is changed due to content of some resource (like
   * CssImportPreProcessor).
   * <p>
   * Inserts a new resource immediately before existent resource.
   *
   * @param resourceToInsert new {@link Resource} to be inserted in the list.
   * @param resource before which the new {@link Resource} will be inserted.
   */
  public final void insertResourceBefore(final Resource resourceToInsert, final Resource resource) {
    if (resourceToInsert == null) {
      throw new IllegalArgumentException("newResource cannot be NULL");
    }
    if (resource == null) {
      throw new IllegalArgumentException("resource cannot be NULL");
    }
    final int index = getResources().indexOf(resource);
    try {
      getResources().listIterator(index).add(resourceToInsert);
    } catch (final IndexOutOfBoundsException e) {
      throw new WroRuntimeException("The passed resource (" + resource + ") could not be found in the list of this group (" + this + ")");
    }
  }


  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE).append("name", getName()).append("resources", getResources()).toString();
  }

}
