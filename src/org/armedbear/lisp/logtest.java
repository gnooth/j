/*
 * logtest.java
 *
 * Copyright (C) 2003-2005 Peter Graves
 * $Id: logtest.java,v 1.7 2007/02/23 21:17:36 piso Exp $
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

import java.math.BigInteger;

// ### logtest integer-1 integer-2 => generalized-boolean
// (logtest x y) == (not (zerop (logand x y)))
public final class logtest extends Primitive
{
    private logtest()
    {
        super("logtest", "integer-1 integer-2");
    }

    public LispObject execute(LispObject first, LispObject second)
        throws ConditionThrowable
    {
        if (first instanceof Fixnum && second instanceof Fixnum) {
            return (((Fixnum)first).value & ((Fixnum)second).value) == 0 ? NIL : T;
        } else {
            BigInteger n1, n2;
            if (first instanceof Fixnum)
                n1 = ((Fixnum)first).getBigInteger();
            else if (first instanceof Bignum)
                n1 = ((Bignum)first).value;
            else
                return type_error(first, Symbol.INTEGER);
            if (second instanceof Fixnum)
                n2 = ((Fixnum)second).getBigInteger();
            else if (second instanceof Bignum)
                n2 = ((Bignum)second).value;
            else
                return type_error(second, Symbol.INTEGER);
            return n1.and(n2).signum() == 0 ? NIL : T;
        }
    }

    private static final Primitive LOGTEST = new logtest();
}
