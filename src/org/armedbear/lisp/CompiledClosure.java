/*
 * CompiledClosure.java
 *
 * Copyright (C) 2004-2005 Peter Graves
 * $Id: CompiledClosure.java,v 1.7 2005/07/15 11:29:30 piso Exp $
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

public class CompiledClosure extends Function
{
    private final ClosureTemplateFunction ctf;
    private final LispObject[] context;

    public CompiledClosure(ClosureTemplateFunction ctf, LispObject[] context)
    {
        super(ctf.getLambdaName(), ctf.getLambdaList());
        this.ctf = ctf;
        this.context = context;
    }

    protected final LispObject[] processArgs(LispObject[] args, LispThread thread)
        throws ConditionThrowable
    {
        return ctf.processArgs(args, thread);
    }

    public LispObject execute() throws ConditionThrowable
    {
        return ctf.execute(context);
    }

    public LispObject execute(LispObject arg) throws ConditionThrowable
    {
        return ctf.execute(context, arg);
    }

    public LispObject execute(LispObject first, LispObject second)
        throws ConditionThrowable
    {
        return ctf.execute(context, first, second);
    }

    public LispObject execute(LispObject first, LispObject second,
                              LispObject third)
        throws ConditionThrowable
    {
        return ctf.execute(context, first, second, third);
    }

    public LispObject execute(LispObject first, LispObject second,
                              LispObject third, LispObject fourth)
        throws ConditionThrowable
    {
        return ctf.execute(context, first, second, third, fourth);
    }

    public LispObject execute(LispObject first, LispObject second,
                              LispObject third, LispObject fourth,
                              LispObject fifth)
        throws ConditionThrowable
    {
        return ctf.execute(context, first, second, third, fourth, fifth);
    }

    public LispObject execute(LispObject first, LispObject second,
                              LispObject third, LispObject fourth,
                              LispObject fifth, LispObject sixth)
        throws ConditionThrowable
    {
        return ctf.execute(context, first, second, third, fourth, fifth, sixth);
    }

    public LispObject execute(LispObject first, LispObject second,
                              LispObject third, LispObject fourth,
                              LispObject fifth, LispObject sixth,
                              LispObject seventh)
        throws ConditionThrowable
    {
        return ctf.execute(context, first, second, third, fourth, fifth, sixth,
                           seventh);
    }

    public LispObject execute(LispObject first, LispObject second,
                              LispObject third, LispObject fourth,
                              LispObject fifth, LispObject sixth,
                              LispObject seventh, LispObject eighth)
        throws ConditionThrowable
    {
        return ctf.execute(context, first, second, third, fourth, fifth, sixth,
                           seventh, eighth);
    }

    public LispObject execute(LispObject[] args) throws ConditionThrowable
    {
        return ctf.execute(context, args);
    }
}
