/*
 * RuntimeClass.java
 *
 * Copyright (C) 2004 Peter Graves
 * $Id: RuntimeClass.java,v 1.12 2007/02/23 21:17:34 piso Exp $
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

import java.io.File;
import java.util.Map;
import java.util.HashMap;

public class RuntimeClass extends Lisp
{
    private static Map classes = new HashMap();

    private Map methods = new HashMap();

    // ### %jnew-runtime-class
    // %jnew-runtime-class class-name &rest method-names-and-defs
    private static final Primitive _JNEW_RUNTIME_CLASS =
        new Primitive("%jnew-runtime-class", PACKAGE_JAVA, false, "class-name &rest method-names-and-defs")
    {
        public LispObject execute(LispObject[] args) throws ConditionThrowable
        {
            int length = args.length;
            if (length < 3 || length % 2 != 1)
                return error(new WrongNumberOfArgumentsException(this));
	    RuntimeClass rc = new RuntimeClass();
	    String className = args[0].getStringValue();
            for (int i = 1; i < length; i = i+2) {
                String methodName = args[i].getStringValue();
                rc.addLispMethod(methodName, (Function)args[i+1]);
	    }
            classes.put(className, rc);
	    return T;
        }
    };

    // ### jredefine-method
    // %jredefine-method class-name method-name method-def
    private static final Primitive _JREDEFINE_METHOD =
        new Primitive("%jredefine-method", PACKAGE_JAVA, false,
                      "class-name method-name method-def")
    {
        public LispObject execute(LispObject className, LispObject methodName,
                                  LispObject methodDef)
            throws ConditionThrowable
        {

	    String cn = className.getStringValue();
	    String mn = methodName.getStringValue();
	    Function def = (Function) methodDef;
	    RuntimeClass rc = null;
	    if (classes.containsKey(cn)) {
                rc = (RuntimeClass) classes.get(cn);
                rc.addLispMethod(mn, def);
                return T;
	    }
	    else {
                error(new LispError("undefined Java class: " + cn));
                return NIL;
	    }
        }
    };

    // ### %load-java-class-from-byte-array
    private static final Primitive _LOAD_JAVA_CLASS_FROM_BYTE_ARRAY =
        new Primitive("%load-java-class-from-byte-array", PACKAGE_JAVA, false,
                      "classname bytearray")
    {
        public LispObject execute(LispObject className, LispObject classBytes)
            throws ConditionThrowable
        {
            String cn = className.getStringValue();
	    String pn = cn.substring(0,cn.lastIndexOf('.'));
	    byte[] cb = (byte[]) classBytes.javaInstance();
            try {
                JavaClassLoader loader = JavaClassLoader.getPersistentInstance(pn);
                Class c = loader.loadClassFromByteArray(cn, cb);
                if (c != null) {
                    return T;
                }
            }
            catch (VerifyError e) {
                return error(new LispError("class verification failed: " +
                                            e.getMessage()));
            }
            catch (LinkageError e) {
                return error(new LispError("class could not be linked: " +
                                            e.getMessage()));
            }
            catch (Throwable t) {
                Debug.trace(t);
            }
            return error(
                new LispError("unable to load ".concat(cn)));
        }
    };

    public static final LispObject evalC(LispObject function,
                                         LispObject args,
                                         Environment env,
                                         LispThread thread)
        throws ConditionThrowable
    {
        return evalCall(function, args, env, thread);
    }

    public static RuntimeClass getRuntimeClass(String className) {
        return (RuntimeClass) classes.get(className);
    }

    public Function getLispMethod(String methodName) {
        return (Function) methods.get(methodName);
    }

    private void addLispMethod(String methodName, Function def) {
        methods.put(methodName, def);
    }

    public static final LispObject makeLispObject(Object obj) throws ConditionThrowable
    {
        return new JavaObject(obj);
    }

    public static final Fixnum makeLispObject(byte i) throws ConditionThrowable
    {
        return new Fixnum(i);
    }

    public static final Fixnum makeLispObject(short i) throws ConditionThrowable
    {
        return new Fixnum(i);
    }

    public static final Fixnum makeLispObject(int i) throws ConditionThrowable
    {
        return new Fixnum(i);
    }

    public static final Bignum makeLispObject(long i) throws ConditionThrowable
    {
        return new Bignum(i);
    }

    public static final SingleFloat makeLispObject(float i) throws ConditionThrowable
    {
        return new SingleFloat(i);
    }

    public static final DoubleFloat makeLispObject(double i) throws ConditionThrowable
    {
        return new DoubleFloat(i);
    }

    public static final LispCharacter makeLispObject(char i) throws ConditionThrowable
    {
        return LispCharacter.getInstance(i);
    }

    public static final LispObject makeLispObject(boolean i) throws ConditionThrowable
    {
        return i ? T : NIL;
    }
}
