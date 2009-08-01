/*
 * SpecialOperators.java
 *
 * Copyright (C) 2003-2007 Peter Graves
 * $Id: SpecialOperators.java,v 1.57 2007/09/17 18:15:15 piso Exp $
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package org.armedbear.lisp;

import java.util.ArrayList;

public final class SpecialOperators extends Lisp
{
  // ### quote
  private static final SpecialOperator QUOTE =
    new SpecialOperator(Symbol.QUOTE, "thing")
    {
      public LispObject execute(LispObject args, Environment env)
        throws ConditionThrowable
      {
        if (args.cdr() != NIL)
          return error(new WrongNumberOfArgumentsException(this));
        return ((Cons)args).car;
      }
    };

  // ### if
  private static final SpecialOperator IF =
    new SpecialOperator(Symbol.IF, "test then &optional else")
    {
      public LispObject execute(LispObject args, Environment env)
        throws ConditionThrowable
      {
        final LispThread thread = LispThread.currentThread();
        switch (args.length())
          {
          case 2:
            {
              if (eval(((Cons)args).car, env, thread) != NIL)
                return eval(args.cadr(), env, thread);
              thread.clearValues();
              return NIL;
            }
          case 3:
            {
              if (eval(((Cons)args).car, env, thread) != NIL)
                return eval(args.cadr(), env, thread);
              return eval((((Cons)args).cdr).cadr(), env, thread);
            }
          default:
            return error(new WrongNumberOfArgumentsException(this));
          }
      }
    };

  // ### let
  private static final SpecialOperator LET =
    new SpecialOperator(Symbol.LET, "bindings &body body")
    {
      public LispObject execute(LispObject args, Environment env)
        throws ConditionThrowable
      {
        if (args == NIL)
          return error(new WrongNumberOfArgumentsException(this));
        return _let(args, env, false);
      }
    };

  // ### let*
  private static final SpecialOperator LET_STAR =
    new SpecialOperator(Symbol.LET_STAR, "bindings &body body")
    {
      public LispObject execute(LispObject args, Environment env)
        throws ConditionThrowable
      {
        if (args == NIL)
          return error(new WrongNumberOfArgumentsException(this));
        return _let(args, env, true);
      }
    };

  private static final LispObject _let(LispObject args, Environment env,
                                       boolean sequential)
    throws ConditionThrowable
  {
    LispObject result = NIL;
    final LispThread thread = LispThread.currentThread();
    final SpecialBinding lastSpecialBinding = thread.lastSpecialBinding;
    try
      {
        LispObject varList = checkList(args.car());
        LispObject body = args.cdr();
        // Process declarations.
        LispObject specials = NIL;
        while (body != NIL)
          {
            LispObject obj = body.car();
            if (obj instanceof Cons && ((Cons)obj).car == Symbol.DECLARE)
              {
                LispObject decls = ((Cons)obj).cdr;
                while (decls != NIL)
                  {
                    LispObject decl = decls.car();
                    if (decl instanceof Cons && ((Cons)decl).car == Symbol.SPECIAL)
                      {
                        LispObject vars = ((Cons)decl).cdr;
                        while (vars != NIL)
                          {
                            specials = new Cons(vars.car(), specials);
                            vars = ((Cons)vars).cdr;
                          }
                      }
                    decls = ((Cons)decls).cdr;
                  }
                body = ((Cons)body).cdr;
              }
            else
              break;
          }
        Environment ext = new Environment(env);
        if (sequential)
          {
            // LET*
            while (varList != NIL)
              {
                final Symbol symbol;
                LispObject value;
                LispObject obj = varList.car();
                if (obj instanceof Cons)
                  {
                    if (obj.length() > 2)
                      return error(new LispError("The LET* binding specification " +
                                                  obj.writeToString() +
                                                  " is invalid."));
                    try
                      {
                        symbol = (Symbol) ((Cons)obj).car;
                      }
                    catch (ClassCastException e)
                      {
                        return type_error(((Cons)obj).car, Symbol.SYMBOL);
                      }
                    value = eval(obj.cadr(), ext, thread);
                  }
                else
                  {
                    try
                      {
                        symbol = (Symbol) obj;
                      }
                    catch (ClassCastException e)
                      {
                        return type_error(obj, Symbol.SYMBOL);
                      }
                    value = NIL;
                  }
                if (specials != NIL && memq(symbol, specials))
                  {
                    thread.bindSpecial(symbol, value);
                    ext.declareSpecial(symbol);
                  }
                else if (symbol.isSpecialVariable())
                  {
                    thread.bindSpecial(symbol, value);
                  }
                else
                  ext.bind(symbol, value);
                varList = ((Cons)varList).cdr;
              }
          }
        else
          {
            // LET
            final int length = varList.length();
            LispObject[] vals = new LispObject[length];
            for (int i = 0; i < length; i++)
              {
                LispObject obj = ((Cons)varList).car;
                if (obj instanceof Cons)
                  {
                    if (obj.length() > 2)
                      return error(new LispError("The LET binding specification " +
                                                  obj.writeToString() +
                                                  " is invalid."));
                    vals[i] = eval(obj.cadr(), env, thread);
                  }
                else
                  vals[i] = NIL;
                varList = ((Cons)varList).cdr;
              }
            varList = args.car();
            int i = 0;
            while (varList != NIL)
              {
                final Symbol symbol;
                LispObject obj = varList.car();
                if (obj instanceof Cons)
                  {
                    try
                      {
                        symbol = (Symbol) ((Cons)obj).car;
                      }
                    catch (ClassCastException e)
                      {
                        return type_error(((Cons)obj).car, Symbol.SYMBOL);
                      }
                  }
                else
                  {
                    try
                      {
                        symbol = (Symbol) obj;
                      }
                    catch (ClassCastException e)
                      {
                        return type_error(obj, Symbol.SYMBOL);
                      }
                  }
                LispObject value = vals[i];
                if (specials != NIL && memq(symbol, specials))
                  {
                    thread.bindSpecial(symbol, value);
                    ext.declareSpecial(symbol);
                  }
                else if (symbol.isSpecialVariable())
                  {
                    thread.bindSpecial(symbol, value);
                  }
                else
                  ext.bind(symbol, value);
                varList = ((Cons)varList).cdr;
                ++i;
              }
          }
        // Make sure free special declarations are visible in the body.
        // "The scope of free declarations specifically does not include
        // initialization forms for bindings established by the form
        // containing the declarations." (3.3.4)
        while (specials != NIL)
          {
            Symbol symbol = (Symbol) specials.car();
            ext.declareSpecial(symbol);
            specials = ((Cons)specials).cdr;
          }
        while (body != NIL)
          {
            result = eval(body.car(), ext, thread);
            body = ((Cons)body).cdr;
          }
      }
    finally
      {
        thread.lastSpecialBinding = lastSpecialBinding;
      }
    return result;
  }

  // ### symbol-macrolet
  private static final SpecialOperator SYMBOL_MACROLET =
    new SpecialOperator(Symbol.SYMBOL_MACROLET, "macrobindings &body body")
    {
      public LispObject execute(LispObject args, Environment env)
        throws ConditionThrowable
      {
        LispObject varList = checkList(args.car());
        final LispThread thread = LispThread.currentThread();
        LispObject result = NIL;
        if (varList != NIL)
          {
            SpecialBinding lastSpecialBinding = thread.lastSpecialBinding;
            try
              {
                Environment ext = new Environment(env);
                for (int i = varList.length(); i-- > 0;)
                  {
                    LispObject obj = varList.car();
                    varList = varList.cdr();
                    if (obj instanceof Cons && obj.length() == 2)
                      {
                        Symbol symbol = checkSymbol(obj.car());
                        if (symbol.isSpecialVariable())
                          {
                            return error(new ProgramError(
                              "Attempt to bind the special variable " +
                              symbol.writeToString() +
                              " with SYMBOL-MACROLET."));
                          }
                        bind(symbol, new SymbolMacro(obj.cadr()), ext);
                      }
                    else
                      {
                        return error(new ProgramError(
                          "Malformed symbol-expansion pair in SYMBOL-MACROLET: " +
                          obj.writeToString()));
                      }
                  }
                LispObject body = args.cdr();
                while (body != NIL)
                  {
                    result = eval(body.car(), ext, thread);
                    body = body.cdr();
                  }
              }
            finally
              {
                thread.lastSpecialBinding = lastSpecialBinding;
              }
          }
        else
          {
            LispObject body = args.cdr();
            while (body != NIL)
              {
                result = eval(body.car(), env, thread);
                body = body.cdr();
              }
          }
        return result;
      }
    };

  // ### load-time-value form &optional read-only-p => object
  private static final SpecialOperator LOAD_TIME_VALUE =
    new SpecialOperator(Symbol.LOAD_TIME_VALUE,
                        "form &optional read-only-p")
    {
      public LispObject execute(LispObject args, Environment env)
        throws ConditionThrowable
      {
        switch (args.length())
          {
          case 1:
          case 2:
            return eval(args.car(), new Environment(),
                        LispThread.currentThread());
          default:
            return error(new WrongNumberOfArgumentsException(this));
          }
      }
    };

  // ### locally
  private static final SpecialOperator LOCALLY =
    new SpecialOperator(Symbol.LOCALLY, "&body body")
    {
      public LispObject execute(LispObject args, Environment env)
        throws ConditionThrowable
      {
        final LispThread thread = LispThread.currentThread();
        final Environment ext = new Environment(env);
        args = ext.processDeclarations(args);
        LispObject result = NIL;
        while (args != NIL)
          {
            result = eval(args.car(), ext, thread);
            args = args.cdr();
          }
        return result;
      }
    };

  // ### progn
  private static final SpecialOperator PROGN =
    new SpecialOperator(Symbol.PROGN, "&rest forms")
    {
      public LispObject execute(LispObject args, Environment env)
        throws ConditionThrowable
      {
        LispThread thread = LispThread.currentThread();
        LispObject result = NIL;
        while (args != NIL)
          {
            result = eval(args.car(), env, thread);
            args = ((Cons)args).cdr;
          }
        return result;
      }
    };

  // ### flet
  private static final SpecialOperator FLET =
    new SpecialOperator(Symbol.FLET, "definitions &body body")
    {
      public LispObject execute(LispObject args, Environment env)
        throws ConditionThrowable
      {
        return _flet(args, env, false);
      }
    };

  // ### labels
  private static final SpecialOperator LABELS =
    new SpecialOperator(Symbol.LABELS, "definitions &body body")
    {
      public LispObject execute(LispObject args, Environment env)
        throws ConditionThrowable
      {
        return _flet(args, env, true);
      }
    };

  private static final LispObject _flet(LispObject args, Environment env,
                                        boolean recursive)
    throws ConditionThrowable
  {
    // First argument is a list of local function definitions.
    LispObject defs = checkList(args.car());
    final LispThread thread = LispThread.currentThread();
    LispObject result;
    if (defs != NIL)
      {
        SpecialBinding lastSpecialBinding = thread.lastSpecialBinding;
        Environment ext = new Environment(env);
        while (defs != NIL)
          {
            final LispObject def = checkList(defs.car());
            final LispObject name = def.car();
            final Symbol symbol;
            if (name instanceof Symbol)
              {
                symbol = checkSymbol(name);
                if (symbol.getSymbolFunction() instanceof SpecialOperator)
                  {
                    String message =
                      symbol.getName() + " is a special operator and may not be redefined";
                    return error(new ProgramError(message));
                  }
              }
            else if (isValidSetfFunctionName(name))
              symbol = checkSymbol(name.cadr());
            else
              return type_error(name, FUNCTION_NAME);
            LispObject rest = def.cdr();
            LispObject parameters = rest.car();
            LispObject body = rest.cdr();
            LispObject decls = NIL;
            while (body.car() instanceof Cons && body.car().car() == Symbol.DECLARE)
              {
                decls = new Cons(body.car(), decls);
                body = body.cdr();
              }
            body = new Cons(symbol, body);
            body = new Cons(Symbol.BLOCK, body);
            body = new Cons(body, NIL);
            while (decls != NIL)
              {
                body = new Cons(decls.car(), body);
                decls = decls.cdr();
              }
            LispObject lambda_expression =
              new Cons(Symbol.LAMBDA, new Cons(parameters, body));
            LispObject lambda_name =
              list2(recursive ? Symbol.LABELS : Symbol.FLET, name);
            Closure closure =
              new Closure(lambda_name, lambda_expression,
                          recursive ? ext : env);
            ext.addFunctionBinding(name, closure);
            defs = defs.cdr();
          }
        try
          {
            result = progn(args.cdr(), ext, thread);
          }
        finally
          {
            thread.lastSpecialBinding = lastSpecialBinding;
          }
      }
    else
      result = progn(args.cdr(), env, thread);
    return result;
  }

  // ### the value-type form => result*
  private static final SpecialOperator THE =
    new SpecialOperator(Symbol.THE, "type value")
    {
      public LispObject execute(LispObject args, Environment env)
        throws ConditionThrowable
      {
        if (args.length() != 2)
          return error(new WrongNumberOfArgumentsException(this));
        return eval(args.cadr(), env, LispThread.currentThread());
      }
    };

  // ### progv
  private static final SpecialOperator PROGV =
    new SpecialOperator(Symbol.PROGV, "symbols values &body body")
    {
      public LispObject execute(LispObject args, Environment env)
        throws ConditionThrowable
      {
        if (args.length() < 2)
          return error(new WrongNumberOfArgumentsException(this));
        final LispThread thread = LispThread.currentThread();
        final LispObject symbols = checkList(eval(args.car(), env, thread));
        LispObject values = checkList(eval(args.cadr(), env, thread));
        SpecialBinding lastSpecialBinding = thread.lastSpecialBinding;
        try
          {
            // Set up the new bindings.
            progvBindVars(symbols, values, thread);
            // Implicit PROGN.
            LispObject result = NIL;
            LispObject body = args.cdr().cdr();
            while (body != NIL)
              {
                result = eval(body.car(), env, thread);
                body = body.cdr();
              }
            return result;
          }
        finally
          {
            thread.lastSpecialBinding = lastSpecialBinding;
          }
      }
    };

  // ### declare
  private static final SpecialOperator DECLARE =
    new SpecialOperator(Symbol.DECLARE, "&rest declaration-specifiers")
    {
      public LispObject execute(LispObject args, Environment env)
        throws ConditionThrowable
      {
        while (args != NIL)
          {
            LispObject decl = args.car();
            args = args.cdr();
            if (decl instanceof Cons && decl.car() == Symbol.SPECIAL)
              {
                LispObject vars = decl.cdr();
                while (vars != NIL)
                  {
                    Symbol var = checkSymbol(vars.car());
                    env.declareSpecial(var);
                    vars = vars.cdr();
                  }
              }
          }
        return NIL;
      }
    };

  // ### function
  private static final SpecialOperator FUNCTION =
    new SpecialOperator(Symbol.FUNCTION, "thing")
    {
      public LispObject execute(LispObject args, Environment env)
        throws ConditionThrowable
      {
        final LispObject arg = args.car();
        if (arg instanceof Symbol)
          {
            LispObject operator = env.lookupFunction(arg);
            if (operator instanceof Autoload)
              {
                Autoload autoload = (Autoload) operator;
                autoload.load();
                operator = autoload.getSymbol().getSymbolFunction();
              }
            if (operator instanceof Function)
              return operator;
            if (operator instanceof StandardGenericFunction)
              return operator;
            return error(new UndefinedFunction(arg));
          }
        if (arg instanceof Cons)
          {
            LispObject car = ((Cons)arg).car;
            if (car == Symbol.SETF)
              {
                LispObject f = env.lookupFunction(arg);
                if (f != null)
                  return f;
                Symbol symbol = checkSymbol(arg.cadr());
                f = get(symbol, Symbol.SETF_FUNCTION, null);
                if (f != null)
                  return f;
                f = get(symbol, Symbol.SETF_INVERSE, null);
                if (f != null)
                  return f;
              }
            if (car == Symbol.LAMBDA)
              return new Closure(arg, env);
            if (car == Symbol.NAMED_LAMBDA)
              {
                LispObject name = arg.cadr();
                if (name instanceof Symbol || isValidSetfFunctionName(name))
                  {
                    return new Closure(name,
                                       new Cons(Symbol.LAMBDA, arg.cddr()),
                                       env);
                  }
                return type_error(name, FUNCTION_NAME);
              }
          }
        return error(new UndefinedFunction(list2(Keyword.NAME, arg)));
      }
    };

  // ### setq
  private static final SpecialOperator SETQ =
    new SpecialOperator(Symbol.SETQ, "&rest vars-and-values")
    {
      public LispObject execute(LispObject args, Environment env)
        throws ConditionThrowable
      {
        LispObject value = Symbol.NIL;
        final LispThread thread = LispThread.currentThread();
        while (args != NIL)
          {
            Symbol symbol = checkSymbol(args.car());
            if (symbol.isConstant())
              {
                return error(new ProgramError(symbol.writeToString() +
                                               " is a constant and thus cannot be set."));
              }
            args = args.cdr();
            if (symbol.isSpecialVariable() || env.isDeclaredSpecial(symbol))
              {
                SpecialBinding binding = thread.getSpecialBinding(symbol);
                if (binding != null)
                  {
                    if (binding.value instanceof SymbolMacro)
                      {
                        LispObject expansion =
                          ((SymbolMacro)binding.value).getExpansion();
                        LispObject form = list3(Symbol.SETF, expansion, args.car());
                        value = eval(form, env, thread);
                      }
                    else
                      {
                        value = eval(args.car(), env, thread);
                        binding.value = value;
                      }
                  }
                else
                  {
                    if (symbol.getSymbolValue() instanceof SymbolMacro)
                      {
                        LispObject expansion =
                          ((SymbolMacro)symbol.getSymbolValue()).getExpansion();
                        LispObject form = list3(Symbol.SETF, expansion, args.car());
                        value = eval(form, env, thread);
                      }
                    else
                      {
                        value = eval(args.car(), env, thread);
                        symbol.setSymbolValue(value);
                      }
                  }
              }
            else
              {
                // Not special.
                Binding binding = env.getBinding(symbol);
                if (binding != null)
                  {
                    if (binding.value instanceof SymbolMacro)
                      {
                        LispObject expansion =
                          ((SymbolMacro)binding.value).getExpansion();
                        LispObject form = list3(Symbol.SETF, expansion, args.car());
                        value = eval(form, env, thread);
                      }
                    else
                      {
                        value = eval(args.car(), env, thread);
                        binding.value = value;
                      }
                  }
                else
                  {
                    if (symbol.getSymbolValue() instanceof SymbolMacro)
                      {
                        LispObject expansion =
                          ((SymbolMacro)symbol.getSymbolValue()).getExpansion();
                        LispObject form = list3(Symbol.SETF, expansion, args.car());
                        value = eval(form, env, thread);
                      }
                    else
                      {
                        value = eval(args.car(), env, thread);
                        symbol.setSymbolValue(value);
                      }
                  }
              }
            args = args.cdr();
          }
        // Return primary value only!
        thread._values = null;
        return value;
      }
    };
}
