/*
 * make_condition.java
 *
 * Copyright (C) 2003-2005 Peter Graves
 * $Id: make_condition.java,v 1.20 2005/06/24 01:30:25 piso Exp $
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

public final class make_condition extends Primitive
{
    private make_condition()
    {
        super("%make-condition", PACKAGE_SYS, true);
    }

    // ### %make-condition
    // %make-condition type slot-initializations => condition
    public LispObject execute(LispObject type, LispObject initArgs)
        throws ConditionThrowable
    {
        final Symbol symbol;
        if (type instanceof Symbol)
            symbol = (Symbol) type;
        else if (type instanceof LispClass)
            symbol = ((LispClass)type).getSymbol();
        else {
            // This function only works on symbols and classes.
            return NIL;
        }

        if (symbol == Symbol.ARITHMETIC_ERROR)
            return new ArithmeticError(initArgs);
        if (symbol == Symbol.CELL_ERROR)
            return new CellError(initArgs);
        if (symbol == Symbol.CONDITION)
            return new Condition(initArgs);
        if (symbol == Symbol.CONTROL_ERROR)
            return new ControlError(initArgs);
        if (symbol == Symbol.DIVISION_BY_ZERO)
            return new DivisionByZero(initArgs);
        if (symbol == Symbol.END_OF_FILE)
            return new EndOfFile(initArgs);
        if (symbol == Symbol.ERROR)
            return new LispError(initArgs);
        if (symbol == Symbol.FILE_ERROR)
            return new FileError(initArgs);
        if (symbol == Symbol.FLOATING_POINT_INEXACT)
            return new FloatingPointInexact(initArgs);
        if (symbol == Symbol.FLOATING_POINT_INVALID_OPERATION)
            return new FloatingPointInvalidOperation(initArgs);
        if (symbol == Symbol.FLOATING_POINT_OVERFLOW)
            return new FloatingPointOverflow(initArgs);
        if (symbol == Symbol.FLOATING_POINT_UNDERFLOW)
            return new FloatingPointUnderflow(initArgs);
        if (symbol == Symbol.PACKAGE_ERROR)
            return new PackageError(initArgs);
        if (symbol == Symbol.PARSE_ERROR)
            return new ParseError(initArgs);
        if (symbol == Symbol.PRINT_NOT_READABLE)
            return new PrintNotReadable(initArgs);
        if (symbol == Symbol.PROGRAM_ERROR)
            return new ProgramError(initArgs);
        if (symbol == Symbol.READER_ERROR)
            return new ReaderError(initArgs);
        if (symbol == Symbol.SERIOUS_CONDITION)
            return new SeriousCondition(initArgs);
        if (symbol == Symbol.SIMPLE_CONDITION)
            return new SimpleCondition(initArgs);
        if (symbol == Symbol.SIMPLE_ERROR)
            return new SimpleError(initArgs);
        if (symbol == Symbol.SIMPLE_TYPE_ERROR)
            return new SimpleTypeError(initArgs);
        if (symbol == Symbol.SIMPLE_WARNING)
            return new SimpleWarning(initArgs);
        if (symbol == Symbol.STORAGE_CONDITION)
            return new StorageCondition(initArgs);
        if (symbol == Symbol.STREAM_ERROR)
            return new StreamError(initArgs);
        if (symbol == Symbol.STYLE_WARNING)
            return new StyleWarning(initArgs);
        if (symbol == Symbol.TYPE_ERROR)
            return new TypeError(initArgs);
        if (symbol == Symbol.UNBOUND_SLOT)
            return new UnboundSlot(initArgs);
        if (symbol == Symbol.UNBOUND_VARIABLE)
            return new UnboundVariable(initArgs);
        if (symbol == Symbol.UNDEFINED_FUNCTION)
            return new UndefinedFunction(initArgs);
        if (symbol == Symbol.WARNING)
            return new Warning(initArgs);

        if (symbol == Symbol.COMPILER_ERROR)
            return new CompilerError(initArgs);
        if (symbol == Symbol.COMPILER_UNSUPPORTED_FEATURE_ERROR)
            return new CompilerUnsupportedFeatureError(initArgs);

        return NIL;
    }

    private static final Primitive MAKE_CONDITION = new make_condition();
}
