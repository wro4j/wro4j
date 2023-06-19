/*
 * Copyright (C) 2011.
 * All rights reserved.
 */
package ro.isdc.wro.extensions.processor.css;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.extensions.processor.support.ObjectPoolHelper;
import ro.isdc.wro.extensions.processor.support.csslint.CssLint;
import ro.isdc.wro.extensions.processor.support.csslint.CssLintError;
import ro.isdc.wro.extensions.processor.support.csslint.CssLintException;
import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.SupportedResourceType;
import ro.isdc.wro.model.resource.processor.Destroyable;
import ro.isdc.wro.model.resource.processor.ResourcePostProcessor;
import ro.isdc.wro.model.resource.processor.ResourcePreProcessor;
import ro.isdc.wro.util.ObjectFactory;

/**
 * Processor which analyze the css code and warns you found problems. The processing result won't change no matter if the processed script
 * contains errors or not. The underlying implementation uses CSSLint script utility
 * <a href="https://github.com/stubbornella/csslint">https://github.com/stubbornella/csslint</a>.
 *
 * @author Alex Objelean
 * @since 1.3.8
 */
@SupportedResourceType(ResourceType.CSS)
public class CssLintProcessor implements ResourcePreProcessor, ResourcePostProcessor, Destroyable {
    private static final Logger LOG = LoggerFactory.getLogger(CssLintProcessor.class);
    public static final String ALIAS = "cssLint";
    /**
     * CSV Options.
     */
    private String options;

    private ObjectPoolHelper<CssLint> enginePool;

    public CssLintProcessor() {
        enginePool = new ObjectPoolHelper<CssLint>(new ObjectFactory<CssLint>() {
            @Override
            public CssLint create() {
                return newCssLint();
            }
        });
    }

    @Override
    public void process(final Resource resource, final Reader reader, final Writer writer) throws IOException {
        final String content = IOUtils.toString(reader);
        final CssLint cssLint = enginePool.getObject();
        try {
            cssLint.setOptions(getOptions()).validate(content);
        } catch (final CssLintException e) {
            onCssLintException(e, resource);
        } catch (final WroRuntimeException e) {
            final String resourceUri = resource == null ? StringUtils.EMPTY : "[" + resource.getUri() + "]";
            LOG.error("Exception while applying " + ALIAS + " processor on the " + resourceUri
                    + " resource, no processing applied...", e);
            onException(e);
        } finally {
            // don't change the processed content no matter what happens.
            writer.write(content);
            reader.close();
            writer.close();
            enginePool.returnObject(cssLint);
        }
    }

    /**
     * Invoked when an unexpected exception occurred during processing. By default the exception is thrown further.
     */
    protected void onException(final WroRuntimeException e) {
        throw e;
    }

    /**
     * @return {@link CssLint} instance.
     */
    protected CssLint newCssLint() {
        return new CssLint();
    }

    @Override
    public void process(final Reader reader, final Writer writer) throws IOException {
        process(null, reader, writer);
    }

    /**
     * Called when {@link CssLintException} is thrown. Allows subclasses to re-throw this exception as a {@link RuntimeException} or handle
     * it differently.
     *
     * @param e {@link CssLintException} which has occurred.
     * @param resource the processed resource which caused the exception.
     */
    protected void onCssLintException(final CssLintException e, final Resource resource) {
        final String uri = resource == null ? StringUtils.EMPTY : resource.getUri();
        LOG.error("The following resource: " + uri + " has " + e.getErrors().size() + " errors.");
        for (final CssLintError x : e.getErrors()) {
            LOG.error(uri + " line " + x.getLine() + " column " + x.getCol() + ": " + x.getType() + " "
                    + x.getMessage());
        }
    }

    /**
     * @param options a CSV representation of options.
     */
    public CssLintProcessor setOptionsAsString(final String options) {
        this.options = options;
        return this;
    }

    /**
     * @return an options as CSV.
     */
    private String getOptions() {
        if (options == null) {
            options = createDefaultOptions();
        }
        return options;
    }

    /**
     * @return default options to use for linting.
     */
    protected String createDefaultOptions() {
        return StringUtils.EMPTY;
    }

    @Override
    public void destroy() throws Exception {
        enginePool.destroy();
    }
}
