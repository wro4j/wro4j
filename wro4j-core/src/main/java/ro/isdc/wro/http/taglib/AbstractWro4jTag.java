/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.http.taglib;

import static java.util.EnumSet.copyOf;

import java.util.EnumSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.TagSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.model.group.InvalidGroupNameException;


/**
 * @author Alex Objelean
 * @created 13 Dec 2011
 * @since 1.4.3
 */
@SuppressWarnings("serial")
public abstract class AbstractWro4jTag extends TagSupport {
  private static final Logger LOG = LoggerFactory.getLogger(AbstractWro4jTag.class);
  private String name;
  private final EnumSet<ResourceHtmlTag> tags;


  AbstractWro4jTag(final ResourceHtmlTag tag) {
    this(EnumSet.of(tag));
  }


  AbstractWro4jTag(final EnumSet<ResourceHtmlTag> tags) {
    this.tags = copyOf(tags);
  }


  @Override
  public int doStartTag()
    throws JspException {
    if (name == null) {
      throw jspTagException("The name parameter is mandatory");
    }
    try {
      //pageContext.getServletContext().get
      final HttpServletRequest request = (HttpServletRequest)pageContext.getRequest();
      //createRenderer(tags).render(pageContext.getOut(), name, request.getContextPath());
//    } catch (final IOException e) {
//      throw jspTagException(e);
    } catch (final InvalidGroupNameException e) {
      throw jspTagException("Group is not in the config file: " + name);
    }
    return SKIP_BODY;
  }


  @Override
  public int doEndTag() {
    return EVAL_PAGE;
  }


  private JspTagException jspTagException(final String message)
    throws JspTagException {
    LOG.error("Wro4jTag error: " + message);
    return new JspTagException("Wro4jTag error: " + message);
  }


  private JspTagException jspTagException(final Exception e) {
    LOG.error("Wro4jTag error: " + e.getMessage(), e);
    return new JspTagException("Wro4jTag threw an exception; see logs for details");
  }
}
