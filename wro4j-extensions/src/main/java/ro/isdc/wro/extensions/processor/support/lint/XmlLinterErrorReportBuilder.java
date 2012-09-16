package ro.isdc.wro.extensions.processor.support.lint;

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
public class XmlLinterErrorReportBuilder extends AbstractXmlLintReportBuilder<LinterError> {
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
  
  /**
   * Factory method for creating {@link XmlLinterErrorReportBuilder}.
   * @param lintReport
   *          {@link LintReport} to build xml reports from.
   */
  public static XmlLinterErrorReportBuilder createLintReportBuilder(final LintReport<LinterError> lintReport) { 
    return new XmlLinterErrorReportBuilder(lintReport);
  }
  
  /**
   * Builder responsible for generating checkstyle reports.
   * @param lintReport
   * @return
   */
  public static XmlLinterErrorReportBuilder createCheckstyleReportBuilder(final LintReport<LinterError> lintReport) { 
    return new XmlLinterErrorReportBuilder(lintReport) {
      @Override
      protected String getCharAttributeName() {
        return ATTR_COLUMN;
      }
      @Override
      protected String getIssueElementName() {
        return ELEMENT_ERROR;
      }
      @Override
      protected String getReasonAttributeName() {
        return ATTR_MESSAGE;
      }
      @Override
      protected String getRootElementName() {
        return ELEMENT_CHECKSTYLE;
      }
    };
  }
  
  /**
   * @param lintReport
   *          a not null collection of {@link LinterError} used to build an XML report from.
   */
  protected XmlLinterErrorReportBuilder(final LintReport<LinterError> lintReport) {
    super(lintReport);
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
    issueElement.setAttribute(getCharAttributeName(), String.valueOf(error.getCharacter()));
    issueElement.setAttribute(ATTR_EVIDENCE, String.valueOf(error.getEvidence()));
    issueElement.setAttribute(ATTR_LINE, String.valueOf(error.getLine()));
    issueElement.setAttribute(getReasonAttributeName(), String.valueOf(error.getReason()));
    return issueElement;
  }

  /**
   * @return the name of the attribute indicating the character number where the issue is located.
   */
  protected String getCharAttributeName() {
    return ATTR_CHARACTER;
  }

  /**
   * @return the name of the attribute indicating a reason of the issue.
   */
  protected String getReasonAttributeName() {
    return ATTR_REASON;
  }

  /**
   * @return name of tag indicating an issue. 
   */
  protected String getIssueElementName() {
    return ELEMENT_ISSUE;
  }
  
  /**
   * @return the name of root element.
   */
  protected String getRootElementName() {
    return ELEMENT_LINT;
  }
}
