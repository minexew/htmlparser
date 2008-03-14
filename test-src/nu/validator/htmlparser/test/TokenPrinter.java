/*
 * Copyright (c) 2007 Henri Sivonen
 * Copyright (c) 2008 Mozilla Foundation
 *
 * Permission is hereby granted, free of charge, to any person obtaining a 
 * copy of this software and associated documentation files (the "Software"), 
 * to deal in the Software without restriction, including without limitation 
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, 
 * and/or sell copies of the Software, and to permit persons to whom the 
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in 
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR 
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, 
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL 
 * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER 
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING 
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER 
 * DEALINGS IN THE SOFTWARE.
 */

package nu.validator.htmlparser.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import nu.validator.htmlparser.impl.TokenHandler;
import nu.validator.htmlparser.impl.Tokenizer;

import org.xml.sax.Attributes;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class TokenPrinter implements TokenHandler, ErrorHandler {

    private final Writer writer;
    
    public void characters(char[] buf, int start, int length)
            throws SAXException {
        try {
        boolean lineStarted = true;
        writer.write('-');
        for (int i = start; i < start + length; i++) {
            if (!lineStarted) {
                writer.write("\n-");                
                lineStarted = true;
            }
            char c = buf[i];
            if (c == '\n') {
                writer.write("\\n");                                
                lineStarted = false;                
            } else {
                writer.write(c);                
            }
        }
        writer.write('\n');
        } catch (IOException e) {
            throw new SAXException(e);
        }
    }

    public void comment(char[] buf, int length) throws SAXException {
        try {
            writer.write('!');
            writer.write(buf, 0, length);
            writer.write('\n');
        } catch (IOException e) {
            throw new SAXException(e);
        }
    }

    public void doctype(String name, String publicIdentifier, String systemIdentifier, boolean forceQuirks) throws SAXException {
        try {
            writer.write('D');
            writer.write(name);
            writer.write(' ');
            writer.write("" + forceQuirks);
            writer.write('\n');
        } catch (IOException e) {
            throw new SAXException(e);
        }
    }

    public void endTag(String name, Attributes attributes) throws SAXException {
        try {
            writer.write(')');
            writer.write(name);
            writer.write('\n');
            for (int i = 0; i < attributes.getLength(); i++) {
                writer.write('A');
                writer.write(attributes.getQName(i));
                writer.write(' ');
                writer.write(attributes.getValue(i));
                writer.write('\n');                
            }
        } catch (IOException e) {
            throw new SAXException(e);
        }
    }

    public void eof() throws SAXException {
    try {
        writer.write("E\n");
        writer.flush();
        writer.close();
    } catch (IOException e) {
        throw new SAXException(e);
    }        
    }

    public void start(Tokenizer self) throws SAXException {

    }

    public void startTag(String name, Attributes attributes)
            throws SAXException {
        try {
            writer.write('(');
            writer.write(name);
            writer.write('\n');
            for (int i = 0; i < attributes.getLength(); i++) {
                writer.write('A');
                writer.write(attributes.getQName(i));
                writer.write(' ');
                writer.write(attributes.getValue(i));
                writer.write('\n');                
            }
        } catch (IOException e) {
            throw new SAXException(e);
        }
    }

    public boolean wantsComments() throws SAXException {
        return true;
    }

    public static void main(String[] args) throws SAXException, IOException {
        TokenPrinter printer = new TokenPrinter(new OutputStreamWriter(System.out, "UTF-8")); 
        Tokenizer tokenizer = new Tokenizer(printer);
        tokenizer.setErrorHandler(printer);
        File file = new File(args[0]);
        InputSource is = new InputSource(new FileInputStream(file));
        is.setSystemId(file.toURI().toASCIIString());
        tokenizer.tokenize(is);
    }

    /**
     * @param writer
     */
    public TokenPrinter(final Writer writer) {
        this.writer = writer;
    }

    public void error(SAXParseException exception) throws SAXException {
        try {
            writer.write("R ");
            writer.write(exception.getMessage());
            writer.write("\n");
        } catch (IOException e) {
            throw new SAXException(e);
        }        
    }

    public void fatalError(SAXParseException exception) throws SAXException {
        try {
            writer.write("F ");
            writer.write(exception.getMessage());
            writer.write("\n");
        } catch (IOException e) {
            throw new SAXException(e);
        }        
    }

    public void warning(SAXParseException exception) throws SAXException {
        try {
            writer.write("W ");
            writer.write(exception.getMessage());
            writer.write("\n");
        } catch (IOException e) {
            throw new SAXException(e);
        }        
    }
    
}
