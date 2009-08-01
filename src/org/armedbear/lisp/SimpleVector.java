/*
 * SimpleVector.java
 *
 * Copyright (C) 2002-2007 Peter Graves
 * $Id: SimpleVector.java,v 1.26 2007/03/01 19:52:19 piso Exp $
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package org.armedbear.lisp;

// "The type of a vector that is not displaced to another array, has no fill
// pointer, is not expressly adjustable and is able to hold elements of any
// type is a subtype of type SIMPLE-VECTOR."
public final class SimpleVector extends AbstractVector
{
  private int capacity;
  private LispObject[] data;

  public SimpleVector(int capacity)
  {
    data = new LispObject[capacity];
    for (int i = capacity; i-- > 0;)
      data[i] = Fixnum.ZERO;
    this.capacity = capacity;
  }

  public SimpleVector(LispObject obj) throws ConditionThrowable
  {
    if (obj.listp())
      {
        data = obj.copyToArray();
        capacity = data.length;
      }
    else if (obj instanceof AbstractVector)
      {
        capacity = obj.length();
        data = new LispObject[capacity];
        for (int i = 0; i < capacity; i++)
          data[i] = obj.elt(i);
      }
    else
      Debug.assertTrue(false);
  }

  public SimpleVector(LispObject[] array)
  {
    data = array;
    capacity = array.length;
  }

  public LispObject typeOf()
  {
    return list2(Symbol.SIMPLE_VECTOR, new Fixnum(capacity));
  }

  public LispObject classOf()
  {
    return BuiltInClass.SIMPLE_VECTOR;
  }

  public LispObject getDescription()
  {
    StringBuffer sb = new StringBuffer("A simple vector with ");
    sb.append(capacity);
    sb.append(" elements");
    return new SimpleString(sb);
  }

  public LispObject typep(LispObject type) throws ConditionThrowable
  {
    if (type == Symbol.SIMPLE_VECTOR)
      return T;
    if (type == Symbol.SIMPLE_ARRAY)
      return T;
    if (type == BuiltInClass.SIMPLE_VECTOR)
      return T;
    if (type == BuiltInClass.SIMPLE_ARRAY)
      return T;
    return super.typep(type);
  }

  public LispObject getElementType()
  {
    return T;
  }

  public boolean isSimpleVector()
  {
    return true;
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
        return data[index];
      }
    catch (ArrayIndexOutOfBoundsException e)
      {
        badIndex(index, capacity);
        return NIL; // Not reached.
      }
  }

  public LispObject AREF(int index) throws ConditionThrowable
  {
    try
      {
        return data[index];
      }
    catch (ArrayIndexOutOfBoundsException e)
      {
        badIndex(index, data.length);
        return NIL; // Not reached.
      }
  }

  public LispObject AREF(LispObject index) throws ConditionThrowable
  {
    try
      {
        return data[((Fixnum)index).value];
      }
    catch (ClassCastException e)
      {
        return error(new TypeError(index, Symbol.FIXNUM));
      }
    catch (ArrayIndexOutOfBoundsException e)
      {
        badIndex(((Fixnum)index).value, data.length);
        return NIL; // Not reached.
      }
  }

  public void aset(int index, LispObject newValue) throws ConditionThrowable
  {
    try
      {
        data[index] = newValue;
      }
    catch (ArrayIndexOutOfBoundsException e)
      {
        badIndex(index, capacity);
      }
  }

  public LispObject SVREF(int index) throws ConditionThrowable
  {
    try
      {
        return data[index];
      }
    catch (ArrayIndexOutOfBoundsException e)
      {
        badIndex(index, data.length);
        return NIL; // Not reached.
      }
  }

  public void svset(int index, LispObject newValue) throws ConditionThrowable
  {
    try
      {
        data[index] = newValue;
      }
    catch (ArrayIndexOutOfBoundsException e)
      {
        badIndex(index, capacity);
      }
  }

  public LispObject subseq(int start, int end) throws ConditionThrowable
  {
    SimpleVector v = new SimpleVector(end - start);
    int i = start, j = 0;
    try
      {
        while (i < end)
          v.data[j++] = data[i++];
        return v;
      }
    catch (ArrayIndexOutOfBoundsException e)
      {
        return error(new TypeError("Array index out of bounds: " + i + "."));
      }
  }

  public void fill(LispObject obj) throws ConditionThrowable
  {
    for (int i = capacity; i-- > 0;)
      data[i] = obj;
  }

  public LispObject deleteEq(LispObject item) throws ConditionThrowable
  {
    final int limit = capacity;
    int i = 0;
    int j = 0;
    while (i < limit)
      {
        LispObject obj = data[i++];
        if (obj != item)
          data[j++] = obj;
      }
    if (j < limit)
      shrink(j);
    return this;
  }

  public LispObject deleteEql(LispObject item) throws ConditionThrowable
  {
    final int limit = capacity;
    int i = 0;
    int j = 0;
    while (i < limit)
      {
        LispObject obj = data[i++];
        if (!obj.eql(item))
          data[j++] = obj;
      }
    if (j < limit)
      shrink(j);
    return this;
  }

  public void shrink(int n) throws ConditionThrowable
  {
    if (n < capacity)
      {
        LispObject[] newData = new LispObject[n];
        System.arraycopy(data, 0, newData, 0, n);
        data = newData;
        capacity = n;
        return;
      }
    if (n == capacity)
      return;
    error(new LispError());
  }

  public LispObject reverse() throws ConditionThrowable
  {
    SimpleVector result = new SimpleVector(capacity);
    int i, j;
    for (i = 0, j = capacity - 1; i < capacity; i++, j--)
      result.data[i] = data[j];
    return result;
  }

  public LispObject nreverse() throws ConditionThrowable
  {
    int i = 0;
    int j = capacity - 1;
    while (i < j)
      {
        LispObject temp = data[i];
        data[i] = data[j];
        data[j] = temp;
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
        LispObject[] newData = new LispObject[newCapacity];
        if (initialContents.listp())
          {
            LispObject list = initialContents;
            for (int i = 0; i < newCapacity; i++)
              {
                newData[i] = list.car();
                list = list.cdr();
              }
          }
        else if (initialContents.vectorp())
          {
            for (int i = 0; i < newCapacity; i++)
              newData[i] = initialContents.elt(i);
          }
        else
          error(new TypeError(initialContents, Symbol.SEQUENCE));
        return new SimpleVector(newData);
      }
    if (capacity != newCapacity)
      {
        LispObject[] newData = new LispObject[newCapacity];
        System.arraycopy(data, 0, newData, 0,
                         Math.min(capacity, newCapacity));
        for (int i = capacity; i < newCapacity; i++)
          newData[i] = initialElement;
        return new SimpleVector(newData);
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

  // ### svref
  // svref simple-vector index => element
  private static final Primitive SVREF =
    new Primitive("svref", "simple-vector index")
    {
      public LispObject execute(LispObject first, LispObject second)
        throws ConditionThrowable
      {
        try
          {
            return ((SimpleVector)first).data[((Fixnum)second).value];
          }
        catch (ClassCastException e)
          {
            if (first instanceof SimpleVector)
              return error(new TypeError(second, Symbol.FIXNUM));
            else
              return error(new TypeError(first, Symbol.SIMPLE_VECTOR));
          }
      }
    };

  // ### svset simple-vector index new-value => new-value
  private static final Primitive SVSET =
    new Primitive("svset", PACKAGE_SYS, true, "simple-vector index new-value")
    {
      public LispObject execute(LispObject first, LispObject second,
                                LispObject third)
        throws ConditionThrowable
      {
        try
          {
            ((SimpleVector)first).data[((Fixnum)second).value] = third;
            return third;
          }
        catch (ClassCastException e)
          {
            if (first instanceof SimpleVector)
              return error(new TypeError(second, Symbol.FIXNUM));
            else
              return error(new TypeError(first, Symbol.SIMPLE_VECTOR));
          }
        catch (ArrayIndexOutOfBoundsException e)
          {
            int index = ((Fixnum)second).value;
            int capacity = ((SimpleVector)first).capacity;
            ((SimpleVector)first).badIndex(index, capacity);
            // Not reached.
            return NIL;
          }
      }
    };
}
