/*
 * ComplexVector_UnsignedByte8.java
 *
 * Copyright (C) 2002-2005 Peter Graves
 * $Id: ComplexVector_UnsignedByte8.java,v 1.8 2007/02/23 21:17:33 piso Exp $
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

// A specialized vector of element type (UNSIGNED-BYTE 8) that is displaced to
// another array, has a fill pointer, and/or is expressly adjustable.
public final class ComplexVector_UnsignedByte8 extends AbstractVector
{
    private int capacity;
    private int fillPointer = -1; // -1 indicates no fill pointer.
    private boolean isDisplaced;

    // For non-displaced arrays.
    private byte[] elements;

    // For displaced arrays.
    private AbstractArray array;
    private int displacement;

    public ComplexVector_UnsignedByte8(int capacity)
    {
        elements = new byte[capacity];
        this.capacity = capacity;
    }

    public ComplexVector_UnsignedByte8(int capacity, AbstractArray array,
                                       int displacement)
    {
        this.capacity = capacity;
        this.array = array;
        this.displacement = displacement;
        isDisplaced = true;
    }

    public LispObject typeOf()
    {
        return list3(Symbol.VECTOR, UNSIGNED_BYTE_8, new Fixnum(capacity));
    }

    public LispObject classOf()
    {
        return BuiltInClass.VECTOR;
    }

    public boolean hasFillPointer()
    {
        return fillPointer >= 0;
    }

    public int getFillPointer()
    {
        return fillPointer;
    }

    public void setFillPointer(int n)
    {
        fillPointer = n;
    }

    public void setFillPointer(LispObject obj) throws ConditionThrowable
    {
        if (obj == T)
            fillPointer = capacity();
        else {
            int n = Fixnum.getValue(obj);
            if (n > capacity()) {
                StringBuffer sb = new StringBuffer("The new fill pointer (");
                sb.append(n);
                sb.append(") exceeds the capacity of the vector (");
                sb.append(capacity());
                sb.append(").");
                error(new LispError(sb.toString()));
            } else if (n < 0) {
                StringBuffer sb = new StringBuffer("The new fill pointer (");
                sb.append(n);
                sb.append(") is negative.");
                error(new LispError(sb.toString()));
            } else
                fillPointer = n;
        }
    }

    public boolean isDisplaced()
    {
        return isDisplaced;
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

    public LispObject getElementType()
    {
        return UNSIGNED_BYTE_8;
    }

    public boolean isSimpleVector()
    {
        return false;
    }

    public int capacity()
    {
        return capacity;
    }

    public int length()
    {
        return fillPointer >= 0 ? fillPointer : capacity;
    }

    public LispObject elt(int index) throws ConditionThrowable
    {
        final int limit = length();
        if (index < 0 || index >= limit)
            badIndex(index, limit);
        return AREF(index);
    }

    // Ignores fill pointer.
    public LispObject AREF(int index) throws ConditionThrowable
    {
        if (elements != null) {
            try {
                return coerceJavaByteToLispObject(elements[index]);
            }
            catch (ArrayIndexOutOfBoundsException e) {
                badIndex(index, elements.length);
                return NIL; // Not reached.
            }
        } else {
            // Displaced array.
            if (index < 0 || index >= capacity)
                badIndex(index, capacity);
            return array.AREF(index + displacement);
        }
    }

    // Ignores fill pointer.
    // FIXME inline
    public LispObject AREF(LispObject index) throws ConditionThrowable
    {
        return AREF(Fixnum.getValue(index));
    }

    public void aset(int index, int n) throws ConditionThrowable
    {
        if (elements != null) {
            try {
                elements[index] = (byte) n;
            }
            catch (ArrayIndexOutOfBoundsException e) {
                badIndex(index, elements.length);
            }
        } else {
            // Displaced array.
            if (index < 0 || index >= capacity)
                badIndex(index, capacity);
            else
                array.aset(index + displacement, n);
        }
    }

    public void aset(int index, LispObject newValue) throws ConditionThrowable
    {
        if (elements != null) {
            try {
                elements[index] = coerceLispObjectToJavaByte(newValue);
            }
            catch (ArrayIndexOutOfBoundsException e) {
                badIndex(index, elements.length);
            }
        } else
            array.aset(index + displacement, newValue);
    }

    public LispObject subseq(int start, int end) throws ConditionThrowable
    {
        SimpleVector v = new SimpleVector(end - start);
        int i = start, j = 0;
        try {
            while (i < end)
                v.aset(j++, AREF(i++));
            return v;
        }
        catch (ArrayIndexOutOfBoundsException e) {
            return error(new TypeError("Array index out of bounds: " + i + "."));
        }
    }

    public void fill(LispObject obj) throws ConditionThrowable
    {
        byte b = (byte) Fixnum.getValue(obj);
        for (int i = capacity; i-- > 0;)
            elements[i] = b;
    }

    public void shrink(int n) throws ConditionThrowable
    {
        if (elements != null) {
            if (n < elements.length) {
                byte[] newArray = new byte[n];
                System.arraycopy(elements, 0, newArray, 0, n);
                elements = newArray;
                capacity = n;
                return;
            }
            if (n == elements.length)
                return;
        }
        error(new LispError());
    }

    public LispObject reverse() throws ConditionThrowable
    {
        int length = length();
        BasicVector_UnsignedByte8 result = new BasicVector_UnsignedByte8(length);
        int i, j;
        for (i = 0, j = length - 1; i < length; i++, j--)
            result.aset(i, AREF(j));
        return result;
    }

    public LispObject nreverse() throws ConditionThrowable
    {
        if (elements != null) {
            int i = 0;
            int j = length() - 1;
            while (i < j) {
                byte temp = elements[i];
                elements[i] = elements[j];
                elements[j] = temp;
                ++i;
                --j;
            }
        } else {
            // Displaced array.
            int length = length();
            byte[] data = new byte[length];
            int i, j;
            for (i = 0, j = length - 1; i < length; i++, j--)
                data[i] = coerceLispObjectToJavaByte(AREF(j));
            elements = data;
            capacity = length;
            array = null;
            displacement = 0;
            isDisplaced = false;
            fillPointer = -1;
        }
        return this;
    }

    public void vectorPushExtend(LispObject element) throws ConditionThrowable
    {
        if (fillPointer < 0)
            noFillPointer();
        if (fillPointer >= capacity) {
            // Need to extend vector.
            ensureCapacity(capacity * 2 + 1);
        }
        aset(fillPointer, element);
        ++fillPointer;
    }

    public LispObject VECTOR_PUSH_EXTEND(LispObject element)
        throws ConditionThrowable
    {
        vectorPushExtend(element);
        return new Fixnum(fillPointer - 1);
    }

    public LispObject VECTOR_PUSH_EXTEND(LispObject element, LispObject extension)
        throws ConditionThrowable
    {
        int ext = Fixnum.getValue(extension);
        if (fillPointer < 0)
            noFillPointer();
        if (fillPointer >= capacity) {
            // Need to extend vector.
            ext = Math.max(ext, capacity + 1);
            ensureCapacity(capacity + ext);
        }
        aset(fillPointer, element);
        return new Fixnum(fillPointer++);
    }

    private final void ensureCapacity(int minCapacity) throws ConditionThrowable
    {
        if (elements != null) {
            if (capacity < minCapacity) {
                byte[] newArray = new byte[minCapacity];
                System.arraycopy(elements, 0, newArray, 0, capacity);
                elements = newArray;
                capacity = minCapacity;
            }
        } else {
            // Displaced array.
            Debug.assertTrue(array != null);
            if (capacity < minCapacity ||
                array.getTotalSize() - displacement < minCapacity)
            {
                // Copy array.
                elements = new byte[minCapacity];
                final int limit =
                    Math.min(capacity, array.getTotalSize() - displacement);
                for (int i = 0; i < limit; i++)
                    elements[i] = coerceLispObjectToJavaByte(array.AREF(displacement + i));
                capacity = minCapacity;
                array = null;
                displacement = 0;
                isDisplaced = false;
            }
        }
    }

    public AbstractVector adjustVector(int newCapacity,
                                       LispObject initialElement,
                                       LispObject initialContents)
        throws ConditionThrowable
    {
        if (initialContents != NIL) {
            // "If INITIAL-CONTENTS is supplied, it is treated as for MAKE-
            // ARRAY. In this case none of the original contents of array
            // appears in the resulting array."
            byte[] newElements = new byte[newCapacity];
            if (initialContents.listp()) {
                LispObject list = initialContents;
                for (int i = 0; i < newCapacity; i++) {
                    newElements[i] = coerceLispObjectToJavaByte(list.car());
                    list = list.cdr();
                }
            } else if (initialContents.vectorp()) {
                for (int i = 0; i < newCapacity; i++)
                    newElements[i] = coerceLispObjectToJavaByte(initialContents.elt(i));
            } else
                error(new TypeError(initialContents, Symbol.SEQUENCE));
            elements = newElements;
        } else {
            if (elements == null) {
                // Displaced array. Copy existing elements.
                elements = new byte[newCapacity];
                final int limit = Math.min(capacity, newCapacity);
                for (int i = 0; i < limit; i++)
                    elements[i] = coerceLispObjectToJavaByte(array.AREF(displacement + i));
            } else if (capacity != newCapacity) {
                byte[] newElements = new byte[newCapacity];
                System.arraycopy(elements, 0, newElements, 0,
                                 Math.min(capacity, newCapacity));
                elements = newElements;
            }
            // Initialize new elements (if aapplicable).
            if (initialElement != NIL) {
                byte b = coerceLispObjectToJavaByte(initialElement);
                for (int i = capacity; i < newCapacity; i++)
                    elements[i] = b;
            }
        }
        capacity = newCapacity;
        array = null;
        displacement = 0;
        isDisplaced = false;
        return this;
    }

    public AbstractVector adjustVector(int newCapacity,
                                       AbstractArray displacedTo,
                                       int displacement)
        throws ConditionThrowable
    {
        capacity = newCapacity;
        array = displacedTo;
        this.displacement = displacement;
        elements = null;
        isDisplaced = true;
        return this;
    }
}
