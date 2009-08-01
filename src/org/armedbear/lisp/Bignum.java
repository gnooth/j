/*
 * Bignum.java
 *
 * Copyright (C) 2003-2007 Peter Graves
 * $Id: Bignum.java,v 1.83 2007/02/23 21:17:32 piso Exp $
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

import java.math.BigInteger;

public final class Bignum extends LispObject
{
  public final BigInteger value;

  public Bignum(long l)
  {
    value = BigInteger.valueOf(l);
  }

  public Bignum(BigInteger n)
  {
    value = n;
  }

  public Bignum(String s, int radix)
  {
    value = new BigInteger(s, radix);
  }

  public Object javaInstance()
  {
    return value;
  }

  public LispObject typeOf()
  {
    if (value.signum() > 0)
      return list2(Symbol.INTEGER,
                   new Bignum((long)Integer.MAX_VALUE + 1));
    return Symbol.BIGNUM;
  }

  public LispObject classOf()
  {
    return BuiltInClass.BIGNUM;
  }

  public LispObject typep(LispObject type) throws ConditionThrowable
  {
    if (type instanceof Symbol)
      {
        if (type == Symbol.BIGNUM)
          return T;
        if (type == Symbol.INTEGER)
          return T;
        if (type == Symbol.RATIONAL)
          return T;
        if (type == Symbol.REAL)
          return T;
        if (type == Symbol.NUMBER)
          return T;
        if (type == Symbol.SIGNED_BYTE)
          return T;
        if (type == Symbol.UNSIGNED_BYTE)
          return value.signum() >= 0 ? T : NIL;
      }
    else if (type instanceof LispClass)
      {
        if (type == BuiltInClass.BIGNUM)
          return T;
        if (type == BuiltInClass.INTEGER)
          return T;
        if (type == BuiltInClass.RATIONAL)
          return T;
        if (type == BuiltInClass.REAL)
          return T;
        if (type == BuiltInClass.NUMBER)
          return T;
      }
    else if (type instanceof Cons)
      {
        if (type.equal(UNSIGNED_BYTE_8))
          return NIL;
        if (type.equal(UNSIGNED_BYTE_32))
          {
            if (minusp())
              return NIL;
            return isLessThan(UNSIGNED_BYTE_32_MAX_VALUE) ? T : NIL;
          }
      }
    return super.typep(type);
  }

  public LispObject NUMBERP()
  {
    return T;
  }

  public boolean numberp()
  {
    return true;
  }

  public LispObject INTEGERP()
  {
    return T;
  }

  public boolean integerp()
  {
    return true;
  }

  public boolean rationalp()
  {
    return true;
  }

  public boolean realp()
  {
    return true;
  }

  public boolean eql(LispObject obj)
  {
    if (this == obj)
      return true;
    if (obj instanceof Bignum)
      {
        if (value.equals(((Bignum)obj).value))
          return true;
      }
    return false;
  }

  public boolean equal(LispObject obj)
  {
    if (this == obj)
      return true;
    if (obj instanceof Bignum)
      {
        if (value.equals(((Bignum)obj).value))
          return true;
      }
    return false;
  }

  public boolean equalp(LispObject obj) throws ConditionThrowable
  {
    if (obj instanceof Bignum)
      return value.equals(((Bignum)obj).value);
    if (obj instanceof SingleFloat)
      return floatValue() == ((SingleFloat)obj).value;
    if (obj instanceof DoubleFloat)
      return doubleValue() == ((DoubleFloat)obj).value;
    return false;
  }

  public LispObject ABS()
  {
    if (value.signum() >= 0)
      return this;
    return new Bignum(value.negate());
  }

  public LispObject NUMERATOR()
  {
    return this;
  }

  public LispObject DENOMINATOR()
  {
    return Fixnum.ONE;
  }

  public boolean evenp() throws ConditionThrowable
  {
    return !value.testBit(0);
  }

  public boolean oddp() throws ConditionThrowable
  {
    return value.testBit(0);
  }

  public boolean plusp()
  {
    return value.signum() > 0;
  }

  public boolean minusp()
  {
    return value.signum() < 0;
  }

  public boolean zerop()
  {
    return false;
  }

  public int intValue()
  {
    return value.intValue();
  }

  public long longValue()
  {
    return value.longValue();
  }

  public float floatValue() throws ConditionThrowable
  {
    float f = value.floatValue();
    if (Float.isInfinite(f))
      error(new TypeError("The value " + writeToString() +
                           " is too large to be converted to a single float."));
    return f;
  }

  public double doubleValue() throws ConditionThrowable
  {
    double d = value.doubleValue();
    if (Double.isInfinite(d))
      error(new TypeError("The value " + writeToString() +
                           " is too large to be converted to a double float."));
    return d;
  }

  public static BigInteger getValue(LispObject obj) throws ConditionThrowable
  {
    try
      {
        return ((Bignum)obj).value;
      }
    catch (ClassCastException e)
      {
        type_error(obj, Symbol.BIGNUM);
        // Not reached.
        return null;
      }
  }

  public final LispObject incr()
  {
    return number(value.add(BigInteger.ONE));
  }

  public final LispObject decr()
  {
    return number(value.subtract(BigInteger.ONE));
  }

  public LispObject add(int n) throws ConditionThrowable
  {
    return number(value.add(BigInteger.valueOf(n)));
  }

  public LispObject add(LispObject obj) throws ConditionThrowable
  {
    if (obj instanceof Fixnum)
      return number(value.add(Fixnum.getBigInteger(obj)));
    if (obj instanceof Bignum)
      return number(value.add(((Bignum)obj).value));
    if (obj instanceof Ratio)
      {
        BigInteger numerator = ((Ratio)obj).numerator();
        BigInteger denominator = ((Ratio)obj).denominator();
        return number(value.multiply(denominator).add(numerator),
                      denominator);
      }
    if (obj instanceof SingleFloat)
      return new SingleFloat(floatValue() + ((SingleFloat)obj).value);
    if (obj instanceof DoubleFloat)
      return new DoubleFloat(doubleValue() + ((DoubleFloat)obj).value);
    if (obj instanceof Complex)
      {
        Complex c = (Complex) obj;
        return Complex.getInstance(add(c.getRealPart()), c.getImaginaryPart());
      }
    return type_error(obj, Symbol.NUMBER);
  }

  public LispObject subtract(LispObject obj) throws ConditionThrowable
  {
    if (obj instanceof Fixnum)
      return number(value.subtract(Fixnum.getBigInteger(obj)));
    if (obj instanceof Bignum)
      return number(value.subtract(((Bignum)obj).value));
    if (obj instanceof Ratio)
      {
        BigInteger numerator = ((Ratio)obj).numerator();
        BigInteger denominator = ((Ratio)obj).denominator();
        return number(value.multiply(denominator).subtract(numerator),
                      denominator);
      }
    if (obj instanceof SingleFloat)
      return new SingleFloat(floatValue() - ((SingleFloat)obj).value);
    if (obj instanceof DoubleFloat)
      return new DoubleFloat(doubleValue() - ((DoubleFloat)obj).value);
    if (obj instanceof Complex)
      {
        Complex c = (Complex) obj;
        return Complex.getInstance(subtract(c.getRealPart()),
                                   Fixnum.ZERO.subtract(c.getImaginaryPart()));
      }
    return type_error(obj, Symbol.NUMBER);
  }

  public LispObject multiplyBy(int n) throws ConditionThrowable
  {
    if (n == 0)
      return Fixnum.ZERO;
    if (n == 1)
      return this;
    return new Bignum(value.multiply(BigInteger.valueOf(n)));
  }

  public LispObject multiplyBy(LispObject obj) throws ConditionThrowable
  {
    if (obj instanceof Fixnum)
      {
        int n = ((Fixnum)obj).value;
        if (n == 0)
          return Fixnum.ZERO;
        if (n == 1)
          return this;
        return new Bignum(value.multiply(BigInteger.valueOf(n)));
      }
    if (obj instanceof Bignum)
      return new Bignum(value.multiply(((Bignum)obj).value));
    if (obj instanceof Ratio)
      {
        BigInteger n = ((Ratio)obj).numerator();
        return number(n.multiply(value), ((Ratio)obj).denominator());
      }
    if (obj instanceof SingleFloat)
      return new SingleFloat(floatValue() * ((SingleFloat)obj).value);
    if (obj instanceof DoubleFloat)
      return new DoubleFloat(doubleValue() * ((DoubleFloat)obj).value);
    if (obj instanceof Complex)
      {
        Complex c = (Complex) obj;
        return Complex.getInstance(multiplyBy(c.getRealPart()),
                                   multiplyBy(c.getImaginaryPart()));
      }
    return type_error(obj, Symbol.NUMBER);
  }

  public LispObject divideBy(LispObject obj) throws ConditionThrowable
  {
    if (obj instanceof Fixnum)
      return number(value, Fixnum.getBigInteger(obj));
    if (obj instanceof Bignum)
      return number(value, ((Bignum)obj).value);
    if (obj instanceof Ratio)
      {
        BigInteger d = ((Ratio)obj).denominator();
        return number(d.multiply(value), ((Ratio)obj).numerator());
      }
    if (obj instanceof SingleFloat)
      return new SingleFloat(floatValue() / ((SingleFloat)obj).value);
    if (obj instanceof DoubleFloat)
      return new DoubleFloat(doubleValue() / ((DoubleFloat)obj).value);
    if (obj instanceof Complex)
      {
        Complex c = (Complex) obj;
        LispObject realPart = c.getRealPart();
        LispObject imagPart = c.getImaginaryPart();
        LispObject denominator =
          realPart.multiplyBy(realPart).add(imagPart.multiplyBy(imagPart));
        return Complex.getInstance(multiplyBy(realPart).divideBy(denominator),
                                   Fixnum.ZERO.subtract(multiplyBy(imagPart).divideBy(denominator)));
      }
    return type_error(obj, Symbol.NUMBER);
  }

  public boolean isEqualTo(LispObject obj) throws ConditionThrowable
  {
    if (obj instanceof Bignum)
      return value.equals(((Bignum)obj).value);
    if (obj instanceof SingleFloat)
      return isEqualTo(((SingleFloat)obj).rational());
    if (obj instanceof DoubleFloat)
      return isEqualTo(((DoubleFloat)obj).rational());
    if (obj.numberp())
      return false;
    type_error(obj, Symbol.NUMBER);
    // Not reached.
    return false;
  }

  public boolean isNotEqualTo(LispObject obj) throws ConditionThrowable
  {
    if (obj instanceof Bignum)
      return !value.equals(((Bignum)obj).value);
    if (obj instanceof SingleFloat)
      return isNotEqualTo(((SingleFloat)obj).rational());
    if (obj instanceof DoubleFloat)
      return isNotEqualTo(((DoubleFloat)obj).rational());
    if (obj.numberp())
      return true;
    type_error(obj, Symbol.NUMBER);
    // Not reached.
    return false;
  }

  public boolean isLessThan(LispObject obj) throws ConditionThrowable
  {
    if (obj instanceof Fixnum)
      return value.compareTo(Fixnum.getBigInteger(obj)) < 0;
    if (obj instanceof Bignum)
      return value.compareTo(((Bignum)obj).value) < 0;
    if (obj instanceof Ratio)
      {
        BigInteger n = value.multiply(((Ratio)obj).denominator());
        return n.compareTo(((Ratio)obj).numerator()) < 0;
      }
    if (obj instanceof SingleFloat)
      return isLessThan(((SingleFloat)obj).rational());
    if (obj instanceof DoubleFloat)
      return isLessThan(((DoubleFloat)obj).rational());
    type_error(obj, Symbol.REAL);
    // Not reached.
    return false;
  }

  public boolean isGreaterThan(LispObject obj) throws ConditionThrowable
  {
    if (obj instanceof Fixnum)
      return value.compareTo(Fixnum.getBigInteger(obj)) > 0;
    if (obj instanceof Bignum)
      return value.compareTo(((Bignum)obj).value) > 0;
    if (obj instanceof Ratio)
      {
        BigInteger n = value.multiply(((Ratio)obj).denominator());
        return n.compareTo(((Ratio)obj).numerator()) > 0;
      }
    if (obj instanceof SingleFloat)
      return isGreaterThan(((SingleFloat)obj).rational());
    if (obj instanceof DoubleFloat)
      return isGreaterThan(((DoubleFloat)obj).rational());
    type_error(obj, Symbol.REAL);
    // Not reached.
    return false;
  }

  public boolean isLessThanOrEqualTo(LispObject obj) throws ConditionThrowable
  {
    if (obj instanceof Fixnum)
      return value.compareTo(Fixnum.getBigInteger(obj)) <= 0;
    if (obj instanceof Bignum)
      return value.compareTo(((Bignum)obj).value) <= 0;
    if (obj instanceof Ratio)
      {
        BigInteger n = value.multiply(((Ratio)obj).denominator());
        return n.compareTo(((Ratio)obj).numerator()) <= 0;
      }
    if (obj instanceof SingleFloat)
      return isLessThanOrEqualTo(((SingleFloat)obj).rational());
    if (obj instanceof DoubleFloat)
      return isLessThanOrEqualTo(((DoubleFloat)obj).rational());
    type_error(obj, Symbol.REAL);
    // Not reached.
    return false;
  }

  public boolean isGreaterThanOrEqualTo(LispObject obj) throws ConditionThrowable
  {
    if (obj instanceof Fixnum)
      return value.compareTo(Fixnum.getBigInteger(obj)) >= 0;
    if (obj instanceof Bignum)
      return value.compareTo(((Bignum)obj).value) >= 0;
    if (obj instanceof Ratio)
      {
        BigInteger n = value.multiply(((Ratio)obj).denominator());
        return n.compareTo(((Ratio)obj).numerator()) >= 0;
      }
    if (obj instanceof SingleFloat)
      return isGreaterThanOrEqualTo(((SingleFloat)obj).rational());
    if (obj instanceof DoubleFloat)
      return isGreaterThanOrEqualTo(((DoubleFloat)obj).rational());
    type_error(obj, Symbol.REAL);
    // Not reached.
    return false;
  }

  public LispObject truncate(LispObject obj) throws ConditionThrowable
  {
    final LispThread thread = LispThread.currentThread();
    LispObject value1, value2;
    try
      {
        if (obj instanceof Fixnum)
          {
            BigInteger divisor = ((Fixnum)obj).getBigInteger();
            BigInteger[] results = value.divideAndRemainder(divisor);
            BigInteger quotient = results[0];
            BigInteger remainder = results[1];
            value1 = number(quotient);
            value2 = (remainder.signum() == 0) ? Fixnum.ZERO : number(remainder);
          }
        else if (obj instanceof Bignum)
          {
            BigInteger divisor = ((Bignum)obj).value;
            BigInteger[] results = value.divideAndRemainder(divisor);
            BigInteger quotient = results[0];
            BigInteger remainder = results[1];
            value1 = number(quotient);
            value2 = (remainder.signum() == 0) ? Fixnum.ZERO : number(remainder);
          }
        else if (obj instanceof Ratio)
          {
            Ratio divisor = (Ratio) obj;
            LispObject quotient =
              multiplyBy(divisor.DENOMINATOR()).truncate(divisor.NUMERATOR());
            LispObject remainder =
              subtract(quotient.multiplyBy(divisor));
            value1 = quotient;
            value2 = remainder;
          }
        else if (obj instanceof SingleFloat)
          {
            // "When rationals and floats are combined by a numerical
            // function, the rational is first converted to a float of the
            // same format." 12.1.4.1
            return new SingleFloat(floatValue()).truncate(obj);
          }
        else if (obj instanceof DoubleFloat)
          {
            // "When rationals and floats are combined by a numerical
            // function, the rational is first converted to a float of the
            // same format." 12.1.4.1
            return new DoubleFloat(doubleValue()).truncate(obj);
          }
        else
          return type_error(obj, Symbol.REAL);
      }
    catch (ArithmeticException e)
      {
        if (obj.zerop())
          return error(new DivisionByZero());
        else
          return error(new ArithmeticError(e.getMessage()));
      }
    return thread.setValues(value1, value2);
  }

  public LispObject ash(LispObject obj) throws ConditionThrowable
  {
    BigInteger n = value;
    if (obj instanceof Fixnum)
      {
        int count = ((Fixnum)obj).value;
        if (count == 0)
          return this;
        // BigInteger.shiftLeft() succumbs to a stack overflow if count
        // is Integer.MIN_VALUE, so...
        if (count == Integer.MIN_VALUE)
          return n.signum() >= 0 ? Fixnum.ZERO : Fixnum.MINUS_ONE;
        return number(n.shiftLeft(count));
      }
    if (obj instanceof Bignum)
      {
        BigInteger count = ((Bignum)obj).value;
        if (count.signum() > 0)
          return error(new LispError("Can't represent result of left shift."));
        if (count.signum() < 0)
          return n.signum() >= 0 ? Fixnum.ZERO : Fixnum.MINUS_ONE;
        Debug.bug(); // Shouldn't happen.
      }
    return type_error(obj, Symbol.INTEGER);
  }

  public LispObject LOGNOT()
  {
    return number(value.not());
  }

  public LispObject LOGAND(int n) throws ConditionThrowable
  {
    if (n >= 0)
      return new Fixnum(value.intValue() & n);
    else
      return number(value.and(BigInteger.valueOf(n)));
  }

  public LispObject LOGAND(LispObject obj) throws ConditionThrowable
  {
    if (obj instanceof Fixnum)
      {
        int n = ((Fixnum)obj).value;
        if (n >= 0)
          return new Fixnum(value.intValue() & n);
        else
          return number(value.and(BigInteger.valueOf(n)));
      }
    else if (obj instanceof Bignum)
      {
        final BigInteger n = ((Bignum)obj).value;
        return number(value.and(n));
      }
    else
      return type_error(obj, Symbol.INTEGER);
  }

  public LispObject LOGIOR(int n) throws ConditionThrowable
  {
    return number(value.or(BigInteger.valueOf(n)));
  }

  public LispObject LOGIOR(LispObject obj) throws ConditionThrowable
  {
    if (obj instanceof Fixnum)
      {
        final BigInteger n = ((Fixnum)obj).getBigInteger();
        return number(value.or(n));
      }
    else if (obj instanceof Bignum)
      {
        final BigInteger n = ((Bignum)obj).value;
        return number(value.or(n));
      }
    else
      return type_error(obj, Symbol.INTEGER);
  }

  public LispObject LOGXOR(int n) throws ConditionThrowable
  {
    return number(value.xor(BigInteger.valueOf(n)));
  }

  public LispObject LOGXOR(LispObject obj) throws ConditionThrowable
  {
    final BigInteger n;
    if (obj instanceof Fixnum)
      n = ((Fixnum)obj).getBigInteger();
    else if (obj instanceof Bignum)
      n = ((Bignum)obj).value;
    else
      return type_error(obj, Symbol.INTEGER);
    return number(value.xor(n));
  }

  public LispObject LDB(int size, int position)
  {
    BigInteger n = value.shiftRight(position);
    BigInteger mask = BigInteger.ONE.shiftLeft(size).subtract(BigInteger.ONE);
    return number(n.and(mask));
  }

  public int hashCode()
  {
    return value.hashCode();
  }

  public String writeToString() throws ConditionThrowable
  {
    final LispThread thread = LispThread.currentThread();
    final int base = Fixnum.getValue(Symbol.PRINT_BASE.symbolValue(thread));
    String s = value.toString(base).toUpperCase();
    if (Symbol.PRINT_RADIX.symbolValue(thread) != NIL)
      {
        StringBuffer sb = new StringBuffer();
        switch (base)
          {
          case 2:
            sb.append("#b");
            sb.append(s);
            break;
          case 8:
            sb.append("#o");
            sb.append(s);
            break;
          case 10:
            sb.append(s);
            sb.append('.');
            break;
          case 16:
            sb.append("#x");
            sb.append(s);
            break;
          default:
            sb.append('#');
            sb.append(String.valueOf(base));
            sb.append('r');
            sb.append(s);
            break;
          }
        s = sb.toString();
      }
    return s;
  }
}
