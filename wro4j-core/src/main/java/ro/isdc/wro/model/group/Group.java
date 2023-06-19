/*
 * Copyright (c) 2008. All rights reserved.
 */
package ro.isdc.wro.model.group;

import static org.apache.commons.lang3.Validate.notNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.ResourceType;


/**
 * A group is an entity holding a list of resources. This class is thread safe.
 *
 * @author Alex Objelean
 */
public final class Group implements Comparable<Group> {
  private static final Logger LOG = LoggerFactory.getLogger(Group.class);
  /**
   * Group name.
   */
  private String name;

  /**
   * Resources of the group.
   */
  private final List<Resource> resources = Collections.synchronizedList(new ArrayList<Resource>());

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
    notNull(name, "Group name cannot be null.");
    this.name = name;
  }

  /**
   * Check if the group has at least one resource of some type.
   *
   * @param resourceType
   *          type of the searched resource.
   * @return true if at least one resource of some type exists.
   */
  public final boolean hasResourcesOfType(final ResourceType resourceType) {
    notNull(resourceType, "ResourceType cannot be null!");
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
   * @return true if a {@link Resource} with an uri same as resourceUri is contained in this group.
   */
  public boolean hasResource(final String resourceUri) {
    for (final Resource resource : resources) {
      if (resource.getUri().equals(resourceUri)) {
        return true;
      }
    }
    return false;
  }

  /**
   * <p>Replace one resource with a list of other resources. The use case is related to wildcard expander functionality,
   * when resources containing wildcard are replaced with a list of wildcard-free resources. The order of resources is
   * preserved.</p>
   *
   * <p>The implementation is synchronized, because it mutates the collection.</p>
   *
   * @param resource
   *          to replace.
   * @param expandedResources
   *          a list of resources to replace. If this list is empty, the result is similar to removing the resource from
   *          the group.
   * @throws IllegalArgumentException
   *           when a missing resources is to be replaced.
   */
  public void replace(final Resource resource, final List<Resource> expandedResources) {
    LOG.debug("replacing resource {} with expanded resources: {}", resource, expandedResources);
    notNull(resource);
    notNull(expandedResources);
    synchronized (this) {
      boolean found = false;
      // use set to avoid duplicates
      final Set<Resource> result = new LinkedHashSet<Resource>();
      for (final Resource resourceItem : resources) {
        if (resourceItem.equals(resource)) {
          found = true;
          for (final Resource expandedResource : expandedResources) {
            // preserve minimize flag.
            expandedResource.setMinimize(resource.isMinimize());
            result.add(expandedResource);
          }
        } else {
          result.add(resourceItem);
        }
      }
      // if no resources found, an invalid replace is performed
      if (!found) {
        throw new IllegalArgumentException("Cannot replace resource: " + resource + " for group: " + this
            + " because the resource is not a part of this group.");
      }
      // update resources with newly built list.
      setResources(new ArrayList<Resource>(result));
    }
  }

  /**
   * @param type
   *          of resources to collect. This value should not be null.
   * @return a new group with the same name as original containing filtered resources. The created group has the same
   *         name.
   */
  public final Group collectResourcesOfType(final ResourceType type) {

    notNull(type);

    // retain only resources of needed type
    final List<Resource> filteredResources = new ArrayList<>();
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
   * <p>Add a {@link Resource} to the collection of resources associated with this group.</p>
   *
   * <p>The implementation is synchronized, because it mutates the collection.</p>
   *
   * @param resource
   *          {@link Resource} to add to this group (at the end).
   * @return the reference to Group (fluent interface).
   */
  public Group addResource(final Resource resource) {
    notNull(resource);
    synchronized (this) {
      if (!hasResource(resource)) {
        resources.add(resource);
      } else {
        LOG.debug("Resource {} is already contained in this group, skipping it.", resource);
      }
    }
    return this;
  }

  /**
   * <p>This method will replace all earlier defined resources with the provided list of resources.</p>
   *
   * <p>The implementation is synchronized, because it mutates the collection.</p>
   *
   * @param resources
   *          the resources to replace the underlying resources.
   */
  public final void setResources(final List<Resource> resources) {
    notNull(resources);
    synchronized (this) {
      this.resources.clear();
      for (final Resource resource : resources) {
        addResource(resource);
      }
    }
  }

  /**
   * @return the name
   */
  public String getName() {
    return name;
  }

  @Override
  public int compareTo(final Group o) {
    return getName().compareTo(o.getName());
  }

  @Override
  public boolean equals(final Object obj) {
    // Because name is unique, we can consider two groups are equals if their name is the same
    if (obj instanceof Group) {
      final Group group = (Group) obj;
      return getName().equals(group.getName());
    }
    return false;
  }

  @Override
  public int hashCode() {
    return getName().hashCode();
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
  }
}
