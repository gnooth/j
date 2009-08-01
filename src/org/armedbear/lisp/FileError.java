/*
 * FileError.java
 *
 * Copyright (C) 2004-2005 Peter Graves
 * $Id: FileError.java,v 1.6 2005/06/23 00:41:08 piso Exp $
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

public final class FileError extends LispError
{
    // initArgs is either a normal initArgs list or a pathname.
    public FileError(LispObject initArgs) throws ConditionThrowable
    {
        super(StandardClass.FILE_ERROR);
        if (initArgs instanceof Cons)
            initialize(initArgs);
        else
            setPathname(initArgs);
    }

    protected void initialize(LispObject initArgs) throws ConditionThrowable
    {
        super.initialize(initArgs);
        LispObject pathname = NIL;
        while (initArgs != NIL) {
            LispObject first = initArgs.car();
            initArgs = initArgs.cdr();
            if (first == Keyword.PATHNAME) {
                pathname = initArgs.car();
                break;
            }
            initArgs = initArgs.cdr();
        }
        setPathname(pathname);
    }

    public FileError(String message) throws ConditionThrowable
    {
        super(StandardClass.FILE_ERROR);
        setFormatControl(message);
        setFormatArguments(NIL);
    }

    public FileError(String message, LispObject pathname)
        throws ConditionThrowable
    {
        super(StandardClass.FILE_ERROR);
        setFormatControl(message);
        setFormatArguments(NIL);
        setPathname(pathname);
    }

    public LispObject getPathname() throws ConditionThrowable
    {
        return getInstanceSlotValue(Symbol.PATHNAME);
    }

    private void setPathname(LispObject pathname) throws ConditionThrowable
    {
        setInstanceSlotValue(Symbol.PATHNAME, pathname);
    }

    public LispObject typeOf()
    {
        return Symbol.FILE_ERROR;
    }

    public LispObject classOf()
    {
        return StandardClass.FILE_ERROR;
    }

    public LispObject typep(LispObject type) throws ConditionThrowable
    {
        if (type == Symbol.FILE_ERROR)
            return T;
        if (type == StandardClass.FILE_ERROR)
            return T;
        return super.typep(type);
    }
}
