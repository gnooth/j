/*
 * dolist.java
 *
 * Copyright (C) 2003-2006 Peter Graves
 * $Id: dolist.java,v 1.15 2006/03/16 01:31:45 piso Exp $
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

// ### dolist
public final class dolist extends SpecialOperator
{
  private dolist()
  {
    super(Symbol.DOLIST);
  }

  public LispObject execute(LispObject args, Environment env)
    throws ConditionThrowable
  {
    LispObject bodyForm = args.cdr();
    args = args.car();
    Symbol var = checkSymbol(args.car());
    LispObject listForm = args.cadr();
    final LispThread thread = LispThread.currentThread();
    LispObject resultForm = args.cdr().cdr().car();
    SpecialBinding lastSpecialBinding = thread.lastSpecialBinding;
    final LispObject stack = thread.getStack();
    // Process declarations.
    LispObject specials = NIL;
    while (bodyForm != NIL)
      {
        LispObject obj = bodyForm.car();
        if (obj instanceof Cons && obj.car() == Symbol.DECLARE)
          {
            LispObject decls = obj.cdr();
            while (decls != NIL)
              {
                LispObject decl = decls.car();
                if (decl instanceof Cons && decl.car() == Symbol.SPECIAL)
                  {
                    LispObject vars = decl.cdr();
                    while (vars != NIL)
                      {
                        specials = new Cons(vars.car(), specials);
                        vars = vars.cdr();
                      }
                  }
                decls = decls.cdr();
              }
            bodyForm = bodyForm.cdr();
          }
        else
          break;
      }
    try
      {
        final Environment ext = new Environment(env);
        // Implicit block.
        ext.addBlock(NIL, new LispObject());
        // Evaluate the list form.
        LispObject list = checkList(eval(listForm, ext, thread));
        // Look for tags.
        LispObject remaining = bodyForm;
        while (remaining != NIL)
          {
            LispObject current = remaining.car();
            remaining = remaining.cdr();
            if (current instanceof Cons)
              continue;
            // It's a tag.
            ext.addTagBinding(current, remaining);
          }
        // Establish a reusable binding.
        final Object binding;
        if (specials != NIL && memq(var, specials))
          {
            thread.bindSpecial(var, null);
            binding = thread.getSpecialBinding(var);
            ext.declareSpecial(var);
          }
        else if (var.isSpecialVariable())
          {
            thread.bindSpecial(var, null);
            binding = thread.getSpecialBinding(var);
          }
        else
          {
            ext.bind(var, null);
            binding = ext.getBinding(var);
          }
        while (specials != NIL)
          {
            ext.declareSpecial(checkSymbol(specials.car()));
            specials = specials.cdr();
          }
        while (list != NIL)
          {
            if (binding instanceof SpecialBinding)
              ((SpecialBinding)binding).value = list.car();
            else
              ((Binding)binding).value = list.car();
            LispObject body = bodyForm;
            while (body != NIL)
              {
                LispObject current = body.car();
                if (current instanceof Cons)
                  {
                    try
                      {
                        // Handle GO inline if possible.
                        if (current.car() == Symbol.GO)
                          {
                            LispObject tag = current.cadr();
                            Binding b = ext.getTagBinding(tag);
                            if (b != null && b.value != null)
                              {
                                body = b.value;
                                continue;
                              }
                            throw new Go(tag);
                          }
                        eval(current, ext, thread);
                      }
                    catch (Go go)
                      {
                        LispObject tag = go.getTag();
                        Binding b = ext.getTagBinding(tag);
                        if (b != null && b.value != null)
                          {
                            body = b.value;
                            thread.setStack(stack);
                            continue;
                          }
                        throw go;
                      }
                  }
                body = body.cdr();
              }
            list = list.cdr();
            if (interrupted)
              handleInterrupt();
          }
        if (binding instanceof SpecialBinding)
          ((SpecialBinding)binding).value = NIL;
        else
          ((Binding)binding).value = NIL;
        LispObject result = eval(resultForm, ext, thread);
        return result;
      }
    catch (Return ret)
      {
        if (ret.getTag() == NIL)
          {
            thread.setStack(stack);
            return ret.getResult();
          }
        throw ret;
      }
    finally
      {
        thread.lastSpecialBinding = lastSpecialBinding;
      }
  }

  private static final dolist DOLIST = new dolist();
}
