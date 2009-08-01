/*
 * jclass_name.java
 *
 * Copyright (C) 2005 Peter Graves
 * $Id: jclass_name.java,v 1.2 2007/02/23 21:17:35 piso Exp $
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

// ### jclass-name class-ref &optional name
public final class jclass_name extends Primitive
{
    private jclass_name()
    {
        super(Symbol.JCLASS_NAME, "class-ref &optional name",
"When called with one argument, returns the name of the Java class\n" +
"  designated by CLASS-REF. When called with two arguments, tests\n" +
"  whether CLASS-REF matches NAME.");
    }

    // When called with one argument, JCLASS-NAME returns the name of the class
    // referenced by CLASS-REF.
    public LispObject execute(LispObject arg)
        throws ConditionThrowable
    {
        if (arg instanceof AbstractString) {
            String s = arg.getStringValue();
            try {
                return new SimpleString((Class.forName(s)).getName());
            }
            catch (ClassNotFoundException e) {
                // Fall through.
            }
        } else if (arg instanceof JavaObject) {
            Object obj = ((JavaObject)arg).getObject();
            if (obj instanceof Class)
                return new SimpleString(((Class)obj).getName());
            // Fall through.
        }
        return error(new LispError(arg.writeToString() + " does not designate a Java class."));
    }

    // When called with two arguments, JCLASS-NAME tests whether CLASS-REF
    // matches NAME.
    public LispObject execute(LispObject first, LispObject second)
        throws ConditionThrowable
    {
        String className = null;
        if (first instanceof AbstractString) {
            String s = first.getStringValue();
            try {
                className = (Class.forName(s)).getName();
            }
            catch (ClassNotFoundException e) {}
        } else if (first instanceof JavaObject) {
            Object obj = ((JavaObject)first).getObject();
            if (obj instanceof Class)
                className = ((Class)obj).getName();
        }
        if (className == null)
            return error(new LispError(first.writeToString() + " does not designate a Java class."));
        final AbstractString name;
        try {
            name = (AbstractString) second;
        }
        catch (ClassCastException e) {
            return type_error(second, Symbol.STRING);
        }
        return LispThread.currentThread().setValues(name.getStringValue().equals(className) ? T : NIL,
                                                    new SimpleString(className));
    }

    private static final Primitive JCLASS_NAME = new jclass_name();
}
