/*
 * Copyright (c) 2008. All rights reserved.
 */
package ro.isdc.wro.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.model.group.Group;
import ro.isdc.wro.model.group.InvalidGroupNameException;
import ro.isdc.wro.model.resource.Resource;

/**
 * The resource model encapsulates the information about all existing groups.
 *
 * @author Alex Objelean
 * @created Created on Oct 30, 2008
 */
public final class WroModel {
  private static final Logger LOG = LoggerFactory.getLogger(WroModel.class);
  /**
   * Set of groups.
   */
  private Set<Group> groups = new HashSet<Group>();

  /**
   * @return a readonly collection of groups.
   */
  public final Collection<Group> getGroups() {
    return Collections.unmodifiableSet(groups);
  }

  /**
   * @return a set of group names.
   * @deprecated use {@link WroModelInspector#getGroupNames()}
   */
  @Deprecated
  public final List<String> getGroupNames() {
    return new WroModelInspector(this).getGroupNames();
  }

  /**
   * @param groups
   *          the groups to set
   */
  public final WroModel setGroups(final Collection<Group> groups) {
    Validate.notNull(groups, "groups cannot be null!");
    LOG.debug("setGroups: {}", groups);
    identifyDuplicateGroupNames(groups);
    this.groups = new HashSet<Group>(groups);
    return this;
  }

  /**
   * @param resource
   *          the {@link Resource} to search in all available groups.
   * @return t collection of group names containing provided resource. If the resource is not availalbe, an empty
   *         collection will be returned.
   * @deprecated use {@link WroModelInspector#getGroupNamesContainingResource(String)}
   */
  @Deprecated
  public Collection<String> getGroupNamesContainingResource(final String resourceUri) {
    return new WroModelInspector(this).getGroupNamesContainingResource(resourceUri);
  }

  /**
   * Identify duplicate group names.
   *
   * @param groups a collection of group to validate.
   */
  private void identifyDuplicateGroupNames(final Collection<Group> groups) {
    LOG.debug("identifyDuplicateGroupNames");
    final List<String> groupNames = new ArrayList<String>();
    for (final Group group : groups) {
      if (groupNames.contains(group.getName())) {
        throw new WroRuntimeException("Duplicate group name detected: " + group.getName());
      }
      groupNames.add(group.getName());
    }
  }

  /**
   * @param name
   *          of group to find.
   * @return group with searched name.
   * @throws runtime
   *           exception if group is not found.
   * @deprecated use {@link WroModelInspector#getGroupByName(String)}
   */
  @Deprecated
  public Group getGroupByName(final String name) {
    final WroModelInspector modelInspector = new WroModelInspector(this);
    final Group group = modelInspector.getGroupByName(name);
    if (group == null) {
      throw new InvalidGroupNameException(String.format("There is no such group: '%s'. Available groups are: [%s]", name,
          modelInspector.getGroupNamesAsString()));
    }
    return group;
  }

/**
   * Merge this model with another model. This is useful for supporting model imports.
   *
   * @param importedModel model to import.
   */
  public void merge(final WroModel importedModel) {
    Validate.notNull(importedModel, "imported model cannot be null!");
    LOG.debug("merging importedModel: {}", importedModel);
    for (final String groupName : new WroModelInspector(importedModel).getGroupNames()) {
      if (new WroModelInspector(this).getGroupNames().contains(groupName)) {
        throw new WroRuntimeException("Duplicate group name detected: " + groupName);
      }
      final Group importedGroup = new WroModelInspector(importedModel).getGroupByName(groupName);
      addGroup(importedGroup);
    }
  }

  /**
   * Add a single group to the model.
   * @param group a not null {@link Group}.
   */
  public WroModel addGroup(final Group group) {
    Validate.notNull(group);
    groups.add(group);
    return this;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean equals(final Object obj) {
    return EqualsBuilder.reflectionEquals(this, obj, true);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int hashCode() {
    return HashCodeBuilder.reflectionHashCode(this);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString() {
    return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE).append(
        "groups", this.groups).toString();
  }
}
