/*
* Copyright 2011 France Télécom
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
*/

package ro.isdc.wro.model.resource.util;

import java.io.InputStream;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * This naming strategy append a timestamp to the name of the file.
 * 
 * This is especially useful when wro4j is used at build time with the
 * maven plugin.
 * 
 * @author Julien Wajsberg
 */
public class TimestampNamingStrategy implements NamingStrategy {

	public String rename(String originalName, InputStream inputStream) {
	    final String baseName = FilenameUtils.getBaseName(originalName);
	    final String extension = FilenameUtils.getExtension(originalName);
	    final long timestamp = getTimestamp();
	    final StringBuilder sb = new StringBuilder(baseName).append("-").append(timestamp);
	    if (!StringUtils.isEmpty(extension)) {
	      sb.append(".").append(extension);
	    }
	    return sb.toString();
	}
	
	/* protected to make the class testable. Should we ? */
	protected long getTimestamp() {
		return System.currentTimeMillis();
	}
	
}
