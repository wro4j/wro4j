/*
 * Copyright (c) 2008 ISDC! Romania. All rights reserved.
 */
package ro.isdc.wro.model.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import ro.isdc.wro.exception.RecursiveGroupDefinitionException;
import ro.isdc.wro.exception.WroRuntimeException;
import ro.isdc.wro.http.Context;
import ro.isdc.wro.model.Group;
import ro.isdc.wro.model.WroModel;
import ro.isdc.wro.model.WroModelFactory;
import ro.isdc.wro.resource.Resource;
import ro.isdc.wro.resource.ResourceType;
import ro.isdc.wro.resource.UriLocator;
import ro.isdc.wro.resource.UriLocatorFactory;

/**
 * Model factory implementation. Creates a WroModel object, based on an xml.
 * This xml contains the description of all groups.
 *
 * @author alexandru.objelean / ISDC! Romania
 * @version $Revision: $
 * @date $Date: $
 * @created Created on Nov 3, 2008
 */
public class XmlModelFactory implements WroModelFactory {
  /**
   * Logger for this class.
   */
  private static final Log log = LogFactory.getLog(XmlModelFactory.class);

  /**
   * Default xml to parse.
   */
  protected static final String XML_CONFIG_FILE = "wro.xml";

  /**
   * Default xml to parse.
   */
  private static final String XML_SCHEMA_FILE = "ro/isdc/wro/wro.xsd";

  /**
   * Group tag used in xml.
   */
  private static final String TAG_GROUP = "group";

  /**
   * CSS tag used in xml.
   */
  private static final String TAG_CSS = "css";

  /**
   * JS tag used in xml.
   */
  private static final String TAG_JS = "js";

  /**
   * GroupRef tag used in xml.
   */
  private static final String TAG_GROUP_REF = "group-ref";

  /**
   * Group name attribute used in xml.
   */
  private static final String ATTR_GROUP_NAME = "name";

  /**
   * UriLocatorFactory. Used to create a resource based on its type.
   */
  private UriLocatorFactory uriLocatorFactory;

  /**
   * Map between the group name and corresponding element. Hold the map<GroupName,
   * Element> of all group nodes to access any element.
   */
  final Map<String, Element> allGroupElements = new HashMap<String, Element>();

  /**
   * List of groups which are currently processing and are partially parsed.
   * This list is useful in order to catch infinite recurse group reference.
   */
  final List<String> processingGroups = new ArrayList<String>();

  /**
   * Reference to cached model instance. Using volatile keyword fix the problem
   * with double-checked locking in JDK 1.5.
   */
  private volatile WroModel model;

  /**
   * {@inheritDoc}
   */
  public synchronized WroModel getInstance(
      final UriLocatorFactory uriLocatorFactory) {
    log.debug("<getInstance>");
    try {
      if (uriLocatorFactory == null) {
        throw new IllegalArgumentException("uriLocatorFactory cannot be NULL!");
      }
      this.uriLocatorFactory = uriLocatorFactory;
      // when in DEVELOPMENT mode, create fresh new instance for each request.
      if (Context.get().isDevelopmentMode()) {
        return newModel();
      }
      // use double-check locking
      if (this.model == null) {
        synchronized (this) {
          if (this.model == null) {
            this.model = newModel();
          }
        }
      }
      return this.model;
    } finally {
      log.debug("</getInstance>");
    }
  }

  /**
   * Build model from scratch after xml is parsed.
   *
   * @return new instance of model.
   */
  private WroModel newModel() {
    // TODO return a single instance based on some configuration?
    Document document = null;
    try {
      final DocumentBuilderFactory factory = DocumentBuilderFactory
          .newInstance();
      factory.setNamespaceAware(true);
      // factory.setSchema(getSchema());
      // factory.setValidating(true);
      document = factory.newDocumentBuilder()
          .parse(getConfigResourceAsStream());
      validate(document);
      document.getDocumentElement().normalize();
    } catch (final IOException e) {
      throw new WroRuntimeException("Cannot find XML to parse", e);
    } catch (final SAXException e) {
      throw new WroRuntimeException("Parsing error", e);
    } catch (final ParserConfigurationException e) {
      throw new WroRuntimeException("Parsing error", e);
    }
    initGroupMap(document);
    // TODO cache model based on application Mode (DEPLOYMENT, DEVELOPMENT)
    final WroModel model = createModel();
    log.debug("</getInstance>");
    return model;
  }

  /**
   * @return Schema
   */
  private Schema getSchema() throws IOException, SAXException {
    // create a SchemaFactory capable of understanding WXS schemas
    final SchemaFactory factory = SchemaFactory
    // .newInstance(schemaLanguage)
        .newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

    // load a WXS schema, represented by a Schema instance
    final Source schemaFile = new StreamSource(
        getResourceAsStream(XML_SCHEMA_FILE));
    final Schema schema = factory.newSchema(schemaFile);
    return schema;
  }

  /**
   * Override this method, in order to provide different xml definition file
   * name.
   *
   * @return name of the xml file containing group & resource definition.
   */
  protected InputStream getConfigResourceAsStream() {
    return getResourceAsStream(XML_CONFIG_FILE);
  }

  /**
   * Initialize the map
   */
  private void initGroupMap(final Document document) {
    final NodeList groupNodeList = document.getElementsByTagName(TAG_GROUP);
    for (int i = 0; i < groupNodeList.getLength(); i++) {
      final Element groupElement = (Element) groupNodeList.item(i);
      final String name = groupElement.getAttribute(ATTR_GROUP_NAME);
      allGroupElements.put(name, groupElement);
    }
  }

  /**
   * Parse the document and creates the model.
   *
   * @param document
   *          to parse.
   * @return {@link WroModel} object.
   */
  private WroModel createModel() {
    final WroModel model = new WroModel();
    final List<Group> groups = new ArrayList<Group>();
    for (final Element element : allGroupElements.values()) {
      parseGroup(element, groups);
    }
    model.setGroups(groups);
    return model;
  }

  /**
   * Recursive method. Add the parsed element group to the group collection. If
   * the group contains group-ref element, parse recursively this group.
   *
   * @param element
   *          Group Element to parse.
   * @param groups
   *          list of parsed groups where the parsed group is added..
   * @return list of resources associated with this resource
   */
  private List<Resource> parseGroup(final Element element,
      final List<Group> groups) {
    log.debug("<parseGroup>");
    final String name = element.getAttribute(ATTR_GROUP_NAME);
    if (processingGroups.contains(name)) {
      throw new RecursiveGroupDefinitionException(
          "Infinite Recursion detected for the group: " + name
              + ". Recursion path: " + processingGroups);
    }
    processingGroups.add(name);
    log.debug("\tgroupName=" + name);
    // skip if this group is already parsed
    final Group parsedGroup = getGroupByName(name, groups);
    if (parsedGroup != null) {
      // remove before returning
      // this group is parsed, remove from unparsed groups collection
      processingGroups.remove(name);
      return parsedGroup.getResources();
    }
    final Group group = new Group();
    group.setName(name);
    final List<Resource> resources = new ArrayList<Resource>();
    final NodeList resourceNodeList = element.getChildNodes();
    for (int i = 0; i < resourceNodeList.getLength(); i++) {
      final Node node = resourceNodeList.item(i);
      if (node instanceof Element) {
        final Element resourceElement = (Element) node;
        parseResource(resourceElement, resources, groups);
      }
    }
    group.setResources(resources);
    // this group is parsed, remove from unparsed collection
    processingGroups.remove(name);
    groups.add(group);
    log.debug("</parseGroup>");
    return resources;
  }

  /**
   * Check if the group with name <code>name</code> was already parsed and
   * returns Group object with it's resources initialized.
   *
   * @param name
   *          the group to check.
   * @param groups
   *          list of parsed groups.
   * @return parsed Group by it's name.
   */
  private static Group getGroupByName(final String name,
      final List<Group> groups) {
    for (final Group group : groups) {
      if (name.equals(group.getName())) {
        return group;
      }
    }
    return null;
  }

  /**
   * Creates a resource from a given resourceElement. It can be css, js. If
   * resource tag name is group-ref, the method will start a recursive
   * computation.
   *
   * @param resourceElement
   * @param resources
   *          list of parsed resources where the parsed resource is added.
   */
  private void parseResource(final Element resourceElement,
      final List<Resource> resources, final List<Group> groups) {
    log.debug("<parseResource>");
    ResourceType type = null;
    final String tagName = resourceElement.getTagName();
    final String uri = resourceElement.getTextContent();
    log.debug("\ttagName=" + tagName);
    log.debug("\turi=" + uri);
    if (TAG_JS.equals(tagName)) {
      type = ResourceType.JS;
    } else if (TAG_CSS.equals(tagName)) {
      type = ResourceType.CSS;
    } else if (TAG_GROUP_REF.equals(tagName)) {
      // uri in this case is the group name
      final Element groupElement = allGroupElements.get(uri);
      log.debug("\tparse groupRef: " + uri + ": " + groupElement);
      resources.addAll(parseGroup(groupElement, groups));
    } else {
      // should not ever happen due to validation of xml.
      throw new WroRuntimeException("Usupported resource type: " + tagName);
    }
    log.debug("\ttype=" + type);
    if (type != null) {
      final UriLocator uriLocator = this.uriLocatorFactory.getInstance(uri);
      final Resource resource = new Resource(uri, type, uriLocator);
      resources.add(resource);
    }
    log.debug("</parseResource>");
  }

  /**
   * @return InputStream of the local resource from classpath.
   */
  protected static InputStream getResourceAsStream(final String fileName) {
    return Thread.currentThread().getContextClassLoader().getResourceAsStream(
        fileName);
  }

  /**
   * Checks if xml structure is valid.
   *
   * @param document
   *          xml document to validate.
   */
  private void validate(final Document document) throws IOException,
      SAXException {
    final Schema schema = getSchema();
    // create a Validator instance, which can be used to validate an instance
    // document
    final Validator validator = schema.newValidator();
    // validate the DOM tree
    validator.validate(new DOMSource(document));
  }
}
