/*
 * Copyright (c) 2017. All rights reserved.
 */
package ro.isdc.wro.model.resource.locator;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ro.isdc.wro.model.resource.locator.support.LocatorProvider;

/**
 * Adjusted loader class for saas resources "scss" files. As so imports can now the achieved using
 * the following configuration in the <b>wro.properties</b>:
 * 
 * <pre>
 * managerFactoryClassName=ro.isdc.wro.manager.factory.ConfigurableWroManagerFactory
 * preProcessors=lessCssImport
 * postProcessors=rubySassCss
 * uriLocators=webjar,saasUri,uri,classpath
 * </pre>
 * 
 * @author Paul Sterl
 */
public class SaasUriLocator implements UriLocator {
    private static final Logger LOG = LoggerFactory.getLogger(SaasUriLocator.class);
    /**
     * Alias used to register this locator with {@link LocatorProvider}.
     */
    public static final String ALIAS = "saasUri";

    /**
     * {@inheritDoc}
     */
    public boolean accept(final String url) {
        if (url == null) return false;
        final String extension = FilenameUtils.getExtension(url);
        // scss file have either no extension or scss
        // maybe check for the "_"?
        if ("".equals(extension) || "scss".equals(extension)) {
            boolean result = getScssFile(url) != null;
            if (!result) {
                LOG.debug("Possible scss file not found {}", url);
            }
            return result;
        } else {
            return false;
        }
    }
    
    File getScssFile(String url) {
        if (url == null) return null;
        File result;
        // remove any "file:" at start
        if (url.startsWith("file:")) url = url.replace("file:", "");
        
        result = new File(url);
        if (result.isFile()) return result;
        result = new File(url + ".scss");
        if (result.isFile()) return result;

        // if we don't have a scss file end the ending isn't a / it is most likely an import
        if (!url.endsWith(".scss") && !url.endsWith("/")) {
            final int lastSlash = url.lastIndexOf('/') + 1;
            String cleanUrl = url.substring(0, lastSlash);
            cleanUrl = cleanUrl + "_" + url.substring(lastSlash, url.length()) + ".scss";
            result = new File(cleanUrl);
        } else {
            result = new File(url);
        }

        if (result.isFile()) return result;
        else return null;
    }

    /**
     * {@inheritDoc}
     */
    public InputStream locate(final String uri) throws IOException {
        Validate.notNull(uri, "URI cannot be NULL!");
        LOG.debug("loading  scss file: {}", uri);
        return new FileInputStream(getScssFile(uri));
    }
}
