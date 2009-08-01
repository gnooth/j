/*
 * SimpleTypeError.java
 *
 * Copyright (C) 2002-2005 Peter Graves
 * $Id: SimpleTypeError.java,v 1.11 2005/09/08 16:12:06 piso Exp $
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

public final class SimpleTypeError extends TypeError
{
    public SimpleTypeError(LispObject initArgs) throws ConditionThrowable
    {
        super(StandardClass.SIMPLE_TYPE_ERROR);
        initialize(initArgs);
    }

    public LispObject typeOf()
    {
        return Symbol.SIMPLE_TYPE_ERROR;
    }

    public LispObject classOf()
    {
        return StandardClass.SIMPLE_TYPE_ERROR;
    }

    public LispObject typep(LispObject type) throws ConditionThrowable
    {
        if (type == Symbol.SIMPLE_TYPE_ERROR)
            return T;
        if (type == StandardClass.SIMPLE_TYPE_ERROR)
            return T;
        if (type == Symbol.SIMPLE_CONDITION)
            return T;
        if (type == StandardClass.SIMPLE_CONDITION)
            return T;
        return super.typep(type);
    }

    public String getMessage()
    {
        try {
            LispObject formatControl = getFormatControl();
            if (formatControl != NIL) {
                LispObject formatArguments = getFormatArguments();
                // (apply 'format (append '(nil format-control) format-arguments))
                LispObject result =
                    Primitives.APPLY.execute(Symbol.FORMAT,
                                             Primitives.APPEND.execute(list2(NIL,
                                                                             formatControl),
                                                                       formatArguments));
                return result.getStringValue();
            }
            return super.getMessage();
        }
        catch (Throwable t) {}
        return null;
    }
}
