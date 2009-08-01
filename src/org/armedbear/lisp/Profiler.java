/*
 * Profiler.java
 *
 * Copyright (C) 2003-2005 Peter Graves
 * $Id: Profiler.java,v 1.14 2007/02/23 21:17:34 piso Exp $
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

public class Profiler extends Lisp
{
    private static int sleep = 1;

    public static final void sample(LispThread thread)
        throws ConditionThrowable
    {
        sampleNow = false;
        thread.incrementCallCounts();
    }

    private static final Runnable profilerRunnable = new Runnable() {
        public void run()
        {
            while (profiling) {
                sampleNow = true;
                try {
                    Thread.sleep(sleep);
                }
                catch (InterruptedException e) {
                    Debug.trace(e);
                }
            }
        }
    };

    // ### %start-profiler
    // %start-profiler type granularity
    public static final Primitive _START_PROFILER =
        new Primitive("%start-profiler", PACKAGE_PROF, false)
    {
        public LispObject execute(LispObject first, LispObject second)
            throws ConditionThrowable
        {
            final LispThread thread = LispThread.currentThread();
            Stream out = getStandardOutput();
            out.freshLine();
            if (profiling) {
                out._writeLine("; Profiler already started.");
            } else {
                if (first == Keyword.TIME)
                    sampling = true;
                else if (first == Keyword.COUNT_ONLY)
                    sampling = false;
                else
                    return error(new LispError(
                        "%START-PROFILER: argument must be either :TIME or :COUNT-ONLY"));
                Package[] packages = Packages.getAllPackages();
                for (int i = 0; i < packages.length; i++) {
                    Package pkg = packages[i];
                    Symbol[] symbols = pkg.symbols();
                    for (int j = 0; j < symbols.length; j++) {
                        Symbol symbol = symbols[j];
                        LispObject object = symbol.getSymbolFunction();
                        if (object != null) {
                            object.setCallCount(0);
                            if (object instanceof StandardGenericFunction) {
                                LispObject methods =
                                    PACKAGE_MOP.intern("GENERIC-FUNCTION-METHODS").execute(object);
                                while (methods != NIL) {
                                    StandardMethod method = (StandardMethod) methods.car();
                                    method.getFunction().setCallCount(0);
                                    methods = methods.cdr();
                                }
                            }
                        }
                    }
                }
                if (sampling) {
                    sleep = Fixnum.getValue(second);
                    thread.resetStack();
                    Thread t = new Thread(profilerRunnable);
                    int priority =
                        Math.min(Thread.currentThread().getPriority() + 1,
                                 Thread.MAX_PRIORITY);
                    t.setPriority(priority);
                    new Thread(profilerRunnable).start();
                }
                out._writeLine("; Profiler started.");
                profiling = true;
            }
            return thread.nothing();
        }
    };

    // ### stop-profiler
    public static final Primitive STOP_PROFILER =
        new Primitive("stop-profiler", PACKAGE_PROF, true)
    {
        public LispObject execute() throws ConditionThrowable
        {
            Stream out = getStandardOutput();
            out.freshLine();
            if (profiling) {
                profiling = false;
                out._writeLine("; Profiler stopped.");
            } else
                out._writeLine("; Profiler was not started.");
            out._finishOutput();
            return LispThread.currentThread().nothing();
        }
    };
}
