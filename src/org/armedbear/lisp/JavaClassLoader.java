/*
 * JavaClassLoader.java
 *
 * Copyright (C) 2003-2005 Peter Graves
 * $Id: JavaClassLoader.java,v 1.13 2005/10/15 16:13:20 piso Exp $
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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class JavaClassLoader extends ClassLoader
{
    private static final boolean isSableVM;

    static {
        String vm = System.getProperty("java.vm.name");
        if (vm != null && vm.equals("SableVM"))
            isSableVM = true;
        else
            isSableVM = false;
    }

    private static JavaClassLoader persistentInstance;

    private static Set packages = Collections.synchronizedSet(new HashSet());

    public JavaClassLoader()
    {
        super(JavaClassLoader.class.getClassLoader());
    }

    public static JavaClassLoader getPersistentInstance()
    {
        return getPersistentInstance(null);
    }

    public static JavaClassLoader getPersistentInstance(String packageName)
    {
        if (persistentInstance == null)
            persistentInstance = new JavaClassLoader();
	definePackage(packageName);
        return persistentInstance;
    }

    private static void definePackage(String packageName)
    {
        if (packageName != null && !packages.contains(packageName)) {
            persistentInstance.definePackage(packageName,"","1.0","","","1.0","",null);
            packages.add(packageName);
        }
    }

    public Class loadClassFromByteArray(String className, byte[] classbytes)
    {
        try {
            long length = classbytes.length;
            if (length < Integer.MAX_VALUE) {
                Class c = defineClass(className, classbytes, 0, (int) length);
                if (c != null) {
                    resolveClass(c);
                    return c;
                }
            }
        }
	catch (LinkageError e) {
            throw e;
	}
        catch (Throwable t) {
            Debug.trace(t);
        }
        return null;
    }

    public Class loadClassFromByteArray(String className, byte[] bytes,
                                        int offset, int length)
    {
        try {
            Class c = defineClass(className, bytes, offset, length);
            if (c != null) {
                resolveClass(c);
                return c;
            }
        }
        catch (Throwable t) {
            Debug.trace(t);
        }
        return null;
    }
}
