/*
 * Copyright (c) 2008. All rights reserved.
 */
package ro.isdc.wro.model.group;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.ResourceType;


/**
 * A group is an entity holding a list of resources.
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
   * To be used by JSON serializer.
   */
  public Group() {
  }

  /**
   * Creates a group with a name.
   *
   * @param name
   *          of the group.
   */
  public Group(final String name) {
    Validate.notNull(name, "Group name cannot be null.");
    this.name = name;
  }


  /**
   * Check if the group has at least one resource of some type.
   *
   * @param resourceType type of the searched resource.
   * @return true if at least one resource of some type exists.
   */
  public final boolean hasResourcesOfType(final ResourceType resourceType) {
    Validate.notNull(resourceType, "ResourceType cannot be null!");
    for (final Resource resource : resources) {
      if (resourceType.equals(resource.getType())) {
        return true;
      }
    }
    return false;
  }

  /**
   * @return true if the resourceToCheck is already contained in this group.
   */
  private boolean hasResource(final Resource resourceToCheck) {
    for (final Resource resource : resources) {
      if (resource.equals(resourceToCheck)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Replace one resource with a list of other resources. The use case is related to wildcard exploder functionality,
   * when resources containing wildcards are replaced with simple resources. The order of resources is preserved.
   *
   * @param resource
   *          to replace.
   * @param explodedResources
   *          a list of resources to replace. If this list is empty, the result is similar to removing the resource from
   *          the group.
   * @throws IllegalArgumentException
   *           when a missing resources is to be replaced.
   */
  public void replace(final Resource resource, final List<Resource> explodedResources) {
    LOG.debug("replacing resource \n{} with exploded resources: \n{} for group: \n" + this, resource, explodedResources);
    Validate.notNull(resource);
    Validate.notNull(explodedResources);
    boolean found = false;
    final List<Resource> result = new ArrayList<Resource>();
    for (final Resource resourceItem : resources) {
      if (resourceItem.equals(resource)) {
        found = true;
        for (final Resource explodedResource : explodedResources) {
          //preserve minimize flag.
          explodedResource.setMinimize(resource.isMinimize());
          //use only resources which do not already exist in the group
          if (!hasResource(explodedResource)) {
            result.add(explodedResource);
          }
        }
      } else {
        result.add(resourceItem);
      }
    }
    //if no resources found, an invalid replace is performed
    if (!found) {
      LOG.debug("Cannot replace resource: {} because this resource is not a part of the group: {}", resource, this);
      //Don't throw exception, because transformation is invoked multiple times... TODO find out why
      throw new IllegalArgumentException("Cannot replace resource: " + resource + " for group: " + this
          + " because the resource is not a part of this group.");
    }
    //update resources with newly built list.
    setResources(result);
  }

  /**
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * @return the readonly list of resources.
   */
  public List<Resource> getResources() {
    return Collections.unmodifiableList(resources);
  }

  /**
   * Add a {@link Resource} to the collection of resources associated with this group.
   *
   * @param resource
   * @return
   */
  public Group addResource(final Resource resource) {
    resources.add(resource);
    return this;
  }

  /**
   * This method will replace all earlier defined resources with the provided list of resources.
   *
   * @param resources
   *          the resources to set.
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
    return getName().hashCode();
  }


  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
  }
}
