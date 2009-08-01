/*
 * BasicVector_UnsignedByte16.java
 *
 * Copyright (C) 2002-2005 Peter Graves
 * $Id: BasicVector_UnsignedByte16.java,v 1.2 2007/02/23 21:17:32 piso Exp $
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

// A basic vector is a specialized vector that is not displaced to another
// array, has no fill pointer, and is not expressly adjustable.
public final class BasicVector_UnsignedByte16 extends AbstractVector
{
    private int capacity;
    private int[] elements;

    public BasicVector_UnsignedByte16(int capacity)
    {
        elements = new int[capacity];
        this.capacity = capacity;
    }

    private BasicVector_UnsignedByte16(LispObject[] array)
        throws ConditionThrowable
    {
        capacity = array.length;
        elements = new int[capacity];
        for (int i = array.length; i-- > 0;)
            elements[i] = Fixnum.getValue(array[i]);
    }

    public LispObject typeOf()
    {
        return list3(Symbol.SIMPLE_ARRAY, UNSIGNED_BYTE_16,
                     new Cons(new Fixnum(capacity)));
    }

    public LispObject classOf()
    {
        return BuiltInClass.VECTOR;
    }

    public LispObject typep(LispObject type) throws ConditionThrowable
    {
        if (type == Symbol.SIMPLE_ARRAY)
            return T;
        if (type == BuiltInClass.SIMPLE_ARRAY)
            return T;
        return super.typep(type);
    }

    public LispObject getElementType()
    {
        return UNSIGNED_BYTE_16;
    }

    public boolean isSimpleVector()
    {
        return false;
    }

    public boolean hasFillPointer()
    {
        return false;
    }

    public boolean isAdjustable()
    {
        return false;
    }

    public int capacity()
    {
        return capacity;
    }

    public int length()
    {
        return capacity;
    }

    public LispObject elt(int index) throws ConditionThrowable
    {
        try {
            return new Fixnum(elements[index]);
        }
        catch (ArrayIndexOutOfBoundsException e) {
            badIndex(index, capacity);
            return NIL; // Not reached.
        }
    }

    // Ignores fill pointer.
    public int aref(int index) throws ConditionThrowable
    {
        try {
            return elements[index];
        }
        catch (ArrayIndexOutOfBoundsException e) {
            badIndex(index, elements.length);
            // Not reached.
            return 0;
        }
    }

    // Ignores fill pointer.
    public LispObject AREF(int index) throws ConditionThrowable
    {
        try {
            return new Fixnum(elements[index]);
        }
        catch (ArrayIndexOutOfBoundsException e) {
            badIndex(index, elements.length);
            return NIL; // Not reached.
        }
    }

    // Ignores fill pointer.
    public LispObject AREF(LispObject index) throws ConditionThrowable
    {
        try {
            return new Fixnum(elements[((Fixnum)index).value]);
        }
        catch (ClassCastException e) {
            return error(new TypeError(index, Symbol.FIXNUM));
        }
        catch (ArrayIndexOutOfBoundsException e) {
            badIndex(Fixnum.getValue(index), elements.length);
            return NIL; // Not reached.
        }
    }

    public void aset(int index, int n) throws ConditionThrowable
    {
        try {
            elements[index] = n;
        }
        catch (ArrayIndexOutOfBoundsException e) {
            badIndex(index, capacity);
        }
    }

    public void aset(int index, LispObject obj) throws ConditionThrowable
    {
        try {
            elements[index] = ((Fixnum)obj).value;
        }
        catch (ArrayIndexOutOfBoundsException e) {
            badIndex(index, capacity);
        }
        catch (ClassCastException e) {
            error(new TypeError(obj, UNSIGNED_BYTE_16));
        }
    }

    public LispObject subseq(int start, int end) throws ConditionThrowable
    {
        BasicVector_UnsignedByte16 v = new BasicVector_UnsignedByte16(end - start);
        int i = start, j = 0;
        try {
            while (i < end)
                v.elements[j++] = elements[i++];
            return v;
        }
        catch (ArrayIndexOutOfBoundsException e) {
            return error(new TypeError("Array index out of bounds: " + i + "."));
        }
    }

    public void fill(LispObject obj) throws ConditionThrowable
    {
        int n = Fixnum.getValue(obj);
        for (int i = capacity; i-- > 0;)
            elements[i] = n;
    }

    public void shrink(int n) throws ConditionThrowable
    {
        if (n < capacity) {
            int[] newArray = new int[n];
            System.arraycopy(elements, 0, newArray, 0, n);
            elements = newArray;
            capacity = n;
            return;
        }
        if (n == capacity)
            return;
        error(new LispError());
    }

    public LispObject reverse() throws ConditionThrowable
    {
        BasicVector_UnsignedByte16 result = new BasicVector_UnsignedByte16(capacity);
        int i, j;
        for (i = 0, j = capacity - 1; i < capacity; i++, j--)
            result.elements[i] = elements[j];
        return result;
    }

    public LispObject nreverse() throws ConditionThrowable
    {
        int i = 0;
        int j = capacity - 1;
        while (i < j) {
            int temp = elements[i];
            elements[i] = elements[j];
            elements[j] = temp;
            ++i;
            --j;
        }
        return this;
    }

    public AbstractVector adjustVector(int newCapacity,
                                       LispObject initialElement,
                                       LispObject initialContents)
        throws ConditionThrowable
    {
        if (initialContents != NIL) {
            LispObject[] newElements = new LispObject[newCapacity];
            if (initialContents.listp()) {
                LispObject list = initialContents;
                for (int i = 0; i < newCapacity; i++) {
                    newElements[i] = list.car();
                    list = list.cdr();
                }
            } else if (initialContents.vectorp()) {
                for (int i = 0; i < newCapacity; i++)
                    newElements[i] = initialContents.elt(i);
            } else
                error(new TypeError(initialContents, Symbol.SEQUENCE));
            return new BasicVector_UnsignedByte16(newElements);
        }
        if (capacity != newCapacity) {
            LispObject[] newElements = new LispObject[newCapacity];
            System.arraycopy(elements, 0, newElements, 0,
                             Math.min(capacity, newCapacity));
            for (int i = capacity; i < newCapacity; i++)
                newElements[i] = initialElement;
            return new BasicVector_UnsignedByte16(newElements);
        }
        // No change.
        return this;
    }

    public AbstractVector adjustVector(int newCapacity,
                                       AbstractArray displacedTo,
                                       int displacement)
    {
        return new ComplexVector(newCapacity, displacedTo, displacement);
    }
}
