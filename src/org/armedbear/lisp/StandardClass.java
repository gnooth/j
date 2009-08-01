/*
 * StandardClass.java
 *
 * Copyright (C) 2003-2005 Peter Graves
 * $Id: StandardClass.java,v 1.47 2005/12/27 19:46:47 piso Exp $
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

public class StandardClass extends SlotClass
{
  public StandardClass()
  {
    setClassLayout(new Layout(this, NIL, NIL));
  }

  public StandardClass(Symbol symbol, LispObject directSuperclasses)
  {
    super(symbol, directSuperclasses);
    setClassLayout(new Layout(this, NIL, NIL));
  }

  public LispObject typeOf()
  {
    return Symbol.STANDARD_CLASS;
  }

  public LispObject classOf()
  {
    return STANDARD_CLASS;
  }

  public LispObject typep(LispObject type) throws ConditionThrowable
  {
    if (type == Symbol.STANDARD_CLASS)
      return T;
    if (type == STANDARD_CLASS)
      return T;
    return super.typep(type);
  }

  public LispObject allocateInstance() throws ConditionThrowable
  {
    Layout layout = getClassLayout();
    if (layout == null)
      {
        Symbol.ERROR.execute(Symbol.SIMPLE_ERROR,
                             Keyword.FORMAT_CONTROL,
                             new SimpleString("No layout for class ~S."),
                             Keyword.FORMAT_ARGUMENTS,
                             list1(this));
      }
    return new StandardObject(this, layout.getLength());
  }

  public String writeToString() throws ConditionThrowable
  {
    FastStringBuffer sb =
      new FastStringBuffer(Symbol.STANDARD_CLASS.writeToString());
    if (symbol != null)
      {
        sb.append(' ');
        sb.append(symbol.writeToString());
      }
    return unreadableString(sb.toString());
  }

  private static final StandardClass addStandardClass(Symbol name,
                                                      LispObject directSuperclasses)
  {
    StandardClass c = new StandardClass(name, directSuperclasses);
    addClass(name, c);
    return c;
  }

  // At this point, BuiltInClass.java has not been completely loaded yet, and
  // BuiltInClass.CLASS_T is null. So we need to call setDirectSuperclass()
  // for STANDARD_CLASS and STANDARD_OBJECT in initializeStandardClasses()
  // below.
  public static final StandardClass STANDARD_CLASS =
    addStandardClass(Symbol.STANDARD_CLASS, list1(BuiltInClass.CLASS_T));
  public static final StandardClass STANDARD_OBJECT =
    addStandardClass(Symbol.STANDARD_OBJECT, list1(BuiltInClass.CLASS_T));

  // BuiltInClass.FUNCTION is also null here (see previous comment).
  public static final StandardClass GENERIC_FUNCTION =
    addStandardClass(Symbol.GENERIC_FUNCTION, list2(BuiltInClass.FUNCTION,
                                                    STANDARD_OBJECT));

  public static final StandardClass CLASS =
    addStandardClass(Symbol.CLASS, list1(STANDARD_OBJECT));

  public static final StandardClass BUILT_IN_CLASS =
    addStandardClass(Symbol.BUILT_IN_CLASS, list1(CLASS));

  public static final StandardClass FORWARD_REFERENCED_CLASS =
    addStandardClass(Symbol.FORWARD_REFERENCED_CLASS, list1(CLASS));

  public static final StandardClass STRUCTURE_CLASS =
    addStandardClass(Symbol.STRUCTURE_CLASS, list1(CLASS));

  public static final StandardClass CONDITION =
    addStandardClass(Symbol.CONDITION, list1(STANDARD_OBJECT));

  public static final StandardClass SIMPLE_CONDITION =
    addStandardClass(Symbol.SIMPLE_CONDITION, list1(CONDITION));

  public static final StandardClass WARNING =
    addStandardClass(Symbol.WARNING, list1(CONDITION));

  public static final StandardClass SIMPLE_WARNING =
    addStandardClass(Symbol.SIMPLE_WARNING, list2(SIMPLE_CONDITION, WARNING));

  public static final StandardClass STYLE_WARNING =
    addStandardClass(Symbol.STYLE_WARNING, list1(WARNING));

  public static final StandardClass SERIOUS_CONDITION =
    addStandardClass(Symbol.SERIOUS_CONDITION, list1(CONDITION));

  public static final StandardClass STORAGE_CONDITION =
    addStandardClass(Symbol.STORAGE_CONDITION, list1(SERIOUS_CONDITION));

  public static final StandardClass ERROR =
    addStandardClass(Symbol.ERROR, list1(SERIOUS_CONDITION));

  public static final StandardClass ARITHMETIC_ERROR =
    addStandardClass(Symbol.ARITHMETIC_ERROR, list1(ERROR));

  public static final StandardClass CELL_ERROR =
    addStandardClass(Symbol.CELL_ERROR, list1(ERROR));

  public static final StandardClass CONTROL_ERROR =
    addStandardClass(Symbol.CONTROL_ERROR, list1(ERROR));

  public static final StandardClass FILE_ERROR =
    addStandardClass(Symbol.FILE_ERROR, list1(ERROR));

  public static final StandardClass DIVISION_BY_ZERO =
    addStandardClass(Symbol.DIVISION_BY_ZERO, list1(ARITHMETIC_ERROR));

  public static final StandardClass FLOATING_POINT_INEXACT =
    addStandardClass(Symbol.FLOATING_POINT_INEXACT, list1(ARITHMETIC_ERROR));

  public static final StandardClass FLOATING_POINT_INVALID_OPERATION =
    addStandardClass(Symbol.FLOATING_POINT_INVALID_OPERATION, list1(ARITHMETIC_ERROR));

  public static final StandardClass FLOATING_POINT_OVERFLOW =
    addStandardClass(Symbol.FLOATING_POINT_OVERFLOW, list1(ARITHMETIC_ERROR));

  public static final StandardClass FLOATING_POINT_UNDERFLOW =
    addStandardClass(Symbol.FLOATING_POINT_UNDERFLOW, list1(ARITHMETIC_ERROR));

  public static final StandardClass PROGRAM_ERROR =
    addStandardClass(Symbol.PROGRAM_ERROR, list1(ERROR));

  public static final StandardClass PACKAGE_ERROR =
    addStandardClass(Symbol.PACKAGE_ERROR, list1(ERROR));

  public static final StandardClass STREAM_ERROR =
    addStandardClass(Symbol.STREAM_ERROR, list1(ERROR));

  public static final StandardClass PARSE_ERROR =
    addStandardClass(Symbol.PARSE_ERROR, list1(ERROR));

  public static final StandardClass PRINT_NOT_READABLE =
    addStandardClass(Symbol.PRINT_NOT_READABLE, list1(ERROR));

  public static final StandardClass READER_ERROR =
    addStandardClass(Symbol.READER_ERROR, list2(PARSE_ERROR, STREAM_ERROR));

  public static final StandardClass END_OF_FILE =
    addStandardClass(Symbol.END_OF_FILE, list1(STREAM_ERROR));

  public static final StandardClass SIMPLE_ERROR =
    addStandardClass(Symbol.SIMPLE_ERROR, list2(SIMPLE_CONDITION, ERROR));

  public static final StandardClass TYPE_ERROR =
    addStandardClass(Symbol.TYPE_ERROR, list1(ERROR));

  public static final StandardClass SIMPLE_TYPE_ERROR =
    addStandardClass(Symbol.SIMPLE_TYPE_ERROR, list2(SIMPLE_CONDITION,
                                                     TYPE_ERROR));

  public static final StandardClass UNBOUND_SLOT =
    addStandardClass(Symbol.UNBOUND_SLOT, list1(CELL_ERROR));

  public static final StandardClass UNBOUND_VARIABLE =
    addStandardClass(Symbol.UNBOUND_VARIABLE, list1(CELL_ERROR));

  public static final StandardClass UNDEFINED_FUNCTION =
    addStandardClass(Symbol.UNDEFINED_FUNCTION, list1(CELL_ERROR));

  public static final StandardClass COMPILER_ERROR =
    addStandardClass(Symbol.COMPILER_ERROR, list1(CONDITION));

  public static final StandardClass COMPILER_UNSUPPORTED_FEATURE_ERROR =
    addStandardClass(Symbol.COMPILER_UNSUPPORTED_FEATURE_ERROR,
                     list1(CONDITION));

  public static final StandardClass JAVA_EXCEPTION =
    addStandardClass(Symbol.JAVA_EXCEPTION, list1(ERROR));

  public static final StandardClass METHOD =
    addStandardClass(Symbol.METHOD, list1(STANDARD_OBJECT));

  public static final StandardClass STANDARD_METHOD =
    new StandardMethodClass();
  static
  {
    addClass(Symbol.STANDARD_METHOD, STANDARD_METHOD);
  }

  public static final StandardClass STANDARD_READER_METHOD =
    new StandardReaderMethodClass();
  static
  {
    addClass(Symbol.STANDARD_READER_METHOD, STANDARD_READER_METHOD);
  }

  public static final StandardClass STANDARD_GENERIC_FUNCTION =
    new StandardGenericFunctionClass();
  static
  {
    addClass(Symbol.STANDARD_GENERIC_FUNCTION, STANDARD_GENERIC_FUNCTION);
  }

  public static final StandardClass SLOT_DEFINITION =
    new SlotDefinitionClass();
  static
  {
    addClass(Symbol.SLOT_DEFINITION, SLOT_DEFINITION);
  }

  public static void initializeStandardClasses() throws ConditionThrowable
  {
    // We need to call setDirectSuperclass() here for classes that have a
    // BuiltInClass as a superclass. See comment above (at first mention of
    // STANDARD_OBJECT).
    STANDARD_CLASS.setDirectSuperclass(CLASS);
    STANDARD_OBJECT.setDirectSuperclass(BuiltInClass.CLASS_T);
    GENERIC_FUNCTION.setDirectSuperclasses(list2(BuiltInClass.FUNCTION,
                                                 STANDARD_OBJECT));

    ARITHMETIC_ERROR.setCPL(ARITHMETIC_ERROR, ERROR, SERIOUS_CONDITION,
                            CONDITION, STANDARD_OBJECT, BuiltInClass.CLASS_T);
    ARITHMETIC_ERROR.setDirectSlotDefinitions(
      list2(new SlotDefinition(Symbol.OPERATION,
                               list1(PACKAGE_CL.intern("ARITHMETIC-ERROR-OPERATION"))),
            new SlotDefinition(Symbol.OPERANDS,
                               list1(PACKAGE_CL.intern("ARITHMETIC-ERROR-OPERANDS")))));
    BUILT_IN_CLASS.setCPL(BUILT_IN_CLASS, CLASS, STANDARD_OBJECT,
                          BuiltInClass.CLASS_T);
    CELL_ERROR.setCPL(CELL_ERROR, ERROR, SERIOUS_CONDITION, CONDITION,
                      STANDARD_OBJECT, BuiltInClass.CLASS_T);
    CELL_ERROR.setDirectSlotDefinitions(
      list1(new SlotDefinition(Symbol.NAME,
                               list1(Symbol.CELL_ERROR_NAME))));
    CLASS.setCPL(CLASS, STANDARD_OBJECT, BuiltInClass.CLASS_T);
    COMPILER_ERROR.setCPL(COMPILER_ERROR, CONDITION, STANDARD_OBJECT,
                          BuiltInClass.CLASS_T);
    COMPILER_UNSUPPORTED_FEATURE_ERROR.setCPL(COMPILER_UNSUPPORTED_FEATURE_ERROR,
                                              CONDITION, STANDARD_OBJECT,
                                              BuiltInClass.CLASS_T);
    CONDITION.setCPL(CONDITION, STANDARD_OBJECT, BuiltInClass.CLASS_T);
    CONDITION.setDirectSlotDefinitions(
      list2(new SlotDefinition(Symbol.FORMAT_CONTROL,
                               list1(Symbol.SIMPLE_CONDITION_FORMAT_CONTROL)),
            new SlotDefinition(Symbol.FORMAT_ARGUMENTS,
                               list1(Symbol.SIMPLE_CONDITION_FORMAT_ARGUMENTS),
                               NIL)));
    CONDITION.setDirectDefaultInitargs(list2(Keyword.FORMAT_ARGUMENTS,
                                             // FIXME
                                             new Closure(list3(Symbol.LAMBDA, NIL, NIL),
                                                         new Environment())));
    CONTROL_ERROR.setCPL(CONTROL_ERROR, ERROR, SERIOUS_CONDITION, CONDITION,
                         STANDARD_OBJECT, BuiltInClass.CLASS_T);
    DIVISION_BY_ZERO.setCPL(DIVISION_BY_ZERO, ARITHMETIC_ERROR, ERROR,
                            SERIOUS_CONDITION, CONDITION, STANDARD_OBJECT,
                            BuiltInClass.CLASS_T);
    END_OF_FILE.setCPL(END_OF_FILE, STREAM_ERROR, ERROR, SERIOUS_CONDITION,
                       CONDITION, STANDARD_OBJECT, BuiltInClass.CLASS_T);
    ERROR.setCPL(ERROR, SERIOUS_CONDITION, CONDITION, STANDARD_OBJECT,
                 BuiltInClass.CLASS_T);
    FILE_ERROR.setCPL(FILE_ERROR, ERROR, SERIOUS_CONDITION, CONDITION,
                      STANDARD_OBJECT, BuiltInClass.CLASS_T);
    FILE_ERROR.setDirectSlotDefinitions(
      list1(new SlotDefinition(Symbol.PATHNAME,
                               list1(PACKAGE_CL.intern("FILE-ERROR-PATHNAME")))));
    FLOATING_POINT_INEXACT.setCPL(FLOATING_POINT_INEXACT, ARITHMETIC_ERROR,
                                  ERROR, SERIOUS_CONDITION, CONDITION,
                                  STANDARD_OBJECT, BuiltInClass.CLASS_T);
    FLOATING_POINT_INVALID_OPERATION.setCPL(FLOATING_POINT_INVALID_OPERATION,
                                            ARITHMETIC_ERROR, ERROR,
                                            SERIOUS_CONDITION, CONDITION,
                                            STANDARD_OBJECT, BuiltInClass.CLASS_T);
    FLOATING_POINT_OVERFLOW.setCPL(FLOATING_POINT_OVERFLOW, ARITHMETIC_ERROR,
                                   ERROR, SERIOUS_CONDITION, CONDITION,
                                   STANDARD_OBJECT, BuiltInClass.CLASS_T);
    FLOATING_POINT_UNDERFLOW.setCPL(FLOATING_POINT_UNDERFLOW, ARITHMETIC_ERROR,
                                    ERROR, SERIOUS_CONDITION, CONDITION,
                                    STANDARD_OBJECT, BuiltInClass.CLASS_T);
    FORWARD_REFERENCED_CLASS.setCPL(FORWARD_REFERENCED_CLASS, CLASS,
                                    BuiltInClass.CLASS_T);
    GENERIC_FUNCTION.setCPL(GENERIC_FUNCTION, STANDARD_OBJECT,
                            BuiltInClass.FUNCTION,
                            BuiltInClass.CLASS_T);
    JAVA_EXCEPTION.setCPL(JAVA_EXCEPTION, ERROR, SERIOUS_CONDITION, CONDITION,
                          STANDARD_OBJECT, BuiltInClass.CLASS_T);
    JAVA_EXCEPTION.setDirectSlotDefinitions(
      list1(new SlotDefinition(Symbol.CAUSE, list1(Symbol.JAVA_EXCEPTION_CAUSE))));
    METHOD.setCPL(METHOD, STANDARD_OBJECT, BuiltInClass.CLASS_T);
    PACKAGE_ERROR.setCPL(PACKAGE_ERROR, ERROR, SERIOUS_CONDITION, CONDITION,
                         STANDARD_OBJECT, BuiltInClass.CLASS_T);
    PACKAGE_ERROR.setDirectSlotDefinitions(
      list1(new SlotDefinition(Symbol.PACKAGE,
                               list1(PACKAGE_CL.intern("PACKAGE-ERROR-PACKAGE")))));
    PARSE_ERROR.setCPL(PARSE_ERROR, ERROR, SERIOUS_CONDITION, CONDITION,
                       STANDARD_OBJECT, BuiltInClass.CLASS_T);
    PRINT_NOT_READABLE.setCPL(PRINT_NOT_READABLE, ERROR, SERIOUS_CONDITION,
                              CONDITION, STANDARD_OBJECT, BuiltInClass.CLASS_T);
    PRINT_NOT_READABLE.setDirectSlotDefinitions(
      list1(new SlotDefinition(Symbol.OBJECT,
                               list1(PACKAGE_CL.intern("PRINT-NOT-READABLE-OBJECT")))));
    PROGRAM_ERROR.setCPL(PROGRAM_ERROR, ERROR, SERIOUS_CONDITION, CONDITION,
                         STANDARD_OBJECT, BuiltInClass.CLASS_T);
    READER_ERROR.setCPL(READER_ERROR, PARSE_ERROR, STREAM_ERROR, ERROR,
                        SERIOUS_CONDITION, CONDITION, STANDARD_OBJECT,
                        BuiltInClass.CLASS_T);
    SERIOUS_CONDITION.setCPL(SERIOUS_CONDITION, CONDITION, STANDARD_OBJECT,
                             BuiltInClass.CLASS_T);
    SIMPLE_CONDITION.setCPL(SIMPLE_CONDITION, CONDITION, STANDARD_OBJECT,
                            BuiltInClass.CLASS_T);
    SIMPLE_ERROR.setCPL(SIMPLE_ERROR, SIMPLE_CONDITION, ERROR,
                        SERIOUS_CONDITION, CONDITION, STANDARD_OBJECT,
                        BuiltInClass.CLASS_T);
    SIMPLE_TYPE_ERROR.setDirectSuperclasses(list2(SIMPLE_CONDITION,
                                                  TYPE_ERROR));
    SIMPLE_TYPE_ERROR.setCPL(SIMPLE_TYPE_ERROR, SIMPLE_CONDITION,
                             TYPE_ERROR, ERROR, SERIOUS_CONDITION,
                             CONDITION, STANDARD_OBJECT, BuiltInClass.CLASS_T);
    SIMPLE_WARNING.setDirectSuperclasses(list2(SIMPLE_CONDITION, WARNING));
    SIMPLE_WARNING.setCPL(SIMPLE_WARNING, SIMPLE_CONDITION, WARNING,
                          CONDITION, STANDARD_OBJECT, BuiltInClass.CLASS_T);
    STANDARD_CLASS.setCPL(STANDARD_CLASS, CLASS,
                          STANDARD_OBJECT, BuiltInClass.CLASS_T);
    STANDARD_OBJECT.setCPL(STANDARD_OBJECT, BuiltInClass.CLASS_T);
    STORAGE_CONDITION.setCPL(STORAGE_CONDITION, SERIOUS_CONDITION, CONDITION,
                             STANDARD_OBJECT, BuiltInClass.CLASS_T);
    STREAM_ERROR.setCPL(STREAM_ERROR, ERROR, SERIOUS_CONDITION, CONDITION,
                        STANDARD_OBJECT, BuiltInClass.CLASS_T);
    STREAM_ERROR.setDirectSlotDefinitions(
      list1(new SlotDefinition(Symbol.STREAM,
                               list1(PACKAGE_CL.intern("STREAM-ERROR-STREAM")))));
    STRUCTURE_CLASS.setCPL(STRUCTURE_CLASS, CLASS, STANDARD_OBJECT,
                           BuiltInClass.CLASS_T);
    STYLE_WARNING.setCPL(STYLE_WARNING, WARNING, CONDITION, STANDARD_OBJECT,
                         BuiltInClass.CLASS_T);
    TYPE_ERROR.setCPL(TYPE_ERROR, ERROR, SERIOUS_CONDITION, CONDITION,
                      STANDARD_OBJECT, BuiltInClass.CLASS_T);
    TYPE_ERROR.setDirectSlotDefinitions(
      list2(new SlotDefinition(Symbol.DATUM,
                               list1(PACKAGE_CL.intern("TYPE-ERROR-DATUM"))),
            new SlotDefinition(Symbol.EXPECTED_TYPE,
                               list1(PACKAGE_CL.intern("TYPE-ERROR-EXPECTED-TYPE")))));
    UNBOUND_SLOT.setCPL(UNBOUND_SLOT, CELL_ERROR, ERROR, SERIOUS_CONDITION,
                        CONDITION, STANDARD_OBJECT, BuiltInClass.CLASS_T);
    UNBOUND_SLOT.setDirectSlotDefinitions(
      list1(new SlotDefinition(Symbol.INSTANCE,
                               list1(PACKAGE_CL.intern("UNBOUND-SLOT-INSTANCE")))));
    UNBOUND_VARIABLE.setCPL(UNBOUND_VARIABLE, CELL_ERROR, ERROR,
                            SERIOUS_CONDITION, CONDITION, STANDARD_OBJECT,
                            BuiltInClass.CLASS_T);
    UNDEFINED_FUNCTION.setCPL(UNDEFINED_FUNCTION, CELL_ERROR, ERROR,
                              SERIOUS_CONDITION, CONDITION, STANDARD_OBJECT,
                              BuiltInClass.CLASS_T);
    WARNING.setCPL(WARNING, CONDITION, STANDARD_OBJECT, BuiltInClass.CLASS_T);

    // Condition classes.
    ARITHMETIC_ERROR.finalizeClass();
    CELL_ERROR.finalizeClass();
    COMPILER_ERROR.finalizeClass();
    COMPILER_UNSUPPORTED_FEATURE_ERROR.finalizeClass();
    CONDITION.finalizeClass();
    CONTROL_ERROR.finalizeClass();
    DIVISION_BY_ZERO.finalizeClass();
    END_OF_FILE.finalizeClass();
    ERROR.finalizeClass();
    FILE_ERROR.finalizeClass();
    FLOATING_POINT_INEXACT.finalizeClass();
    FLOATING_POINT_INVALID_OPERATION.finalizeClass();
    FLOATING_POINT_OVERFLOW.finalizeClass();
    FLOATING_POINT_UNDERFLOW.finalizeClass();
    JAVA_EXCEPTION.finalizeClass();
    PACKAGE_ERROR.finalizeClass();
    PARSE_ERROR.finalizeClass();
    PRINT_NOT_READABLE.finalizeClass();
    PROGRAM_ERROR.finalizeClass();
    READER_ERROR.finalizeClass();
    SERIOUS_CONDITION.finalizeClass();
    SIMPLE_CONDITION.finalizeClass();
    SIMPLE_ERROR.finalizeClass();
    SIMPLE_TYPE_ERROR.finalizeClass();
    SIMPLE_WARNING.finalizeClass();
    STORAGE_CONDITION.finalizeClass();
    STREAM_ERROR.finalizeClass();
    STYLE_WARNING.finalizeClass();
    TYPE_ERROR.finalizeClass();
    UNBOUND_SLOT.finalizeClass();
    UNBOUND_VARIABLE.finalizeClass();
    UNDEFINED_FUNCTION.finalizeClass();
    WARNING.finalizeClass();

    // SYS:SLOT-DEFINITION is constructed and finalized in
    // SlotDefinitionClass.java, but we need to fill in a few things here.
    Debug.assertTrue(SLOT_DEFINITION.isFinalized());
    SLOT_DEFINITION.setCPL(SLOT_DEFINITION, STANDARD_OBJECT,
                           BuiltInClass.CLASS_T);
    SLOT_DEFINITION.setDirectSlotDefinitions(SLOT_DEFINITION.getClassLayout().generateSlotDefinitions());
    // There are no inherited slots.
    SLOT_DEFINITION.setSlotDefinitions(SLOT_DEFINITION.getDirectSlotDefinitions());

    // STANDARD-METHOD
    Debug.assertTrue(STANDARD_METHOD.isFinalized());
    STANDARD_METHOD.setCPL(STANDARD_METHOD, METHOD, STANDARD_OBJECT,
                           BuiltInClass.CLASS_T);
    STANDARD_METHOD.setDirectSlotDefinitions(STANDARD_METHOD.getClassLayout().generateSlotDefinitions());
    // There are no inherited slots.
    STANDARD_METHOD.setSlotDefinitions(STANDARD_METHOD.getDirectSlotDefinitions());

    // STANDARD-READER-METHOD
    Debug.assertTrue(STANDARD_READER_METHOD.isFinalized());
    STANDARD_READER_METHOD.setCPL(STANDARD_READER_METHOD, STANDARD_METHOD,
                                  METHOD, STANDARD_OBJECT, BuiltInClass.CLASS_T);
    STANDARD_READER_METHOD.setSlotDefinitions(STANDARD_READER_METHOD.getClassLayout().generateSlotDefinitions());
    // All but the last slot are inherited.
    STANDARD_READER_METHOD.setDirectSlotDefinitions(list1(STANDARD_READER_METHOD.getSlotDefinitions().reverse().car()));

    // STANDARD-GENERIC-FUNCTION
    Debug.assertTrue(STANDARD_GENERIC_FUNCTION.isFinalized());
    STANDARD_GENERIC_FUNCTION.setCPL(STANDARD_GENERIC_FUNCTION,
                                     GENERIC_FUNCTION, STANDARD_OBJECT,
                                     BuiltInClass.FUNCTION,
                                     BuiltInClass.CLASS_T);
    STANDARD_GENERIC_FUNCTION.setDirectSlotDefinitions(STANDARD_GENERIC_FUNCTION.getClassLayout().generateSlotDefinitions());
    // There are no inherited slots.
    STANDARD_GENERIC_FUNCTION.setSlotDefinitions(STANDARD_GENERIC_FUNCTION.getDirectSlotDefinitions());
  }
}
