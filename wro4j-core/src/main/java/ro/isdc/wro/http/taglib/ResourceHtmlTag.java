/*
 * Copyright 2008-2011 Microarray Informatics Team, EMBL-European Bioinformatics Institute
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 * For further details of the Gene Expression Atlas project, including source code,
 * downloads and documentation, please see:
 *
 * http://gxa.github.com/gxa
 */
package ro.isdc.wro.http.taglib;

import java.util.EnumMap;

import ro.isdc.wro.model.resource.ResourceType;

/**
 * Custom extension for  {@link ResourceType}
 */
public enum ResourceHtmlTag {
    CSS(ResourceType.CSS, "css", "<link type=\"text/css\" rel=\"stylesheet\" href=\"%s\"/>"),
    JS(ResourceType.JS, "js", "<script type=\"text/javascript\" src=\"%s\"></script>");

    private static final EnumMap<ResourceType, ResourceHtmlTag> BY_TYPE = new EnumMap<ResourceType, ResourceHtmlTag>(ResourceType.class);

    private ResourceType type;
    private String extension;
    private String tag;

    ResourceHtmlTag(final ResourceType type, final String extension, final String tag) {
        this.type = type;
        this.extension = extension;
        this.tag = tag;
    }

    public ResourceType getType() {
        return type;
    }

    public String getExtension() {
        return extension;
    }

    public String render(final String uri) {
        return String.format(tag, uri);
    }

    public static ResourceHtmlTag forType(final ResourceType type) {
        return BY_TYPE.get(type);
    }

    static {
        // Prepare lookup table
        for (final ResourceHtmlTag tag : ResourceHtmlTag.values()) {
            BY_TYPE.put(tag.type, tag);
        }
        // Make sure every ResourceType is supported
        for (final ResourceType type : ResourceType.values()) {
            if (!BY_TYPE.containsKey(type)) {
                throw new IllegalStateException("Cannot find mapping for " + type);
            }
        }
    }
}
