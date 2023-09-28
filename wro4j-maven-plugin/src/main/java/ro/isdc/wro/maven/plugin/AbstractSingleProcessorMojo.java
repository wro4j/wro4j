/**
 * Copyright Alex Objelean
 */
package ro.isdc.wro.maven.plugin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Callable;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Parameter;
import org.mockito.Mockito;

import jakarta.servlet.FilterConfig;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import ro.isdc.wro.config.Context;
import ro.isdc.wro.config.jmx.WroConfiguration;
import ro.isdc.wro.extensions.manager.standalone.ExtensionsStandaloneManagerFactory;
import ro.isdc.wro.http.support.DelegatingServletOutputStream;
import ro.isdc.wro.manager.factory.WroManagerFactory;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;
import ro.isdc.wro.model.resource.processor.factory.ProcessorsFactory;
import ro.isdc.wro.model.resource.processor.factory.SimpleProcessorsFactory;
import ro.isdc.wro.util.io.NullOutputStream;

/**
 * Maven plugin which use a single processor.
 *
 * @author Alex Objelean
 * @author Paul Podgorsek
 */
public abstract class AbstractSingleProcessorMojo extends AbstractWro4jMojo {

	/**
	 * Comma separated options. This field is optional. If no value is provided, no
	 * options will be used..
	 */
	@Parameter
	private String options;

	/**
	 * When true, all the plugin won't stop its execution and will log all found
	 * errors.
	 */
	@Parameter(defaultValue = "false")
	private boolean failNever;

	@Override
	public final void doExecute() throws Exception {
		getLog().info("options: " + options);

		final Collection<Callable<Void>> callables = new ArrayList<Callable<Void>>();

		final Collection<String> groupsAsList = getTargetGroupsAsList();
		for (final String group : groupsAsList) {
			for (final ResourceType resourceType : ResourceType.values()) {
				if (isParallelProcessing()) {
					callables.add(Context.decorate(new Callable<Void>() {
						public Void call() throws Exception {
							processGroup(group, resourceType);
							return null;
						}
					}));
				} else {
					processGroup(group, resourceType);
				}
			}
		}
		if (isParallelProcessing()) {
			getTaskExecutor().submit(callables);
		}
	}

	/**
	 * @param group the name of the group to process.
	 */
	private void processGroup(final String groupName, final ResourceType resourceType) throws Exception {
		if (wantProcessGroup(groupName, resourceType)) {
			// group With Extension
			final String group = groupName + "." + resourceType.name().toLowerCase();
			getLog().info("processing group: " + group);

			// mock request
			final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
			Mockito.when(request.getRequestURI()).thenReturn(group);
			// mock response
			final HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
			Mockito.when(response.getOutputStream())
					.thenReturn(new DelegatingServletOutputStream(new NullOutputStream()));

			// init context
			final WroConfiguration config = Context.get().getConfig();
			Context.set(Context.webContext(request, response, Mockito.mock(FilterConfig.class)), config);
			// perform processing
			getManagerFactory().create().process();

			getLog().debug("Processing group: " + group + " [OK]");
		}
	}

	/**
	 * Allow subclasses to decide if a group needs to be processed. Useful for
	 * plugins which process only resources of a certain type (ex: jshint).
	 *
	 * @return true if the resource of a certain type from a group should be
	 *         processed.
	 */
	protected boolean wantProcessGroup(final String groupName, final ResourceType resourceType) {
		return true;
	}

	/**
	 * Initialize the manager factory with a processor factory using a single
	 * processor.
	 */
	@Override
	protected final WroManagerFactory newWroManagerFactory() throws MojoExecutionException {
		return new ExtensionsStandaloneManagerFactory().setProcessorsFactory(createSingleProcessorsFactory());
	}

	private ProcessorsFactory createSingleProcessorsFactory() {
		final SimpleProcessorsFactory factory = new SimpleProcessorsFactory();
		final ResourcePreProcessor processor = createResourceProcessor();
		factory.addPreProcessor(processor);
		return factory;
	}

	/**
	 * Factory method responsible for creating the processor which will be applied
	 * for this build.
	 */
	protected abstract ResourcePreProcessor createResourceProcessor();

	/**
	 * @return raw representation of the option value.
	 */
	protected String getOptions() {
		return options;
	}

	/**
	 * Used for tests only.
	 *
	 * @param options the options to set
	 */
	void setOptions(final String options) {
		this.options = options;
	}

	/**
	 * @param failNever the failFast to set
	 */
	public void setFailNever(final boolean failNever) {
		this.failNever = failNever;
	}

	/**
	 * @return the failNever
	 */
	public boolean isFailNever() {
		return failNever;
	}

}
