/*
 * FloatFunctions.java
 *
 * Copyright (C) 2003-2006 Peter Graves
 * $Id: FloatFunctions.java,v 1.10 2007/02/23 21:17:33 piso Exp $
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

public final class FloatFunctions extends Lisp
{
    // ### set-floating-point-modes &key traps => <no values>
    private static final Primitive SET_FLOATING_POINT_MODES =
        new Primitive("set-floating-point-modes", PACKAGE_EXT, true,
                      "&key traps")
    {
        public LispObject execute(LispObject[] args) throws ConditionThrowable
        {
            if (args.length % 2 != 0)
                error(new ProgramError("Odd number of keyword arguments."));
            for (int i = 0; i < args.length; i += 2) {
                LispObject key = checkSymbol(args[i]);
                LispObject value = args[i+1];
                if (key == Keyword.TRAPS) {
                    boolean trap_overflow  = false;
                    boolean trap_underflow = false;
                    while (value != NIL) {
                        LispObject car = value.car();
                        if (car == Keyword.OVERFLOW)
                            trap_overflow = true;
                        else if (car == Keyword.UNDERFLOW)
                            trap_underflow = true;
                        else
                            error(new LispError("Unsupported floating point trap: " +
                                                 car.writeToString()));
                        value = value.cdr();
                    }
                    TRAP_OVERFLOW  = trap_overflow;
                    TRAP_UNDERFLOW = trap_underflow;
                } else
                    error(new LispError("Unrecognized keyword: " + key.writeToString()));
            }
            return LispThread.currentThread().nothing();
        }
    };

    // ### get-floating-point-modes => modes
    private static final Primitive GET_FLOATING_POINT_MODES =
        new Primitive("get-floating-point-modes", PACKAGE_EXT, true, "")
    {
        public LispObject execute() throws ConditionThrowable
        {
            LispObject traps = NIL;
            if (TRAP_UNDERFLOW)
                traps = traps.push(Keyword.UNDERFLOW);
            if (TRAP_OVERFLOW)
                traps = traps.push(Keyword.OVERFLOW);
            return list2(Keyword.TRAPS, traps);
        }
    };

    // ### integer-decode-float float => significand, exponent, integer-sign
    private static final Primitive INTEGER_DECODE_FLOAT =
        new Primitive("integer-decode-float", "float")
    {
//         (defun sane-integer-decode-float (float)
//           (multiple-value-bind (mantissa exp sign)
//               (integer-decode-float float)
//             (let ((fixup (- (integer-length mantissa) (float-precision float))))
//                   (values (ash mantissa (- fixup))
//                           (+ exp fixup)
//                           sign))))

        // See also: http://paste.lisp.org/display/10847

        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            if (arg instanceof SingleFloat) {
                int bits =
                    Float.floatToRawIntBits(((SingleFloat)arg).value);
                int s = ((bits >> 31) == 0) ? 1 : -1;
                int e = (int) ((bits >> 23) & 0xffL);
                int m;
                if (e == 0)
                    m = (bits & 0x7fffff) << 1;
                else
                    m = (bits & 0x7fffff) | 0x800000;
                LispObject significand = number(m);
                Fixnum exponent = new Fixnum(e - 150);
                Fixnum sign = new Fixnum(s);
                return LispThread.currentThread().setValues(significand,
                                                            exponent,
                                                            sign);
            }
            if (arg instanceof DoubleFloat) {
                long bits =
                    Double.doubleToRawLongBits((double)((DoubleFloat)arg).value);
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
                return LispThread.currentThread().setValues(significand,
                                                            exponent,
                                                            sign);
            }
            return type_error(arg, Symbol.FLOAT);
        }
    };

    // ### rational
    private static final Primitive RATIONAL =
        new Primitive("rational", "number")
    {
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            if (arg instanceof SingleFloat)
                return ((SingleFloat)arg).rational();
            if (arg instanceof DoubleFloat)
                return ((DoubleFloat)arg).rational();
            if (arg.rationalp())
                return arg;
            return type_error(arg, Symbol.REAL);
        }
    };

    // ### float-radix
    // float-radix float => float-radix
    private static final Primitive FLOAT_RADIX =
        new Primitive("float-radix", "float")
    {
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            if (arg instanceof SingleFloat || arg instanceof DoubleFloat)
                return Fixnum.TWO;
            return type_error(arg, Symbol.FLOAT);
        }
    };

    private static final Fixnum FIXNUM_24 = new Fixnum(24);
    private static final Fixnum FIXNUM_53 = new Fixnum(53);

    // ### float-digits
    // float-digits float => float-digits
    private static final Primitive FLOAT_DIGITS =
        new Primitive("float-digits", "float")
    {
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            if (arg instanceof SingleFloat)
                return FIXNUM_24;
            if (arg instanceof DoubleFloat)
                return FIXNUM_53;
            return type_error(arg, Symbol.FLOAT);
        }
    };

    // ### scale-float float integer => scaled-float
    private static final Primitive SCALE_FLOAT =
        new Primitive("scale-float", "float integer")
    {
        public LispObject execute(LispObject first, LispObject second)
            throws ConditionThrowable
        {
            if (first instanceof SingleFloat) {
                float f = ((SingleFloat)first).value;
                int n = Fixnum.getValue(second);
                return new SingleFloat(f * (float) Math.pow(2, n));
            }
            if (first instanceof DoubleFloat) {
                double d = ((DoubleFloat)first).value;
                int n = Fixnum.getValue(second);
                return new DoubleFloat(d * Math.pow(2, n));
            }
            return type_error(first, Symbol.FLOAT);
        }
    };

    // ### coerce-to-single-float
    private static final Primitive COERCE_TO_SINGLE_FLOAT =
        new Primitive("coerce-to-single-float", PACKAGE_SYS, false)
    {
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            return SingleFloat.coerceToFloat(arg);
        }
    };

    // ### coerce-to-double-float
    private static final Primitive COERCE_TO_DOUBLE_FLOAT =
        new Primitive("coerce-to-double-float", PACKAGE_SYS, false)
    {
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            return DoubleFloat.coerceToFloat(arg);
        }
    };

    // ### float
    // float number &optional prototype => float
    private static final Primitive FLOAT =
        new Primitive("float", "number &optional prototype")
    {
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            if (arg instanceof SingleFloat || arg instanceof DoubleFloat)
                return arg;
            return SingleFloat.coerceToFloat(arg);
        }
        public LispObject execute(LispObject first, LispObject second)
            throws ConditionThrowable
        {
            if (second instanceof SingleFloat)
                return SingleFloat.coerceToFloat(first);
            if (second instanceof DoubleFloat)
                return DoubleFloat.coerceToFloat(first);
            return type_error(second, Symbol.FLOAT);
        }
    };

    // ### floatp
    // floatp object => generalized-boolean
    private static final Primitive FLOATP = new Primitive("floatp", "object")
    {
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            if (arg instanceof SingleFloat)
                return T;
            if (arg instanceof DoubleFloat)
                return T;
            return NIL;
        }
    };

    // ### single-float-bits
    private static final Primitive SINGLE_FLOAT_BITS =
        new Primitive("single-float-bits", PACKAGE_SYS, true, "float")
    {
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            if (arg instanceof SingleFloat) {
                SingleFloat f = (SingleFloat) arg;
                return new Fixnum(Float.floatToIntBits(f.value));
            }
            return type_error(arg, Symbol.FLOAT);
        }
    };

    // ### double-float-high-bits
    private static final Primitive DOUBLE_FLOAT_HIGH_BITS =
        new Primitive("double-float-high-bits", PACKAGE_SYS, true, "float")
    {
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            if (arg instanceof DoubleFloat) {
                DoubleFloat f = (DoubleFloat) arg;
                return number(Double.doubleToLongBits(f.value) >>> 32);
            }
            return type_error(arg, Symbol.DOUBLE_FLOAT);
        }
    };

    // ### double-float-low-bits
    private static final Primitive DOUBLE_FLOAT_LOW_BITS =
        new Primitive("double-float-low-bits", PACKAGE_SYS, true, "float")
    {
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            if (arg instanceof DoubleFloat) {
                DoubleFloat f = (DoubleFloat) arg;
                return number(Double.doubleToLongBits(f.value) & 0xffffffffL);
            }
            return type_error(arg, Symbol.DOUBLE_FLOAT);
        }
    };

    // ### make-single-float bits => float
    private static final Primitive MAKE_SINGLE_FLOAT =
        new Primitive("make-single-float", PACKAGE_SYS, true, "bits")
    {
        public LispObject execute(LispObject arg)
            throws ConditionThrowable
        {
            if (arg instanceof Fixnum) {
                int bits = ((Fixnum)arg).value;
                return new SingleFloat(Float.intBitsToFloat(bits));
            }
            if (arg instanceof Bignum) {
                long bits = ((Bignum)arg).value.longValue();
                return new SingleFloat(Float.intBitsToFloat((int)bits));
            }
            return type_error(arg, Symbol.INTEGER);
        }
    };

    // ### make-double-float bits => float
    private static final Primitive MAKE_DOUBLE_FLOAT =
        new Primitive("make-double-float", PACKAGE_SYS, true, "bits")
    {
        public LispObject execute(LispObject arg)
            throws ConditionThrowable
        {
            if (arg instanceof Fixnum) {
                long bits = (long) ((Fixnum)arg).value;
                return new DoubleFloat(Double.longBitsToDouble(bits));
            }
            if (arg instanceof Bignum) {
                long bits = ((Bignum)arg).value.longValue();
                return new DoubleFloat(Double.longBitsToDouble(bits));
            }
            return type_error(arg, Symbol.INTEGER);
        }
    };

    // ### float-infinity-p
    private static final Primitive FLOAT_INFINITY_P =
        new Primitive("float-infinity-p", PACKAGE_SYS, true)
    {
        public LispObject execute(LispObject arg)
            throws ConditionThrowable
        {
            if (arg instanceof SingleFloat)
                return Float.isInfinite(((SingleFloat)arg).value) ? T : NIL;
            if (arg instanceof DoubleFloat)
                return Double.isInfinite(((DoubleFloat)arg).value) ? T : NIL;
            return type_error(arg, Symbol.FLOAT);
        }
    };

    // ### float-nan-p
    private static final Primitive FLOAT_NAN_P =
        new Primitive("float-nan-p", PACKAGE_SYS, true)
    {
        public LispObject execute(LispObject arg)
            throws ConditionThrowable
        {
            if (arg instanceof SingleFloat)
                return Float.isNaN(((SingleFloat)arg).value) ? T : NIL;
            if (arg instanceof DoubleFloat)
                return Double.isNaN(((DoubleFloat)arg).value) ? T : NIL;
            return type_error(arg, Symbol.FLOAT);
        }
    };

    // ### float-string
    private static final Primitive FLOAT_STRING =
        new Primitive("float-string", PACKAGE_SYS, true)
    {
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            final String s1;
            if (arg instanceof SingleFloat)
                s1 = String.valueOf(((SingleFloat)arg).value);
            else if (arg instanceof DoubleFloat)
                s1 = String.valueOf(((DoubleFloat)arg).value);
            else
                return type_error(arg, Symbol.FLOAT);
            int i = s1.indexOf('E');
            if (i < 0)
                return new SimpleString(s1);
            String s2 = s1.substring(0, i);
            int exponent = Integer.parseInt(s1.substring(i + 1));
            if (exponent == 0)
                return new SimpleString(s2);
            int index = s2.indexOf('.');
            if (index < 0)
                return new SimpleString(s2);
            StringBuffer sb = new StringBuffer(s2);
            if (index >= 0)
                sb.deleteCharAt(index);
            // Now we've got just the digits in the StringBuffer.
            if (exponent > 0) {
                int newIndex = index + exponent;
                if (newIndex < sb.length())
                    sb.insert(newIndex, '.');
                else if (newIndex == sb.length())
                    sb.append('.');
                else {
                    // We need to add some zeros.
                    while (newIndex > sb.length())
                        sb.append('0');
                    sb.append('.');
                }
            } else {
                Debug.assertTrue(exponent < 0);
                int newIndex = index + exponent;
                while (newIndex < 0) {
                    sb.insert(0, '0');
                    ++newIndex;
                }
                sb.insert(0, '.');
            }
            return new SimpleString(sb.toString());
        }
    };
}
