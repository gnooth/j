/*
 * StructureClass.java
 *
 * Copyright (C) 2003-2005 Peter Graves
 * $Id: StructureClass.java,v 1.16 2007/02/23 21:17:35 piso Exp $
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

public class StructureClass extends SlotClass
{
    private StructureClass(Symbol symbol)
    {
        super(symbol, new Cons(BuiltInClass.STRUCTURE_OBJECT));
    }

    public StructureClass(Symbol symbol, LispObject directSuperclasses)
    {
        super(symbol, directSuperclasses);
    }

    public LispObject typeOf()
    {
        return Symbol.STRUCTURE_CLASS;
    }

    public LispObject classOf()
    {
        return StandardClass.STRUCTURE_CLASS;
    }

    public LispObject typep(LispObject type) throws ConditionThrowable
    {
        if (type == Symbol.STRUCTURE_CLASS)
            return T;
        if (type == StandardClass.STRUCTURE_CLASS)
            return T;
        return super.typep(type);
    }

    public LispObject getDescription() throws ConditionThrowable
    {
        return new SimpleString(writeToString());
    }

    public String writeToString() throws ConditionThrowable
    {
        StringBuffer sb = new StringBuffer("#<STRUCTURE-CLASS ");
        sb.append(symbol.writeToString());
        sb.append('>');
        return sb.toString();
    }

    // ### make-structure-class name direct-slots slots include => class
    private static final Primitive MAKE_STRUCTURE_CLASS =
        new Primitive("make-structure-class", PACKAGE_SYS, false)
    {
        public LispObject execute(LispObject first, LispObject second,
                                  LispObject third, LispObject fourth)
            throws ConditionThrowable
        {
            Symbol symbol = checkSymbol(first);
            LispObject directSlots = checkList(second);
            LispObject slots = checkList(third);
            Symbol include = checkSymbol(fourth);
            StructureClass c = new StructureClass(symbol);
            if (include != NIL) {
                LispClass includedClass = LispClass.findClass(include);
                if (includedClass == null)
                    return error(new SimpleError("Class " + include +
                                                  " is undefined."));
                c.setCPL(new Cons(c, includedClass.getCPL()));
            } else
                c.setCPL(c, BuiltInClass.STRUCTURE_OBJECT, BuiltInClass.CLASS_T);
            c.setDirectSlotDefinitions(directSlots);
            c.setSlotDefinitions(slots);
            addClass(symbol, c);
            return c;
        }
    };
}
