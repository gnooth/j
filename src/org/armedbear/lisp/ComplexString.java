/*
 * ComplexString.java
 *
 * Copyright (C) 2002-2007 Peter Graves
 * $Id: ComplexString.java,v 1.37 2007/03/18 11:35:20 piso Exp $
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

public final class ComplexString extends AbstractString
{
  private int capacity;
  private int fillPointer = -1; // -1 indicates no fill pointer.
  private boolean isDisplaced;

  // For non-displaced arrays.
  private char[] chars;

  // For displaced arrays.
  private AbstractArray array;
  private int displacement;

  public ComplexString(int capacity)
  {
    this.capacity = capacity;
    chars = new char[capacity];
    isDisplaced = false;
  }

  public ComplexString(int capacity, AbstractArray array, int displacement)
  {
    this.capacity = capacity;
    this.array = array;
    this.displacement = displacement;
    isDisplaced = true;
  }

  public LispObject typeOf()
  {
    return list2(Symbol.STRING, number(capacity()));
  }

  public LispObject classOf()
  {
    return BuiltInClass.STRING;
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
    else
      {
        int n = Fixnum.getValue(obj);
        if (n > capacity())
          {
            StringBuffer sb = new StringBuffer("The new fill pointer (");
            sb.append(n);
            sb.append(") exceeds the capacity of the vector (");
            sb.append(capacity());
            sb.append(").");
            error(new LispError(sb.toString()));
          }
        else if (n < 0)
          {
            StringBuffer sb = new StringBuffer("The new fill pointer (");
            sb.append(n);
            sb.append(") is negative.");
            error(new LispError(sb.toString()));
          }
        else
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
    if (array != null)
      {
        value1 = array;
        value2 = new Fixnum(displacement);
      }
    else
      {
        value1 = NIL;
        value2 = Fixnum.ZERO;
      }
    return LispThread.currentThread().setValues(value1, value2);
  }

  public char[] chars() throws ConditionThrowable
  {
    if (chars != null)
      return chars;
    Debug.assertTrue(array != null);
    char[] copy = new char[capacity];
    if (array instanceof AbstractString)
      System.arraycopy(array.chars(), displacement, copy, 0, capacity);
    else if (array.getElementType() == Symbol.CHARACTER)
      {
        for (int i = 0; i < capacity; i++)
          {
            LispObject obj = array.AREF(displacement + i);
            try
              {
                copy[i] = ((LispCharacter)obj).value;
              }
            catch (ClassCastException e)
              {
                type_error(obj, Symbol.CHARACTER);
              }
          }
      }
    else
      type_error(array, Symbol.STRING);
    return copy;
  }

  public char[] getStringChars() throws ConditionThrowable
  {
    if (fillPointer < 0)
      return chars();
    char[] ret = new char[fillPointer];
    System.arraycopy(chars(), 0, ret, 0, fillPointer);
    return ret;
  }

  public boolean equal(LispObject obj) throws ConditionThrowable
  {
    if (this == obj)
      return true;
    if (obj instanceof AbstractString)
      {
        AbstractString string = (AbstractString) obj;
        if (string.length() != length())
          return false;
        for (int i = length(); i-- > 0;)
          if (string.charAt(i) != charAt(i))
            return false;
        return true;
      }
    if (obj instanceof NilVector)
      return obj.equal(this);
    return false;
  }

  public boolean equalp(LispObject obj) throws ConditionThrowable
  {
    if (this == obj)
      return true;
    if (obj instanceof AbstractString)
      {
        AbstractString string = (AbstractString) obj;
        if (string.length() != length())
          return false;
        for (int i = length(); i-- > 0;)
          {
            if (string.charAt(i) != charAt(i))
              {
                if (LispCharacter.toLowerCase(string.charAt(i)) != LispCharacter.toLowerCase(charAt(i)))
                  return false;
              }
          }
        return true;
      }
    if (obj instanceof AbstractBitVector)
      return false;
    if (obj instanceof AbstractArray)
      return obj.equalp(this);
    return false;
  }

  public LispObject subseq(int start, int end) throws ConditionThrowable
  {
    SimpleString s = new SimpleString(end - start);
    int i = start, j = 0;
    while (i < end)
      s.setCharAt(j++, charAt(i++));
    return s;
  }

  public void fill(LispObject obj) throws ConditionThrowable
  {
    fill(LispCharacter.getValue(obj));
  }

  public void fill(char c) throws ConditionThrowable
  {
    for (int i = length(); i-- > 0;)
      setCharAt(i, c);
  }

  public void shrink(int n) throws ConditionThrowable
  {
    if (chars != null)
      {
        if (n < capacity)
          {
            char[] newArray = new char[n];
            System.arraycopy(chars, 0, newArray, 0, n);
            chars = newArray;
            capacity = n;
            fillPointer = -1;
            return;
          }
        if (n == capacity)
          return;
      }
    Debug.assertTrue(chars == null);
    // Displaced array. Copy existing characters.
    chars = new char[n];
    if (array instanceof AbstractString)
      {
        AbstractString string = (AbstractString) array;
        for (int i = 0; i < n; i++)
          {
            chars[i] = string.charAt(displacement + i);
          }
      }
    else
      {
        for (int i = 0; i < n; i++)
          {
            LispCharacter character =
              (LispCharacter) array.AREF(displacement + i);
            chars[i] = character.value;
          }
      }
    capacity = n;
    array = null;
    displacement = 0;
    isDisplaced = false;
    fillPointer = -1;
  }

  public LispObject reverse() throws ConditionThrowable
  {
    int length = length();
    SimpleString result = new SimpleString(length);
    int i, j;
    for (i = 0, j = length - 1; i < length; i++, j--)
      result.setCharAt(i, charAt(j));
    return result;
  }

  public LispObject nreverse() throws ConditionThrowable
  {
    int i = 0;
    int j = length() - 1;
    while (i < j)
      {
        char temp = charAt(i);
        setCharAt(i, charAt(j));
        setCharAt(j, temp);
        ++i;
        --j;
      }
    return this;
  }

  public String getStringValue() throws ConditionThrowable
  {
    if (fillPointer >= 0)
      return new String(chars(), 0, fillPointer);
    else
      return new String(chars());
  }

  public Object javaInstance() throws ConditionThrowable
  {
    return new String(chars());
  }

  public Object javaInstance(Class c) throws ConditionThrowable
  {
    return javaInstance();
  }

  public final int capacity()
  {
    return capacity;
  }

  public final int length()
  {
    return fillPointer >= 0 ? fillPointer : capacity;
  }

  public char charAt(int index) throws ConditionThrowable
  {
    if (chars != null)
      {
        try
          {
            return chars[index];
          }
        catch (ArrayIndexOutOfBoundsException e)
          {
            badIndex(index, capacity);
            return 0; // Not reached.
          }
      }
    else
      return LispCharacter.getValue(array.AREF(index + displacement));
  }

  public void setCharAt(int index, char c) throws ConditionThrowable
  {
    if (chars != null)
      {
        try
          {
            chars[index] = c;
          }
        catch (ArrayIndexOutOfBoundsException e)
          {
            badIndex(index, capacity);
          }
      }
    else
      array.aset(index + displacement, LispCharacter.getInstance(c));
  }

  public LispObject elt(int index) throws ConditionThrowable
  {
    final int limit = length();
    if (index < 0 || index >= limit)
      badIndex(index, limit);
    return LispCharacter.getInstance(charAt(index));
  }

  // Ignores fill pointer.
  public LispObject CHAR(int index) throws ConditionThrowable
  {
    return LispCharacter.getInstance(charAt(index));
  }

  // Ignores fill pointer.
  public LispObject AREF(int index) throws ConditionThrowable
  {
    return LispCharacter.getInstance(charAt(index));
  }

  // Ignores fill pointer.
  public LispObject AREF(LispObject index) throws ConditionThrowable
  {
    return LispCharacter.getInstance(charAt(Fixnum.getValue(index)));
  }

  public void aset(int index, LispObject newValue) throws ConditionThrowable
  {
    try
      {
        setCharAt(index, ((LispCharacter)newValue).value);
      }
    catch (ClassCastException e)
      {
        type_error(newValue, Symbol.CHARACTER);
      }
  }

  public void vectorPushExtend(LispObject element)
    throws ConditionThrowable
  {
    if (fillPointer < 0)
      noFillPointer();
    if (fillPointer >= capacity)
      {
        // Need to extend vector.
        ensureCapacity(capacity * 2 + 1);
      }
    if (chars != null)
      {
        try
          {
            chars[fillPointer] = ((LispCharacter)element).value;
          }
        catch (ClassCastException e)
          {
            type_error(element, Symbol.CHARACTER);
          }
      }
    else
      array.aset(fillPointer + displacement, element);
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
    if (fillPointer >= capacity)
      {
        // Need to extend vector.
        ext = Math.max(ext, capacity + 1);
        ensureCapacity(capacity + ext);
      }
    if (chars != null)
      {
        try
          {
            chars[fillPointer] = ((LispCharacter)element).value;
          }
        catch (ClassCastException e)
          {
            type_error(element, Symbol.CHARACTER);
          }
      }
    else
      array.aset(fillPointer + displacement, element);
    return new Fixnum(fillPointer++);
  }

  public final void ensureCapacity(int minCapacity) throws ConditionThrowable
  {
    if (chars != null)
      {
        if (capacity < minCapacity)
          {
            char[] newArray = new char[minCapacity];
            System.arraycopy(chars, 0, newArray, 0, capacity);
            chars = newArray;
            capacity = minCapacity;
          }
      }
    else
      {
        Debug.assertTrue(array != null);
        if (capacity < minCapacity ||
            array.getTotalSize() - displacement < minCapacity)
          {
            // Copy array.
            chars = new char[minCapacity];
            final int limit =
              Math.min(capacity, array.getTotalSize() - displacement);
            if (array instanceof AbstractString)
              {
                AbstractString string = (AbstractString) array;
                for (int i = 0; i < limit; i++)
                  {
                    chars[i] = string.charAt(displacement + i);
                  }
              }
            else
              {
                for (int i = 0; i < limit; i++)
                  {
                    LispCharacter character =
                      (LispCharacter) array.AREF(displacement + i);
                    chars[i] = character.value;
                  }
              }
            capacity = minCapacity;
            array = null;
            displacement = 0;
            isDisplaced = false;
          }
      }
  }

  public int sxhash()
  {
    int hashCode = 0;
    final int limit = length();
    for (int i = 0; i < limit; i++)
      {
        try
          {
            hashCode += charAt(i);
          }
        catch (ConditionThrowable t)
          {
            Debug.trace(t);
          }
        hashCode += (hashCode << 10);
        hashCode ^= (hashCode >> 6);
      }
    hashCode += (hashCode << 3);
    hashCode ^= (hashCode >> 11);
    hashCode += (hashCode << 15);
    return (hashCode & 0x7fffffff);
  }

  // For EQUALP hash tables.
  public int psxhash()
  {
    int hashCode = 0;
    final int limit = length();
    for (int i = 0; i < limit; i++)
      {
        try
          {
            hashCode += Character.toUpperCase(charAt(i));
          }
        catch (ConditionThrowable t)
          {
            Debug.trace(t);
          }
        hashCode += (hashCode << 10);
        hashCode ^= (hashCode >> 6);
      }
    hashCode += (hashCode << 3);
    hashCode ^= (hashCode >> 11);
    hashCode += (hashCode << 15);
    return (hashCode & 0x7fffffff);
  }

  public AbstractVector adjustVector(int newCapacity,
                                     LispObject initialElement,
                                     LispObject initialContents)
    throws ConditionThrowable
  {
    if (initialContents != NIL)
      {
        // "If INITIAL-CONTENTS is supplied, it is treated as for MAKE-
        // ARRAY. In this case none of the original contents of array
        // appears in the resulting array."
        char[] newChars = new char[newCapacity];
        if (initialContents.listp())
          {
            LispObject list = initialContents;
            for (int i = 0; i < newCapacity; i++)
              {
                newChars[i] = LispCharacter.getValue(list.car());
                list = list.cdr();
              }
          }
        else if (initialContents.vectorp())
          {
            for (int i = 0; i < newCapacity; i++)
              newChars[i] = LispCharacter.getValue(initialContents.elt(i));
          }
        else
          type_error(initialContents, Symbol.SEQUENCE);
        chars = newChars;
      }
    else
      {
        if (chars == null)
          {
            // Displaced array. Copy existing characters.
            chars = new char[newCapacity];
            final int limit = Math.min(capacity, newCapacity);
            if (array instanceof AbstractString)
              {
                AbstractString string = (AbstractString) array;
                for (int i = 0; i < limit; i++)
                  {
                    chars[i] = string.charAt(displacement + i);
                  }
              }
            else
              {
                for (int i = 0; i < limit; i++)
                  {
                    LispCharacter character =
                      (LispCharacter) array.AREF(displacement + i);
                    chars[i] = character.value;
                  }
              }
          }
        else if (capacity != newCapacity)
          {
            char[] newElements = new char[newCapacity];
            System.arraycopy(chars, 0, newElements, 0,
                             Math.min(capacity, newCapacity));
            chars = newElements;
          }
        if (initialElement != NIL && capacity < newCapacity)
          {
            // Initialize new elements.
            final char c = LispCharacter.getValue(initialElement);
            for (int i = capacity; i < newCapacity; i++)
              chars[i] = c;
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
    chars = null;
    isDisplaced = true;
    return this;
  }
}
