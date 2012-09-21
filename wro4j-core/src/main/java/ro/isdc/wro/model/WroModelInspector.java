package ro.isdc.wro.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import ro.isdc.wro.model.group.Group;
import ro.isdc.wro.model.resource.Resource;

/**
 * Simplifies model inspection.
 * 
 * @author Alex Objelean
 * @created 21 Sep 2012
 * @since 1.4.10
 */
public class WroModelInspector {
  private final WroModel model;
  public WroModelInspector(final WroModel model) {
    Validate.notNull(model);
    this.model = model;
  }
  
  /**
   * @param name
   *          of group to find.
   * @return group with searched name (if available) or null otherwise.
   * 
   */
  public Group getGroupByName(final String name) {
    final Collection<Group> groups = model.getGroups();
    for (final Group group : groups) {
      if (name.equals(group.getName())) {
        return group;
      }
    }
    return null;
  }
  
  /**
   * @param resource
   *          the {@link Resource} to search in all available groups.
   * @return t collection of group names containing provided resource. If the resource is not availalbe, an empty collection
   *         will be returned.
   */
  public Collection<String> getGroupNamesContainingResource(final String resourceUri) {
    Validate.notNull(resourceUri);
    final Set<String> groupNames = new HashSet<String>();
    for (Group group : model.getGroups()) {
      if (group.hasResource(resourceUri)) {
        groupNames.add(group.getName());
      }
    }
    return groupNames;
  }
  
  /**
   * @return a set of group names.
   */
  public final List<String> getGroupNames() {
    final List<String> groupNames = new ArrayList<String>();
    for (final Group group : model.getGroups()) {
      groupNames.add(group.getName());
    }
    return groupNames;
  }
  
  /**
   * This implementation would be simpler if java would have closures :).
   *
   * @return a comma separated list of group names.
   */
  public String getGroupNamesAsString() {
    return String.format("[%s]", StringUtils.join(getGroupNames(), ", "));
  }
  

  /**
   * @return the set of all resources from all the groups of the model (no particular order).
   */
  public Set<Resource> getAllResources() {
    final Set<Resource> resources = new HashSet<Resource>();
    for (final Group group : model.getGroups()) {
      resources.addAll(group.getResources());
    }
    return resources;
  }
}
