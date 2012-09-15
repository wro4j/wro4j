package ro.isdc.wro.extensions.processor.support.linter.report;

import static org.apache.commons.lang3.Validate.notNull;

import java.io.OutputStream;
import java.util.Collection;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.extensions.processor.support.linter.LinterError;
import ro.isdc.wro.extensions.processor.support.linter.ResourceLinterErrors;


/**
 * Builds an XML report for lint errors based on provided collection of {@link LinterError}'s.
 * 
 * @author Alex Objelean
 * @since 1.4.10
 * @created 15 Sep 2012
 */
public class XmlReportBuilder {
  private static final String ATTR_NAME = "name";
  private static final String ELEMENT_FILE = "file";
  private static final String ATTR_REASON = "reason";
  private static final String ATTR_LINE = "line";
  private static final String ATTR_EVIDENCE = "evidence";
  private static final String ATTR_CHARACTER = "char";
  private Document doc;
  private Collection<ResourceLinterErrors<LinterError>> errors;
  
  /**
   * @param errors
   *          a not null collection of {@link LinterError} used to build an XML report from.
   */
  public XmlReportBuilder(final Collection<ResourceLinterErrors<LinterError>> errors) {
    notNull(errors);
    this.errors = errors;
    initDocument();
  }
  
  private void initDocument() {
    try {
      final DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
      final DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
      this.doc = docBuilder.newDocument();
    } catch (ParserConfigurationException e) {
      throw new WroRuntimeException("Unexpected problem while XML Document", e);
    }
  }
  
  /**
   * @param outputStream
   *          the {@link OutputStream} where the xml representation of the report is written.
   */
  public void write(final OutputStream outputStream) {
    final Element rootElement = doc.createElement("lint");
    doc.appendChild(rootElement);
    
    for (ResourceLinterErrors<LinterError> resourceErrors : errors) {
      rootElement.appendChild(createFileElement(resourceErrors));
    }
    // write the content into xml file
    writeReport(outputStream);
  }
  
  private void writeReport(final OutputStream outputStream) {
    Transformer transformer;
    try {
      transformer = TransformerFactory.newInstance().newTransformer();
      transformer.transform(new DOMSource(doc), new StreamResult(outputStream));
    } catch (Exception e) {
      throw WroRuntimeException.wrap(e, "Problem during Document transformation").logError();
    }
  }
  
  /**
   * Creates a {@link Node} containing informations about all errors associated to a single resource.
   */
  private Node createFileElement(final ResourceLinterErrors<LinterError> resourceErrors) {
    final Element fileElement = doc.createElement(ELEMENT_FILE);
    fileElement.setAttribute(ATTR_NAME, resourceErrors.getResourcePath());
    for (LinterError error : resourceErrors.getErrors()) {
      fileElement.appendChild(createIssueElement(error));
    }
    return fileElement;
  }
  
  /**
   * Creates a {@link Node} containing a detailed description of an issue.
   */
  private Node createIssueElement(final LinterError error) {
    final Element issueElement = doc.createElement(ELEMENT_FILE);
    issueElement.setAttribute(ATTR_CHARACTER, String.valueOf(error.getCharacter()));
    issueElement.setAttribute(ATTR_EVIDENCE, String.valueOf(error.getEvidence()));
    issueElement.setAttribute(ATTR_LINE, String.valueOf(error.getLine()));
    issueElement.setAttribute(ATTR_REASON, String.valueOf(error.getReason()));
    return issueElement;
  }
}
