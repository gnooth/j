/*
 * Autoload.java
 *
 * Copyright (C) 2003-2006 Peter Graves
 * $Id: Autoload.java,v 1.259 2007/02/23 21:17:32 piso Exp $
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

public class Autoload extends Function
{
    protected final String fileName;
    protected final String className;

    private final Symbol symbol;

    protected Autoload(Symbol symbol)
    {
        super();
        fileName = null;
        className = null;
        this.symbol = symbol;
        symbol.setBuiltInFunction(false);
    }

    protected Autoload(Symbol symbol, String fileName, String className)
    {
        super();
        this.fileName = fileName;
        this.className = className;
        this.symbol = symbol;
        symbol.setBuiltInFunction(false);
    }

    protected final Symbol getSymbol()
    {
        return symbol;
    }

    public static void autoload(String symbolName, String className)
    {
        autoload(PACKAGE_CL, symbolName, className);
    }

    public static void autoload(Package pkg, String symbolName,
                                String className)
    {
        autoload(pkg, symbolName, className, false);
    }

    public static void autoload(Package pkg, String symbolName,
                                String className, boolean exported)
    {
        Symbol symbol = intern(symbolName.toUpperCase(), pkg);
        if (pkg != PACKAGE_CL && exported) {
            try {
                pkg.export(symbol);
            }
            catch (ConditionThrowable t) {
                Debug.assertTrue(false);
            }
        }
        if (symbol.getSymbolFunction() == null)
            symbol.setSymbolFunction(new Autoload(symbol, null,
                                                  "org.armedbear.lisp.".concat(className)));
    }

    public static void autoload(Symbol symbol, String className)
    {
        if (symbol.getSymbolFunction() == null)
            symbol.setSymbolFunction(new Autoload(symbol, null,
                                                  "org.armedbear.lisp.".concat(className)));
    }

    public void load() throws ConditionThrowable
    {
        if (className != null) {
            final LispThread thread = LispThread.currentThread();
            final SpecialBinding lastSpecialBinding = thread.lastSpecialBinding;
            int loadDepth = Fixnum.getValue(_LOAD_DEPTH_.symbolValue());
            thread.bindSpecial(_LOAD_DEPTH_, new Fixnum(++loadDepth));
            try {
                if (_AUTOLOAD_VERBOSE_.symbolValue(thread) != NIL) {
                    final String prefix = Load.getLoadVerbosePrefix(loadDepth);
                    Stream out = getStandardOutput();
                    out._writeString(prefix);
                    out._writeString(" Autoloading ");
                    out._writeString(className);
                    out._writeLine(" ...");
                    out._finishOutput();
                    long start = System.currentTimeMillis();
                    Class.forName(className);
                    long elapsed = System.currentTimeMillis() - start;
                    out._writeString(prefix);
                    out._writeString(" Autoloaded ");
                    out._writeString(className);
                    out._writeString(" (");
                    out._writeString(String.valueOf(((float)elapsed)/1000));
                    out._writeLine(" seconds)");
                    out._finishOutput();
                } else
                    Class.forName(className);
            }
            catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            finally {
                thread.lastSpecialBinding = lastSpecialBinding;
            }
        } else
            Load.loadSystemFile(getFileName(), true);
        if (debug) {
            if (symbol != null) {
                if (symbol.getSymbolFunction() instanceof Autoload) {
                    Debug.trace("Unable to autoload " + symbol.writeToString());
                    System.exit(-1);
                }
            }
        }
    }

    protected final String getFileName()
    {
        if (fileName != null)
            return fileName;
        return symbol.getName().toLowerCase();
    }

    public LispObject execute() throws ConditionThrowable
    {
        load();
        return symbol.execute();
    }

    public LispObject execute(LispObject arg) throws ConditionThrowable
    {
        load();
        return symbol.execute(arg);
    }

    public LispObject execute(LispObject first, LispObject second)
        throws ConditionThrowable
    {
        load();
        return symbol.execute(first, second);
    }

    public LispObject execute(LispObject first, LispObject second,
                              LispObject third)
        throws ConditionThrowable
    {
        load();
        return symbol.execute(first, second, third);
    }

    public LispObject execute(LispObject first, LispObject second,
                              LispObject third, LispObject fourth)
        throws ConditionThrowable
    {
        load();
        return symbol.execute(first, second, third, fourth);
    }

    public LispObject execute(LispObject first, LispObject second,
                              LispObject third, LispObject fourth,
                              LispObject fifth)
        throws ConditionThrowable
    {
        load();
        return symbol.execute(first, second, third, fourth, fifth);
    }

    public LispObject execute(LispObject first, LispObject second,
                              LispObject third, LispObject fourth,
                              LispObject fifth, LispObject sixth)
        throws ConditionThrowable
    {
        load();
        return symbol.execute(first, second, third, fourth, fifth, sixth);
    }

    public LispObject execute(LispObject first, LispObject second,
                              LispObject third, LispObject fourth,
                              LispObject fifth, LispObject sixth,
                              LispObject seventh)
        throws ConditionThrowable
    {
        load();
        return symbol.execute(first, second, third, fourth, fifth, sixth,
                              seventh);
    }

    public LispObject execute(LispObject first, LispObject second,
                              LispObject third, LispObject fourth,
                              LispObject fifth, LispObject sixth,
                              LispObject seventh, LispObject eighth)
        throws ConditionThrowable
    {
        load();
        return symbol.execute(first, second, third, fourth, fifth, sixth,
                              seventh, eighth);
    }

    public LispObject execute(LispObject[] args) throws ConditionThrowable
    {
        load();
        return symbol.execute(args);
    }

    public String writeToString() throws ConditionThrowable
    {
        StringBuffer sb = new StringBuffer("#<AUTOLOAD ");
        sb.append(symbol.writeToString());
        sb.append(" \"");
        if (className != null) {
            int index = className.lastIndexOf('.');
            if (index >= 0)
                sb.append(className.substring(index + 1));
            else
                sb.append(className);
            sb.append(".class");
        } else
            sb.append(getFileName());
        sb.append("\">");
        return sb.toString();
    }

    // ### autoload
    private static final Primitive AUTOLOAD =
        new Primitive("autoload", PACKAGE_EXT, true)
    {
        public LispObject execute(LispObject first) throws ConditionThrowable
        {
            if (first instanceof Symbol) {
                Symbol symbol = (Symbol) first;
                symbol.setSymbolFunction(new Autoload(symbol));
                return T;
            }
            if (first instanceof Cons) {
                for (LispObject list = first; list != NIL; list = list.cdr()) {
                    Symbol symbol = checkSymbol(list.car());
                    symbol.setSymbolFunction(new Autoload(symbol));
                }
                return T;
            }
            return error(new TypeError(first));
        }
        public LispObject execute(LispObject first, LispObject second)
            throws ConditionThrowable
        {
            final String fileName = second.getStringValue();
            if (first instanceof Symbol) {
                Symbol symbol = (Symbol) first;
                symbol.setSymbolFunction(new Autoload(symbol, fileName, null));
                return T;
            }
            if (first instanceof Cons) {
                for (LispObject list = first; list != NIL; list = list.cdr()) {
                    Symbol symbol = checkSymbol(list.car());
                    symbol.setSymbolFunction(new Autoload(symbol, fileName, null));
                }
                return T;
            }
            return error(new TypeError(first));
        }
    };

    // ### resolve
    // Force autoload to be resolved.
    private static final Primitive RESOLVE =
        new Primitive("resolve", PACKAGE_EXT, true, "symbol")
    {
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            Symbol symbol = checkSymbol(arg);
            LispObject fun = symbol.getSymbolFunction();
            if (fun instanceof Autoload) {
                Autoload autoload = (Autoload) fun;
                autoload.load();
                return symbol.getSymbolFunction();
            }
            return fun;
        }
    };

    // ### autoloadp
    private static final Primitive AUTOLOADP =
        new Primitive("autoloadp", PACKAGE_EXT, true, "symbol")
    {
        public LispObject execute(LispObject arg) throws ConditionThrowable
        {
            if (arg instanceof Symbol) {
                if (arg.getSymbolFunction() instanceof Autoload)
                    return T;
            }
            return NIL;
        }
    };

    static {
        autoload("acos", "MathFunctions");
        autoload("acosh", "MathFunctions");
        autoload("arithmetic-error-operands", "ArithmeticError");
        autoload("arithmetic-error-operation", "ArithmeticError");
        autoload("ash", "ash");
        autoload("asin", "MathFunctions");
        autoload("asinh", "MathFunctions");
        autoload("atan", "MathFunctions");
        autoload("atanh", "MathFunctions");
        autoload("broadcast-stream-streams", "BroadcastStream");
        autoload("ceiling", "ceiling");
        autoload("cell-error-name", "cell_error_name");
        autoload("char", "StringFunctions");
        autoload("char-equal", "CharacterFunctions");
        autoload("char-greaterp", "CharacterFunctions");
        autoload("char-lessp", "CharacterFunctions");
        autoload("char-not-greaterp", "CharacterFunctions");
        autoload("char-not-lessp", "CharacterFunctions");
        autoload("char<", "CharacterFunctions");
        autoload("char<=", "CharacterFunctions");
        autoload("char=", "CharacterFunctions");
        autoload("cis", "MathFunctions");
        autoload("clrhash", "HashTableFunctions");
        autoload("clrhash", "HashTableFunctions");
        autoload("concatenated-stream-streams", "ConcatenatedStream");
        autoload("cos", "MathFunctions");
        autoload("cosh", "MathFunctions");
        autoload("delete-file", "delete_file");
        autoload("delete-package", "PackageFunctions");
        autoload("echo-stream-input-stream", "EchoStream");
        autoload("echo-stream-output-stream", "EchoStream");
        autoload("exp", "MathFunctions");
        autoload("expt", "MathFunctions");
        autoload("file-author", "file_author");
        autoload("file-error-pathname", "file_error_pathname");
        autoload("file-length", "file_length");
        autoload("file-string-length", "file_string_length");
        autoload("file-write-date", "file_write_date");
        autoload("float", "FloatFunctions");
        autoload("float-digits", "FloatFunctions");
        autoload("float-radix", "FloatFunctions");
        autoload("float-sign", "float_sign");
        autoload("floatp", "FloatFunctions");
        autoload("floor", "floor");
        autoload("ftruncate", "ftruncate");
        autoload("get-internal-real-time", "Time");
        autoload("get-internal-run-time", "Time");
        autoload("get-output-stream-string", "StringOutputStream");
        autoload("get-properties", "get_properties");
        autoload("get-universal-time", "Time");
        autoload("gethash", "HashTableFunctions");
        autoload("gethash", "HashTableFunctions");
        autoload("hash-table-count", "HashTableFunctions");
        autoload("hash-table-count", "HashTableFunctions");
        autoload("hash-table-p", "HashTableFunctions");
        autoload("hash-table-p", "HashTableFunctions");
        autoload("hash-table-rehash-size", "HashTableFunctions");
        autoload("hash-table-rehash-size", "HashTableFunctions");
        autoload("hash-table-rehash-threshold", "HashTableFunctions");
        autoload("hash-table-rehash-threshold", "HashTableFunctions");
        autoload("hash-table-size", "HashTableFunctions");
        autoload("hash-table-size", "HashTableFunctions");
        autoload("hash-table-test", "HashTableFunctions");
        autoload("hash-table-test", "HashTableFunctions");
        autoload("import", "PackageFunctions");
        autoload("input-stream-p", "input_stream_p");
        autoload("integer-decode-float", "FloatFunctions");
        autoload("interactive-stream-p", "interactive_stream_p");
        autoload("last", "last");
        autoload("lisp-implementation-type", "lisp_implementation_type");
        autoload("lisp-implementation-version", "lisp_implementation_version");
        autoload("list-all-packages", "PackageFunctions");
        autoload("listen", "listen");
        autoload("log", "MathFunctions");
        autoload("logand", "logand");
        autoload("logandc1", "logandc1");
        autoload("logandc2", "logandc2");
        autoload("logbitp", "logbitp");
        autoload("logcount", "logcount");
        autoload("logeqv", "logeqv");
        autoload("logior", "logior");
        autoload("lognand", "lognand");
        autoload("lognor", "lognor");
        autoload("lognot", "lognot");
        autoload("logorc1", "logorc1");
        autoload("logorc2", "logorc2");
        autoload("logtest", "logtest");
        autoload("logxor", "logxor");
        autoload("long-site-name", "SiteName");
        autoload("machine-instance", "SiteName");
        autoload("machine-type", "machine_type");
        autoload("machine-version", "machine_version");
        autoload("make-broadcast-stream", "BroadcastStream");
        autoload("make-concatenated-stream", "ConcatenatedStream");
        autoload("make-echo-stream", "EchoStream");
        autoload("make-string-input-stream", "StringInputStream");
        autoload("make-synonym-stream", "SynonymStream");
        autoload("maphash", "HashTableFunctions");
        autoload("mod", "mod");
        autoload("open-stream-p", "open_stream_p");
        autoload("output-stream-p", "output_stream_p");
        autoload("package-error-package", "package_error_package");
        autoload("package-error-package", "package_error_package");
        autoload("package-name", "PackageFunctions");
        autoload("package-nicknames", "PackageFunctions");
        autoload("package-shadowing-symbols", "PackageFunctions");
        autoload("package-use-list", "PackageFunctions");
        autoload("package-used-by-list", "PackageFunctions");
        autoload("packagep", "PackageFunctions");
        autoload("peek-char", "peek_char");
        autoload("print-not-readable-object", "PrintNotReadable");
        autoload("probe-file", "probe_file");
        autoload("rational", "FloatFunctions");
        autoload("read-char-no-hang", "read_char_no_hang");
        autoload("read-delimited-list", "read_delimited_list");
        autoload("rem", "rem");
        autoload("remhash", "HashTableFunctions");
        autoload("remhash", "HashTableFunctions");
        autoload("rename-package", "PackageFunctions");
        autoload("room", "room");
        autoload("scale-float", "FloatFunctions");
        autoload("schar", "StringFunctions");
        autoload("shadow", "PackageFunctions");
        autoload("shadowing-import", "PackageFunctions");
        autoload("short-site-name", "SiteName");
        autoload("simple-condition-format-arguments", "SimpleCondition");
        autoload("simple-condition-format-control", "SimpleCondition");
        autoload("simple-string-p", "StringFunctions");
        autoload("sin", "MathFunctions");
        autoload("sinh", "MathFunctions");
        autoload("software-type", "software_type");
        autoload("software-version", "software_version");
        autoload("sqrt", "MathFunctions");
        autoload("stream-element-type", "stream_element_type");
        autoload("stream-error-stream", "StreamError");
        autoload("stream-external-format", "stream_external_format");
        autoload("stringp", "StringFunctions");
        autoload("sxhash", "HashTableFunctions");
        autoload("sxhash", "HashTableFunctions");
        autoload("synonym-stream-symbol", "SynonymStream");
        autoload("tan", "MathFunctions");
        autoload("tanh", "MathFunctions");
        autoload("truename", "probe_file");
        autoload("truncate", "truncate");
        autoload("type-error-datum", "TypeError");
        autoload("type-error-expected-type", "TypeError");
        autoload("unbound-slot-instance", "unbound_slot_instance");
        autoload("unexport", "PackageFunctions");
        autoload("unuse-package", "PackageFunctions");
        autoload(PACKAGE_EXT, "arglist", "arglist", true);
        autoload(PACKAGE_EXT, "assq", "assq", true);
        autoload(PACKAGE_EXT, "assql", "assql", true);
        autoload(PACKAGE_EXT, "file-directory-p", "probe_file", true);
        autoload(PACKAGE_EXT, "gc", "gc", true);
        autoload(PACKAGE_EXT, "get-floating-point-modes", "FloatFunctions", true);
        autoload(PACKAGE_EXT, "get-mutex", "Mutex", true);
        autoload(PACKAGE_EXT, "mailbox-empty-p", "Mailbox", true);
        autoload(PACKAGE_EXT, "mailbox-peek", "Mailbox", true);
        autoload(PACKAGE_EXT, "mailbox-read", "Mailbox", true);
        autoload(PACKAGE_EXT, "mailbox-send", "Mailbox", true);
        autoload(PACKAGE_EXT, "make-mailbox", "Mailbox", true);
        autoload(PACKAGE_EXT, "make-mutex", "Mutex", true);
        autoload(PACKAGE_EXT, "make-slime-input-stream", "SlimeInputStream", true);
        autoload(PACKAGE_EXT, "make-slime-output-stream", "SlimeOutputStream", true);
        autoload(PACKAGE_EXT, "make-thread-lock", "ThreadLock", true);
        autoload(PACKAGE_EXT, "probe-directory", "probe_file", true);
        autoload(PACKAGE_EXT, "release-mutex", "Mutex", true);
        autoload(PACKAGE_EXT, "set-floating-point-modes", "FloatFunctions", true);
        autoload(PACKAGE_EXT, "simple-string-fill", "StringFunctions");
        autoload(PACKAGE_EXT, "simple-string-search", "StringFunctions");
        autoload(PACKAGE_EXT, "string-input-stream-current", "StringInputStream", true);
        autoload(PACKAGE_EXT, "string-find", "StringFunctions");
        autoload(PACKAGE_EXT, "string-position", "StringFunctions");
        autoload(PACKAGE_EXT, "thread-lock", "ThreadLock", true);
        autoload(PACKAGE_EXT, "thread-unlock", "ThreadLock", true);
        autoload(PACKAGE_JAVA, "%jnew-proxy", "JProxy");
        autoload(PACKAGE_JAVA, "%jnew-runtime-class", "RuntimeClass");
        autoload(PACKAGE_JAVA, "%jredefine-method", "RuntimeClass");
        autoload(PACKAGE_JAVA, "%jregister-handler", "JHandler");
        autoload(PACKAGE_JAVA, "%load-java-class-from-byte-array", "RuntimeClass");
        autoload(PACKAGE_MOP, "funcallable-instance-function", "StandardGenericFunction", false);
        autoload(PACKAGE_MOP, "generic-function-name", "StandardGenericFunction", true);
        autoload(PACKAGE_MOP, "method-qualifiers", "StandardMethod", true);
        autoload(PACKAGE_MOP, "method-specializers", "StandardMethod", true);
        autoload(PACKAGE_MOP, "set-funcallable-instance-function", "StandardGenericFunction", true);
        autoload(PACKAGE_PROF, "%start-profiler", "Profiler", true);
        autoload(PACKAGE_PROF, "stop-profiler", "Profiler", true);
        autoload(PACKAGE_SYS, "%%string=", "StringFunctions");
        autoload(PACKAGE_SYS, "%adjust-array", "adjust_array");
        autoload(PACKAGE_SYS, "%defpackage", "PackageFunctions");
        autoload(PACKAGE_SYS, "%finalize-generic-function", "StandardGenericFunction", true);
        autoload(PACKAGE_SYS, "%generic-function-lambda-list", "StandardGenericFunction", true);
        autoload(PACKAGE_SYS, "%generic-function-name", "StandardGenericFunction", true);
        autoload(PACKAGE_SYS, "%make-array", "make_array");
        autoload(PACKAGE_SYS, "%make-condition", "make_condition", true);
        autoload(PACKAGE_SYS, "%make-hash-table", "HashTableFunctions");
        autoload(PACKAGE_SYS, "%make-hash-table", "HashTableFunctions");
        autoload(PACKAGE_SYS, "%make-logical-pathname", "LogicalPathname", true);
        autoload(PACKAGE_SYS, "%make-server-socket", "make_server_socket");
        autoload(PACKAGE_SYS, "%make-socket", "make_socket");
        autoload(PACKAGE_SYS, "%make-string", "StringFunctions");
        autoload(PACKAGE_SYS, "%make-string-output-stream", "StringOutputStream");
        autoload(PACKAGE_SYS, "%method-fast-function", "StandardMethod", true);
        autoload(PACKAGE_SYS, "%method-function", "StandardMethod", true);
        autoload(PACKAGE_SYS, "%method-generic-function", "StandardMethod", true);
        autoload(PACKAGE_SYS, "%method-specializers", "StandardMethod", true);
        autoload(PACKAGE_SYS, "%nstring-capitalize", "StringFunctions");
        autoload(PACKAGE_SYS, "%nstring-downcase", "StringFunctions");
        autoload(PACKAGE_SYS, "%nstring-upcase", "StringFunctions");
        autoload(PACKAGE_SYS, "%run-shell-command", "ShellCommand");
        autoload(PACKAGE_SYS, "%server-socket-close", "server_socket_close");
        autoload(PACKAGE_SYS, "%set-arglist", "arglist");
        autoload(PACKAGE_SYS, "%set-class-direct-slots", "SlotClass", true);
        autoload(PACKAGE_SYS, "%set-function-info", "function_info");
        autoload(PACKAGE_SYS, "%set-generic-function-lambda-list", "StandardGenericFunction", true);
        autoload(PACKAGE_SYS, "%set-generic-function-name", "StandardGenericFunction", true);
        autoload(PACKAGE_SYS, "%set-gf-required-args", "StandardGenericFunction", true);
        autoload(PACKAGE_SYS, "%set-method-fast-function", "StandardMethod", true);
        autoload(PACKAGE_SYS, "%set-method-function", "StandardMethod", true);
        autoload(PACKAGE_SYS, "%set-method-generic-function", "StandardMethod", true);
        autoload(PACKAGE_SYS, "%set-method-specializers", "StandardMethod", true);
        autoload(PACKAGE_SYS, "%simple-bit-vector-bit-and", "SimpleBitVector");
        autoload(PACKAGE_SYS, "%simple-bit-vector-bit-andc1", "SimpleBitVector");
        autoload(PACKAGE_SYS, "%simple-bit-vector-bit-andc2", "SimpleBitVector");
        autoload(PACKAGE_SYS, "%simple-bit-vector-bit-eqv", "SimpleBitVector");
        autoload(PACKAGE_SYS, "%simple-bit-vector-bit-ior", "SimpleBitVector");
        autoload(PACKAGE_SYS, "%simple-bit-vector-bit-nand", "SimpleBitVector");
        autoload(PACKAGE_SYS, "%simple-bit-vector-bit-nor", "SimpleBitVector");
        autoload(PACKAGE_SYS, "%simple-bit-vector-bit-not", "SimpleBitVector");
        autoload(PACKAGE_SYS, "%simple-bit-vector-bit-orc1", "SimpleBitVector");
        autoload(PACKAGE_SYS, "%simple-bit-vector-bit-orc2", "SimpleBitVector");
        autoload(PACKAGE_SYS, "%simple-bit-vector-bit-xor", "SimpleBitVector");
        autoload(PACKAGE_SYS, "%slot-definition-allocation", "SlotDefinition", true);
        autoload(PACKAGE_SYS, "%slot-definition-allocation-class", "SlotDefinition", true);
        autoload(PACKAGE_SYS, "%slot-definition-initargs", "SlotDefinition", true);
        autoload(PACKAGE_SYS, "%slot-definition-initform", "SlotDefinition", true);
        autoload(PACKAGE_SYS, "%slot-definition-initfunction", "SlotDefinition", true);
        autoload(PACKAGE_SYS, "%slot-definition-location", "SlotDefinition", true);
        autoload(PACKAGE_SYS, "%slot-definition-name", "SlotDefinition", true);
        autoload(PACKAGE_SYS, "%slot-definition-readers", "SlotDefinition", true);
        autoload(PACKAGE_SYS, "%slot-definition-writers", "SlotDefinition", true);
        autoload(PACKAGE_SYS, "%socket-accept", "socket_accept");
        autoload(PACKAGE_SYS, "%socket-close", "socket_close");
        autoload(PACKAGE_SYS, "%socket-stream", "socket_stream");
        autoload(PACKAGE_SYS, "%string-capitalize", "StringFunctions");
        autoload(PACKAGE_SYS, "%string-downcase", "StringFunctions");
        autoload(PACKAGE_SYS, "%string-equal", "StringFunctions");
        autoload(PACKAGE_SYS, "%string-greaterp", "StringFunctions");
        autoload(PACKAGE_SYS, "%string-lessp", "StringFunctions");
        autoload(PACKAGE_SYS, "%string-not-equal", "StringFunctions");
        autoload(PACKAGE_SYS, "%string-not-greaterp", "StringFunctions");
        autoload(PACKAGE_SYS, "%string-not-lessp", "StringFunctions");
        autoload(PACKAGE_SYS, "%string-upcase", "StringFunctions");
        autoload(PACKAGE_SYS, "%string/=", "StringFunctions");
        autoload(PACKAGE_SYS, "%string<", "StringFunctions");
        autoload(PACKAGE_SYS, "%string<=", "StringFunctions");
        autoload(PACKAGE_SYS, "%string=", "StringFunctions");
        autoload(PACKAGE_SYS, "%string>", "StringFunctions");
        autoload(PACKAGE_SYS, "%string>=", "StringFunctions");
        autoload(PACKAGE_SYS, "%time", "Time");
        autoload(PACKAGE_SYS, "cache-emf", "StandardGenericFunction", true);
        autoload(PACKAGE_SYS, "cache-slot-location", "StandardGenericFunction", true);
        autoload(PACKAGE_SYS, "canonicalize-logical-host", "LogicalPathname", true);
        autoload(PACKAGE_SYS, "class-direct-slots", "SlotClass");
        autoload(PACKAGE_SYS, "coerce-to-double-float", "FloatFunctions");
        autoload(PACKAGE_SYS, "coerce-to-single-float", "FloatFunctions");
        autoload(PACKAGE_SYS, "compute-class-direct-slots", "SlotClass", true);
        autoload(PACKAGE_SYS, "create-new-file", "create_new_file");
        autoload(PACKAGE_SYS, "default-time-zone", "Time");
        autoload(PACKAGE_SYS, "disassemble-class-bytes", "disassemble_class_bytes", true);
        autoload(PACKAGE_SYS, "double-float-high-bits", "FloatFunctions", true);
        autoload(PACKAGE_SYS, "double-float-low-bits", "FloatFunctions", true);
        autoload(PACKAGE_SYS, "float-infinity-p", "FloatFunctions", true);
        autoload(PACKAGE_SYS, "float-nan-p", "FloatFunctions", true);
        autoload(PACKAGE_SYS, "float-string", "FloatFunctions", true);
        autoload(PACKAGE_SYS, "function-info", "function_info");
        autoload(PACKAGE_SYS, "generic-function-argument-precedence-order","StandardGenericFunction", true);
        autoload(PACKAGE_SYS, "generic-function-classes-to-emf-table","StandardGenericFunction", true);
        autoload(PACKAGE_SYS, "generic-function-documentation","StandardGenericFunction", true);
        autoload(PACKAGE_SYS, "generic-function-initial-methods","StandardGenericFunction", true);
        autoload(PACKAGE_SYS, "generic-function-method-class","StandardGenericFunction", true);
        autoload(PACKAGE_SYS, "generic-function-method-combination","StandardGenericFunction", true);
        autoload(PACKAGE_SYS, "generic-function-methods","StandardGenericFunction", true);
        autoload(PACKAGE_SYS, "get-cached-emf", "StandardGenericFunction", true);
        autoload(PACKAGE_SYS, "get-cached-slot-location", "StandardGenericFunction", true);
        autoload(PACKAGE_SYS, "get-function-info-value", "function_info");
        autoload(PACKAGE_SYS, "gf-required-args", "StandardGenericFunction", true);
        autoload(PACKAGE_SYS, "hash-table-entries", "HashTableFunctions");
        autoload(PACKAGE_SYS, "hash-table-entries", "HashTableFunctions");
        autoload(PACKAGE_SYS, "layout-class", "Layout", true);
        autoload(PACKAGE_SYS, "layout-length", "Layout", true);
        autoload(PACKAGE_SYS, "layout-slot-index", "Layout", true);
        autoload(PACKAGE_SYS, "layout-slot-location", "Layout", true);
        autoload(PACKAGE_SYS, "make-case-frob-stream", "CaseFrobStream");
        autoload(PACKAGE_SYS, "make-double-float", "FloatFunctions", true);
        autoload(PACKAGE_SYS, "make-file-stream", "FileStream");
        autoload(PACKAGE_SYS, "make-fill-pointer-output-stream", "FillPointerOutputStream");
        autoload(PACKAGE_SYS, "make-forward-referenced-class", "ForwardReferencedClass", true);
        autoload(PACKAGE_SYS, "make-layout", "Layout", true);
        autoload(PACKAGE_SYS, "make-single-float", "FloatFunctions", true);
        autoload(PACKAGE_SYS, "make-slot-definition", "SlotDefinition", true);
        autoload(PACKAGE_SYS, "make-structure-class", "StructureClass");
        autoload(PACKAGE_SYS, "make-symbol-macro", "SymbolMacro");
        autoload(PACKAGE_SYS, "method-documentation", "StandardMethod", true);
        autoload(PACKAGE_SYS, "method-lambda-list", "StandardMethod", true);
        autoload(PACKAGE_SYS, "psxhash", "HashTableFunctions");
        autoload(PACKAGE_SYS, "puthash", "HashTableFunctions");
        autoload(PACKAGE_SYS, "puthash", "HashTableFunctions");
        autoload(PACKAGE_SYS, "set-function-info-value", "function_info");
        autoload(PACKAGE_SYS, "set-generic-function-argument-precedence-order","StandardGenericFunction", true);
        autoload(PACKAGE_SYS, "set-generic-function-classes-to-emf-table","StandardGenericFunction", true);
        autoload(PACKAGE_SYS, "set-generic-function-documentation","StandardGenericFunction", true);
        autoload(PACKAGE_SYS, "set-generic-function-initial-methods","StandardGenericFunction", true);
        autoload(PACKAGE_SYS, "set-generic-function-method-class","StandardGenericFunction", true);
        autoload(PACKAGE_SYS, "set-generic-function-method-combination","StandardGenericFunction", true);
        autoload(PACKAGE_SYS, "set-generic-function-methods","StandardGenericFunction", true);
        autoload(PACKAGE_SYS, "set-method-documentation", "StandardMethod", true);
        autoload(PACKAGE_SYS, "set-method-lambda-list", "StandardMethod", true);
        autoload(PACKAGE_SYS, "set-method-qualifiers", "StandardMethod", true);
        autoload(PACKAGE_SYS, "set-slot-definition-allocation", "SlotDefinition", true);
        autoload(PACKAGE_SYS, "set-slot-definition-allocation-class", "SlotDefinition", true);
        autoload(PACKAGE_SYS, "set-slot-definition-initargs", "SlotDefinition", true);
        autoload(PACKAGE_SYS, "set-slot-definition-initform", "SlotDefinition", true);
        autoload(PACKAGE_SYS, "set-slot-definition-initfunction", "SlotDefinition", true);
        autoload(PACKAGE_SYS, "set-slot-definition-location", "SlotDefinition", true);
        autoload(PACKAGE_SYS, "set-slot-definition-name", "SlotDefinition", true);
        autoload(PACKAGE_SYS, "set-slot-definition-readers", "SlotDefinition", true);
        autoload(PACKAGE_SYS, "set-slot-definition-writers", "SlotDefinition", true);
        autoload(PACKAGE_SYS, "simple-list-remove-duplicates", "simple_list_remove_duplicates");
        autoload(PACKAGE_SYS, "single-float-bits", "FloatFunctions", true);
        autoload(PACKAGE_SYS, "std-allocate-instance", "StandardObjectFunctions", true);
        autoload(PACKAGE_SYS, "zip", "zip", true);

        autoload(Symbol.COPY_LIST, "copy_list");

        autoload(Symbol.SET_CHAR, "StringFunctions");
        autoload(Symbol.SET_SCHAR, "StringFunctions");

        autoload(Symbol.SET_CLASS_SLOTS, "SlotClass");
        autoload(Symbol._CLASS_SLOTS, "SlotClass");

        autoload(Symbol.JAVA_EXCEPTION_CAUSE, "JavaException");
        autoload(Symbol.JCLASS_NAME, "jclass_name");
        autoload(Symbol.JCLASS_OF, "jclass_of");
        autoload(Symbol.JMETHOD_RETURN_TYPE, "jmethod_return_type");
    }
}
