/*
 * Copyright (c) 2008. All rights reserved.
 */

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.model.WroModel;
import ro.isdc.wro.model.group.Group;
import ro.isdc.wro.model.group.Inject;
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
   * Group tag used in xml.
   */
  private static final String TAG_GROUP = "group";

  /**
   * Groups tag used in xml.
   */
  private static final String TAG_GROUPS = "groups";
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
   * Group name attribute used in xml.
   */
  private static final String ATTR_GROUP_NAME = "name";
  /**
   * Minimize attribute specified on resource level, used to turn on/off minimization on this particular resource during
   * pre processing.
   */
  private static final String ATTR_MINIMIZE = "minimize";

  /**
   * Used to locate imports;
   */
  @Inject
  private UriLocatorFactory uriLocatorFactory;
  /**
   * {@inheritDoc}
   */
  public synchronized WroModel create() {
    // TODO cache model based on application Mode (DEPLOYMENT, DEVELOPMENT)
    final StopWatch stopWatch = new StopWatch("Create Wro Model from XML");
    try {
      stopWatch.start("createDocument");
      final Document document = createDocument();

      final WroModel model = createModel();
      
      parseElement(model, document.getDocumentElement());

      return model;
    } finally {
      //clear the processed imports even when the model creation fails.
      stopWatch.stop();
      LOG.debug(stopWatch.prettyPrint());
    }
  }


  private void parseElement(final WroModel model, final Element element) {
    final String tagName = element.getTagName();
    if (TAG_JS.equals(tagName)) {
      parseJS(model, element);
    } else if (TAG_CSS.equals(tagName)) {
      parseCSS(model, element);
    } else if (TAG_GROUP.equals(tagName)) {
      parseGroup(model, element);
    } else if (TAG_GROUP_REF.equals(tagName)) {
      parseGroupRef(model, element);
    } else if (TAG_IMPORT.equals(tagName)) {
      parseImport(model, element);
    } else if (TAG_GROUPS.equals(tagName)) {
      parseChildren(model, element);
    }
    }

  private void parseImport(final WroModel model, final Element element) {
    final String uri = element.getTextContent().trim();
    LOG.debug("processing import: {}", uri);

    final XmlModelFactory importedModelFactory = new XmlModelFactory() {
      @Override
      protected InputStream getModelResourceAsStream()
          throws IOException {
        LOG.debug("build model from import: {}", uri);
        LOG.debug("uriLocatorFactory: {}", uriLocatorFactory);
        return uriLocatorFactory.locate(uri);
      };
    };

    WroModel other = importedModelFactory.create();
    
    for (Group g : other.getGroups()) {
      addGroup(g);
      model.addGroup(g);
    }
  }

  private void parseGroupRef(final WroModel model, final Element element) {
    String ref = element.getTextContent().trim();
    
    Group other = groups.get(ref);
    if (null == other) {
      throw new WroRuntimeException("Missing group [" + ref + "]");
    }
    
    for (Resource r : other.getResources()) {
      currentGroup.addResource(r);
    }
    }

  private final Map<String, Group> groups = new HashMap<String, Group>();
  
  private void parseGroup(final WroModel model, final Element element) {
    
    final String name = element.getAttribute(ATTR_GROUP_NAME);
    Group group = new Group(name);
    
    addGroup(group);
    
    final Group parentGroup = currentGroup;
    currentGroup = group;
    
    parseChildren(model, element);
    model.addGroup(group);
    
    currentGroup = parentGroup;
  }


  void addGroup(final Group group) {
    final Group old = groups.put(group.getName(), group);
    if (null != old) {
      throw new WroRuntimeException("Found two groups with the name [" + group.getName() + "]");
    }
    }

  private void parseCSS(final WroModel model, final Element element) {
    Resource resource = createResoure(ResourceType.CSS, element);
    currentGroup.addResource(resource);
    }

  private Group currentGroup;

  private void parseJS(final WroModel model, final Element element) {
    Resource resource = createResoure(ResourceType.JS, element);
    currentGroup.addResource(resource);
    }
  
  private Resource createResoure(final ResourceType type, final Element element) {
    final String minimizeAsString = element.getAttribute(ATTR_MINIMIZE);
    final boolean minimize = StringUtils.isEmpty(minimizeAsString) ? true
        : Boolean.valueOf(element.getAttribute(ATTR_MINIMIZE));
    String text = element.getTextContent();
    final Resource resource = Resource.create(text, type);
    resource.setMinimize(minimize);
    return resource;
    }
  
  private void parseChildren(final WroModel model, final Element element) {
    NodeList children = element.getChildNodes();
    for (int i = 0; i < children.getLength(); i++) {
      if (Element.ELEMENT_NODE != children.item(i).getNodeType()) {
        continue;
      }
      
      final Element child = (Element) children.item(i);
      parseElement(model, child);
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
      return document;
    } catch (final Exception e) {
      throw new WroRuntimeException("Cannot build model from XML", e);
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
    return model;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected String getDefaultModelFilename() {
    return DEFAULT_FILE_NAME;
  }
}