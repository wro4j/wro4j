/*
 * YUI Compressor
 * Author: Julien Lecomte - http://www.julienlecomte.net/
 * Copyright (c) 2009 Yahoo! Inc.  All rights reserved.
 * The copyrights embodied in the content of this file are licensed
 * by Yahoo! Inc. under the BSD (revised) open source license.
 */

package com.yahoo.platform.yui.compressor;


public class YUICompressor {


    private static void usage() {
        System.err.println(
                "\nUsage: java -jar yuicompressor-x.y.z.jar [options] [input file]\n\n"

                        + "Global Options\n"
                        + "  -h, --help                Displays this information\n"
                        + "  --type <js|css>           Specifies the type of the input file\n"
                        + "  --charset <charset>       Read the input file using <charset>\n"
                        + "  --line-break <column>     Insert a line break after the specified column number\n"
                        + "  -v, --verbose             Display informational messages and warnings\n"
                        + "  -o <file>                 Place the output into <file>. Defaults to stdout.\n"
                        + "                            Multiple files can be processed using the following syntax:\n"
                        + "                            java -jar yuicompressor.jar -o '.css$:-min.css' *.css\n"
                        + "                            java -jar yuicompressor.jar -o '.js$:-min.js' *.js\n\n"

                        + "JavaScript Options\n"
                        + "  --nomunge                 Minify only, do not obfuscate\n"
                        + "  --preserve-semi           Preserve all semicolons\n"
                        + "  --disable-optimizations   Disable all micro optimizations\n\n"

                        + "If no input file is specified, it defaults to stdin. In this case, the 'type'\n"
                        + "option is required. Otherwise, the 'type' option is required only if the input\n"
                        + "file extension is neither 'js' nor 'css'.");
    }
}
