package ro.isdc.wro.extensions.support.lint;

import static org.apache.commons.lang3.Validate.notNull;

import java.io.OutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.extensions.processor.support.linter.LinterError;


/**
 * Builds an XML report for lint errors based on provided collection of {@link LinterError}'s.
 * 
 * @author Alex Objelean
 * @since 1.5.0
 */
public abstract class AbstractReportXmlFormatter<T> {
  private Document doc;
  private LintReport<T> lintReport;
  /**
   * @param lintReport
   *          a not null collection of {@link LinterError} used to build an XML report from.
   */
  protected AbstractReportXmlFormatter(final LintReport<T> lintReport) {
    notNull(lintReport);
    this.lintReport = lintReport;
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
  public final void write(final OutputStream outputStream) {
    buildDocument();
    writeReport(outputStream);
  }

  /**
   * Populates document with required DOM elements.
   */
  protected abstract void buildDocument();
  
  /**
   * @return {@link LintReport} used to build the xml lint report from.
   */
  protected final LintReport<T> getLintReport() {
    return this.lintReport;
  }
  
  /**
   * @return the {@link Document} used to build xml report.
   */
  protected final Document getDocument() {
    return doc;
  }

  /**
   * write the content to the {@link OutputStream}
   */
  private void writeReport(final OutputStream outputStream) {
    Transformer transformer;
    try {
      transformer = TransformerFactory.newInstance().newTransformer();
      transformer.transform(new DOMSource(doc), new StreamResult(outputStream));
    } catch (Exception e) {
      throw WroRuntimeException.wrap(e, "Problem during Document transformation").logError();
    }
  }
}
