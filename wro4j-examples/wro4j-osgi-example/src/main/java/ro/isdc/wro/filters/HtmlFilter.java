package ro.isdc.wro.filters;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.FileInputStream;
import java.io.IOException;

public class HtmlFilter implements Filter {

  final protected Logger log = LoggerFactory.getLogger(HtmlFilter.class);

  String contextPath;
  String webappDirectory;

  public HtmlFilter(String contextPath, String webappDirectory){

    this.contextPath = contextPath;
    this.webappDirectory = webappDirectory;

  }

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
    log.info("HtmlFilter started.");
  }

  @Override
  public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
    log.debug("Intercepting request...");

    HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
    String uri = httpServletRequest.getRequestURI();

    if(uri != null && uri.matches(".*\\.html$")){

      String sourceFile= null;

      try {

        log.debug("Processing html file!");

        String relpath = uri.replace(contextPath, "");
        if (relpath != null) {

          sourceFile = webappDirectory + relpath;

          log.debug("Attempting to serve {}", sourceFile);

          HttpServletResponse response = (HttpServletResponse) servletResponse;
          response.setContentType("text/html");
          IOUtils.copy(new FileInputStream(sourceFile), servletResponse.getOutputStream());

        }

      } catch (Exception e) {
        log.error("Problem occurred while trying to serve static html file", e);

        HttpServletResponse response = (HttpServletResponse) servletResponse;
        response.setContentType("text/html");
        String result = "<html><title>Error</title><body><p>Problem occurred when attempting to server file:" +
                sourceFile == null ? "(unknown)" : sourceFile +
                "</p></body></html>";

      }

    } else {
      // continue as normal
      filterChain.doFilter(servletRequest, servletResponse);
    }
  }

  @Override
  public void destroy() {
    log.info("HtmlFilter stopped.");
  }
}
