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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import ro.isdc.wro.model.resource.ResourceType;


public class TestResourceHtmlTag {
  @Test
  public void allTypesAreFoundAndSenbsible() {
    for (final ResourceType type : ResourceType.values()) {
      assertTrue(ResourceHtmlTag.forType(type).getType() == type);
    }
  }


  @Test
  public void htmlTags() {
    assertEquals("<link type=\"text/css\" rel=\"stylesheet\" href=\"/context/stylesheet.css\"/>",
      ResourceHtmlTag.CSS.render("/context/stylesheet.css"));
    assertEquals("<script type=\"text/javascript\" src=\"/js/script.js\"></script>",
      ResourceHtmlTag.JS.render("/js/script.js"));
  }
}
