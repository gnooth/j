/*
 * peek_char.java
 *
 * Copyright (C) 2004-2005 Peter Graves
 * $Id: peek_char.java,v 1.6 2007/02/23 21:17:36 piso Exp $
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package org.armedbear.lisp;

// ### peek-char
public final class peek_char extends Primitive
{
    private peek_char()
    {
        super("peek-char",
              "&optional peek-type input-stream eof-error-p eof-value recursive-p");
    }

    public LispObject execute(LispObject[] args) throws ConditionThrowable
    {
        int length = args.length;
        if (length > 5)
            error(new WrongNumberOfArgumentsException(this));
        LispObject peekType = length > 0 ? args[0] : NIL;
        Stream stream = length > 1 ? inSynonymOf(args[1]) : getStandardInput();
        boolean eofError = length > 2 ? (args[2] != NIL) : true;
        LispObject eofValue = length > 3 ? args[3] : NIL;
        // recursive-p is ignored
        // boolean recursive = length > 4 ? (args[4] != NIL) : false;
        if (peekType == NIL) {
            // "If PEEK-TYPE is not supplied or NIL, PEEK-CHAR returns the next
            // character to be read from INPUT-STREAM, without actually
            // removing it from INPUT-STREAM."
            final Stream in;
            if (stream instanceof EchoStream)
                // "When INPUT-STREAM is an echo stream, characters that are
                // only peeked at are not echoed." Read from the echo stream's
                // input stream to bypass the echo.
                in = ((EchoStream)stream).getInputStream();
            else
                in = stream;
            final LispObject result = in.readChar(eofError, eofValue);
            if (result instanceof LispCharacter)
                in.unreadChar((LispCharacter)result);
            return result;
        }
        if (peekType == T) {
            // "If PEEK-TYPE is T, then PEEK-CHAR skips over whitespace[2]
            // characters, but not comments, and then performs the peeking
            // operation on the next character."
            Readtable rt = currentReadtable();
            while (true) {
                LispObject result = stream.readChar(eofError, eofValue);
                if (result instanceof LispCharacter) {
                    char c = ((LispCharacter)result).value;
                    if (!rt.isWhitespace(c)) {
                        stream.unreadChar((LispCharacter)result);
                        return result;
                    }
                } else
                    return result;
            }
        }
        if (peekType instanceof LispCharacter) {
            // "If PEEK-TYPE is a character, then PEEK-CHAR skips over input
            // characters until a character that is CHAR= to that character is
            // found; that character is left in INPUT-STREAM."
            char c = ((LispCharacter)peekType).value;
            while (true) {
                LispObject result = stream.readChar(eofError, eofValue);
                if (result instanceof LispCharacter) {
                    if (((LispCharacter)result).value == c) {
                        stream.unreadChar((LispCharacter)result);
                        return result;
                    }
                } else
                    return result;
            }
        }
        return error(new SimpleError(String.valueOf(peekType) +
                                      " is an illegal peek-type."));
    }

    private static final Primitive PEEK_CHAR = new peek_char();
}
