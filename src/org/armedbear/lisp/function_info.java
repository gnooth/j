/*
 * function_info.java
 *
 * Copyright (C) 2004-2005 Peter Graves
 * $Id: function_info.java,v 1.7 2007/02/23 21:17:35 piso Exp $
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

public final class function_info extends Lisp
{
    private static EqualHashTable FUNCTION_TABLE =
        new EqualHashTable(64, NIL, NIL);

    // ### function-info name
    private static final Primitive FUNCTION_INFO =
        new Primitive("function-info", PACKAGE_SYS, false)
    {
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            LispObject info = FUNCTION_TABLE.get(arg);
            return info != null ? info : NIL;
        }
    };

    // ### %set-function-info name info
    private static final Primitive _SET_FUNCTION_INFO =
        new Primitive("%set-function-info", PACKAGE_SYS, false)
    {
        public LispObject execute(LispObject name, LispObject info)
            throws ConditionThrowable
        {
            if (info == NIL)
                FUNCTION_TABLE.remhash(name);
            else
                FUNCTION_TABLE.put(name, info);
            return info;
        }
    };

    // ### get-function-info-value name indicator => value
    private static final Primitive GET_FUNCTION_INFO_VALUE =
        new Primitive("get-function-info-value", PACKAGE_SYS, true,
                      "name indicator")
    {
        public LispObject execute(LispObject name, LispObject indicator)
            throws ConditionThrowable
        {
            // info is an alist
            LispObject info = FUNCTION_TABLE.get(name);
            if (info != null) {
                while (info != NIL) {
                    LispObject cons = info.car();
                    if (cons instanceof Cons) {
                        if (cons.car().eql(indicator)) {
                            // Found it.
                            return LispThread.currentThread().setValues(cons.cdr(), T);
                        }
                    } else if (cons != NIL)
                        error(new TypeError(cons, Symbol.LIST));
                    info = info.cdr();
                }
            }
            return LispThread.currentThread().setValues(NIL, NIL);
        }
    };

    // ### set-function-info-value name indicator value => value
    private static final Primitive SET_FUNCTION_INFO_VALUE =
        new Primitive("set-function-info-value", PACKAGE_SYS, true,
                      "name indicator value")
    {
        public LispObject execute(LispObject name, LispObject indicator,
                                  LispObject value)
            throws ConditionThrowable
        {
            // info is an alist
            LispObject info = FUNCTION_TABLE.get(name);
            if (info == null)
                info = NIL;
            LispObject alist = info;
            while (alist != NIL) {
                LispObject cons = alist.car();
                if (cons instanceof Cons) {
                    if (cons.car().eql(indicator)) {
                        // Found it.
                        cons.setCdr(value);
                        return value;
                    }
                } else if (cons != NIL)
                    error(new TypeError(cons, Symbol.LIST));
                alist = alist.cdr();
            }
            // Not found.
            FUNCTION_TABLE.put(name, info.push(new Cons(indicator, value)));
            return value;
        }
    };
}
