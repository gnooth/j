/*
 * Fixnum.java
 *
 * Copyright (C) 2002-2006 Peter Graves
 * $Id: Fixnum.java,v 1.138 2007/02/23 21:17:33 piso Exp $
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

public final class Fixnum extends LispObject
{
  public static final Fixnum[] constants = new Fixnum[256];
  static
  {
    for (int i = 0; i < 256; i++)
      constants[i] = new Fixnum(i);
  }

  public static final Fixnum ZERO      = constants[0];
  public static final Fixnum ONE       = constants[1];
  public static final Fixnum TWO       = constants[2];
  public static final Fixnum THREE     = constants[3];

  public static final Fixnum MINUS_ONE = new Fixnum(-1);

  public static Fixnum getInstance(int n)
  {
    return (n >= 0 && n < 256) ? constants[n] : new Fixnum(n);
  }

  public final int value;

  public Fixnum(int value)
  {
    this.value = value;
  }

  public Object javaInstance()
  {
    return new Integer(value);
  }

  public Object javaInstance(Class c)
  {
    String cn = c.getName();
    if (cn.equals("java.lang.Byte") || cn.equals("byte"))
      return new Byte(((Integer)javaInstance()).byteValue());
    if (cn.equals("java.lang.Short") || cn.equals("short"))
      return new Short(((Integer)javaInstance()).shortValue());
    if (cn.equals("java.lang.Long") || cn.equals("long"))
      return new Long(((Integer)javaInstance()).longValue());
    return javaInstance();
  }

  public LispObject typeOf()
  {
    if (value == 0 || value == 1)
      return Symbol.BIT;
    if (value > 1)
      return list3(Symbol.INTEGER, ZERO, new Fixnum(Integer.MAX_VALUE));
    return Symbol.FIXNUM;
  }

  public LispObject classOf()
  {
    return BuiltInClass.FIXNUM;
  }

  public LispObject getDescription()
  {
    StringBuffer sb = new StringBuffer("The fixnum ");
    sb.append(value);
    return new SimpleString(sb);
  }

  public LispObject typep(LispObject type) throws ConditionThrowable
  {
    if (type instanceof Symbol)
      {
        if (type == Symbol.FIXNUM)
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
          return value >= 0 ? T : NIL;
        if (type == Symbol.BIT)
          return (value == 0 || value == 1) ? T : NIL;
      }
    else if (type instanceof LispClass)
      {
        if (type == BuiltInClass.FIXNUM)
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
          return (value >= 0 && value <= 255) ? T : NIL;
        if (type.equal(UNSIGNED_BYTE_16))
          return (value >= 0 && value <= 65535) ? T : NIL;
        if (type.equal(UNSIGNED_BYTE_32))
          return value >= 0 ? T : NIL;
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

  public boolean eql(int n)
  {
    return value == n;
  }

  public boolean eql(LispObject obj)
  {
    if (this == obj)
      return true;
    if (obj instanceof Fixnum)
      {
        if (value == ((Fixnum)obj).value)
          return true;
      }
    return false;
  }

  public boolean equal(int n)
  {
    return value == n;
  }

  public boolean equal(LispObject obj)
  {
    if (this == obj)
      return true;
    if (obj instanceof Fixnum)
      {
        if (value == ((Fixnum)obj).value)
          return true;
      }
    return false;
  }

  public boolean equalp(int n)
  {
    return value == n;
  }

  public boolean equalp(LispObject obj)
  {
    if (obj instanceof Fixnum)
      return value == ((Fixnum)obj).value;
    if (obj instanceof SingleFloat)
      return value == ((SingleFloat)obj).value;
    if (obj instanceof DoubleFloat)
      return value == ((DoubleFloat)obj).value;
    return false;
  }

  public LispObject ABS()
  {
    if (value >= 0)
      return this;
    if (value > Integer.MIN_VALUE)
      return new Fixnum(-value);
    return new Bignum(-((long)Integer.MIN_VALUE));
  }

  public LispObject NUMERATOR()
  {
    return this;
  }

  public LispObject DENOMINATOR()
  {
    return ONE;
  }

  public boolean evenp() throws ConditionThrowable
  {
    return (value & 0x01) == 0;
  }

  public boolean oddp() throws ConditionThrowable
  {
    return (value & 0x01) != 0;
  }

  public boolean plusp()
  {
    return value > 0;
  }

  public boolean minusp()
  {
    return value < 0;
  }

  public boolean zerop()
  {
    return value == 0;
  }

  public static int getValue(LispObject obj) throws ConditionThrowable
  {
    try
      {
        return ((Fixnum)obj).value;
      }
    catch (ClassCastException e)
      {
        type_error(obj, Symbol.FIXNUM);
        // Not reached.
        return 0;
      }
  }

  public static int getInt(LispObject obj) throws ConditionThrowable
  {
    try
      {
        return (int) ((Fixnum)obj).value;
      }
    catch (ClassCastException e)
      {
        type_error(obj, Symbol.FIXNUM);
        // Not reached.
        return 0;
      }
  }

  public static BigInteger getBigInteger(LispObject obj) throws ConditionThrowable
  {
    try
      {
        return BigInteger.valueOf(((Fixnum)obj).value);
      }
    catch (ClassCastException e)
      {
        type_error(obj, Symbol.FIXNUM);
        // Not reached.
        return null;
      }
  }

  public int intValue()
  {
    return value;
  }

  public long longValue()
  {
    return (long) value;
  }

  public final BigInteger getBigInteger()
  {
    return BigInteger.valueOf(value);
  }

  public final LispObject incr()
  {
    if (value < Integer.MAX_VALUE)
      return new Fixnum(value + 1);
    return new Bignum((long) value + 1);
  }

  public final LispObject decr()
  {
    if (value > Integer.MIN_VALUE)
      return new Fixnum(value - 1);
    return new Bignum((long) value - 1);
  }

  public LispObject negate()
  {
    long result = 0L - value;
    if (result >= Integer.MIN_VALUE && result <= Integer.MAX_VALUE)
      return new Fixnum((int)result);
    else
      return new Bignum(result);
  }

  public LispObject add(int n)
  {
    long result = (long) value + n;
    if (result >= Integer.MIN_VALUE && result <= Integer.MAX_VALUE)
      return new Fixnum((int)result);
    else
      return new Bignum(result);
  }

  public LispObject add(LispObject obj) throws ConditionThrowable
  {
    if (obj instanceof Fixnum)
      {
        long result = (long) value + ((Fixnum)obj).value;
        if (result >= Integer.MIN_VALUE && result <= Integer.MAX_VALUE)
          return new Fixnum((int)result);
        else
          return new Bignum(result);
      }
    if (obj instanceof Bignum)
      return number(getBigInteger().add(((Bignum)obj).value));
    if (obj instanceof Ratio)
      {
        BigInteger numerator = ((Ratio)obj).numerator();
        BigInteger denominator = ((Ratio)obj).denominator();
        return number(getBigInteger().multiply(denominator).add(numerator),
                      denominator);
      }
    if (obj instanceof SingleFloat)
      return new SingleFloat(value + ((SingleFloat)obj).value);
    if (obj instanceof DoubleFloat)
      return new DoubleFloat(value + ((DoubleFloat)obj).value);
    if (obj instanceof Complex)
      {
        Complex c = (Complex) obj;
        return Complex.getInstance(add(c.getRealPart()), c.getImaginaryPart());
      }
    return type_error(obj, Symbol.NUMBER);
  }

  public LispObject subtract(int n)
  {
    long result = (long) value - n;
    if (result >= Integer.MIN_VALUE && result <= Integer.MAX_VALUE)
      return new Fixnum((int)result);
    else
      return new Bignum(result);
  }

  public LispObject subtract(LispObject obj) throws ConditionThrowable
  {
    if (obj instanceof Fixnum)
      return number((long) value - ((Fixnum)obj).value);
    if (obj instanceof Bignum)
      return number(getBigInteger().subtract(Bignum.getValue(obj)));
    if (obj instanceof Ratio)
      {
        BigInteger numerator = ((Ratio)obj).numerator();
        BigInteger denominator = ((Ratio)obj).denominator();
        return number(
          getBigInteger().multiply(denominator).subtract(numerator),
          denominator);
      }
    if (obj instanceof SingleFloat)
      return new SingleFloat(value - ((SingleFloat)obj).value);
    if (obj instanceof DoubleFloat)
      return new DoubleFloat(value - ((DoubleFloat)obj).value);
    if (obj instanceof Complex)
      {
        Complex c = (Complex) obj;
        return Complex.getInstance(subtract(c.getRealPart()),
                                   ZERO.subtract(c.getImaginaryPart()));
      }
    return type_error(obj, Symbol.NUMBER);
  }

  public LispObject multiplyBy(int n)
  {
    long result = (long) value * n;
    if (result >= Integer.MIN_VALUE && result <= Integer.MAX_VALUE)
      return new Fixnum((int)result);
    else
      return new Bignum(result);
  }

  public LispObject multiplyBy(LispObject obj) throws ConditionThrowable
  {
    if (obj instanceof Fixnum)
      {
        long result = (long) value * ((Fixnum)obj).value;
        if (result >= Integer.MIN_VALUE && result <= Integer.MAX_VALUE)
          return new Fixnum((int)result);
        else
          return new Bignum(result);
      }
    if (obj instanceof Bignum)
      return number(getBigInteger().multiply(((Bignum)obj).value));
    if (obj instanceof Ratio)
      {
        BigInteger numerator = ((Ratio)obj).numerator();
        BigInteger denominator = ((Ratio)obj).denominator();
        return number(
          getBigInteger().multiply(numerator),
          denominator);
      }
    if (obj instanceof SingleFloat)
      return new SingleFloat(value * ((SingleFloat)obj).value);
    if (obj instanceof DoubleFloat)
      return new DoubleFloat(value * ((DoubleFloat)obj).value);
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
    try
      {
        if (obj instanceof Fixnum)
          {
            final int divisor = ((Fixnum)obj).value;
            // (/ MOST-NEGATIVE-FIXNUM -1) is a bignum.
            if (value > Integer.MIN_VALUE)
              if (value % divisor == 0)
                return new Fixnum(value / divisor);
            return number(BigInteger.valueOf(value),
                          BigInteger.valueOf(divisor));
          }
        if (obj instanceof Bignum)
          return number(getBigInteger(), ((Bignum)obj).value);
        if (obj instanceof Ratio)
          {
            BigInteger numerator = ((Ratio)obj).numerator();
            BigInteger denominator = ((Ratio)obj).denominator();
            return number(getBigInteger().multiply(denominator),
                          numerator);
          }
        if (obj instanceof SingleFloat)
          return new SingleFloat(value / ((SingleFloat)obj).value);
        if (obj instanceof DoubleFloat)
          return new DoubleFloat(value / ((DoubleFloat)obj).value);
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
    catch (ArithmeticException e)
      {
        if (obj.zerop())
          return error(new DivisionByZero());
        return error(new ArithmeticError(e.getMessage()));
      }
  }

  public boolean isEqualTo(int n)
  {
    return value == n;
  }

  public boolean isEqualTo(LispObject obj) throws ConditionThrowable
  {
    if (obj instanceof Fixnum)
      return value == ((Fixnum)obj).value;
    if (obj instanceof SingleFloat)
      return isEqualTo(((SingleFloat)obj).rational());
    if (obj instanceof DoubleFloat)
      return value == ((DoubleFloat)obj).value;
    if (obj instanceof Complex)
      return obj.isEqualTo(this);
    if (obj.numberp())
      return false;
    type_error(obj, Symbol.NUMBER);
    // Not reached.
    return false;
  }

  public boolean isNotEqualTo(int n)
  {
    return value != n;
  }

  public boolean isNotEqualTo(LispObject obj) throws ConditionThrowable
  {
    if (obj instanceof Fixnum)
      return value != ((Fixnum)obj).value;
    // obj is not a fixnum.
    if (obj instanceof SingleFloat)
      return isNotEqualTo(((SingleFloat)obj).rational());
    if (obj instanceof DoubleFloat)
      return value != ((DoubleFloat)obj).value;
    if (obj instanceof Complex)
      return obj.isNotEqualTo(this);
    if (obj.numberp())
      return true;
    type_error(obj, Symbol.NUMBER);
    // Not reached.
    return false;
  }

  public boolean isLessThan(int n)
  {
    return value < n;
  }

  public boolean isLessThan(LispObject obj) throws ConditionThrowable
  {
    if (obj instanceof Fixnum)
      return value < ((Fixnum)obj).value;
    if (obj instanceof Bignum)
      return getBigInteger().compareTo(Bignum.getValue(obj)) < 0;
    if (obj instanceof Ratio)
      {
        BigInteger n = getBigInteger().multiply(((Ratio)obj).denominator());
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

  public boolean isGreaterThan(int n) throws ConditionThrowable
  {
    return value > n;
  }

  public boolean isGreaterThan(LispObject obj) throws ConditionThrowable
  {
    if (obj instanceof Fixnum)
      return value > ((Fixnum)obj).value;
    if (obj instanceof Bignum)
      return getBigInteger().compareTo(Bignum.getValue(obj)) > 0;
    if (obj instanceof Ratio)
      {
        BigInteger n = getBigInteger().multiply(((Ratio)obj).denominator());
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

  public boolean isLessThanOrEqualTo(int n)
  {
    return value <= n;
  }

  public boolean isLessThanOrEqualTo(LispObject obj) throws ConditionThrowable
  {
    if (obj instanceof Fixnum)
      return value <= ((Fixnum)obj).value;
    if (obj instanceof Bignum)
      return getBigInteger().compareTo(Bignum.getValue(obj)) <= 0;
    if (obj instanceof Ratio)
      {
        BigInteger n = getBigInteger().multiply(((Ratio)obj).denominator());
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

  public boolean isGreaterThanOrEqualTo(int n)
  {
    return value >= n;
  }

  public boolean isGreaterThanOrEqualTo(LispObject obj) throws ConditionThrowable
  {
    if (obj instanceof Fixnum)
      return value >= ((Fixnum)obj).value;
    if (obj instanceof Bignum)
      return getBigInteger().compareTo(Bignum.getValue(obj)) >= 0;
    if (obj instanceof Ratio)
      {
        BigInteger n = getBigInteger().multiply(((Ratio)obj).denominator());
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
    final LispObject value1, value2;
    try
      {
        if (obj instanceof Fixnum)
          {
            int divisor = ((Fixnum)obj).value;
            int quotient = value / divisor;
            int remainder = value % divisor;
            value1 = new Fixnum(quotient);
            value2 = remainder == 0 ? Fixnum.ZERO : new Fixnum(remainder);
          }
        else if (obj instanceof Bignum)
          {
            BigInteger val = getBigInteger();
            BigInteger divisor = ((Bignum)obj).value;
            BigInteger[] results = val.divideAndRemainder(divisor);
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
            // "When rationals and floats are combined by a numerical function,
            // the rational is first converted to a float of the same format."
            // 12.1.4.1
            return new SingleFloat(value).truncate(obj);
          }
        else if (obj instanceof DoubleFloat)
          {
            // "When rationals and floats are combined by a numerical function,
            // the rational is first converted to a float of the same format."
            // 12.1.4.1
            return new DoubleFloat(value).truncate(obj);
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

  public LispObject MOD(LispObject divisor) throws ConditionThrowable
  {
    if (divisor instanceof Fixnum)
      return MOD(((Fixnum)divisor).value);
    return super.MOD(divisor);
  }

  public LispObject MOD(int divisor) throws ConditionThrowable
  {
    final int r;
    try
      {
        r = value % divisor;
      }
    catch (ArithmeticException e)
      {
        return error(new ArithmeticError("Division by zero."));
      }
    if (r == 0)
      return Fixnum.ZERO;
    if (divisor < 0)
      {
        if (value > 0)
          return new Fixnum(r + divisor);
      }
    else
      {
        if (value < 0)
          return new Fixnum(r + divisor);
      }
    return new Fixnum(r);
  }

  public LispObject ash(int shift)
  {
    if (value == 0)
      return this;
    if (shift == 0)
      return this;
    long n = value;
    if (shift <= -32)
      {
        // Right shift.
        return n >= 0 ? Fixnum.ZERO : Fixnum.MINUS_ONE;
      }
    if (shift < 0)
      return new Fixnum((int)(n >> -shift));
    if (shift <= 32)
      {
        n = n << shift;
        if (n >= Integer.MIN_VALUE && n <= Integer.MAX_VALUE)
          return new Fixnum((int)n);
        else
          return new Bignum(n);
      }
    // BigInteger.shiftLeft() succumbs to a stack overflow if shift
    // is Integer.MIN_VALUE, so...
    if (shift == Integer.MIN_VALUE)
      return n >= 0 ? Fixnum.ZERO : Fixnum.MINUS_ONE;
    return number(BigInteger.valueOf(value).shiftLeft(shift));
  }

  public LispObject ash(LispObject obj) throws ConditionThrowable
  {
    if (obj instanceof Fixnum)
      return ash(((Fixnum)obj).value);
    if (obj instanceof Bignum)
      {
        if (value == 0)
          return this;
        BigInteger n = BigInteger.valueOf(value);
        BigInteger shift = ((Bignum)obj).value;
        if (shift.signum() > 0)
          return error(new LispError("Can't represent result of left shift."));
        if (shift.signum() < 0)
          return n.signum() >= 0 ? Fixnum.ZERO : Fixnum.MINUS_ONE;
        Debug.bug(); // Shouldn't happen.
      }
    return type_error(obj, Symbol.INTEGER);
  }

  public LispObject LOGNOT()
  {
    return new Fixnum(~value);
  }

  public LispObject LOGAND(int n) throws ConditionThrowable
  {
    return new Fixnum(value & n);
  }

  public LispObject LOGAND(LispObject obj) throws ConditionThrowable
  {
    if (obj instanceof Fixnum)
      return new Fixnum(value & ((Fixnum)obj).value);
    if (obj instanceof Bignum)
      {
        if (value >= 0)
          {
            int n2 = (((Bignum)obj).value).intValue();
            return new Fixnum(value & n2);
          }
        else
          {
            BigInteger n1 = getBigInteger();
            BigInteger n2 = ((Bignum)obj).value;
            return number(n1.and(n2));
          }
      }
    return type_error(obj, Symbol.INTEGER);
  }

  public LispObject LOGIOR(int n) throws ConditionThrowable
  {
    return new Fixnum(value | n);
  }

  public LispObject LOGIOR(LispObject obj) throws ConditionThrowable
  {
    if (obj instanceof Fixnum)
      return new Fixnum(value | ((Fixnum)obj).value);
    if (obj instanceof Bignum)
      {
        BigInteger n1 = getBigInteger();
        BigInteger n2 = ((Bignum)obj).value;
        return number(n1.or(n2));
      }
    return type_error(obj, Symbol.INTEGER);
  }

  public LispObject LOGXOR(int n) throws ConditionThrowable
  {
    return new Fixnum(value ^ n);
  }

  public LispObject LOGXOR(LispObject obj) throws ConditionThrowable
  {
    if (obj instanceof Fixnum)
      return new Fixnum(value ^ ((Fixnum)obj).value);
    if (obj instanceof Bignum)
      {
        BigInteger n1 = getBigInteger();
        BigInteger n2 = ((Bignum)obj).value;
        return number(n1.xor(n2));
      }
    return type_error(obj, Symbol.INTEGER);
  }

  public LispObject LDB(int size, int position)
  {
    long n = (long) value >> position;
    long mask = (1L << size) - 1;
    return number(n & mask);
  }

  public int hashCode()
  {
    return value;
  }

  public String writeToString() throws ConditionThrowable
  {
    final LispThread thread = LispThread.currentThread();
    int base = Fixnum.getValue(Symbol.PRINT_BASE.symbolValue(thread));
    String s = Integer.toString(value, base).toUpperCase();
    if (Symbol.PRINT_RADIX.symbolValue(thread) != NIL)
      {
        FastStringBuffer sb = new FastStringBuffer();
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
