package ro.isdc.wro.extensions.processor.support.lint;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import ro.isdc.wro.extensions.processor.support.csslint.CssLintError;
import ro.isdc.wro.extensions.processor.support.linter.LinterError;


/**
 * Builds an XML report for css lint errors based on provided {@link LintReport}
 * 
 * @author Alex Objelean
 * @since 1.4.10
 * @created 17 Sep 2012
 */
public class XmlCssLintErrorReportBuilder extends AbstractXmlLintReportBuilder<CssLintError> {
  /**
   * Checkstyle related constants
   */
  private static final String ELEMENT_CHECKSTYLE = "checkstyle";
  private static final String ELEMENT_ERROR = "error";
  private static final String ATTR_MESSAGE = "message";
  /**
   * Lint related constants
   */
  private static final String ELEMENT_LINT = "csslint";
  private static final String ELEMENT_ISSUE = "issue";
  private static final String ELEMENT_FILE = "file";
  private static final String ATTR_NAME = "name";
  private static final String ATTR_REASON = "reason";
  private static final String ATTR_LINE = "line";
  private static final String ATTR_EVIDENCE = "evidence";
  private static final String ATTR_COLUMN = "column";
  
  /**
   * Factory method for creating {@link XmlCssLintErrorReportBuilder}.
   * @param lintReport
   *          {@link LintReport} to build xml reports from.
   */
  public static XmlCssLintErrorReportBuilder createLintReportBuilder(final LintReport<CssLintError> lintReport) { 
    return new XmlCssLintErrorReportBuilder(lintReport);
  }
  
  /**
   * Builder responsible for generating checkstyle reports.
   * @param lintReport
   * @return
   */
  public static XmlCssLintErrorReportBuilder createCheckstyleReportBuilder(final LintReport<CssLintError> lintReport) { 
    return new XmlCssLintErrorReportBuilder(lintReport) {
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
  protected XmlCssLintErrorReportBuilder(final LintReport<CssLintError> lintReport) {
    super(lintReport);
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  protected void buildDocument() {
    final Element rootElement = getDocument().createElement(getRootElementName());
    getDocument().appendChild(rootElement);
    
    for (ResourceLintReport<CssLintError> resourceErrors : getLintReport().getReports()) {
      rootElement.appendChild(createFileElement(resourceErrors));
    }
  }
  
  /**
   * Creates a {@link Node} containing informations about all errors associated to a single resource.
   */
  private Node createFileElement(final ResourceLintReport<CssLintError> resourceErrors) {
    final Element fileElement = getDocument().createElement(ELEMENT_FILE);
    fileElement.setAttribute(ATTR_NAME, resourceErrors.getResourcePath());
    for (CssLintError error : resourceErrors.getLints()) {
      fileElement.appendChild(createIssueElement(error));
    }
    return fileElement;
  }
  
  /**
   * Creates a {@link Node} containing a detailed description of an issue.
   */
  private Node createIssueElement(final CssLintError error) {
    final Element issueElement = getDocument().createElement(getIssueElementName());
    issueElement.setAttribute(ATTR_COLUMN, String.valueOf(error.getCol()));
    issueElement.setAttribute(ATTR_EVIDENCE, String.valueOf(error.getEvidence()));
    issueElement.setAttribute(ATTR_LINE, String.valueOf(error.getLine()));
    issueElement.setAttribute(getReasonAttributeName(), String.valueOf(error.getMessage()));
    return issueElement;
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
