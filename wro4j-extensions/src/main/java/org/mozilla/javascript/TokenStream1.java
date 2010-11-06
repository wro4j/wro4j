/* ***** BEGIN LICENSE BLOCK *****
 *
 * Version: MPL 1.1
 *
 * The contents of this file are subject to the Mozilla Public License
 * Version 1.1 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License
 * at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied.
 * See the License for the specific language governing rights and
 * limitations under the License.
 *
 * The Original Code is org/mozilla/javascript/TokenStream.java,
 * a component of the Rhino Library ( http://www.mozilla.org/rhino/ )
 * This file is a modification of the Original Code developed
 * for YUI Compressor.
 *
 * The Initial Developer of the Original Code is Mozilla Foundation
 *
 * Copyright (c) 2009 Mozilla Foundation. All Rights Reserved.
 *
 * Contributor(s):  Yahoo! Inc. 2009
 *
 * ***** END LICENSE BLOCK ***** */

package org.mozilla.javascript;

import java.io.IOException;
import java.io.Reader;

/**
 * This class implements the JavaScript scanner.
 *
 * It is based on the C source files jsscan.c and jsscan.h
 * in the jsref package.
 *
 * @see org.mozilla.javascript.Parser1
 *
 * @author Mike McCabe
 * @author Brendan Eich
 */

class TokenStream1
{
    /*
     * For chars - because we need something out-of-range
     * to check.  (And checking EOF by exception is annoying.)
     * Note distinction from EOF token type!
     */
    private final static int
        EOF_CHAR = -1;

    TokenStream1(final Parser1 parser, final Reader sourceReader, final String sourceString,
                final int lineno)
    {
        this.parser = parser;
        this.lineno = lineno;
        if (sourceReader != null) {
            if (sourceString != null) Kit.codeBug();
            this.sourceReader = sourceReader;
            this.sourceBuffer = new char[512];
            this.sourceEnd = 0;
        } else {
            if (sourceString == null) Kit.codeBug();
            this.sourceString = sourceString;
            this.sourceEnd = sourceString.length();
        }
        this.sourceCursor = 0;
    }

    /* This function uses the cached op, string and number fields in
     * TokenStream; if getToken has been called since the passed token
     * was scanned, the op or string printed may be incorrect.
     */
    String tokenToString(final int token)
    {
        if (Token1.printTrees) {
            final String name = Token1.name(token);

            switch (token) {
            case Token1.STRING:
            case Token1.REGEXP:
            case Token1.NAME:
                return name + " `" + this.string + "'";

            case Token1.NUMBER:
                return "NUMBER " + this.number;
            }

            return name;
        }
        return "";
    }

    static boolean isKeyword(final String s)
    {
        return Token1.EOF != stringToKeyword(s);
    }

    private static int stringToKeyword(final String name)
    {
// #string_id_map#
// The following assumes that Token.EOF == 0
        final int
            Id_break         = Token1.BREAK,
            Id_case          = Token1.CASE,
            Id_continue      = Token1.CONTINUE,
            Id_default       = Token1.DEFAULT,
            Id_delete        = Token1.DELPROP,
            Id_do            = Token1.DO,
            Id_else          = Token1.ELSE,
            Id_export        = Token1.EXPORT,
            Id_false         = Token1.FALSE,
            Id_for           = Token1.FOR,
            Id_function      = Token1.FUNCTION,
            Id_if            = Token1.IF,
            Id_in            = Token1.IN,
            Id_new           = Token1.NEW,
            Id_null          = Token1.NULL,
            Id_return        = Token1.RETURN,
            Id_switch        = Token1.SWITCH,
            Id_this          = Token1.THIS,
            Id_true          = Token1.TRUE,
            Id_typeof        = Token1.TYPEOF,
            Id_var           = Token1.VAR,
            Id_void          = Token1.VOID,
            Id_while         = Token1.WHILE,
            Id_with          = Token1.WITH,

            // the following are #ifdef RESERVE_JAVA_KEYWORDS in jsscan.c
            Id_abstract      = Token1.RESERVED,
            Id_boolean       = Token1.RESERVED,
            Id_byte          = Token1.RESERVED,
            Id_catch         = Token1.CATCH,
            Id_char          = Token1.RESERVED,
            Id_class         = Token1.RESERVED,
            Id_const         = Token1.CONST,
            Id_debugger      = Token1.RESERVED,
            Id_double        = Token1.RESERVED,
            Id_enum          = Token1.RESERVED,
            Id_extends       = Token1.RESERVED,
            Id_final         = Token1.RESERVED,
            Id_finally       = Token1.FINALLY,
            Id_float         = Token1.RESERVED,
            Id_goto          = Token1.RESERVED,
            Id_implements    = Token1.RESERVED,
            Id_import        = Token1.IMPORT,
            Id_instanceof    = Token1.INSTANCEOF,
            Id_int           = Token1.RESERVED,
            Id_interface     = Token1.RESERVED,
            Id_long          = Token1.RESERVED,
            Id_native        = Token1.RESERVED,
            Id_package       = Token1.RESERVED,
            Id_private       = Token1.RESERVED,
            Id_protected     = Token1.RESERVED,
            Id_public        = Token1.RESERVED,
            Id_short         = Token1.RESERVED,
            Id_static        = Token1.RESERVED,
            Id_super         = Token1.RESERVED,
            Id_synchronized  = Token1.RESERVED,
            Id_throw         = Token1.THROW,
            Id_throws        = Token1.RESERVED,
            Id_transient     = Token1.RESERVED,
            Id_try           = Token1.TRY,
            Id_volatile      = Token1.RESERVED;

        int id;
        final String s = name;
// #generated# Last update: 2001-06-01 17:45:01 CEST
        L0: { id = 0; String X = null; int c;
            L: switch (s.length()) {
            case 2: c=s.charAt(1);
                if (c=='f') { if (s.charAt(0)=='i') {id=Id_if; break L0;} }
                else if (c=='n') { if (s.charAt(0)=='i') {id=Id_in; break L0;} }
                else if (c=='o') { if (s.charAt(0)=='d') {id=Id_do; break L0;} }
                break L;
            case 3: switch (s.charAt(0)) {
                case 'f': if (s.charAt(2)=='r' && s.charAt(1)=='o') {id=Id_for; break L0;} break L;
                case 'i': if (s.charAt(2)=='t' && s.charAt(1)=='n') {id=Id_int; break L0;} break L;
                case 'n': if (s.charAt(2)=='w' && s.charAt(1)=='e') {id=Id_new; break L0;} break L;
                case 't': if (s.charAt(2)=='y' && s.charAt(1)=='r') {id=Id_try; break L0;} break L;
                case 'v': if (s.charAt(2)=='r' && s.charAt(1)=='a') {id=Id_var; break L0;} break L;
                } break L;
            case 4: switch (s.charAt(0)) {
                case 'b': X="byte";id=Id_byte; break L;
                case 'c': c=s.charAt(3);
                    if (c=='e') { if (s.charAt(2)=='s' && s.charAt(1)=='a') {id=Id_case; break L0;} }
                    else if (c=='r') { if (s.charAt(2)=='a' && s.charAt(1)=='h') {id=Id_char; break L0;} }
                    break L;
                case 'e': c=s.charAt(3);
                    if (c=='e') { if (s.charAt(2)=='s' && s.charAt(1)=='l') {id=Id_else; break L0;} }
                    else if (c=='m') { if (s.charAt(2)=='u' && s.charAt(1)=='n') {id=Id_enum; break L0;} }
                    break L;
                case 'g': X="goto";id=Id_goto; break L;
                case 'l': X="long";id=Id_long; break L;
                case 'n': X="null";id=Id_null; break L;
                case 't': c=s.charAt(3);
                    if (c=='e') { if (s.charAt(2)=='u' && s.charAt(1)=='r') {id=Id_true; break L0;} }
                    else if (c=='s') { if (s.charAt(2)=='i' && s.charAt(1)=='h') {id=Id_this; break L0;} }
                    break L;
                case 'v': X="void";id=Id_void; break L;
                case 'w': X="with";id=Id_with; break L;
                } break L;
            case 5: switch (s.charAt(2)) {
                case 'a': X="class";id=Id_class; break L;
                case 'e': X="break";id=Id_break; break L;
                case 'i': X="while";id=Id_while; break L;
                case 'l': X="false";id=Id_false; break L;
                case 'n': c=s.charAt(0);
                    if (c=='c') { X="const";id=Id_const; }
                    else if (c=='f') { X="final";id=Id_final; }
                    break L;
                case 'o': c=s.charAt(0);
                    if (c=='f') { X="float";id=Id_float; }
                    else if (c=='s') { X="short";id=Id_short; }
                    break L;
                case 'p': X="super";id=Id_super; break L;
                case 'r': X="throw";id=Id_throw; break L;
                case 't': X="catch";id=Id_catch; break L;
                } break L;
            case 6: switch (s.charAt(1)) {
                case 'a': X="native";id=Id_native; break L;
                case 'e': c=s.charAt(0);
                    if (c=='d') { X="delete";id=Id_delete; }
                    else if (c=='r') { X="return";id=Id_return; }
                    break L;
                case 'h': X="throws";id=Id_throws; break L;
                case 'm': X="import";id=Id_import; break L;
                case 'o': X="double";id=Id_double; break L;
                case 't': X="static";id=Id_static; break L;
                case 'u': X="public";id=Id_public; break L;
                case 'w': X="switch";id=Id_switch; break L;
                case 'x': X="export";id=Id_export; break L;
                case 'y': X="typeof";id=Id_typeof; break L;
                } break L;
            case 7: switch (s.charAt(1)) {
                case 'a': X="package";id=Id_package; break L;
                case 'e': X="default";id=Id_default; break L;
                case 'i': X="finally";id=Id_finally; break L;
                case 'o': X="boolean";id=Id_boolean; break L;
                case 'r': X="private";id=Id_private; break L;
                case 'x': X="extends";id=Id_extends; break L;
                } break L;
            case 8: switch (s.charAt(0)) {
                case 'a': X="abstract";id=Id_abstract; break L;
                case 'c': X="continue";id=Id_continue; break L;
                case 'd': X="debugger";id=Id_debugger; break L;
                case 'f': X="function";id=Id_function; break L;
                case 'v': X="volatile";id=Id_volatile; break L;
                } break L;
            case 9: c=s.charAt(0);
                if (c=='i') { X="interface";id=Id_interface; }
                else if (c=='p') { X="protected";id=Id_protected; }
                else if (c=='t') { X="transient";id=Id_transient; }
                break L;
            case 10: c=s.charAt(1);
                if (c=='m') { X="implements";id=Id_implements; }
                else if (c=='n') { X="instanceof";id=Id_instanceof; }
                break L;
            case 12: X="synchronized";id=Id_synchronized; break L;
            }
            if (X!=null && X!=s && !X.equals(s)) id = 0;
        }
// #/generated#
// #/string_id_map#
        if (id == 0) { return Token1.EOF; }
        return id & 0xff;
    }

    final int getLineno() { return lineno; }

    final String getString() { return string; }

    final double getNumber() { return number; }

    final boolean eof() { return hitEOF; }

    final int getToken() throws IOException
    {
        int c;

    retry:
        for (;;) {
            // Eat whitespace, possibly sensitive to newlines.
            for (;;) {
                c = getChar();
                if (c == EOF_CHAR) {
                    return Token1.EOF;
                } else if (c == '\n') {
                    dirtyLine = false;
                    return Token1.EOL;
                } else if (!isJSSpace(c)) {
                    if (c != '-') {
                        dirtyLine = true;
                    }
                    break;
                }
            }

            if (c == '@') return Token1.XMLATTR;

            // identifier/keyword/instanceof?
            // watch out for starting with a <backslash>
            boolean identifierStart;
            boolean isUnicodeEscapeStart = false;
            if (c == '\\') {
                c = getChar();
                if (c == 'u') {
                    identifierStart = true;
                    isUnicodeEscapeStart = true;
                    stringBufferTop = 0;
                } else {
                    identifierStart = false;
                    ungetChar(c);
                    c = '\\';
                }
            } else {
                identifierStart = Character.isJavaIdentifierStart((char)c);
                if (identifierStart) {
                    stringBufferTop = 0;
                    addToString(c);
                }
            }

            if (identifierStart) {
                boolean containsEscape = isUnicodeEscapeStart;
                for (;;) {
                    if (isUnicodeEscapeStart) {
                        // strictly speaking we should probably push-back
                        // all the bad characters if the <backslash>uXXXX
                        // sequence is malformed. But since there isn't a
                        // correct context(is there?) for a bad Unicode
                        // escape sequence in an identifier, we can report
                        // an error here.
                        int escapeVal = 0;
                        for (int i = 0; i != 4; ++i) {
                            c = getChar();
                            escapeVal = Kit.xDigitToInt(c, escapeVal);
                            // Next check takes care about c < 0 and bad escape
                            if (escapeVal < 0) { break; }
                        }
                        if (escapeVal < 0) {
                            parser.addError("msg.invalid.escape");
                            return Token1.ERROR;
                        }
                        addToString(escapeVal);
                        isUnicodeEscapeStart = false;
                    } else {
                        c = getChar();
                        if (c == '\\') {
                            c = getChar();
                            if (c == 'u') {
                                isUnicodeEscapeStart = true;
                                containsEscape = true;
                            } else {
                                parser.addError("msg.illegal.character");
                                return Token1.ERROR;
                            }
                        } else {
                            if (c == EOF_CHAR
                                || !Character.isJavaIdentifierPart((char)c))
                            {
                                break;
                            }
                            addToString(c);
                        }
                    }
                }
                ungetChar(c);

                final String str = getStringFromBuffer();
                if (!containsEscape) {
                    // OPT we shouldn't have to make a string (object!) to
                    // check if it's a keyword.

                    // Return the corresponding token if it's a keyword
                    final int result = stringToKeyword(str);
                    if (result != Token1.EOF) {
                        if (result != Token1.RESERVED) {
                            return result;
                        } else if (!parser.compilerEnv.
                                        isReservedKeywordAsIdentifier())
                        {
                            return result;
                        } else {
                            // If implementation permits to use future reserved
                            // keywords in violation with the EcmaScript,
                            // treat it as name but issue warning
                            parser.addWarning("msg.reserved.keyword", str);
                        }
                    }
                }
                this.string = (String)allStrings.intern(str);
                return Token1.NAME;
            }

            // is it a number?
            if (isDigit(c) || (c == '.' && isDigit(peekChar()))) {

                stringBufferTop = 0;
                int base = 10;

                if (c == '0') {
                    c = getChar();
                    if (c == 'x' || c == 'X') {
                        base = 16;
                        c = getChar();
                    } else if (isDigit(c)) {
                        base = 8;
                    } else {
                        addToString('0');
                    }
                }

                if (base == 16) {
                    while (0 <= Kit.xDigitToInt(c, 0)) {
                        addToString(c);
                        c = getChar();
                    }
                } else {
                    while ('0' <= c && c <= '9') {
                        /*
                         * We permit 08 and 09 as decimal numbers, which
                         * makes our behavior a superset of the ECMA
                         * numeric grammar.  We might not always be so
                         * permissive, so we warn about it.
                         */
                        if (base == 8 && c >= '8') {
                            parser.addWarning("msg.bad.octal.literal",
                                              c == '8' ? "8" : "9");
                            base = 10;
                        }
                        addToString(c);
                        c = getChar();
                    }
                }

                boolean isInteger = true;

                if (base == 10 && (c == '.' || c == 'e' || c == 'E')) {
                    isInteger = false;
                    if (c == '.') {
                        do {
                            addToString(c);
                            c = getChar();
                        } while (isDigit(c));
                    }
                    if (c == 'e' || c == 'E') {
                        addToString(c);
                        c = getChar();
                        if (c == '+' || c == '-') {
                            addToString(c);
                            c = getChar();
                        }
                        if (!isDigit(c)) {
                            parser.addError("msg.missing.exponent");
                            return Token1.ERROR;
                        }
                        do {
                            addToString(c);
                            c = getChar();
                        } while (isDigit(c));
                    }
                }
                ungetChar(c);
                final String numString = getStringFromBuffer();

                double dval;
                if (base == 10 && !isInteger) {
                    try {
                        // Use Java conversion to number from string...
                        dval = Double.valueOf(numString).doubleValue();
                    }
                    catch (final NumberFormatException ex) {
                        parser.addError("msg.caught.nfe");
                        return Token1.ERROR;
                    }
                } else {
                    dval = ScriptRuntime.stringToNumber(numString, 0, base);
                }

                this.number = dval;
                return Token1.NUMBER;
            }

            // is it a string?
            if (c == '"' || c == '\'') {
                // We attempt to accumulate a string the fast way, by
                // building it directly out of the reader.  But if there
                // are any escaped characters in the string, we revert to
                // building it out of a StringBuffer.

                final int quoteChar = c;
                stringBufferTop = 0;

                c = getChar();
                while (c != quoteChar) {
                    if (c == '\n' || c == EOF_CHAR) {
                        ungetChar(c);
                        parser.addError("msg.unterminated.string.lit");
                        return Token1.ERROR;
                    }

                    if (c == '\\') {
                        // We've hit an escaped character

                        c = getChar();

                        switch (c) {

                            case '\\': // backslash
                            case 'b':  // backspace
                            case 'f':  // form feed
                            case 'n':  // line feed
                            case 'r':  // carriage return
                            case 't':  // horizontal tab
                            case 'v':  // vertical tab
                            case 'd':  // octal sequence
                            case 'u':  // unicode sequence
                            case 'x':  // hexadecimal sequence
                                // Only keep the '\' character for those
                                // characters that need to be escaped...
                                // Don't escape quoting characters...
                                addToString('\\');
                                addToString(c);
                                break;

                            case '\n':
                                // Remove line terminator after escape
                                break;

                            default:
                                if (isDigit(c)) {
                                    // Octal representation of a character.
                                    // Preserve the escaping (see Y! bug #1637286)
                                    addToString('\\');
                                }
                                addToString(c);
                                break;
                        }

                    } else {

                        addToString(c);
                    }

                    c = getChar();
                }

                final String str = getStringFromBuffer();
                this.string = (String)allStrings.intern(str);
                return Token1.STRING;
            }

            switch (c) {
            case ';': return Token1.SEMI;
            case '[': return Token1.LB;
            case ']': return Token1.RB;
            case '{': return Token1.LC;
            case '}': return Token1.RC;
            case '(': return Token1.LP;
            case ')': return Token1.RP;
            case ',': return Token1.COMMA;
            case '?': return Token1.HOOK;
            case ':':
                if (matchChar(':')) {
                    return Token1.COLONCOLON;
                } else {
                    return Token1.COLON;
                }
            case '.':
                if (matchChar('.')) {
                    return Token1.DOTDOT;
                } else if (matchChar('(')) {
                    return Token1.DOTQUERY;
                } else {
                    return Token1.DOT;
                }

            case '|':
                if (matchChar('|')) {
                    return Token1.OR;
                } else if (matchChar('=')) {
                    return Token1.ASSIGN_BITOR;
                } else {
                    return Token1.BITOR;
                }

            case '^':
                if (matchChar('=')) {
                    return Token1.ASSIGN_BITXOR;
                } else {
                    return Token1.BITXOR;
                }

            case '&':
                if (matchChar('&')) {
                    return Token1.AND;
                } else if (matchChar('=')) {
                    return Token1.ASSIGN_BITAND;
                } else {
                    return Token1.BITAND;
                }

            case '=':
                if (matchChar('=')) {
                    if (matchChar('='))
                        return Token1.SHEQ;
                    else
                        return Token1.EQ;
                } else {
                    return Token1.ASSIGN;
                }

            case '!':
                if (matchChar('=')) {
                    if (matchChar('='))
                        return Token1.SHNE;
                    else
                        return Token1.NE;
                } else {
                    return Token1.NOT;
                }

            case '<':
                /* NB:treat HTML begin-comment as comment-till-eol */
                if (matchChar('!')) {
                    if (matchChar('-')) {
                        if (matchChar('-')) {
                            skipLine();
                            continue retry;
                        }
                        ungetChar('-');
                    }
                    ungetChar('!');
                }
                if (matchChar('<')) {
                    if (matchChar('=')) {
                        return Token1.ASSIGN_LSH;
                    } else {
                        return Token1.LSH;
                    }
                } else {
                    if (matchChar('=')) {
                        return Token1.LE;
                    } else {
                        return Token1.LT;
                    }
                }

            case '>':
                if (matchChar('>')) {
                    if (matchChar('>')) {
                        if (matchChar('=')) {
                            return Token1.ASSIGN_URSH;
                        } else {
                            return Token1.URSH;
                        }
                    } else {
                        if (matchChar('=')) {
                            return Token1.ASSIGN_RSH;
                        } else {
                            return Token1.RSH;
                        }
                    }
                } else {
                    if (matchChar('=')) {
                        return Token1.GE;
                    } else {
                        return Token1.GT;
                    }
                }

            case '*':
                if (matchChar('=')) {
                    return Token1.ASSIGN_MUL;
                } else {
                    return Token1.MUL;
                }

            case '/':
                // is it a // comment?
                if (matchChar('/')) {
                    skipLine();
                    continue retry;
                }
                if (matchChar('*')) {
                    boolean lookForSlash = false;
                    final StringBuffer sb = new StringBuffer();
                    for (;;) {
                        c = getChar();
                        if (c == EOF_CHAR) {
                            parser.addError("msg.unterminated.comment");
                            return Token1.ERROR;
                        }
                        sb.append((char) c);
                        if (c == '*') {
                            lookForSlash = true;
                        } else if (c == '/') {
                            if (lookForSlash) {
                                sb.delete(sb.length()-2, sb.length());
                                final String s1 = sb.toString();
                                final String s2 = s1.trim();
                                /*
                                if (s1.startsWith("!")) {
                                    // Remove the leading '!' ** EDIT actually don't remove it:
                                    // http://yuilibrary.com/projects/yuicompressor/ticket/2528008
                                    // this.string = s1.substring(1);
                                    this.string = s1;
                                    return Token.KEEPCOMMENT;
                                } else if (s2.startsWith("@cc_on") ||
                                           s2.startsWith("@if")    ||
                                           s2.startsWith("@elif")  ||
                                           s2.startsWith("@else")  ||
                                           s2.startsWith("@end")) {
                                    this.string = s1;
                                    return Token.CONDCOMMENT;
                                } else {
                                    continue retry;
                                }
                                */
                                continue retry;
                            }
                        } else {
                            lookForSlash = false;
                        }
                    }
                }

                if (matchChar('=')) {
                    return Token1.ASSIGN_DIV;
                } else {
                    return Token1.DIV;
                }

            case '%':
                if (matchChar('=')) {
                    return Token1.ASSIGN_MOD;
                } else {
                    return Token1.MOD;
                }

            case '~':
                return Token1.BITNOT;

            case '+':
                if (matchChar('=')) {
                    return Token1.ASSIGN_ADD;
                } else if (matchChar('+')) {
                    return Token1.INC;
                } else {
                    return Token1.ADD;
                }

            case '-':
                if (matchChar('=')) {
                    c = Token1.ASSIGN_SUB;
                } else if (matchChar('-')) {
                    if (!dirtyLine) {
                        // treat HTML end-comment after possible whitespace
                        // after line start as comment-utill-eol
                        if (matchChar('>')) {
                            skipLine();
                            continue retry;
                        }
                    }
                    c = Token1.DEC;
                } else {
                    c = Token1.SUB;
                }
                dirtyLine = true;
                return c;

            default:
                parser.addError("msg.illegal.character");
                return Token1.ERROR;
            }
        }
    }

    private static boolean isAlpha(final int c)
    {
        // Use 'Z' < 'a'
        if (c <= 'Z') {
            return 'A' <= c;
        } else {
            return 'a' <= c && c <= 'z';
        }
    }

    static boolean isDigit(final int c)
    {
        return '0' <= c && c <= '9';
    }

    /* As defined in ECMA.  jsscan.c uses C isspace() (which allows
     * \v, I think.)  note that code in getChar() implicitly accepts
     * '\r' == \u000D as well.
     */
    static boolean isJSSpace(final int c)
    {
        if (c <= 127) {
            return c == 0x20 || c == 0x9 || c == 0xC || c == 0xB;
        } else {
            return c == 0xA0
                || Character.getType((char)c) == Character.SPACE_SEPARATOR;
        }
    }

    private static boolean isJSFormatChar(final int c)
    {
        return c > 127 && Character.getType((char)c) == Character.FORMAT;
    }

    /**
     * Parser calls the method when it gets / or /= in literal context.
     */
    void readRegExp(final int startToken)
        throws IOException
    {
        stringBufferTop = 0;
        if (startToken == Token1.ASSIGN_DIV) {
            // Miss-scanned /=
            addToString('=');
        } else {
            if (startToken != Token1.DIV) Kit.codeBug();
        }

        int c;
        boolean inClass = false;
        while ((c = getChar()) != '/' || inClass) {
            if (c == '\n' || c == EOF_CHAR) {
                ungetChar(c);
                throw parser.reportError("msg.unterminated.re.lit");
            }
            if (c == '\\') {
                addToString(c);
                c = getChar();
            } else if (c == '[') {
                inClass = true;
            } else if (c == ']') {
                inClass = false;
            }

            addToString(c);
        }
        final int reEnd = stringBufferTop;

        while (true) {
            if (matchChar('g'))
                addToString('g');
            else if (matchChar('i'))
                addToString('i');
            else if (matchChar('m'))
                addToString('m');
            else
                break;
        }

        if (isAlpha(peekChar())) {
            throw parser.reportError("msg.invalid.re.flag");
        }

        this.string = new String(stringBuffer, 0, reEnd);
        this.regExpFlags = new String(stringBuffer, reEnd,
                                      stringBufferTop - reEnd);
    }

    boolean isXMLAttribute()
    {
        return xmlIsAttribute;
    }

    int getFirstXMLToken() throws IOException
    {
        xmlOpenTagsCount = 0;
        xmlIsAttribute = false;
        xmlIsTagContent = false;
        ungetChar('<');
        return getNextXMLToken();
    }

    int getNextXMLToken() throws IOException
    {
        stringBufferTop = 0; // remember the XML

        for (int c = getChar(); c != EOF_CHAR; c = getChar()) {
            if (xmlIsTagContent) {
                switch (c) {
                case '>':
                    addToString(c);
                    xmlIsTagContent = false;
                    xmlIsAttribute = false;
                    break;
                case '/':
                    addToString(c);
                    if (peekChar() == '>') {
                        c = getChar();
                        addToString(c);
                        xmlIsTagContent = false;
                        xmlOpenTagsCount--;
                    }
                    break;
                case '{':
                    ungetChar(c);
                    this.string = getStringFromBuffer();
                    return Token1.XML;
                case '\'':
                case '"':
                    addToString(c);
                    if (!readQuotedString(c)) return Token1.ERROR;
                    break;
                case '=':
                    addToString(c);
                    xmlIsAttribute = true;
                    break;
                case ' ':
                case '\t':
                case '\r':
                case '\n':
                    addToString(c);
                    break;
                default:
                    addToString(c);
                    xmlIsAttribute = false;
                    break;
                }

                if (!xmlIsTagContent && xmlOpenTagsCount == 0) {
                    this.string = getStringFromBuffer();
                    return Token1.XMLEND;
                }
            } else {
                switch (c) {
                case '<':
                    addToString(c);
                    c = peekChar();
                    switch (c) {
                    case '!':
                        c = getChar(); // Skip !
                        addToString(c);
                        c = peekChar();
                        switch (c) {
                        case '-':
                            c = getChar(); // Skip -
                            addToString(c);
                            c = getChar();
                            if (c == '-') {
                                addToString(c);
                                if(!readXmlComment()) return Token1.ERROR;
                            } else {
                                // throw away the string in progress
                                stringBufferTop = 0;
                                this.string = null;
                                parser.addError("msg.XML.bad.form");
                                return Token1.ERROR;
                            }
                            break;
                        case '[':
                            c = getChar(); // Skip [
                            addToString(c);
                            if (getChar() == 'C' &&
                                getChar() == 'D' &&
                                getChar() == 'A' &&
                                getChar() == 'T' &&
                                getChar() == 'A' &&
                                getChar() == '[')
                            {
                                addToString('C');
                                addToString('D');
                                addToString('A');
                                addToString('T');
                                addToString('A');
                                addToString('[');
                                if (!readCDATA()) return Token1.ERROR;

                            } else {
                                // throw away the string in progress
                                stringBufferTop = 0;
                                this.string = null;
                                parser.addError("msg.XML.bad.form");
                                return Token1.ERROR;
                            }
                            break;
                        default:
                            if(!readEntity()) return Token1.ERROR;
                            break;
                        }
                        break;
                    case '?':
                        c = getChar(); // Skip ?
                        addToString(c);
                        if (!readPI()) return Token1.ERROR;
                        break;
                    case '/':
                        // End tag
                        c = getChar(); // Skip /
                        addToString(c);
                        if (xmlOpenTagsCount == 0) {
                            // throw away the string in progress
                            stringBufferTop = 0;
                            this.string = null;
                            parser.addError("msg.XML.bad.form");
                            return Token1.ERROR;
                        }
                        xmlIsTagContent = true;
                        xmlOpenTagsCount--;
                        break;
                    default:
                        // Start tag
                        xmlIsTagContent = true;
                        xmlOpenTagsCount++;
                        break;
                    }
                    break;
                case '{':
                    ungetChar(c);
                    this.string = getStringFromBuffer();
                    return Token1.XML;
                default:
                    addToString(c);
                    break;
                }
            }
        }

        stringBufferTop = 0; // throw away the string in progress
        this.string = null;
        parser.addError("msg.XML.bad.form");
        return Token1.ERROR;
    }

    /**
     *
     */
    private boolean readQuotedString(final int quote) throws IOException
    {
        for (int c = getChar(); c != EOF_CHAR; c = getChar()) {
            addToString(c);
            if (c == quote) return true;
        }

        stringBufferTop = 0; // throw away the string in progress
        this.string = null;
        parser.addError("msg.XML.bad.form");
        return false;
    }

    /**
     *
     */
    private boolean readXmlComment() throws IOException
    {
        for (int c = getChar(); c != EOF_CHAR;) {
            addToString(c);
            if (c == '-' && peekChar() == '-') {
                c = getChar();
                addToString(c);
                if (peekChar() == '>') {
                    c = getChar(); // Skip >
                    addToString(c);
                    return true;
                } else {
                    continue;
                }
            }
            c = getChar();
        }

        stringBufferTop = 0; // throw away the string in progress
        this.string = null;
        parser.addError("msg.XML.bad.form");
        return false;
    }

    /**
     *
     */
    private boolean readCDATA() throws IOException
    {
        for (int c = getChar(); c != EOF_CHAR;) {
            addToString(c);
            if (c == ']' && peekChar() == ']') {
                c = getChar();
                addToString(c);
                if (peekChar() == '>') {
                    c = getChar(); // Skip >
                    addToString(c);
                    return true;
                } else {
                    continue;
                }
            }
            c = getChar();
        }

        stringBufferTop = 0; // throw away the string in progress
        this.string = null;
        parser.addError("msg.XML.bad.form");
        return false;
    }

    /**
     *
     */
    private boolean readEntity() throws IOException
    {
        int declTags = 1;
        for (int c = getChar(); c != EOF_CHAR; c = getChar()) {
            addToString(c);
            switch (c) {
            case '<':
                declTags++;
                break;
            case '>':
                declTags--;
                if (declTags == 0) return true;
                break;
            }
        }

        stringBufferTop = 0; // throw away the string in progress
        this.string = null;
        parser.addError("msg.XML.bad.form");
        return false;
    }

    /**
     *
     */
    private boolean readPI() throws IOException
    {
        for (int c = getChar(); c != EOF_CHAR; c = getChar()) {
            addToString(c);
            if (c == '?' && peekChar() == '>') {
                c = getChar(); // Skip >
                addToString(c);
                return true;
            }
        }

        stringBufferTop = 0; // throw away the string in progress
        this.string = null;
        parser.addError("msg.XML.bad.form");
        return false;
    }

    private String getStringFromBuffer()
    {
        return new String(stringBuffer, 0, stringBufferTop);
    }

    private void addToString(final int c)
    {
        final int N = stringBufferTop;
        if (N == stringBuffer.length) {
            final char[] tmp = new char[stringBuffer.length * 2];
            System.arraycopy(stringBuffer, 0, tmp, 0, N);
            stringBuffer = tmp;
        }
        stringBuffer[N] = (char)c;
        stringBufferTop = N + 1;
    }

    private void ungetChar(final int c)
    {
        // can not unread past across line boundary
        if (ungetCursor != 0 && ungetBuffer[ungetCursor - 1] == '\n')
            Kit.codeBug();
        ungetBuffer[ungetCursor++] = c;
    }

    private boolean matchChar(final int test) throws IOException
    {
        final int c = getChar();
        if (c == test) {
            return true;
        } else {
            ungetChar(c);
            return false;
        }
    }

    private int peekChar() throws IOException
    {
        final int c = getChar();
        ungetChar(c);
        return c;
    }

    private int getChar() throws IOException
    {
        if (ungetCursor != 0) {
            return ungetBuffer[--ungetCursor];
        }

        for(;;) {
            int c;
            if (sourceString != null) {
                if (sourceCursor == sourceEnd) {
                    hitEOF = true;
                    return EOF_CHAR;
                }
                c = sourceString.charAt(sourceCursor++);
            } else {
                if (sourceCursor == sourceEnd) {
                    if (!fillSourceBuffer()) {
                        hitEOF = true;
                        return EOF_CHAR;
                    }
                }
                c = sourceBuffer[sourceCursor++];
            }

            if (lineEndChar >= 0) {
                if (lineEndChar == '\r' && c == '\n') {
                    lineEndChar = '\n';
                    continue;
                }
                lineEndChar = -1;
                lineStart = sourceCursor - 1;
                lineno++;
            }

            if (c <= 127) {
                if (c == '\n' || c == '\r') {
                    lineEndChar = c;
                    c = '\n';
                }
            } else {
                if (isJSFormatChar(c)) {
                    continue;
                }
                if (ScriptRuntime.isJSLineTerminator(c)) {
                    lineEndChar = c;
                    c = '\n';
                }
            }
            return c;
        }
    }

    private void skipLine() throws IOException
    {
        // skip to end of line
        int c;
        while ((c = getChar()) != EOF_CHAR && c != '\n') { }
        ungetChar(c);
    }

    final int getOffset()
    {
        int n = sourceCursor - lineStart;
        if (lineEndChar >= 0) { --n; }
        return n;
    }

    final String getLine()
    {
        if (sourceString != null) {
            // String case
            int lineEnd = sourceCursor;
            if (lineEndChar >= 0) {
                --lineEnd;
            } else {
                for (; lineEnd != sourceEnd; ++lineEnd) {
                    final int c = sourceString.charAt(lineEnd);
                    if (ScriptRuntime.isJSLineTerminator(c)) {
                        break;
                    }
                }
            }
            return sourceString.substring(lineStart, lineEnd);
        } else {
            // Reader case
            int lineLength = sourceCursor - lineStart;
            if (lineEndChar >= 0) {
                --lineLength;
            } else {
                // Read until the end of line
                for (;; ++lineLength) {
                    int i = lineStart + lineLength;
                    if (i == sourceEnd) {
                        try {
                            if (!fillSourceBuffer()) { break; }
                        } catch (final IOException ioe) {
                            // ignore it, we're already displaying an error...
                            break;
                        }
                        // i recalculuation as fillSourceBuffer can move saved
                        // line buffer and change lineStart
                        i = lineStart + lineLength;
                    }
                    final int c = sourceBuffer[i];
                    if (ScriptRuntime.isJSLineTerminator(c)) {
                        break;
                    }
                }
            }
            return new String(sourceBuffer, lineStart, lineLength);
        }
    }

    private boolean fillSourceBuffer() throws IOException
    {
        if (sourceString != null) Kit.codeBug();
        if (sourceEnd == sourceBuffer.length) {
            if (lineStart != 0) {
                System.arraycopy(sourceBuffer, lineStart, sourceBuffer, 0,
                                 sourceEnd - lineStart);
                sourceEnd -= lineStart;
                sourceCursor -= lineStart;
                lineStart = 0;
            } else {
                final char[] tmp = new char[sourceBuffer.length * 2];
                System.arraycopy(sourceBuffer, 0, tmp, 0, sourceEnd);
                sourceBuffer = tmp;
            }
        }
        final int n = sourceReader.read(sourceBuffer, sourceEnd,
                                  sourceBuffer.length - sourceEnd);
        if (n < 0) {
            return false;
        }
        sourceEnd += n;
        return true;
    }

    // stuff other than whitespace since start of line
    private boolean dirtyLine;

    String regExpFlags;

    // Set this to an inital non-null value so that the Parser has
    // something to retrieve even if an error has occured and no
    // string is found.  Fosters one class of error, but saves lots of
    // code.
    private String string = "";
    private double number;

    private char[] stringBuffer = new char[128];
    private int stringBufferTop;
    private ObjToIntMap allStrings = new ObjToIntMap(50);

    // Room to backtrace from to < on failed match of the last - in <!--
    private final int[] ungetBuffer = new int[3];
    private int ungetCursor;

    private boolean hitEOF = false;

    private int lineStart = 0;
    private int lineno;
    private int lineEndChar = -1;

    private String sourceString;
    private Reader sourceReader;
    private char[] sourceBuffer;
    private int sourceEnd;
    private int sourceCursor;

    // for xml tokenizer
    private boolean xmlIsAttribute;
    private boolean xmlIsTagContent;
    private int xmlOpenTagsCount;

    private Parser1 parser;
}
