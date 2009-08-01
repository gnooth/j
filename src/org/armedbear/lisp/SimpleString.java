/*
 * SimpleString.java
 *
 * Copyright (C) 2004-2005 Peter Graves
 * $Id: SimpleString.java,v 1.35 2007/02/23 21:17:34 piso Exp $
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

public final class SimpleString extends AbstractString
{
    private int capacity;
    private char[] chars;

    public SimpleString(LispCharacter c)
    {
        chars = new char[1];
        chars[0] = c.value;
        capacity = 1;
    }

    public SimpleString(char c)
    {
        chars = new char[1];
        chars[0] = c;
        capacity = 1;
    }

    public SimpleString(int capacity)
    {
        this.capacity = capacity;
        chars = new char[capacity];
    }

    public SimpleString(String s)
    {
        capacity = s.length();
        chars = s.toCharArray();
    }

    public SimpleString(StringBuffer sb)
    {
        chars = new char[capacity = sb.length()];
        sb.getChars(0, capacity, chars, 0);
    }

    public SimpleString(FastStringBuffer sb)
    {
        chars = sb.toCharArray();
        capacity = chars.length;
    }

    private SimpleString(char[] chars)
    {
        this.chars = chars;
        capacity = chars.length;
    }

    public char[] chars()
    {
        return chars;
    }

    public char[] getStringChars()
    {
        return chars;
    }

    public LispObject typeOf()
    {
        return list2(Symbol.SIMPLE_STRING, new Fixnum(capacity));
    }

    public LispObject classOf()
    {
        return BuiltInClass.SIMPLE_STRING;
    }

    public LispObject getDescription()
    {
        FastStringBuffer sb = new FastStringBuffer("A simple-string (");
        sb.append(capacity);
        sb.append(") \"");
        sb.append(chars);
        sb.append('"');
        return new SimpleString(sb);
    }

    public LispObject typep(LispObject type) throws ConditionThrowable
    {
        if (type == Symbol.SIMPLE_STRING)
            return T;
        if (type == Symbol.SIMPLE_ARRAY)
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

    public boolean hasFillPointer()
    {
        return false;
    }

    public boolean isAdjustable()
    {
        return false;
    }

    public boolean equal(LispObject obj) throws ConditionThrowable
    {
        if (this == obj)
            return true;
        if (obj instanceof SimpleString) {
            SimpleString string = (SimpleString) obj;
            if (string.capacity != capacity)
                return false;
            for (int i = capacity; i-- > 0;)
                if (string.chars[i] != chars[i])
                    return false;
            return true;
        }
        if (obj instanceof AbstractString) {
            AbstractString string = (AbstractString) obj;
            if (string.length() != capacity)
                return false;
            for (int i = length(); i-- > 0;)
                if (string.charAt(i) != chars[i])
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
        if (obj instanceof SimpleString) {
            SimpleString string = (SimpleString) obj;
            if (string.capacity != capacity)
                return false;
            for (int i = capacity; i-- > 0;) {
                if (string.chars[i] != chars[i]) {
                    if (LispCharacter.toLowerCase(string.chars[i]) != LispCharacter.toLowerCase(chars[i]))
                        return false;
                }
            }
            return true;
        }
        if (obj instanceof AbstractString) {
            AbstractString string = (AbstractString) obj;
            if (string.length() != capacity)
                return false;
            for (int i = length(); i-- > 0;) {
                if (string.charAt(i) != chars[i]) {
                    if (LispCharacter.toLowerCase(string.charAt(i)) != LispCharacter.toLowerCase(chars[i]))
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

    public final SimpleString substring(int start) throws ConditionThrowable
    {
        return substring(start, capacity);
    }

    public final SimpleString substring(int start, int end)
        throws ConditionThrowable
    {
        SimpleString s = new SimpleString(end - start);
        int i = start, j = 0;
        try {
            while (i < end)
                s.chars[j++] = chars[i++];
            return s;
        }
        catch (ArrayIndexOutOfBoundsException e) {
            error(new TypeError("Array index out of bounds: " + i));
            // Not reached.
            return null;
        }
    }

    public final LispObject subseq(int start, int end) throws ConditionThrowable
    {
        return substring(start, end);
    }

    public void fill(LispObject obj) throws ConditionThrowable
    {
        fill(LispCharacter.getValue(obj));
    }

    public void fill(char c)
    {
        for (int i = capacity; i-- > 0;)
            chars[i] = c;
    }

    public void shrink(int n) throws ConditionThrowable
    {
        if (n < capacity) {
            char[] newArray = new char[n];
            System.arraycopy(chars, 0, newArray, 0, n);
            chars = newArray;
            capacity = n;
            return;
        }
        if (n == capacity)
            return;
        error(new LispError());
    }

    public LispObject reverse() throws ConditionThrowable
    {
        SimpleString result = new SimpleString(capacity);
        int i, j;
        for (i = 0, j = capacity - 1; i < capacity; i++, j--)
            result.chars[i] = chars[j];
        return result;
    }

    public LispObject nreverse() throws ConditionThrowable
    {
        int i = 0;
        int j = capacity - 1;
        while (i < j) {
            char temp = chars[i];
            chars[i] = chars[j];
            chars[j] = temp;
            ++i;
            --j;
        }
        return this;
    }

    public String getStringValue()
    {
        return new String(chars);
    }

    public Object javaInstance()
    {
        return new String(chars);
    }

    public Object javaInstance(Class c)
    {
        return javaInstance();
    }

    public final int capacity()
    {
        return capacity;
    }

    public final int length()
    {
        return capacity;
    }

    public char charAt(int index) throws ConditionThrowable
    {
        try {
            return chars[index];
        }
        catch (ArrayIndexOutOfBoundsException e) {
            badIndex(index, capacity);
            return 0; // Not reached.
        }
    }

    public void setCharAt(int index, char c) throws ConditionThrowable
    {
        try {
            chars[index] = c;
        }
        catch (ArrayIndexOutOfBoundsException e) {
            badIndex(index, capacity);
        }
    }

    public LispObject elt(int index) throws ConditionThrowable
    {
        try {
            return LispCharacter.getInstance(chars[index]);
        }
        catch (ArrayIndexOutOfBoundsException e) {
            badIndex(index, capacity);
            return NIL; // Not reached.
        }
    }

    public LispObject CHAR(int index) throws ConditionThrowable
    {
        try {
            return LispCharacter.getInstance(chars[index]);
        }
        catch (ArrayIndexOutOfBoundsException e) {
            badIndex(index, capacity);
            return NIL; // Not reached.
        }
    }

    public LispObject SCHAR(int index) throws ConditionThrowable
    {
        try {
            return LispCharacter.getInstance(chars[index]);
        }
        catch (ArrayIndexOutOfBoundsException e) {
            badIndex(index, capacity);
            return NIL; // Not reached.
        }
    }

    public LispObject AREF(int index) throws ConditionThrowable
    {
        try {
            return LispCharacter.getInstance(chars[index]);
        }
        catch (ArrayIndexOutOfBoundsException e) {
            badIndex(index, capacity);
            return NIL; // Not reached.
        }
    }

    public LispObject AREF(LispObject index) throws ConditionThrowable
    {
        try {
            return LispCharacter.getInstance(chars[((Fixnum)index).value]);
        }
        catch (ClassCastException e) {
            return type_error(index, Symbol.FIXNUM);
        }
        catch (ArrayIndexOutOfBoundsException e) {
            badIndex(((Fixnum)index).value, capacity);
            return NIL; // Not reached.
        }
    }

    public void aset(int index, LispObject obj) throws ConditionThrowable
    {
        try {
            chars[index] = ((LispCharacter)obj).value;
        }
        catch (ArrayIndexOutOfBoundsException e) {
            badIndex(index, capacity);
        }
        catch (ClassCastException e) {
            type_error(obj, Symbol.CHARACTER);
        }
    }

    public int sxhash()
    {
        int hashCode = 0;
        for (int i = 0; i < capacity; i++) {
            hashCode += chars[i];
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
        for (int i = 0; i < capacity; i++) {
            hashCode += Character.toUpperCase(chars[i]);
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
        if (initialContents != NIL) {
            char[] newChars = new char[newCapacity];
            if (initialContents.listp()) {
                LispObject list = initialContents;
                for (int i = 0; i < newCapacity; i++) {
                    newChars[i] = LispCharacter.getValue(list.car());
                    list = list.cdr();
                }
            } else if (initialContents.vectorp()) {
                for (int i = 0; i < newCapacity; i++)
                    newChars[i] = LispCharacter.getValue(initialContents.elt(i));
            } else
                type_error(initialContents, Symbol.SEQUENCE);
            return new SimpleString(newChars);
        }
        if (capacity != newCapacity) {
            char[] newChars = new char[newCapacity];
            System.arraycopy(chars, 0, newChars, 0, Math.min(newCapacity, capacity));
            if (initialElement != NIL && capacity < newCapacity) {
                final char c = LispCharacter.getValue(initialElement);
                for (int i = capacity; i < newCapacity; i++)
                    newChars[i] = c;
            }
            return new SimpleString(newChars);
        }
        // No change.
        return this;
    }

    public AbstractVector adjustVector(int newCapacity,
                                       AbstractArray displacedTo,
                                       int displacement)
        throws ConditionThrowable
    {
        return new ComplexString(newCapacity, displacedTo, displacement);
    }
}
