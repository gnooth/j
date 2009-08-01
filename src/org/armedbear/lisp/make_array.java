/*
 * make_array.java
 *
 * Copyright (C) 2003-2005 Peter Graves
 * $Id: make_array.java,v 1.32 2007/02/23 21:17:36 piso Exp $
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

// ### %make-array dimensions element-type initial-element initial-element-p
// initial-contents adjustable fill-pointer displaced-to displaced-index-offset
// => new-array
public final class make_array extends Primitive
{
  public make_array()
  {
    super("%make-array", PACKAGE_SYS, false);
  }

  public LispObject execute(LispObject[] args) throws ConditionThrowable
  {
    if (args.length != 9)
      return error(new WrongNumberOfArgumentsException(this));
    LispObject dimensions = args[0];
    LispObject elementType = args[1];
    LispObject initialElement = args[2];
    LispObject initialElementProvided = args[3];
    LispObject initialContents = args[4];
    LispObject adjustable = args[5];
    LispObject fillPointer = args[6];
    LispObject displacedTo = args[7];
    LispObject displacedIndexOffset = args[8];
    if (initialElementProvided != NIL && initialContents != NIL)
      {
        return error(new LispError("MAKE-ARRAY: cannot specify both " +
                                    "initial element and initial contents."));
      }
    final int rank = dimensions.listp() ? dimensions.length() : 1;
    int[] dimv = new int[rank];
    if (dimensions.listp())
      {
        for (int i = 0; i < rank; i++)
          {
            LispObject dim = dimensions.car();
            dimv[i] = Fixnum.getValue(dim);
            dimensions = dimensions.cdr();
          }
      }
    else
      dimv[0] = Fixnum.getValue(dimensions);
    if (displacedTo != NIL)
      {
        // FIXME Make sure element type (if specified) is compatible with
        // displaced-to array.
        final AbstractArray array = checkArray(displacedTo);
        if (initialElementProvided != NIL)
          return error(new LispError("Initial element must not be specified for a displaced array."));
        if (initialContents != NIL)
          return error(new LispError("Initial contents must not be specified for a displaced array."));
        final int displacement;
        if (displacedIndexOffset != NIL)
          displacement = Fixnum.getValue(displacedIndexOffset);
        else
          displacement = 0;
        if (rank == 1)
          {
            AbstractVector v;
            LispObject arrayElementType = array.getElementType();
            if (arrayElementType == Symbol.CHARACTER)
              v = new ComplexString(dimv[0], array, displacement);
            else if (arrayElementType == Symbol.BIT)
              v = new ComplexBitVector(dimv[0], array, displacement);
            else if (arrayElementType.equal(UNSIGNED_BYTE_8))
              v = new ComplexVector_UnsignedByte8(dimv[0], array, displacement);
            else if (arrayElementType.equal(UNSIGNED_BYTE_32))
              v = new ComplexVector_UnsignedByte32(dimv[0], array, displacement);
            else
              v = new ComplexVector(dimv[0], array, displacement);
            if (fillPointer != NIL)
              v.setFillPointer(fillPointer);
            return v;
          }
        return new ComplexArray(dimv, array, displacement);
      }
    LispObject upgradedType = getUpgradedArrayElementType(elementType);
    if (rank == 0)
      {
        LispObject data;
        if (initialElementProvided != NIL)
          data = initialElement;
        else
          data = initialContents;
        return new ZeroRankArray(upgradedType, data, adjustable != NIL);
      }
    if (rank == 1)
      {
        final int size = dimv[0];
        if (size < 0 || size >= ARRAY_DIMENSION_MAX)
          {
            FastStringBuffer sb = new FastStringBuffer();
            sb.append("The size specified for this array (");
            sb.append(size);
            sb.append(')');
            if (size >= ARRAY_DIMENSION_MAX)
              {
                sb.append(" is >= ARRAY-DIMENSION-LIMIT (");
                sb.append(ARRAY_DIMENSION_MAX);
                sb.append(").");
              }
            else
              sb.append(" is negative.");
            return error(new LispError(sb.toString()));
          }
        final AbstractVector v;
        if (upgradedType == Symbol.CHARACTER)
          {
            if (fillPointer != NIL || adjustable != NIL)
              v = new ComplexString(size);
            else
              v = new SimpleString(size);
          }
        else if (upgradedType == Symbol.BIT)
          {
            if (fillPointer != NIL || adjustable != NIL)
              v = new ComplexBitVector(size);
            else
              v = new SimpleBitVector(size);
          }
        else if (upgradedType.equal(UNSIGNED_BYTE_8))
          {
            if (fillPointer != NIL || adjustable != NIL)
              v = new ComplexVector_UnsignedByte8(size);
            else
              v = new BasicVector_UnsignedByte8(size);
          }
        else if (upgradedType.equal(UNSIGNED_BYTE_16) &&
                 fillPointer == NIL && adjustable == NIL)
          {
            v = new BasicVector_UnsignedByte16(size);
          }
        else if (upgradedType.equal(UNSIGNED_BYTE_32))
          {
            if (fillPointer != NIL || adjustable != NIL)
              v = new ComplexVector_UnsignedByte32(size);
            else
              v = new BasicVector_UnsignedByte32(size);
          }
        else if (upgradedType == NIL)
          v = new NilVector(size);
        else
          {
            if (fillPointer != NIL || adjustable != NIL)
              v = new ComplexVector(size);
            else
              v = new SimpleVector(size);
          }
        if (initialElementProvided != NIL)
          {
            // Initial element was specified.
            v.fill(initialElement);
          }
        else if (initialContents != NIL)
          {
            if (initialContents.listp())
              {
                LispObject list = initialContents;
                for (int i = 0; i < size; i++)
                  {
                    v.aset(i, list.car());
                    list = list.cdr();
                  }
              }
            else if (initialContents.vectorp())
              {
                for (int i = 0; i < size; i++)
                  v.aset(i, initialContents.elt(i));
              }
            else
              return type_error(initialContents, Symbol.SEQUENCE);
          }
        if (fillPointer != NIL)
          v.setFillPointer(fillPointer);
        return v;
      }
    // rank > 1
    AbstractArray array;
    if (adjustable == NIL)
      {
        if (upgradedType.equal(UNSIGNED_BYTE_8))
          {
            if (initialContents != NIL)
              {
                array = new SimpleArray_UnsignedByte8(dimv, initialContents);
              }
            else
              {
                array = new SimpleArray_UnsignedByte8(dimv);
                if (initialElementProvided != NIL)
                  array.fill(initialElement);
              }
          }
        else if (upgradedType.equal(UNSIGNED_BYTE_16))
          {
            if (initialContents != NIL)
              {
                array = new SimpleArray_UnsignedByte16(dimv, initialContents);
              }
            else
              {
                array = new SimpleArray_UnsignedByte16(dimv);
                if (initialElementProvided != NIL)
                  array.fill(initialElement);
              }
          }
        else if (upgradedType.equal(UNSIGNED_BYTE_32))
          {
            if (initialContents != NIL)
              {
                array = new SimpleArray_UnsignedByte32(dimv, initialContents);
              }
            else
              {
                array = new SimpleArray_UnsignedByte32(dimv);
                if (initialElementProvided != NIL)
                  array.fill(initialElement);
              }
          }
        else
          {
            if (initialContents != NIL)
              {
                array = new SimpleArray_T(dimv, upgradedType, initialContents);
              }
            else
              {
                array = new SimpleArray_T(dimv, upgradedType);
                if (initialElementProvided != NIL)
                  array.fill(initialElement);
              }
          }
      }
    else
      {
        // Adjustable.
        if (upgradedType.equal(UNSIGNED_BYTE_8))
          {
            if (initialContents != NIL)
              {
                array = new ComplexArray_UnsignedByte8(dimv, initialContents);
              }
            else
              {
                array = new ComplexArray_UnsignedByte8(dimv);
                if (initialElementProvided != NIL)
                  array.fill(initialElement);
              }
          }
        else if (upgradedType.equal(UNSIGNED_BYTE_32))
          {
            if (initialContents != NIL)
              {
                array = new ComplexArray_UnsignedByte32(dimv, initialContents);
              }
            else
              {
                array = new ComplexArray_UnsignedByte32(dimv);
                if (initialElementProvided != NIL)
                  array.fill(initialElement);
              }
          }
        else
          {
            if (initialContents != NIL)
              {
                array = new ComplexArray(dimv, upgradedType, initialContents);
              }
            else
              {
                array = new ComplexArray(dimv, upgradedType);
                if (initialElementProvided != NIL)
                  array.fill(initialElement);
              }
          }
      }
    return array;
  }

  private static final Primitive _MAKE_ARRAY = new make_array();
}
