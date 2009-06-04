/*
 * Copyright (c) 2005-2007 Henri Sivonen
 * Copyright (c) 2007-2009 Mozilla Foundation
 * Portions of comments Copyright 2004-2008 Apple Computer, Inc., Mozilla 
 * Foundation, and Opera Software ASA.
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

/*
 * The comments following this one that use the same comment syntax as this 
 * comment are quotes from the WHATWG HTML 5 spec as of 2 June 2007 
 * amended as of June 18 2008.
 * That document came with this statement:
 * "© Copyright 2004-2008 Apple Computer, Inc., Mozilla Foundation, and 
 * Opera Software ASA. You are granted a license to use, reproduce and 
 * create derivative works of this document."
 */

package nu.validator.htmlparser.impl;

import nu.validator.htmlparser.annotation.Inline;
import nu.validator.htmlparser.annotation.Local;
import nu.validator.htmlparser.annotation.NoLength;
import nu.validator.htmlparser.common.EncodingDeclarationHandler;
import nu.validator.htmlparser.common.TokenHandler;
import nu.validator.htmlparser.common.XmlViolationPolicy;

import org.xml.sax.ErrorHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * An implementation of
 * http://www.whatwg.org/specs/web-apps/current-work/multipage/tokenization.html
 * 
 * This class implements the <code>Locator</code> interface. This is not an
 * incidental implementation detail: Users of this class are encouraged to make
 * use of the <code>Locator</code> nature.
 * 
 * By default, the tokenizer may report data that XML 1.0 bans. The tokenizer
 * can be configured to treat these conditions as fatal or to coerce the infoset
 * to something that XML 1.0 allows.
 * 
 * @version $Id$
 * @author hsivonen
 */
public class Tokenizer implements Locator {

    public static final int DATA = 0;

    public static final int RCDATA = 1;

    public static final int CDATA = 2;

    public static final int PLAINTEXT = 3;

    private static final int TAG_OPEN = 49;

    private static final int CLOSE_TAG_OPEN_PCDATA = 50;

    private static final int TAG_NAME = 58;

    private static final int BEFORE_ATTRIBUTE_NAME = 4;

    private static final int ATTRIBUTE_NAME = 5;

    private static final int AFTER_ATTRIBUTE_NAME = 6;

    private static final int BEFORE_ATTRIBUTE_VALUE = 7;

    private static final int ATTRIBUTE_VALUE_DOUBLE_QUOTED = 8;

    private static final int ATTRIBUTE_VALUE_SINGLE_QUOTED = 9;

    private static final int ATTRIBUTE_VALUE_UNQUOTED = 10;

    private static final int AFTER_ATTRIBUTE_VALUE_QUOTED = 11;

    private static final int BOGUS_COMMENT = 12;

    private static final int MARKUP_DECLARATION_OPEN = 13;

    private static final int DOCTYPE = 14;

    private static final int BEFORE_DOCTYPE_NAME = 15;

    private static final int DOCTYPE_NAME = 16;

    private static final int AFTER_DOCTYPE_NAME = 17;

    private static final int BEFORE_DOCTYPE_PUBLIC_IDENTIFIER = 18;

    private static final int DOCTYPE_PUBLIC_IDENTIFIER_DOUBLE_QUOTED = 19;

    private static final int DOCTYPE_PUBLIC_IDENTIFIER_SINGLE_QUOTED = 20;

    private static final int AFTER_DOCTYPE_PUBLIC_IDENTIFIER = 21;

    private static final int BEFORE_DOCTYPE_SYSTEM_IDENTIFIER = 22;

    private static final int DOCTYPE_SYSTEM_IDENTIFIER_DOUBLE_QUOTED = 23;

    private static final int DOCTYPE_SYSTEM_IDENTIFIER_SINGLE_QUOTED = 24;

    private static final int AFTER_DOCTYPE_SYSTEM_IDENTIFIER = 25;

    private static final int BOGUS_DOCTYPE = 26;

    private static final int COMMENT_START = 27;

    private static final int COMMENT_START_DASH = 28;

    private static final int COMMENT = 29;

    private static final int COMMENT_END_DASH = 30;

    private static final int COMMENT_END = 31;

    private static final int CLOSE_TAG_OPEN_NOT_PCDATA = 32;

    private static final int MARKUP_DECLARATION_HYPHEN = 33;

    private static final int MARKUP_DECLARATION_OCTYPE = 34;

    private static final int DOCTYPE_UBLIC = 35;

    private static final int DOCTYPE_YSTEM = 36;

    private static final int CONSUME_CHARACTER_REFERENCE = 37;

    private static final int CONSUME_NCR = 38;

    private static final int CHARACTER_REFERENCE_LOOP = 39;

    private static final int HEX_NCR_LOOP = 41;

    private static final int DECIMAL_NRC_LOOP = 42;

    private static final int HANDLE_NCR_VALUE = 43;

    private static final int SELF_CLOSING_START_TAG = 44;

    private static final int CDATA_START = 45;

    private static final int CDATA_SECTION = 46;

    private static final int CDATA_RSQB = 47;

    private static final int CDATA_RSQB_RSQB = 48;

    private static final int TAG_OPEN_NON_PCDATA = 51;

    private static final int ESCAPE_EXCLAMATION = 52;

    private static final int ESCAPE_EXCLAMATION_HYPHEN = 53;

    private static final int ESCAPE = 54;

    private static final int ESCAPE_HYPHEN = 55;

    private static final int ESCAPE_HYPHEN_HYPHEN = 56;

    private static final int BOGUS_COMMENT_HYPHEN = 57;

    /**
     * Magic value for UTF-16 operations.
     */
    private static final int LEAD_OFFSET = (0xD800 - (0x10000 >> 10));

    /**
     * Magic value for UTF-16 operations.
     */
    private static final int SURROGATE_OFFSET = (0x10000 - (0xD800 << 10) - 0xDC00);

    /**
     * UTF-16 code unit array containing less than and greater than for emitting
     * those characters on certain parse errors.
     */
    private static final @NoLength char[] LT_GT = { '<', '>' };

    /**
     * UTF-16 code unit array containing less than and solidus for emitting
     * those characters on certain parse errors.
     */
    private static final @NoLength char[] LT_SOLIDUS = { '<', '/' };

    /**
     * UTF-16 code unit array containing ]] for emitting those characters on
     * state transitions.
     */
    private static final @NoLength char[] RSQB_RSQB = { ']', ']' };

    /**
     * Array version of U+FFFD.
     */
    private static final @NoLength char[] REPLACEMENT_CHARACTER = { '\uFFFD' };

    // [NOCPP[

    /**
     * Array version of space.
     */
    private static final @NoLength char[] SPACE = { ' ' };

    // ]NOCPP]

    /**
     * Array version of line feed.
     */
    private static final @NoLength char[] LF = { '\n' };

    /**
     * Buffer growth parameter.
     */
    private static final int BUFFER_GROW_BY = 1024;

    /**
     * "CDATA[" as <code>char[]</code>
     */
    private static final @NoLength char[] CDATA_LSQB = "CDATA[".toCharArray();

    /**
     * "octype" as <code>char[]</code>
     */
    private static final @NoLength char[] OCTYPE = "octype".toCharArray();

    /**
     * "ublic" as <code>char[]</code>
     */
    private static final @NoLength char[] UBLIC = "ublic".toCharArray();

    /**
     * "ystem" as <code>char[]</code>
     */
    private static final @NoLength char[] YSTEM = "ystem".toCharArray();

    private static final char[] TITLE_ARR = { 't', 'i', 't', 'l', 'e' };

    private static final char[] SCRIPT_ARR = { 's', 'c', 'r', 'i', 'p', 't' };

    private static final char[] STYLE_ARR = { 's', 't', 'y', 'l', 'e' };

    private static final char[] PLAINTEXT_ARR = { 'p', 'l', 'a', 'i', 'n', 't',
            'e', 'x', 't' };

    private static final char[] XMP_ARR = { 'x', 'm', 'p' };

    private static final char[] TEXTAREA_ARR = { 't', 'e', 'x', 't', 'a', 'r',
            'e', 'a' };

    private static final char[] IFRAME_ARR = { 'i', 'f', 'r', 'a', 'm', 'e' };

    private static final char[] NOEMBED_ARR = { 'n', 'o', 'e', 'm', 'b', 'e',
            'd' };

    private static final char[] NOSCRIPT_ARR = { 'n', 'o', 's', 'c', 'r', 'i',
            'p', 't' };

    private static final char[] NOFRAMES_ARR = { 'n', 'o', 'f', 'r', 'a', 'm',
            'e', 's' };

    /**
     * The token handler.
     */
    private final TokenHandler tokenHandler;

    private final EncodingDeclarationHandler encodingDeclarationHandler;

    // [NOCPP[

    /**
     * The error handler.
     */
    private ErrorHandler errorHandler;

    // ]NOCPP]

    /**
     * Whether the previous char read was CR.
     */
    private boolean prevCR;

    /**
     * The current line number in the current resource being parsed. (First line
     * is 1.) Passed on as locator data.
     */
    private int line;

    private int linePrev;

    /**
     * The current column number in the current resource being tokenized. (First
     * column is 1, counted by UTF-16 code units.) Passed on as locator data.
     */
    private int col;

    private int colPrev;

    private boolean nextCharOnNewLine;

    private int stateSave;

    private int returnStateSave;

    private int index;

    private boolean forceQuirks;

    private char additional;

    private int entCol;

    private int lo;

    private int hi;

    private int candidate;

    private int strBufMark;

    private int prevValue;

    private int value;

    private boolean seenDigits;

    private int cstart;

    /**
     * The SAX public id for the resource being tokenized. (Only passed to back
     * as part of locator data.)
     */
    private String publicId;

    /**
     * The SAX system id for the resource being tokenized. (Only passed to back
     * as part of locator data.)
     */
    private String systemId;

    /**
     * Buffer for short identifiers.
     */
    private char[] strBuf;

    /**
     * Number of significant <code>char</code>s in <code>strBuf</code>.
     */
    private int strBufLen;

    /**
     * <code>-1</code> to indicate that <code>strBuf</code> is used or otherwise
     * an offset to the main buffer.
     */
    // private int strBufOffset = -1;
    /**
     * Buffer for long strings.
     */
    private char[] longStrBuf;

    /**
     * Number of significant <code>char</code>s in <code>longStrBuf</code>.
     */
    private int longStrBufLen;

    /**
     * <code>-1</code> to indicate that <code>longStrBuf</code> is used or
     * otherwise an offset to the main buffer.
     */
    // private int longStrBufOffset = -1;
    /**
     * The attribute holder.
     */
    private HtmlAttributes attributes;

    /**
     * Buffer for expanding NCRs falling into the Basic Multilingual Plane.
     */
    private final char[] bmpChar;

    /**
     * Buffer for expanding astral NCRs.
     */
    private final char[] astralChar;

    /**
     * Keeps track of PUA warnings.
     */
    private boolean alreadyWarnedAboutPrivateUseCharacters;

    /**
     * The element whose end tag closes the current CDATA or RCDATA element.
     */
    private ElementName contentModelElement = null;

    private char[] contentModelElementNameAsArray;

    /**
     * <code>true</code> if tokenizing an end tag
     */
    private boolean endTag;

    /**
     * The current tag token name.
     */
    private ElementName tagName = null;

    /**
     * The current attribute name.
     */
    private AttributeName attributeName = null;

    // [NOCPP[

    /**
     * Whether comment tokens are emitted.
     */
    private boolean wantsComments = false;

    /**
     * <code>true</code> when HTML4-specific additional errors are requested.
     */
    private boolean html4;

    /**
     * Used together with <code>nonAsciiProhibited</code>.
     */
    private boolean alreadyComplainedAboutNonAscii;

    /**
     * Whether the stream is past the first 512 bytes.
     */
    private boolean metaBoundaryPassed;

    // ]NOCPP]

    /**
     * The name of the current doctype token.
     */
    private @Local String doctypeName;

    /**
     * The public id of the current doctype token.
     */
    private String publicIdentifier;

    /**
     * The system id of the current doctype token.
     */
    private String systemIdentifier;

    // [NOCPP[

    /**
     * The policy for vertical tab and form feed.
     */
    private XmlViolationPolicy contentSpacePolicy = XmlViolationPolicy.ALTER_INFOSET;

    /**
     * The policy for non-space non-XML characters.
     */
    private XmlViolationPolicy contentNonXmlCharPolicy = XmlViolationPolicy.ALTER_INFOSET;

    /**
     * The policy for comments.
     */
    private XmlViolationPolicy commentPolicy = XmlViolationPolicy.ALTER_INFOSET;

    private XmlViolationPolicy xmlnsPolicy = XmlViolationPolicy.ALTER_INFOSET;

    private XmlViolationPolicy namePolicy = XmlViolationPolicy.ALTER_INFOSET;

    private boolean html4ModeCompatibleWithXhtml1Schemata;

    private final boolean newAttributesEachTime;

    // ]NOCPP]

    private int mappingLangToXmlLang;

    private boolean shouldSuspend;

    private boolean confident;

    // [NOCPP[

    private PushedLocation pushedLocation;

    private LocatorImpl ampersandLocation;

    public Tokenizer(TokenHandler tokenHandler,
            EncodingDeclarationHandler encodingDeclarationHandler,
            boolean newAttributesEachTime) {
        this.tokenHandler = tokenHandler;
        this.encodingDeclarationHandler = encodingDeclarationHandler;
        this.newAttributesEachTime = newAttributesEachTime;
        this.bmpChar = new char[1];
        this.astralChar = new char[2];
    }

    /**
     * The constructor.
     * 
     * @param tokenHandler
     *            the handler for receiving tokens
     */
    public Tokenizer(TokenHandler tokenHandler) {
        this.tokenHandler = tokenHandler;
        this.encodingDeclarationHandler = null;
        this.newAttributesEachTime = false;
        this.bmpChar = new char[1];
        this.astralChar = new char[2];
    }

    // ]NOCPP]

    public Tokenizer(TokenHandler tokenHandler,
            EncodingDeclarationHandler encodingDeclarationHandler) {
        this.tokenHandler = tokenHandler;
        this.encodingDeclarationHandler = encodingDeclarationHandler;
        // [NOCPP[
        this.newAttributesEachTime = false;
        // ]NOCPP]
        this.bmpChar = new char[1];
        this.astralChar = new char[2];
    }

    public void initLocation(String newPublicId, String newSystemId) {
        this.systemId = newSystemId;
        this.publicId = newPublicId;

    }

    void destructor() {
        Portability.releaseArray(bmpChar);
        Portability.releaseArray(astralChar);
    }

    // [NOCPP[

    /**
     * Returns the mappingLangToXmlLang.
     * 
     * @return the mappingLangToXmlLang
     */
    public boolean isMappingLangToXmlLang() {
        return mappingLangToXmlLang == AttributeName.HTML_LANG;
    }

    /**
     * Sets the mappingLangToXmlLang.
     * 
     * @param mappingLangToXmlLang
     *            the mappingLangToXmlLang to set
     */
    public void setMappingLangToXmlLang(boolean mappingLangToXmlLang) {
        this.mappingLangToXmlLang = mappingLangToXmlLang ? AttributeName.HTML_LANG
                : AttributeName.HTML;
    }

    /**
     * Sets the error handler.
     * 
     * @see org.xml.sax.XMLReader#setErrorHandler(org.xml.sax.ErrorHandler)
     */
    public void setErrorHandler(ErrorHandler eh) {
        this.errorHandler = eh;
    }

    public ErrorHandler getErrorHandler() {
        return this.errorHandler;
    }

    /**
     * Sets the commentPolicy.
     * 
     * @param commentPolicy
     *            the commentPolicy to set
     */
    public void setCommentPolicy(XmlViolationPolicy commentPolicy) {
        this.commentPolicy = commentPolicy;
    }

    /**
     * Sets the contentNonXmlCharPolicy.
     * 
     * @param contentNonXmlCharPolicy
     *            the contentNonXmlCharPolicy to set
     */
    public void setContentNonXmlCharPolicy(
            XmlViolationPolicy contentNonXmlCharPolicy) {
        this.contentNonXmlCharPolicy = contentNonXmlCharPolicy;
    }

    /**
     * Sets the contentSpacePolicy.
     * 
     * @param contentSpacePolicy
     *            the contentSpacePolicy to set
     */
    public void setContentSpacePolicy(XmlViolationPolicy contentSpacePolicy) {
        this.contentSpacePolicy = contentSpacePolicy;
    }

    /**
     * Sets the xmlnsPolicy.
     * 
     * @param xmlnsPolicy
     *            the xmlnsPolicy to set
     */
    public void setXmlnsPolicy(XmlViolationPolicy xmlnsPolicy) {
        if (xmlnsPolicy == XmlViolationPolicy.FATAL) {
            throw new IllegalArgumentException("Can't use FATAL here.");
        }
        this.xmlnsPolicy = xmlnsPolicy;
    }

    public void setNamePolicy(XmlViolationPolicy namePolicy) {
        this.namePolicy = namePolicy;
    }

    /**
     * Sets the html4ModeCompatibleWithXhtml1Schemata.
     * 
     * @param html4ModeCompatibleWithXhtml1Schemata
     *            the html4ModeCompatibleWithXhtml1Schemata to set
     */
    public void setHtml4ModeCompatibleWithXhtml1Schemata(
            boolean html4ModeCompatibleWithXhtml1Schemata) {
        this.html4ModeCompatibleWithXhtml1Schemata = html4ModeCompatibleWithXhtml1Schemata;
    }

    // ]NOCPP]

    // For the token handler to call
    /**
     * Sets the content model flag and the associated element name.
     * 
     * @param contentModelFlag
     *            the flag
     * @param contentModelElement
     *            the element causing the flag to be set
     */
    public void setContentModelFlag(int contentModelFlag,
            @Local String contentModelElement) {
        this.stateSave = contentModelFlag;
        if (contentModelFlag == Tokenizer.DATA) {
            return;
        }
        // XXX does this make any sense?
        char[] asArray = Portability.newCharArrayFromLocal(contentModelElement);
        this.contentModelElement = ElementName.elementNameByBuffer(asArray, 0,
                asArray.length);
        Portability.releaseArray(asArray);
        contentModelElementToArray();
    }

    /**
     * Sets the content model flag and the associated element name.
     * 
     * @param contentModelFlag
     *            the flag
     * @param contentModelElement
     *            the element causing the flag to be set
     */
    public void setContentModelFlag(int contentModelFlag,
            ElementName contentModelElement) {
        this.stateSave = contentModelFlag;
        this.contentModelElement = contentModelElement;
        contentModelElementToArray();
    }

    private void contentModelElementToArray() {
        switch (contentModelElement.group) {
            case TreeBuilder.TITLE:
                contentModelElementNameAsArray = TITLE_ARR;
                return;
            case TreeBuilder.SCRIPT:
                contentModelElementNameAsArray = SCRIPT_ARR;
                return;
            case TreeBuilder.STYLE:
                contentModelElementNameAsArray = STYLE_ARR;
                return;
            case TreeBuilder.PLAINTEXT:
                contentModelElementNameAsArray = PLAINTEXT_ARR;
                return;
            case TreeBuilder.XMP:
                contentModelElementNameAsArray = XMP_ARR;
                return;
            case TreeBuilder.TEXTAREA:
                contentModelElementNameAsArray = TEXTAREA_ARR;
                return;
            case TreeBuilder.IFRAME:
                contentModelElementNameAsArray = IFRAME_ARR;
                return;
            case TreeBuilder.NOEMBED:
                contentModelElementNameAsArray = NOEMBED_ARR;
                return;
            case TreeBuilder.NOSCRIPT:
                contentModelElementNameAsArray = NOSCRIPT_ARR;
                return;
            case TreeBuilder.NOFRAMES:
                contentModelElementNameAsArray = NOFRAMES_ARR;
                return;
            default:
                assert false;
                return;
        }
    }

    // start Locator impl

    /**
     * @see org.xml.sax.Locator#getPublicId()
     */
    public String getPublicId() {
        return publicId;
    }

    /**
     * @see org.xml.sax.Locator#getSystemId()
     */
    public String getSystemId() {
        return systemId;
    }

    /**
     * @see org.xml.sax.Locator#getLineNumber()
     */
    public int getLineNumber() {
        if (line > 0) {
            return line;
        } else {
            return -1;
        }
    }

    /**
     * @see org.xml.sax.Locator#getColumnNumber()
     */
    public int getColumnNumber() {
        if (col > 0) {
            return col;
        } else {
            return -1;
        }
    }

    // end Locator impl

    // end public API

    // [NOCPP[

    public void notifyAboutMetaBoundary() {
        metaBoundaryPassed = true;
    }

    void turnOnAdditionalHtml4Errors() {
        html4 = true;
    }

    // ]NOCPP]

    HtmlAttributes emptyAttributes() {
        // [NOCPP[
        if (newAttributesEachTime) {
            return new HtmlAttributes(mappingLangToXmlLang);
        } else {
            // ]NOCPP]
            return HtmlAttributes.EMPTY_ATTRIBUTES;
            // [NOCPP[
        }
        // ]NOCPP]
    }

    private void clearStrBufAndAppendCurrentC(char c) {
        strBuf[0] = c;

        strBufLen = 1;
        // strBufOffset = pos;
    }

    private void clearStrBufAndAppendForceWrite(char c) {
        strBuf[0] = c; // test

        strBufLen = 1;
        // strBufOffset = pos;
        // buf[pos] = c;
    }

    private void clearStrBufForNextState() {
        strBufLen = 0;
        // strBufOffset = pos + 1;
    }

    /**
     * Appends to the smaller buffer.
     * 
     * @param c
     *            the UTF-16 code unit to append
     */
    private void appendStrBuf(char c) {
        // if (strBufOffset != -1) {
        // strBufLen++;
        // } else {
        if (strBufLen == strBuf.length) {
            char[] newBuf = new char[strBuf.length + Tokenizer.BUFFER_GROW_BY];
            System.arraycopy(strBuf, 0, newBuf, 0, strBuf.length);
            Portability.releaseArray(strBuf);
            strBuf = newBuf;
        }
        strBuf[strBufLen++] = c;
        // }
    }

    /**
     * Appends to the smaller buffer.
     * 
     * @param c
     *            the UTF-16 code unit to append
     */
    private void appendStrBufForceWrite(char c) {
        // if (strBufOffset != -1) {
        // strBufLen++;
        // buf[pos] = c;
        // } else {
        if (strBufLen == strBuf.length) {
            char[] newBuf = new char[strBuf.length + Tokenizer.BUFFER_GROW_BY];
            System.arraycopy(strBuf, 0, newBuf, 0, strBuf.length);
            Portability.releaseArray(strBuf);
            strBuf = newBuf;
        }
        strBuf[strBufLen++] = c;
        // }
    }

    /**
     * The smaller buffer as a String. Currently only used for error reporting.
     * 
     * <p>
     * C++ memory note: The return value must be released.
     * 
     * @return the smaller buffer as a string
     */
    private String strBufToString() {
        // if (strBufOffset != -1) {
        // return Portability.newStringFromBuffer(buf, strBufOffset, strBufLen);
        // } else {
        return Portability.newStringFromBuffer(strBuf, 0, strBufLen);
        // }
    }

    /**
     * Returns the short buffer as a local name. The return value is released in
     * emitDoctypeToken().
     * 
     * @return the smaller buffer as local name
     */
    private void strBufToDoctypeName() {
        doctypeName = Portability.newLocalNameFromBuffer(strBuf, 0, strBufLen);
    }

    /**
     * Emits the smaller buffer as character tokens.
     * 
     * @throws SAXException
     *             if the token handler threw
     */
    private void emitStrBuf() throws SAXException {
        if (strBufLen > 0) {
            // if (strBufOffset != -1) {
            // tokenHandler.characters(buf, strBufOffset, strBufLen);
            // } else {
            tokenHandler.characters(strBuf, 0, strBufLen);
            // }
        }
    }

    private void clearLongStrBufForNextState() {
        // longStrBufOffset = pos + 1;
        longStrBufLen = 0;
    }

    private void clearLongStrBuf() {
        // longStrBufOffset = pos;
        longStrBufLen = 0;
    }

    private void clearLongStrBufAndAppendCurrentC(char c) {
        longStrBuf[0] = c;
        longStrBufLen = 1;
        // longStrBufOffset = pos;
    }

    private void clearLongStrBufAndAppendToComment(char c) {
        longStrBuf[0] = c;
        // longStrBufOffset = pos;
        longStrBufLen = 1;
    }

    /**
     * Appends to the larger buffer.
     * 
     * @param c
     *            the UTF-16 code unit to append
     */
    private void appendLongStrBuf(char c) {
        // if (longStrBufOffset != -1) {
        // longStrBufLen++;
        // } else {
        if (longStrBufLen == longStrBuf.length) {
            char[] newBuf = new char[longStrBufLen + (longStrBufLen >> 1)];
            System.arraycopy(longStrBuf, 0, newBuf, 0, longStrBuf.length);
            Portability.releaseArray(longStrBuf);
            longStrBuf = newBuf;
        }
        longStrBuf[longStrBufLen++] = c;
        // }
    }

    private void appendSecondHyphenToBogusComment() throws SAXException {
        // [NOCPP[
        switch (commentPolicy) {
            case ALTER_INFOSET:
                // detachLongStrBuf();
                appendLongStrBuf(' ');
                // FALLTHROUGH
            case ALLOW:
                warn("The document is not mappable to XML 1.0 due to two consecutive hyphens in a comment.");
                // ]NOCPP]
                appendLongStrBuf('-');
                // [NOCPP[
                break;
            case FATAL:
                fatal("The document is not mappable to XML 1.0 due to two consecutive hyphens in a comment.");
                break;
        }
        // ]NOCPP]
    }

    // [NOCPP[
    private void maybeAppendSpaceToBogusComment() throws SAXException {
        switch (commentPolicy) {
            case ALTER_INFOSET:
                // detachLongStrBuf();
                appendLongStrBuf(' ');
                // FALLTHROUGH
            case ALLOW:
                warn("The document is not mappable to XML 1.0 due to a trailing hyphen in a comment.");
                break;
            case FATAL:
                fatal("The document is not mappable to XML 1.0 due to a trailing hyphen in a comment.");
                break;
        }
    }

    // ]NOCPP]

    private void adjustDoubleHyphenAndAppendToLongStrBuf(char c)
            throws SAXException {
        // [NOCPP[
        switch (commentPolicy) {
            case ALTER_INFOSET:
                // detachLongStrBuf();
                longStrBufLen--;
                appendLongStrBuf(' ');
                appendLongStrBuf('-');
                // FALLTHROUGH
            case ALLOW:
                warn("The document is not mappable to XML 1.0 due to two consecutive hyphens in a comment.");
                // ]NOCPP]
                appendLongStrBuf(c);
                // [NOCPP[
                break;
            case FATAL:
                fatal("The document is not mappable to XML 1.0 due to two consecutive hyphens in a comment.");
                break;
        }
        // ]NOCPP]
    }

    private void appendLongStrBuf(char[] buffer, int offset, int length) {
        int reqLen = longStrBufLen + length;
        if (longStrBuf.length < reqLen) {
            char[] newBuf = new char[reqLen + (reqLen >> 1)];
            System.arraycopy(longStrBuf, 0, newBuf, 0, longStrBuf.length);
            Portability.releaseArray(longStrBuf);
            longStrBuf = newBuf;
        }
        System.arraycopy(buffer, offset, longStrBuf, longStrBufLen, length);
        longStrBufLen = reqLen;
    }

    /**
     * Appends to the larger buffer.
     * 
     * @param arr
     *            the UTF-16 code units to append
     */
    private void appendLongStrBuf(char[] arr) {
        // assert longStrBufOffset == -1;
        appendLongStrBuf(arr, 0, arr.length);
    }

    /**
     * Append the contents of the smaller buffer to the larger one.
     */
    private void appendStrBufToLongStrBuf() {
        // assert longStrBufOffset == -1;
        // if (strBufOffset != -1) {
        // appendLongStrBuf(buf, strBufOffset, strBufLen);
        // } else {
        appendLongStrBuf(strBuf, 0, strBufLen);
        // }
    }

    /**
     * The larger buffer as a string.
     * 
     * <p>
     * C++ memory note: The return value must be released.
     * 
     * @return the larger buffer as a string
     */
    private String longStrBufToString() {
        // if (longStrBufOffset != -1) {
        // return Portability.newStringFromBuffer(buf, longStrBufOffset,
        // longStrBufLen);
        // } else {
        return Portability.newStringFromBuffer(longStrBuf, 0, longStrBufLen);
        // }
    }

    /**
     * Emits the current comment token.
     * @param pos TODO
     * 
     * @throws SAXException
     */
    private void emitComment(int provisionalHyphens, int pos) throws SAXException {
        // [NOCPP[
        if (wantsComments) {
            // ]NOCPP]
            // if (longStrBufOffset != -1) {
            // tokenHandler.comment(buf, longStrBufOffset, longStrBufLen
            // - provisionalHyphens);
            // } else {
            tokenHandler.comment(longStrBuf, 0, longStrBufLen
                    - provisionalHyphens);
            // }
            // [NOCPP[
        }
        // ]NOCPP]
        cstart = pos + 1;
    }

    // [NOCPP[

    private String toUPlusString(char c) {
        String hexString = Integer.toHexString(c);
        switch (hexString.length()) {
            case 1:
                return "U+000" + hexString;
            case 2:
                return "U+00" + hexString;
            case 3:
                return "U+0" + hexString;
            case 4:
                return "U+" + hexString;
            default:
                throw new RuntimeException("Unreachable.");
        }
    }

    // ]NOCPP]

    /**
     * Emits a warning about private use characters if the warning has not been
     * emitted yet.
     * 
     * @throws SAXException
     */
    private void warnAboutPrivateUseChar() throws SAXException {
        if (!alreadyWarnedAboutPrivateUseCharacters) {
            warn("Document uses the Unicode Private Use Area(s), which should not be used in publicly exchanged documents. (Charmod C073)");
            alreadyWarnedAboutPrivateUseCharacters = true;
        }
    }

    /**
     * Tells if the argument is a BMP PUA character.
     * 
     * @param c
     *            the UTF-16 code unit to check
     * @return <code>true</code> if PUA character
     */
    private boolean isPrivateUse(char c) {
        return c >= '\uE000' && c <= '\uF8FF';
    }

    /**
     * Tells if the argument is an astral PUA character.
     * 
     * @param c
     *            the code point to check
     * @return <code>true</code> if astral private use
     */
    private boolean isAstralPrivateUse(int c) {
        return (c >= 0xF0000 && c <= 0xFFFFD)
                || (c >= 0x100000 && c <= 0x10FFFD);
    }

    /**
     * Tells if the argument is a non-character (works for BMP and astral).
     * 
     * @param c
     *            the code point to check
     * @return <code>true</code> if non-character
     */
    private boolean isNonCharacter(int c) {
        return (c & 0xFFFE) == 0xFFFE;
    }

    /**
     * Flushes coalesced character tokens.
     * @param buf TODO
     * @param pos TODO
     * 
     * @throws SAXException
     */
    private void flushChars(char[] buf, int pos) throws SAXException {
        if (pos > cstart) {
            int currLine = line;
            int currCol = col;
            line = linePrev;
            col = colPrev;
            tokenHandler.characters(buf, cstart, pos - cstart);
            line = currLine;
            col = currCol;
        }
        cstart = 0x7fffffff;
    }

    /**
     * Reports an condition that would make the infoset incompatible with XML
     * 1.0 as fatal.
     * 
     * @param message
     *            the message
     * @throws SAXException
     * @throws SAXParseException
     */
    public void fatal(String message) throws SAXException {
        SAXParseException spe = new SAXParseException(message, this);
        if (errorHandler != null) {
            errorHandler.fatalError(spe);
        }
        throw spe;
    }

    /**
     * Reports a Parse Error.
     * 
     * @param message
     *            the message
     * @throws SAXException
     */
    public void err(String message) throws SAXException {
        if (errorHandler == null) {
            return;
        }
        SAXParseException spe = new SAXParseException(message, this);
        errorHandler.error(spe);
    }

    public void errTreeBuilder(String message) throws SAXException {
        ErrorHandler eh = null;
        if (tokenHandler instanceof TreeBuilder<?>) {
            TreeBuilder<?> treeBuilder = (TreeBuilder<?>) tokenHandler;
            eh = treeBuilder.getErrorHandler();
        }
        if (eh == null) {
            eh = errorHandler;
        }
        if (eh == null) {
            return;
        }
        SAXParseException spe = new SAXParseException(message, this);
        eh.error(spe);
    }

    /**
     * Reports a warning
     * 
     * @param message
     *            the message
     * @throws SAXException
     */
    public void warn(String message) throws SAXException {
        if (errorHandler == null) {
            return;
        }
        SAXParseException spe = new SAXParseException(message, this);
        errorHandler.warning(spe);
    }

    /**
     * 
     */
    private void resetAttributes() {
        // [NOCPP[
        if (newAttributesEachTime) {
            attributes = null;
        } else {
            // ]NOCPP]
            attributes.clear(mappingLangToXmlLang);
            // [NOCPP[
        }
        // ]NOCPP]
    }

    private void strBufToElementNameString() {
        // if (strBufOffset != -1) {
        // return ElementName.elementNameByBuffer(buf, strBufOffset, strBufLen);
        // } else {
        tagName = ElementName.elementNameByBuffer(strBuf, 0, strBufLen);
        // }
    }

    private int emitCurrentTagToken(boolean selfClosing, int pos) throws SAXException {
        cstart = pos + 1;
        maybeErrSlashInEndTag(selfClosing);
        stateSave = Tokenizer.DATA;
        HtmlAttributes attrs = (attributes == null ? HtmlAttributes.EMPTY_ATTRIBUTES
                : attributes);
        if (endTag) {
            /*
             * When an end tag token is emitted, the content model flag must be
             * switched to the PCDATA state.
             */
            maybeErrAttributesOnEndTag(attrs);
            tokenHandler.endTag(tagName);
        } else {
            tokenHandler.startTag(tagName, attrs, selfClosing);
        }
        resetAttributes();
        return stateSave;
    }

    private void attributeNameComplete() throws SAXException {
        // if (strBufOffset != -1) {
        // attributeName = AttributeName.nameByBuffer(buf, strBufOffset,
        // strBufLen, namePolicy != XmlViolationPolicy.ALLOW);
        // } else {
        attributeName = AttributeName.nameByBuffer(strBuf, 0, strBufLen
        // [NOCPP[
                , namePolicy != XmlViolationPolicy.ALLOW
        // ]NOCPP]
        );
        // }

        // [NOCPP[
        if (attributes == null) {
            attributes = new HtmlAttributes(mappingLangToXmlLang);
        }
        // ]NOCPP]

        /*
         * When the user agent leaves the attribute name state (and before
         * emitting the tag token, if appropriate), the complete attribute's
         * name must be compared to the other attributes on the same token; if
         * there is already an attribute on the token with the exact same name,
         * then this is a parse error and the new attribute must be dropped,
         * along with the value that gets associated with it (if any).
         */
        if (attributes.contains(attributeName)) {
            errDuplicateAttribute();
            attributeName.release();
            attributeName = null;
        }
    }

    private void addAttributeWithoutValue() throws SAXException {
        // [NOCPP[
        if (metaBoundaryPassed && AttributeName.CHARSET == attributeName
                && ElementName.META == tagName) {
            err("A \u201Ccharset\u201D attribute on a \u201Cmeta\u201D element found after the first 512 bytes.");
        }
        // ]NOCPP]
        if (attributeName != null) {
            // [NOCPP[
            if (html4) {
                if (attributeName.isBoolean()) {
                    if (html4ModeCompatibleWithXhtml1Schemata) {
                        attributes.addAttribute(attributeName,
                                attributeName.getLocal(AttributeName.HTML),
                                xmlnsPolicy);
                    } else {
                        attributes.addAttribute(attributeName, "", xmlnsPolicy);
                    }
                } else {
                    err("Attribute value omitted for a non-boolean attribute. (HTML4-only error.)");
                    attributes.addAttribute(attributeName, "", xmlnsPolicy);
                }
            } else {
                if (AttributeName.SRC == attributeName
                        || AttributeName.HREF == attributeName) {
                    warn("Attribute \u201C"
                            + attributeName.getLocal(AttributeName.HTML)
                            + "\u201D without an explicit value seen. The attribute may be dropped by IE7.");
                }
                // ]NOCPP]
                attributes.addAttribute(attributeName,
                        Portability.newEmptyString()
                        // [NOCPP[
                        , xmlnsPolicy
                // ]NOCPP]
                );
                // [NOCPP[
            }
            // ]NOCPP]
        }
    }

    private void addAttributeWithValue() throws SAXException {
        // [NOCPP[
        if (metaBoundaryPassed && ElementName.META == tagName
                && AttributeName.CHARSET == attributeName) {
            err("A \u201Ccharset\u201D attribute on a \u201Cmeta\u201D element found after the first 512 bytes.");
        }
        // ]NOCPP]
        if (attributeName != null) {
            String value = longStrBufToString(); // Ownership transferred to
            // HtmlAttributes
            // [NOCPP[
            if (!endTag && html4 && html4ModeCompatibleWithXhtml1Schemata
                    && attributeName.isCaseFolded()) {
                value = newAsciiLowerCaseStringFromString(value);
            }
            // ]NOCPP]
            attributes.addAttribute(attributeName, value
            // [NOCPP[
                    , xmlnsPolicy
            // ]NOCPP]
            );
        }
    }

    // [NOCPP[

    private static String newAsciiLowerCaseStringFromString(String str) {
        if (str == null) {
            return null;
        }
        char[] buf = new char[str.length()];
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c >= 'A' && c <= 'Z') {
                c += 0x20;
            }
            buf[i] = c;
        }
        return new String(buf);
    }

    // ]NOCPP]

    public void start() throws SAXException {
        confident = false;
        strBuf = new char[64];
        strBufLen = 0;
        longStrBuf = new char[1024];
        longStrBufLen = 0;
        stateSave = Tokenizer.DATA;
        line = linePrev = 0;
        col = colPrev = 1;
        nextCharOnNewLine = true;
        prevCR = false;
        // [NOCPP[
        html4 = false;
        alreadyWarnedAboutPrivateUseCharacters = false;
        alreadyComplainedAboutNonAscii = false;
        metaBoundaryPassed = false;
        // ]NOCPP]
        tokenHandler.startTokenization(this);
        // [NOCPP[
        wantsComments = tokenHandler.wantsComments();
        // ]NOCPP]
        index = 0;
        forceQuirks = false;
        additional = '\u0000';
        entCol = -1;
        lo = 0;
        hi = (NamedCharacters.NAMES.length - 1);
        candidate = -1;
        strBufMark = 0;
        prevValue = -1;
        value = 0;
        seenDigits = false;
        shouldSuspend = false;
        // [NOCPP[
        if (!newAttributesEachTime) {
            // ]NOCPP]
            attributes = new HtmlAttributes(mappingLangToXmlLang);
            // [NOCPP[
        }
        // ]NOCPP]
    }

    public boolean tokenizeBuffer(UTF16Buffer buffer) throws SAXException {
        int state = stateSave;
        int returnState = returnStateSave;
        char c = '\u0000';
        shouldSuspend = false;
        prevCR = false;

        int start = buffer.getStart();
        /**
         * The index of the last <code>char</code> read from <code>buf</code>.
         */
        int pos = start - 1;

        /**
         * The index of the first <code>char</code> in <code>buf</code> that is
         * part of a coalesced run of character tokens or
         * <code>Integer.MAX_VALUE</code> if there is not a current run being
         * coalesced.
         */
        switch (state) {
            case DATA:
            case RCDATA:
            case CDATA:
            case PLAINTEXT:
            case CDATA_SECTION:
            case ESCAPE:
            case ESCAPE_EXCLAMATION:
            case ESCAPE_EXCLAMATION_HYPHEN:
            case ESCAPE_HYPHEN:
            case ESCAPE_HYPHEN_HYPHEN:
                cstart = start;
                break;
            default:
                cstart = Integer.MAX_VALUE;
                break;
        }

        /**
         * The number of <code>char</code>s in <code>buf</code> that have
         * meaning. (The rest of the array is garbage and should not be
         * examined.)
         */
        pos = stateLoop(state, c, pos, buffer.getBuffer(), false, returnState, buffer.getEnd());
        if (pos == buffer.getEnd()) {
            // exiting due to end of buffer
            buffer.setStart(pos);
        } else {
            buffer.setStart(pos + 1);
        }
        return prevCR;
    }

    // WARNING When editing this, makes sure the bytecode length shown by javap
    // stays under 8000 bytes!
    private int stateLoop(int state, char c, int pos, @NoLength char[] buf, boolean reconsume, int returnState, int endPos)
            throws SAXException {
        stateloop: for (;;) {
            switch (state) {
                case DATA:
                    dataloop: for (;;) {
                        if (reconsume) {
                            reconsume = false;
                        } else {
                            if (++pos == endPos) {
                                break stateloop;
                            }
                            c = checkChar(buf, pos);
                        }
                        switch (c) {
                            case '&':
                                /*
                                 * U+0026 AMPERSAND (&) When the content model
                                 * flag is set to one of the PCDATA or RCDATA
                                 * states and the escape flag is false: switch
                                 * to the character reference data state.
                                 * Otherwise: treat it as per the "anything
                                 * else" entry below.
                                 */
                                flushChars(buf, pos);
                                clearStrBufAndAppendCurrentC(c);
                                rememberAmpersandLocation('\u0000');
                                returnState = state;
                                state = Tokenizer.CONSUME_CHARACTER_REFERENCE;
                                continue stateloop;
                            case '<':
                                /*
                                 * U+003C LESS-THAN SIGN (<) When the content
                                 * model flag is set to the PCDATA state: switch
                                 * to the tag open state. When the content model
                                 * flag is set to either the RCDATA state or the
                                 * CDATA state, and the escape flag is false:
                                 * switch to the tag open state. Otherwise:
                                 * treat it as per the "anything else" entry
                                 * below.
                                 */
                                flushChars(buf, pos);

                                state = Tokenizer.TAG_OPEN;
                                break dataloop; // FALL THROUGH continue
                            // stateloop;
                            case '\u0000':
                                emitReplacementCharacter(buf, pos);
                                continue;
                            case '\r':
                                emitCarriageReturn(buf, pos);
                                break stateloop;
                            case '\n':
                                silentLineFeed();
                            default:
                                /*
                                 * Anything else Emit the input character as a
                                 * character token.
                                 */
                                /*
                                 * Stay in the data state.
                                 */
                                continue;
                        }
                    }
                    // WARNING FALLTHRU CASE TRANSITION: DON'T REORDER
                case TAG_OPEN:
                    tagopenloop: for (;;) {
                        /*
                         * The behavior of this state depends on the content
                         * model flag.
                         */
                        if (++pos == endPos) {
                            break stateloop;
                        }
                        c = checkChar(buf, pos);
                        /*
                         * If the content model flag is set to the PCDATA state
                         * Consume the next input character:
                         */
                        if (c >= 'A' && c <= 'Z') {
                            /*
                             * U+0041 LATIN CAPITAL LETTER A through to U+005A
                             * LATIN CAPITAL LETTER Z Create a new start tag
                             * token,
                             */
                            endTag = false;
                            /*
                             * set its tag name to the lowercase version of the
                             * input character (add 0x0020 to the character's
                             * code point),
                             */
                            clearStrBufAndAppendForceWrite((char) (c + 0x20));
                            /* then switch to the tag name state. */
                            state = Tokenizer.TAG_NAME;
                            /*
                             * (Don't emit the token yet; further details will
                             * be filled in before it is emitted.)
                             */
                            break tagopenloop;
                            // continue stateloop;
                        } else if (c >= 'a' && c <= 'z') {
                            /*
                             * U+0061 LATIN SMALL LETTER A through to U+007A
                             * LATIN SMALL LETTER Z Create a new start tag
                             * token,
                             */
                            endTag = false;
                            /*
                             * set its tag name to the input character,
                             */
                            clearStrBufAndAppendCurrentC(c);
                            /* then switch to the tag name state. */
                            state = Tokenizer.TAG_NAME;
                            /*
                             * (Don't emit the token yet; further details will
                             * be filled in before it is emitted.)
                             */
                            break tagopenloop;
                            // continue stateloop;
                        }
                        switch (c) {
                            case '!':
                                /*
                                 * U+0021 EXCLAMATION MARK (!) Switch to the
                                 * markup declaration open state.
                                 */
                                state = Tokenizer.MARKUP_DECLARATION_OPEN;
                                continue stateloop;
                            case '/':
                                /*
                                 * U+002F SOLIDUS (/) Switch to the close tag
                                 * open state.
                                 */
                                state = Tokenizer.CLOSE_TAG_OPEN_PCDATA;
                                continue stateloop;
                            case '?':
                                /*
                                 * U+003F QUESTION MARK (?) Parse error.
                                 */
                                errProcessingInstruction();
                                /*
                                 * Switch to the bogus comment state.
                                 */
                                clearLongStrBufAndAppendToComment(c);
                                state = Tokenizer.BOGUS_COMMENT;
                                continue stateloop;
                            case '>':
                                /*
                                 * U+003E GREATER-THAN SIGN (>) Parse error.
                                 */
                                errLtGt();
                                /*
                                 * Emit a U+003C LESS-THAN SIGN character token
                                 * and a U+003E GREATER-THAN SIGN character
                                 * token.
                                 */
                                tokenHandler.characters(Tokenizer.LT_GT, 0, 2);
                                /* Switch to the data state. */
                                cstart = pos + 1;
                                state = Tokenizer.DATA;
                                continue stateloop;
                            default:
                                /*
                                 * Anything else Parse error.
                                 */
                                errBadCharAfterLt(c);
                                /*
                                 * Emit a U+003C LESS-THAN SIGN character token
                                 */
                                tokenHandler.characters(Tokenizer.LT_GT, 0, 1);
                                /*
                                 * and reconsume the current input character in
                                 * the data state.
                                 */
                                cstart = pos;
                                state = Tokenizer.DATA;
                                reconsume = true;
                                continue stateloop;
                        }
                    }
                    // FALL THROUGH DON'T REORDER
                case TAG_NAME:
                    tagnameloop: for (;;) {
                        if (++pos == endPos) {
                            break stateloop;
                        }
                        c = checkChar(buf, pos);
                        /*
                         * Consume the next input character:
                         */
                        switch (c) {
                            case '\r':
                                silentCarriageReturn();
                                strBufToElementNameString();
                                state = Tokenizer.BEFORE_ATTRIBUTE_NAME;
                                break stateloop;
                            case '\n':
                                silentLineFeed();
                            case ' ':
                            case '\t':
                            case '\u000C':
                                /*
                                 * U+0009 CHARACTER TABULATION U+000A LINE FEED
                                 * (LF) U+000C FORM FEED (FF) U+0020 SPACE
                                 * Switch to the before attribute name state.
                                 */
                                strBufToElementNameString();
                                state = Tokenizer.BEFORE_ATTRIBUTE_NAME;
                                break tagnameloop;
                            // continue stateloop;
                            case '/':
                                /*
                                 * U+002F SOLIDUS (/) Switch to the self-closing
                                 * start tag state.
                                 */
                                strBufToElementNameString();
                                state = Tokenizer.SELF_CLOSING_START_TAG;
                                continue stateloop;
                            case '>':
                                /*
                                 * U+003E GREATER-THAN SIGN (>) Emit the current
                                 * tag token.
                                 */
                                strBufToElementNameString();
                                state = emitCurrentTagToken(false, pos);
                                if (shouldSuspend) {
                                    break stateloop;
                                }
                                /*
                                 * Switch to the data state.
                                 */
                                continue stateloop;
                            case '\u0000':
                                c = '\uFFFD';
                                // fall thru
                            default:
                                if (c >= 'A' && c <= 'Z') {
                                    /*
                                     * U+0041 LATIN CAPITAL LETTER A through to
                                     * U+005A LATIN CAPITAL LETTER Z Append the
                                     * lowercase version of the current input
                                     * character (add 0x0020 to the character's
                                     * code point) to the current tag token's
                                     * tag name.
                                     */
                                    c += 0x20;
                                }
                                /*
                                 * Anything else Append the current input
                                 * character to the current tag token's tag
                                 * name.
                                 */
                                appendStrBuf(c);
                                /*
                                 * Stay in the tag name state.
                                 */
                                continue;
                        }
                    }
                    // FALLTHRU DON'T REORDER
                case BEFORE_ATTRIBUTE_NAME:
                    beforeattributenameloop: for (;;) {
                        if (reconsume) {
                            reconsume = false;
                        } else {
                            if (++pos == endPos) {
                                break stateloop;
                            }
                            c = checkChar(buf, pos);
                        }
                        /*
                         * Consume the next input character:
                         */
                        switch (c) {
                            case '\r':
                                silentCarriageReturn();
                                break stateloop;
                            case '\n':
                                silentLineFeed();
                                // fall thru
                            case ' ':
                            case '\t':
                            case '\u000C':
                                /*
                                 * U+0009 CHARACTER TABULATION U+000A LINE FEED
                                 * (LF) U+000C FORM FEED (FF) U+0020 SPACE Stay
                                 * in the before attribute name state.
                                 */
                                continue;
                            case '/':
                                /*
                                 * U+002F SOLIDUS (/) Switch to the self-closing
                                 * start tag state.
                                 */
                                state = Tokenizer.SELF_CLOSING_START_TAG;
                                continue stateloop;
                            case '>':
                                /*
                                 * U+003E GREATER-THAN SIGN (>) Emit the current
                                 * tag token.
                                 */
                                state = emitCurrentTagToken(false, pos);
                                if (shouldSuspend) {
                                    break stateloop;
                                }
                                /*
                                 * Switch to the data state.
                                 */
                                continue stateloop;
                            case '\u0000':
                                c = '\uFFFD';
                                // fall thru
                            case '\"':
                            case '\'':
                            case '=':
                                /*
                                 * U+0022 QUOTATION MARK (") U+0027 APOSTROPHE
                                 * (') U+003D EQUALS SIGN (=) Parse error.
                                 */
                                errBadCharBeforeAttributeNameOrNull(c);
                                /*
                                 * Treat it as per the "anything else" entry
                                 * below.
                                 */
                            default:
                                /*
                                 * Anything else Start a new attribute in the
                                 * current tag token.
                                 */
                                if (c >= 'A' && c <= 'Z') {
                                    /*
                                     * U+0041 LATIN CAPITAL LETTER A through to
                                     * U+005A LATIN CAPITAL LETTER Z Set that
                                     * attribute's name to the lowercase version
                                     * of the current input character (add
                                     * 0x0020 to the character's code point)
                                     */
                                    c += 0x20;
                                }
                                /*
                                 * Set that attribute's name to the current
                                 * input character,
                                 */
                                clearStrBufAndAppendCurrentC(c);
                                /*
                                 * and its value to the empty string.
                                 */
                                // Will do later.
                                /*
                                 * Switch to the attribute name state.
                                 */
                                state = Tokenizer.ATTRIBUTE_NAME;
                                break beforeattributenameloop;
                            // continue stateloop;
                        }
                    }
                    // FALLTHRU DON'T REORDER
                case ATTRIBUTE_NAME:
                    attributenameloop: for (;;) {
                        if (++pos == endPos) {
                            break stateloop;
                        }
                        c = checkChar(buf, pos);
                        /*
                         * Consume the next input character:
                         */
                        switch (c) {
                            case '\r':
                                silentCarriageReturn();
                                attributeNameComplete();
                                state = Tokenizer.AFTER_ATTRIBUTE_NAME;
                                break stateloop;
                            case '\n':
                                silentLineFeed();
                                // fall thru
                            case ' ':
                            case '\t':
                            case '\u000C':
                                /*
                                 * U+0009 CHARACTER TABULATION U+000A LINE FEED
                                 * (LF) U+000C FORM FEED (FF) U+0020 SPACE
                                 * Switch to the after attribute name state.
                                 */
                                attributeNameComplete();
                                state = Tokenizer.AFTER_ATTRIBUTE_NAME;
                                continue stateloop;
                            case '/':
                                /*
                                 * U+002F SOLIDUS (/) Switch to the self-closing
                                 * start tag state.
                                 */
                                attributeNameComplete();
                                addAttributeWithoutValue();
                                state = Tokenizer.SELF_CLOSING_START_TAG;
                                continue stateloop;
                            case '=':
                                /*
                                 * U+003D EQUALS SIGN (=) Switch to the before
                                 * attribute value state.
                                 */
                                attributeNameComplete();
                                state = Tokenizer.BEFORE_ATTRIBUTE_VALUE;
                                break attributenameloop;
                            // continue stateloop;
                            case '>':
                                /*
                                 * U+003E GREATER-THAN SIGN (>) Emit the current
                                 * tag token.
                                 */
                                attributeNameComplete();
                                addAttributeWithoutValue();
                                state = emitCurrentTagToken(false, pos);
                                if (shouldSuspend) {
                                    break stateloop;
                                }
                                /*
                                 * Switch to the data state.
                                 */
                                continue stateloop;
                            case '\u0000':
                                c = '\uFFFD';
                                // fall thru
                            case '\"':
                            case '\'':
                                /*
                                 * U+0022 QUOTATION MARK (") U+0027 APOSTROPHE
                                 * (') Parse error.
                                 */
                                errQuoteInAttributeNameOrNull(c);
                                /*
                                 * Treat it as per the "anything else" entry
                                 * below.
                                 */
                            default:
                                if (c >= 'A' && c <= 'Z') {
                                    /*
                                     * U+0041 LATIN CAPITAL LETTER A through to
                                     * U+005A LATIN CAPITAL LETTER Z Append the
                                     * lowercase version of the current input
                                     * character (add 0x0020 to the character's
                                     * code point) to the current attribute's
                                     * name.
                                     */
                                    c += 0x20;
                                }
                                /*
                                 * Anything else Append the current input
                                 * character to the current attribute's name.
                                 */
                                appendStrBuf(c);
                                /*
                                 * Stay in the attribute name state.
                                 */
                                continue;
                        }
                    }
                    // FALLTHRU DON'T REORDER
                case BEFORE_ATTRIBUTE_VALUE:
                    beforeattributevalueloop: for (;;) {
                        if (++pos == endPos) {
                            break stateloop;
                        }
                        c = checkChar(buf, pos);
                        /*
                         * Consume the next input character:
                         */
                        switch (c) {
                            case '\r':
                                silentCarriageReturn();
                                break stateloop;
                            case '\n':
                                silentLineFeed();
                                // fall thru
                            case ' ':
                            case '\t':
                            case '\u000C':
                                /*
                                 * U+0009 CHARACTER TABULATION U+000A LINE FEED
                                 * (LF) U+000C FORM FEED (FF) U+0020 SPACE Stay
                                 * in the before attribute value state.
                                 */
                                continue;
                            case '"':
                                /*
                                 * U+0022 QUOTATION MARK (") Switch to the
                                 * attribute value (double-quoted) state.
                                 */
                                clearLongStrBufForNextState();
                                state = Tokenizer.ATTRIBUTE_VALUE_DOUBLE_QUOTED;
                                break beforeattributevalueloop;
                            // continue stateloop;
                            case '&':
                                /*
                                 * U+0026 AMPERSAND (&) Switch to the attribute
                                 * value (unquoted) state and reconsume this
                                 * input character.
                                 */
                                clearLongStrBuf();
                                state = Tokenizer.ATTRIBUTE_VALUE_UNQUOTED;
                                reconsume = true;
                                continue stateloop;
                            case '\'':
                                /*
                                 * U+0027 APOSTROPHE (') Switch to the attribute
                                 * value (single-quoted) state.
                                 */
                                clearLongStrBufForNextState();
                                state = Tokenizer.ATTRIBUTE_VALUE_SINGLE_QUOTED;
                                continue stateloop;
                            case '>':
                                /*
                                 * U+003E GREATER-THAN SIGN (>) Parse error.
                                 */
                                errAttributeValueMissing();
                                /*
                                 * Emit the current tag token.
                                 */
                                addAttributeWithoutValue();
                                state = emitCurrentTagToken(false, pos);
                                if (shouldSuspend) {
                                    break stateloop;
                                }
                                /*
                                 * Switch to the data state.
                                 */
                                continue stateloop;
                            case '\u0000':
                                c = '\uFFFD';
                                // fall thru
                            case '=':
                                /*
                                 * U+003D EQUALS SIGN (=) Parse error.
                                 */
                                errEqualsInUnquotedAttributeOrNull(c);
                                /*
                                 * Treat it as per the "anything else" entry
                                 * below.
                                 */
                            default:
                                // [NOCPP[
                                errHtml4NonNameInUnquotedAttribute(c);
                                // ]NOCPP]
                                /*
                                 * Anything else Append the current input
                                 * character to the current attribute's value.
                                 */
                                clearLongStrBufAndAppendCurrentC(c);
                                /*
                                 * Switch to the attribute value (unquoted)
                                 * state.
                                 */

                                state = Tokenizer.ATTRIBUTE_VALUE_UNQUOTED;
                                continue stateloop;
                        }
                    }
                    // FALLTHRU DON'T REORDER
                case ATTRIBUTE_VALUE_DOUBLE_QUOTED:
                    attributevaluedoublequotedloop: for (;;) {
                        if (reconsume) {
                            reconsume = false;
                        } else {
                            if (++pos == endPos) {
                                break stateloop;
                            }
                            c = checkChar(buf, pos);
                        }
                        /*
                         * Consume the next input character:
                         */
                        switch (c) {
                            case '"':
                                /*
                                 * U+0022 QUOTATION MARK (") Switch to the after
                                 * attribute value (quoted) state.
                                 */
                                addAttributeWithValue();

                                state = Tokenizer.AFTER_ATTRIBUTE_VALUE_QUOTED;
                                break attributevaluedoublequotedloop;
                            // continue stateloop;
                            case '&':
                                /*
                                 * U+0026 AMPERSAND (&) Switch to the character
                                 * reference in attribute value state, with the
                                 * additional allowed character being U+0022
                                 * QUOTATION MARK (").
                                 */
                                clearStrBufAndAppendCurrentC(c);
                                rememberAmpersandLocation('\"');
                                returnState = state;
                                state = Tokenizer.CONSUME_CHARACTER_REFERENCE;
                                continue stateloop;
                            case '\r':
                                appendLongStrBufCarriageReturn();
                                break stateloop;
                            case '\n':
                                appendLongStrBufLineFeed();
                                continue;
                            case '\u0000':
                                c = '\uFFFD';
                                // fall thru
                            default:
                                /*
                                 * Anything else Append the current input
                                 * character to the current attribute's value.
                                 */
                                appendLongStrBuf(c);
                                /*
                                 * Stay in the attribute value (double-quoted)
                                 * state.
                                 */
                                continue;
                        }
                    }
                    // FALLTHRU DON'T REORDER
                case AFTER_ATTRIBUTE_VALUE_QUOTED:
                    afterattributevaluequotedloop: for (;;) {
                        if (++pos == endPos) {
                            break stateloop;
                        }
                        c = checkChar(buf, pos);
                        /*
                         * Consume the next input character:
                         */
                        switch (c) {
                            case '\r':
                                silentCarriageReturn();
                                state = Tokenizer.BEFORE_ATTRIBUTE_NAME;
                                break stateloop;
                            case '\n':
                                silentLineFeed();
                                // fall thru
                            case ' ':
                            case '\t':
                            case '\u000C':
                                /*
                                 * U+0009 CHARACTER TABULATION U+000A LINE FEED
                                 * (LF) U+000C FORM FEED (FF) U+0020 SPACE
                                 * Switch to the before attribute name state.
                                 */
                                state = Tokenizer.BEFORE_ATTRIBUTE_NAME;
                                continue stateloop;
                            case '/':
                                /*
                                 * U+002F SOLIDUS (/) Switch to the self-closing
                                 * start tag state.
                                 */
                                state = Tokenizer.SELF_CLOSING_START_TAG;
                                break afterattributevaluequotedloop;
                            // continue stateloop;
                            case '>':
                                /*
                                 * U+003E GREATER-THAN SIGN (>) Emit the current
                                 * tag token.
                                 */
                                state = emitCurrentTagToken(false, pos);
                                if (shouldSuspend) {
                                    break stateloop;
                                }
                                /*
                                 * Switch to the data state.
                                 */
                                continue stateloop;
                            default:
                                /*
                                 * Anything else Parse error.
                                 */
                                errNoSpaceBetweenAttributes();
                                /*
                                 * Reconsume the character in the before
                                 * attribute name state.
                                 */
                                state = Tokenizer.BEFORE_ATTRIBUTE_NAME;
                                reconsume = true;
                                continue stateloop;
                        }
                    }
                    // FALLTHRU DON'T REORDER
                case SELF_CLOSING_START_TAG:
                    if (++pos == endPos) {
                        break stateloop;
                    }
                    c = checkChar(buf, pos);
                    /*
                     * Consume the next input character:
                     */
                    switch (c) {
                        case '>':
                            /*
                             * U+003E GREATER-THAN SIGN (>) Set the self-closing
                             * flag of the current tag token. Emit the current
                             * tag token.
                             */
                            // [NOCPP[
                            errHtml4XmlVoidSyntax();
                            // ]NOCPP]
                            state = emitCurrentTagToken(true, pos);
                            if (shouldSuspend) {
                                break stateloop;
                            }
                            /*
                             * Switch to the data state.
                             */
                            continue stateloop;
                        default:
                            /* Anything else Parse error. */
                            errSlashNotFollowedByGt();
                            /*
                             * Reconsume the character in the before attribute
                             * name state.
                             */
                            state = Tokenizer.BEFORE_ATTRIBUTE_NAME;
                            reconsume = true;
                            continue stateloop;
                    }
                    // XXX reorder point
                case ATTRIBUTE_VALUE_UNQUOTED:
                    for (;;) {
                        if (reconsume) {
                            reconsume = false;
                        } else {
                            if (++pos == endPos) {
                                break stateloop;
                            }
                            c = checkChar(buf, pos);
                        }
                        /*
                         * Consume the next input character:
                         */
                        switch (c) {
                            case '\r':
                                silentCarriageReturn();
                                addAttributeWithValue();
                                state = Tokenizer.BEFORE_ATTRIBUTE_NAME;
                                break stateloop;
                            case '\n':
                                silentLineFeed();
                                // fall thru
                            case ' ':
                            case '\t':
                            case '\u000C':
                                /*
                                 * U+0009 CHARACTER TABULATION U+000A LINE FEED
                                 * (LF) U+000C FORM FEED (FF) U+0020 SPACE
                                 * Switch to the before attribute name state.
                                 */
                                addAttributeWithValue();
                                state = Tokenizer.BEFORE_ATTRIBUTE_NAME;
                                continue stateloop;
                            case '&':
                                /*
                                 * U+0026 AMPERSAND (&) Switch to the character
                                 * reference in attribute value state, with no +
                                 * additional allowed character.
                                 */
                                clearStrBufAndAppendCurrentC(c);
                                rememberAmpersandLocation('\u0000');
                                returnState = state;
                                state = Tokenizer.CONSUME_CHARACTER_REFERENCE;
                                continue stateloop;
                            case '>':
                                /*
                                 * U+003E GREATER-THAN SIGN (>) Emit the current
                                 * tag token.
                                 */
                                addAttributeWithValue();
                                state = emitCurrentTagToken(false, pos);
                                if (shouldSuspend) {
                                    break stateloop;
                                }
                                /*
                                 * Switch to the data state.
                                 */
                                continue stateloop;
                            case '\u0000':
                                c = '\uFFFD';
                                // fall thru
                            case '<':
                            case '\"':
                            case '\'':
                            case '=':
                                /*
                                 * U+0022 QUOTATION MARK (") U+0027 APOSTROPHE
                                 * (') U+003D EQUALS SIGN (=) Parse error.
                                 */
                                errWarnUnquotedAttributeValOrNull(c);
                                /*
                                 * Treat it as per the "anything else" entry
                                 * below.
                                 */
                                // fall through
                            default:
                                // [NOCPP]
                                errHtml4NonNameInUnquotedAttribute(c);
                                // ]NOCPP]
                                /*
                                 * Anything else Append the current input
                                 * character to the current attribute's value.
                                 */
                                appendLongStrBuf(c);
                                /*
                                 * Stay in the attribute value (unquoted) state.
                                 */
                                continue;
                        }
                    }
                    // XXX reorder point
                case AFTER_ATTRIBUTE_NAME:
                    for (;;) {
                        if (++pos == endPos) {
                            break stateloop;
                        }
                        c = checkChar(buf, pos);
                        /*
                         * Consume the next input character:
                         */
                        switch (c) {
                            case '\r':
                                silentCarriageReturn();
                                break stateloop;
                            case '\n':
                                silentLineFeed();
                                // fall thru
                            case ' ':
                            case '\t':
                            case '\u000C':
                                /*
                                 * U+0009 CHARACTER TABULATION U+000A LINE FEED
                                 * (LF) U+000C FORM FEED (FF) U+0020 SPACE Stay
                                 * in the after attribute name state.
                                 */
                                continue;
                            case '/':
                                /*
                                 * U+002F SOLIDUS (/) Switch to the self-closing
                                 * start tag state.
                                 */
                                addAttributeWithoutValue();
                                state = Tokenizer.SELF_CLOSING_START_TAG;
                                continue stateloop;
                            case '=':
                                /*
                                 * U+003D EQUALS SIGN (=) Switch to the before
                                 * attribute value state.
                                 */
                                state = Tokenizer.BEFORE_ATTRIBUTE_VALUE;
                                continue stateloop;
                            case '>':
                                /*
                                 * U+003E GREATER-THAN SIGN (>) Emit the current
                                 * tag token.
                                 */
                                addAttributeWithoutValue();
                                state = emitCurrentTagToken(false, pos);
                                if (shouldSuspend) {
                                    break stateloop;
                                }
                                /*
                                 * Switch to the data state.
                                 */
                                continue stateloop;
                            case '\u0000':
                                c = '\uFFFD';
                                // fall thru
                            case '\"':
                            case '\'':
                                errQuoteInAttributeNameOrNull(c);
                                /*
                                 * Treat it as per the "anything else" entry
                                 * below.
                                 */
                            default:
                                addAttributeWithoutValue();
                                /*
                                 * Anything else Start a new attribute in the
                                 * current tag token.
                                 */
                                if (c >= 'A' && c <= 'Z') {
                                    /*
                                     * U+0041 LATIN CAPITAL LETTER A through to
                                     * U+005A LATIN CAPITAL LETTER Z Set that
                                     * attribute's name to the lowercase version
                                     * of the current input character (add
                                     * 0x0020 to the character's code point)
                                     */
                                    c += 0x20;
                                }
                                /*
                                 * Set that attribute's name to the current
                                 * input character,
                                 */
                                clearStrBufAndAppendCurrentC(c);
                                /*
                                 * and its value to the empty string.
                                 */
                                // Will do later.
                                /*
                                 * Switch to the attribute name state.
                                 */
                                state = Tokenizer.ATTRIBUTE_NAME;
                                continue stateloop;
                        }
                    }
                    // XXX reorder point
                case BOGUS_COMMENT:
                    boguscommentloop: for (;;) {
                        if (reconsume) {
                            reconsume = false;
                        } else {
                            if (++pos == endPos) {
                                break stateloop;
                            }
                            c = checkChar(buf, pos);
                        }
                        /*
                         * (This can only happen if the content model flag is
                         * set to the PCDATA state.)
                         * 
                         * Consume every character up to and including the first
                         * U+003E GREATER-THAN SIGN character (>) or the end of
                         * the file (EOF), whichever comes first. Emit a comment
                         * token whose data is the concatenation of all the
                         * characters starting from and including the character
                         * that caused the state machine to switch into the
                         * bogus comment state, up to and including the
                         * character immediately before the last consumed
                         * character (i.e. up to the character just before the
                         * U+003E or EOF character). (If the comment was started
                         * by the end of the file (EOF), the token is empty.)
                         * 
                         * Switch to the data state.
                         * 
                         * If the end of the file was reached, reconsume the EOF
                         * character.
                         */
                        switch (c) {
                            case '>':
                                emitComment(0, pos);
                                state = Tokenizer.DATA;
                                continue stateloop;
                            case '-':
                                appendLongStrBuf(c);
                                state = Tokenizer.BOGUS_COMMENT_HYPHEN;
                                break boguscommentloop;
                            case '\r':
                                appendLongStrBufCarriageReturn();
                                break stateloop;
                            case '\n':
                                appendLongStrBufLineFeed();
                                continue;
                            case '\u0000':
                                c = '\uFFFD';
                                // fall thru
                            default:
                                appendLongStrBuf(c);
                                continue;
                        }
                    }
                    // FALLTHRU DON'T REORDER
                case BOGUS_COMMENT_HYPHEN:
                    boguscommenthyphenloop: for (;;) {
                        if (++pos == endPos) {
                            break stateloop;
                        }
                        c = checkChar(buf, pos);
                        switch (c) {
                            case '>':
                                // [NOCPP[
                                maybeAppendSpaceToBogusComment();
                                // ]NOCPP]
                                emitComment(0, pos);
                                state = Tokenizer.DATA;
                                continue stateloop;
                            case '-':
                                appendSecondHyphenToBogusComment();
                                continue boguscommenthyphenloop;
                            case '\r':
                                appendLongStrBufCarriageReturn();
                                state = Tokenizer.BOGUS_COMMENT;
                                break stateloop;
                            case '\n':
                                appendLongStrBufLineFeed();
                                state = Tokenizer.BOGUS_COMMENT;
                                continue stateloop;
                            case '\u0000':
                                c = '\uFFFD';
                                // fall thru
                            default:
                                appendLongStrBuf(c);
                                state = Tokenizer.BOGUS_COMMENT;
                                continue stateloop;
                        }
                    }
                    // XXX reorder point
                case MARKUP_DECLARATION_OPEN:
                    markupdeclarationopenloop: for (;;) {
                        if (++pos == endPos) {
                            break stateloop;
                        }
                        c = checkChar(buf, pos);
                        /*
                         * (This can only happen if the content model flag is
                         * set to the PCDATA state.)
                         * 
                         * If the next two characters are both U+002D
                         * HYPHEN-MINUS (-) characters, consume those two
                         * characters, create a comment token whose data is the
                         * empty string, and switch to the comment start state.
                         * 
                         * Otherwise, if the next seven characters are an ASCII
                         * case-insensitive match for the word "DOCTYPE", then
                         * consume those characters and switch to the DOCTYPE
                         * state.
                         * 
                         * Otherwise, if the insertion mode is "in foreign
                         * content" and the current node is not an element in
                         * the HTML namespace and the next seven characters are
                         * an ASCII case-sensitive match for the string
                         * "[CDATA[" (the five uppercase letters "CDATA" with a
                         * U+005B LEFT SQUARE BRACKET character before and
                         * after), then consume those characters and switch to
                         * the CDATA section state (which is unrelated to the
                         * content model flag's CDATA state).
                         * 
                         * Otherwise, is is a parse error. Switch to the bogus
                         * comment state. The next character that is consumed,
                         * if any, is the first character that will be in the
                         * comment.
                         */
                        switch (c) {
                            case '-':
                                clearLongStrBufAndAppendToComment(c);
                                state = Tokenizer.MARKUP_DECLARATION_HYPHEN;
                                break markupdeclarationopenloop;
                            // continue stateloop;
                            case 'd':
                            case 'D':
                                clearLongStrBufAndAppendToComment(c);
                                index = 0;
                                state = Tokenizer.MARKUP_DECLARATION_OCTYPE;
                                continue stateloop;
                            case '[':
                                if (tokenHandler.inForeign()) {
                                    clearLongStrBufAndAppendToComment(c);
                                    index = 0;
                                    state = Tokenizer.CDATA_START;
                                    continue stateloop;
                                } else {
                                    // fall through
                                }
                            default:
                                errBogusComment();
                                clearLongStrBuf();
                                state = Tokenizer.BOGUS_COMMENT;
                                reconsume = true;
                                continue stateloop;
                        }
                    }
                    // FALLTHRU DON'T REORDER
                case MARKUP_DECLARATION_HYPHEN:
                    markupdeclarationhyphenloop: for (;;) {
                        if (++pos == endPos) {
                            break stateloop;
                        }
                        c = checkChar(buf, pos);
                        switch (c) {
                            case '\u0000':
                                break stateloop;
                            case '-':
                                clearLongStrBufForNextState();
                                state = Tokenizer.COMMENT_START;
                                break markupdeclarationhyphenloop;
                            // continue stateloop;
                            default:
                                errBogusComment();
                                state = Tokenizer.BOGUS_COMMENT;
                                reconsume = true;
                                continue stateloop;
                        }
                    }
                    // FALLTHRU DON'T REORDER
                case COMMENT_START:
                    commentstartloop: for (;;) {
                        if (++pos == endPos) {
                            break stateloop;
                        }
                        c = checkChar(buf, pos);
                        /*
                         * Comment start state
                         * 
                         * 
                         * Consume the next input character:
                         */
                        switch (c) {
                            case '-':
                                /*
                                 * U+002D HYPHEN-MINUS (-) Switch to the comment
                                 * start dash state.
                                 */
                                appendLongStrBuf(c);
                                state = Tokenizer.COMMENT_START_DASH;
                                continue stateloop;
                            case '>':
                                /*
                                 * U+003E GREATER-THAN SIGN (>) Parse error.
                                 */
                                errPrematureEndOfComment();
                                /* Emit the comment token. */
                                emitComment(0, pos);
                                /*
                                 * Switch to the data state.
                                 */
                                state = Tokenizer.DATA;
                                continue stateloop;
                            case '\r':
                                appendLongStrBufCarriageReturn();
                                state = Tokenizer.COMMENT;
                                break stateloop;
                            case '\n':
                                appendLongStrBufLineFeed();
                                state = Tokenizer.COMMENT;
                                break commentstartloop;
                            case '\u0000':
                                c = '\uFFFD';
                                // fall thru
                            default:
                                /*
                                 * Anything else Append the input character to
                                 * the comment token's data.
                                 */
                                appendLongStrBuf(c);
                                /*
                                 * Switch to the comment state.
                                 */
                                state = Tokenizer.COMMENT;
                                break commentstartloop;
                            // continue stateloop;
                        }
                    }
                    // FALLTHRU DON'T REORDER
                case COMMENT:
                    commentloop: for (;;) {
                        if (++pos == endPos) {
                            break stateloop;
                        }
                        c = checkChar(buf, pos);
                        /*
                         * Comment state Consume the next input character:
                         */
                        switch (c) {
                            case '-':
                                /*
                                 * U+002D HYPHEN-MINUS (-) Switch to the comment
                                 * end dash state
                                 */
                                appendLongStrBuf(c);
                                state = Tokenizer.COMMENT_END_DASH;
                                break commentloop;
                            // continue stateloop;
                            case '\r':
                                appendLongStrBufCarriageReturn();
                                break stateloop;
                            case '\n':
                                appendLongStrBufLineFeed();
                                continue;
                            case '\u0000':
                                c = '\uFFFD';
                                // fall thru
                            default:
                                /*
                                 * Anything else Append the input character to
                                 * the comment token's data.
                                 */
                                appendLongStrBuf(c);
                                /*
                                 * Stay in the comment state.
                                 */
                                continue;
                        }
                    }
                    // FALLTHRU DON'T REORDER
                case COMMENT_END_DASH:
                    commentenddashloop: for (;;) {
                        if (++pos == endPos) {
                            break stateloop;
                        }
                        c = checkChar(buf, pos);
                        /*
                         * Comment end dash state Consume the next input
                         * character:
                         */
                        switch (c) {
                            case '-':
                                /*
                                 * U+002D HYPHEN-MINUS (-) Switch to the comment
                                 * end state
                                 */
                                appendLongStrBuf(c);
                                state = Tokenizer.COMMENT_END;
                                break commentenddashloop;
                            // continue stateloop;
                            case '\r':
                                appendLongStrBufCarriageReturn();
                                state = Tokenizer.COMMENT;
                                break stateloop;
                            case '\n':
                                appendLongStrBufLineFeed();
                                state = Tokenizer.COMMENT;
                                continue stateloop;
                            case '\u0000':
                                c = '\uFFFD';
                                // fall thru
                            default:
                                /*
                                 * Anything else Append a U+002D HYPHEN-MINUS
                                 * (-) character and the input character to the
                                 * comment token's data.
                                 */
                                appendLongStrBuf(c);
                                /*
                                 * Switch to the comment state.
                                 */
                                state = Tokenizer.COMMENT;
                                continue stateloop;
                        }
                    }
                    // FALLTHRU DON'T REORDER
                case COMMENT_END:
                    for (;;) {
                        if (++pos == endPos) {
                            break stateloop;
                        }
                        c = checkChar(buf, pos);
                        /*
                         * Comment end dash state Consume the next input
                         * character:
                         */
                        switch (c) {
                            case '>':
                                /*
                                 * U+003E GREATER-THAN SIGN (>) Emit the comment
                                 * token.
                                 */
                                emitComment(2, pos);
                                /*
                                 * Switch to the data state.
                                 */
                                state = Tokenizer.DATA;
                                continue stateloop;
                            case '-':
                                /* U+002D HYPHEN-MINUS (-) Parse error. */
                                errConsecutiveHyphens();
                                /*
                                 * Append a U+002D HYPHEN-MINUS (-) character to
                                 * the comment token's data.
                                 */
                                adjustDoubleHyphenAndAppendToLongStrBuf(c);
                                /*
                                 * Stay in the comment end state.
                                 */
                                continue;
                            case '\r':
                                adjustDoubleHyphenAndAppendToLongStrBufCarriageReturn();
                                state = Tokenizer.COMMENT;
                                break stateloop;
                            case '\n':
                                adjustDoubleHyphenAndAppendToLongStrBufLineFeed();
                                state = Tokenizer.COMMENT;
                                continue stateloop;
                            case '\u0000':
                                c = '\uFFFD';
                                // fall thru
                            default:
                                errConsecutiveHyphens();
                                /*
                                 * Append two U+002D HYPHEN-MINUS (-) characters
                                 * and the input character to the comment
                                 * token's data.
                                 */
                                adjustDoubleHyphenAndAppendToLongStrBuf(c);
                                /*
                                 * Switch to the comment state.
                                 */
                                state = Tokenizer.COMMENT;
                                continue stateloop;
                        }
                    }
                    // XXX reorder point
                case COMMENT_START_DASH:
                    if (++pos == endPos) {
                        break stateloop;
                    }
                    c = checkChar(buf, pos);
                    /*
                     * Comment start dash state
                     * 
                     * Consume the next input character:
                     */
                    switch (c) {
                        case '-':
                            /*
                             * U+002D HYPHEN-MINUS (-) Switch to the comment end
                             * state
                             */
                            appendLongStrBuf(c);
                            state = Tokenizer.COMMENT_END;
                            continue stateloop;
                        case '>':
                            errPrematureEndOfComment();
                            /* Emit the comment token. */
                            emitComment(1, pos);
                            /*
                             * Switch to the data state.
                             */
                            state = Tokenizer.DATA;
                            continue stateloop;
                        case '\r':
                            appendLongStrBufCarriageReturn();
                            state = Tokenizer.COMMENT;
                            break stateloop;
                        case '\n':
                            appendLongStrBufLineFeed();
                            state = Tokenizer.COMMENT;
                            continue stateloop;
                        case '\u0000':
                            c = '\uFFFD';
                            // fall thru
                        default:
                            /*
                             * Anything else Append a U+002D HYPHEN-MINUS (-)
                             * character and the input character to the comment
                             * token's data.
                             */
                            appendLongStrBuf(c);
                            /*
                             * Switch to the comment state.
                             */
                            state = Tokenizer.COMMENT;
                            continue stateloop;
                    }
                    // XXX reorder point
                case MARKUP_DECLARATION_OCTYPE:
                    markupdeclarationdoctypeloop: for (;;) {
                        if (++pos == endPos) {
                            break stateloop;
                        }
                        c = checkChar(buf, pos);
                        if (index < 6) { // OCTYPE.length
                            char folded = c;
                            if (c >= 'A' && c <= 'Z') {
                                folded += 0x20;
                            }
                            if (folded == Tokenizer.OCTYPE[index]) {
                                appendLongStrBuf(c);
                            } else {
                                errBogusComment();
                                state = Tokenizer.BOGUS_COMMENT;
                                reconsume = true;
                                continue stateloop;
                            }
                            index++;
                            continue;
                        } else {
                            state = Tokenizer.DOCTYPE;
                            reconsume = true;
                            break markupdeclarationdoctypeloop;
                            // continue stateloop;
                        }
                    }
                    // FALLTHRU DON'T REORDER
                case DOCTYPE:
                    doctypeloop: for (;;) {
                        if (reconsume) {
                            reconsume = false;
                        } else {
                            if (++pos == endPos) {
                                break stateloop;
                            }
                            c = checkChar(buf, pos);
                        }
                        initDoctypeFields();
                        /*
                         * Consume the next input character:
                         */
                        switch (c) {
                            case '\r':
                                silentCarriageReturn();
                                state = Tokenizer.BEFORE_DOCTYPE_NAME;
                                break stateloop;
                            case '\n':
                                silentLineFeed();
                                // fall thru
                            case ' ':
                            case '\t':
                            case '\u000C':
                                /*
                                 * U+0009 CHARACTER TABULATION U+000A LINE FEED
                                 * (LF) U+000C FORM FEED (FF) U+0020 SPACE
                                 * Switch to the before DOCTYPE name state.
                                 */
                                state = Tokenizer.BEFORE_DOCTYPE_NAME;
                                break doctypeloop;
                            // continue stateloop;
                            default:
                                /*
                                 * Anything else Parse error.
                                 */
                                errMissingSpaceBeforeDoctypeName();
                                /*
                                 * Reconsume the current character in the before
                                 * DOCTYPE name state.
                                 */
                                state = Tokenizer.BEFORE_DOCTYPE_NAME;
                                reconsume = true;
                                break doctypeloop;
                            // continue stateloop;
                        }
                    }
                    // FALLTHRU DON'T REORDER
                case BEFORE_DOCTYPE_NAME:
                    beforedoctypenameloop: for (;;) {
                        if (reconsume) {
                            reconsume = false;
                        } else {
                            if (++pos == endPos) {
                                break stateloop;
                            }
                            c = checkChar(buf, pos);
                        }
                        /*
                         * Consume the next input character:
                         */
                        switch (c) {
                            case '\r':
                                silentCarriageReturn();
                                break stateloop;
                            case '\n':
                                silentLineFeed();
                                // fall thru
                                /*
                                 * U+0009 CHARACTER TABULATION U+000A LINE FEED
                                 * (LF) U+000C FORM FEED (FF) U+0020 SPACE Stay
                                 * in the before DOCTYPE name state.
                                 */
                                continue;
                            case '>':
                                /*
                                 * U+003E GREATER-THAN SIGN (>) Parse error.
                                 */
                                errNamelessDoctype();
                                /*
                                 * Create a new DOCTYPE token. Set its
                                 * force-quirks flag to on.
                                 */
                                forceQuirks = true;
                                /*
                                 * Emit the token.
                                 */
                                emitDoctypeToken(pos);
                                /*
                                 * Switch to the data state.
                                 */
                                state = Tokenizer.DATA;
                                continue stateloop;
                            case '\u0000':
                                c = '\uFFFD';
                                // fall thru
                            default:
                                if (c >= 'A' && c <= 'Z') {
                                    /*
                                     * U+0041 LATIN CAPITAL LETTER A through to
                                     * U+005A LATIN CAPITAL LETTER Z Create a
                                     * new DOCTYPE token. Set the token's name
                                     * to the lowercase version of the input
                                     * character (add 0x0020 to the character's
                                     * code point).
                                     */
                                    c += 0x20;
                                }
                                /* Anything else Create a new DOCTYPE token. */
                                /*
                                 * Set the token's name name to the current
                                 * input character.
                                 */
                                clearStrBufAndAppendCurrentC(c);
                                /*
                                 * Switch to the DOCTYPE name state.
                                 */
                                state = Tokenizer.DOCTYPE_NAME;
                                break beforedoctypenameloop;
                            // continue stateloop;
                        }
                    }
                    // FALLTHRU DON'T REORDER
                case DOCTYPE_NAME:
                    doctypenameloop: for (;;) {
                        if (++pos == endPos) {
                            break stateloop;
                        }
                        c = checkChar(buf, pos);
                        /*
                         * Consume the next input character:
                         */
                        switch (c) {
                            case '\r':
                                silentCarriageReturn();
                                strBufToDoctypeName();
                                state = Tokenizer.AFTER_DOCTYPE_NAME;
                                break stateloop;
                            case '\n':
                                silentLineFeed();
                                // fall thru
                            case ' ':
                            case '\t':
                            case '\u000C':
                                /*
                                 * U+0009 CHARACTER TABULATION U+000A LINE FEED
                                 * (LF) U+000C FORM FEED (FF) U+0020 SPACE
                                 * Switch to the after DOCTYPE name state.
                                 */
                                strBufToDoctypeName();
                                state = Tokenizer.AFTER_DOCTYPE_NAME;
                                break doctypenameloop;
                            // continue stateloop;
                            case '>':
                                /*
                                 * U+003E GREATER-THAN SIGN (>) Emit the current
                                 * DOCTYPE token.
                                 */
                                strBufToDoctypeName();
                                emitDoctypeToken(pos);
                                /*
                                 * Switch to the data state.
                                 */
                                state = Tokenizer.DATA;
                                continue stateloop;
                            case '\u0000':
                                c = '\uFFFD';
                                // fall thru
                            default:
                                /*
                                 * U+0041 LATIN CAPITAL LETTER A through to
                                 * U+005A LATIN CAPITAL LETTER Z Append the
                                 * lowercase version of the input character (add
                                 * 0x0020 to the character's code point) to the
                                 * current DOCTYPE token's name.
                                 */
                                if (c >= 'A' && c <= 'Z') {
                                    c += 0x0020;
                                }
                                /*
                                 * Anything else Append the current input
                                 * character to the current DOCTYPE token's
                                 * name.
                                 */
                                appendStrBuf(c);
                                /*
                                 * Stay in the DOCTYPE name state.
                                 */
                                continue;
                        }
                    }
                    // FALLTHRU DON'T REORDER
                case AFTER_DOCTYPE_NAME:
                    afterdoctypenameloop: for (;;) {
                        if (++pos == endPos) {
                            break stateloop;
                        }
                        c = checkChar(buf, pos);
                        /*
                         * Consume the next input character:
                         */
                        switch (c) {
                            case '\r':
                                silentCarriageReturn();
                                break stateloop;
                            case '\n':
                                silentLineFeed();
                                // fall thru
                            case ' ':
                            case '\t':
                            case '\u000C':
                                /*
                                 * U+0009 CHARACTER TABULATION U+000A LINE FEED
                                 * (LF) U+000C FORM FEED (FF) U+0020 SPACE Stay
                                 * in the after DOCTYPE name state.
                                 */
                                continue;
                            case '>':
                                /*
                                 * U+003E GREATER-THAN SIGN (>) Emit the current
                                 * DOCTYPE token.
                                 */
                                emitDoctypeToken(pos);
                                /*
                                 * Switch to the data state.
                                 */
                                state = Tokenizer.DATA;
                                continue stateloop;
                            case 'p':
                            case 'P':
                                index = 0;
                                state = Tokenizer.DOCTYPE_UBLIC;
                                break afterdoctypenameloop;
                            // continue stateloop;
                            case 's':
                            case 'S':
                                index = 0;
                                state = Tokenizer.DOCTYPE_YSTEM;
                                continue stateloop;
                            default:
                                /*
                                 * Otherwise, this is the parse error.
                                 */
                                bogusDoctype();

                                /*
                                 * Set the DOCTYPE token's force-quirks flag to
                                 * on.
                                 */
                                // done by bogusDoctype();
                                /*
                                 * Switch to the bogus DOCTYPE state.
                                 */
                                state = Tokenizer.BOGUS_DOCTYPE;
                                continue stateloop;
                        }
                    }
                    // FALLTHRU DON'T REORDER
                case DOCTYPE_UBLIC:
                    doctypeublicloop: for (;;) {
                        if (++pos == endPos) {
                            break stateloop;
                        }
                        c = checkChar(buf, pos);
                        /*
                         * If the six characters starting from the current input
                         * character are an ASCII case-insensitive match for the
                         * word "PUBLIC", then consume those characters and
                         * switch to the before DOCTYPE public identifier state.
                         */
                        if (index < 5) { // UBLIC.length
                            char folded = c;
                            if (c >= 'A' && c <= 'Z') {
                                folded += 0x20;
                            }
                            if (folded != Tokenizer.UBLIC[index]) {
                                bogusDoctype();
                                // forceQuirks = true;
                                state = Tokenizer.BOGUS_DOCTYPE;
                                reconsume = true;
                                continue stateloop;
                            }
                            index++;
                            continue;
                        } else {
                            state = Tokenizer.BEFORE_DOCTYPE_PUBLIC_IDENTIFIER;
                            reconsume = true;
                            break doctypeublicloop;
                            // continue stateloop;
                        }
                    }
                    // FALLTHRU DON'T REORDER
                case BEFORE_DOCTYPE_PUBLIC_IDENTIFIER:
                    beforedoctypepublicidentifierloop: for (;;) {
                        if (reconsume) {
                            reconsume = false;
                        } else {
                            if (++pos == endPos) {
                                break stateloop;
                            }
                            c = checkChar(buf, pos);
                        }
                        /*
                         * Consume the next input character:
                         */
                        switch (c) {
                            case '\r':
                                silentCarriageReturn();
                                break stateloop;
                            case '\n':
                                silentLineFeed();
                                // fall thru
                            case ' ':
                            case '\t':
                            case '\u000C':
                                /*
                                 * U+0009 CHARACTER TABULATION U+000A LINE FEED
                                 * (LF) U+000C FORM FEED (FF) U+0020 SPACE Stay
                                 * in the before DOCTYPE public identifier
                                 * state.
                                 */
                                continue;
                            case '"':
                                /*
                                 * U+0022 QUOTATION MARK (") Set the DOCTYPE
                                 * token's public identifier to the empty string
                                 * (not missing),
                                 */
                                clearLongStrBufForNextState();
                                /*
                                 * then switch to the DOCTYPE public identifier
                                 * (double-quoted) state.
                                 */
                                state = Tokenizer.DOCTYPE_PUBLIC_IDENTIFIER_DOUBLE_QUOTED;
                                break beforedoctypepublicidentifierloop;
                            // continue stateloop;
                            case '\'':
                                /*
                                 * U+0027 APOSTROPHE (') Set the DOCTYPE token's
                                 * public identifier to the empty string (not
                                 * missing),
                                 */
                                clearLongStrBufForNextState();
                                /*
                                 * then switch to the DOCTYPE public identifier
                                 * (single-quoted) state.
                                 */
                                state = Tokenizer.DOCTYPE_PUBLIC_IDENTIFIER_SINGLE_QUOTED;
                                continue stateloop;
                            case '>':
                                /* U+003E GREATER-THAN SIGN (>) Parse error. */
                                errExpectedPublicId();
                                /*
                                 * Set the DOCTYPE token's force-quirks flag to
                                 * on.
                                 */
                                forceQuirks = true;
                                /*
                                 * Emit that DOCTYPE token.
                                 */
                                emitDoctypeToken(pos);
                                /*
                                 * Switch to the data state.
                                 */
                                state = Tokenizer.DATA;
                                continue stateloop;
                            default:
                                bogusDoctype();
                                /*
                                 * Set the DOCTYPE token's force-quirks flag to
                                 * on.
                                 */
                                // done by bogusDoctype();
                                /*
                                 * Switch to the bogus DOCTYPE state.
                                 */
                                state = Tokenizer.BOGUS_DOCTYPE;
                                continue stateloop;
                        }
                    }
                    // FALLTHRU DON'T REORDER
                case DOCTYPE_PUBLIC_IDENTIFIER_DOUBLE_QUOTED:
                    doctypepublicidentifierdoublequotedloop: for (;;) {
                        if (++pos == endPos) {
                            break stateloop;
                        }
                        c = checkChar(buf, pos);
                        /*
                         * Consume the next input character:
                         */
                        switch (c) {
                            case '"':
                                /*
                                 * U+0022 QUOTATION MARK (") Switch to the after
                                 * DOCTYPE public identifier state.
                                 */
                                publicIdentifier = longStrBufToString();
                                state = Tokenizer.AFTER_DOCTYPE_PUBLIC_IDENTIFIER;
                                break doctypepublicidentifierdoublequotedloop;
                            // continue stateloop;
                            case '>':
                                /*
                                 * U+003E GREATER-THAN SIGN (>) Parse error.
                                 */
                                errGtInPublicId();
                                /*
                                 * Set the DOCTYPE token's force-quirks flag to
                                 * on.
                                 */
                                forceQuirks = true;
                                /*
                                 * Emit that DOCTYPE token.
                                 */
                                publicIdentifier = longStrBufToString();
                                emitDoctypeToken(pos);
                                /*
                                 * Switch to the data state.
                                 */
                                state = Tokenizer.DATA;
                                continue stateloop;
                            case '\r':
                                appendLongStrBufCarriageReturn();
                                break stateloop;
                            case '\n':
                                appendLongStrBufLineFeed();
                                continue;
                            case '\u0000':
                                c = '\uFFFD';
                                // fall thru
                            default:
                                /*
                                 * Anything else Append the current input
                                 * character to the current DOCTYPE token's
                                 * public identifier.
                                 */
                                appendLongStrBuf(c);
                                /*
                                 * Stay in the DOCTYPE public identifier
                                 * (double-quoted) state.
                                 */
                                continue;
                        }
                    }
                    // FALLTHRU DON'T REORDER
                case AFTER_DOCTYPE_PUBLIC_IDENTIFIER:
                    afterdoctypepublicidentifierloop: for (;;) {
                        if (++pos == endPos) {
                            break stateloop;
                        }
                        c = checkChar(buf, pos);
                        /*
                         * Consume the next input character:
                         */
                        switch (c) {
                            case '\r':
                                silentCarriageReturn();
                                break stateloop;
                            case '\n':
                                silentLineFeed();
                                // fall thru
                            case ' ':
                            case '\t':
                            case '\u000C':
                                /*
                                 * U+0009 CHARACTER TABULATION U+000A LINE FEED
                                 * (LF) U+000C FORM FEED (FF) U+0020 SPACE Stay
                                 * in the after DOCTYPE public identifier state.
                                 */
                                continue;
                            case '"':
                                /*
                                 * U+0022 QUOTATION MARK (") Set the DOCTYPE
                                 * token's system identifier to the empty string
                                 * (not missing),
                                 */
                                clearLongStrBufForNextState();
                                /*
                                 * then switch to the DOCTYPE system identifier
                                 * (double-quoted) state.
                                 */
                                state = Tokenizer.DOCTYPE_SYSTEM_IDENTIFIER_DOUBLE_QUOTED;
                                break afterdoctypepublicidentifierloop;
                            // continue stateloop;
                            case '\'':
                                /*
                                 * U+0027 APOSTROPHE (') Set the DOCTYPE token's
                                 * system identifier to the empty string (not
                                 * missing),
                                 */
                                clearLongStrBufForNextState();
                                /*
                                 * then switch to the DOCTYPE system identifier
                                 * (single-quoted) state.
                                 */
                                state = Tokenizer.DOCTYPE_SYSTEM_IDENTIFIER_SINGLE_QUOTED;
                                continue stateloop;
                            case '>':
                                /*
                                 * U+003E GREATER-THAN SIGN (>) Emit the current
                                 * DOCTYPE token.
                                 */
                                emitDoctypeToken(pos);
                                /*
                                 * Switch to the data state.
                                 */
                                state = Tokenizer.DATA;
                                continue stateloop;
                            default:
                                bogusDoctype();
                                /*
                                 * Set the DOCTYPE token's force-quirks flag to
                                 * on.
                                 */
                                // done by bogusDoctype();
                                /*
                                 * Switch to the bogus DOCTYPE state.
                                 */
                                state = Tokenizer.BOGUS_DOCTYPE;
                                continue stateloop;
                        }
                    }
                    // FALLTHRU DON'T REORDER
                case DOCTYPE_SYSTEM_IDENTIFIER_DOUBLE_QUOTED:
                    doctypesystemidentifierdoublequotedloop: for (;;) {
                        if (++pos == endPos) {
                            break stateloop;
                        }
                        c = checkChar(buf, pos);
                        /*
                         * Consume the next input character:
                         */
                        switch (c) {
                            case '"':
                                /*
                                 * U+0022 QUOTATION MARK (") Switch to the after
                                 * DOCTYPE system identifier state.
                                 */
                                systemIdentifier = longStrBufToString();
                                state = Tokenizer.AFTER_DOCTYPE_SYSTEM_IDENTIFIER;
                                continue stateloop;
                            case '>':
                                /*
                                 * U+003E GREATER-THAN SIGN (>) Parse error.
                                 */
                                errGtInSystemId();
                                /*
                                 * Set the DOCTYPE token's force-quirks flag to
                                 * on.
                                 */
                                forceQuirks = true;
                                /*
                                 * Emit that DOCTYPE token.
                                 */
                                systemIdentifier = longStrBufToString();
                                emitDoctypeToken(pos);
                                /*
                                 * Switch to the data state.
                                 */
                                state = Tokenizer.DATA;
                                continue stateloop;
                            case '\r':
                                appendLongStrBufCarriageReturn();
                                break stateloop;
                            case '\n':
                                appendLongStrBufLineFeed();
                                continue;
                            case '\u0000':
                                c = '\uFFFD';
                                // fall thru
                            default:
                                /*
                                 * Anything else Append the current input
                                 * character to the current DOCTYPE token's
                                 * system identifier.
                                 */
                                appendLongStrBuf(c);
                                /*
                                 * Stay in the DOCTYPE system identifier
                                 * (double-quoted) state.
                                 */
                                continue;
                        }
                    }
                    // FALLTHRU DON'T REORDER
                case AFTER_DOCTYPE_SYSTEM_IDENTIFIER:
                    afterdoctypesystemidentifierloop: for (;;) {
                        if (++pos == endPos) {
                            break stateloop;
                        }
                        c = checkChar(buf, pos);
                        /*
                         * Consume the next input character:
                         */
                        switch (c) {
                            case '\r':
                                silentCarriageReturn();
                                break stateloop;
                            case '\n':
                                silentLineFeed();
                                // fall thru
                            case ' ':
                            case '\t':
                            case '\u000C':
                                /*
                                 * U+0009 CHARACTER TABULATION U+000A LINE FEED
                                 * (LF) U+000C FORM FEED (FF) U+0020 SPACE Stay
                                 * in the after DOCTYPE system identifier state.
                                 */
                                continue;
                            case '>':
                                /*
                                 * U+003E GREATER-THAN SIGN (>) Emit the current
                                 * DOCTYPE token.
                                 */
                                emitDoctypeToken(pos);
                                /*
                                 * Switch to the data state.
                                 */
                                state = Tokenizer.DATA;
                                continue stateloop;
                            default:
                                /*
                                 * Switch to the bogus DOCTYPE state. (This does
                                 * not set the DOCTYPE token's force-quirks flag
                                 * to on.)
                                 */
                                bogusDoctypeWithoutQuirks();
                                state = Tokenizer.BOGUS_DOCTYPE;
                                break afterdoctypesystemidentifierloop;
                            // continue stateloop;
                        }
                    }
                    // FALLTHRU DON'T REORDER
                case BOGUS_DOCTYPE:
                    for (;;) {
                        if (reconsume) {
                            reconsume = false;
                        } else {
                            if (++pos == endPos) {
                                break stateloop;
                            }
                            c = checkChar(buf, pos);
                        }
                        /*
                         * Consume the next input character:
                         */
                        switch (c) {
                            case '>':
                                /*
                                 * U+003E GREATER-THAN SIGN (>) Emit that
                                 * DOCTYPE token.
                                 */
                                emitDoctypeToken(pos);
                                /*
                                 * Switch to the data state.
                                 */
                                state = Tokenizer.DATA;
                                continue stateloop;
                            case '\r':
                                silentCarriageReturn();
                                break stateloop;
                            case '\n':
                                silentLineFeed();
                                // fall thru
                            default:
                                /*
                                 * Anything else Stay in the bogus DOCTYPE
                                 * state.
                                 */
                                continue;
                        }
                    }
                    // XXX reorder point
                case DOCTYPE_YSTEM:
                    doctypeystemloop: for (;;) {
                        if (++pos == endPos) {
                            break stateloop;
                        }
                        c = checkChar(buf, pos);
                        /*
                         * Otherwise, if the six characters starting from the
                         * current input character are an ASCII case-insensitive
                         * match for the word "SYSTEM", then consume those
                         * characters and switch to the before DOCTYPE system
                         * identifier state.
                         */
                        if (index < 5) { // YSTEM.length
                            char folded = c;
                            if (c >= 'A' && c <= 'Z') {
                                folded += 0x20;
                            }
                            if (folded != Tokenizer.YSTEM[index]) {
                                bogusDoctype();
                                state = Tokenizer.BOGUS_DOCTYPE;
                                reconsume = true;
                                continue stateloop;
                            }
                            index++;
                            continue stateloop;
                        } else {
                            state = Tokenizer.BEFORE_DOCTYPE_SYSTEM_IDENTIFIER;
                            reconsume = true;
                            break doctypeystemloop;
                            // continue stateloop;
                        }
                    }
                    // FALLTHRU DON'T REORDER
                case BEFORE_DOCTYPE_SYSTEM_IDENTIFIER:
                    beforedoctypesystemidentifierloop: for (;;) {
                        if (reconsume) {
                            reconsume = false;
                        } else {
                            if (++pos == endPos) {
                                break stateloop;
                            }
                            c = checkChar(buf, pos);
                        }
                        /*
                         * Consume the next input character:
                         */
                        switch (c) {
                            case '\r':
                                silentCarriageReturn();
                                break stateloop;
                            case '\n':
                                silentLineFeed();
                                // fall thru
                            case ' ':
                            case '\t':
                            case '\u000C':
                                /*
                                 * U+0009 CHARACTER TABULATION U+000A LINE FEED
                                 * (LF) U+000C FORM FEED (FF) U+0020 SPACE Stay
                                 * in the before DOCTYPE system identifier
                                 * state.
                                 */
                                continue;
                            case '"':
                                /*
                                 * U+0022 QUOTATION MARK (") Set the DOCTYPE
                                 * token's system identifier to the empty string
                                 * (not missing),
                                 */
                                clearLongStrBufForNextState();
                                /*
                                 * then switch to the DOCTYPE system identifier
                                 * (double-quoted) state.
                                 */
                                state = Tokenizer.DOCTYPE_SYSTEM_IDENTIFIER_DOUBLE_QUOTED;
                                continue stateloop;
                            case '\'':
                                /*
                                 * U+0027 APOSTROPHE (') Set the DOCTYPE token's
                                 * system identifier to the empty string (not
                                 * missing),
                                 */
                                clearLongStrBufForNextState();
                                /*
                                 * then switch to the DOCTYPE system identifier
                                 * (single-quoted) state.
                                 */
                                state = Tokenizer.DOCTYPE_SYSTEM_IDENTIFIER_SINGLE_QUOTED;
                                break beforedoctypesystemidentifierloop;
                            // continue stateloop;
                            case '>':
                                /* U+003E GREATER-THAN SIGN (>) Parse error. */
                                errExpectedSystemId();
                                /*
                                 * Set the DOCTYPE token's force-quirks flag to
                                 * on.
                                 */
                                forceQuirks = true;
                                /*
                                 * Emit that DOCTYPE token.
                                 */
                                emitDoctypeToken(pos);
                                /*
                                 * Switch to the data state.
                                 */
                                state = Tokenizer.DATA;
                                continue stateloop;
                            default:
                                bogusDoctype();
                                /*
                                 * Set the DOCTYPE token's force-quirks flag to
                                 * on.
                                 */
                                // done by bogusDoctype();
                                /*
                                 * Switch to the bogus DOCTYPE state.
                                 */
                                state = Tokenizer.BOGUS_DOCTYPE;
                                continue stateloop;
                        }
                    }
                    // FALLTHRU DON'T REORDER
                case DOCTYPE_SYSTEM_IDENTIFIER_SINGLE_QUOTED:
                    for (;;) {
                        if (++pos == endPos) {
                            break stateloop;
                        }
                        c = checkChar(buf, pos);
                        /*
                         * Consume the next input character:
                         */
                        switch (c) {
                            case '\'':
                                /*
                                 * U+0027 APOSTROPHE (') Switch to the after
                                 * DOCTYPE system identifier state.
                                 */
                                systemIdentifier = longStrBufToString();
                                state = Tokenizer.AFTER_DOCTYPE_SYSTEM_IDENTIFIER;
                                continue stateloop;
                            case '>':
                                errGtInSystemId();
                                /*
                                 * Set the DOCTYPE token's force-quirks flag to
                                 * on.
                                 */
                                forceQuirks = true;
                                /*
                                 * Emit that DOCTYPE token.
                                 */
                                systemIdentifier = longStrBufToString();
                                emitDoctypeToken(pos);
                                /*
                                 * Switch to the data state.
                                 */
                                state = Tokenizer.DATA;
                                continue stateloop;
                            case '\r':
                                appendLongStrBufCarriageReturn();
                                break stateloop;
                            case '\n':
                                appendLongStrBufLineFeed();
                                continue;
                            case '\u0000':
                                c = '\uFFFD';
                                // fall thru
                            default:
                                /*
                                 * Anything else Append the current input
                                 * character to the current DOCTYPE token's
                                 * system identifier.
                                 */
                                appendLongStrBuf(c);
                                /*
                                 * Stay in the DOCTYPE system identifier
                                 * (double-quoted) state.
                                 */
                                continue;
                        }
                    }
                    // XXX reorder point
                case DOCTYPE_PUBLIC_IDENTIFIER_SINGLE_QUOTED:
                    for (;;) {
                        if (++pos == endPos) {
                            break stateloop;
                        }
                        c = checkChar(buf, pos);
                        /*
                         * Consume the next input character:
                         */
                        switch (c) {
                            case '\'':
                                /*
                                 * U+0027 APOSTROPHE (') Switch to the after
                                 * DOCTYPE public identifier state.
                                 */
                                publicIdentifier = longStrBufToString();
                                state = Tokenizer.AFTER_DOCTYPE_PUBLIC_IDENTIFIER;
                                continue stateloop;
                            case '>':
                                errGtInPublicId();
                                /*
                                 * Set the DOCTYPE token's force-quirks flag to
                                 * on.
                                 */
                                forceQuirks = true;
                                /*
                                 * Emit that DOCTYPE token.
                                 */
                                publicIdentifier = longStrBufToString();
                                emitDoctypeToken(pos);
                                /*
                                 * Switch to the data state.
                                 */
                                state = Tokenizer.DATA;
                                continue stateloop;
                            case '\r':
                                appendLongStrBufCarriageReturn();
                                break stateloop;
                            case '\n':
                                appendLongStrBufLineFeed();
                                continue;
                            case '\u0000':
                                c = '\uFFFD';
                                // fall thru
                            default:
                                /*
                                 * Anything else Append the current input
                                 * character to the current DOCTYPE token's
                                 * public identifier.
                                 */
                                appendLongStrBuf(c);
                                /*
                                 * Stay in the DOCTYPE public identifier
                                 * (single-quoted) state.
                                 */
                                continue;
                        }
                    }
                    // XXX reorder point
                case CDATA_START:
                    for (;;) {
                        if (++pos == endPos) {
                            break stateloop;
                        }
                        c = checkChar(buf, pos);
                        if (index < 6) { // CDATA_LSQB.length
                            if (c == Tokenizer.CDATA_LSQB[index]) {
                                appendLongStrBuf(c);
                            } else {
                                errBogusComment();
                                state = Tokenizer.BOGUS_COMMENT;
                                reconsume = true;
                                continue stateloop;
                            }
                            index++;
                            continue;
                        } else {
                            cstart = pos; // start coalescing
                            state = Tokenizer.CDATA_SECTION;
                            reconsume = true;
                            break; // FALL THROUGH continue stateloop;
                        }
                    }
                    // WARNING FALLTHRU CASE TRANSITION: DON'T REORDER
                case CDATA_SECTION:
                    cdatasectionloop: for (;;) {
                        if (reconsume) {
                            reconsume = false;
                        } else {
                            if (++pos == endPos) {
                                break stateloop;
                            }
                            c = checkChar(buf, pos);
                        }
                        switch (c) {
                            case ']':
                                flushChars(buf, pos);
                                state = Tokenizer.CDATA_RSQB;
                                break cdatasectionloop; // FALL THROUGH
                            case '\u0000':
                                emitReplacementCharacter(buf, pos);
                                continue;
                            case '\r':
                                emitCarriageReturn(buf, pos);
                                break stateloop;
                            case '\n':
                                silentLineFeed();
                                // fall thru
                            default:
                                continue;
                        }
                    }
                    // WARNING FALLTHRU CASE TRANSITION: DON'T REORDER
                case CDATA_RSQB:
                    cdatarsqb: for (;;) {
                        if (++pos == endPos) {
                            break stateloop;
                        }
                        c = checkChar(buf, pos);
                        switch (c) {
                            case ']':
                                state = Tokenizer.CDATA_RSQB_RSQB;
                                break cdatarsqb;
                            default:
                                tokenHandler.characters(Tokenizer.RSQB_RSQB, 0,
                                        1);
                                cstart = pos;
                                state = Tokenizer.CDATA_SECTION;
                                reconsume = true;
                                continue stateloop;
                        }
                    }
                    // WARNING FALLTHRU CASE TRANSITION: DON'T REORDER
                case CDATA_RSQB_RSQB:
                    if (++pos == endPos) {
                        break stateloop;
                    }
                    c = checkChar(buf, pos);
                    switch (c) {
                        case '>':
                            cstart = pos + 1;
                            state = Tokenizer.DATA;
                            continue stateloop;
                        default:
                            tokenHandler.characters(Tokenizer.RSQB_RSQB, 0, 2);
                            cstart = pos;
                            state = Tokenizer.CDATA_SECTION;
                            reconsume = true;
                            continue stateloop;

                    }
                    // XXX reorder point
                case ATTRIBUTE_VALUE_SINGLE_QUOTED:
                    attributevaluesinglequotedloop: for (;;) {
                        if (reconsume) {
                            reconsume = false;
                        } else {
                            if (++pos == endPos) {
                                break stateloop;
                            }
                            c = checkChar(buf, pos);
                        }
                        /*
                         * Consume the next input character:
                         */
                        switch (c) {
                            case '\'':
                                /*
                                 * U+0027 APOSTROPHE (') Switch to the after
                                 * attribute value (quoted) state.
                                 */
                                addAttributeWithValue();

                                state = Tokenizer.AFTER_ATTRIBUTE_VALUE_QUOTED;
                                continue stateloop;
                            case '&':
                                /*
                                 * U+0026 AMPERSAND (&) Switch to the character
                                 * reference in attribute value state, with the
                                 * + additional allowed character being U+0027
                                 * APOSTROPHE (').
                                 */
                                clearStrBufAndAppendCurrentC(c);
                                rememberAmpersandLocation('\'');
                                returnState = state;
                                state = Tokenizer.CONSUME_CHARACTER_REFERENCE;
                                break attributevaluesinglequotedloop;
                            // continue stateloop;
                            case '\r':
                                appendLongStrBufCarriageReturn();
                                break stateloop;
                            case '\n':
                                appendLongStrBufLineFeed();
                                continue;
                            case '\u0000':
                                c = '\uFFFD';
                                // fall thru
                            default:
                                /*
                                 * Anything else Append the current input
                                 * character to the current attribute's value.
                                 */
                                appendLongStrBuf(c);
                                /*
                                 * Stay in the attribute value (double-quoted)
                                 * state.
                                 */
                                continue;
                        }
                    }
                    // FALLTHRU DON'T REORDER
                case CONSUME_CHARACTER_REFERENCE:
                    if (++pos == endPos) {
                        break stateloop;
                    }
                    c = checkChar(buf, pos);
                    if (c == '\u0000') {
                        break stateloop;
                    }
                    /*
                     * Unlike the definition is the spec, this state does not
                     * return a value and never requires the caller to
                     * backtrack. This state takes care of emitting characters
                     * or appending to the current attribute value. It also
                     * takes care of that in the case when consuming the
                     * character reference fails.
                     */
                    /*
                     * This section defines how to consume a character
                     * reference. This definition is used when parsing character
                     * references in text and in attributes.
                     * 
                     * The behavior depends on the identity of the next
                     * character (the one immediately after the U+0026 AMPERSAND
                     * character):
                     */
                    switch (c) {
                        case ' ':
                        case '\t':
                        case '\n':
                        case '\r': // we'll reconsume!
                        case '\u000C':
                        case '<':
                        case '&':
                            emitOrAppendStrBuf(returnState);
                            if ((returnState & (~1)) == 0) {
                                cstart = pos;
                            }
                            state = returnState;
                            reconsume = true;
                            continue stateloop;
                        case '#':
                            /*
                             * U+0023 NUMBER SIGN (#) Consume the U+0023 NUMBER
                             * SIGN.
                             */
                            appendStrBuf('#');
                            state = Tokenizer.CONSUME_NCR;
                            continue stateloop;
                        default:
                            if (c == additional) {
                                emitOrAppendStrBuf(returnState);
                                state = returnState;
                                reconsume = true;
                                continue stateloop;
                            }
                            entCol = -1;
                            lo = 0;
                            hi = (NamedCharacters.NAMES.length - 1);
                            candidate = -1;
                            strBufMark = 0;
                            state = Tokenizer.CHARACTER_REFERENCE_LOOP;
                            reconsume = true;
                            // FALL THROUGH continue stateloop;
                    }
                    // WARNING FALLTHRU CASE TRANSITION: DON'T REORDER
                case CHARACTER_REFERENCE_LOOP:
                    outer: for (;;) {
                        if (reconsume) {
                            reconsume = false;
                        } else {
                            if (++pos == endPos) {
                                break stateloop;
                            }
                            c = checkChar(buf, pos);
                        }
                        if (c == '\u0000') {
                            break stateloop;
                        }
                        entCol++;
                        /*
                         * Consume the maximum number of characters possible,
                         * with the consumed characters matching one of the
                         * identifiers in the first column of the named
                         * character references table (in a case-sensitive
                         * manner).
                         */
                        hiloop: for (;;) {
                            if (hi == -1) {
                                break hiloop;
                            }
                            if (entCol == NamedCharacters.NAMES[hi].length) {
                                break hiloop;
                            }
                            if (entCol > NamedCharacters.NAMES[hi].length) {
                                break outer;
                            } else if (c < NamedCharacters.NAMES[hi][entCol]) {
                                hi--;
                            } else {
                                break hiloop;
                            }
                        }

                        loloop: for (;;) {
                            if (hi < lo) {
                                break outer;
                            }
                            if (entCol == NamedCharacters.NAMES[lo].length) {
                                candidate = lo;
                                strBufMark = strBufLen;
                                lo++;
                            } else if (entCol > NamedCharacters.NAMES[lo].length) {
                                break outer;
                            } else if (c > NamedCharacters.NAMES[lo][entCol]) {
                                lo++;
                            } else {
                                break loloop;
                            }
                        }
                        if (hi < lo) {
                            break outer;
                        }
                        appendStrBuf(c);
                        continue;
                    }

                    // TODO warn about apos (IE) and TRADE (Opera)
                    if (candidate == -1) {
                        // reconsume deals with CR, LF or nul
                        /*
                         * If no match can be made, then this is a parse error.
                         */
                        errNoNamedCharacterMatch();
                        emitOrAppendStrBuf(returnState);
                        if ((returnState & (~1)) == 0) {
                            cstart = pos;
                        }
                        state = returnState;
                        reconsume = true;
                        continue stateloop;
                    } else {
                        // c can't be CR, LF or nul if we got here
                        char[] candidateArr = NamedCharacters.NAMES[candidate];
                        if (candidateArr[candidateArr.length - 1] != ';') {
                            /*
                             * If the last character matched is not a U+003B
                             * SEMICOLON (;), there is a parse error.
                             */
                            if ((returnState & (~1)) != 0) {
                                /*
                                 * If the entity is being consumed as part of an
                                 * attribute, and the last character matched is
                                 * not a U+003B SEMICOLON (;),
                                 */
                                char ch;
                                if (strBufMark == strBufLen) {
                                    ch = c;
                                } else {
                                    // if (strBufOffset != -1) {
                                    // ch = buf[strBufOffset + strBufMark];
                                    // } else {
                                    ch = strBuf[strBufMark];
                                    // }
                                }
                                if ((ch >= '0' && ch <= '9')
                                        || (ch >= 'A' && ch <= 'Z')
                                        || (ch >= 'a' && ch <= 'z')) {
                                    /*
                                     * and the next character is in the range
                                     * U+0030 DIGIT ZERO to U+0039 DIGIT NINE,
                                     * U+0041 LATIN CAPITAL LETTER A to U+005A
                                     * LATIN CAPITAL LETTER Z, or U+0061 LATIN
                                     * SMALL LETTER A to U+007A LATIN SMALL
                                     * LETTER Z, then, for historical reasons,
                                     * all the characters that were matched
                                     * after the U+0026 AMPERSAND (&) must be
                                     * unconsumed, and nothing is returned.
                                     */
                                    errNoNamedCharacterMatch();
                                    appendStrBufToLongStrBuf();
                                    state = returnState;
                                    reconsume = true;
                                    continue stateloop;
                                }
                            }
                            if ((returnState & (~1)) != 0) {
                                errUnescapedAmpersandInterpretedAsCharacterReference();
                            } else {
                                errNotSemicolonTerminated();
                            }
                        }

                        /*
                         * Otherwise, return a character token for the character
                         * corresponding to the entity name (as given by the
                         * second column of the named character references
                         * table).
                         */
                        char[] val = NamedCharacters.VALUES[candidate];
                        emitOrAppend(val, returnState);
                        // this is so complicated!
                        if (strBufMark < strBufLen) {
                            // if (strBufOffset != -1) {
                            // if ((returnState & (~1)) != 0) {
                            // for (int i = strBufMark; i < strBufLen; i++) {
                            // appendLongStrBuf(buf[strBufOffset + i]);
                            // }
                            // } else {
                            // tokenHandler.characters(buf, strBufOffset
                            // + strBufMark, strBufLen
                            // - strBufMark);
                            // }
                            // } else {
                            if ((returnState & (~1)) != 0) {
                                for (int i = strBufMark; i < strBufLen; i++) {
                                    appendLongStrBuf(strBuf[i]);
                                }
                            } else {
                                tokenHandler.characters(strBuf, strBufMark,
                                        strBufLen - strBufMark);
                            }
                            // }
                        }
                        if ((returnState & (~1)) == 0) {
                            cstart = pos;
                        }
                        state = returnState;
                        reconsume = true;
                        continue stateloop;
                        /*
                         * If the markup contains I'm &notit; I tell you, the
                         * entity is parsed as "not", as in, I'm ¬it; I tell
                         * you. But if the markup was I'm &notin; I tell you,
                         * the entity would be parsed as "notin;", resulting in
                         * I'm ∉ I tell you.
                         */
                    }
                    // XXX reorder point
                case CONSUME_NCR:
                    if (++pos == endPos) {
                        break stateloop;
                    }
                    c = checkChar(buf, pos);
                    prevValue = -1;
                    value = 0;
                    seenDigits = false;
                    /*
                     * The behavior further depends on the character after the
                     * U+0023 NUMBER SIGN:
                     */
                    switch (c) {
                        case 'x':
                        case 'X':

                            /*
                             * U+0078 LATIN SMALL LETTER X U+0058 LATIN CAPITAL
                             * LETTER X Consume the X.
                             * 
                             * Follow the steps below, but using the range of
                             * characters U+0030 DIGIT ZERO through to U+0039
                             * DIGIT NINE, U+0061 LATIN SMALL LETTER A through
                             * to U+0066 LATIN SMALL LETTER F, and U+0041 LATIN
                             * CAPITAL LETTER A, through to U+0046 LATIN CAPITAL
                             * LETTER F (in other words, 0-9, A-F, a-f).
                             * 
                             * When it comes to interpreting the number,
                             * interpret it as a hexadecimal number.
                             */
                            appendStrBuf(c);
                            state = Tokenizer.HEX_NCR_LOOP;
                            continue stateloop;
                        default:
                            /*
                             * Anything else Follow the steps below, but using
                             * the range of characters U+0030 DIGIT ZERO through
                             * to U+0039 DIGIT NINE (i.e. just 0-9).
                             * 
                             * When it comes to interpreting the number,
                             * interpret it as a decimal number.
                             */
                            state = Tokenizer.DECIMAL_NRC_LOOP;
                            reconsume = true;
                            // FALL THROUGH continue stateloop;
                    }
                    // WARNING FALLTHRU CASE TRANSITION: DON'T REORDER
                case DECIMAL_NRC_LOOP:
                    decimalloop: for (;;) {
                        if (reconsume) {
                            reconsume = false;
                        } else {
                            if (++pos == endPos) {
                                break stateloop;
                            }
                            c = checkChar(buf, pos);
                        }
                        // Deal with overflow gracefully
                        if (value < prevValue) {
                            value = 0x110000; // Value above Unicode range but
                            // within int
                            // range
                        }
                        prevValue = value;
                        /*
                         * Consume as many characters as match the range of
                         * characters given above.
                         */
                        if (c >= '0' && c <= '9') {
                            seenDigits = true;
                            value *= 10;
                            value += c - '0';
                            continue;
                        } else if (c == ';') {
                            if (seenDigits) {
                                if ((returnState & (~1)) == 0) {
                                    cstart = pos + 1;
                                }
                                state = Tokenizer.HANDLE_NCR_VALUE;
                                // FALL THROUGH continue stateloop;
                                break decimalloop;
                            } else {
                                errNoDigitsInNCR();
                                appendStrBuf(';');
                                emitOrAppendStrBuf(returnState);
                                if ((returnState & (~1)) == 0) {
                                    cstart = pos + 1;
                                }
                                state = returnState;
                                continue stateloop;
                            }
                        } else {
                            /*
                             * If no characters match the range, then don't
                             * consume any characters (and unconsume the U+0023
                             * NUMBER SIGN character and, if appropriate, the X
                             * character). This is a parse error; nothing is
                             * returned.
                             * 
                             * Otherwise, if the next character is a U+003B
                             * SEMICOLON, consume that too. If it isn't, there
                             * is a parse error.
                             */
                            if (!seenDigits) {
                                errNoDigitsInNCR();
                                emitOrAppendStrBuf(returnState);
                                if ((returnState & (~1)) == 0) {
                                    cstart = pos;
                                }
                                state = returnState;
                                reconsume = true;
                                continue stateloop;
                            } else {
                                errCharRefLacksSemicolon();
                                if ((returnState & (~1)) == 0) {
                                    cstart = pos;
                                }
                                state = Tokenizer.HANDLE_NCR_VALUE;
                                reconsume = true;
                                // FALL THROUGH continue stateloop;
                                break decimalloop;
                            }
                        }
                    }
                    // WARNING FALLTHRU CASE TRANSITION: DON'T REORDER
                case HANDLE_NCR_VALUE:
                    // WARNING previous state sets reconsume
                    // XXX inline this case if the method size can take it
                    handleNcrValue(returnState);
                    state = returnState;
                    continue stateloop;
                    // XXX reorder point
                case HEX_NCR_LOOP:
                    for (;;) {
                        if (++pos == endPos) {
                            break stateloop;
                        }
                        c = checkChar(buf, pos);
                        // Deal with overflow gracefully
                        if (value < prevValue) {
                            value = 0x110000; // Value above Unicode range but
                            // within int
                            // range
                        }
                        prevValue = value;
                        /*
                         * Consume as many characters as match the range of
                         * characters given above.
                         */
                        if (c >= '0' && c <= '9') {
                            seenDigits = true;
                            value *= 16;
                            value += c - '0';
                            continue;
                        } else if (c >= 'A' && c <= 'F') {
                            seenDigits = true;
                            value *= 16;
                            value += c - 'A' + 10;
                            continue;
                        } else if (c >= 'a' && c <= 'f') {
                            seenDigits = true;
                            value *= 16;
                            value += c - 'a' + 10;
                            continue;
                        } else if (c == ';') {
                            if (seenDigits) {
                                if ((returnState & (~1)) == 0) {
                                    cstart = pos + 1;
                                }
                                state = Tokenizer.HANDLE_NCR_VALUE;
                                continue stateloop;
                            } else {
                                errNoDigitsInNCR();
                                appendStrBuf(';');
                                emitOrAppendStrBuf(returnState);
                                if ((returnState & (~1)) == 0) {
                                    cstart = pos + 1;
                                }
                                state = returnState;
                                continue stateloop;
                            }
                        } else {
                            /*
                             * If no characters match the range, then don't
                             * consume any characters (and unconsume the U+0023
                             * NUMBER SIGN character and, if appropriate, the X
                             * character). This is a parse error; nothing is
                             * returned.
                             * 
                             * Otherwise, if the next character is a U+003B
                             * SEMICOLON, consume that too. If it isn't, there
                             * is a parse error.
                             */
                            if (!seenDigits) {
                                errNoDigitsInNCR();
                                emitOrAppendStrBuf(returnState);
                                if ((returnState & (~1)) == 0) {
                                    cstart = pos;
                                }
                                state = returnState;
                                reconsume = true;
                                continue stateloop;
                            } else {
                                errCharRefLacksSemicolon();
                                if ((returnState & (~1)) == 0) {
                                    cstart = pos;
                                }
                                state = Tokenizer.HANDLE_NCR_VALUE;
                                reconsume = true;
                                continue stateloop;
                            }
                        }
                    }
                    // XXX reorder point
                case PLAINTEXT:
                    plaintextloop: for (;;) {
                        if (reconsume) {
                            reconsume = false;
                        } else {
                            if (++pos == endPos) {
                                break stateloop;
                            }
                            c = checkChar(buf, pos);
                        }
                        switch (c) {
                            case '\u0000':
                                emitReplacementCharacter(buf, pos);
                                continue;
                            case '\r':
                                emitCarriageReturn(buf, pos);
                                break stateloop;
                            case '\n':
                                silentLineFeed();
                            default:
                                /*
                                 * Anything else Emit the input character as a
                                 * character token.
                                 */
                                /*
                                 * Stay in the data state.
                                 */
                                continue;
                        }
                    }
                    // XXX reorder point
                case CDATA:
                    cdataloop: for (;;) {
                        if (reconsume) {
                            reconsume = false;
                        } else {
                            if (++pos == endPos) {
                                break stateloop;
                            }
                            c = checkChar(buf, pos);
                        }
                        switch (c) {
                            case '<':
                                /*
                                 * U+003C LESS-THAN SIGN (<) When the content
                                 * model flag is set to the PCDATA state: switch
                                 * to the tag open state. When the content model
                                 * flag is set to either the RCDATA state or the
                                 * CDATA state, and the escape flag is false:
                                 * switch to the tag open state. Otherwise:
                                 * treat it as per the "anything else" entry
                                 * below.
                                 */
                                flushChars(buf, pos);

                                returnState = state;
                                state = Tokenizer.TAG_OPEN_NON_PCDATA;
                                break cdataloop; // FALL THRU continue
                            // stateloop;
                            case '\u0000':
                                emitReplacementCharacter(buf, pos);
                                continue;
                            case '\r':
                                emitCarriageReturn(buf, pos);
                                break stateloop;
                            case '\n':
                                silentLineFeed();
                            default:
                                /*
                                 * Anything else Emit the input character as a
                                 * character token.
                                 */
                                /*
                                 * Stay in the data state.
                                 */
                                continue;
                        }
                    }
                    // WARNING FALLTHRU CASE TRANSITION: DON'T REORDER
                case TAG_OPEN_NON_PCDATA:
                    tagopennonpcdataloop: for (;;) {
                        if (++pos == endPos) {
                            break stateloop;
                        }
                        c = checkChar(buf, pos);
                        switch (c) {
                            case '!':
                                tokenHandler.characters(Tokenizer.LT_GT, 0, 1);
                                cstart = pos;
                                state = Tokenizer.ESCAPE_EXCLAMATION;
                                break tagopennonpcdataloop; // FALL THRU
                            // continue
                            // stateloop;
                            case '/':
                                /*
                                 * If the content model flag is set to the
                                 * RCDATA or CDATA states Consume the next input
                                 * character.
                                 */
                                if (contentModelElement != null) {
                                    /*
                                     * If it is a U+002F SOLIDUS (/) character,
                                     * switch to the close tag open state.
                                     */
                                    index = 0;
                                    clearStrBufForNextState();
                                    state = Tokenizer.CLOSE_TAG_OPEN_NOT_PCDATA;
                                    continue stateloop;
                                } // else fall through
                            default:
                                /*
                                 * Otherwise, emit a U+003C LESS-THAN SIGN
                                 * character token
                                 */
                                tokenHandler.characters(Tokenizer.LT_GT, 0, 1);
                                /*
                                 * and reconsume the current input character in
                                 * the data state.
                                 */
                                cstart = pos;
                                state = returnState;
                                reconsume = true;
                                continue stateloop;
                        }
                    }
                    // WARNING FALLTHRU CASE TRANSITION: DON'T REORDER
                case ESCAPE_EXCLAMATION:
                    escapeexclamationloop: for (;;) {
                        if (++pos == endPos) {
                            break stateloop;
                        }
                        c = checkChar(buf, pos);
                        switch (c) {
                            case '-':
                                state = Tokenizer.ESCAPE_EXCLAMATION_HYPHEN;
                                break escapeexclamationloop; // FALL THRU
                            // continue
                            // stateloop;
                            default:
                                state = returnState;
                                reconsume = true;
                                continue stateloop;
                        }
                    }
                    // WARNING FALLTHRU CASE TRANSITION: DON'T REORDER
                case ESCAPE_EXCLAMATION_HYPHEN:
                    escapeexclamationhyphenloop: for (;;) {
                        if (++pos == endPos) {
                            break stateloop;
                        }
                        c = checkChar(buf, pos);
                        switch (c) {
                            case '-':
                                state = Tokenizer.ESCAPE_HYPHEN_HYPHEN;
                                break escapeexclamationhyphenloop;
                            // continue stateloop;
                            default:
                                state = returnState;
                                reconsume = true;
                                continue stateloop;
                        }
                    }
                    // WARNING FALLTHRU CASE TRANSITION: DON'T REORDER
                case ESCAPE_HYPHEN_HYPHEN:
                    escapehyphenhyphenloop: for (;;) {
                        if (++pos == endPos) {
                            break stateloop;
                        }
                        c = checkChar(buf, pos);
                        switch (c) {
                            case '-':
                                continue;
                            case '>':
                                state = returnState;
                                continue stateloop;
                            case '\u0000':
                                emitReplacementCharacter(buf, pos);
                                state = Tokenizer.ESCAPE;
                                break escapehyphenhyphenloop;
                            case '\r':
                                emitCarriageReturn(buf, pos);
                                state = Tokenizer.ESCAPE;
                                break stateloop;
                            case '\n':
                                silentLineFeed();
                            default:
                                state = Tokenizer.ESCAPE;
                                break escapehyphenhyphenloop;
                            // continue stateloop;
                        }
                    }
                    // WARNING FALLTHRU CASE TRANSITION: DON'T REORDER
                case ESCAPE:
                    escapeloop: for (;;) {
                        if (++pos == endPos) {
                            break stateloop;
                        }
                        c = checkChar(buf, pos);
                        switch (c) {
                            case '-':
                                state = Tokenizer.ESCAPE_HYPHEN;
                                break escapeloop; // FALL THRU continue
                            // stateloop;
                            case '\u0000':
                                emitReplacementCharacter(buf, pos);
                                continue;
                            case '\r':
                                emitCarriageReturn(buf, pos);
                                break stateloop;
                            case '\n':
                                silentLineFeed();
                            default:
                                continue;
                        }
                    }
                    // WARNING FALLTHRU CASE TRANSITION: DON'T REORDER
                case ESCAPE_HYPHEN:
                    escapehyphenloop: for (;;) {
                        if (++pos == endPos) {
                            break stateloop;
                        }
                        c = checkChar(buf, pos);
                        switch (c) {
                            case '-':
                                state = Tokenizer.ESCAPE_HYPHEN_HYPHEN;
                                continue stateloop;
                            case '\u0000':
                                emitReplacementCharacter(buf, pos);
                                state = Tokenizer.ESCAPE;
                                continue stateloop;
                            case '\r':
                                emitCarriageReturn(buf, pos);
                                state = Tokenizer.ESCAPE;
                                continue stateloop;
                            case '\n':
                                silentLineFeed();
                            default:
                                state = Tokenizer.ESCAPE;
                                continue stateloop;
                        }
                    }
                    // XXX reorder point
                case CLOSE_TAG_OPEN_NOT_PCDATA:
                    for (;;) {
                        if (++pos == endPos) {
                            break stateloop;
                        }
                        c = checkChar(buf, pos);
                        // ASSERT! when entering this state, set index to 0 and
                        // call clearStrBuf()
                        // assert (contentModelElement != null);
                        /*
                         * If the content model flag is set to the RCDATA or
                         * CDATA states but no start tag token has ever been
                         * emitted by this instance of the tokeniser (fragment
                         * case), or, if the content model flag is set to the
                         * RCDATA or CDATA states and the next few characters do
                         * not match the tag name of the last start tag token
                         * emitted (compared in an ASCII case-insensitive
                         * manner), or if they do but they are not immediately
                         * followed by one of the following characters: + U+0009
                         * CHARACTER TABULATION + U+000A LINE FEED (LF) + +
                         * U+000C FORM FEED (FF) + U+0020 SPACE + U+003E
                         * GREATER-THAN SIGN (>) + U+002F SOLIDUS (/) + EOF
                         * 
                         * ...then emit a U+003C LESS-THAN SIGN character token,
                         * a U+002F SOLIDUS character token, and switch to the
                         * data state to process the next input character.
                         */
                        // Let's implement the above without lookahead. strBuf
                        // holds
                        // characters that need to be emitted if looking for an
                        // end tag
                        // fails.
                        // Duplicating the relevant part of tag name state here
                        // as well.
                        if (index < contentModelElementNameAsArray.length) {
                            char e = contentModelElementNameAsArray[index];
                            char folded = c;
                            if (c >= 'A' && c <= 'Z') {
                                folded += 0x20;
                            }
                            if (folded != e) {
                                // [NOCPP[
                                errHtml4LtSlashInRcdata(folded);
                                // ]NOCPP]
                                tokenHandler.characters(Tokenizer.LT_SOLIDUS,
                                        0, 2);
                                emitStrBuf();
                                cstart = pos;
                                state = returnState;
                                reconsume = true;
                                continue stateloop;
                            }
                            appendStrBuf(c);
                            index++;
                            continue;
                        } else {
                            endTag = true;
                            // XXX replace contentModelElement with different
                            // type
                            tagName = contentModelElement;
                            switch (c) {
                                case '\r':
                                    silentCarriageReturn();
                                    state = Tokenizer.BEFORE_ATTRIBUTE_NAME;
                                    break stateloop;
                                case '\n':
                                    silentLineFeed();
                                    // fall thru
                                case ' ':
                                case '\t':
                                case '\u000C':
                                    /*
                                     * U+0009 CHARACTER TABULATION U+000A LINE
                                     * FEED (LF) U+000C FORM FEED (FF) U+0020
                                     * SPACE Switch to the before attribute name
                                     * state.
                                     */
                                    state = Tokenizer.BEFORE_ATTRIBUTE_NAME;
                                    continue stateloop;
                                case '>':
                                    /*
                                     * U+003E GREATER-THAN SIGN (>) Emit the
                                     * current tag token.
                                     */
                                    state = emitCurrentTagToken(false, pos);
                                    if (shouldSuspend) {
                                        break stateloop;
                                    }
                                    /*
                                     * Switch to the data state.
                                     */
                                    continue stateloop;
                                case '/':
                                    /*
                                     * U+002F SOLIDUS (/) Switch to the
                                     * self-closing start tag state.
                                     */
                                    state = Tokenizer.SELF_CLOSING_START_TAG;
                                    continue stateloop;                  
                                default:
                                    // [NOCPP[
                                    errWarnLtSlashInRcdata();
                                    // ]NOCPP]
                                    tokenHandler.characters(
                                            Tokenizer.LT_SOLIDUS, 0, 2);
                                    emitStrBuf();
                                    if (c == '\u0000') {
                                        emitReplacementCharacter(buf, pos);                                        
                                    } else {
                                        cstart = pos; // don't drop the character
                                    }
                                    state = returnState;
                                    continue stateloop;
                            }
                        }
                    }
                    // XXX reorder point
                case CLOSE_TAG_OPEN_PCDATA:
                    if (++pos == endPos) {
                        break stateloop;
                    }
                    c = checkChar(buf, pos);
                    /*
                     * Otherwise, if the content model flag is set to the PCDATA
                     * state, or if the next few characters do match that tag
                     * name, consume the next input character:
                     */
                    switch (c) {
                        case '>':
                            /* U+003E GREATER-THAN SIGN (>) Parse error. */
                            errLtSlashGt();
                            /*
                             * Switch to the data state.
                             */
                            cstart = pos + 1;
                            state = Tokenizer.DATA;
                            continue stateloop;
                        case '\r':
                            silentCarriageReturn();
                            /* Anything else Parse error. */
                            errGarbageAfterLtSlash();
                            /*
                             * Switch to the bogus comment state.
                             */
                            clearLongStrBufAndAppendToComment('\n');
                            state = Tokenizer.BOGUS_COMMENT;
                            break stateloop;
                        case '\n':
                            silentLineFeed();
                            /* Anything else Parse error. */
                            errGarbageAfterLtSlash();
                            /*
                             * Switch to the bogus comment state.
                             */
                            clearLongStrBufAndAppendToComment('\n');
                            state = Tokenizer.BOGUS_COMMENT;
                            continue stateloop;
                        case '\u0000':
                            c = '\uFFFD';
                            // fall thru
                        default:
                            if (c >= 'A' && c <= 'Z') {
                                c += 0x20;
                            }
                            if (c >= 'a' && c <= 'z') {
                                /*
                                 * U+0061 LATIN SMALL LETTER A through to U+007A
                                 * LATIN SMALL LETTER Z Create a new end tag
                                 * token,
                                 */
                                endTag = true;
                                /*
                                 * set its tag name to the input character,
                                 */
                                clearStrBufAndAppendCurrentC(c);
                                /*
                                 * then switch to the tag name state. (Don't
                                 * emit the token yet; further details will be
                                 * filled in before it is emitted.)
                                 */
                                state = Tokenizer.TAG_NAME;
                                continue stateloop;
                            } else {
                                /* Anything else Parse error. */
                                errGarbageAfterLtSlash();
                                /*
                                 * Switch to the bogus comment state.
                                 */
                                clearLongStrBufAndAppendToComment(c);
                                state = Tokenizer.BOGUS_COMMENT;
                                continue stateloop;
                            }
                    }
                    // XXX reorder point
                case RCDATA:
                    rcdataloop: for (;;) {
                        if (reconsume) {
                            reconsume = false;
                        } else {
                            if (++pos == endPos) {
                                break stateloop;
                            }
                            c = checkChar(buf, pos);
                        }
                        switch (c) {
                            case '&':
                                /*
                                 * U+0026 AMPERSAND (&) When the content model
                                 * flag is set to one of the PCDATA or RCDATA
                                 * states and the escape flag is false: switch
                                 * to the character reference data state.
                                 * Otherwise: treat it as per the "anything
                                 * else" entry below.
                                 */
                                flushChars(buf, pos);
                                clearStrBufAndAppendCurrentC(c);
                                additional = '\u0000';
                                returnState = state;
                                state = Tokenizer.CONSUME_CHARACTER_REFERENCE;
                                continue stateloop;
                            case '<':
                                /*
                                 * U+003C LESS-THAN SIGN (<) When the content
                                 * model flag is set to the PCDATA state: switch
                                 * to the tag open state. When the content model
                                 * flag is set to either the RCDATA state or the
                                 * CDATA state, and the escape flag is false:
                                 * switch to the tag open state. Otherwise:
                                 * treat it as per the "anything else" entry
                                 * below.
                                 */
                                flushChars(buf, pos);

                                returnState = state;
                                state = Tokenizer.TAG_OPEN_NON_PCDATA;
                                continue stateloop;
                            case '\u0000':
                                emitReplacementCharacter(buf, pos);
                                continue;
                            case '\r':
                                emitCarriageReturn(buf, pos);
                                break stateloop;
                            case '\n':
                                silentLineFeed();
                            default:
                                /*
                                 * Anything else Emit the input character as a
                                 * character token.
                                 */
                                /*
                                 * Stay in the data state.
                                 */
                                continue;
                        }
                    }

            }
        }
        flushChars(buf, pos);
        if (prevCR && pos != endPos) {
            // why is this needed?
            pos--;
            col--;
        }
        // Save locals
        stateSave = state;
        returnStateSave = returnState;
        return pos;
    }

    @Inline private void initDoctypeFields() {
        doctypeName = "";
        systemIdentifier = null;
        publicIdentifier = null;
        forceQuirks = false;
    }

    @Inline private void adjustDoubleHyphenAndAppendToLongStrBufCarriageReturn() throws SAXException {
        silentCarriageReturn();
        adjustDoubleHyphenAndAppendToLongStrBuf('\n');
    }

    @Inline private void adjustDoubleHyphenAndAppendToLongStrBufLineFeed() throws SAXException {
        silentLineFeed();
        adjustDoubleHyphenAndAppendToLongStrBuf('\n');
    }

    @Inline private void appendLongStrBufLineFeed() {
        silentLineFeed();
        appendLongStrBuf('\n');
    }

    @Inline private void appendLongStrBufCarriageReturn() {
        silentCarriageReturn();
        appendLongStrBuf('\n');
    }

    @Inline private void silentCarriageReturn() {
        nextCharOnNewLine = true;
        prevCR = true;
    }

    @Inline private void silentLineFeed() {
        nextCharOnNewLine = true;
    }

    private void emitCarriageReturn(char[] buf, int pos) throws SAXException {
        silentCarriageReturn();
        flushChars(buf, pos);
        tokenHandler.characters(Tokenizer.LF, 0, 1);
        cstart = Integer.MAX_VALUE;
    }

    private void emitReplacementCharacter(char[] buf, int pos) throws SAXException {
        silentCarriageReturn();
        flushChars(buf, pos);
        tokenHandler.characters(Tokenizer.REPLACEMENT_CHARACTER, 0, 1);
        cstart = Integer.MAX_VALUE;
    }
    
    private void rememberAmpersandLocation(char add) {
        additional = add;
        // [NOCPP[
        ampersandLocation = new LocatorImpl(this);
        // ]NOCPP]
    }

    private void bogusDoctype() throws SAXException {
        errBogusDoctype();
        forceQuirks = true;
    }

    private void bogusDoctypeWithoutQuirks() throws SAXException {
        errBogusDoctype();
        forceQuirks = false;
    }

    private void emitOrAppendStrBuf(int returnState) throws SAXException {
        if ((returnState & (~1)) != 0) {
            appendStrBufToLongStrBuf();
        } else {
            emitStrBuf();
        }
    }

    private void handleNcrValue(int returnState) throws SAXException {
        /*
         * If one or more characters match the range, then take them all and
         * interpret the string of characters as a number (either hexadecimal or
         * decimal as appropriate).
         */
        if (value >= 0x80 && value <= 0x9f) {
            /*
             * If that number is one of the numbers in the first column of the
             * following table, then this is a parse error.
             */
            errNcrInC1Range();
            /*
             * Find the row with that number in the first column, and return a
             * character token for the Unicode character given in the second
             * column of that row.
             */
            @NoLength char[] val = NamedCharacters.WINDOWS_1252[value - 0x80];
            emitOrAppendOne(val, returnState);
        } else if (value == 0x0D) {
            errRcnCr();
            emitOrAppendOne(Tokenizer.LF, returnState);
            // [NOCPP[
        } else if (value == 0xC
                && contentSpacePolicy != XmlViolationPolicy.ALLOW) {
            if (contentSpacePolicy == XmlViolationPolicy.ALTER_INFOSET) {
                emitOrAppendOne(Tokenizer.SPACE, returnState);
            } else if (contentSpacePolicy == XmlViolationPolicy.FATAL) {
                fatal("A character reference expanded to a form feed which is not legal XML 1.0 white space.");
            }
            // ]NOCPP]
        } else if ((value >= 0x0000 && value <= 0x0008) || (value == 0x000B)
                || (value >= 0x000E && value <= 0x001F) || value == 0x007F) {
            /*
             * Otherwise, if the number is in the range 0x0000 to 0x0008, 0x000E
             * to 0x001F, 0x007F to 0x009F, 0xD800 to 0xDFFF, 0xFDD0 to 0xFDEF,
             * or is one of 0x000B, 0xFFFE, 0xFFFF, 0x1FFFE, 0x1FFFF, 0x2FFFE,
             * 0x2FFFF, 0x3FFFE, 0x3FFFF, 0x4FFFE, 0x4FFFF, 0x5FFFE, 0x5FFFF,
             * 0x6FFFE, 0x6FFFF, 0x7FFFE, 0x7FFFF, 0x8FFFE, 0x8FFFF, 0x9FFFE,
             * 0x9FFFF, 0xAFFFE, 0xAFFFF, 0xBFFFE, 0xBFFFF, 0xCFFFE, 0xCFFFF,
             * 0xDFFFE, 0xDFFFF, 0xEFFFE, 0xEFFFF, 0xFFFFE, 0xFFFFF, 0x10FFFE,
             * or 0x10FFFF, or is higher than 0x10FFFF, then this is a parse
             * error; return a character token for the U+FFFD REPLACEMENT
             * CHARACTER character instead.
             */
            errNcrControlChar();
            emitOrAppendOne(Tokenizer.REPLACEMENT_CHARACTER, returnState);
        } else if ((value & 0xF800) == 0xD800) {
            errNcrSurrogate();
            emitOrAppendOne(Tokenizer.REPLACEMENT_CHARACTER, returnState);
        } else if (isNonCharacter(value)) {
            errNcrNonCharacter();
            emitOrAppendOne(Tokenizer.REPLACEMENT_CHARACTER, returnState);
        } else if (value >= 0xFDD0 && value <= 0xFDEF) {
            errNcrUnassigned();
            emitOrAppendOne(Tokenizer.REPLACEMENT_CHARACTER, returnState);
        } else if (value <= 0xFFFF) {
            /*
             * Otherwise, return a character token for the Unicode character
             * whose code point is that number.
             */
            char ch = (char) value;
            // [NOCPP[
            maybeWarnPrivateUse(ch);
            // ]NOCPP]
            bmpChar[0] = ch;
            emitOrAppendOne(bmpChar, returnState);
        } else if (value <= 0x10FFFF) {
            // [NOCPP[
            maybeWarnPrivateUseAstral();
            // ]NOCPP]
            astralChar[0] = (char) (Tokenizer.LEAD_OFFSET + (value >> 10));
            astralChar[1] = (char) (0xDC00 + (value & 0x3FF));
            emitOrAppend(astralChar, returnState);
        } else {
            errNcrOutOfRange();
            emitOrAppendOne(Tokenizer.REPLACEMENT_CHARACTER, returnState);
        }
    }

    public void eof() throws SAXException {
        int state = stateSave;
        int returnState = returnStateSave;

        eofloop: for (;;) {
            switch (state) {
                case TAG_OPEN_NON_PCDATA:
                    /*
                     * Otherwise, emit a U+003C LESS-THAN SIGN character token
                     */
                    tokenHandler.characters(Tokenizer.LT_GT, 0, 1);
                    /*
                     * and reconsume the current input character in the data
                     * state.
                     */
                    break eofloop;
                case TAG_OPEN:
                    /*
                     * The behavior of this state depends on the content model
                     * flag.
                     */
                    /*
                     * Anything else Parse error.
                     */
                    errEofAfterLt();
                    /*
                     * Emit a U+003C LESS-THAN SIGN character token
                     */
                    tokenHandler.characters(Tokenizer.LT_GT, 0, 1);
                    /*
                     * and reconsume the current input character in the data
                     * state.
                     */
                    break eofloop;
                case CLOSE_TAG_OPEN_NOT_PCDATA:
                    if (index < contentModelElementNameAsArray.length) {
                        break eofloop;
                    } else {
                        errEofInEndTag();
                        /*
                         * Reconsume the EOF character in the data state.
                         */
                        break eofloop;
                    }
                case CLOSE_TAG_OPEN_PCDATA:
                    /* EOF Parse error. */
                    errEofAfterLt();
                    /*
                     * Emit a U+003C LESS-THAN SIGN character token and a U+002F
                     * SOLIDUS character token.
                     */
                    tokenHandler.characters(Tokenizer.LT_SOLIDUS, 0, 2);
                    /*
                     * Reconsume the EOF character in the data state.
                     */
                    break eofloop;
                case TAG_NAME:
                    /*
                     * EOF Parse error.
                     */
                    errEofInTagName();
                    /*
                     * Reconsume the EOF character in the data state.
                     */
                    break eofloop;
                case BEFORE_ATTRIBUTE_NAME:
                case AFTER_ATTRIBUTE_VALUE_QUOTED:
                case SELF_CLOSING_START_TAG:
                    /* EOF Parse error. */
                    errEofWithoutGt();
                    /*
                     * Reconsume the EOF character in the data state.
                     */
                    break eofloop;
                case ATTRIBUTE_NAME:
                    /*
                     * EOF Parse error.
                     */
                    errEofInAttributeName();
                    /*
                     * Reconsume the EOF character in the data state.
                     */
                    break eofloop;
                case AFTER_ATTRIBUTE_NAME:
                case BEFORE_ATTRIBUTE_VALUE:
                    /* EOF Parse error. */
                    errEofWithoutGt();
                    /*
                     * Reconsume the EOF character in the data state.
                     */
                    break eofloop;
                case ATTRIBUTE_VALUE_DOUBLE_QUOTED:
                case ATTRIBUTE_VALUE_SINGLE_QUOTED:
                case ATTRIBUTE_VALUE_UNQUOTED:
                    /* EOF Parse error. */
                    errEofInAttributeValue();
                    /*
                     * Reconsume the EOF character in the data state.
                     */
                    break eofloop;
                case BOGUS_COMMENT:
                    emitComment(0, 0);
                    break eofloop;
                case BOGUS_COMMENT_HYPHEN:
                    // [NOCPP[
                    maybeAppendSpaceToBogusComment();
                    // ]NOCPP]
                    emitComment(0, 0);
                    break eofloop;
                case MARKUP_DECLARATION_OPEN:
                    errBogusComment();
                    clearLongStrBuf();
                    emitComment(0, 0);
                    break eofloop;
                case MARKUP_DECLARATION_HYPHEN:
                    errBogusComment();
                    emitComment(0, 0);
                    break eofloop;
                case MARKUP_DECLARATION_OCTYPE:
                    if (index < 6) {
                        errBogusComment();
                        emitComment(0, 0);
                    } else {
                        /* EOF Parse error. */
                        errEofInDoctype();
                        /*
                         * Create a new DOCTYPE token. Set its force-quirks flag
                         * to on.
                         */
                        doctypeName = "";
                        publicIdentifier = null;
                        systemIdentifier = null;
                        forceQuirks = true;
                        /*
                         * Emit the token.
                         */
                        emitDoctypeToken(0);
                        /*
                         * Reconsume the EOF character in the data state.
                         */
                        break eofloop;
                    }
                    break eofloop;
                case COMMENT_START:
                case COMMENT:
                    /*
                     * EOF Parse error.
                     */
                    errEofInComment();
                    /* Emit the comment token. */
                    emitComment(0, 0);
                    /*
                     * Reconsume the EOF character in the data state.
                     */
                    break eofloop;
                case COMMENT_END:
                    errEofInComment();
                    /* Emit the comment token. */
                    emitComment(2, 0);
                    /*
                     * Reconsume the EOF character in the data state.
                     */
                    break eofloop;
                case COMMENT_END_DASH:
                case COMMENT_START_DASH:
                    errEofInComment();
                    /* Emit the comment token. */
                    emitComment(1, 0);
                    /*
                     * Reconsume the EOF character in the data state.
                     */
                    break eofloop;
                case DOCTYPE:
                case BEFORE_DOCTYPE_NAME:
                    errEofInDoctype();
                    /*
                     * Create a new DOCTYPE token. Set its force-quirks flag to
                     * on.
                     */
                    forceQuirks = true;
                    /*
                     * Emit the token.
                     */
                    emitDoctypeToken(0);
                    /*
                     * Reconsume the EOF character in the data state.
                     */
                    break eofloop;
                case DOCTYPE_NAME:
                    errEofInDoctype();
                    strBufToDoctypeName();
                    /*
                     * Set the DOCTYPE token's force-quirks flag to on.
                     */
                    forceQuirks = true;
                    /*
                     * Emit that DOCTYPE token.
                     */
                    emitDoctypeToken(0);
                    /*
                     * Reconsume the EOF character in the data state.
                     */
                    break eofloop;
                case DOCTYPE_UBLIC:
                case DOCTYPE_YSTEM:
                case AFTER_DOCTYPE_NAME:
                case BEFORE_DOCTYPE_PUBLIC_IDENTIFIER:
                    errEofInDoctype();
                    /*
                     * Set the DOCTYPE token's force-quirks flag to on.
                     */
                    forceQuirks = true;
                    /*
                     * Emit that DOCTYPE token.
                     */
                    emitDoctypeToken(0);
                    /*
                     * Reconsume the EOF character in the data state.
                     */
                    break eofloop;
                case DOCTYPE_PUBLIC_IDENTIFIER_DOUBLE_QUOTED:
                case DOCTYPE_PUBLIC_IDENTIFIER_SINGLE_QUOTED:
                    /* EOF Parse error. */
                    errEofInPublicId();
                    /*
                     * Set the DOCTYPE token's force-quirks flag to on.
                     */
                    forceQuirks = true;
                    /*
                     * Emit that DOCTYPE token.
                     */
                    publicIdentifier = longStrBufToString();
                    emitDoctypeToken(0);
                    /*
                     * Reconsume the EOF character in the data state.
                     */
                    break eofloop;
                case AFTER_DOCTYPE_PUBLIC_IDENTIFIER:
                case BEFORE_DOCTYPE_SYSTEM_IDENTIFIER:
                    errEofInDoctype();
                    /*
                     * Set the DOCTYPE token's force-quirks flag to on.
                     */
                    forceQuirks = true;
                    /*
                     * Emit that DOCTYPE token.
                     */
                    emitDoctypeToken(0);
                    /*
                     * Reconsume the EOF character in the data state.
                     */
                    break eofloop;
                case DOCTYPE_SYSTEM_IDENTIFIER_DOUBLE_QUOTED:
                case DOCTYPE_SYSTEM_IDENTIFIER_SINGLE_QUOTED:
                    /* EOF Parse error. */
                    errEofInSystemId();
                    /*
                     * Set the DOCTYPE token's force-quirks flag to on.
                     */
                    forceQuirks = true;
                    /*
                     * Emit that DOCTYPE token.
                     */
                    systemIdentifier = longStrBufToString();
                    emitDoctypeToken(0);
                    /*
                     * Reconsume the EOF character in the data state.
                     */
                    break eofloop;
                case AFTER_DOCTYPE_SYSTEM_IDENTIFIER:
                    errEofInDoctype();
                    /*
                     * Set the DOCTYPE token's force-quirks flag to on.
                     */
                    forceQuirks = true;
                    /*
                     * Emit that DOCTYPE token.
                     */
                    emitDoctypeToken(0);
                    /*
                     * Reconsume the EOF character in the data state.
                     */
                    break eofloop;
                case BOGUS_DOCTYPE:
                    /*
                     * Emit that DOCTYPE token.
                     */
                    emitDoctypeToken(0);
                    /*
                     * Reconsume the EOF character in the data state.
                     */
                    break eofloop;
                case CONSUME_CHARACTER_REFERENCE:
                    /*
                     * Unlike the definition is the spec, this state does not
                     * return a value and never requires the caller to
                     * backtrack. This state takes care of emitting characters
                     * or appending to the current attribute value. It also
                     * takes care of that in the case when consuming the entity
                     * fails.
                     */
                    /*
                     * This section defines how to consume an entity. This
                     * definition is used when parsing entities in text and in
                     * attributes.
                     * 
                     * The behavior depends on the identity of the next
                     * character (the one immediately after the U+0026 AMPERSAND
                     * character):
                     */

                    emitOrAppendStrBuf(returnState);
                    state = returnState;
                    continue;
                case CHARACTER_REFERENCE_LOOP:
                    outer: for (;;) {
                        char c = '\u0000';
                        entCol++;
                        /*
                         * Consume the maximum number of characters possible,
                         * with the consumed characters matching one of the
                         * identifiers in the first column of the named
                         * character references table (in a case-sensitive
                         * manner).
                         */
                        hiloop: for (;;) {
                            if (hi == -1) {
                                break hiloop;
                            }
                            if (entCol == NamedCharacters.NAMES[hi].length) {
                                break hiloop;
                            }
                            if (entCol > NamedCharacters.NAMES[hi].length) {
                                break outer;
                            } else if (c < NamedCharacters.NAMES[hi][entCol]) {
                                hi--;
                            } else {
                                break hiloop;
                            }
                        }

                        loloop: for (;;) {
                            if (hi < lo) {
                                break outer;
                            }
                            if (entCol == NamedCharacters.NAMES[lo].length) {
                                candidate = lo;
                                strBufMark = strBufLen;
                                lo++;
                            } else if (entCol > NamedCharacters.NAMES[lo].length) {
                                break outer;
                            } else if (c > NamedCharacters.NAMES[lo][entCol]) {
                                lo++;
                            } else {
                                break loloop;
                            }
                        }
                        if (hi < lo) {
                            break outer;
                        }
                        continue;
                    }

                    // TODO warn about apos (IE) and TRADE (Opera)
                    if (candidate == -1) {
                        /*
                         * If no match can be made, then this is a parse error.
                         */
                        errNoNamedCharacterMatch();
                        emitOrAppendStrBuf(returnState);
                        state = returnState;
                        continue eofloop;
                    } else {
                        char[] candidateArr = NamedCharacters.NAMES[candidate];
                        if (candidateArr[candidateArr.length - 1] != ';') {
                            /*
                             * If the last character matched is not a U+003B
                             * SEMICOLON (;), there is a parse error.
                             */
                            if ((returnState & (~1)) != 0) {
                                /*
                                 * If the entity is being consumed as part of an
                                 * attribute, and the last character matched is
                                 * not a U+003B SEMICOLON (;),
                                 */
                                char ch;
                                if (strBufMark == strBufLen) {
                                    ch = '\u0000';
                                } else {
                                    ch = strBuf[strBufMark];
                                }
                                if ((ch >= '0' && ch <= '9')
                                        || (ch >= 'A' && ch <= 'Z')
                                        || (ch >= 'a' && ch <= 'z')) {
                                    /*
                                     * and the next character is in the range
                                     * U+0030 DIGIT ZERO to U+0039 DIGIT NINE,
                                     * U+0041 LATIN CAPITAL LETTER A to U+005A
                                     * LATIN CAPITAL LETTER Z, or U+0061 LATIN
                                     * SMALL LETTER A to U+007A LATIN SMALL
                                     * LETTER Z, then, for historical reasons,
                                     * all the characters that were matched
                                     * after the U+0026 AMPERSAND (&) must be
                                     * unconsumed, and nothing is returned.
                                     */
                                    errNoNamedCharacterMatch();
                                    appendStrBufToLongStrBuf();
                                    state = returnState;
                                    continue eofloop;
                                }
                            }
                            if ((returnState & (~1)) != 0) {
                                errUnescapedAmpersandInterpretedAsCharacterReference();
                            } else {
                                errNotSemicolonTerminated();
                            }
                        }

                        /*
                         * Otherwise, return a character token for the character
                         * corresponding to the entity name (as given by the
                         * second column of the named character references
                         * table).
                         */
                        char[] val = NamedCharacters.VALUES[candidate];
                        emitOrAppend(val, returnState);
                        // this is so complicated!
                        if (strBufMark < strBufLen) {
                            if ((returnState & (~1)) != 0) {
                                for (int i = strBufMark; i < strBufLen; i++) {
                                    appendLongStrBuf(strBuf[i]);
                                }
                            } else {
                                tokenHandler.characters(strBuf, strBufMark,
                                        strBufLen - strBufMark);
                            }
                        }
                        state = returnState;
                        continue eofloop;
                        /*
                         * If the markup contains I'm &notit; I tell you, the
                         * entity is parsed as "not", as in, I'm ¬it; I tell
                         * you. But if the markup was I'm &notin; I tell you,
                         * the entity would be parsed as "notin;", resulting in
                         * I'm ∉ I tell you.
                         */
                    }
                case CONSUME_NCR:
                case DECIMAL_NRC_LOOP:
                case HEX_NCR_LOOP:
                    /*
                     * If no characters match the range, then don't consume any
                     * characters (and unconsume the U+0023 NUMBER SIGN
                     * character and, if appropriate, the X character). This is
                     * a parse error; nothing is returned.
                     * 
                     * Otherwise, if the next character is a U+003B SEMICOLON,
                     * consume that too. If it isn't, there is a parse error.
                     */
                    if (!seenDigits) {
                        errNoDigitsInNCR();
                        emitOrAppendStrBuf(returnState);
                        state = returnState;
                        continue;
                    } else {
                        errCharRefLacksSemicolon();
                    }
                    // WARNING previous state sets reconsume
                    handleNcrValue(returnState);
                    state = returnState;
                    continue;
                case DATA:
                default:
                    break eofloop;
            }
        }
        // case DATA:
        /*
         * EOF Emit an end-of-file token.
         */
        tokenHandler.eof();
        return;
    }

    private void emitDoctypeToken(int pos) throws SAXException {
        cstart = pos + 1;
        tokenHandler.doctype(doctypeName, publicIdentifier, systemIdentifier,
                forceQuirks);
        // It is OK and sufficient to release these here, since
        // there's no way out of the doctype states than through paths
        // that call this method.
        Portability.releaseLocal(doctypeName);
        Portability.releaseString(publicIdentifier);
        Portability.releaseString(systemIdentifier);
    }

    private char checkChar(@NoLength char[] buf, int pos) throws SAXException {
        advanceCol();
        char c = buf[pos];
        if (errorHandler == null
                && contentNonXmlCharPolicy == XmlViolationPolicy.ALLOW) {
            return c;
        }
        if (!confident && !alreadyComplainedAboutNonAscii && c > '\u007F') {
            complainAboutNonAscii();
            alreadyComplainedAboutNonAscii = true;
        }
        switch (c) {
            case '\u0000':
                err("Saw U+0000 in stream.");
            case '\t':
            case '\r':
            case '\n':
                break;
            case '\u000C':
                if (contentNonXmlCharPolicy == XmlViolationPolicy.FATAL) {
                    fatal("This document is not mappable to XML 1.0 without data loss due to "
                            + toUPlusString(c)
                            + " which is not a legal XML 1.0 character.");
                } else {
                    if (contentNonXmlCharPolicy == XmlViolationPolicy.ALTER_INFOSET) {
                        c = buf[pos] = ' ';
                    }
                    warn("This document is not mappable to XML 1.0 without data loss due to "
                            + toUPlusString(c)
                            + " which is not a legal XML 1.0 character.");
                }
                break;
            default:
                if ((c < ' ' || isNonCharacter(c))) {
                    switch (contentNonXmlCharPolicy) {
                        case FATAL:
                            fatal("Forbidden code point " + toUPlusString(c)
                                    + ".");
                            break;
                        case ALTER_INFOSET:
                            c = buf[pos] = '\uFFFD';
                            // fall through
                        case ALLOW:
                            err("Forbidden code point " + toUPlusString(c)
                                    + ".");
                    }
                } else if ((c >= '\u007F') && (c <= '\u009F')
                        || (c >= '\uFDD0') && (c <= '\uFDDF')) {
                    err("Forbidden code point " + toUPlusString(c) + ".");
                } else if (isPrivateUse(c)) {
                    warnAboutPrivateUseChar();
                }
        }
        return c;
    }

    private void advanceCol() {
        linePrev = line;
        colPrev = col;
        if (nextCharOnNewLine) {
            line++;
            col = 1;
            nextCharOnNewLine = false;
        } else {
            col++;
        }
    }

    // [NOCPP[

    private void complainAboutNonAscii() throws SAXException {
        String encoding = null;
        if (encodingDeclarationHandler != null) {
            encoding = encodingDeclarationHandler.getCharacterEncoding();
        }
        if (encoding == null) {
            err("The character encoding of the document was not explicit but the document contains non-ASCII.");
        } else {
            err("No explicit character encoding declaration has been seen yet (assumed \u201C"
                    + encoding + "\u201D) but the document contains non-ASCII.");
        }
    }

    /**
     * Returns the alreadyComplainedAboutNonAscii.
     * 
     * @return the alreadyComplainedAboutNonAscii
     */
    public boolean isAlreadyComplainedAboutNonAscii() {
        return alreadyComplainedAboutNonAscii;
    }

    // ]NOCPP]

    public void internalEncodingDeclaration(String internalCharset)
            throws SAXException {
        if (encodingDeclarationHandler != null) {
            encodingDeclarationHandler.internalEncodingDeclaration(internalCharset);
        }
    }

    /**
     * @param val
     * @throws SAXException
     */
    private void emitOrAppend(char[] val, int returnState) throws SAXException {
        if ((returnState & (~1)) != 0) {
            appendLongStrBuf(val);
        } else {
            tokenHandler.characters(val, 0, val.length);
        }
    }

    private void emitOrAppendOne(@NoLength char[] val, int returnState)
            throws SAXException {
        if ((returnState & (~1)) != 0) {
            appendLongStrBuf(val[0]);
        } else {
            tokenHandler.characters(val, 0, 1);
        }
    }

    public void end() throws SAXException {
        strBuf = null;
        longStrBuf = null;
        systemIdentifier = null;
        publicIdentifier = null;
        doctypeName = null;
        tagName = null;
        attributeName = null;
        tokenHandler.endTokenization();
        if (attributes != null) {
            attributes.clear(mappingLangToXmlLang);
            Portability.delete(attributes);
            attributes = null;
        }
    }

    public void requestSuspension() {
        shouldSuspend = true;
    }

    public void becomeConfident() {
        confident = true;
    }

    /**
     * Returns the nextCharOnNewLine.
     * 
     * @return the nextCharOnNewLine
     */
    public boolean isNextCharOnNewLine() {
        return nextCharOnNewLine;
    }

    public boolean isPrevCR() {
        return prevCR;
    }

    /**
     * Returns the line.
     * 
     * @return the line
     */
    public int getLine() {
        return line;
    }

    /**
     * Returns the col.
     * 
     * @return the col
     */
    public int getCol() {
        return col;
    }

    public boolean isInDataState() {
        return (stateSave == DATA);
    }
    

    private void errGarbageAfterLtSlash() throws SAXException {
        err("Garbage after \u201C</\u201D.");
    }

    private void errLtSlashGt() throws SAXException {
        err("Saw \u201C</>\u201D. Probable causes: Unescaped \u201C<\u201D (escape as \u201C&lt;\u201D) or mistyped end tag.");
    }

    private void errWarnLtSlashInRcdata() throws SAXException {
        if (html4) {
            err((stateSave == Tokenizer.DATA ? "CDATA" : "RCDATA")
                    + " element \u201C"
                    + contentModelElement
                    + "\u201D contained the string \u201C</\u201D, but it was not the start of the end tag. (HTML4-only error)");
        } else {
            warn((stateSave == Tokenizer.DATA ? "CDATA" : "RCDATA")
                    + " element \u201C"
                    + contentModelElement
                    + "\u201D contained the string \u201C</\u201D, but this did not close the element.");
        }
    }

    private void errHtml4LtSlashInRcdata(char folded) throws SAXException {
        if (html4 && (index > 0 || (folded >= 'a' && folded <= 'z'))
                && ElementName.IFRAME != contentModelElement) {
            err((stateSave == Tokenizer.DATA ? "CDATA" : "RCDATA")
                    + " element \u201C"
                    + contentModelElement.name
                    + "\u201D contained the string \u201C</\u201D, but it was not the start of the end tag. (HTML4-only error)");
        }
    }

    private void errCharRefLacksSemicolon() throws SAXException {
        err("Character reference was not terminated by a semicolon.");
    }

    private void errNoDigitsInNCR() throws SAXException {
        err("No digits after \u201C" + strBufToString() + "\u201D.");
    }

    private void errGtInSystemId() throws SAXException {
        err("\u201C>\u201D in system identifier.");
    }

    private void errGtInPublicId() throws SAXException {
        err("\u201C>\u201D in public identifier.");
    }

    private void errNamelessDoctype() throws SAXException {
        err("Nameless doctype.");
    }

    private void errConsecutiveHyphens() throws SAXException {
        err("Consecutive hyphens did not terminate a comment. \u201C--\u201D is not permitted inside a comment, but e.g. \u201C- -\u201D is.");
    }

    private void errPrematureEndOfComment() throws SAXException {
        err("Premature end of comment. Use \u201C-->\u201D to end a comment properly.");
    }

    private void errBogusComment() throws SAXException {
        err("Bogus comment.");
    }

    private void errWarnUnquotedAttributeValOrNull(char c) throws SAXException {
        if (c == '<') {
            warn("\u201C<\u201D in an unquoted attribute value. This does not end the tag. Probable cause: Missing \u201C>\u201D immediately before.");
        } else {
            err("\u201C"
                    + c
                    + "\u201D in an unquoted attribute value. Probable causes: Attributes running together or a URL query string in an unquoted attribute value.");
        }
    }

    private void errSlashNotFollowedByGt() throws SAXException {
        err("A slash was not immediate followed by \u201C>\u201D.");
    }

    private void errHtml4XmlVoidSyntax() throws SAXException {
        if (html4) {
            err("The \u201C/>\u201D syntax on void elements is not allowed.  (This is an HTML4-only error.)");
        }
    }

    private void errNoSpaceBetweenAttributes() throws SAXException {
        err("No space between attributes.");
    }

    private void errHtml4NonNameInUnquotedAttribute(char c) throws SAXException {
        if (html4
                && !((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')
                        || (c >= '0' && c <= '9') || c == '.' || c == '-'
                        || c == '_' || c == ':')) {
            err("Non-name character in an unquoted attribute value. (This is an HTML4-only error.)");
        }
    }

    private void errEqualsInUnquotedAttributeOrNull(char c) throws SAXException {
        err("\u201C=\u201D in an unquoted attribute value. Probable cause: Stray duplicate equals sign.");
    }

    private void errAttributeValueMissing() throws SAXException {
        err("Attribute value missing.");
    }

    private void errBadCharBeforeAttributeNameOrNull(char c)
            throws SAXException {
        if (c == '=') {
            errEqualsSignBeforeAttributeName();
        } else {
            errQuoteBeforeAttributeName(c);
        }
    }

    private void errEqualsSignBeforeAttributeName() throws SAXException {
        err("Saw \u201C=\u201D when expecting an attribute name. Probable cause: Attribute name missing.");
    }

    private void errBadCharAfterLt(char c) throws SAXException {
        err("Bad character \u201C"
                + c
                + "\u201D after \u201C<\u201D. Probable cause: Unescaped \u201C<\u201D. Try escaping it as \u201C&lt;\u201D.");
    }

    private void errLtGt() throws SAXException {
        err("Saw \u201C<>\u201D. Probable causes: Unescaped \u201C<\u201D (escape as \u201C&lt;\u201D) or mistyped start tag.");
    }

    private void errProcessingInstruction() throws SAXException {
        err("Saw \u201C<?\u201D. Probable cause: Attempt to use an XML processing instruction in HTML. (XML processing instructions are not supported in HTML.)");
    }

    private void errUnescapedAmpersandInterpretedAsCharacterReference()
            throws SAXException {
        if (errorHandler == null) {
            return;
        }
        SAXParseException spe = new SAXParseException(
                "The string following \u201C&\u201D was interpreted as a character reference. (\u201C&\u201D probably should have been escaped as \u201C&amp;\u201D.)",
                ampersandLocation);
        errorHandler.error(spe);
    }

    private void errNotSemicolonTerminated() throws SAXException {
        err("Named character reference was not terminated by a semicolon. (Or \u201C&\u201D should have been escaped as \u201C&amp;\u201D.)");
    }

    private void errNoNamedCharacterMatch() throws SAXException {
        if (errorHandler == null) {
            return;
        }
        SAXParseException spe = new SAXParseException(
                "\u201C&\u201D did not start a character reference. (\u201C&\u201D probably should have been escaped as \u201C&amp;\u201D.)",
                ampersandLocation);
        errorHandler.error(spe);
    }

    private void errQuoteBeforeAttributeName(char c) throws SAXException {
        err("Saw \u201C"
                + c
                + "\u201D when expecting an attribute name. Probable cause: \u201C=\u201D missing immediately before.");
    }

    private void errQuoteInAttributeNameOrNull(char c) throws SAXException {
        err("Quote \u201C"
                + c
                + "\u201D in attribute name. Probable cause: Matching quote missing somewhere earlier.");
    }
    
    private void errExpectedPublicId() throws SAXException {
        err("Expected a public identifier but the doctype ended.");
    }

    private void errBogusDoctype() throws SAXException {
        err("Bogus doctype.");
    }

    private void maybeWarnPrivateUseAstral() throws SAXException {
        if (errorHandler != null && isAstralPrivateUse(value)) {
            warnAboutPrivateUseChar();
        }
    }

    private void maybeWarnPrivateUse(char ch) throws SAXException {
        if (errorHandler != null && isPrivateUse(ch)) {
            warnAboutPrivateUseChar();
        }
    }

    private void maybeErrAttributesOnEndTag(HtmlAttributes attrs)
            throws SAXException {
        if (attrs.getLength() != 0) {
            /*
             * When an end tag token is emitted with attributes, that is a parse
             * error.
             */
            err("End tag had attributes.");
        }
    }

    private void maybeErrSlashInEndTag(boolean selfClosing) throws SAXException {
        if (selfClosing && endTag) {
            err("Stray \u201C/\u201D at the end of an end tag.");
        }
    }

    private void errNcrNonCharacter() throws SAXException {
        err("Character reference expands to a non-character.");
    }

    private void errNcrSurrogate() throws SAXException {
        err("Character reference expands to a surrogate.");
    }

    private void errNcrControlChar() throws SAXException {
        err("Character reference expands to a control character ("
                + toUPlusString((char) value) + ").");
    }

    private void errRcnCr() throws SAXException {
        err("A numeric character reference expanded to carriage return.");
    }

    private void errNcrInC1Range() throws SAXException {
        err("A numeric character reference expanded to the C1 controls range.");
    }

    private void errEofInPublicId() throws SAXException {
        err("End of file inside public identifier.");
    }

    private void errEofInComment() throws SAXException {
        err("End of file inside comment.");
    }

    private void errEofInDoctype() throws SAXException {
        err("End of file inside doctype.");
    }

    private void errEofInAttributeValue() throws SAXException {
        err("End of file reached when inside an attribute value. Ignoring tag.");
    }

    private void errEofInAttributeName() throws SAXException {
        err("End of file occurred in an attribute name. Ignoring tag.");
    }

    private void errEofWithoutGt() throws SAXException {
        err("Saw end of file without the previous tag ending with \u201C>\u201D. Ignoring tag.");
    }

    private void errEofInTagName() throws SAXException {
        err("End of file seen when looking for tag name. Ignoring tag.");
    }

    private void errEofInEndTag() throws SAXException {
        err("End of file inside end tag. Ignoring tag.");
    }

    private void errEofAfterLt() throws SAXException {
        err("End of file after \u201C<\u201D.");
    }

    private void errNcrOutOfRange() throws SAXException {
        err("Character reference outside the permissible Unicode range.");
    }

    private void errNcrUnassigned() throws SAXException {
        err("Character reference expands to a permanently unassigned code point.");
    }
    
    private void errDuplicateAttribute() throws SAXException {
        err("Duplicate attribute \u201C"
                + attributeName.getLocal(AttributeName.HTML) + "\u201D.");
    }
    
    private void errEofInSystemId() throws SAXException {
        err("End of file inside system identifier.");
    }
    
    private void errExpectedSystemId() throws SAXException {
        err("Expected a system identifier but the doctype ended.");
    }

    private void errMissingSpaceBeforeDoctypeName() throws SAXException {
        err("Missing space before doctype name.");
    }

}
