package ro.isdc.wro.extensions.support.lint;

import static org.apache.commons.lang3.Validate.notNull;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import ro.isdc.wro.extensions.processor.support.linter.LinterError;


/**
 * Builds an XML report for lint errors based on provided collection of {@link LinterError}'s.
 * 
 * @author Alex Objelean
 * @since 1.4.10
 * @created 15 Sep 2012
 */
public class XmlLinterErrorReportFormatter extends AbstractXmlLintReportFormatter<LinterError> {
  /**
   * Checkstyle related constants
   */
  private static final String ELEMENT_CHECKSTYLE = "checkstyle";
  private static final String ELEMENT_ERROR = "error";
  private static final String ATTR_COLUMN = "column";
  private static final String ATTR_MESSAGE = "message";
  /**
   * Lint related constants
   */
  private static final String ELEMENT_LINT = "lint";
  private static final String ELEMENT_ISSUE = "issue";
  private static final String ELEMENT_FILE = "file";
  private static final String ATTR_NAME = "name";
  private static final String ATTR_REASON = "reason";
  private static final String ATTR_LINE = "line";
  private static final String ATTR_EVIDENCE = "evidence";
  private static final String ATTR_CHARACTER = "char";
  
  private final Type  type;
  
  public static enum Type {
    LINT, CHECKSTYLE
  }
  
  /**
   * Factory method for creating {@link XmlLinterErrorReportFormatter}.
   * @param lintReport
   *          {@link LintReport} to build xml reports from.
   */
  public static XmlLinterErrorReportFormatter create(final LintReport<LinterError> lintReport, final Type type) {
    return new XmlLinterErrorReportFormatter(lintReport, type);
  }
  
  /**
   * @param lintReport
   *          a not null collection of {@link LinterError} used to build an XML report from.
   */
  protected XmlLinterErrorReportFormatter(final LintReport<LinterError> lintReport, final Type type) {
    super(lintReport);
    notNull(type);
    this.type = type;
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  protected void buildDocument() {
    final Element rootElement = getDocument().createElement(getRootElementName());
    getDocument().appendChild(rootElement);
    
    for (ResourceLintReport<LinterError> resourceErrors : getLintReport().getReports()) {
      rootElement.appendChild(createFileElement(resourceErrors));
    }
  }
  
  /**
   * Creates a {@link Node} containing informations about all errors associated to a single resource.
   */
  private Node createFileElement(final ResourceLintReport<LinterError> resourceErrors) {
    final Element fileElement = getDocument().createElement(ELEMENT_FILE);
    fileElement.setAttribute(ATTR_NAME, resourceErrors.getResourcePath());
    for (LinterError error : resourceErrors.getLints()) {
      fileElement.appendChild(createIssueElement(error));
    }
    return fileElement;
  }
  
  /**
   * Creates a {@link Node} containing a detailed description of an issue.
   */
  private Node createIssueElement(final LinterError error) {
    final Element issueElement = getDocument().createElement(getIssueElementName());
    issueElement.setAttribute(getColumnAttributeName(), String.valueOf(error.getCharacter()));
    issueElement.setAttribute(ATTR_EVIDENCE, String.valueOf(error.getEvidence()));
    issueElement.setAttribute(ATTR_LINE, String.valueOf(error.getLine()));
    issueElement.setAttribute(getReasonAttributeName(), String.valueOf(error.getReason()));
    return issueElement;
  }

  /**
   * @return the name of the attribute indicating the character number where the issue is located.
   */
  protected String getColumnAttributeName() {
    return type == Type.LINT ? ATTR_CHARACTER : ATTR_COLUMN;
  }

  /**
   * @return the name of the attribute indicating a reason of the issue.
   */
  protected String getReasonAttributeName() {
    return type == Type.LINT ? ATTR_REASON : ATTR_MESSAGE;
  }

  /**
   * @return name of tag indicating an issue. 
   */
  protected String getIssueElementName() {
    return type == Type.LINT ? ELEMENT_ISSUE : ELEMENT_ERROR;
  }
  
  /**
   * @return the name of root element.
   */
  protected String getRootElementName() {
    return type == Type.LINT ? ELEMENT_LINT: ELEMENT_CHECKSTYLE;
  }
}
