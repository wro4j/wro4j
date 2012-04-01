/*
 * Copyright (c) 2008. All rights reserved.
 */
package ro.isdc.wro.model.group;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
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
   * Flag indicating if this group used for processing. Require to achieve lazy group processing functionality. By
   * default this value is false (not processed). The group can be marked as used by invoking {@link Group#markAsUsed()}
   */
  private boolean used = false;
  /**
   * Group name.
   */
  private String name;

  /**
   * Resources of the group.
   */
  private final List<Resource> resources = new ArrayList<Resource>();

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
   * Mark this group as being used. Once the used flag was changed, it can be reverted.
   */
  public void markAsUsed() {
    LOG.debug("Marking group [{}] as used", this.name);
    used = true;
  }
  
  
  /**
   * @return true if this group was used for processing.
   */
  public boolean isUsed() {
    return used;
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
   * Replace one resource with a list of other resources. The use case is related to wildcard expander functionality,
   * when resources containing wildcard are replaced with a list of wildcard-free resources. The order of resources is
   * preserved.
   *
   * @param resource to replace.
   * @param expandedResources a list of resources to replace. If this list is empty, the result is similar to removing
   *        the resource from the group.
   * @throws IllegalArgumentException when a missing resources is to be replaced.
   */
  public void replace(final Resource resource, final List<Resource> expandedResources) {
    LOG.debug("replacing resource {} with expanded resources: {}", resource, expandedResources);
    Validate.notNull(resource);
    Validate.notNull(expandedResources);
    boolean found = false;
    final List<Resource> result = new ArrayList<Resource>();
    for (final Resource resourceItem : resources) {
      if (resourceItem.equals(resource)) {
        found = true;
        for (final Resource expandedResource : expandedResources) {
          //preserve minimize flag.
          expandedResource.setMinimize(resource.isMinimize());
          //use only resources which do not already exist in the group
          if (!hasResource(expandedResource)) {
            result.add(expandedResource);
          }
        }
      } else {
        result.add(resourceItem);
      }
    }
    //if no resources found, an invalid replace is performed
    if (!found) {
      throw new IllegalArgumentException("Cannot replace resource: " + resource + " for group: " + this
          + " because the resource is not a part of this group.");
    }
    //update resources with newly built list.
    setResources(result);
  }

  /**
   * @param type
   *          of resources to collect.
   * @return a group containing filtered resources. The created group has the same name.
   */
  public final Group collectResourcesOfType(final ResourceType type) {
    final List<Resource> allResources = new ArrayList<Resource>();
    allResources.addAll(getResources());

    // retain only resources of needed type
    final List<Resource> filteredResources = new ArrayList<Resource>();
    for (final Resource resource : getResources()) {
      if (type == resource.getType()) {
        if (filteredResources.contains(resource)) {
          LOG.warn("Duplicated resource detected: " + resource + ". This resource won't be included more than once!");
        } else {
          filteredResources.add(resource);
        }
      }
    }
    
    final Group filteredGroup = new Group(getName());
    filteredGroup.setResources(filteredResources);
    return filteredGroup;
  }
  
  /**
   * @return the readonly list of resources.
   */
  public List<Resource> getResources() {
    // use a new list to avoid ConcurrentModificationException when the Group#replace method is called.
    return Collections.unmodifiableList(new ArrayList<Resource>(resources));
  }

  /**
   * Add a {@link Resource} to the collection of resources associated with this group.
   *
   * @param resource
   * @return
   */
  public Group addResource(final Resource resource) {
    Validate.notNull(resource);
    if (!hasResource(resource)) {
      resources.add(resource);
    } else {
      LOG.warn("Resource {} is already contained in this group, skiping it.", resource);
    }
    return this;
  }

  /**
   * This method will replace all earlier defined resources with the provided list of resources.
   *
   * @param resources
   *          the resources to set.
   */
  public final void setResources(final List<Resource> resources) {
    Validate.notNull(resources);
    this.resources.clear();
    for (final Resource resource : resources) {
      addResource(resource);
    }
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
   * @return the name
   */
  public String getName() {
    return name;
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
