/*
 * Primitive1R.java
 *
 * Copyright (C) 2005 Peter Graves
 * $Id: Primitive1R.java,v 1.3 2007/02/23 21:17:34 piso Exp $
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

public class Primitive1R extends Function
{
    public Primitive1R(LispObject name)
    {
        super(name);
    }

    public Primitive1R(String name)
    {
        super(name);
    }

    public Primitive1R(String name, String arglist)
    {
        super(name, arglist);
    }

    public Primitive1R(LispObject name, LispObject lambdaList)
    {
        super(name, lambdaList);
    }

    public Primitive1R(String name, Package pkg)
    {
        super(name, pkg);
    }

    public Primitive1R(String name, Package pkg, boolean exported)
    {
        super(name, pkg, exported);
    }

    public Primitive1R(String name, Package pkg, boolean exported,
                     String arglist)
    {
        super(name, pkg, exported, arglist);
    }

    public Primitive1R(String name, Package pkg, boolean exported,
                     String arglist, String docstring)
    {
        super(name, pkg, exported, arglist, docstring);
    }

    public LispObject typeOf()
    {
        return Symbol.COMPILED_FUNCTION;
    }

    public LispObject execute(LispObject arg) throws ConditionThrowable
    {
        return _execute(arg, NIL);
    }

    public LispObject execute(LispObject first, LispObject second)
        throws ConditionThrowable
    {
        return _execute(first, new Cons(second));
    }

    public LispObject execute(LispObject first, LispObject second,
                              LispObject third)
        throws ConditionThrowable
    {
        return _execute(first, list2(second, third));
    }

    public LispObject execute(LispObject first, LispObject second,
                              LispObject third, LispObject fourth)
        throws ConditionThrowable
    {
        return _execute(first, list3(second, third, fourth));
    }

    public LispObject execute(LispObject first, LispObject second,
                              LispObject third, LispObject fourth,
                              LispObject fifth)
        throws ConditionThrowable
    {
        return _execute(first, list4(second, third, fourth, fifth));
    }

    public LispObject execute(LispObject first, LispObject second,
                              LispObject third, LispObject fourth,
                              LispObject fifth, LispObject sixth)
        throws ConditionThrowable
    {
        return _execute(first, list5(second, third, fourth, fifth, sixth));
    }

    public LispObject execute(LispObject first, LispObject second,
                              LispObject third, LispObject fourth,
                              LispObject fifth, LispObject sixth,
                              LispObject seventh)
        throws ConditionThrowable
    {
        return _execute(first, list6(second, third, fourth, fifth, sixth,
                                     seventh));
    }

    public LispObject execute(LispObject first, LispObject second,
                              LispObject third, LispObject fourth,
                              LispObject fifth, LispObject sixth,
                              LispObject seventh, LispObject eighth)
        throws ConditionThrowable
    {
        return _execute(first, list7(second, third, fourth, fifth, sixth,
                                     seventh, eighth));
    }

    public LispObject execute(LispObject[] args) throws ConditionThrowable
    {
        LispObject list = NIL;
        for (int i = args.length; i-- > 1;)
            list = new Cons(args[i], list);
        return _execute(args[0], list);
    }

    protected LispObject _execute(LispObject first, LispObject second)
        throws ConditionThrowable
    {
        return error(new LispError("Not implemented."));
    }
}
