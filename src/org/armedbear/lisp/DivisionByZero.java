/*
 * DivisionByZero.java
 *
 * Copyright (C) 2003-2005 Peter Graves
 * $Id: DivisionByZero.java,v 1.7 2005/06/22 17:45:20 piso Exp $
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

public final class DivisionByZero extends ArithmeticError
{
    public DivisionByZero() throws ConditionThrowable
    {
        super(StandardClass.DIVISION_BY_ZERO);
        setFormatControl("Arithmetic error DIVISION-BY-ZERO signalled.");
    }

    public DivisionByZero(LispObject initArgs) throws ConditionThrowable
    {
        super(StandardClass.DIVISION_BY_ZERO);
        initialize(initArgs);
    }

    public LispObject typeOf()
    {
        return Symbol.DIVISION_BY_ZERO;
    }

    public LispObject classOf()
    {
        return StandardClass.DIVISION_BY_ZERO;
    }

    public LispObject typep(LispObject type) throws ConditionThrowable
    {
        if (type == Symbol.DIVISION_BY_ZERO)
            return T;
        if (type == StandardClass.DIVISION_BY_ZERO)
            return T;
        return super.typep(type);
    }
}
