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
* 
* This was inspired by TestFingerprintCreatorNamingStrategy.
*/

package ro.isdc.wro.model.resource.util;

import static junit.framework.Assert.*;


import org.junit.Before;
import org.junit.Test;

import ro.isdc.wro.model.resource.util.NamingStrategy;

/**
 * Test class for {@link TimestampNamingStrategy}
 *
 * @author Julien Wajsberg
 * @created 05 dec 2011
 */
public class TestTimestampNamingStrategy {
  private NamingStrategy namingStrategy;

  private static long TIMESTAMP = 123456789;
  
  @Before
  public void setUp() {
    namingStrategy = new TimestampNamingStrategy() {
    	@Override
    	protected long getTimestamp() {
    		return TIMESTAMP;
    	}
    };
  }

  @Test
  public void testWithExtension() throws Exception {
    //second argument doesn't matter.
    final String result = namingStrategy.rename("fileName.js", null);
    assertEquals("fileName-" + TIMESTAMP + ".js", result);
  }

  @Test
  public void testNoExtension() throws Exception {
  //second argument doesn't matter.
    final String result = namingStrategy.rename("fileName", null);
    assertEquals("fileName-" + TIMESTAMP, result);
  }
}
