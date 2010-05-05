/*
 * Copyright (c) 2009 Nicholas C. Zakas. All rights reserved.
 * http://www.nczonline.net/
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
 
package net.nczonline.web.cssembed;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import net.nczonline.web.datauri.DataURIGenerator;

/**
 * Generator for Data URIs.
 * @author Nicholas C. Zakas
 */
public class CSSURLEmbedder { 
    
    public static final int DATAURI_OPTION = 1;
    public static final int MHTML_OPTION = 2;
    
    protected static String MHTML_SEPARATOR = "CSSEmbed_Image";
    
    private static HashSet<String> imageTypes;    
    static {
        imageTypes = new HashSet<String>();
        imageTypes.add("jpg");
        imageTypes.add("jpeg");
        imageTypes.add("gif");
        imageTypes.add("png");
    }        
    
    private boolean verbose = false;
    private String code = null;
    private int options = 1;
    private String mhtmlRoot = "";
    private String outputFilename = "";
    
    //--------------------------------------------------------------------------
    // Constructors
    //--------------------------------------------------------------------------    
    
    public CSSURLEmbedder(Reader in) throws IOException {
        this(in, false);
    }
    
    public CSSURLEmbedder(Reader in, int options) throws IOException {
        this(in, false);
    }
    
    public CSSURLEmbedder(Reader in, boolean verbose) throws IOException {
        this(in,1,verbose);
    }
    
    public CSSURLEmbedder(Reader in, int options, boolean verbose) throws IOException {
        this.code = readCode(in);
        this.verbose = verbose;
        this.options = options;
    }
    
    //--------------------------------------------------------------------------
    // Get/Set verbose flag
    //--------------------------------------------------------------------------    
    
    public boolean getVerbose(){
        return verbose;
    }
    
    public void setVerbose(boolean newVerbose){
        verbose = newVerbose;
    }
    
    //--------------------------------------------------------------------------
    // Determine if an option is set - Options support not yet complete
    //--------------------------------------------------------------------------    
    
    private boolean hasOption(int option){
        return (options & option) > 0;
    }

    //--------------------------------------------------------------------------
    // MHTML Support
    //--------------------------------------------------------------------------    
    
    public String getMHTMLRoot(){
        return mhtmlRoot;
    }

    public void setMHTMLRoot(String mhtmlRoot){
        this.mhtmlRoot = mhtmlRoot;
    }
    
    public String getFilename(){
        return outputFilename;
    }
    
    public void setFilename(String filename){
        this.outputFilename = filename;
    }
    
    //--------------------------------------------------------------------------
    // Embed images
    //--------------------------------------------------------------------------
    
    /**
     * Embeds data URI images into a CSS file.
     * @param out The place to write out the source code.
     * @throws java.io.IOException
     */
    public void embedImages(Writer out) throws IOException {
        embedImages(out, null);
    }
        
    /**
     * Embeds data URI images into a CSS file.
     * @param out The place to write out the source code.
     * @param root The root to prepend to any relative paths.
     * @throws java.io.IOException
     */
    public void embedImages(Writer out, String root) throws IOException {
        BufferedReader reader = new BufferedReader(new StringReader(code));        
        StringBuilder builder = new StringBuilder();
        StringBuilder mhtmlHeader = new StringBuilder();
        HashMap<String,Integer> foundMedia = new HashMap<String,Integer>();
        
        String line;
        int lineNum = 1;        
        
        //create initial MHTML code
        if (hasOption(MHTML_OPTION)){
            mhtmlHeader.append("/*\n");
            mhtmlHeader.append("Content-Type: multipart/related; boundary=\"");
            mhtmlHeader.append(MHTML_SEPARATOR);
            mhtmlHeader.append("\"\n\n");
        }
        
        while((line = reader.readLine()) != null){
            
            int start = 0;
            int pos = line.indexOf("url(", start);
            int npos;
            
            if (lineNum > 1){
                builder.append("\n");
            }
            
            if (pos > -1){
                while (pos > -1){
                    pos += 4;
                    builder.append(line.substring(start, pos));
                    npos = line.indexOf(")", pos);
                    String url = line.substring(pos, npos).trim();
                    
                    //eliminate quotes at the beginning and end
                    if (url.startsWith("\"")){
                        if (url.endsWith("\"")){
                            url = url.substring(1, url.length()-1);
                        } else {
                            throw new IOException("Invalid CSS URL format (" + url + ") at line " + lineNum + ", col " + pos + ".");
                        }                        
                    } else if (url.startsWith("'")){
                        if (url.endsWith("'")){
                            url = url.substring(1, url.length()-1);
                        } else {
                            throw new IOException("Invalid CSS URL format (" + url + ") at line " + lineNum + ", col " + pos + ".");
                        }                         
                    }
                    
                    //check for duplicates
                    if (foundMedia.containsKey(url)){
                        if (verbose){
                            System.err.println("[WARNING] Duplicate URL '" + url + "' found at line " + lineNum + ", previously declared at line " + foundMedia.get(url) + ".");
                        }                        
                    }                    
                    foundMedia.put(url, lineNum);                    
                    
                    
                    //Begin processing URL
                    String newUrl = url;                    
                    if (verbose){
                        System.err.println("[INFO] Found URL '" + url + "' at line " + lineNum + ", col " + pos + ".");
                    }
                    if (url.indexOf("http:") != 0 && root != null){
                        newUrl = root + url;
                        if (verbose){
                            System.err.println("[INFO] Applying root to URL, URL is now '" + newUrl + "'.");
                        }                        
                    }
                    
                    //get the data URI format
                    String uriString = getImageURIString(newUrl, url);

                    //if it doesn't begin with data:, it's not a data URI
                    if (uriString.startsWith("data:")){

                        //IE8 only allows dataURIs up to 32KB
                        if (verbose && uriString.length() > 32768){
                            System.err.println("[WARNING] File " + newUrl + " creates a data URI larger than 32KB. IE8 can't display data URI images this large.");
                        }

                        /*
                         * Determine what to do. Eventually, you should be able to
                         * have both a data URI and MHTML in the same file.
                         */
                        if (hasOption(MHTML_OPTION)){

                            String entryName = getFilename(url);

                            //create MHTML header entry
                            mhtmlHeader.append("--");
                            mhtmlHeader.append(MHTML_SEPARATOR);
                            mhtmlHeader.append("\nContent-Location:");
                            mhtmlHeader.append(entryName);
                            mhtmlHeader.append("\nContent-Transfer-Encoding:base64\n\n");
                            mhtmlHeader.append(uriString.substring(uriString.indexOf(",")+1));
                            mhtmlHeader.append("\n");

                            //output the URI
                            builder.append("mhtml:");
                            builder.append(getMHTMLPath());
                            builder.append("!");
                            builder.append(entryName);
                        } else if (hasOption(DATAURI_OPTION)){
                            builder.append(uriString);
                        }
                    } else {
                        //TODO: Clean up, duplicate code
                        builder.append(uriString);
                    }

                    start = npos;                    
                    pos = line.indexOf("url(", start);
                } 
                
                //finish out the line
                if (start < line.length()){
                    builder.append(line.substring(start));
                }
            } else {
                builder.append(line);
            }
            
            lineNum++;
        }
        reader.close();

        if (hasOption(MHTML_OPTION)){
            mhtmlHeader.append("*/\n");
            out.write(mhtmlHeader.toString());
        }
        out.write(builder.toString());        
    }
    
    /**
     * Returns a URI string for the given URL. If the URL is for an image, 
     * the data URI will be returned. If the URL is not for an image, then the
     * original URI is returned.
     * @param url The URL to attempt to read.
     * @param originalUrl The original URL as stated in the source code.
     * @return The appropriate data URI to use.
     * @throws java.io.IOException
     */
    private String getImageURIString(String url, String originalUrl) throws IOException {
        
        //check the extension - only encode for images
        String fileType = url.substring(url.lastIndexOf(".") + 1);
        
        //it's an image, so encode it
        if (imageTypes.contains(fileType)){
            
            DataURIGenerator.setVerbose(verbose);
            
            StringWriter writer = new StringWriter();
            
            if (url.startsWith("http://")){
                if (verbose){
                    System.err.println("[INFO] Downloading '" + url + "' to generate data URI.");
                }                
                
                DataURIGenerator.generate(new URL(url), writer); 
              
            } else {
                if (verbose){
                    System.err.println("[INFO] Opening file '" + url + "' to generate data URI.");
                }                
                
                File file = new File(url);
                
                if (verbose && !file.isFile()){
                    System.err.println("[INFO] Could not find file '" + file.getCanonicalPath() + "'.");
                }                 
                
                DataURIGenerator.generate(new File(url), writer); 
            }

            if (verbose){
                System.err.println("[INFO] Generated data URI for '" + url + "'.");
            }              
            
            return writer.toString();
            
        } else {
            
            if (verbose){
                System.err.println("[INFO] URL '" + originalUrl + "' is not an image, skipping.");
            }
            
            //not an image, ignore
            return originalUrl;
        }
        
    }
    
    private String getFilename(String path){
        if (path.indexOf("/") > -1){
            return path.substring(path.lastIndexOf("/")+1);
        } else if (path.indexOf("\\") > -1){
            return path.substring(path.lastIndexOf("\\")+1);
        } else {
            return path;
        }
    }
    
    private String getMHTMLPath(){
        String result = mhtmlRoot;
        if (!result.endsWith("/")){
            result += "/";
        }
        
        result += outputFilename;
        
        return result;
    }
    
    private String readCode(Reader in) throws IOException {
        StringBuilder builder = new StringBuilder();
        int c;
        
        while ((c = in.read()) != -1){
            builder.append((char)c);
        }
        
        in.close();
        return builder.toString();
    }
            
}
