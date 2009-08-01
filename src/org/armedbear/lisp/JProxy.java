/*
 * JProxy.java
 *
 * Copyright (C) 2002-2005 Peter Graves, Andras Simon
 * $Id: JProxy.java,v 1.9 2007/02/23 21:17:33 piso Exp $
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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

public final class JProxy extends Lisp
{
  private static final Map table = new WeakHashMap();

  // ### %jnew-proxy interface &rest method-names-and-defs
  private static final Primitive _JNEW_PROXY =
    new Primitive("%jnew-proxy", PACKAGE_JAVA, false,
                  "interface &rest method-names-and-defs")
    {
      public LispObject execute(LispObject[] args) throws ConditionThrowable
      {
        int length = args.length;
        if (length < 3 || length % 2 != 1)
          return error(new WrongNumberOfArgumentsException(this));
        Map lispDefinedMethods = new HashMap();
        for (int i = 1; i < length; i += 2)
          lispDefinedMethods.put(args[i].getStringValue(),
                                 (Function) args[i + 1]);
        Class iface = (Class) args[0].javaInstance();
        Object proxy = Proxy.newProxyInstance(iface.getClassLoader(),
                                              new Class[] { iface },
                                              new LispHandler(table));
        table.put(proxy, new Entry(iface, lispDefinedMethods));
        return new JavaObject(proxy);
      }
    };

  private static class LispHandler implements InvocationHandler
  {
    Map table;

    LispHandler (Map table)
    {
      this.table = table;
    }

    public Object invoke(Object proxy, Method method, Object[] args)
    {
      String methodName = method.getName();

      if (methodName.equals("hashCode"))
          return new Integer(System.identityHashCode(proxy));
      if (methodName.equals("equals"))
        return (proxy == args[0] ? Boolean.TRUE : Boolean.FALSE);
      if (methodName.equals("toString"))
        return proxy.getClass().getName() + '@' + Integer.toHexString(proxy.hashCode());

      if (table.containsKey(proxy))
        {
          Entry entry = (Entry) table.get(proxy);
          Function f = entry.getLispMethod(methodName);
          if (f != null)
            {
              try
                {
                  LispObject lispArgs = NIL;
                  if (args != null)
                    {
                      for (int i = args.length - 1 ; 0 <= i  ; i--)
                        lispArgs = lispArgs.push(new JavaObject(args[i]));
                    }
                  LispObject result = evalCall(f, lispArgs, new Environment(),
                                               LispThread.currentThread());
                  return (method.getReturnType() == void.class ? null : result.javaInstance());
                }
              catch (ConditionThrowable t)
                {
                  t.printStackTrace();
                }
            }
        }
      return null;
    }
  }

  private static class Entry
  {
    Class iface;
    Map lispDefinedMethods;

    public Entry (Class iface, Map lispDefinedMethods)
    {
      this.iface = iface;
      this.lispDefinedMethods = lispDefinedMethods;
    }

    public Function getLispMethod(String methodName)
    {
      if (lispDefinedMethods.containsKey(methodName))
        return (Function)lispDefinedMethods.get(methodName);
      return null;
    }
  }
}
