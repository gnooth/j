/*
 * SimpleWarning.java
 *
 * Copyright (C) 2003-2005 Peter Graves
 * $Id: SimpleWarning.java,v 1.6 2005/06/23 00:53:43 piso Exp $
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

public final class SimpleWarning extends Warning
{
    public SimpleWarning(LispObject initArgs) throws ConditionThrowable
    {
        super(StandardClass.SIMPLE_WARNING);
        initialize(initArgs);
    }

    public SimpleWarning(LispObject formatControl, LispObject formatArguments)
        throws ConditionThrowable
    {
        super(StandardClass.SIMPLE_WARNING);
        setFormatControl(formatControl);
        setFormatArguments(formatArguments);
    }

    public LispObject typeOf()
    {
        return Symbol.SIMPLE_WARNING;
    }

    public LispObject classOf()
    {
        return StandardClass.SIMPLE_WARNING;
    }

    public LispObject typep(LispObject type) throws ConditionThrowable
    {
        if (type == Symbol.SIMPLE_WARNING)
            return T;
        if (type == StandardClass.SIMPLE_WARNING)
            return T;
        if (type == Symbol.SIMPLE_CONDITION)
            return T;
        if (type == StandardClass.SIMPLE_CONDITION)
            return T;
        return super.typep(type);
    }
}
