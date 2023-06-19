/*
 * Copyright (c) 2008. All rights reserved.
 */
package ro.isdc.wro.http;

import java.io.IOException;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import ro.isdc.wro.config.Context;
import ro.isdc.wro.config.jmx.WroConfiguration;
import ro.isdc.wro.http.support.ServletContextAttributeHelper;

/**
 * This filter is responsible for setting the {@link Context} to the current
 * request cycle. This is required if you want to use
 * {@link ServletContextAttributeHelper} in order to access wro related
 * attributes from within a tag or a servlet. Usually this filter will be mapped
 * to all requests using the {@code /*} URL pattern on the filter named
 * {@code wroContextFilter}.
 * 
 * @author Alex Objelean
 * @since 1.4.7
 */
public class WroContextFilter implements Filter {
	private FilterConfig filterConfig;

	/**
	 * {@inheritDoc}
	 */
	public void init(final FilterConfig filterConfig) throws ServletException {
		this.filterConfig = filterConfig;
	}

	/**
	 * {@inheritDoc}
	 */
	public final void doFilter(final ServletRequest req, final ServletResponse res, final FilterChain chain)
			throws IOException, ServletException {
		final HttpServletRequest request = (HttpServletRequest) req;
		final HttpServletResponse response = (HttpServletResponse) res;
		// preserve current correlationId to ensure proper clean up during nested
		// requests.
		final String originalCorrelationId = Context.isContextSet() ? Context.getCorrelationId() : null;
		Context.set(Context.webContext(request, response, this.filterConfig), getWroConfiguration());
		final String correlationId = Context.getCorrelationId();
		try {
			chain.doFilter(request, response);
		} finally {
			Context.setCorrelationId(correlationId);
			Context.unset();
			if (originalCorrelationId != null) {
				Context.setCorrelationId(originalCorrelationId);
			}
		}
	}

	/**
	 * @return the {@link WroConfiguration} extracted from {@link ServletContext} if
	 *         exist or default one otherwise.
	 */
	private WroConfiguration getWroConfiguration() {
		final WroConfiguration configAttribute = getServletContextAttributeHelper().getWroConfiguration();
		return configAttribute != null ? configAttribute : new WroConfiguration();
	}

	/**
	 * @VisibleForTesting
	 * @return the instance responsible for {@link WroConfiguration} lookup.
	 */
	ServletContextAttributeHelper getServletContextAttributeHelper() {
		return ServletContextAttributeHelper.create(filterConfig);
	}

	/**
	 * {@inheritDoc}
	 */
	public void destroy() {
		Context.destroy();
	}
}
