/*
 * ceiling.java
 *
 * Copyright (C) 2004 Peter Graves
 * $Id: ceiling.java,v 1.1 2004/08/15 11:16:08 piso Exp $
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

// ### ceiling number &optional divisor
public final class ceiling extends Primitive
{
    private ceiling()
    {
        super("ceiling", "number &optional divisor");
    }

    public LispObject execute(LispObject arg) throws ConditionThrowable
    {
        return execute(arg, Fixnum.ONE);
    }

    public LispObject execute(LispObject first, LispObject second)
        throws ConditionThrowable
    {
        LispObject quotient = first.truncate(second);
        final LispThread thread = LispThread.currentThread();
        LispObject remainder = thread._values[1];
        if (remainder.zerop())
            return quotient;
        if (second.minusp()) {
            if (first.plusp())
                return quotient;
        } else {
            if (first.minusp())
                return quotient;
        }
        quotient = quotient.incr();
        thread._values[0] = quotient;
        thread._values[1] = remainder.subtract(second);
        return quotient;
    }

    private static final Primitive CEILING = new ceiling();
}
