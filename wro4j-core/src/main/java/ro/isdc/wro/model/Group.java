/*
 * Copyright (c) 2008. All rights reserved.
 */
package ro.isdc.wro.model;

import java.util.ArrayList;
import java.util.List;

import ro.isdc.wro.exception.WroRuntimeException;
import ro.isdc.wro.resource.Resource;
import ro.isdc.wro.resource.ResourceType;

/**
 * A group is an entity holding a list of resources.
 *
 * @author Alex Objelean
 * @created Created on Oct 30, 2008
 */
public final class Group {
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
   * Check if the group has at least one resource of some type.
   *
   * @param resourceType type of the searched resource.
   * @return true if at least one resource of some type exists.
   */
  public final boolean hasResourcesOfType(final ResourceType resourceType) {
    if (resourceType == null) {
      throw new IllegalArgumentException("ResourceType cannot be null!");
    }
    for (final Resource resource : getResources()) {
      if (resourceType.equals(resource.getType())) {
        return true;
      }
    }
    return false;
  }

  /**
   * @param resources
   *          the resources to set
   */
  public final void setResources(final List<Resource> resources) {
    //Use ArrayList to be sure that the list supports all necessary operations.
    this.resources = new ArrayList<Resource>(resources);
    for (final Resource resource : resources) {
      resource.setGroup(this);
    }
  }


  /**
   * FOR INTERNAL USE ONLY!
   * <p>
   * It is very unlikely that you should use this method. Use {@link Resource#prepend(Resource)} instead.
   * <p>
   * Allow change of the resource list. Useful when the model is changed due to content of some resource (like
   * CssImportPreProcessor).
   * <p>
   * Inserts a new resource immediately before existent resource.
   *
   * @param resourceToInsert new {@link Resource} to be inserted in the list.
   * @param resource before which the new {@link Resource} will be inserted.
   */
  //do not allow model change once it was created
  @Deprecated
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
  public boolean equals(final Object obj) {
    //Because name is unique, we can consider two groups are equals if their name is the same
    if (obj instanceof Group) {
      final Group group = (Group) obj;
      return getName().equals(group.getName());
    }
    return false;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    return getName();
  }

}
