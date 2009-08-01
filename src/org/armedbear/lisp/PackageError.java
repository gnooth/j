/*
 * PackageError.java
 *
 * Copyright (C) 2003-2005 Peter Graves
 * $Id: PackageError.java,v 1.21 2005/06/22 17:46:05 piso Exp $
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

public final class PackageError extends LispError
{
    public PackageError(LispObject initArgs) throws ConditionThrowable
    {
        super(StandardClass.PACKAGE_ERROR);
        initialize(initArgs);
    }

    protected void initialize(LispObject initArgs) throws ConditionThrowable
    {
        super.initialize(initArgs);
        LispObject pkg = NIL;
        LispObject first, second;
        while (initArgs != NIL) {
            first = initArgs.car();
            initArgs = initArgs.cdr();
            second = initArgs.car();
            initArgs = initArgs.cdr();
            if (first == Keyword.PACKAGE)
                pkg = second;
        }
        setPackage(pkg);
    }

    public PackageError(String message) throws ConditionThrowable
    {
        super(StandardClass.PACKAGE_ERROR);
        setFormatControl(message);
    }

    public LispObject typeOf()
    {
        return Symbol.PACKAGE_ERROR;
    }

    public LispObject classOf()
    {
        return StandardClass.PACKAGE_ERROR;
    }

    public LispObject typep(LispObject type) throws ConditionThrowable
    {
        if (type == Symbol.PACKAGE_ERROR)
            return T;
        if (type == StandardClass.PACKAGE_ERROR)
            return T;
        return super.typep(type);
    }

    public LispObject getPackage()
    {
        Debug.assertTrue(layout != null);
        int index = layout.getSlotIndex(Symbol.PACKAGE);
        Debug.assertTrue(index >= 0);
        return slots[index];
    }

    public void setPackage(LispObject pkg)
    {
        Debug.assertTrue(layout != null);
        int index = layout.getSlotIndex(Symbol.PACKAGE);
        Debug.assertTrue(index >= 0);
        slots[index] = pkg;
    }
}
