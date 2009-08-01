/*
 * machine_version.java
 *
 * Copyright (C) 2004 Peter Graves
 * $Id: machine_version.java,v 1.2 2004/11/03 15:39:02 piso Exp $
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

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

// ### machine-version
public final class machine_version extends Primitive
{
    private machine_version()
    {
        super("machine-version");
    }

    public LispObject execute() throws ConditionThrowable
    {
        String osName = System.getProperty("os.name");
        if (osName != null && osName.toLowerCase().startsWith("linux")) {
            try {
                FileInputStream in = new FileInputStream("/proc/cpuinfo");
                if (in != null) {
                    BufferedReader reader =
                        new BufferedReader(new InputStreamReader(in));
                    try {
                        String s;
                        while ((s = reader.readLine()) != null) {
                            int start = s.indexOf("model name");
                            if (start >= 0) {
                                start = s.indexOf(':', start);
                                if (start >= 0) {
                                    return new SimpleString(s.substring(start + 1).trim());
                                }
                            }
                        }
                    }
                    finally {
                        reader.close();
                    }
                }
            }
            catch (IOException e) {}
        }
        return NIL;
    }

    private static final Primitive MACHINE_VERSION = new machine_version();
}
