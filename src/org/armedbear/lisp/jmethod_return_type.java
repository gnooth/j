/*
 * jmethod_return_type.java
 *
 * Copyright (C) 2005 Peter Graves
 * $Id: jmethod_return_type.java,v 1.2 2007/02/23 21:17:35 piso Exp $
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

import java.lang.reflect.Method;

// ### jmethod-return-type method => class
public final class jmethod_return_type extends Primitive
{
    private jmethod_return_type()
    {
        super(Symbol.JMETHOD_RETURN_TYPE, "method",
"Returns a reference to the Class object that represents the formal return type of METHOD.");
    }

    public LispObject execute(LispObject arg)
        throws ConditionThrowable
    {
        final Method method;
        try {
            method = (Method) ((JavaObject)arg).getObject();
        }
        catch (ClassCastException e) {
            return error(new LispError(arg.writeToString() + " does not designate a Java method."));
        }
        return new JavaObject(method.getReturnType());
    }

    private static final Primitive JMETHOD_RETURN_TYPE = new jmethod_return_type();
}
