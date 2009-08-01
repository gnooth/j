/*
 * ComplexArray_UnsignedByte8.java
 *
 * Copyright (C) 2003-2005 Peter Graves
 * $Id: ComplexArray_UnsignedByte8.java,v 1.5 2007/02/23 21:17:33 piso Exp $
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

public final class ComplexArray_UnsignedByte8 extends AbstractArray
{
    private final int[] dimv;
    private final int totalSize;

    // For non-displaced arrays.
    private byte[] data;

    // For displaced arrays.
    private AbstractArray array;
    private int displacement;

    public ComplexArray_UnsignedByte8(int[] dimv)
    {
        this.dimv = dimv;
        totalSize = computeTotalSize(dimv);
        data = new byte[totalSize];
    }

    public ComplexArray_UnsignedByte8(int[] dimv, LispObject initialContents)
        throws ConditionThrowable
    {
        this.dimv = dimv;
        final int rank = dimv.length;
        LispObject rest = initialContents;
        for (int i = 0; i < rank; i++) {
            dimv[i] = rest.length();
            rest = rest.elt(0);
        }
        totalSize = computeTotalSize(dimv);
        data = new byte[totalSize];
        setInitialContents(0, dimv, initialContents, 0);
    }

    public ComplexArray_UnsignedByte8(int[] dimv, AbstractArray array, int displacement)
    {
        this.dimv = dimv;
        this.array = array;
        this.displacement = displacement;
        totalSize = computeTotalSize(dimv);
    }

    private int setInitialContents(int axis, int[] dims, LispObject contents,
                                   int index)
        throws ConditionThrowable
    {
        if (dims.length == 0) {
            try {
                data[index] = coerceLispObjectToJavaByte(contents);
            }
            catch (ArrayIndexOutOfBoundsException e) {
                error(new LispError("Bad initial contents for array."));
                return -1;
            }
            ++index;
        } else {
            int dim = dims[0];
            if (dim != contents.length()) {
                error(new LispError("Bad initial contents for array."));
                return -1;
            }
            int[] newDims = new int[dims.length-1];
            for (int i = 1; i < dims.length; i++)
                newDims[i-1] = dims[i];
            if (contents.listp()) {
                for (int i = contents.length();i-- > 0;) {
                    LispObject content = contents.car();
                    index =
                        setInitialContents(axis + 1, newDims, content, index);
                    contents = contents.cdr();
                }
            } else {
                AbstractVector v = checkVector(contents);
                final int length = v.length();
                for (int i = 0; i < length; i++) {
                    LispObject content = v.AREF(i);
                    index =
                        setInitialContents(axis + 1, newDims, content, index);
                }
            }
        }
        return index;
    }

    public LispObject typeOf()
    {
        return list3(Symbol.ARRAY, UNSIGNED_BYTE_8, getDimensions());
    }

    public LispObject classOf()
    {
        return BuiltInClass.ARRAY;
    }

    public int getRank()
    {
        return dimv.length;
    }

    public LispObject getDimensions()
    {
        LispObject result = NIL;
        for (int i = dimv.length; i-- > 0;)
            result = new Cons(new Fixnum(dimv[i]), result);
        return result;
    }

    public int getDimension(int n) throws ConditionThrowable
    {
        try {
            return dimv[n];
        }
        catch (ArrayIndexOutOfBoundsException e) {
            error(new TypeError("Bad array dimension " + n + "."));
            return -1;
        }
    }

    public LispObject getElementType()
    {
        return UNSIGNED_BYTE_8;
    }

    public int getTotalSize()
    {
        return totalSize;
    }

    public LispObject arrayDisplacement() throws ConditionThrowable
    {
        LispObject value1, value2;
        if (array != null) {
            value1 = array;
            value2 = new Fixnum(displacement);
        } else {
            value1 = NIL;
            value2 = Fixnum.ZERO;
        }
        return LispThread.currentThread().setValues(value1, value2);
    }

    public LispObject AREF(int index) throws ConditionThrowable
    {
        if (data != null) {
            try {
                return coerceJavaByteToLispObject(data[index]);
            }
            catch (ArrayIndexOutOfBoundsException e) {
                return error(new TypeError("Bad row major index " + index + "."));
            }
        } else
            return array.AREF(index + displacement);
    }

    public void aset(int index, LispObject newValue) throws ConditionThrowable
    {
        if (data != null) {
            try {
                data[index] = coerceLispObjectToJavaByte(newValue);
            }
            catch (ArrayIndexOutOfBoundsException e) {
                error(new TypeError("Bad row major index " + index + "."));
            }
        } else
            array.aset(index + displacement, newValue);
    }

    public void fill(LispObject obj) throws ConditionThrowable
    {
        if (data != null) {
            byte b = coerceLispObjectToJavaByte(obj);
            for (int i = data.length; i-- > 0;)
                data[i] = b;
        } else {
            for (int i = totalSize; i-- > 0;)
                aset(i, obj);
        }
    }

    public String writeToString() throws ConditionThrowable
    {
        if (Symbol.PRINT_READABLY.symbolValue() != NIL) {
            error(new PrintNotReadable(list2(Keyword.OBJECT, this)));
            // Not reached.
            return null;
        }
        return writeToString(dimv);
    }
}
