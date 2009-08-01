/*
 * BasicVector_UnsignedByte8.java
 *
 * Copyright (C) 2002-2006 Peter Graves
 * $Id: BasicVector_UnsignedByte8.java,v 1.8 2007/02/23 21:17:32 piso Exp $
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
public final class BasicVector_UnsignedByte8 extends AbstractVector
{
  private int capacity;
  private byte[] elements;

  public BasicVector_UnsignedByte8(int capacity)
  {
    elements = new byte[capacity];
    this.capacity = capacity;
  }

  public BasicVector_UnsignedByte8(LispObject[] array)
    throws ConditionThrowable
  {
    capacity = array.length;
    elements = new byte[capacity];
    for (int i = array.length; i-- > 0;)
      elements[i] = coerceLispObjectToJavaByte(array[i]);
  }

  public LispObject typeOf()
  {
    return list3(Symbol.SIMPLE_ARRAY, UNSIGNED_BYTE_8, new Cons(new Fixnum(capacity)));
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
    return UNSIGNED_BYTE_8;
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
    try
      {
        return coerceJavaByteToLispObject(elements[index]);
      }
    catch (ArrayIndexOutOfBoundsException e)
      {
        badIndex(index, capacity);
        return NIL; // Not reached.
      }
  }

  public int aref(int index) throws ConditionThrowable
  {
    try
      {
        return (((int)elements[index]) & 0xff);
      }
    catch (ArrayIndexOutOfBoundsException e)
      {
        badIndex(index, elements.length);
        // Not reached.
        return 0;
      }
  }

  public LispObject AREF(int index) throws ConditionThrowable
  {
    try
      {
        return coerceJavaByteToLispObject(elements[index]);
      }
    catch (ArrayIndexOutOfBoundsException e)
      {
        badIndex(index, elements.length);
        return NIL; // Not reached.
      }
  }

  public LispObject AREF(LispObject index) throws ConditionThrowable
  {
    try
      {
        return coerceJavaByteToLispObject(elements[((Fixnum)index).value]);
      }
    catch (ClassCastException e)
      {
        return error(new TypeError(index, Symbol.FIXNUM));
      }
    catch (ArrayIndexOutOfBoundsException e)
      {
        badIndex(Fixnum.getValue(index), elements.length);
        return NIL; // Not reached.
      }
  }

  public void aset(int index, int n) throws ConditionThrowable
  {
    try
      {
        elements[index] = (byte) n;
      }
    catch (ArrayIndexOutOfBoundsException e)
      {
        badIndex(index, capacity);
      }
  }

  public void aset(int index, LispObject value) throws ConditionThrowable
  {
    try
      {
        elements[index] = coerceLispObjectToJavaByte(value);
      }
    catch (ArrayIndexOutOfBoundsException e)
      {
        badIndex(index, capacity);
      }
  }

  public LispObject subseq(int start, int end) throws ConditionThrowable
  {
    BasicVector_UnsignedByte8 v = new BasicVector_UnsignedByte8(end - start);
    int i = start, j = 0;
    try
      {
        while (i < end)
          v.elements[j++] = elements[i++];
        return v;
      }
    catch (ArrayIndexOutOfBoundsException e)
      {
        return error(new TypeError("Array index out of bounds: " + i + "."));
      }
  }

  public void fill(LispObject obj) throws ConditionThrowable
  {
    byte b = coerceLispObjectToJavaByte(obj);
    for (int i = capacity; i-- > 0;)
      elements[i] = b;
  }

  public void shrink(int n) throws ConditionThrowable
  {
    if (n < capacity)
      {
        byte[] newArray = new byte[n];
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
    BasicVector_UnsignedByte8 result = new BasicVector_UnsignedByte8(capacity);
    int i, j;
    for (i = 0, j = capacity - 1; i < capacity; i++, j--)
      result.elements[i] = elements[j];
    return result;
  }

  public LispObject nreverse() throws ConditionThrowable
  {
    int i = 0;
    int j = capacity - 1;
    while (i < j)
      {
        byte temp = elements[i];
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
    if (initialContents != NIL)
      {
        LispObject[] newElements = new LispObject[newCapacity];
        if (initialContents.listp())
          {
            LispObject list = initialContents;
            for (int i = 0; i < newCapacity; i++)
              {
                newElements[i] = list.car();
                list = list.cdr();
              }
          }
        else if (initialContents.vectorp())
          {
            for (int i = 0; i < newCapacity; i++)
              newElements[i] = initialContents.elt(i);
          }
        else
          type_error(initialContents, Symbol.SEQUENCE);
        return new BasicVector_UnsignedByte8(newElements);
      }
    if (capacity != newCapacity)
      {
        LispObject[] newElements = new LispObject[newCapacity];
        System.arraycopy(elements, 0, newElements, 0,
                         Math.min(capacity, newCapacity));
        for (int i = capacity; i < newCapacity; i++)
          newElements[i] = initialElement;
        return new BasicVector_UnsignedByte8(newElements);
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
