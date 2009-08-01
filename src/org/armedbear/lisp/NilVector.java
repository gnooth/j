/*
 * NilVector.java
 *
 * Copyright (C) 2004-2005 Peter Graves
 * $Id: NilVector.java,v 1.19 2007/02/23 21:17:34 piso Exp $
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

public final class NilVector extends AbstractString
{
    private int capacity;

    public NilVector(int capacity) throws ConditionThrowable
    {
        this.capacity = capacity;
    }

    public char[] chars() throws ConditionThrowable
    {
        if (capacity != 0)
            accessError();
        return new char[0];
    }

    public char[] getStringChars() throws ConditionThrowable
    {
        if (capacity != 0)
            accessError();
        return new char[0];
    }

    public String getStringValue() throws ConditionThrowable
    {
        if (capacity != 0)
            accessError();
        return "";
    }

    public LispObject typeOf()
    {
        return list2(Symbol.NIL_VECTOR, new Fixnum(capacity));
    }

    public LispObject classOf()
    {
        return BuiltInClass.NIL_VECTOR;
    }

    public LispObject typep(LispObject type) throws ConditionThrowable
    {
        if (type == Symbol.NIL_VECTOR)
            return T;
        if (type == Symbol.SIMPLE_STRING)
            return T;
        if (type == Symbol.SIMPLE_ARRAY)
            return T;
        if (type == BuiltInClass.NIL_VECTOR)
            return T;
        if (type == BuiltInClass.SIMPLE_STRING)
            return T;
        if (type == BuiltInClass.SIMPLE_ARRAY)
            return T;
        return super.typep(type);
    }

    public LispObject SIMPLE_STRING_P()
    {
        return T;
    }

    public boolean equal(LispObject obj) throws ConditionThrowable
    {
        if (obj instanceof NilVector) {
            if (capacity != ((NilVector)obj).capacity)
                return false;
            if (capacity != 0) {
                accessError();
                // Not reached.
                return false;
            }
            return true;
        }
        if (obj instanceof AbstractString) {
            if (capacity != obj.length())
                return false;
            if (capacity != 0) {
                accessError();
                // Not reached.
                return false;
            }
            return true;
        }
        return false;
    }

    public String getValue() throws ConditionThrowable
    {
        if (capacity == 0)
            return "";
        accessError();
        // Not reached.
        return null;
    }

    public int length()
    {
        return capacity;
    }

    public int capacity()
    {
        return capacity;
    }

    public LispObject getElementType()
    {
        return NIL;
    }

    public LispObject CHAR(int index) throws ConditionThrowable
    {
        return accessError();
    }

    public LispObject SCHAR(int index) throws ConditionThrowable
    {
        return accessError();
    }

    public LispObject AREF(int index) throws ConditionThrowable
    {
        return accessError();
    }

    public void aset(int index, LispObject newValue) throws ConditionThrowable
    {
        storeError(newValue);
    }

    public char charAt(int index) throws ConditionThrowable
    {
        accessError();
        // Not reached.
        return 0;
    }

    public void setCharAt(int index, char c) throws ConditionThrowable
    {
        storeError(LispCharacter.getInstance(c));
    }

    public LispObject subseq(int start, int end) throws ConditionThrowable
    {
        if (capacity == 0 && start == 0 && end == 0)
            return this;
        return accessError();
    }

    public void fill(LispObject obj) throws ConditionThrowable
    {
        storeError(obj);
    }

    public void fill(char c) throws ConditionThrowable
    {
        storeError(LispCharacter.getInstance(c));
    }

    public void shrink(int n) throws ConditionThrowable
    {
    }

    public LispObject reverse() throws ConditionThrowable
    {
        return accessError();
    }

    public LispObject accessError() throws ConditionThrowable
    {
        return error(new TypeError("Attempt to access an array of element type NIL."));
    }

    private void storeError(LispObject obj) throws ConditionThrowable
    {
        error(new TypeError(String.valueOf(obj) + " is not of type NIL."));
    }

    public String toString()
    {
        return unreadableString("NIL-VECTOR");
    }

    public int sxhash()
    {
        return 0;
    }

    public AbstractVector adjustVector(int newCapacity,
                                       LispObject initialElement,
                                       LispObject initialContents)
        throws ConditionThrowable
    {
        accessError();
        // Not reached.
        return null;
    }

    public AbstractVector adjustVector(int size, AbstractArray displacedTo,
                                       int displacement)
        throws ConditionThrowable
    {
        accessError();
        // Not reached.
        return null;
    }
}
