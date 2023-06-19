package ro.isdc.wro.extensions.support.lint;

import static org.apache.commons.lang3.Validate.notNull;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.extensions.processor.support.csslint.CssLintError;
import ro.isdc.wro.extensions.processor.support.linter.LinterError;
import ro.isdc.wro.util.Function;


/**
 * Builds an XML report for lint errors based on provided collection of {@link LinterError}'s.
 *
 * @author Alex Objelean
 * @since 1.5.0
 */
public class ReportXmlFormatter
    extends AbstractReportXmlFormatter<LintItem> {
  private static final Logger LOG = LoggerFactory.getLogger(ReportXmlFormatter.class);
  /**
   * Checkstyle related constants
   */
  private static final String ELEMENT_ERROR = "error";
  private static final String ATTR_COLUMN = "column";
  private static final String ATTR_MESSAGE = "message";
  /**
   * Lint related constants
   */
  private static final String ELEMENT_ISSUE = "issue";
  private static final String ELEMENT_FILE = "file";
  private static final String ATTR_NAME = "name";
  private static final String ATTR_REASON = "reason";
  private static final String ATTR_LINE = "line";
  private static final String ATTR_EVIDENCE = "evidence";
  private static final String ATTR_CHARACTER = "char";
  private static final String ATTR_SEVERITY = "severity";

  private final FormatterType formatterType;

  public static enum FormatterType {
    LINT("lint"), CHECKSTYLE("checkstyle"), CSSLINT("csslint"), JSLINT("jslint");
    private String rootElementName;

    FormatterType(final String rootElementName) {
      this.rootElementName = rootElementName;
    }

    /**
     * @param format
     *          of the {@link FormatterType} to return.
     * @return the {@link FormatterType} of searched format type or null if such format does not exist.
     */
    public static FormatterType getByFormat(final String format) {
      for (final FormatterType type : FormatterType.values()) {
        if (type.getFormat().equals(format)) {
          return type;
        }
      }
      return null;
    }

    /**
     * @return supported formats as CSV.
     */
    public static String getSupportedFormatsAsCSV() {
      final StringBuffer sb = new StringBuffer();
      for (final FormatterType type : FormatterType.values()) {
        sb.append(type.getFormat()).append(",");
      }
      return StringUtils.removeEnd(sb.toString(), ",");
    }

    /**
     * @return string representation of the format.
     */
    public String getFormat() {
      return String.format("%s-xml", this.rootElementName);
    }
  }

  /**
   * Factory method for creating {@link ReportXmlFormatter}.
   *
   * @param lintReport
   *          {@link LintReport} to build xml reports from.
   */
  public static ReportXmlFormatter create(final LintReport<LintItem> lintReport, final FormatterType formatterType) {
    return new ReportXmlFormatter(lintReport, formatterType);
  }

  public static ReportXmlFormatter createForLinterError(final LintReport<LinterError> lintReport,
      final FormatterType formatterType) {
    return createInternal(lintReport, formatterType, new Function<LinterError, LintItem>() {
      @Override
      public LintItem apply(final LinterError input)
          throws Exception {
        notNull(input);
        return new LintItemAdapter(input);
      }
    });
  }

  public static ReportXmlFormatter createForCssLintError(final LintReport<CssLintError> lintReport,
      final FormatterType formatterType) {
    return createInternal(lintReport, formatterType, new Function<CssLintError, LintItem>() {
      @Override
      public LintItem apply(final CssLintError input)
          throws Exception {
        notNull(input);
        return new LintItemAdapter(input);
      }
    });
  }

  /**
   * Creates a report which handles the adaptation of type <F> to {@link LintItem}.
   *
   * @param lintReport
   *          {@link LintReport} containing all lints.
   * @param formatterType
   *          the type of formatter to use.
   * @param adapter
   *          a {@link Function} responsible for adapting a type <F> into {@link LintItem}
   */
  private static <F> ReportXmlFormatter createInternal(final LintReport<F> lintReport,
      final FormatterType formatterType, final Function<F, LintItem> adapter) {
    Validate.notNull(lintReport);
    final LintReport<LintItem> report = new LintReport<LintItem>();
    for (final ResourceLintReport<F> reportItem : lintReport.getReports()) {
      final Collection<LintItem> lintItems = new ArrayList<LintItem>();
      for (final F lint : reportItem.getLints()) {
        try {
          LOG.debug("Adding lint: {}", lint);
          lintItems.add(adapter.apply(lint));
        } catch (final Exception e) {
          throw WroRuntimeException.wrap(e, "Problem while adapting lint item");
        }
      }
      report.addReport(ResourceLintReport.create(reportItem.getResourcePath(), lintItems));
    }
    return new ReportXmlFormatter(report, formatterType);
  }

  /**
   * @param lintReport
   *          a not null collection of {@link LinterError} used to build an XML report from.
   */
  protected ReportXmlFormatter(final LintReport<LintItem> lintReport, final FormatterType type) {
    super(lintReport);
    notNull(type);
    this.formatterType = type;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected void buildDocument() {
    final Element rootElement = getDocument().createElement(formatterType.rootElementName);
    getDocument().appendChild(rootElement);

    for (final ResourceLintReport<LintItem> resourceErrors : getLintReport().getReports()) {
      rootElement.appendChild(createFileElement(resourceErrors));
    }
  }

  /**
   * Creates a {@link Node} containing informations about all errors associated to a single resource.
   */
  private Node createFileElement(final ResourceLintReport<LintItem> resourceErrors) {
    final Element fileElement = getDocument().createElement(ELEMENT_FILE);
    fileElement.setAttribute(ATTR_NAME, resourceErrors.getResourcePath());
    for (final LintItem error : resourceErrors.getLints()) {
      fileElement.appendChild(createIssueElement(error));
    }
    return fileElement;
  }

  /**
   * Creates a {@link Node} containing a detailed description of an issue.
   */
  private Node createIssueElement(final LintItem error) {
    final Element issueElement = getDocument().createElement(getIssueElementName());

    final String column = String.valueOf(error.getColumn());
    if (StringUtils.isNotBlank(column)) {
      issueElement.setAttribute(getColumnAttributeName(), column);
    }

    final String evidence = error.getEvidence();
    if (StringUtils.isNotBlank(evidence)) {
      issueElement.setAttribute(ATTR_EVIDENCE, evidence);
    }

    final String line = String.valueOf(error.getLine());
    if (StringUtils.isNotBlank(line)) {
      issueElement.setAttribute(ATTR_LINE, line);
    }

    final String reason = error.getReason();
    if (StringUtils.isNotBlank(reason)) {
      issueElement.setAttribute(getReasonAttributeName(), reason);
    }

    final String severity = error.getSeverity();
    if (StringUtils.isNotBlank(severity)) {
      issueElement.setAttribute(ATTR_SEVERITY, severity);
    }
    return issueElement;
  }

  /**
   * @return the name of the attribute indicating the character number where the issue is located.
   */
  protected String getColumnAttributeName() {
    return formatterType == FormatterType.CHECKSTYLE ? ATTR_COLUMN : ATTR_CHARACTER;
  }

  /**
   * @return the name of the attribute indicating a reason of the issue.
   */
  protected String getReasonAttributeName() {
    return formatterType == FormatterType.CHECKSTYLE ? ATTR_MESSAGE : ATTR_REASON;
  }

  /**
   * @return name of tag indicating an issue.
   */
  protected String getIssueElementName() {
    return formatterType == FormatterType.CHECKSTYLE ? ELEMENT_ERROR : ELEMENT_ISSUE;
  }
}
