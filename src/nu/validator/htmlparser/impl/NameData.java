/*
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

package nu.validator.htmlparser.impl;

import java.util.HashSet;
import java.util.Set;

public class NameData {
    
    public static final NameData IMG = new NameData("img", TreeBuilder.EMBED_OR_IMG, true, false, false);
    public static final NameData HEAD = new NameData("head", TreeBuilder.HEAD, true, false, false);
    public static final NameData HTML = new NameData("html", TreeBuilder.HTML, false, true, false);
    public static final NameData FORM = new NameData("form", TreeBuilder.FORM, true, false, false);
    public static final NameData BODY = new NameData("body", TreeBuilder.BODY, true, false, false);
    public static final NameData P = new NameData("p", TreeBuilder.P, true, false, false);
    public static final NameData TR = new NameData("tr", TreeBuilder.TR, true, false, true);
    public static final NameData COLGROUP = new NameData("colgroup", TreeBuilder.COLGROUP, true, false, false);
    public static final NameData TBODY = new NameData("tbody", TreeBuilder.TBODY_OR_THEAD_OR_TFOOT, true, false, true);
    public static final NameData LABEL = new NameData("label", TreeBuilder.OTHER, false, false, false);
    public final String name;   
    public final String camelCaseName;
    public final int magic;
    public final boolean special;
    public final boolean scoping;
    public final boolean fosterParenting;
    public final boolean custom;
    
    private NameData(String camelCaseName, int magic, boolean special, boolean scoping, boolean fosterParenting) {
        this.name = camelCaseName.toLowerCase().intern();
        this.camelCaseName = camelCaseName.intern();
        this.magic = magic;
        this.special = special;
        this.scoping = scoping;
        this.fosterParenting = fosterParenting;
        this.custom = false;
    }
    
    NameData(String name) {
        this.name = name;
        this.camelCaseName = name;
        this.magic = TreeBuilder.OTHER;
        this.special = false;
        this.scoping = false;
        this.fosterParenting = false;
        this.custom = true;
    }
    
    static Set<NameData> buildSet() {
        Set<NameData> set = new HashSet<NameData>();
        set.add(new NameData("a", TreeBuilder.A, false, false, false));
        set.add(new NameData("abbr", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("abs", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("acronym", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("address", TreeBuilder.FIELDSET_OR_ADDRESS_OR_DIR, true, false, false));
        set.add(new NameData("altGlyph", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("altGlyphDef", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("altGlyphItem", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("and", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("animate", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("animateColor", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("animateMotion", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("animateTransform", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("animation", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("annotation", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("annotation-xml", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("applet", TreeBuilder.OBJECT_OR_MARQUEE_OR_APPLET, false, true, false));
        set.add(new NameData("apply", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("approx", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("arccos", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("arccosh", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("arccot", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("arccoth", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("arccsc", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("arccsch", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("arcsec", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("arcsech", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("arcsin", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("arcsinh", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("arctan", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("arctanh", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("area", TreeBuilder.AREA_OR_BASEFONT_OR_BGSOUND_OR_PARAM_OR_SPACER_OR_WBR, true, false, false));
        set.add(new NameData("arg", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("article", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("aside", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("audio", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("b", TreeBuilder.B_OR_BIG_OR_EM_OR_FONT_OR_I_OR_S_OR_SMALL_OR_STRIKE_OR_STRONG_OR_TT_OR_U, false, false, false));
        set.add(new NameData("base", TreeBuilder.BASE, true, false, false));
        set.add(new NameData("basefont", TreeBuilder.AREA_OR_BASEFONT_OR_BGSOUND_OR_PARAM_OR_SPACER_OR_WBR, true, false, false));
        set.add(new NameData("bgsound", TreeBuilder.AREA_OR_BASEFONT_OR_BGSOUND_OR_PARAM_OR_SPACER_OR_WBR, true, false, false));
        set.add(new NameData("bdo", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("big", TreeBuilder.B_OR_BIG_OR_EM_OR_FONT_OR_I_OR_S_OR_SMALL_OR_STRIKE_OR_STRONG_OR_TT_OR_U, false, false, false));
        set.add(new NameData("blockquote", TreeBuilder.DIV_OR_BLOCKQUOTE_OR_CENTER_OR_MENU, true, false, false));
        set.add(BODY);
        set.add(new NameData("br", TreeBuilder.BR, true, false, false));
        set.add(new NameData("button", TreeBuilder.BUTTON, false, true, false));
        set.add(new NameData("bvar", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("canvas", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("caption", TreeBuilder.CAPTION, false, true, false));
        set.add(new NameData("card", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("cartesianproduct", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("ceiling", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("center", TreeBuilder.DIV_OR_BLOCKQUOTE_OR_CENTER_OR_MENU, true, false, false));
        set.add(new NameData("ci", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("circle", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("cite", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("clipPath", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("cn", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("code", TreeBuilder.CODE_OR_RUBY_OR_SPAN_OR_SUB_OR_SUP_OR_VAR, false, false, false));
        set.add(new NameData("codomain", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("col", TreeBuilder.COL, true, false, false));
        set.add(COLGROUP);
        set.add(new NameData("color-profile", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("command", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("complexes", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("compose", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("condition", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("conjugate", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("cos", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("cosh", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("cot", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("coth", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("csc", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("csch", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("csymbol", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("curl", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("cursor", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("datagrid", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("datatemplate", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("dd", TreeBuilder.DD_OR_DT, true, false, false));
        set.add(new NameData("declare", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("definition-src", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("defs", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("degree", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("del", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("desc", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("details", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("determinant", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("dfn", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("dialog", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("diff", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("dir", TreeBuilder.FIELDSET_OR_ADDRESS_OR_DIR, true, false, false));
        set.add(new NameData("discard", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("div", TreeBuilder.DIV_OR_BLOCKQUOTE_OR_CENTER_OR_MENU, true, false, false));
        set.add(new NameData("divergence", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("divide", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("dl", TreeBuilder.UL_OR_OL_OR_DL, true, false, false));
        set.add(new NameData("domain", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("domainofapplication", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("dt", TreeBuilder.DD_OR_DT, true, false, false));
        set.add(new NameData("ellipse", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("em", TreeBuilder.B_OR_BIG_OR_EM_OR_FONT_OR_I_OR_S_OR_SMALL_OR_STRIKE_OR_STRONG_OR_TT_OR_U, false, false, false));
        set.add(new NameData("embed", TreeBuilder.EMBED_OR_IMG, true, false, false));
        set.add(new NameData("emptyset", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("eq", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("equivalent", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("eulergamma", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("event-source", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("exists", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("exp", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("exponentiale", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("factorial", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("factorof", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("false", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("feBlend", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("feColorMatrix", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("feComponentTransfer", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("feComposite", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("feConvolveMatrix", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("feDiffuseLighting", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("feDisplacementMap", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("feDistantLight", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("feFlood", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("feFuncA", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("feFuncB", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("feFuncG", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("feFuncR", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("feGaussianBlur", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("feImage", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("feMerge", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("feMergeNode", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("feMorphology", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("feOffset", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("fePointLight", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("feSpecularLighting", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("feSpotLight", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("feTile", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("feTurbulence", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("fieldset", TreeBuilder.FIELDSET_OR_ADDRESS_OR_DIR, true, false, false));
        set.add(new NameData("figure", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("filter", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("floor", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("fn", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("font", TreeBuilder.B_OR_BIG_OR_EM_OR_FONT_OR_I_OR_S_OR_SMALL_OR_STRIKE_OR_STRONG_OR_TT_OR_U, false, false, false));
        set.add(new NameData("font-face", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("font-face-format", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("font-face-name", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("font-face-src", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("font-face-uri", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("footer", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("forall", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("foreignObject", TreeBuilder.OTHER, false, false, false));
        set.add(FORM);
        set.add(new NameData("frame", TreeBuilder.FRAME, true, false, false));
        set.add(new NameData("frameset", TreeBuilder.FRAMESET, true, false, false));
        set.add(new NameData("g", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("gcd", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("geq", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("glyph", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("glyphRef", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("grad", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("gt", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("h1", TreeBuilder.H1_OR_H2_OR_H3_OR_H4_OR_H5_OR_H6, true, false, false));
        set.add(new NameData("h2", TreeBuilder.H1_OR_H2_OR_H3_OR_H4_OR_H5_OR_H6, true, false, false));
        set.add(new NameData("h3", TreeBuilder.H1_OR_H2_OR_H3_OR_H4_OR_H5_OR_H6, true, false, false));
        set.add(new NameData("h4", TreeBuilder.H1_OR_H2_OR_H3_OR_H4_OR_H5_OR_H6, true, false, false));
        set.add(new NameData("h5", TreeBuilder.H1_OR_H2_OR_H3_OR_H4_OR_H5_OR_H6, true, false, false));
        set.add(new NameData("h6", TreeBuilder.H1_OR_H2_OR_H3_OR_H4_OR_H5_OR_H6, true, false, false));
        set.add(new NameData("handler", TreeBuilder.OTHER, false, false, false));
        set.add(HEAD);
        set.add(new NameData("header", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("hkern", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("hr", TreeBuilder.HR, true, false, false));
        set.add(HTML);
        set.add(new NameData("i", TreeBuilder.B_OR_BIG_OR_EM_OR_FONT_OR_I_OR_S_OR_SMALL_OR_STRIKE_OR_STRONG_OR_TT_OR_U, false, false, false));
        set.add(new NameData("ident", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("iframe", TreeBuilder.IFRAME_OR_NOEMBED, true, false, false));
        set.add(new NameData("image", TreeBuilder.IMAGE, true, false, false));
        set.add(new NameData("imaginary", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("imaginaryi", TreeBuilder.OTHER, false, false, false));
        set.add(IMG);
        set.add(new NameData("implies", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("in", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("infinity", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("input", TreeBuilder.INPUT, true, false, false));
        set.add(new NameData("ins", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("int", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("integers", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("intersect", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("interval", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("inverse", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("isindex", TreeBuilder.ISINDEX, true, false, false));
        set.add(new NameData("kbd", TreeBuilder.OTHER, false, false, false));
        set.add(LABEL);
        set.add(new NameData("lambda", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("laplacian", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("lcm", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("legend", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("leq", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("li", TreeBuilder.LI, true, false, false));
        set.add(new NameData("limit", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("line", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("linearGradient", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("link", TreeBuilder.LINK, true, false, false));
        set.add(new NameData("list", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("listener", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("listing", TreeBuilder.PRE_OR_LISTING, true, false, false));
        set.add(new NameData("ln", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("log", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("logbase", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("lowlimit", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("lt", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("maction", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("maligngroup", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("malignmark", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("map", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("mark", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("marker", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("marquee", TreeBuilder.OBJECT_OR_MARQUEE_OR_APPLET, false, true, false));
        set.add(new NameData("mask", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("math", TreeBuilder.MATH, false, false, false));
        set.add(new NameData("matrix", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("matrixrow", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("max", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("mean", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("median", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("menclose", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("menu", TreeBuilder.DIV_OR_BLOCKQUOTE_OR_CENTER_OR_MENU, true, false, false));
        set.add(new NameData("merror", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("meta", TreeBuilder.META, true, false, false));
        set.add(new NameData("metadata", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("meter", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("mfenced", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("mfrac", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("mglyph", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("mi", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("min", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("minus", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("missing-glyph", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("mlabeledtr", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("mmultiscripts", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("mn", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("mo", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("mode", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("moment", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("momentabout", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("mover", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("mpadded", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("mpath", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("mphantom", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("mprescripts", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("mroot", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("mrow", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("ms", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("mspace", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("msqrt", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("mstyle", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("msub", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("msubsup", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("msup", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("mtable", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("mtd", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("mtext", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("mtr", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("munder", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("munderover", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("naturalnumbers", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("nav", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("neq", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("nest", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("nobr", TreeBuilder.NOBR, false, false, false));
        set.add(new NameData("noembed", TreeBuilder.IFRAME_OR_NOEMBED, true, false, false));
        set.add(new NameData("noframes", TreeBuilder.NOFRAMES, true, false, false));
        set.add(new NameData("none", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("noscript", TreeBuilder.NOSCRIPT, true, false, false));
        set.add(new NameData("not", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("notanumber", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("notin", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("notprsubset", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("notsubset", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("object", TreeBuilder.OBJECT_OR_MARQUEE_OR_APPLET, false, true, false));
        set.add(new NameData("ol", TreeBuilder.UL_OR_OL_OR_DL, true, false, false));
        set.add(new NameData("optgroup", TreeBuilder.OPTGROUP, true, false, false));
        set.add(new NameData("option", TreeBuilder.OPTION, true, false, false));
        set.add(new NameData("or", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("otherwise", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("outerproduct", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("output", TreeBuilder.OTHER, false, false, false));
        set.add(P);
        set.add(new NameData("param", TreeBuilder.AREA_OR_BASEFONT_OR_BGSOUND_OR_PARAM_OR_SPACER_OR_WBR, true, false, false));
        set.add(new NameData("partialdiff", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("path", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("pattern", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("pi", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("piece", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("piecewise", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("plaintext", TreeBuilder.PLAINTEXT, true, false, false));
        set.add(new NameData("plus", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("polygon", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("polyline", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("power", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("pre", TreeBuilder.PRE_OR_LISTING, true, false, false));
        set.add(new NameData("prefetch", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("primes", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("product", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("progress", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("prsubset", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("q", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("quotient", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("radialGradient", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("rationals", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("real", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("reals", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("rect", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("reln", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("rem", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("root", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("ruby", TreeBuilder.CODE_OR_RUBY_OR_SPAN_OR_SUB_OR_SUP_OR_VAR, false, false, false));
        set.add(new NameData("rule", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("s", TreeBuilder.B_OR_BIG_OR_EM_OR_FONT_OR_I_OR_S_OR_SMALL_OR_STRIKE_OR_STRONG_OR_TT_OR_U, false, false, false));
        set.add(new NameData("samp", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("scalarproduct", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("script", TreeBuilder.SCRIPT, true, false, false));
        set.add(new NameData("sdev", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("sec", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("sech", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("section", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("select", TreeBuilder.SELECT, true, false, false));
        set.add(new NameData("selector", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("semantics", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("sep", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("set", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("setdiff", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("sin", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("sinh", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("small", TreeBuilder.B_OR_BIG_OR_EM_OR_FONT_OR_I_OR_S_OR_SMALL_OR_STRIKE_OR_STRONG_OR_TT_OR_U, false, false, false));
        set.add(new NameData("solidColor", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("source", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("spacer", TreeBuilder.AREA_OR_BASEFONT_OR_BGSOUND_OR_PARAM_OR_SPACER_OR_WBR, true, false, false));
        set.add(new NameData("span", TreeBuilder.CODE_OR_RUBY_OR_SPAN_OR_SUB_OR_SUP_OR_VAR, false, false, false));
        set.add(new NameData("stop", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("strike", TreeBuilder.B_OR_BIG_OR_EM_OR_FONT_OR_I_OR_S_OR_SMALL_OR_STRIKE_OR_STRONG_OR_TT_OR_U, false, false, false));
        set.add(new NameData("strong", TreeBuilder.B_OR_BIG_OR_EM_OR_FONT_OR_I_OR_S_OR_SMALL_OR_STRIKE_OR_STRONG_OR_TT_OR_U, false, false, false));
        set.add(new NameData("style", TreeBuilder.STYLE, true, false, false));
        set.add(new NameData("sub", TreeBuilder.CODE_OR_RUBY_OR_SPAN_OR_SUB_OR_SUP_OR_VAR, false, false, false));
        set.add(new NameData("subset", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("sum", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("sup", TreeBuilder.CODE_OR_RUBY_OR_SPAN_OR_SUB_OR_SUP_OR_VAR, false, false, false));
        set.add(new NameData("svg", TreeBuilder.SVG, false, false, false));
        set.add(new NameData("switch", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("symbol", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("table", TreeBuilder.TABLE, false, true, true));
        set.add(new NameData("tan", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("tanh", TreeBuilder.OTHER, false, false, false));
        set.add(TBODY);
        set.add(new NameData("tbreak", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("td", TreeBuilder.TD_OR_TH, false, true, false));
        set.add(new NameData("tendsto", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("text", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("textPath", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("textarea", TreeBuilder.TEXTAREA, true, false, false));
        set.add(new NameData("tfoot", TreeBuilder.TBODY_OR_THEAD_OR_TFOOT, true, false, true));
        set.add(new NameData("th", TreeBuilder.TD_OR_TH, false, true, false));
        set.add(new NameData("thead", TreeBuilder.TBODY_OR_THEAD_OR_TFOOT, true, false, true));
        set.add(new NameData("time", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("times", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("title", TreeBuilder.TITLE, true, false, false));
        set.add(TR);
        set.add(new NameData("transpose", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("tref", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("true", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("tspan", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("tt", TreeBuilder.B_OR_BIG_OR_EM_OR_FONT_OR_I_OR_S_OR_SMALL_OR_STRIKE_OR_STRONG_OR_TT_OR_U, false, false, false));
        set.add(new NameData("u", TreeBuilder.B_OR_BIG_OR_EM_OR_FONT_OR_I_OR_S_OR_SMALL_OR_STRIKE_OR_STRONG_OR_TT_OR_U, false, false, false));
        set.add(new NameData("ul", TreeBuilder.UL_OR_OL_OR_DL, true, false, false));
        set.add(new NameData("union", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("uplimit", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("use", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("var", TreeBuilder.CODE_OR_RUBY_OR_SPAN_OR_SUB_OR_SUP_OR_VAR, false, false, false));
        set.add(new NameData("variance", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("vector", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("vectorproduct", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("video", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("view", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("vkern", TreeBuilder.OTHER, false, false, false));
        set.add(new NameData("wbr", TreeBuilder.AREA_OR_BASEFONT_OR_BGSOUND_OR_PARAM_OR_SPACER_OR_WBR, true, false, false));
        set.add(new NameData("xmp", TreeBuilder.XMP, false, false, false));
        set.add(new NameData("xor", TreeBuilder.OTHER, false, false, false));
        return set;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return name;
    }
}
