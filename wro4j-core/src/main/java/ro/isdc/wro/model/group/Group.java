/*
 * Copyright (c) 2008. All rights reserved.
 */
package ro.isdc.wro.model.group;

import java.util.ArrayList;
import java.util.List;

import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.ResourceType;


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
   * Check if the group has at least one resource of some type.
   *
   * @param resourceType type of the searched resource.
   * @return true if at least one resource of some type exists.
   */
  public final boolean hasResourcesOfType(final ResourceType resourceType) {
    if (resourceType == null) {
      throw new IllegalArgumentException("ResourceType cannot be null!");
    }
    for (final Resource resource : resources) {
      if (resourceType.equals(resource.getType())) {
        return true;
      }
    }
    return false;
  }


  /**
   * @return the name
   */
  public final String getName() {
    return name;
  }


  /**
   * @param name the name to set
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
   * @param resources the resources to set
   */
  public final void setResources(final List<Resource> resources) {
    this.resources = resources;
  }


  /**
   * {@inheritDoc}
   */
  @Override
  public boolean equals(final Object obj) {
    // Because name is unique, we can consider two groups are equals if their name is the same
    if (obj instanceof Group) {
      final Group group = (Group)obj;
      return getName().equals(group.getName());
    }
    return false;
  }


  /**
   * {@inheritDoc}
   */
  @Override
  public int hashCode() {
    int hash = 7;
    hash = 31 * hash + getName().hashCode();
    hash = 31 * hash + getResources().hashCode();
    return hash;
  }


  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    return getName();
  }
}
