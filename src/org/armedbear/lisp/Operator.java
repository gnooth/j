/*
 * Operator.java
 *
 * Copyright (C) 2003-2005 Peter Graves
 * $Id: Operator.java,v 1.3 2005/04/08 10:46:23 piso Exp $
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

public abstract class Operator extends LispObject
{
    protected LispObject lambdaName;

    private LispObject lambdaList;

    public final LispObject getLambdaName()
    {
        return lambdaName;
    }

    public final void setLambdaName(LispObject obj)
    {
        lambdaName = obj;
    }

    public final LispObject getLambdaList()
    {
        return lambdaList;
    }

    public final void setLambdaList(LispObject obj)
    {
        lambdaList = obj;
    }

    public LispObject getParts() throws ConditionThrowable
    {
        LispObject result = NIL;
        result = result.push(new Cons("lambda-name", lambdaName));
        result = result.push(new Cons("lambda-list", lambdaList));
        return result.nreverse();
    }
}
