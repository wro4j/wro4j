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
import ro.isdc.wro.model.WroModelInspector;
import ro.isdc.wro.model.group.Group;
import ro.isdc.wro.model.group.Inject;
import ro.isdc.wro.model.group.RecursiveGroupDefinitionException;
import ro.isdc.wro.model.group.processor.Injector;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.locator.factory.UriLocatorFactory;
import ro.isdc.wro.util.StopWatch;


/**
 * <p>Model factory implementation. Creates a WroModel object, based on an xml. This xml contains the description of all
 * groups.</p>
 *
 * <p>This class is thread-safe (the create method is synchronized).</p>
 *
 * @author Alex Objelean
 */
public class XmlModelFactory
    extends AbstractWroModelFactory {
  /**
   * Logger for this class.
   */
  private static final Logger LOG = LoggerFactory.getLogger(XmlModelFactory.class);
  /**
   * The alias for this model factory used by spi provider.
   */
  public static final String ALIAS = "xml";
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
  protected static final String TAG_GROUP = "group";
  /**
   * Import tag used in xml.
   */
  protected static final String TAG_IMPORT = "import";
  /**
   * CSS tag used in xml.
   */
  protected static final String TAG_CSS = "css";

  /**
   * JS tag used in xml.
   */
  protected static final String TAG_JS = "js";

  /**
   * GroupRef tag used in xml.
   */
  protected static final String TAG_GROUP_REF = "group-ref";

  /**
   * Group name attribute.
   */
  protected static final String ATTR_GROUP_NAME = "name";
  /**
   * Group abstract attribute used to distinguish an abstract group when its value is true. By default the value is
   * false.
   */
  protected static final String ATTR_GROUP_ABSTRACT = "abstract";
  /**
   * Minimize attribute specified on resource level, used to turn on/off minimization on this particular resource during
   * pre processing.
   */
  protected static final String ATTR_MINIMIZE = "minimize";
  /**
   * Map between the group name and corresponding element. Hold the map<GroupName, Element> of all group nodes to access
   * any element.
   */
  private final Map<String, Element> allGroupElements = new HashMap<String, Element>();

  /**
   * List of groups which are currently being processing and are partially parsed. This list is useful in order to catch
   * infinite recurse group reference.
   */
  private final Collection<String> groupsInProcess = new HashSet<String>();
  /**
   * Used to locate imports;
   */
  @Inject
  private UriLocatorFactory locatorFactory;
  @Inject
  private Injector injector;
  /**
   * Used to detect recursive import processing.
   */
  private final Set<String> processedImports = new HashSet<String>();
  /**
   * Flag for enabling xml validation.
   */
  private boolean validateXml = true;
  /**
   * The model being created.
   */
  private WroModel model;

  /**
   * Default constructor.
   */
  public XmlModelFactory() {
  }

  /**
   * Allow aggregate processed imports during recursive model creation.
   */
  private XmlModelFactory(final Set<String> processedImports) {
	Validate.notNull(processedImports);
    this.processedImports.addAll(processedImports);
  }

  public synchronized WroModel create() {
    model = new WroModel();
    final StopWatch stopWatch = new StopWatch("Create Wro Model from XML");
    try {
      stopWatch.start("createDocument");
      final Document document = createDocument();
      stopWatch.stop();

      stopWatch.start("processGroups");
      processGroups(document);
      stopWatch.stop();

      stopWatch.start("processImports");
      processImports(document);
      stopWatch.stop();

      stopWatch.start("createModel");
      parseGroups();
      stopWatch.stop();
      return model;
    } finally {
      // clear the processed imports even when the model creation fails.
      processedImports.clear();
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
      final Document document = factory.newDocumentBuilder().parse(new AutoCloseInputStream(getModelResourceAsStream()));
      document.getDocumentElement().normalize();
      if (isValidateXml()) {
        validate(document);
      }
      return document;
    } catch (final Exception e) {
      throw new WroRuntimeException("Cannot build model from XML: " + e.getMessage(), e);
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
    return new AutoCloseInputStream(WroRuntimeException.class.getResourceAsStream(XML_SCHEMA_FILE));
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

  private void processImports(final Document document) {
    final NodeList importsList = document.getElementsByTagName(TAG_IMPORT);
    LOG.debug("number of imports: {}", importsList.getLength());
    for (int i = 0; i < importsList.getLength(); i++) {
      final Element element = (Element) importsList.item(i);
      final String name = element.getTextContent();
      LOG.debug("processing import: {}", name);
      Validate.notNull(locatorFactory, "The Locator cannot be null!");
      if (processedImports.contains(name)) {
        final String message = "Recursive import detected: " + name;
        LOG.error(message);
        throw new RecursiveGroupDefinitionException(message);
      }

      processedImports.add(name);
      model.merge(createImportedModel(name));
    }
  }

  /**
   * @param modelLocation
   *          the path where the model is located.
   * @return the {@link WroModel} created from provided modelLocation.
   */
  private WroModel createImportedModel(final String modelLocation) {
    final XmlModelFactory importedModelFactory = new XmlModelFactory(this.processedImports) {
      @Override
      protected InputStream getModelResourceAsStream()
          throws IOException {
        LOG.debug("build model from import: {}", modelLocation);
        return new AutoCloseInputStream(locatorFactory.locate(modelLocation));
      };
    };
    // inject manually created modelFactory
    injector.inject(importedModelFactory);
    try {
      return importedModelFactory.create();
    } catch (final WroRuntimeException e) {
      LOG.error("Detected invalid model import from location {}", modelLocation);
      throw e;
    }
  }

  /**
   * Parse the document and creates groups which are added to the provided model.
   *
   * @param document
   *          to parse.
   */
  private void parseGroups() {
    // use groups created by imports (if any)
    for (final Element element : allGroupElements.values()) {
      parseGroup(element);
    }
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
  private Collection<Resource> parseGroup(final Element element) {
    final String name = element.getAttribute(ATTR_GROUP_NAME);
    final String isAbstractAsString = element.getAttribute(ATTR_GROUP_ABSTRACT);
    final boolean isAbstractGroup = StringUtils.isNotEmpty(isAbstractAsString) && Boolean.valueOf(isAbstractAsString);
    if (groupsInProcess.contains(name)) {
      throw new RecursiveGroupDefinitionException("Infinite Recursion detected for the group: " + name
          + ". Recursion path: " + groupsInProcess);
    }
    LOG.debug("\tadding group: {}", name);
    groupsInProcess.add(name);
    // skip if this group is already parsed
    final Group parsedGroup = new WroModelInspector(model).getGroupByName(name);
    if (parsedGroup != null) {
      // remove before returning
      // this group is parsed, remove from unparsed groups collection
      groupsInProcess.remove(name);
      return parsedGroup.getResources();
    }
    final Group group = createGroup(element);
    // this group is parsed, remove from unparsed collection
    groupsInProcess.remove(name);
    if (!isAbstractGroup) {
      // add only non abstract groups
      model.addGroup(group);
    }
    return group.getResources();
  }

  /**
   * Creates a group and all its associated resources.
   *
   * @param element
   *          Group element to parse.
   * @return fully initialized group
   */
  protected Group createGroup(final Element element) {
    final String name = element.getAttribute(ATTR_GROUP_NAME);
    final Group group = new Group(name);
    final List<Resource> resources = new ArrayList<Resource>();
    final NodeList resourceNodeList = element.getChildNodes();
    for (int i = 0; i < resourceNodeList.getLength(); i++) {
      final Node node = resourceNodeList.item(i);
      if (node instanceof Element) {
        final Element resourceElement = (Element) node;
        parseResource(resourceElement, resources);
      }
    }
    group.setResources(resources);
    return group;
  }

  /**
   * Creates a resource from a given resourceElement. It can be css, js. If resource tag name is group-ref, the method
   * will start a recursive computation.
   *
   * @param resources
   *          list of parsed resources where the parsed resource is added.
   */
  private void parseResource(final Element resourceElement, final Collection<Resource> resources) {
    final String tagName = resourceElement.getTagName();
    final String uri = resourceElement.getTextContent();
    if (TAG_GROUP_REF.equals(tagName)) {
      // uri in this case is the group name
      resources.addAll(getResourcesForGroup(uri));
    }
    if (getResourceType(resourceElement) != null) {
      final Resource resource = createResource(resourceElement);
      LOG.debug("\t\tadding resource: {}", resource);
      resources.add(resource);
    }
  }
  
  /**
   * @return the {@link ResourceType} of the provided {@link Element}. If the resource type is not known (not a js or css resource), null is returned.
   */
  protected final ResourceType getResourceType(final Element resourceElement) {
    ResourceType type = null;
    final String tagName = resourceElement.getTagName();
    if (TAG_JS.equals(tagName)) {
      type = ResourceType.JS;
    } else if (TAG_CSS.equals(tagName)) {
      type = ResourceType.CSS;
    }
    return type;
  }

  /**
   * Creates a resource from a given resourceElement. The element is guaranteed to be of a simple non-recursive type
   * (i.e. css or js).
   *
   * @param resourceElement
   *          Resource element to parse.
   */
  protected Resource createResource(final Element resourceElement) {
    final String uri = resourceElement.getTextContent();
    final String minimizeAsString = resourceElement.getAttribute(ATTR_MINIMIZE);
    final boolean minimize = StringUtils.isEmpty(minimizeAsString) || Boolean.valueOf(minimizeAsString);
    final Resource resource = Resource.create(uri, getResourceType(resourceElement));
    resource.setMinimize(minimize);
    return resource;
  }

  /**
   * Search for all resources for a group with a given name.
   */
  private Collection<Resource> getResourcesForGroup(final String groupName) {
    final WroModelInspector modelInspector = new WroModelInspector(model);
    final Group foundGroup = modelInspector.getGroupByName(groupName);
    if (foundGroup == null) {
      final Element groupElement = allGroupElements.get(groupName);
      if (groupElement == null) {
        throw new WroRuntimeException("Invalid group-ref: " + groupName);
      }
      return parseGroup(groupElement);
    }
    return foundGroup.getResources();
  }

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
