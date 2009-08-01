/*
 * SingleFloat.java
 *
 * Copyright (C) 2003-2007 Peter Graves
 * $Id: SingleFloat.java,v 1.8 2007/09/17 16:58:39 piso Exp $
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

import java.math.BigInteger;

public final class SingleFloat extends LispObject
{
    public static final SingleFloat ZERO       = new SingleFloat(0);
    public static final SingleFloat MINUS_ZERO = new SingleFloat(-0.0f);
    public static final SingleFloat ONE        = new SingleFloat(1);
    public static final SingleFloat MINUS_ONE  = new SingleFloat(-1);

    public static final SingleFloat SINGLE_FLOAT_POSITIVE_INFINITY =
        new SingleFloat(Float.POSITIVE_INFINITY);

    public static final SingleFloat SINGLE_FLOAT_NEGATIVE_INFINITY =
        new SingleFloat(Float.NEGATIVE_INFINITY);

    static {
        Symbol.SINGLE_FLOAT_POSITIVE_INFINITY.initializeConstant(SINGLE_FLOAT_POSITIVE_INFINITY);
        Symbol.SINGLE_FLOAT_NEGATIVE_INFINITY.initializeConstant(SINGLE_FLOAT_NEGATIVE_INFINITY);
    }

    public final float value;

    public SingleFloat(float value)
    {
        this.value = value;
    }

    public LispObject typeOf()
    {
        return Symbol.SINGLE_FLOAT;
    }

    public LispObject classOf()
    {
        return BuiltInClass.SINGLE_FLOAT;
    }

    public LispObject typep(LispObject typeSpecifier) throws ConditionThrowable
    {
        if (typeSpecifier == Symbol.FLOAT)
            return T;
        if (typeSpecifier == Symbol.REAL)
            return T;
        if (typeSpecifier == Symbol.NUMBER)
            return T;
        if (typeSpecifier == Symbol.SINGLE_FLOAT)
            return T;
        if (typeSpecifier == Symbol.SHORT_FLOAT)
            return T;
        if (typeSpecifier == BuiltInClass.FLOAT)
            return T;
        if (typeSpecifier == BuiltInClass.SINGLE_FLOAT)
            return T;
        return super.typep(typeSpecifier);
    }

    public LispObject NUMBERP()
    {
        return T;
    }

    public boolean numberp()
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
        if (obj instanceof SingleFloat) {
            if (value == 0) {
                // "If an implementation supports positive and negative zeros
                // as distinct values, then (EQL 0.0 -0.0) returns false."
                float f = ((SingleFloat)obj).value;
                int bits = Float.floatToRawIntBits(f);
                return bits == Float.floatToRawIntBits(value);
            }
            if (value == ((SingleFloat)obj).value)
                return true;
        }
        return false;
    }

    public boolean equal(LispObject obj)
    {
        if (this == obj)
            return true;
        if (obj instanceof SingleFloat) {
            if (value == 0) {
                // same as EQL
                float f = ((SingleFloat)obj).value;
                int bits = Float.floatToRawIntBits(f);
                return bits == Float.floatToRawIntBits(value);
            }
            if (value == ((SingleFloat)obj).value)
                return true;
        }
        return false;
    }

    public boolean equalp(int n)
    {
        // "If two numbers are the same under =."
        return value == n;
    }

    public boolean equalp(LispObject obj) throws ConditionThrowable
    {
        if (obj instanceof SingleFloat)
            return value == ((SingleFloat)obj).value;
        if (obj instanceof DoubleFloat)
            return value == ((DoubleFloat)obj).value;
        if (obj instanceof Fixnum)
            return value == ((Fixnum)obj).value;
        if (obj instanceof Bignum)
            return value == ((Bignum)obj).floatValue();
        if (obj instanceof Ratio)
            return value == ((Ratio)obj).floatValue();
        return false;
    }

    public LispObject ABS()
    {
        if (value > 0)
            return this;
        if (value == 0) // 0.0 or -0.0
            return ZERO;
        return new SingleFloat(- value);
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

    public LispObject FLOATP()
    {
        return T;
    }

    public boolean floatp()
    {
        return true;
    }

    public static double getValue(LispObject obj) throws ConditionThrowable
    {
        try {
            return ((SingleFloat)obj).value;
        }
        catch (ClassCastException e) {
            error(new TypeError(obj, Symbol.FLOAT));
            // Not reached.
            return 0;
        }
    }

    public final float getValue()
    {
        return value;
    }

    public Object javaInstance()
    {
        return new Float(value);
    }

    public Object javaInstance(Class c)
    {
        String cn = c.getName();
        if (cn.equals("java.lang.Float") || cn.equals("float"))
            return new Float(value);
        return javaInstance();
    }

    public final LispObject incr()
    {
        return new SingleFloat(value + 1);
    }

    public final LispObject decr()
    {
        return new SingleFloat(value - 1);
    }

    public LispObject add(LispObject obj) throws ConditionThrowable
    {
        if (obj instanceof Fixnum)
            return new SingleFloat(value + ((Fixnum)obj).value);
        if (obj instanceof SingleFloat)
            return new SingleFloat(value + ((SingleFloat)obj).value);
        if (obj instanceof DoubleFloat)
            return new DoubleFloat(value + ((DoubleFloat)obj).value);
        if (obj instanceof Bignum)
            return new SingleFloat(value + ((Bignum)obj).floatValue());
        if (obj instanceof Ratio)
            return new SingleFloat(value + ((Ratio)obj).floatValue());
        if (obj instanceof Complex) {
            Complex c = (Complex) obj;
            return Complex.getInstance(add(c.getRealPart()), c.getImaginaryPart());
        }
        return error(new TypeError(obj, Symbol.NUMBER));
    }

    public LispObject negate()
    {
        if (value == 0) {
            int bits = Float.floatToRawIntBits(value);
            return (bits < 0) ? ZERO : MINUS_ZERO;
        }
        return new SingleFloat(-value);
    }

    public LispObject subtract(LispObject obj) throws ConditionThrowable
    {
        if (obj instanceof Fixnum)
            return new SingleFloat(value - ((Fixnum)obj).value);
        if (obj instanceof SingleFloat)
            return new SingleFloat(value - ((SingleFloat)obj).value);
        if (obj instanceof DoubleFloat)
            return new DoubleFloat(value - ((DoubleFloat)obj).value);
        if (obj instanceof Bignum)
            return new SingleFloat(value - ((Bignum)obj).floatValue());
        if (obj instanceof Ratio)
            return new SingleFloat(value - ((Ratio)obj).floatValue());
        if (obj instanceof Complex) {
            Complex c = (Complex) obj;
            return Complex.getInstance(subtract(c.getRealPart()),
                                       ZERO.subtract(c.getImaginaryPart()));
        }
        return error(new TypeError(obj, Symbol.NUMBER));
    }

    public LispObject multiplyBy(LispObject obj) throws ConditionThrowable
    {
        if (obj instanceof Fixnum)
            return new SingleFloat(value * ((Fixnum)obj).value);
        if (obj instanceof SingleFloat)
            return new SingleFloat(value * ((SingleFloat)obj).value);
        if (obj instanceof DoubleFloat)
            return new DoubleFloat(value * ((DoubleFloat)obj).value);
        if (obj instanceof Bignum)
            return new SingleFloat(value * ((Bignum)obj).floatValue());
        if (obj instanceof Ratio)
            return new SingleFloat(value * ((Ratio)obj).floatValue());
        if (obj instanceof Complex) {
            Complex c = (Complex) obj;
            return Complex.getInstance(multiplyBy(c.getRealPart()),
                                       multiplyBy(c.getImaginaryPart()));
        }
        return error(new TypeError(obj, Symbol.NUMBER));
    }

    public LispObject divideBy(LispObject obj) throws ConditionThrowable
    {
        if (obj instanceof Fixnum)
            return new SingleFloat(value / ((Fixnum)obj).value);
        if (obj instanceof SingleFloat)
            return new SingleFloat(value / ((SingleFloat)obj).value);
        if (obj instanceof DoubleFloat)
            return new DoubleFloat(value / ((DoubleFloat)obj).value);
        if (obj instanceof Bignum)
            return new SingleFloat(value / ((Bignum)obj).floatValue());
        if (obj instanceof Ratio)
            return new SingleFloat(value / ((Ratio)obj).floatValue());
        if (obj instanceof Complex) {
            Complex c = (Complex) obj;
            LispObject re = c.getRealPart();
            LispObject im = c.getImaginaryPart();
            LispObject denom = re.multiplyBy(re).add(im.multiplyBy(im));
            LispObject resX = multiplyBy(re).divideBy(denom);
            LispObject resY =
                multiplyBy(Fixnum.MINUS_ONE).multiplyBy(im).divideBy(denom);
            return Complex.getInstance(resX, resY);
        }
        return error(new TypeError(obj, Symbol.NUMBER));
    }

    public boolean isEqualTo(LispObject obj) throws ConditionThrowable
    {
        if (obj instanceof Fixnum)
            return rational().isEqualTo(obj);
        if (obj instanceof SingleFloat)
            return value == ((SingleFloat)obj).value;
        if (obj instanceof DoubleFloat)
            return value == ((DoubleFloat)obj).value;
        if (obj instanceof Bignum)
            return rational().isEqualTo(obj);
        if (obj instanceof Ratio)
            return rational().isEqualTo(obj);
        if (obj instanceof Complex)
            return obj.isEqualTo(this);
        error(new TypeError(obj, Symbol.NUMBER));
        // Not reached.
        return false;
    }

    public boolean isNotEqualTo(LispObject obj) throws ConditionThrowable
    {
        return !isEqualTo(obj);
    }

    public boolean isLessThan(LispObject obj) throws ConditionThrowable
    {
        if (obj instanceof Fixnum)
            return rational().isLessThan(obj);
        if (obj instanceof SingleFloat)
            return value < ((SingleFloat)obj).value;
        if (obj instanceof DoubleFloat)
            return value < ((DoubleFloat)obj).value;
        if (obj instanceof Bignum)
            return rational().isLessThan(obj);
        if (obj instanceof Ratio)
            return rational().isLessThan(obj);
        error(new TypeError(obj, Symbol.REAL));
        // Not reached.
        return false;
    }

    public boolean isGreaterThan(LispObject obj) throws ConditionThrowable
    {
        if (obj instanceof Fixnum)
            return rational().isGreaterThan(obj);
        if (obj instanceof SingleFloat)
            return value > ((SingleFloat)obj).value;
        if (obj instanceof DoubleFloat)
            return value > ((DoubleFloat)obj).value;
        if (obj instanceof Bignum)
            return rational().isGreaterThan(obj);
        if (obj instanceof Ratio)
            return rational().isGreaterThan(obj);
        error(new TypeError(obj, Symbol.REAL));
        // Not reached.
        return false;
    }

    public boolean isLessThanOrEqualTo(LispObject obj) throws ConditionThrowable
    {
        if (obj instanceof Fixnum)
            return rational().isLessThanOrEqualTo(obj);
        if (obj instanceof SingleFloat)
            return value <= ((SingleFloat)obj).value;
        if (obj instanceof DoubleFloat)
            return value <= ((DoubleFloat)obj).value;
        if (obj instanceof Bignum)
            return rational().isLessThanOrEqualTo(obj);
        if (obj instanceof Ratio)
            return rational().isLessThanOrEqualTo(obj);
        error(new TypeError(obj, Symbol.REAL));
        // Not reached.
        return false;
    }

    public boolean isGreaterThanOrEqualTo(LispObject obj) throws ConditionThrowable
    {
        if (obj instanceof Fixnum)
            return rational().isGreaterThanOrEqualTo(obj);
        if (obj instanceof SingleFloat)
            return value >= ((SingleFloat)obj).value;
        if (obj instanceof DoubleFloat)
            return value >= ((DoubleFloat)obj).value;
        if (obj instanceof Bignum)
            return rational().isGreaterThanOrEqualTo(obj);
        if (obj instanceof Ratio)
            return rational().isGreaterThanOrEqualTo(obj);
        error(new TypeError(obj, Symbol.REAL));
        // Not reached.
        return false;
    }

    public LispObject truncate(LispObject obj) throws ConditionThrowable
    {
        // "When rationals and floats are combined by a numerical function,
        // the rational is first converted to a float of the same format."
        // 12.1.4.1
        if (obj instanceof Fixnum) {
            return truncate(new SingleFloat(((Fixnum)obj).value));
        }
        if (obj instanceof Bignum) {
            return truncate(new SingleFloat(((Bignum)obj).floatValue()));
        }
        if (obj instanceof Ratio) {
            return truncate(new SingleFloat(((Ratio)obj).floatValue()));
        }
        if (obj instanceof SingleFloat) {
            final LispThread thread = LispThread.currentThread();
            float divisor = ((SingleFloat)obj).value;
            float quotient = value / divisor;
            if (quotient >= Integer.MIN_VALUE && quotient <= Integer.MAX_VALUE) {
                int q = (int) quotient;
                return thread.setValues(new Fixnum(q),
                                        new SingleFloat(value - q * divisor));
            }
            // We need to convert the quotient to a bignum.
            int bits = Float.floatToRawIntBits(quotient);
            int s = ((bits >> 31) == 0) ? 1 : -1;
            int e = (int) ((bits >> 23) & 0xff);
            long m;
            if (e == 0)
                m = (bits & 0x7fffff) << 1;
            else
                m = (bits & 0x7fffff) | 0x800000;
            LispObject significand = number(m);
            Fixnum exponent = new Fixnum(e - 150);
            Fixnum sign = new Fixnum(s);
            LispObject result = significand;
            result =
                result.multiplyBy(MathFunctions.EXPT.execute(Fixnum.TWO, exponent));
            result = result.multiplyBy(sign);
            // Calculate remainder.
            LispObject product = result.multiplyBy(obj);
            LispObject remainder = subtract(product);
            return thread.setValues(result, remainder);
        }
        if (obj instanceof DoubleFloat) {
            final LispThread thread = LispThread.currentThread();
            double divisor = ((DoubleFloat)obj).value;
            double quotient = value / divisor;
            if (quotient >= Integer.MIN_VALUE && quotient <= Integer.MAX_VALUE) {
                int q = (int) quotient;
                return thread.setValues(new Fixnum(q),
                                        new DoubleFloat(value - q * divisor));
            }
            // We need to convert the quotient to a bignum.
            long bits = Double.doubleToRawLongBits((double)quotient);
            int s = ((bits >> 63) == 0) ? 1 : -1;
            int e = (int) ((bits >> 52) & 0x7ffL);
            long m;
            if (e == 0)
                m = (bits & 0xfffffffffffffL) << 1;
            else
                m = (bits & 0xfffffffffffffL) | 0x10000000000000L;
            LispObject significand = number(m);
            Fixnum exponent = new Fixnum(e - 1075);
            Fixnum sign = new Fixnum(s);
            LispObject result = significand;
            result =
                result.multiplyBy(MathFunctions.EXPT.execute(Fixnum.TWO, exponent));
            result = result.multiplyBy(sign);
            // Calculate remainder.
            LispObject product = result.multiplyBy(obj);
            LispObject remainder = subtract(product);
            return thread.setValues(result, remainder);
        }
        return error(new TypeError(obj, Symbol.REAL));
    }

    public int hashCode()
    {
        return Float.floatToIntBits(value);
    }

    public int psxhash()
    {
        if ((value % 1) == 0)
            return (((int)value) & 0x7fffffff);
        else
            return (hashCode() & 0x7fffffff);
    }

    public String writeToString() throws ConditionThrowable
    {
        if (value == Float.POSITIVE_INFINITY) {
            StringBuffer sb = new StringBuffer("#.");
            sb.append(Symbol.SINGLE_FLOAT_POSITIVE_INFINITY.writeToString());
            return sb.toString();
        }
        if (value == Float.NEGATIVE_INFINITY) {
            StringBuffer sb = new StringBuffer("#.");
            sb.append(Symbol.SINGLE_FLOAT_NEGATIVE_INFINITY.writeToString());
            return sb.toString();
        }
        if (value != value)
            return "#<SINGLE-FLOAT NaN>";
        String s1 = String.valueOf(value);
        LispThread thread = LispThread.currentThread();
        if (Symbol.PRINT_READABLY.symbolValue(thread) != NIL ||
            !memq(Symbol.READ_DEFAULT_FLOAT_FORMAT.symbolValue(thread),
                  list2(Symbol.SINGLE_FLOAT, Symbol.SHORT_FLOAT)))
        {
            if (s1.indexOf('E') >= 0)
                return s1.replace('E', 'f');
            else
                return s1.concat("f0");
        } else
            return s1;
    }

    public LispObject rational() throws ConditionThrowable
    {
        final int bits = Float.floatToRawIntBits(value);
        int sign = ((bits >> 31) == 0) ? 1 : -1;
        int storedExponent = ((bits >> 23) & 0xff);
        long mantissa;
        if (storedExponent == 0)
            mantissa = (bits & 0x7fffff) << 1;
        else
            mantissa = (bits & 0x7fffff) | 0x800000;
        if (mantissa == 0)
            return Fixnum.ZERO;
        if (sign < 0)
            mantissa = -mantissa;
        // Subtract bias.
        final int exponent = storedExponent - 127;
        BigInteger numerator, denominator;
        if (exponent < 0) {
            numerator = BigInteger.valueOf(mantissa);
            denominator = BigInteger.valueOf(1).shiftLeft(23 - exponent);
        } else {
            numerator = BigInteger.valueOf(mantissa).shiftLeft(exponent);
            denominator = BigInteger.valueOf(0x800000); // (ash 1 23)
        }
        return number(numerator, denominator);
    }

    public static SingleFloat coerceToFloat(LispObject obj) throws ConditionThrowable
    {
        if (obj instanceof Fixnum)
            return new SingleFloat(((Fixnum)obj).value);
        if (obj instanceof SingleFloat)
            return (SingleFloat) obj;
        if (obj instanceof DoubleFloat)
            return new SingleFloat((float)((DoubleFloat)obj).value);
        if (obj instanceof Bignum)
            return new SingleFloat(((Bignum)obj).floatValue());
        if (obj instanceof Ratio)
            return new SingleFloat(((Ratio)obj).floatValue());
        error(new TypeError("The value " + obj.writeToString() +
                             " cannot be converted to type SINGLE-FLOAT."));
        // Not reached.
        return null;
    }
}
