/*
 * Nil.java
 *
 * Copyright (C) 2002-2006 Peter Graves
 * $Id: Nil.java,v 1.45 2007/02/23 21:17:34 piso Exp $
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

public final class Nil extends Symbol
{
    public Nil(Package pkg)
    {
        super("NIL", pkg);
        pkg.addSymbol(this);
        initializeConstant(this);
    }

    public LispObject typeOf()
    {
        return Symbol.NULL;
    }

    public LispObject classOf()
    {
        return BuiltInClass.NULL;
    }

    public LispObject getDescription()
    {
        return new SimpleString("The symbol NIL");
    }

    public boolean getBooleanValue()
    {
        return false;
    }

    public LispObject typep(LispObject typeSpecifier) throws ConditionThrowable
    {
        if (typeSpecifier == Symbol.NULL)
            return T;
        if (typeSpecifier == Symbol.LIST)
            return T;
        if (typeSpecifier == Symbol.SEQUENCE)
            return T;
        if (typeSpecifier == Symbol.SYMBOL)
            return T;
        if (typeSpecifier == Symbol.BOOLEAN)
            return T;
        if (typeSpecifier == BuiltInClass.NULL)
            return T;
        if (typeSpecifier == BuiltInClass.LIST)
            return T;
        if (typeSpecifier == BuiltInClass.SEQUENCE)
            return T;
        if (typeSpecifier == BuiltInClass.SYMBOL)
            return T;
        return super.typep(typeSpecifier);
    }

    public boolean constantp()
    {
        return true;
    }

    public final LispObject getSymbolValue()
    {
        return this;
    }

    public LispObject car()
    {
        return this;
    }

    public LispObject cdr()
    {
        return this;
    }

    public final LispObject cadr()
    {
        return this;
    }

    public final LispObject cddr()
    {
        return this;
    }

    public final LispObject caddr()
    {
        return this;
    }

    public LispObject nthcdr(int n) throws ConditionThrowable
    {
        if (n < 0)
            return type_error(new Fixnum(n),
                                   list2(Symbol.INTEGER, Fixnum.ZERO));
        return this;
    }

    public int length()
    {
        return 0;
    }

    public LispObject push(LispObject obj)
    {
        return new Cons(obj);
    }

    public LispObject NTH(int index) throws ConditionThrowable
    {
        if (index < 0)
            error(new TypeError(String.valueOf(index) +
                                 " is not of type UNSIGNED-BYTE."));
        return NIL;
    }

    public LispObject NTH(LispObject arg) throws ConditionThrowable
    {
        int index;
        try {
            index = ((Fixnum)arg).value;
        }
        catch (ClassCastException e) {
            if (arg instanceof Bignum) {
                if (arg.minusp())
                    return error(new TypeError(arg, Symbol.UNSIGNED_BYTE));
                return NIL;
            }
            return error(new TypeError(arg, Symbol.UNSIGNED_BYTE));
        }
        if (index < 0)
            error(new TypeError(arg, Symbol.UNSIGNED_BYTE));
        return NIL;
    }

    public LispObject elt(int index) throws ConditionThrowable
    {
        return error(new TypeError("ELT: invalid index " + index + " for " + this + "."));
    }

    public LispObject reverse()
    {
        return this;
    }

    public LispObject nreverse()
    {
        return this;
    }

    public LispObject[] copyToArray()
    {
        return new LispObject[0];
    }

    public boolean listp()
    {
        return true;
    }

    public LispObject LISTP()
    {
        return T;
    }

    public boolean endp()
    {
        return true;
    }

    public LispObject ENDP()
    {
        return T;
    }

    public LispObject NOT()
    {
        return T;
    }

    public final LispObject getSymbolFunction()
    {
        return null;
    }

    public String toString()
    {
        if (Symbol.PRINT_READABLY.symbolValueNoThrow() != NIL)
            return "|COMMON-LISP|::|NIL|";
        return "NIL";
    }
}
