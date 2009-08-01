/*
 * lisp_implementation_type.java
 *
 * Copyright (C) 2003-2004 Peter Graves
 * $Id: lisp_implementation_type.java,v 1.5 2004/11/03 15:39:02 piso Exp $
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

// ### lisp-implementation-type <no arguments> => description
public final class lisp_implementation_type extends Primitive
{
    private lisp_implementation_type()
    {
        super("lisp-implementation-type", "");
    }

    public LispObject execute()
    {
        return new SimpleString("Armed Bear Common Lisp");
    }

    private static final lisp_implementation_type LISP_IMPLEMENTATION_TYPE =
        new lisp_implementation_type();
}
