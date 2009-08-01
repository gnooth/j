/*
 * MacroObject.java
 *
 * Copyright (C) 2003-2005 Peter Graves
 * $Id: MacroObject.java,v 1.16 2007/02/23 21:17:34 piso Exp $
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

public final class MacroObject extends Function
{
  private final LispObject name;
  public final LispObject expander;

  public MacroObject(LispObject name, LispObject expander)
  {
    this.name = name;
    this.expander = expander;
    if (name instanceof Symbol && name != NIL && expander instanceof Function)
      ((Function)expander).setLambdaName(list2(Symbol.MACRO_FUNCTION,
                                               name));
  }

  public LispObject execute() throws ConditionThrowable
  {
    return error(new UndefinedFunction(name));
  }

  public LispObject execute(LispObject arg) throws ConditionThrowable
  {
    return error(new UndefinedFunction(name));
  }

  public LispObject execute(LispObject first, LispObject second)
    throws ConditionThrowable
  {
    return error(new UndefinedFunction(name));
  }

  public LispObject execute(LispObject first, LispObject second,
                            LispObject third)
    throws ConditionThrowable
  {
    return error(new UndefinedFunction(name));
  }

  public LispObject execute(LispObject first, LispObject second,
                            LispObject third, LispObject fourth)
    throws ConditionThrowable
  {
    return error(new UndefinedFunction(name));
  }

  public LispObject execute(LispObject first, LispObject second,
                            LispObject third, LispObject fourth,
                            LispObject fifth)
    throws ConditionThrowable
  {
    return error(new UndefinedFunction(name));
  }

  public LispObject execute(LispObject first, LispObject second,
                            LispObject third, LispObject fourth,
                            LispObject fifth, LispObject sixth)
    throws ConditionThrowable
  {
    return error(new UndefinedFunction(name));
  }

  public LispObject execute(LispObject first, LispObject second,
                            LispObject third, LispObject fourth,
                            LispObject fifth, LispObject sixth,
                            LispObject seventh)
    throws ConditionThrowable
  {
    return error(new UndefinedFunction(name));
  }

  public LispObject execute(LispObject first, LispObject second,
                            LispObject third, LispObject fourth,
                            LispObject fifth, LispObject sixth,
                            LispObject seventh, LispObject eighth)
    throws ConditionThrowable
  {
    return error(new UndefinedFunction(name));
  }

  public LispObject execute(LispObject[] args) throws ConditionThrowable
  {
    return error(new UndefinedFunction(name));
  }

  public String writeToString()
  {
    return unreadableString("MACRO-OBJECT");
  }
}
