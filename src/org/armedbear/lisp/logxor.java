/*
 * logxor.java
 *
 * Copyright (C) 2003-2005 Peter Graves
 * $Id: logxor.java,v 1.7 2007/02/23 21:17:36 piso Exp $
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

// ### logxor &rest integers => result-integer
public final class logxor extends Primitive
{
    private logxor()
    {
        super("logxor", "&rest integers");
    }

    public LispObject execute()
    {
        return Fixnum.ZERO;
    }

    public LispObject execute(LispObject arg) throws ConditionThrowable
    {
        if (arg instanceof Fixnum || arg instanceof Bignum)
            return arg;
        return type_error(arg, Symbol.INTEGER);
    }

    public LispObject execute(LispObject first, LispObject second)
        throws ConditionThrowable
    {
        return first.LOGXOR(second);
    }

    public LispObject execute(LispObject[] args) throws ConditionThrowable
    {
        LispObject result = Fixnum.ZERO;
        for (int i = 0; i < args.length; i++)
            result = result.LOGXOR(args[i]);
        return result;
    }

    private static final Primitive LOGXOR = new logxor();
}
