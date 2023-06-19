/*
 * Copyright (c) 2008. All rights reserved.
 */
package ro.isdc.wro.model.resource.processor.support;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PushbackInputStream;

/**
 * Copyright (c) 2006 John Reilly (www.inconspicuous.org) This work is a
 * translation from C to Java of jsmin.c published by Douglas Crockford.
 * Permission is hereby granted to use the Java version under the same
 * conditions as the jsmin.c on which it is based.
 * <p>
 * http://www.crockford.com/javascript/jsmin.html
 *
 * @author Alex Objelean
 */
@SuppressWarnings("serial")
public class JSMin {
  private static final int EOF = -1;

  private final PushbackInputStream in;

  private final OutputStream out;

  private int theA;

  private int theB;

  private int theX = EOF;

  private int theY = EOF;

  public JSMin(final InputStream in, final OutputStream out) {
    this.in = new PushbackInputStream(in);
    this.out = out;
  }

  /**
   * isAlphanum -- return true if the character is a letter, digit, underscore,
   * dollar sign, or non-ASCII character.
   */
  static boolean isAlphanum(final int c) {
    return ((c >= 'a' && c <= 'z') || (c >= '0' && c <= '9')
        || (c >= 'A' && c <= 'Z') || c == '_' || c == '$' || c == '\\' || c > 126);
  }

  /**
   * get -- return the next character from stdin. Watch out for lookahead. If
   * the character is a control character, translate it to a space or linefeed.
   */
  int get() throws IOException {
    final int c = in.read();

    if (c >= ' ' || c == '\n' || c == EOF) {
      return c;
    }

    if (c == '\r') {
      return '\n';
    }

    return ' ';
  }

  /**
   * Get the next character without getting it.
   */
  int peek() throws IOException {
    final int lookaheadChar = in.read();
    in.unread(lookaheadChar);
    return lookaheadChar;
  }

  /**
   * next -- get the next character, excluding comments. peek() is used to see
   * if a '/' is followed by a '/' or '*'.
   */
  int next() throws IOException, UnterminatedCommentException {
    int c = get();
    if (c == '/') {
      switch (peek()) {
      case '/':
        for (;;) {
          c = get();
          if (c <= '\n') {
            break;
          }
        }
        break;
      case '*':
        get();
        while (c != ' ') {
          switch (get()) {
          case '*':
            if (peek() == '/') {
              get();
              c = ' ';
            }
            break;
          case EOF:
            throw new UnterminatedCommentException();
          }
        }
        break;
      }

    }
    theY = theX;
    theX = c;
    return c;
  }

  /**
   * action -- do something! What you do is determined by the argument:
   * <ul>
   *   <li>1 Output A. Copy B to A. Get the next B.</li>
   *   <li>2 Copy B to A. Get the next B. (Delete A).</li>
   *   <li>3 Get the next B. (Delete B).</li>
   * </ul>
   * action treats a string as a single character. Wow!<br/>
   * action recognizes a regular expression if it is preceded by ( or , or =.
   */

  void action(final int d) throws IOException,
      UnterminatedRegExpLiteralException, UnterminatedCommentException,
      UnterminatedStringLiteralException {
    switch (d) {
    case 1:
      out.write(theA);
      if (theA == theB && (theA == '+' || theA == '-') && theY != theA) {
        out.write(' ');
      }
    case 2:
      theA = theB;

      if (theA == '\'' || theA == '"' || theA == '`') {
        for (;;) {
          out.write(theA);
          theA = get();
          if (theA == theB) {
            break;
          }
          if (theA <= '\n') {
            throw new UnterminatedStringLiteralException();
          }
          if (theA == '\\') {
            out.write(theA);
            theA = get();
          }
        }
      }

    case 3:
      theB = next();
      if (theB == '/'
          && (theA == '(' || theA == ',' || theA == '=' || theA == ':'
              || theA == '[' || theA == '!' || theA == '&' || theA == '|'
              || theA == '?' || theA == '+' || theA == '-' || theA == '~'
              || theA == '*' || theA == '/' || theA == '{' || theA == '\n')) {
        out.write(theA);
        if (theA == '/' || theA == '*') {
          out.write(' ');
        }
        out.write(theB);
        for (;;) {
          theA = get();
          if (theA == '[') {
            for (;;) {
              out.write(theA);
              theA = get();
              if (theA == ']') {
                break;
              }
              if (theA == '\\') {
                out.write(theA);
                theA = get();
              }
              if (theA <= '\n') {
                throw new UnterminatedRegExpLiteralException();
              }
            }
          } else if (theA == '/') {
            switch (peek()) {
            case '/':
            case '*':
              throw new UnterminatedRegExpLiteralException();
            }
            break;
          } else if (theA == '\\') {
            out.write(theA);
            theA = get();
          } else if (theA <= '\n') {
            throw new UnterminatedRegExpLiteralException();
          }
          out.write(theA);
        }
        theB = next();
      }
    }
  }

  /**
   * jsmin -- Copy the input to the output, deleting the characters which are
   * insignificant to JavaScript. Comments will be removed. Tabs will be
   * replaced with spaces. Carriage returns will be replaced with linefeeds.
   * Most spaces and linefeeds will be removed.
   */
  public void jsmin() throws IOException, UnterminatedRegExpLiteralException,
      UnterminatedCommentException, UnterminatedStringLiteralException {
    if (peek() == 0xEF) {
      get();
      get();
      get();
    }
    theA = '\n';
    action(3);
    while (theA != EOF) {
      switch (theA) {
      case ' ':
        if (isAlphanum(theB)) {
          action(1);
        } else {
          action(2);
        }
        break;
      case '\n':
        switch (theB) {
        case '{':
        case '[':
        case '(':
        case '+':
        case '-':
        case '!':
        case '~':
          action(1);
          break;
        case ' ':
          action(3);
          break;
        default:
          if (isAlphanum(theB)) {
            action(1);
          } else {
            action(2);
          }
        }
        break;
      default:
        switch (theB) {
        case ' ':
          if (isAlphanum(theA)) {
            action(1);
            break;
          }
          action(3);
          break;
        case '\n':
          switch (theA) {
          case '}':
          case ']':
          case ')':
          case '+':
          case '-':
          case '"':
          case '\'':
          case '`':
            action(1);
            break;
          default:
            if (isAlphanum(theA)) {
              action(1);
            } else {
              action(3);
            }
          }
          break;
        default:
          action(1);
          break;
        }
      }
    }
    out.flush();
  }

  private static class UnterminatedCommentException extends Exception {
  }

  private static class UnterminatedStringLiteralException extends Exception {
  }

  private static class UnterminatedRegExpLiteralException extends Exception {
  }

}
