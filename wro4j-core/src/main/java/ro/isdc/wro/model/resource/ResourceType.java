/*
 * Copyright (c) 2008. All rights reserved.
 */
package ro.isdc.wro.model.resource;

import static org.apache.commons.lang3.Validate.isTrue;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * Make a distinction between resource type. Can be CSS or JS.
 *
 * @author Alex Objelean
 * @created Created on Oct 30, 2008
 */
public enum ResourceType {
    CSS {
        @Override
        public String getContentType() {
            return "text/css";
        }
    },
    JS {
        @Override
        public String getContentType() {
            return "text/javascript";
        }
    },
    CSS_MAP {
        @Override
        public String getContentType() {
            return "application/json";
        }
        @Override
        public ResourceType getSourceType() {
            return CSS;
        }
    },
    JS_MAP {
        @Override
        public String getContentType() {
            return "application/json";
        }

        @Override
        public ResourceType getSourceType() {
            return JS;
        }
    };

    public static final String MAP_PROCESSOR = "googleClosureSourceMap";
    private static final String MAP_EXTENSION = "map";

    /**
     * @return the content type of the resource type.
     */
    public abstract String getContentType();

    /**
     * <tt>JS -> JS<br />
     * JS_MAP -> JS</tt>
     * 
     * @return the source type of the resource type.
     */
    public ResourceType getSourceType() {
        return this;
    }

    /**
     * @return {@link ResourceType} associated to the string representation of
     *         the type.
     */
    public static ResourceType get(final String uri) {
        isTrue(uri != null, "ResourceType cannot be NULL.");
        String uriExtension = FilenameUtils.getExtension(uri);
        if (StringUtils.equalsIgnoreCase(MAP_EXTENSION, uriExtension)) {
            String mapType = FilenameUtils.getExtension(FilenameUtils.getBaseName(uri));
            return ResourceType.valueOf(String.format("%S_%S", mapType, MAP_EXTENSION));
        } else if (StringUtils.isNotBlank(uriExtension)) {
            return ResourceType.valueOf(uriExtension.toUpperCase());
        } else {
            return ResourceType.valueOf(uri);
        }
    }
    
    public static ResourceType[] defaultSupported() {
        return new ResourceType[]{ResourceType.CSS, ResourceType.JS};
    }
}
