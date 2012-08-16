/*
 * Copyright (c) 2008. All rights reserved.
 */
package ro.isdc.wro.model.factory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.apache.commons.io.input.AutoCloseInputStream;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.model.WroModel;
import ro.isdc.wro.model.group.Group;
import ro.isdc.wro.model.group.Inject;
import ro.isdc.wro.model.group.RecursiveGroupDefinitionException;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.locator.factory.UriLocatorFactory;
import ro.isdc.wro.util.StopWatch;


/**
 * Model factory implementation. Creates a WroModel object, based on an xml. This xml contains the description of all
 * groups.
 * <p/>
 * This class is thread-safe (the create method is synchronized).
 * 
 * @author Alex Objelean
 * @created Created on Nov 3, 2008
 */
public class XmlModelFactory
    extends AbstractWroModelFactory {
  /**
   * Logger for this class.
   */
  private static final Logger LOG = LoggerFactory.getLogger(XmlModelFactory.class);
  
  /**
   * Default xml filename.
   */
  private static final String DEFAULT_FILE_NAME = "wro.xml";
  
  /**
   * Default xml to parse.
   */
  private static final String XML_SCHEMA_FILE = "wro.xsd";
  
  /**
   * Group tag used in xml.
   */
  private static final String TAG_GROUP = "group";
  /**
   * Import tag used in xml.
   */
  private static final String TAG_IMPORT = "import";
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
   * Group name attribute.
   */
  private static final String ATTR_GROUP_NAME = "name";
  /**
   * Group abstract attribute used to distinguish an abstract group when its value is true. By default the value is false.
   */
  private static final String ATTR_GROUP_ABSTRACT = "abstract";
  /**
   * Minimize attribute specified on resource level, used to turn on/off minimization on this particular resource during
   * pre processing.
   */
  private static final String ATTR_MINIMIZE = "minimize";
  
  /**
   * Map between the group name and corresponding element. Hold the map<GroupName, Element> of all group nodes to access
   * any element.
   */
  final Map<String, Element> allGroupElements = new HashMap<String, Element>();
  
  /**
   * List of groups which are currently being processing and are partially parsed. This list is useful in order to catch
   * infinite recurse group reference.
   */
  final Collection<String> groupsInProcess = new HashSet<String>();
  
  /**
   * Used to locate imports;
   */
  @Inject
  private UriLocatorFactory locatorFactory;
  /**
   * Used to detect recursive import processing.
   */
  private final Set<String> processedImports = new HashSet<String>();
  /**
   * Flag for enabling xml validation.
   */
  private boolean validateXml = true;
  
  /**
   * {@inheritDoc}
   */
  public synchronized WroModel create() {
    // TODO cache model based on application Mode (DEPLOYMENT, DEVELOPMENT)
    final StopWatch stopWatch = new StopWatch("Create Wro Model from XML");
    try {
      stopWatch.start("createDocument");
      final Document document = createDocument();
      stopWatch.stop();
      
      stopWatch.start("processGroups");
      processGroups(document);
      stopWatch.stop();
      
      stopWatch.start("createModel");
      final WroModel model = createModel();
      stopWatch.stop();
      
      stopWatch.start("processImports");
      processImports(document, model);
      return model;
    } finally {
      // clear the processed imports even when the model creation fails.
      processedImports.clear();
      stopWatch.stop();
      LOG.debug(stopWatch.prettyPrint());
    }
  }
  
  /**
   * @return valid {@link Document} of the xml containing model representation.
   */
  private Document createDocument() {
    try {
      final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setNamespaceAware(true);
      final Document document = factory.newDocumentBuilder().parse(getModelResourceAsStream());
      document.getDocumentElement().normalize();
      if (isValidateXml()) {
        validate(document);
      }
      return document;
    } catch (final Exception e) {
      throw new WroRuntimeException("Cannot build model from XML", e);
    }
  }
  
  /**
   * @param document
   *          xml document to validate.
   */
  private void validate(final Document document)
      throws IOException, SAXException {
    final SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
    final Schema schema = factory.newSchema(new StreamSource(getSchemaStream()));
    schema.newValidator().validate(new DOMSource(document));
  }
  
  private InputStream getSchemaStream()
      throws IOException {
    // use the class located in same package where xsd is located
    return WroRuntimeException.class.getResourceAsStream(XML_SCHEMA_FILE);
  }
  
  /**
   * Initialize the map
   */
  private void processGroups(final Document document) {
    // handle imports
    final NodeList groupNodeList = document.getElementsByTagName(TAG_GROUP);
    for (int i = 0; i < groupNodeList.getLength(); i++) {
      final Element groupElement = (Element) groupNodeList.item(i);
      final String name = groupElement.getAttribute(ATTR_GROUP_NAME);
      allGroupElements.put(name, groupElement);
    }
  }
  
  private void processImports(final Document document, final WroModel model) {
    final NodeList importsList = document.getElementsByTagName(TAG_IMPORT);
    LOG.debug("number of imports: {}", importsList.getLength());
    for (int i = 0; i < importsList.getLength(); i++) {
      final Element element = (Element) importsList.item(i);
      final String name = element.getTextContent();
      LOG.debug("processing import: {}", name);
      Validate.notNull(locatorFactory, "The Locator cannot be null!");
      final XmlModelFactory importedModelFactory = new XmlModelFactory() {
        @Override
        protected InputStream getModelResourceAsStream()
            throws IOException {
          LOG.debug("build model from import: {}", name);
          return new AutoCloseInputStream(locatorFactory.locate(name));
        };
      };
      // pass the reference of the uriLocatorFactory to the anonymously created factory.
      importedModelFactory.locatorFactory = this.locatorFactory;
      if (processedImports.contains(name)) {
        final String message = "Recursive import detected: " + name;
        LOG.error(message);
        throw new RecursiveGroupDefinitionException(message);
      }
      
      processedImports.add(name);
      importedModelFactory.processedImports.addAll(this.processedImports);
      model.merge(importedModelFactory.create());
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
    final Set<Group> groups = new HashSet<Group>();
    for (final Element element : allGroupElements.values()) {
      parseGroup(element, groups);
    }
    model.setGroups(groups);
    return model;
  }
  
  /**
   * Recursive method. Add the parsed element group to the group collection. If the group contains group-ref element,
   * parse recursively this group.
   * 
   * @param element
   *          Group Element to parse.
   * @param groups
   *          list of parsed groups where the parsed group is added..
   * @return list of resources associated with this resource
   */
  private Collection<Resource> parseGroup(final Element element, final Collection<Group> groups) {
    final String name = element.getAttribute(ATTR_GROUP_NAME);
    final String isAbstractAsString = element.getAttribute(ATTR_GROUP_ABSTRACT);
    boolean isAbstractGroup = StringUtils.isNotEmpty(isAbstractAsString) && Boolean.valueOf(isAbstractAsString);
    if (groupsInProcess.contains(name)) {
      throw new RecursiveGroupDefinitionException("Infinite Recursion detected for the group: " + name
          + ". Recursion path: " + groupsInProcess);
    }
    groupsInProcess.add(name);
    LOG.debug("\tgroupName={}", name);
    // skip if this group is already parsed
    final Group parsedGroup = getGroupByName(name, groups);
    if (parsedGroup != null) {
      // remove before returning
      // this group is parsed, remove from unparsed groups collection
      groupsInProcess.remove(name);
      return parsedGroup.getResources();
    }
    final Group group = new Group(name);
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
    groupsInProcess.remove(name);
    if (!isAbstractGroup) {
      //add only non abstract groups
      groups.add(group);
    }
    return resources;
  }
  
  /**
   * Check if the group with name <code>name</code> was already parsed and returns Group object with it's resources
   * initialized.
   * 
   * @param name
   *          the group to check.
   * @param groups
   *          list of parsed groups.
   * @return parsed Group by it's name.
   */
  private Group getGroupByName(final String name, final Collection<Group> groups) {
    for (final Group group : groups) {
      if (name.equals(group.getName())) {
        return group;
      }
    }
    return null;
  }
  
  /**
   * Creates a resource from a given resourceElement. It can be css, js. If resource tag name is group-ref, the method
   * will start a recursive computation.
   * 
   * @param resourceElement
   * @param resources
   *          list of parsed resources where the parsed resource is added.
   */
  private void parseResource(final Element resourceElement, final Collection<Resource> resources,
      final Collection<Group> groups) {
    ResourceType type = null;
    final String tagName = resourceElement.getTagName();
    final String uri = resourceElement.getTextContent();
    if (TAG_JS.equals(tagName)) {
      type = ResourceType.JS;
    } else if (TAG_CSS.equals(tagName)) {
      type = ResourceType.CSS;
    } else if (TAG_GROUP_REF.equals(tagName)) {
      // uri in this case is the group name
      final Element groupElement = allGroupElements.get(uri);
      resources.addAll(parseGroup(groupElement, groups));
    }
    if (type != null) {
      final String minimizeAsString = resourceElement.getAttribute(ATTR_MINIMIZE);
      final boolean minimize = StringUtils.isEmpty(minimizeAsString) ? true
          : Boolean.valueOf(resourceElement.getAttribute(ATTR_MINIMIZE));
      final Resource resource = Resource.create(uri, type);
      resource.setMinimize(minimize);
      resources.add(resource);
    }
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  protected String getDefaultModelFilename() {
    return DEFAULT_FILE_NAME;
  }
  
  /**
   * @return true if xml validation should be performed.
   */
  private boolean isValidateXml() {
    return this.validateXml;
  }
  
  /**
   * Allows disable the xml validation (which is true by default. Be aware that this is not a good idea to disable
   * validation, because you cannot be sure that the model is built correctly. Disabling makes sense only when for some
   * reason the xsd schema cannot be loaded on some environments. An example of issue can be found here:<br/>
   * <a href="http://code.google.com/p/wro4j/issues/detail?id=371">Wro4j doesn't work on Websphere with 2 or more
   * wars</a>
   */
  public XmlModelFactory setValidateXml(final boolean validateXml) {
    this.validateXml = validateXml;
    return this;
  }
}
