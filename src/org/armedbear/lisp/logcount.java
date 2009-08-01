/*
 * logcount.java
 *
 * Copyright (C) 2003-2005 Peter Graves
 * $Id: logcount.java,v 1.6 2007/02/23 21:17:35 piso Exp $
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

// ### logcount integer => number-of-on-bits
public final class logcount extends Primitive
{
    private logcount()
    {
        super("logcount","integer");
    }

    // FIXME Optimize fixnum case!
    public LispObject execute(LispObject arg) throws ConditionThrowable
    {
        BigInteger n;
        if (arg instanceof Fixnum)
            n = ((Fixnum)arg).getBigInteger();
        else if (arg instanceof Bignum)
            n = ((Bignum)arg).value;
        else
            return type_error(arg, Symbol.INTEGER);
        return new Fixnum(n.bitCount());
    }

    private static final Primitive LOGCOUNT = new logcount();
}
