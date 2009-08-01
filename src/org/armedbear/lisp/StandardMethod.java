/*
 * StandardMethod.java
 *
 * Copyright (C) 2005 Peter Graves
 * $Id: StandardMethod.java,v 1.8 2007/02/23 21:17:34 piso Exp $
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

public class StandardMethod extends StandardObject
{
  public StandardMethod()
  {
    super(StandardClass.STANDARD_METHOD,
          StandardClass.STANDARD_METHOD.getClassLayout().getLength());
  }

  protected StandardMethod(LispClass cls, int length)
  {
    super(cls, length);
  }

  public StandardMethod(StandardGenericFunction gf,
                        Function fastFunction,
                        LispObject lambdaList,
                        LispObject specializers)
  {
    this();
    slots[StandardMethodClass.SLOT_INDEX_GENERIC_FUNCTION] = gf;
    slots[StandardMethodClass.SLOT_INDEX_LAMBDA_LIST] = lambdaList;
    slots[StandardMethodClass.SLOT_INDEX_SPECIALIZERS] = specializers;
    slots[StandardMethodClass.SLOT_INDEX_QUALIFIERS] = NIL;
    slots[StandardMethodClass.SLOT_INDEX_FUNCTION] = NIL;
    slots[StandardMethodClass.SLOT_INDEX_FAST_FUNCTION] = fastFunction;
    slots[StandardMethodClass.SLOT_INDEX_DOCUMENTATION] = NIL;
  }

  // ### method-lambda-list
  // generic function
  private static final Primitive METHOD_LAMBDA_LIST =
    new Primitive("method-lambda-list", PACKAGE_SYS, true, "method")
    {
      public LispObject execute(LispObject arg) throws ConditionThrowable
      {
        try
          {
            return ((StandardMethod)arg).slots[StandardMethodClass.SLOT_INDEX_LAMBDA_LIST];
          }
        catch (ClassCastException e)
          {
            return type_error(arg, Symbol.STANDARD_METHOD);
          }
      }
    };

  // ### set-method-lambda-list
  private static final Primitive SET_METHOD_LAMBDA_LIST =
    new Primitive("set-method-lambda-list", PACKAGE_SYS, true,
                  "method lambda-list")
    {
      public LispObject execute(LispObject first, LispObject second)
        throws ConditionThrowable
      {
        try
          {
            ((StandardMethod)first).slots[StandardMethodClass.SLOT_INDEX_LAMBDA_LIST] = second;
            return second;
          }
        catch (ClassCastException e)
          {
            return type_error(first, Symbol.STANDARD_METHOD);
          }
      }
    };

  // ### method-qualifiers
  private static final Primitive _METHOD_QUALIFIERS =
    new Primitive("%method-qualifiers", PACKAGE_SYS, true, "method")
    {
      public LispObject execute(LispObject arg) throws ConditionThrowable
      {
        try
          {
            return ((StandardMethod)arg).slots[StandardMethodClass.SLOT_INDEX_QUALIFIERS];
          }
        catch (ClassCastException e)
          {
            return type_error(arg, Symbol.STANDARD_METHOD);
          }
      }
    };

  // ### set-method-qualifiers
  private static final Primitive SET_METHOD_QUALIFIERS =
    new Primitive("set-method-qualifiers", PACKAGE_SYS, true,
                  "method qualifiers")
    {
      public LispObject execute(LispObject first, LispObject second)
        throws ConditionThrowable
      {
        try
          {
            ((StandardMethod)first).slots[StandardMethodClass.SLOT_INDEX_QUALIFIERS] = second;
            return second;
          }
        catch (ClassCastException e)
          {
            return type_error(first, Symbol.STANDARD_METHOD);
          }
      }
    };

  // ### method-documentation
  private static final Primitive METHOD_DOCUMENTATION =
    new Primitive("method-documentation", PACKAGE_SYS, true, "method")
    {
      public LispObject execute(LispObject arg) throws ConditionThrowable
      {
        try
          {
            return ((StandardMethod)arg).slots[StandardMethodClass.SLOT_INDEX_DOCUMENTATION];
          }
        catch (ClassCastException e)
          {
            return type_error(arg, Symbol.STANDARD_METHOD);
          }
      }
    };

  // ### set-method-documentation
  private static final Primitive SET_METHOD_DOCUMENTATION =
    new Primitive("set-method-documentation", PACKAGE_SYS, true,
                  "method documentation")
    {
      public LispObject execute(LispObject first, LispObject second)
        throws ConditionThrowable
      {
        try
          {
            ((StandardMethod)first).slots[StandardMethodClass.SLOT_INDEX_DOCUMENTATION] = second;
            return second;
          }
        catch (ClassCastException e)
          {
            return type_error(first, Symbol.STANDARD_METHOD);
          }
      }
    };

  public LispObject getFunction()
  {
    return slots[StandardMethodClass.SLOT_INDEX_FUNCTION];
  }

  public String writeToString() throws ConditionThrowable
  {
    LispObject genericFunction =
      slots[StandardMethodClass.SLOT_INDEX_GENERIC_FUNCTION];
    if (genericFunction instanceof StandardGenericFunction)
      {
        LispObject name =
          ((StandardGenericFunction)genericFunction).getGenericFunctionName();
        if (name != null)
          {
            FastStringBuffer sb = new FastStringBuffer();
            sb.append(getLispClass().getSymbol().writeToString());
            sb.append(' ');
            sb.append(name.writeToString());
            LispObject specializers =
              slots[StandardMethodClass.SLOT_INDEX_SPECIALIZERS];
            if (specializers != null)
              {
                LispObject specs = specializers;
                LispObject names = NIL;
                while (specs != NIL)
                  {
                    LispObject spec = specs.car();
                    if (spec instanceof LispClass)
                      names = names.push(((LispClass)spec).getSymbol());
                    else
                      names = names.push(spec);
                    specs = specs.cdr();
                  }
                sb.append(' ');
                sb.append(names.nreverse().writeToString());
              }
            return unreadableString(sb.toString());
          }
      }
    return super.writeToString();
  }

  // ### %method-generic-function
  private static final Primitive _METHOD_GENERIC_FUNCTION =
    new Primitive("%method-generic-function", PACKAGE_SYS, true)
    {
      public LispObject execute(LispObject arg) throws ConditionThrowable
      {
        try
          {
            return ((StandardMethod)arg).slots[StandardMethodClass.SLOT_INDEX_GENERIC_FUNCTION];
          }
        catch (ClassCastException e)
          {
            return type_error(arg, Symbol.METHOD);
          }
      }
    };

  // ### %set-method-generic-function
  private static final Primitive _SET_METHOD_GENERICFUNCTION =
    new Primitive("%set-method-generic-function", PACKAGE_SYS, true)
    {
      public LispObject execute(LispObject first, LispObject second)
        throws ConditionThrowable
      {
        try
          {
            ((StandardMethod)first).slots[StandardMethodClass.SLOT_INDEX_GENERIC_FUNCTION] = second;
            return second;
          }
        catch (ClassCastException e)
          {
            return type_error(first, Symbol.METHOD);
          }
      }
    };

  // ### %method-function
  private static final Primitive _METHOD_FUNCTION =
    new Primitive("%method-function", PACKAGE_SYS, true, "method")
    {
      public LispObject execute(LispObject arg) throws ConditionThrowable
      {
        try
          {
            return ((StandardMethod)arg).slots[StandardMethodClass.SLOT_INDEX_FUNCTION];
          }
        catch (ClassCastException e)
          {
            return type_error(arg, Symbol.METHOD);
          }
      }
    };

  // ### %set-method-function
  private static final Primitive _SET_METHOD_FUNCTION =
    new Primitive("%set-method-function", PACKAGE_SYS, true,
                  "method function")
    {
      public LispObject execute(LispObject first, LispObject second)
        throws ConditionThrowable
      {
        try
          {
            ((StandardMethod)first).slots[StandardMethodClass.SLOT_INDEX_FUNCTION] = second;
            return second;
          }
        catch (ClassCastException e)
          {
            return type_error(first, Symbol.METHOD);
          }
      }
    };

  // ### %method-fast-function
  private static final Primitive _METHOD_FAST_FUNCTION =
    new Primitive("%method-fast-function", PACKAGE_SYS, true, "method")
    {
      public LispObject execute(LispObject arg) throws ConditionThrowable
      {
        try
          {
            return ((StandardMethod)arg).slots[StandardMethodClass.SLOT_INDEX_FAST_FUNCTION];
          }
        catch (ClassCastException e)
          {
            return type_error(arg, Symbol.METHOD);
          }
      }
    };

  // ### %set-method-fast-function
  private static final Primitive _SET_METHOD_FAST_FUNCTION =
    new Primitive("%set-method-fast-function", PACKAGE_SYS, true,
                  "method fast-function")
    {
      public LispObject execute(LispObject first, LispObject second)
        throws ConditionThrowable
      {
        try
          {
            ((StandardMethod)first).slots[StandardMethodClass.SLOT_INDEX_FAST_FUNCTION] = second;
            return second;
          }
        catch (ClassCastException e)
          {
            return type_error(first, Symbol.METHOD);
          }
      }
    };

  // ### %method-specializers
  private static final Primitive _METHOD_SPECIALIZERS =
    new Primitive("%method-specializers", PACKAGE_SYS, true, "method")
    {
      public LispObject execute(LispObject arg) throws ConditionThrowable
      {
        try
          {
            return ((StandardMethod)arg).slots[StandardMethodClass.SLOT_INDEX_SPECIALIZERS];
          }
        catch (ClassCastException e)
          {
            return type_error(arg, Symbol.METHOD);
          }
      }
    };

  // ### %set-method-specializers
  private static final Primitive _SET_METHOD_SPECIALIZERS =
    new Primitive("%set-method-specializers", PACKAGE_SYS, true,
                  "method specializers")
    {
      public LispObject execute(LispObject first, LispObject second)
        throws ConditionThrowable
      {
        try
          {
            ((StandardMethod)first).slots[StandardMethodClass.SLOT_INDEX_SPECIALIZERS] = second;
            return second;
          }
        catch (ClassCastException e)
          {
            return type_error(first, Symbol.METHOD);
          }
      }
    };

  private static final StandardGenericFunction METHOD_SPECIALIZERS =
    new StandardGenericFunction("method-specializers",
                                PACKAGE_MOP,
                                true,
                                _METHOD_SPECIALIZERS,
                                list1(Symbol.METHOD),
                                list1(StandardClass.STANDARD_METHOD));

  private static final StandardGenericFunction METHOD_QUALIFIERS =
    new StandardGenericFunction("method-qualifiers",
                                PACKAGE_MOP,
                                true,
                                _METHOD_QUALIFIERS,
                                list1(Symbol.METHOD),
                                list1(StandardClass.STANDARD_METHOD));

}
