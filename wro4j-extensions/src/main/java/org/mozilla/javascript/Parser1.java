/* -*- Mode: java; tab-width: 8; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 *
 * ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is Rhino code, released
 * May 6, 1999.
 *
 * The Initial Developer of the Original Code is
 * Netscape Communications Corporation.
 * Portions created by the Initial Developer are Copyright (C) 1997-1999
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 *   Mike Ang
 *   Igor Bukanov
 *   Yuh-Ruey Chen
 *   Ethan Hugg
 *   Bob Jervis
 *   Terry Lucas
 *   Mike McCabe
 *   Milen Nankov
 *   Norris Boyd
 *
 * Alternatively, the contents of this file may be used under the terms of
 * the GNU General Public License Version 2 or later (the "GPL"), in which
 * case the provisions of the GPL are applicable instead of those above. If
 * you wish to allow use of your version of this file only under the terms of
 * the GPL and not to allow others to use your version of this file under the
 * MPL, indicate your decision by deleting the provisions above and replacing
 * them with the notice and other provisions required by the GPL. If you do
 * not delete the provisions above, a recipient may use your version of this
 * file under either the MPL or the GPL.
 *
 * ***** END LICENSE BLOCK ***** */

package org.mozilla.javascript;

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

/**
 * This class implements the JavaScript parser.
 *
 * It is based on the C source files jsparse.c and jsparse.h
 * in the jsref package.
 *
 * @see TokenStream1
 *
 * @author Mike McCabe
 * @author Brendan Eich
 */

public class Parser1
{
    // TokenInformation flags : currentFlaggedToken stores them together
    // with token type
    final static int
        CLEAR_TI_MASK  = 0xFFFF,   // mask to clear token information bits
        TI_AFTER_EOL   = 1 << 16,  // first token of the source line
        TI_CHECK_LABEL = 1 << 17;  // indicates to check for label

    CompilerEnvirons compilerEnv;
    private ErrorReporter errorReporter;
    private String sourceURI;
    boolean calledByCompileFunction;

    private TokenStream1 ts;
    private int currentFlaggedToken;
    private int syntaxErrorCount;

    private IRFactory nf;

    private int nestingOfFunction;

    private Decompiler decompiler;
    private String encodedSource;

// The following are per function variables and should be saved/restored
// during function parsing.
// XXX Move to separated class?
    ScriptOrFnNode currentScriptOrFn;
    Node.Scope currentScope;
    private int nestingOfWith;
    private Map<String,Node> labelSet; // map of label names into nodes
    private ObjArray loopSet;
    private ObjArray loopAndSwitchSet;
    private int endFlags;
// end of per function variables

    public int getCurrentLineNumber() {
        return ts.getLineno();
    }

    // Exception to unwind
    private static class ParserException extends RuntimeException
    {
        static final long serialVersionUID = 5882582646773765630L;
    }

    public Parser1(final CompilerEnvirons compilerEnv, final ErrorReporter errorReporter)
    {
        this.compilerEnv = compilerEnv;
        this.errorReporter = errorReporter;
    }

    protected Decompiler createDecompiler(final CompilerEnvirons compilerEnv)
    {
        return new Decompiler();
    }

    void addStrictWarning(final String messageId, final String messageArg)
    {
        if (compilerEnv.isStrictMode())
            addWarning(messageId, messageArg);
    }

    void addWarning(final String messageId, final String messageArg)
    {
        final String message = ScriptRuntime.getMessage1(messageId, messageArg);
        if (compilerEnv.reportWarningAsError()) {
            ++syntaxErrorCount;
            errorReporter.error(message, sourceURI, ts.getLineno(),
                                ts.getLine(), ts.getOffset());
        } else
            errorReporter.warning(message, sourceURI, ts.getLineno(),
                                  ts.getLine(), ts.getOffset());
    }

    void addError(final String messageId)
    {
        ++syntaxErrorCount;
        final String message = ScriptRuntime.getMessage0(messageId);
        errorReporter.error(message, sourceURI, ts.getLineno(),
                            ts.getLine(), ts.getOffset());
    }

    void addError(final String messageId, final String messageArg)
    {
        ++syntaxErrorCount;
        final String message = ScriptRuntime.getMessage1(messageId, messageArg);
        errorReporter.error(message, sourceURI, ts.getLineno(),
                            ts.getLine(), ts.getOffset());
    }

    RuntimeException reportError(final String messageId)
    {
        addError(messageId);

        // Throw a ParserException exception to unwind the recursive descent
        // parse.
        throw new ParserException();
    }

    private int peekToken()
        throws IOException
    {
        int tt = currentFlaggedToken;
        if (tt == Token1.EOF) {
            tt = ts.getToken();
            if (tt == Token1.EOL) {
                do {
                    tt = ts.getToken();
                } while (tt == Token1.EOL);
                tt |= TI_AFTER_EOL;
            }
            currentFlaggedToken = tt;
        }
        return tt & CLEAR_TI_MASK;
    }

    private int peekFlaggedToken()
        throws IOException
    {
        peekToken();
        return currentFlaggedToken;
    }

    private void consumeToken()
    {
        currentFlaggedToken = Token1.EOF;
    }

    private int nextToken()
        throws IOException
    {
        final int tt = peekToken();
        consumeToken();
        return tt;
    }

    private int nextFlaggedToken()
        throws IOException
    {
        peekToken();
        final int ttFlagged = currentFlaggedToken;
        consumeToken();
        return ttFlagged;
    }

    private boolean matchToken(final int toMatch)
        throws IOException
    {
        final int tt = peekToken();
        if (tt != toMatch) {
            return false;
        }
        consumeToken();
        return true;
    }

    private int peekTokenOrEOL()
        throws IOException
    {
        int tt = peekToken();
        // Check for last peeked token flags
        if ((currentFlaggedToken & TI_AFTER_EOL) != 0) {
            tt = Token1.EOL;
        }
        return tt;
    }

    private void setCheckForLabel()
    {
        if ((currentFlaggedToken & CLEAR_TI_MASK) != Token1.NAME)
            throw Kit.codeBug();
        currentFlaggedToken |= TI_CHECK_LABEL;
    }

    private void mustMatchToken(final int toMatch, final String messageId)
        throws IOException, ParserException
    {
        if (!matchToken(toMatch)) {
            reportError(messageId);
        }
    }

    private void mustHaveXML()
    {
        if (!compilerEnv.isXmlAvailable()) {
            reportError("msg.XML.not.available");
        }
    }

    public String getEncodedSource()
    {
        return encodedSource;
    }

    public boolean eof()
    {
        return ts.eof();
    }

    boolean insideFunction()
    {
        return nestingOfFunction != 0;
    }

    void pushScope(final Node node) {
        final Node.Scope scopeNode = (Node.Scope) node;
        if (scopeNode.getParentScope() != null) throw Kit.codeBug();
        scopeNode.setParent(currentScope);
        currentScope = scopeNode;
    }

    void popScope() {
        currentScope = currentScope.getParentScope();
    }

    private Node enterLoop(final Node loopLabel, final boolean doPushScope)
    {
        final Node loop = nf.createLoopNode(loopLabel, ts.getLineno());
        if (loopSet == null) {
            loopSet = new ObjArray();
            if (loopAndSwitchSet == null) {
                loopAndSwitchSet = new ObjArray();
            }
        }
        loopSet.push(loop);
        loopAndSwitchSet.push(loop);
        if (doPushScope) {
            pushScope(loop);
        }
        return loop;
    }

    private void exitLoop(final boolean doPopScope)
    {
        loopSet.pop();
        loopAndSwitchSet.pop();
        if (doPopScope) {
            popScope();
        }
    }

    private Node enterSwitch(final Node switchSelector, final int lineno)
    {
        final Node switchNode = nf.createSwitch(switchSelector, lineno);
        if (loopAndSwitchSet == null) {
            loopAndSwitchSet = new ObjArray();
        }
        loopAndSwitchSet.push(switchNode);
        return switchNode;
    }

    private void exitSwitch()
    {
        loopAndSwitchSet.pop();
    }

    /*
     * Build a parse tree from the given sourceString.
     *
     * @return an Object representing the parsed
     * program.  If the parse fails, null will be returned.  (The
     * parse failure will result in a call to the ErrorReporter from
     * CompilerEnvirons.)
     */
    public ScriptOrFnNode parse(final String sourceString,
                                final String sourceURI, final int lineno)
    {
        this.sourceURI = sourceURI;
        this.ts = new TokenStream1(this, null, sourceString, lineno);
        try {
            return parse();
        } catch (final IOException ex) {
            // Should never happen
            throw new IllegalStateException();
        }
    }

    /*
     * Build a parse tree from the given sourceString.
     *
     * @return an Object representing the parsed
     * program.  If the parse fails, null will be returned.  (The
     * parse failure will result in a call to the ErrorReporter from
     * CompilerEnvirons.)
     */
    public ScriptOrFnNode parse(final Reader sourceReader,
                                final String sourceURI, final int lineno)
        throws IOException
    {
        this.sourceURI = sourceURI;
        this.ts = new TokenStream1(this, sourceReader, null, lineno);
        return parse();
    }

    private ScriptOrFnNode parse()
        throws IOException
    {
        this.decompiler = createDecompiler(compilerEnv);
        //this.nf = new IRFactory(this);
        this.nf = new IRFactory(new Parser(compilerEnv, errorReporter));
        currentScriptOrFn = nf.createScript();
        currentScope = currentScriptOrFn;
        final int sourceStartOffset = decompiler.getCurrentOffset();
        this.encodedSource = null;
        decompiler.addToken(Token1.SCRIPT);

        this.currentFlaggedToken = Token1.EOF;
        this.syntaxErrorCount = 0;

        final int baseLineno = ts.getLineno();  // line number where source starts

        /* so we have something to add nodes to until
         * we've collected all the source */
        final Node pn = nf.createLeaf(Token1.BLOCK);

        try {
            for (;;) {
                final int tt = peekToken();

                if (tt <= Token1.EOF) {
                    break;
                }

                Node n;
                if (tt == Token1.FUNCTION) {
                    consumeToken();
                    try {
                        n = function(calledByCompileFunction
                                     ? FunctionNode.FUNCTION_EXPRESSION
                                     : FunctionNode.FUNCTION_STATEMENT);
                    } catch (final ParserException e) {
                        break;
                    }
                } else {
                    n = statement();
                }
                nf.addChildToBack(pn, n);
            }
        } catch (final StackOverflowError ex) {
            final String msg = ScriptRuntime.getMessage0(
                "msg.too.deep.parser.recursion");
            throw Context.reportRuntimeError(msg, sourceURI,
                                             ts.getLineno(), null, 0);
        }

        if (this.syntaxErrorCount != 0) {
            String msg = String.valueOf(this.syntaxErrorCount);
            msg = ScriptRuntime.getMessage1("msg.got.syntax.errors", msg);
            throw errorReporter.runtimeError(msg, sourceURI, baseLineno,
                                             null, 0);
        }

        currentScriptOrFn.setSourceName(sourceURI);
        currentScriptOrFn.setBaseLineno(baseLineno);
        currentScriptOrFn.setEndLineno(ts.getLineno());

        final int sourceEndOffset = decompiler.getCurrentOffset();
        currentScriptOrFn.setEncodedSourceBounds(sourceStartOffset,
                                                 sourceEndOffset);

        nf.initScript(currentScriptOrFn, pn);

        if (compilerEnv.isGeneratingSource()) {
            encodedSource = decompiler.getEncodedSource();
        }
        this.decompiler = null; // It helps GC

        return currentScriptOrFn;
    }

    /*
     * The C version of this function takes an argument list,
     * which doesn't seem to be needed for tree generation...
     * it'd only be useful for checking argument hiding, which
     * I'm not doing anyway...
     */
    private Node parseFunctionBody()
        throws IOException
    {
        ++nestingOfFunction;
        final Node pn = nf.createBlock(ts.getLineno());
        try {
            bodyLoop: for (;;) {
                Node n;
                final int tt = peekToken();
                switch (tt) {
                  case Token1.ERROR:
                  case Token1.EOF:
                  case Token1.RC:
                    break bodyLoop;

                  case Token1.FUNCTION:
                    consumeToken();
                    n = function(FunctionNode.FUNCTION_STATEMENT);
                    break;
                  default:
                    n = statement();
                    break;
                }
                nf.addChildToBack(pn, n);
            }
        } catch (final ParserException e) {
            // Ignore it
        } finally {
            --nestingOfFunction;
        }

        return pn;
    }

    private Node function(final int functionType)
        throws IOException, ParserException
    {
        int syntheticType = functionType;
        final int baseLineno = ts.getLineno();  // line number where source starts

        final int functionSourceStart = decompiler.markFunctionStart(functionType);
        String name;
        Node memberExprNode = null;
        if (matchToken(Token1.NAME)) {
            name = ts.getString();
            decompiler.addName(name);
            if (!matchToken(Token1.LP)) {
                if (compilerEnv.isAllowMemberExprAsFunctionName()) {
                    // Extension to ECMA: if 'function <name>' does not follow
                    // by '(', assume <name> starts memberExpr
                    final Node memberExprHead = nf.createName(name);
                    name = "";
                    memberExprNode = memberExprTail(false, memberExprHead);
                }
                mustMatchToken(Token1.LP, "msg.no.paren.parms");
            }
        } else if (matchToken(Token1.LP)) {
            // Anonymous function
            name = "";
        } else {
            name = "";
            if (compilerEnv.isAllowMemberExprAsFunctionName()) {
                // Note that memberExpr can not start with '(' like
                // in function (1+2).toString(), because 'function (' already
                // processed as anonymous function
                memberExprNode = memberExpr(false);
            }
            mustMatchToken(Token1.LP, "msg.no.paren.parms");
        }

        if (memberExprNode != null) {
            syntheticType = FunctionNode.FUNCTION_EXPRESSION;
        }

        if (syntheticType != FunctionNode.FUNCTION_EXPRESSION &&
            name.length() > 0)
        {
            // Function statements define a symbol in the enclosing scope
            defineSymbol(Token1.FUNCTION, false, name);
        }

        final boolean nested = insideFunction();

        final FunctionNode fnNode = nf.createFunction(name);
        if (nested || nestingOfWith > 0) {
            // 1. Nested functions are not affected by the dynamic scope flag
            // as dynamic scope is already a parent of their scope.
            // 2. Functions defined under the with statement also immune to
            // this setup, in which case dynamic scope is ignored in favor
            // of with object.
            fnNode.itsIgnoreDynamicScope = true;
        }
        final int functionIndex = currentScriptOrFn.addFunction(fnNode);

        int functionSourceEnd;

        final ScriptOrFnNode savedScriptOrFn = currentScriptOrFn;
        currentScriptOrFn = fnNode;
        final Node.Scope savedCurrentScope = currentScope;
        currentScope = fnNode;
        final int savedNestingOfWith = nestingOfWith;
        nestingOfWith = 0;
        final Map<String,Node> savedLabelSet = labelSet;
        labelSet = null;
        final ObjArray savedLoopSet = loopSet;
        loopSet = null;
        final ObjArray savedLoopAndSwitchSet = loopAndSwitchSet;
        loopAndSwitchSet = null;
        final int savedFunctionEndFlags = endFlags;
        endFlags = 0;

        Node destructuring = null;
        Node body;
        try {
            decompiler.addToken(Token1.LP);
            if (!matchToken(Token1.RP)) {
                boolean first = true;
                do {
                    if (!first)
                        decompiler.addToken(Token1.COMMA);
                    first = false;
                    final int tt = peekToken();
                    if (tt == Token1.LB || tt == Token1.LC) {
                        // Destructuring assignment for parameters: add a
                        // dummy parameter name, and add a statement to the
                        // body to initialize variables from the destructuring
                        // assignment
                        if (destructuring == null) {
                            destructuring = new Node(Token1.COMMA);
                        }
                        final String parmName = currentScriptOrFn.getNextTempName();
                        defineSymbol(Token1.LP, false, parmName);
                        destructuring.addChildToBack(
                            nf.createDestructuringAssignment(Token1.VAR,
                                primaryExpr(), nf.createName(parmName)));
                    } else {
                        mustMatchToken(Token1.NAME, "msg.no.parm");
                        final String s = ts.getString();
                        defineSymbol(Token1.LP, false, s);
                        decompiler.addName(s);
                    }
                } while (matchToken(Token1.COMMA));

                mustMatchToken(Token1.RP, "msg.no.paren.after.parms");
            }
            decompiler.addToken(Token1.RP);

            mustMatchToken(Token1.LC, "msg.no.brace.body");
            decompiler.addEOL(Token1.LC);
            body = parseFunctionBody();
            if (destructuring != null) {
                body.addChildToFront(
                    new Node(Token1.EXPR_VOID, destructuring, ts.getLineno()));
            }
            mustMatchToken(Token1.RC, "msg.no.brace.after.body");

            if (compilerEnv.isStrictMode() && !body.hasConsistentReturnUsage())
            {
              final String msg = name.length() > 0 ? "msg.no.return.value"
                                             : "msg.anon.no.return.value";
              addStrictWarning(msg, name);
            }

            if (syntheticType == FunctionNode.FUNCTION_EXPRESSION &&
                name.length() > 0 && currentScope.getSymbol(name) == null)
            {
                // Function expressions define a name only in the body of the
                // function, and only if not hidden by a parameter name
                defineSymbol(Token1.FUNCTION, false, name);
            }

            decompiler.addToken(Token1.RC);
            functionSourceEnd = decompiler.markFunctionEnd(functionSourceStart);
            if (functionType != FunctionNode.FUNCTION_EXPRESSION) {
                // Add EOL only if function is not part of expression
                // since it gets SEMI + EOL from Statement in that case
                decompiler.addToken(Token1.EOL);
            }
        }
        finally {
            endFlags = savedFunctionEndFlags;
            loopAndSwitchSet = savedLoopAndSwitchSet;
            loopSet = savedLoopSet;
            labelSet = savedLabelSet;
            nestingOfWith = savedNestingOfWith;
            currentScriptOrFn = savedScriptOrFn;
            currentScope = savedCurrentScope;
        }

        fnNode.setEncodedSourceBounds(functionSourceStart, functionSourceEnd);
        fnNode.setSourceName(sourceURI);
        fnNode.setBaseLineno(baseLineno);
        fnNode.setEndLineno(ts.getLineno());

        Node pn = nf.initFunction(fnNode, functionIndex, body, syntheticType);
        if (memberExprNode != null) {
            pn = nf.createAssignment(Token1.ASSIGN, memberExprNode, pn);
            if (functionType != FunctionNode.FUNCTION_EXPRESSION) {
                // XXX check JScript behavior: should it be createExprStatement?
                pn = nf.createExprStatementNoReturn(pn, baseLineno);
            }
        }
        return pn;
    }

    private Node statements(final Node scope)
        throws IOException
    {
        final Node pn = scope != null ? scope : nf.createBlock(ts.getLineno());

        int tt;
        while ((tt = peekToken()) > Token1.EOF && tt != Token1.RC) {
            nf.addChildToBack(pn, statement());
        }

        return pn;
    }

    private Node condition()
        throws IOException, ParserException
    {
        mustMatchToken(Token1.LP, "msg.no.paren.cond");
        decompiler.addToken(Token1.LP);
        final Node pn = expr(false);
        mustMatchToken(Token1.RP, "msg.no.paren.after.cond");
        decompiler.addToken(Token1.RP);

        // Report strict warning on code like "if (a = 7) ...". Suppress the
        // warning if the condition is parenthesized, like "if ((a = 7)) ...".
        if (pn.getProp(Node.PARENTHESIZED_PROP) == null &&
            (pn.getType() == Token1.SETNAME || pn.getType() == Token1.SETPROP ||
             pn.getType() == Token1.SETELEM))
        {
            addStrictWarning("msg.equal.as.assign", "");
        }
        return pn;
    }

    // match a NAME; return null if no match.
    private Node matchJumpLabelName()
        throws IOException, ParserException
    {
        Node label = null;

        final int tt = peekTokenOrEOL();
        if (tt == Token1.NAME) {
            consumeToken();
            final String name = ts.getString();
            decompiler.addName(name);
            if (labelSet != null) {
                label = labelSet.get(name);
            }
            if (label == null) {
                reportError("msg.undef.label");
            }
        }

        return label;
    }

    private Node statement()
        throws IOException
    {
        try {
            final Node pn = statementHelper(null);
            if (pn != null) {
                if (compilerEnv.isStrictMode() && !pn.hasSideEffects())
                    addStrictWarning("msg.no.side.effects", "");
                return pn;
            }
        } catch (final ParserException e) { }

        // skip to end of statement
        final int lineno = ts.getLineno();
        guessingStatementEnd: for (;;) {
            final int tt = peekTokenOrEOL();
            consumeToken();
            switch (tt) {
              case Token1.ERROR:
              case Token1.EOF:
              case Token1.EOL:
              case Token1.SEMI:
                break guessingStatementEnd;
            }
        }
        return nf.createExprStatement(nf.createName("error"), lineno);
    }

    private Node statementHelper(Node statementLabel)
        throws IOException, ParserException
    {
        Node pn = null;
        int tt = peekToken();

        switch (tt) {
          case Token1.IF: {
            consumeToken();

            decompiler.addToken(Token1.IF);
            final int lineno = ts.getLineno();
            final Node cond = condition();
            decompiler.addEOL(Token1.LC);
            final Node ifTrue = statement();
            Node ifFalse = null;
            if (matchToken(Token1.ELSE)) {
                decompiler.addToken(Token1.RC);
                decompiler.addToken(Token1.ELSE);
                decompiler.addEOL(Token1.LC);
                ifFalse = statement();
            }
            decompiler.addEOL(Token1.RC);
            pn = nf.createIf(cond, ifTrue, ifFalse, lineno);
            return pn;
          }

          case Token1.SWITCH: {
            consumeToken();

            decompiler.addToken(Token1.SWITCH);
            final int lineno = ts.getLineno();
            mustMatchToken(Token1.LP, "msg.no.paren.switch");
            decompiler.addToken(Token1.LP);
            pn = enterSwitch(expr(false), lineno);
            try {
                mustMatchToken(Token1.RP, "msg.no.paren.after.switch");
                decompiler.addToken(Token1.RP);
                mustMatchToken(Token1.LC, "msg.no.brace.switch");
                decompiler.addEOL(Token1.LC);

                boolean hasDefault = false;
                switchLoop: for (;;) {
                    tt = nextToken();
                    Node caseExpression;
                    switch (tt) {
                      case Token1.RC:
                        break switchLoop;

                      case Token1.CASE:
                        decompiler.addToken(Token1.CASE);
                        caseExpression = expr(false);
                        mustMatchToken(Token1.COLON, "msg.no.colon.case");
                        decompiler.addEOL(Token1.COLON);
                        break;

                      case Token1.DEFAULT:
                        if (hasDefault) {
                            reportError("msg.double.switch.default");
                        }
                        decompiler.addToken(Token1.DEFAULT);
                        hasDefault = true;
                        caseExpression = null;
                        mustMatchToken(Token1.COLON, "msg.no.colon.case");
                        decompiler.addEOL(Token1.COLON);
                        break;

                      default:
                        reportError("msg.bad.switch");
                        break switchLoop;
                    }

                    final Node block = nf.createLeaf(Token1.BLOCK);
                    while ((tt = peekToken()) != Token1.RC
                           && tt != Token1.CASE
                           && tt != Token1.DEFAULT
                           && tt != Token1.EOF)
                    {
                        nf.addChildToBack(block, statement());
                    }

                    // caseExpression == null => add default label
                    nf.addSwitchCase(pn, caseExpression, block);
                }
                decompiler.addEOL(Token1.RC);
                nf.closeSwitch(pn);
            } finally {
                exitSwitch();
            }
            return pn;
          }

          case Token1.WHILE: {
            consumeToken();
            decompiler.addToken(Token1.WHILE);

            final Node loop = enterLoop(statementLabel, true);
            try {
                final Node cond = condition();
                decompiler.addEOL(Token1.LC);
                final Node body = statement();
                decompiler.addEOL(Token1.RC);
                pn = nf.createWhile(loop, cond, body);
            } finally {
                exitLoop(true);
            }
            return pn;
          }

          case Token1.DO: {
            consumeToken();
            decompiler.addToken(Token1.DO);
            decompiler.addEOL(Token1.LC);

            final Node loop = enterLoop(statementLabel, true);
            try {
                final Node body = statement();
                decompiler.addToken(Token1.RC);
                mustMatchToken(Token1.WHILE, "msg.no.while.do");
                decompiler.addToken(Token1.WHILE);
                final Node cond = condition();
                pn = nf.createDoWhile(loop, body, cond);
            } finally {
                exitLoop(true);
            }
            // Always auto-insert semicolon to follow SpiderMonkey:
            // It is required by ECMAScript but is ignored by the rest of
            // world, see bug 238945
            matchToken(Token1.SEMI);
            decompiler.addEOL(Token1.SEMI);
            return pn;
          }

          case Token1.FOR: {
            consumeToken();
            boolean isForEach = false;
            decompiler.addToken(Token1.FOR);

            final Node loop = enterLoop(statementLabel, true);
            try {
                Node init;  // Node init is also foo in 'foo in object'
                Node cond;  // Node cond is also object in 'foo in object'
                Node incr = null;
                Node body;
                int declType = -1;

                // See if this is a for each () instead of just a for ()
                if (matchToken(Token1.NAME)) {
                    decompiler.addName(ts.getString());
                    if (ts.getString().equals("each")) {
                        isForEach = true;
                    } else {
                        reportError("msg.no.paren.for");
                    }
                }

                mustMatchToken(Token1.LP, "msg.no.paren.for");
                decompiler.addToken(Token1.LP);
                tt = peekToken();
                if (tt == Token1.SEMI) {
                    init = nf.createLeaf(Token1.EMPTY);
                } else {
                    if (tt == Token1.VAR || tt == Token1.LET) {
                        // set init to a var list or initial
                        consumeToken();    // consume the token
                        decompiler.addToken(tt);
                        init = variables(true, tt);
                        declType = tt;
                    }
                    else {
                        init = expr(true);
                    }
                }

                if (matchToken(Token1.IN)) {
                    decompiler.addToken(Token1.IN);
                    // 'cond' is the object over which we're iterating
                    cond = expr(false);
                } else {  // ordinary for loop
                    mustMatchToken(Token1.SEMI, "msg.no.semi.for");
                    decompiler.addToken(Token1.SEMI);
                    if (peekToken() == Token1.SEMI) {
                        // no loop condition
                        cond = nf.createLeaf(Token1.EMPTY);
                    } else {
                        cond = expr(false);
                    }

                    mustMatchToken(Token1.SEMI, "msg.no.semi.for.cond");
                    decompiler.addToken(Token1.SEMI);
                    if (peekToken() == Token1.RP) {
                        incr = nf.createLeaf(Token1.EMPTY);
                    } else {
                        incr = expr(false);
                    }
                }

                mustMatchToken(Token1.RP, "msg.no.paren.for.ctrl");
                decompiler.addToken(Token1.RP);
                decompiler.addEOL(Token1.LC);
                body = statement();
                decompiler.addEOL(Token1.RC);

                if (incr == null) {
                    // cond could be null if 'in obj' got eaten
                    // by the init node.
                    pn = nf.createForIn(declType, loop, init, cond, body,
                                        isForEach);
                } else {
                    pn = nf.createFor(loop, init, cond, incr, body);
                }
            } finally {
                exitLoop(true);
            }
            return pn;
          }

          case Token1.TRY: {
            consumeToken();
            final int lineno = ts.getLineno();

            Node tryblock;
            Node catchblocks = null;
            Node finallyblock = null;

            decompiler.addToken(Token1.TRY);
            if (peekToken() != Token1.LC) {
                reportError("msg.no.brace.try");
            }
            decompiler.addEOL(Token1.LC);
            tryblock = statement();
            decompiler.addEOL(Token1.RC);

            catchblocks = nf.createLeaf(Token1.BLOCK);

            boolean sawDefaultCatch = false;
            final int peek = peekToken();
            if (peek == Token1.CATCH) {
                while (matchToken(Token1.CATCH)) {
                    if (sawDefaultCatch) {
                        reportError("msg.catch.unreachable");
                    }
                    decompiler.addToken(Token1.CATCH);
                    mustMatchToken(Token1.LP, "msg.no.paren.catch");
                    decompiler.addToken(Token1.LP);

                    mustMatchToken(Token1.NAME, "msg.bad.catchcond");
                    final String varName = ts.getString();
                    decompiler.addName(varName);

                    Node catchCond = null;
                    if (matchToken(Token1.IF)) {
                        decompiler.addToken(Token1.IF);
                        catchCond = expr(false);
                    } else {
                        sawDefaultCatch = true;
                    }

                    mustMatchToken(Token1.RP, "msg.bad.catchcond");
                    decompiler.addToken(Token1.RP);
                    mustMatchToken(Token1.LC, "msg.no.brace.catchblock");
                    decompiler.addEOL(Token1.LC);

                    nf.addChildToBack(catchblocks,
                        nf.createCatch(varName, catchCond,
                                       statements(null),
                                       ts.getLineno()));

                    mustMatchToken(Token1.RC, "msg.no.brace.after.body");
                    decompiler.addEOL(Token1.RC);
                }
            } else if (peek != Token1.FINALLY) {
                mustMatchToken(Token1.FINALLY, "msg.try.no.catchfinally");
            }

            if (matchToken(Token1.FINALLY)) {
                decompiler.addToken(Token1.FINALLY);
                decompiler.addEOL(Token1.LC);
                finallyblock = statement();
                decompiler.addEOL(Token1.RC);
            }

            pn = nf.createTryCatchFinally(tryblock, catchblocks,
                                          finallyblock, lineno);

            return pn;
          }

          case Token1.THROW: {
            consumeToken();
            if (peekTokenOrEOL() == Token1.EOL) {
                // ECMAScript does not allow new lines before throw expression,
                // see bug 256617
                reportError("msg.bad.throw.eol");
            }

            final int lineno = ts.getLineno();
            decompiler.addToken(Token1.THROW);
            pn = nf.createThrow(expr(false), lineno);
            break;
          }

          case Token1.BREAK: {
            consumeToken();
            final int lineno = ts.getLineno();

            decompiler.addToken(Token1.BREAK);

            // matchJumpLabelName only matches if there is one
            Node breakStatement = matchJumpLabelName();
            if (breakStatement == null) {
                if (loopAndSwitchSet == null || loopAndSwitchSet.size() == 0) {
                    reportError("msg.bad.break");
                    return null;
                }
                breakStatement = (Node)loopAndSwitchSet.peek();
            }
            pn = nf.createBreak(breakStatement, lineno);
            break;
          }

          case Token1.CONTINUE: {
            consumeToken();
            final int lineno = ts.getLineno();

            decompiler.addToken(Token1.CONTINUE);

            Node loop;
            // matchJumpLabelName only matches if there is one
            final Node label = matchJumpLabelName();
            if (label == null) {
                if (loopSet == null || loopSet.size() == 0) {
                    reportError("msg.continue.outside");
                    return null;
                }
                loop = (Node)loopSet.peek();
            } else {
                loop = nf.getLabelLoop(label);
                if (loop == null) {
                    reportError("msg.continue.nonloop");
                    return null;
                }
            }
            pn = nf.createContinue(loop, lineno);
            break;
          }

          case Token1.WITH: {
            consumeToken();

            decompiler.addToken(Token1.WITH);
            final int lineno = ts.getLineno();
            mustMatchToken(Token1.LP, "msg.no.paren.with");
            decompiler.addToken(Token1.LP);
            final Node obj = expr(false);
            mustMatchToken(Token1.RP, "msg.no.paren.after.with");
            decompiler.addToken(Token1.RP);
            decompiler.addEOL(Token1.LC);

            ++nestingOfWith;
            Node body;
            try {
                body = statement();
            } finally {
                --nestingOfWith;
            }

            decompiler.addEOL(Token1.RC);

            pn = nf.createWith(obj, body, lineno);
            return pn;
          }

          case Token1.CONST:
          case Token1.VAR: {
            consumeToken();
            decompiler.addToken(tt);
            pn = variables(false, tt);
            break;
          }

          case Token1.LET: {
            consumeToken();
            decompiler.addToken(Token1.LET);
            if (peekToken() == Token1.LP) {
                return let(true);
            } else {
                pn = variables(false, tt);
                if (peekToken() == Token1.SEMI)
                    break;
                return pn;
            }
          }

          case Token1.RETURN:
          case Token1.YIELD: {
            pn = returnOrYield(tt, false);
            break;
          }

          case Token1.DEBUGGER:
            consumeToken();
            decompiler.addToken(Token1.DEBUGGER);
            pn = nf.createDebugger(ts.getLineno());
            break;

          case Token1.LC:
            consumeToken();
            if (statementLabel != null) {
                decompiler.addToken(Token1.LC);
            }
            final Node scope = nf.createScopeNode(Token1.BLOCK, ts.getLineno());
            pushScope(scope);
            try {
                statements(scope);
                mustMatchToken(Token1.RC, "msg.no.brace.block");
                if (statementLabel != null) {
                    decompiler.addEOL(Token1.RC);
                }
                return scope;
            } finally {
                popScope();
            }

          case Token1.ERROR:
            // Fall thru, to have a node for error recovery to work on
          case Token1.SEMI:
            consumeToken();
            pn = nf.createLeaf(Token1.EMPTY);
            return pn;

          case Token1.FUNCTION: {
            consumeToken();
            pn = function(FunctionNode.FUNCTION_EXPRESSION_STATEMENT);
            return pn;
          }

          case Token1.DEFAULT :
            consumeToken();
            mustHaveXML();

            decompiler.addToken(Token1.DEFAULT);
            final int nsLine = ts.getLineno();

            if (!(matchToken(Token1.NAME)
                  && ts.getString().equals("xml")))
            {
                reportError("msg.bad.namespace");
            }
            decompiler.addName(" xml");

            if (!(matchToken(Token1.NAME)
                  && ts.getString().equals("namespace")))
            {
                reportError("msg.bad.namespace");
            }
            decompiler.addName(" namespace");

            if (!matchToken(Token1.ASSIGN)) {
                reportError("msg.bad.namespace");
            }
            decompiler.addToken(Token1.ASSIGN);

            final Node expr = expr(false);
            pn = nf.createDefaultNamespace(expr, nsLine);
            break;

          case Token1.NAME: {
            final int lineno = ts.getLineno();
            final String name = ts.getString();
            setCheckForLabel();
            pn = expr(false);
            if (pn.getType() != Token1.LABEL) {
                pn = nf.createExprStatement(pn, lineno);
            } else {
                // Parsed the label: push back token should be
                // colon that primaryExpr left untouched.
                if (peekToken() != Token1.COLON) Kit.codeBug();
                consumeToken();
                // depend on decompiling lookahead to guess that that
                // last name was a label.
                decompiler.addName(name);
                decompiler.addEOL(Token1.COLON);

                if (labelSet == null) {
                    labelSet = new HashMap<String,Node>();
                } else if (labelSet.containsKey(name)) {
                    reportError("msg.dup.label");
                }

                boolean firstLabel;
                if (statementLabel == null) {
                    firstLabel = true;
                    statementLabel = pn;
                } else {
                    // Discard multiple label nodes and use only
                    // the first: it allows to simplify IRFactory
                    firstLabel = false;
                }
                labelSet.put(name, statementLabel);
                try {
                    pn = statementHelper(statementLabel);
                } finally {
                    labelSet.remove(name);
                }
                if (firstLabel) {
                    pn = nf.createLabeledStatement(statementLabel, pn);
                }
                return pn;
            }
            break;
          }

          default: {
            final int lineno = ts.getLineno();
            pn = expr(false);
            pn = nf.createExprStatement(pn, lineno);
            break;
          }
        }

        final int ttFlagged = peekFlaggedToken();
        switch (ttFlagged & CLEAR_TI_MASK) {
          case Token1.SEMI:
            // Consume ';' as a part of expression
            consumeToken();
            break;
          case Token1.ERROR:
          case Token1.EOF:
          case Token1.RC:
            // Autoinsert ;
            break;
          default:
            if ((ttFlagged & TI_AFTER_EOL) == 0) {
                // Report error if no EOL or autoinsert ; otherwise
                reportError("msg.no.semi.stmt");
            }
            break;
        }
        decompiler.addEOL(Token1.SEMI);

        return pn;
    }

    /**
     * Returns whether or not the bits in the mask have changed to all set.
     * @param before bits before change
     * @param after bits after change
     * @param mask mask for bits
     * @return true if all the bits in the mask are set in "after" but not
     *              "before"
     */
    private static final boolean nowAllSet(final int before, final int after, final int mask)
    {
        return ((before & mask) != mask) && ((after & mask) == mask);
    }

    private Node returnOrYield(final int tt, final boolean exprContext)
        throws IOException, ParserException
    {
        if (!insideFunction()) {
            reportError(tt == Token1.RETURN ? "msg.bad.return"
                                           : "msg.bad.yield");
        }
        consumeToken();
        decompiler.addToken(tt);
        final int lineno = ts.getLineno();

        Node e;
        /* This is ugly, but we don't want to require a semicolon. */
        switch (peekTokenOrEOL()) {
          case Token1.SEMI:
          case Token1.RC:
          case Token1.EOF:
          case Token1.EOL:
          case Token1.ERROR:
          case Token1.RB:
          case Token1.RP:
          case Token1.YIELD:
            e = null;
            break;
          default:
            e = expr(false);
            break;
        }

        final int before = endFlags;
        Node ret;

        if (tt == Token1.RETURN) {
            if (e == null ) {
                endFlags |= Node.END_RETURNS;
            } else {
                endFlags |= Node.END_RETURNS_VALUE;
            }
            ret = nf.createReturn(e, lineno);

            // see if we need a strict mode warning
            if (nowAllSet(before, endFlags,
                          Node.END_RETURNS|Node.END_RETURNS_VALUE))
            {
                addStrictWarning("msg.return.inconsistent", "");
            }
        } else {
            endFlags |= Node.END_YIELDS;
            ret = nf.createYield(e, lineno);
            if (!exprContext)
                ret = new Node(Token1.EXPR_VOID, ret, lineno);
        }

        // see if we are mixing yields and value returns.
        if (nowAllSet(before, endFlags,
                      Node.END_YIELDS|Node.END_RETURNS_VALUE))
        {
            final String name = ((FunctionNode)currentScriptOrFn).getFunctionName();
            if (name.length() == 0)
                addError("msg.anon.generator.returns", "");
            else
                addError("msg.generator.returns", name);
        }

        return ret;
    }

    /**
     * Parse a 'var' or 'const' statement, or a 'var' init list in a for
     * statement.
     * @param inFor true if we are currently in the midst of the init
     * clause of a for.
     * @param declType A token value: either VAR, CONST, or LET depending on
     * context.
     * @return The parsed statement
     * @throws IOException
     * @throws ParserException
     */
    private Node variables(final boolean inFor, final int declType)
        throws IOException, ParserException
    {
        final Node result = nf.createVariables(declType, ts.getLineno());
        boolean first = true;
        for (;;) {
            Node destructuring = null;
            String s = null;
            final int tt = peekToken();
            if (tt == Token1.LB || tt == Token1.LC) {
                // Destructuring assignment, e.g., var [a,b] = ...
                destructuring = primaryExpr();
            } else {
                // Simple variable name
                mustMatchToken(Token1.NAME, "msg.bad.var");
                s = ts.getString();

                if (!first)
                    decompiler.addToken(Token1.COMMA);
                first = false;

                decompiler.addName(s);
                defineSymbol(declType, inFor, s);
            }

            Node init = null;
            if (matchToken(Token1.ASSIGN)) {
                decompiler.addToken(Token1.ASSIGN);
                init = assignExpr(inFor);
            }

            if (destructuring != null) {
                if (init == null) {
                    if (!inFor)
                        reportError("msg.destruct.assign.no.init");
                    nf.addChildToBack(result, destructuring);
                } else {
                    nf.addChildToBack(result,
                        nf.createDestructuringAssignment(declType,
                            destructuring, init));
                }
            } else {
                final Node name = nf.createName(s);
                if (init != null)
                    nf.addChildToBack(name, init);
                nf.addChildToBack(result, name);
            }

            if (!matchToken(Token1.COMMA))
                break;
        }
        return result;
    }


    private Node let(final boolean isStatement)
        throws IOException, ParserException
    {
        mustMatchToken(Token1.LP, "msg.no.paren.after.let");
        decompiler.addToken(Token1.LP);
        Node result = nf.createScopeNode(Token1.LET, ts.getLineno());
        pushScope(result);
        try {
              final Node vars = variables(false, Token1.LET);
              nf.addChildToBack(result, vars);
              mustMatchToken(Token1.RP, "msg.no.paren.let");
              decompiler.addToken(Token1.RP);
              if (isStatement && peekToken() == Token1.LC) {
                  // let statement
                  consumeToken();
                  decompiler.addEOL(Token1.LC);
                  nf.addChildToBack(result, statements(null));
                  mustMatchToken(Token1.RC, "msg.no.curly.let");
                  decompiler.addToken(Token1.RC);
              } else {
                  // let expression
                  result.setType(Token1.LETEXPR);
                  nf.addChildToBack(result, expr(false));
                  if (isStatement) {
                      // let expression in statement context
                      result = nf.createExprStatement(result, ts.getLineno());
                  }
              }
        } finally {
            popScope();
        }
        return result;
    }

    void defineSymbol(final int declType, final boolean ignoreNotInBlock, final String name) {
        final Node.Scope definingScope = currentScope.getDefiningScope(name);
        final Node.Scope.Symbol symbol = definingScope != null
                                  ? definingScope.getSymbol(name)
                                  : null;
        boolean error = false;
        if (symbol != null && (symbol.declType == Token1.CONST ||
            declType == Token1.CONST))
        {
            error = true;
        } else {
            switch (declType) {
              case Token1.LET:
                if (symbol != null && definingScope == currentScope) {
                    error = symbol.declType == Token1.LET;
                }
                final int currentScopeType = currentScope.getType();
                if (!ignoreNotInBlock &&
                    ((currentScopeType == Token1.LOOP) ||
                     (currentScopeType == Token1.IF)))
                {
                    addError("msg.let.decl.not.in.block");
                }
                currentScope.putSymbol(name,
                    new Node.Scope.Symbol(declType, name));
                break;

              case Token1.VAR:
              case Token1.CONST:
              case Token1.FUNCTION:
                if (symbol != null) {
                    if (symbol.declType == Token1.VAR)
                        addStrictWarning("msg.var.redecl", name);
                    else if (symbol.declType == Token1.LP) {
                        addStrictWarning("msg.var.hides.arg", name);
                    }
                } else {
                    currentScriptOrFn.putSymbol(name,
                        new Node.Scope.Symbol(declType, name));
                }
                break;

              case Token1.LP:
                if (symbol != null) {
                    // must be duplicate parameter. Second parameter hides the
                    // first, so go ahead and add the second pararameter
                    addWarning("msg.dup.parms", name);
                }
                currentScriptOrFn.putSymbol(name,
                    new Node.Scope.Symbol(declType, name));
                break;

              default:
                throw Kit.codeBug();
            }
        }
        if (error) {
            addError(symbol.declType == Token1.CONST ? "msg.const.redecl" :
                     symbol.declType == Token1.LET ? "msg.let.redecl" :
                     symbol.declType == Token1.VAR ? "msg.var.redecl" :
                     symbol.declType == Token1.FUNCTION ? "msg.fn.redecl" :
                     "msg.parm.redecl", name);
        }
    }

    private Node expr(final boolean inForInit)
        throws IOException, ParserException
    {
        Node pn = assignExpr(inForInit);
        while (matchToken(Token1.COMMA)) {
            decompiler.addToken(Token1.COMMA);
            if (compilerEnv.isStrictMode() && !pn.hasSideEffects())
                addStrictWarning("msg.no.side.effects", "");
            if (peekToken() == Token1.YIELD) {
              reportError("msg.yield.parenthesized");
            }
            pn = nf.createBinary(Token1.COMMA, pn, assignExpr(inForInit));
        }
        return pn;
    }

    private Node assignExpr(final boolean inForInit)
        throws IOException, ParserException
    {
        int tt = peekToken();
        if (tt == Token1.YIELD) {
            consumeToken();
            return returnOrYield(tt, true);
        }
        Node pn = condExpr(inForInit);

        tt = peekToken();
        if (Token1.FIRST_ASSIGN <= tt && tt <= Token1.LAST_ASSIGN) {
            consumeToken();
            decompiler.addToken(tt);
            pn = nf.createAssignment(tt, pn, assignExpr(inForInit));
        }

        return pn;
    }

    private Node condExpr(final boolean inForInit)
        throws IOException, ParserException
    {
        final Node pn = orExpr(inForInit);

        if (matchToken(Token1.HOOK)) {
            decompiler.addToken(Token1.HOOK);
            final Node ifTrue = assignExpr(false);
            mustMatchToken(Token1.COLON, "msg.no.colon.cond");
            decompiler.addToken(Token1.COLON);
            final Node ifFalse = assignExpr(inForInit);
            return nf.createCondExpr(pn, ifTrue, ifFalse);
        }

        return pn;
    }

    private Node orExpr(final boolean inForInit)
        throws IOException, ParserException
    {
        Node pn = andExpr(inForInit);
        if (matchToken(Token1.OR)) {
            decompiler.addToken(Token1.OR);
            pn = nf.createBinary(Token1.OR, pn, orExpr(inForInit));
        }

        return pn;
    }

    private Node andExpr(final boolean inForInit)
        throws IOException, ParserException
    {
        Node pn = bitOrExpr(inForInit);
        if (matchToken(Token1.AND)) {
            decompiler.addToken(Token1.AND);
            pn = nf.createBinary(Token1.AND, pn, andExpr(inForInit));
        }

        return pn;
    }

    private Node bitOrExpr(final boolean inForInit)
        throws IOException, ParserException
    {
        Node pn = bitXorExpr(inForInit);
        while (matchToken(Token1.BITOR)) {
            decompiler.addToken(Token1.BITOR);
            pn = nf.createBinary(Token1.BITOR, pn, bitXorExpr(inForInit));
        }
        return pn;
    }

    private Node bitXorExpr(final boolean inForInit)
        throws IOException, ParserException
    {
        Node pn = bitAndExpr(inForInit);
        while (matchToken(Token1.BITXOR)) {
            decompiler.addToken(Token1.BITXOR);
            pn = nf.createBinary(Token1.BITXOR, pn, bitAndExpr(inForInit));
        }
        return pn;
    }

    private Node bitAndExpr(final boolean inForInit)
        throws IOException, ParserException
    {
        Node pn = eqExpr(inForInit);
        while (matchToken(Token1.BITAND)) {
            decompiler.addToken(Token1.BITAND);
            pn = nf.createBinary(Token1.BITAND, pn, eqExpr(inForInit));
        }
        return pn;
    }

    private Node eqExpr(final boolean inForInit)
        throws IOException, ParserException
    {
        Node pn = relExpr(inForInit);
        for (;;) {
            final int tt = peekToken();
            switch (tt) {
              case Token1.EQ:
              case Token1.NE:
              case Token1.SHEQ:
              case Token1.SHNE:
                consumeToken();
                int decompilerToken = tt;
                int parseToken = tt;
                if (compilerEnv.getLanguageVersion() == Context.VERSION_1_2) {
                    // JavaScript 1.2 uses shallow equality for == and != .
                    // In addition, convert === and !== for decompiler into
                    // == and != since the decompiler is supposed to show
                    // canonical source and in 1.2 ===, !== are allowed
                    // only as an alias to ==, !=.
                    switch (tt) {
                      case Token1.EQ:
                        parseToken = Token1.SHEQ;
                        break;
                      case Token1.NE:
                        parseToken = Token1.SHNE;
                        break;
                      case Token1.SHEQ:
                        decompilerToken = Token1.EQ;
                        break;
                      case Token1.SHNE:
                        decompilerToken = Token1.NE;
                        break;
                    }
                }
                decompiler.addToken(decompilerToken);
                pn = nf.createBinary(parseToken, pn, relExpr(inForInit));
                continue;
            }
            break;
        }
        return pn;
    }

    private Node relExpr(final boolean inForInit)
        throws IOException, ParserException
    {
        Node pn = shiftExpr();
        for (;;) {
            final int tt = peekToken();
            switch (tt) {
              case Token1.IN:
                if (inForInit)
                    break;
                // fall through
              case Token1.INSTANCEOF:
              case Token1.LE:
              case Token1.LT:
              case Token1.GE:
              case Token1.GT:
                consumeToken();
                decompiler.addToken(tt);
                pn = nf.createBinary(tt, pn, shiftExpr());
                continue;
            }
            break;
        }
        return pn;
    }

    private Node shiftExpr()
        throws IOException, ParserException
    {
        Node pn = addExpr();
        for (;;) {
            final int tt = peekToken();
            switch (tt) {
              case Token1.LSH:
              case Token1.URSH:
              case Token1.RSH:
                consumeToken();
                decompiler.addToken(tt);
                pn = nf.createBinary(tt, pn, addExpr());
                continue;
            }
            break;
        }
        return pn;
    }

    private Node addExpr()
        throws IOException, ParserException
    {
        Node pn = mulExpr();
        for (;;) {
            final int tt = peekToken();
            if (tt == Token1.ADD || tt == Token1.SUB) {
                consumeToken();
                decompiler.addToken(tt);
                // flushNewLines
                pn = nf.createBinary(tt, pn, mulExpr());
                continue;
            }
            break;
        }

        return pn;
    }

    private Node mulExpr()
        throws IOException, ParserException
    {
        Node pn = unaryExpr();
        for (;;) {
            final int tt = peekToken();
            switch (tt) {
              case Token1.MUL:
              case Token1.DIV:
              case Token1.MOD:
                consumeToken();
                decompiler.addToken(tt);
                pn = nf.createBinary(tt, pn, unaryExpr());
                continue;
            }
            break;
        }

        return pn;
    }

    private Node unaryExpr()
        throws IOException, ParserException
    {
        int tt;

        tt = peekToken();

        switch(tt) {
        case Token1.VOID:
        case Token1.NOT:
        case Token1.BITNOT:
        case Token1.TYPEOF:
            consumeToken();
            decompiler.addToken(tt);
            return nf.createUnary(tt, unaryExpr());

        case Token1.ADD:
            consumeToken();
            // Convert to special POS token in decompiler and parse tree
            decompiler.addToken(Token1.POS);
            return nf.createUnary(Token1.POS, unaryExpr());

        case Token1.SUB:
            consumeToken();
            // Convert to special NEG token in decompiler and parse tree
            decompiler.addToken(Token1.NEG);
            return nf.createUnary(Token1.NEG, unaryExpr());

        case Token1.INC:
        case Token1.DEC:
            consumeToken();
            decompiler.addToken(tt);
            return nf.createIncDec(tt, false, memberExpr(true));

        case Token1.DELPROP:
            consumeToken();
            decompiler.addToken(Token1.DELPROP);
            return nf.createUnary(Token1.DELPROP, unaryExpr());

        case Token1.ERROR:
            consumeToken();
            break;

        // XML stream encountered in expression.
        case Token1.LT:
            if (compilerEnv.isXmlAvailable()) {
                consumeToken();
                final Node pn = xmlInitializer();
                return memberExprTail(true, pn);
            }
            // Fall thru to the default handling of RELOP

        default:
            final Node pn = memberExpr(true);

            // Don't look across a newline boundary for a postfix incop.
            tt = peekTokenOrEOL();
            if (tt == Token1.INC || tt == Token1.DEC) {
                consumeToken();
                decompiler.addToken(tt);
                return nf.createIncDec(tt, true, pn);
            }
            return pn;
        }
        return nf.createName("error"); // Only reached on error.Try to continue.

    }

    private Node xmlInitializer() throws IOException
    {
        int tt = ts.getFirstXMLToken();
        if (tt != Token1.XML && tt != Token1.XMLEND) {
            reportError("msg.syntax");
            return null;
        }

        /* Make a NEW node to append to. */
        final Node pnXML = nf.createLeaf(Token1.NEW);

        String xml = ts.getString();
        final boolean fAnonymous = xml.trim().startsWith("<>");

        Node pn = nf.createName(fAnonymous ? "XMLList" : "XML");
        nf.addChildToBack(pnXML, pn);

        pn = null;
        Node expr;
        for (;;tt = ts.getNextXMLToken()) {
            switch (tt) {
            case Token1.XML:
                xml = ts.getString();
                decompiler.addName(xml);
                mustMatchToken(Token1.LC, "msg.syntax");
                decompiler.addToken(Token1.LC);
                expr = (peekToken() == Token1.RC)
                    ? nf.createString("")
                    : expr(false);
                mustMatchToken(Token1.RC, "msg.syntax");
                decompiler.addToken(Token1.RC);
                if (pn == null) {
                    pn = nf.createString(xml);
                } else {
                    pn = nf.createBinary(Token1.ADD, pn, nf.createString(xml));
                }
                if (ts.isXMLAttribute()) {
                    /* Need to put the result in double quotes */
                    expr = nf.createUnary(Token1.ESCXMLATTR, expr);
                    final Node prepend = nf.createBinary(Token1.ADD,
                                                   nf.createString("\""),
                                                   expr);
                    expr = nf.createBinary(Token1.ADD,
                                           prepend,
                                           nf.createString("\""));
                } else {
                    expr = nf.createUnary(Token1.ESCXMLTEXT, expr);
                }
                pn = nf.createBinary(Token1.ADD, pn, expr);
                break;
            case Token1.XMLEND:
                xml = ts.getString();
                decompiler.addName(xml);
                if (pn == null) {
                    pn = nf.createString(xml);
                } else {
                    pn = nf.createBinary(Token1.ADD, pn, nf.createString(xml));
                }

                nf.addChildToBack(pnXML, pn);
                return pnXML;
            default:
                reportError("msg.syntax");
                return null;
            }
        }
    }

    private void argumentList(final Node listNode)
        throws IOException, ParserException
    {
        boolean matched;
        matched = matchToken(Token1.RP);
        if (!matched) {
            boolean first = true;
            do {
                if (!first)
                    decompiler.addToken(Token1.COMMA);
                first = false;
                if (peekToken() == Token1.YIELD) {
                    reportError("msg.yield.parenthesized");
                }
                nf.addChildToBack(listNode, assignExpr(false));
            } while (matchToken(Token1.COMMA));

            mustMatchToken(Token1.RP, "msg.no.paren.arg");
        }
        decompiler.addToken(Token1.RP);
    }

    private Node memberExpr(final boolean allowCallSyntax)
        throws IOException, ParserException
    {
        int tt;

        Node pn;

        /* Check for new expressions. */
        tt = peekToken();
        if (tt == Token1.NEW) {
            /* Eat the NEW token. */
            consumeToken();
            decompiler.addToken(Token1.NEW);

            /* Make a NEW node to append to. */
            pn = nf.createCallOrNew(Token1.NEW, memberExpr(false));

            if (matchToken(Token1.LP)) {
                decompiler.addToken(Token1.LP);
                /* Add the arguments to pn, if any are supplied. */
                argumentList(pn);
            }

            /* XXX there's a check in the C source against
             * "too many constructor arguments" - how many
             * do we claim to support?
             */

            /* Experimental syntax:  allow an object literal to follow a new expression,
             * which will mean a kind of anonymous class built with the JavaAdapter.
             * the object literal will be passed as an additional argument to the constructor.
             */
            tt = peekToken();
            if (tt == Token1.LC) {
                nf.addChildToBack(pn, primaryExpr());
            }
        } else {
            pn = primaryExpr();
        }

        return memberExprTail(allowCallSyntax, pn);
    }

    private Node memberExprTail(final boolean allowCallSyntax, Node pn)
        throws IOException, ParserException
    {
      tailLoop:
        for (;;) {
            int tt = peekToken();
            switch (tt) {

              case Token1.DOT:
              case Token1.DOTDOT:
                {
                    int memberTypeFlags;
                    String s;

                    consumeToken();
                    decompiler.addToken(tt);
                    memberTypeFlags = 0;
                    if (tt == Token1.DOTDOT) {
                        mustHaveXML();
                        memberTypeFlags = Node.DESCENDANTS_FLAG;
                    }
                    if (!compilerEnv.isXmlAvailable()) {
                        mustMatchToken(Token1.NAME, "msg.no.name.after.dot");
                        s = ts.getString();
                        decompiler.addName(s);
                        pn = nf.createPropertyGet(pn, null, s, memberTypeFlags);
                        break;
                    }

                    tt = nextToken();
                    switch (tt) {

                      // needed for generator.throw();
                      case Token1.THROW:
                        decompiler.addName("throw");
                        pn = propertyName(pn, "throw", memberTypeFlags);
                        break;

                      // handles: name, ns::name, ns::*, ns::[expr]
                      case Token1.NAME:
                        s = ts.getString();
                        decompiler.addName(s);
                        pn = propertyName(pn, s, memberTypeFlags);
                        break;

                      // handles: *, *::name, *::*, *::[expr]
                      case Token1.MUL:
                        decompiler.addName("*");
                        pn = propertyName(pn, "*", memberTypeFlags);
                        break;

                      // handles: '@attr', '@ns::attr', '@ns::*', '@ns::*',
                      //          '@::attr', '@::*', '@*', '@*::attr', '@*::*'
                      case Token1.XMLATTR:
                        decompiler.addToken(Token1.XMLATTR);
                        pn = attributeAccess(pn, memberTypeFlags);
                        break;

                      default:
                        reportError("msg.no.name.after.dot");
                    }
                }
                break;

              case Token1.DOTQUERY:
                consumeToken();
                mustHaveXML();
                decompiler.addToken(Token1.DOTQUERY);
                pn = nf.createDotQuery(pn, expr(false), ts.getLineno());
                mustMatchToken(Token1.RP, "msg.no.paren");
                decompiler.addToken(Token1.RP);
                break;

              case Token1.LB:
                consumeToken();
                decompiler.addToken(Token1.LB);
                pn = nf.createElementGet(pn, null, expr(false), 0);
                mustMatchToken(Token1.RB, "msg.no.bracket.index");
                decompiler.addToken(Token1.RB);
                break;

              case Token1.LP:
                if (!allowCallSyntax) {
                    break tailLoop;
                }
                consumeToken();
                decompiler.addToken(Token1.LP);
                pn = nf.createCallOrNew(Token1.CALL, pn);
                /* Add the arguments to pn, if any are supplied. */
                argumentList(pn);
                break;

              default:
                break tailLoop;
            }
        }
        return pn;
    }

    /*
     * Xml attribute expression:
     *   '@attr', '@ns::attr', '@ns::*', '@ns::*', '@*', '@*::attr', '@*::*'
     */
    private Node attributeAccess(Node pn, int memberTypeFlags)
        throws IOException
    {
        memberTypeFlags |= Node.ATTRIBUTE_FLAG;
        final int tt = nextToken();

        switch (tt) {
          // handles: @name, @ns::name, @ns::*, @ns::[expr]
          case Token1.NAME:
            {
                final String s = ts.getString();
                decompiler.addName(s);
                pn = propertyName(pn, s, memberTypeFlags);
            }
            break;

          // handles: @*, @*::name, @*::*, @*::[expr]
          case Token1.MUL:
            decompiler.addName("*");
            pn = propertyName(pn, "*", memberTypeFlags);
            break;

          // handles @[expr]
          case Token1.LB:
            decompiler.addToken(Token1.LB);
            pn = nf.createElementGet(pn, null, expr(false), memberTypeFlags);
            mustMatchToken(Token1.RB, "msg.no.bracket.index");
            decompiler.addToken(Token1.RB);
            break;

          default:
            reportError("msg.no.name.after.xmlAttr");
            pn = nf.createPropertyGet(pn, null, "?", memberTypeFlags);
            break;
        }

        return pn;
    }

    /**
     * Check if :: follows name in which case it becomes qualified name
     */
    private Node propertyName(Node pn, String name, final int memberTypeFlags)
        throws IOException, ParserException
    {
        String namespace = null;
        if (matchToken(Token1.COLONCOLON)) {
            decompiler.addToken(Token1.COLONCOLON);
            namespace = name;

            final int tt = nextToken();
            switch (tt) {
              // handles name::name
              case Token1.NAME:
                name = ts.getString();
                decompiler.addName(name);
                break;

              // handles name::*
              case Token1.MUL:
                decompiler.addName("*");
                name = "*";
                break;

              // handles name::[expr]
              case Token1.LB:
                decompiler.addToken(Token1.LB);
                pn = nf.createElementGet(pn, namespace, expr(false),
                                         memberTypeFlags);
                mustMatchToken(Token1.RB, "msg.no.bracket.index");
                decompiler.addToken(Token1.RB);
                return pn;

              default:
                reportError("msg.no.name.after.coloncolon");
                name = "?";
            }
        }

        pn = nf.createPropertyGet(pn, namespace, name, memberTypeFlags);
        return pn;
    }

    private Node arrayComprehension(final String arrayName, Node expr)
        throws IOException, ParserException
    {
        if (nextToken() != Token1.FOR)
            throw Kit.codeBug(); // shouldn't be here if next token isn't 'for'
        decompiler.addName(" "); // space after array literal expr
        decompiler.addToken(Token1.FOR);
        boolean isForEach = false;
        if (matchToken(Token1.NAME)) {
            decompiler.addName(ts.getString());
            if (ts.getString().equals("each")) {
                isForEach = true;
            } else {
                reportError("msg.no.paren.for");
            }
        }
        mustMatchToken(Token1.LP, "msg.no.paren.for");
        decompiler.addToken(Token1.LP);
        String name;
        int tt = peekToken();
        if (tt == Token1.LB || tt == Token1.LC) {
            // handle destructuring assignment
            name = currentScriptOrFn.getNextTempName();
            defineSymbol(Token1.LP, false, name);
            expr = nf.createBinary(Token1.COMMA,
                nf.createAssignment(Token1.ASSIGN, primaryExpr(),
                                    nf.createName(name)),
                expr);
        } else if (tt == Token1.NAME) {
            consumeToken();
            name = ts.getString();
            decompiler.addName(name);
        } else {
            reportError("msg.bad.var");
            return nf.createNumber(0);
        }

        final Node init = nf.createName(name);
        // Define as a let since we want the scope of the variable to
        // be restricted to the array comprehension
        defineSymbol(Token1.LET, false, name);

        mustMatchToken(Token1.IN, "msg.in.after.for.name");
        decompiler.addToken(Token1.IN);
        final Node iterator = expr(false);
        mustMatchToken(Token1.RP, "msg.no.paren.for.ctrl");
        decompiler.addToken(Token1.RP);

        Node body;
        tt = peekToken();
        if (tt == Token1.FOR) {
            body = arrayComprehension(arrayName, expr);
        } else {
            final Node call = nf.createCallOrNew(Token1.CALL,
                nf.createPropertyGet(nf.createName(arrayName), null,
                                     "push", 0));
            call.addChildToBack(expr);
            body = new Node(Token1.EXPR_VOID, call, ts.getLineno());
            if (tt == Token1.IF) {
                consumeToken();
                decompiler.addToken(Token1.IF);
                final int lineno = ts.getLineno();
                final Node cond = condition();
                body = nf.createIf(cond, body, null, lineno);
            }
            mustMatchToken(Token1.RB, "msg.no.bracket.arg");
            decompiler.addToken(Token1.RB);
        }

        final Node loop = enterLoop(null, true);
        try {
            return nf.createForIn(Token1.LET, loop, init, iterator, body,
                                  isForEach);
        } finally {
            exitLoop(false);
        }
    }

    private Node primaryExpr()
        throws IOException, ParserException
    {
        Node pn;

        final int ttFlagged = nextFlaggedToken();
        int tt = ttFlagged & CLEAR_TI_MASK;

        switch(tt) {

          case Token1.FUNCTION:
            return function(FunctionNode.FUNCTION_EXPRESSION);

          case Token1.LB: {
            final ObjArray elems = new ObjArray();
            int skipCount = 0;
            int destructuringLen = 0;
            decompiler.addToken(Token1.LB);
            boolean after_lb_or_comma = true;
            for (;;) {
                tt = peekToken();

                if (tt == Token1.COMMA) {
                    consumeToken();
                    decompiler.addToken(Token1.COMMA);
                    if (!after_lb_or_comma) {
                        after_lb_or_comma = true;
                    } else {
                        elems.add(null);
                        ++skipCount;
                    }
                } else if (tt == Token1.RB) {
                    consumeToken();
                    decompiler.addToken(Token1.RB);
                    // for ([a,] in obj) is legal, but for ([a] in obj) is
                    // not since we have both key and value supplied. The
                    // trick is that [a,] and [a] are equivalent in other
                    // array literal contexts. So we calculate a special
                    // length value just for destructuring assignment.
                    destructuringLen = elems.size() +
                                       (after_lb_or_comma ? 1 : 0);
                    break;
                } else if (skipCount == 0 && elems.size() == 1 &&
                           tt == Token1.FOR)
                {
                    final Node scopeNode = nf.createScopeNode(Token1.ARRAYCOMP,
                                                        ts.getLineno());
                    final String tempName = currentScriptOrFn.getNextTempName();
                    pushScope(scopeNode);
                    try {
                        defineSymbol(Token1.LET, false, tempName);
                        final Node expr = (Node) elems.get(0);
                        final Node block = nf.createBlock(ts.getLineno());
                        final Node init = new Node(Token1.EXPR_VOID,
                            nf.createAssignment(Token1.ASSIGN,
                                nf.createName(tempName),
                                nf.createCallOrNew(Token1.NEW,
                                    nf.createName("Array"))), ts.getLineno());
                        block.addChildToBack(init);
                        block.addChildToBack(arrayComprehension(tempName,
                            expr));
                        scopeNode.addChildToBack(block);
                        scopeNode.addChildToBack(nf.createName(tempName));
                        return scopeNode;
                    } finally {
                        popScope();
                    }
                } else {
                    if (!after_lb_or_comma) {
                        reportError("msg.no.bracket.arg");
                    }
                    elems.add(assignExpr(false));
                    after_lb_or_comma = false;
                }
            }
            return nf.createArrayLiteral(elems, skipCount, destructuringLen);
          }

          case Token1.LC: {
            final ObjArray elems = new ObjArray();
            decompiler.addToken(Token1.LC);
            if (!matchToken(Token1.RC)) {

                boolean first = true;
            commaloop:
                do {
                    Object property;

                    if (!first)
                        decompiler.addToken(Token1.COMMA);
                    else
                        first = false;

                    tt = peekToken();
                    switch(tt) {
                      case Token1.NAME:
                      case Token1.STRING:
                        consumeToken();
                        // map NAMEs to STRINGs in object literal context
                        // but tell the decompiler the proper type
                        String s = ts.getString();
                        if (tt == Token1.NAME) {
                            if (s.equals("get") &&
                                peekToken() == Token1.NAME) {
                                decompiler.addToken(Token1.GET);
                                consumeToken();
                                s = ts.getString();
                                decompiler.addName(s);
                                property = ScriptRuntime.getIndexObject(s);
                                if (!getterSetterProperty(elems, property,
                                                          true))
                                    break commaloop;
                                break;
                            } else if (s.equals("set") &&
                                       peekToken() == Token1.NAME) {
                                decompiler.addToken(Token1.SET);
                                consumeToken();
                                s = ts.getString();
                                decompiler.addName(s);
                                property = ScriptRuntime.getIndexObject(s);
                                if (!getterSetterProperty(elems, property,
                                                          false))
                                    break commaloop;
                                break;
                            }
                            decompiler.addName(s);
                        } else {
                            decompiler.addString(s);
                        }
                        property = ScriptRuntime.getIndexObject(s);
                        plainProperty(elems, property);
                        break;

                      case Token1.NUMBER:
                        consumeToken();
                        final double n = ts.getNumber();
                        decompiler.addNumber(n);
                        property = ScriptRuntime.getIndexObject(n);
                        plainProperty(elems, property);
                        break;

                      case Token1.RC:
                        // trailing comma is OK.
                        break commaloop;
                    default:
                        reportError("msg.bad.prop");
                        break commaloop;
                    }
                } while (matchToken(Token1.COMMA));

                mustMatchToken(Token1.RC, "msg.no.brace.prop");
            }
            decompiler.addToken(Token1.RC);
            return nf.createObjectLiteral(elems);
          }

          case Token1.LET:
            decompiler.addToken(Token1.LET);
            return let(false);

          case Token1.LP:

            /* Brendan's IR-jsparse.c makes a new node tagged with
             * TOK_LP here... I'm not sure I understand why.  Isn't
             * the grouping already implicit in the structure of the
             * parse tree?  also TOK_LP is already overloaded (I
             * think) in the C IR as 'function call.'  */
            decompiler.addToken(Token1.LP);
            pn = expr(false);
            pn.putProp(Node.PARENTHESIZED_PROP, Boolean.TRUE);
            decompiler.addToken(Token1.RP);
            mustMatchToken(Token1.RP, "msg.no.paren");
            return pn;

          case Token1.XMLATTR:
            mustHaveXML();
            decompiler.addToken(Token1.XMLATTR);
            pn = attributeAccess(null, 0);
            return pn;

          case Token1.NAME: {
            final String name = ts.getString();
            if ((ttFlagged & TI_CHECK_LABEL) != 0) {
                if (peekToken() == Token1.COLON) {
                    // Do not consume colon, it is used as unwind indicator
                    // to return to statementHelper.
                    // XXX Better way?
                    return nf.createLabel(ts.getLineno());
                }
            }

            decompiler.addName(name);
            if (compilerEnv.isXmlAvailable()) {
                pn = propertyName(null, name, 0);
            } else {
                pn = nf.createName(name);
            }
            return pn;
          }

          case Token1.NUMBER: {
            final double n = ts.getNumber();
            decompiler.addNumber(n);
            return nf.createNumber(n);
          }

          case Token1.STRING: {
            final String s = ts.getString();
            decompiler.addString(s);
            return nf.createString(s);
          }

          case Token1.DIV:
          case Token1.ASSIGN_DIV: {
            // Got / or /= which should be treated as regexp in fact
            ts.readRegExp(tt);
            final String flags = ts.regExpFlags;
            ts.regExpFlags = null;
            final String re = ts.getString();
            decompiler.addRegexp(re, flags);
            final int index = currentScriptOrFn.addRegexp(re, flags);
            return nf.createRegExp(index);
          }

          case Token1.NULL:
          case Token1.THIS:
          case Token1.FALSE:
          case Token1.TRUE:
            decompiler.addToken(tt);
            return nf.createLeaf(tt);

          case Token1.RESERVED:
            reportError("msg.reserved.id");
            break;

          case Token1.ERROR:
            /* the scanner or one of its subroutines reported the error. */
            break;

          case Token1.EOF:
            reportError("msg.unexpected.eof");
            break;

          default:
            reportError("msg.syntax");
            break;
        }
        return null;    // should never reach here
    }

    private void plainProperty(final ObjArray elems, final Object property)
            throws IOException {
        mustMatchToken(Token1.COLON, "msg.no.colon.prop");

        // OBJLIT is used as ':' in object literal for
        // decompilation to solve spacing ambiguity.
        decompiler.addToken(Token1.OBJECTLIT);
        elems.add(property);
        elems.add(assignExpr(false));
    }

    private boolean getterSetterProperty(final ObjArray elems, final Object property,
                                         final boolean isGetter) throws IOException {
        final Node f = function(FunctionNode.FUNCTION_EXPRESSION);
        if (f.getType() != Token1.FUNCTION) {
            reportError("msg.bad.prop");
            return false;
        }
        final int fnIndex = f.getExistingIntProp(Node.FUNCTION_PROP);
        final FunctionNode fn = currentScriptOrFn.getFunctionNode(fnIndex);
        if (fn.getFunctionName().length() != 0) {
            reportError("msg.bad.prop");
            return false;
        }
        elems.add(property);
        if (isGetter) {
            elems.add(nf.createUnary(Token1.GET, f));
        } else {
            elems.add(nf.createUnary(Token1.SET, f));
        }
        return true;
    }
}
