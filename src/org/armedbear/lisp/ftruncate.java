/*
 * ftruncate.java
 *
 * Copyright (C) 2004-2005 Peter Graves
 * $Id: ftruncate.java,v 1.7 2005/08/23 20:47:48 piso Exp $
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

// ### ftruncate number &optional divisor => quotient, remainder
// (defun ftruncate (number &optional (divisor 1))
//  (multiple-value-bind (tru rem) (truncate number divisor)
//   (values (float tru) rem)))

// "FFLOOR, FCEILING, FTRUNCATE, and FROUND handle arguments of different types
// in the following way: If number is a float, and divisor is not a float of
// longer format, then the first result is a float of the same type as number.
// Otherwise, the first result is of the type determined by contagion rules."
public final class ftruncate extends Primitive
{
    private ftruncate()
    {
        super("ftruncate", "number &optional divisor");
    }

    public LispObject execute(LispObject arg) throws ConditionThrowable
    {
        final LispThread thread = LispThread.currentThread();
        if (arg.zerop()) {
            LispObject q = arg;
            LispObject r;
            if (arg instanceof DoubleFloat)
                r = DoubleFloat.ZERO;
            else
                r = SingleFloat.ZERO;
            return thread.setValues(q, r);
        }
        if (arg instanceof DoubleFloat) {
            double d = ((DoubleFloat)arg).value;
            if (Double.isInfinite(d) || Double.isNaN(d))
                return thread.setValues(arg, new DoubleFloat(Double.NaN));
        } else if (arg instanceof SingleFloat) {
            float f = ((SingleFloat)arg).value;
            if (Float.isInfinite(f) || Float.isNaN(f))
                return thread.setValues(arg, new SingleFloat(Float.NaN));
        }
        LispObject q = arg.truncate(Fixnum.ONE); // an integer
        if (arg instanceof DoubleFloat) {
            if (q.zerop()) {
                if (arg.minusp())
                    q = new DoubleFloat(-0.0);
                else
                    q = new DoubleFloat(0.0);
            } else if (q instanceof Fixnum)
                q = new DoubleFloat(((Fixnum)q).value);
            else
                q = new DoubleFloat(((Bignum)q).doubleValue());
        } else {
            if (q.zerop()) {
                if (arg.minusp())
                    q = new SingleFloat(-0.0f);
                else
                    q = new SingleFloat(0.0f);
            } else if (q instanceof Fixnum)
                q = new SingleFloat(((Fixnum)q).value);
            else
                q = new SingleFloat(((Bignum)q).floatValue());
        }
        thread._values[0] = q;
        return q;
    }

    public LispObject execute(LispObject first, LispObject second)
        throws ConditionThrowable
    {
        final LispThread thread = LispThread.currentThread();
        if (first.zerop()) {
            LispObject q = first;
            LispObject r;
            if (first instanceof DoubleFloat)
                r = DoubleFloat.ZERO;
            else
                r = SingleFloat.ZERO;
            return thread.setValues(q, r);
        }
        if (first instanceof DoubleFloat) {
            double d1 = ((DoubleFloat)first).value;
            if (Double.isInfinite(d1) || Double.isNaN(d1))
                return thread.setValues(first, new DoubleFloat(Double.NaN));
        } else if (first instanceof SingleFloat) {
            float f1 = ((SingleFloat)first).value;
            if (Float.isInfinite(f1) || Float.isNaN(f1))
                return thread.setValues(first, new SingleFloat(Float.NaN));
        }
        LispObject q = first.truncate(second); // an integer
        if (first instanceof DoubleFloat || second instanceof DoubleFloat) {
            if (q.zerop()) {
                if (first.minusp()) {
                    if (second.minusp())
                        q = new DoubleFloat(0.0);
                    else
                        q = new DoubleFloat(-0.0);
                } else if (second.minusp())
                    q = new DoubleFloat(-0.0);
                else
                    q = new DoubleFloat(0.0);
            } else if (q instanceof Fixnum)
                q = new DoubleFloat(((Fixnum)q).value);
            else
                q = new DoubleFloat(((Bignum)q).doubleValue());
        } else {
            if (q.zerop()) {
                if (first.minusp()) {
                    if (second.minusp())
                        q = new SingleFloat(0.0f);
                    else
                        q = new SingleFloat(-0.0f);
                } else if (second.minusp())
                    q = new SingleFloat(-0.0f);
                else
                    q = new SingleFloat(0.0f);
            } else if (q instanceof Fixnum)
                q = new SingleFloat(((Fixnum)q).value);
            else
                q = new SingleFloat(((Bignum)q).floatValue());
        }
        thread._values[0] = q;
        return q;
    }

    private static final Primitive FTRUNCATE = new ftruncate();
}
