/*
 * zip.java
 *
 * Copyright (C) 2005 Peter Graves
 * $Id: zip.java,v 1.4 2007/02/23 21:17:36 piso Exp $
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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

// ### zip pathname pathnames
public final class zip extends Primitive
{
    private zip()
    {
        super("zip", PACKAGE_SYS, true, "pathname pathnames");
    }

    public LispObject execute(LispObject first, LispObject second)
        throws ConditionThrowable
    {
        Pathname zipfilePathname = coerceToPathname(first);
        byte[] buffer = new byte[4096];
        try {
            String zipfileNamestring = zipfilePathname.getNamestring();
            if (zipfileNamestring == null)
                return error(new SimpleError("Pathname has no namestring: " +
                                              zipfilePathname.writeToString()));
            ZipOutputStream out =
                new ZipOutputStream(new FileOutputStream(zipfileNamestring));
            LispObject list = second;
            while (list != NIL) {
                Pathname pathname = coerceToPathname(list.car());
                String namestring = pathname.getNamestring();
                if (namestring == null) {
                    // Clean up before signalling error.
                    out.close();
                    File zipfile = new File(zipfileNamestring);
                    zipfile.delete();
                    return error(new SimpleError("Pathname has no namestring: " +
                                                  pathname.writeToString()));
                }
                File file = new File(namestring);
                FileInputStream in = new FileInputStream(file);
                ZipEntry entry = new ZipEntry(file.getName());
                out.putNextEntry(entry);
                int n;
                while ((n = in.read(buffer)) > 0)
                    out.write(buffer, 0, n);
                out.closeEntry();
                in.close();
                list = list.cdr();
            }
            out.close();
        }
        catch (IOException e) {
            return error(new LispError(e.getMessage()));
        }
        return zipfilePathname;
    }

    private static final Primitive zip = new zip();
}
