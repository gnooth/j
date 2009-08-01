/*
 * EndOfFile.java
 *
 * Copyright (C) 2002-2005 Peter Graves
 * $Id: EndOfFile.java,v 1.6 2005/06/23 00:09:34 piso Exp $
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

public final class EndOfFile extends StreamError
{
    public EndOfFile(Stream stream) throws ConditionThrowable
    {
        super(StandardClass.END_OF_FILE);
        setStream(stream);
    }

    public EndOfFile(LispObject initArgs) throws ConditionThrowable
    {
        super(StandardClass.END_OF_FILE);
        initialize(initArgs);
    }

    public LispObject typeOf()
    {
        return Symbol.END_OF_FILE;
    }

    public LispObject classOf()
    {
        return StandardClass.END_OF_FILE;
    }

    public LispObject typep(LispObject type) throws ConditionThrowable
    {
        if (type == Symbol.END_OF_FILE)
            return T;
        if (type == StandardClass.END_OF_FILE)
            return T;
        return super.typep(type);
    }

    public String writeToString() throws ConditionThrowable
    {
        return unreadableString(Symbol.END_OF_FILE);
    }
}
