/**
 * Copyright 2008-2009 Jordi Hernández Sellés, Ibrahim Chaehoi
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
package ro.isdc.wro.extensions.processor.yui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

import org.apache.log4j.Logger;
import org.mozilla.javascript.ErrorReporter;
import org.mozilla.javascript.EvaluatorException;

/**
 * Implementation of Rhino's ErrorReporter, used as a callback to log errors encuntered while
 * parsing a javascript file.
 *
 * @author Jordi Hernández Sellés
 * @author Ibrahim Chaehoi
 */
public class YUIErrorReporter implements ErrorReporter {

	/** The logger */
	private static final Logger log = Logger.getLogger(YUIErrorReporter.class);

	/** The bundle content */
	private StringBuffer bundleData;

	/** The error line */
	private int errorLine;

	/** The YUI error message */
	private String yuiErrorMessage;

	/**
	 * Constructor
	 * @param status Current bundling status.
	 * @param bundleData Contents of the bundle, used when an error occurs to display the conflicting line.
	 */
	public YUIErrorReporter(final StringBuffer bundleData) {
		super();
		this.bundleData = bundleData;
	}



	/* (non-Javadoc)
	 * @see org.mozilla.javascript.ErrorReporter#error(java.lang.String, java.lang.String, int, java.lang.String, int)
	 */
	public void error(final String message, final String sourceName, final int line, final String lineSource, final int lineOffset) {

		// Only log the first error...
		if(this.errorLine < 1) {
			this.errorLine = line;
			this.yuiErrorMessage = message;
		}
		else if(this.errorLine == line) {
			this.yuiErrorMessage += "] [" + message;
		}
	}


	/**
	 * Creates an EvaluatorException that will be thrown by Rhino.
	 *
	 * @param message a String describing the error
	 * @param sourceName a String describing the JavaScript source where the error occured; typically a filename or URL
	 * @param line the line number associated with the error
	 * @param lineSource the text of the line (may be null)
	 * @param lineOffset the offset into lineSource where problem was detected
	 *
	 * @return an EvaluatorException that will be thrown.
	 */
	public EvaluatorException runtimeError(final String message, final String sourceName, final int line, final String lineSource, final int lineOffset) {
		final StringBuffer errorMsg = new StringBuffer("YUI failed to minify the bundle with id:.\n");
		errorMsg.append("YUI error message(s):[").append(this.yuiErrorMessage).append("]\n");
		errorMsg.append("The error happened at this point in your javascript: \n");
		errorMsg.append("_______________________________________________\n...\n");

		final BufferedReader rd = new BufferedReader(new StringReader(bundleData.toString()));
		String s;
		int totalLines = 0;
		final int start = this.errorLine - 10;

		try {
			while((s=rd.readLine())!=null){
				totalLines++;
				if(totalLines >= start && totalLines <= this.errorLine){
					errorMsg.append(s);
					if(totalLines == this.errorLine)
						errorMsg.append(" <-- ERROR");
					errorMsg.append("\n");
				}
			}
		} catch (final IOException e) {
			errorMsg.append("[Jawr suffered an IOException while attempting to show the faulty script]");
		}

		errorMsg.append("_______________________________________________");
		errorMsg.append("\nIf you can't find the error, try to check the scripts using JSLint (http://www.jslint.com/) to find the conflicting part of the code.\n");

    return new EvaluatorException(errorMsg.toString(), "sourceName", this.errorLine);
  }

	/* (non-Javadoc)
	 * @see org.mozilla.javascript.ErrorReporter#warning(java.lang.String, java.lang.String, int, java.lang.String, int)
	 */
	public void warning(final String message, final String sourceName, final int line, final String lineSource, final int lineOffset) {
		if(log.isDebugEnabled())
			log.debug(message);
	}

}
