/*
 * PrintNotReadable.java
 *
 * Copyright (C) 2004-2005 Peter Graves
 * $Id: PrintNotReadable.java,v 1.13 2007/02/23 21:17:34 piso Exp $
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

public class PrintNotReadable extends LispError
{
    public PrintNotReadable(LispObject initArgs) throws ConditionThrowable
    {
        super(StandardClass.PRINT_NOT_READABLE);
        super.initialize(initArgs);
        LispObject object = null;
        while (initArgs != NIL) {
            LispObject first = initArgs.car();
            initArgs = initArgs.cdr();
            LispObject second = initArgs.car();
            initArgs = initArgs.cdr();
            if (first == Keyword.OBJECT) {
                object = second;
                break;
            }
        }
        if (object != null)
            setInstanceSlotValue(Symbol.OBJECT, object);
    }

    public LispObject typeOf()
    {
        return Symbol.PRINT_NOT_READABLE;
    }

    public LispObject classOf()
    {
        return StandardClass.PRINT_NOT_READABLE;
    }

    public LispObject typep(LispObject type) throws ConditionThrowable
    {
        if (type == Symbol.PRINT_NOT_READABLE)
            return T;
        if (type == StandardClass.PRINT_NOT_READABLE)
            return T;
        return super.typep(type);
    }

    public String getMessage()
    {
        FastStringBuffer sb = new FastStringBuffer();
        LispObject object = UNBOUND_VALUE;
        try {
            object = getInstanceSlotValue(Symbol.OBJECT);
        }
        catch (Throwable t) {
            Debug.trace(t);
        }
        if (object != UNBOUND_VALUE) {
            final LispThread thread = LispThread.currentThread();
            final SpecialBinding lastSpecialBinding = thread.lastSpecialBinding;
            thread.bindSpecial(Symbol.PRINT_READABLY, NIL);
            thread.bindSpecial(Symbol.PRINT_ARRAY, NIL);
            try {
                sb.append(object.writeToString());
            }
            catch (Throwable t) {
                sb.append("Object");
            }
            finally {
                thread.lastSpecialBinding = lastSpecialBinding;
            }
        } else
            sb.append("Object");
        sb.append(" cannot be printed readably.");
        return sb.toString();
    }

    // ### print-not-readable-object
    private static final Primitive PRINT_NOT_READABLE_OBJECT =
        new Primitive("print-not-readable-object", "condition")
    {
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            try {
                return ((PrintNotReadable)arg).getInstanceSlotValue(Symbol.OBJECT);
            }
            catch (ClassCastException e) {
                return type_error(arg, Symbol.PRINT_NOT_READABLE);
            }
        }
    };
}
