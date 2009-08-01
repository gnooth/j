;;; precompiler.lisp
;;;
;;; Copyright (C) 2003-2008 Peter Graves <peter@armedbear.org>
;;; $Id: precompiler.lisp,v 1.163 2008/02/20 18:31:15 piso Exp $
;;;
;;; This program is free software; you can redistribute it and/or
;;; modify it under the terms of the GNU General Public License
;;; as published by the Free Software Foundation; either version 2
;;; of the License, or (at your option) any later version.
;;;
;;; This program is distributed in the hope that it will be useful,
;;; but WITHOUT ANY WARRANTY; without even the implied warranty of
;;; MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
;;; GNU General Public License for more details.
;;;
;;; You should have received a copy of the GNU General Public License
;;; along with this program; if not, write to the Free Software
;;; Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.

(in-package "SYSTEM")

(export '(*inline-declarations*
          process-optimization-declarations
          process-special-declarations
          inline-p notinline-p inline-expansion expand-inline
          *defined-functions* *undefined-functions* note-name-defined))

(defvar *inline-declarations* nil)

(declaim (ftype (function (t) t) process-optimization-declarations))
(defun process-optimization-declarations (forms)
  (dolist (form forms)
    (unless (and (consp form) (eq (%car form) 'DECLARE))
      (return))
    (dolist (decl (%cdr form))
      (case (car decl)
        (OPTIMIZE
         (dolist (spec (%cdr decl))
           (let ((val 3)
                 (quality spec))
             (when (consp spec)
               (setf quality (%car spec)
                     val (cadr spec)))
             (when (and (fixnump val)
                        (<= 0 val 3))
               (case quality
                 (speed
                  (setf *speed* val))
                 (safety
                  (setf *safety* val))
                 (debug
                  (setf *debug* val))
                 (space
                  (setf *space* val))
                 (compilation-speed) ;; Ignored.
                 (t
                  (compiler-warn "Ignoring unknown optimization quality ~S in ~S." quality decl)))))))
        ((INLINE NOTINLINE)
         (dolist (symbol (%cdr decl))
           (push (cons symbol (%car decl)) *inline-declarations*)))
        (:explain
         (dolist (spec (%cdr decl))
           (let ((val t)
                 (quality spec))
             (when (consp spec)
               (setf quality (%car spec))
               (when (= (length spec) 2)
                 (setf val (%cadr spec))))
             (if val
                 (pushnew quality *explain*)
                 (setf *explain* (remove quality *explain*)))))))))
  t)

;; Returns list of declared specials.
(declaim (ftype (function (list) list) process-special-declarations))
(defun process-special-declarations (forms)
  (let ((specials nil))
    (dolist (form forms)
      (unless (and (consp form) (eq (%car form) 'DECLARE))
        (return))
      (let ((decls (%cdr form)))
        (dolist (decl decls)
          (when (eq (car decl) 'special)
            (setq specials (append (cdr decl) specials))))))
    specials))

(declaim (ftype (function (t) t) inline-p))
(defun inline-p (name)
  (declare (optimize speed))
  (let ((entry (assoc name *inline-declarations*)))
    (if entry
        (eq (cdr entry) 'INLINE)
        (and (symbolp name) (eq (get name '%inline) 'INLINE)))))

(declaim (ftype (function (t) t) notinline-p))
(defun notinline-p (name)
  (declare (optimize speed))
  (let ((entry (assoc name *inline-declarations*)))
    (if entry
        (eq (cdr entry) 'NOTINLINE)
        (and (symbolp name) (eq (get name '%inline) 'NOTINLINE)))))

(defun expand-inline (form expansion)
;;   (format t "expand-inline form = ~S~%" form)
;;   (format t "expand-inline expansion = ~S~%" expansion)
  (let* ((op (car form))
         (proclaimed-ftype (proclaimed-ftype op))
         (args (cdr form))
         (vars (cadr expansion))
         (varlist ())
         new-form)
;;     (format t "op = ~S proclaimed-ftype = ~S~%" op (proclaimed-ftype op))
    (do ((vars vars (cdr vars))
         (args args (cdr args)))
        ((null vars))
      (push (list (car vars) (car args)) varlist))
    (setf new-form (list* 'LET (nreverse varlist)
                          (copy-tree (cddr expansion))))
    (when proclaimed-ftype
      (let ((result-type (ftype-result-type proclaimed-ftype)))
        (when (and result-type
                   (neq result-type t)
                   (neq result-type '*))
          (setf new-form (list 'TRULY-THE result-type new-form)))))
;;     (format t "expand-inline new form = ~S~%" new-form)
    new-form))

(define-compiler-macro assoc (&whole form &rest args)
  (cond ((and (= (length args) 4)
              (eq (third args) :test)
              (or (equal (fourth args) '(quote eq))
                  (equal (fourth args) '(function eq))))
         `(assq ,(first args) ,(second args)))
        ((= (length args) 2)
         `(assql ,(first args) ,(second args)))
        (t form)))

(define-compiler-macro member (&whole form &rest args)
  (let ((arg1 (first args))
        (arg2 (second args)))
    (case (length args)
      (2
       `(memql ,arg1 ,arg2))
      (4
       (let ((arg3 (third args))
             (arg4 (fourth args)))
         (cond ((and (eq arg3 :test)
                     (or (equal arg4 '(quote eq))
                         (equal arg4 '(function eq))))
                `(memq ,arg1 ,arg2))
               ((and (eq arg3 :test)
                     (or (equal arg4 '(quote eql))
                         (equal arg4 '(function eql))
                         (equal arg4 '(quote char=))
                         (equal arg4 '(function char=))))
                `(memql ,arg1 ,arg2))
               (t
                form))))
      (t
       form))))

(define-compiler-macro search (&whole form &rest args)
  (if (= (length args) 2)
      `(simple-search ,@args)
      form))

(define-compiler-macro identity (&whole form &rest args)
  (if (= (length args) 1)
      `(progn ,(car args))
      form))

(defun quoted-form-p (form)
  (and (consp form) (eq (%car form) 'QUOTE) (= (length form) 2)))

(define-compiler-macro eql (&whole form &rest args)
  (let ((first (car args))
        (second (cadr args)))
    (if (or (and (quoted-form-p first) (symbolp (cadr first)))
            (and (quoted-form-p second) (symbolp (cadr second))))
        `(eq ,first ,second)
        form)))

(define-compiler-macro not (&whole form arg)
  (if (atom arg)
      form
      (let ((op (case (car arg)
                  (>= '<)
                  (<  '>=)
                  (<= '>)
                  (>  '<=)
                  (t  nil))))
        (if (and op (= (length arg) 3))
            (cons op (cdr arg))
            form))))

(defun predicate-for-type (type)
  (cdr (assq type '((ARRAY             . arrayp)
                    (ATOM              . atom)
                    (BIT-VECTOR        . bit-vector-p)
                    (CHARACTER         . characterp)
                    (COMPLEX           . complexp)
                    (CONS              . consp)
                    (FIXNUM            . fixnump)
                    (FLOAT             . floatp)
                    (FUNCTION          . functionp)
                    (HASH-TABLE        . hash-table-p)
                    (INTEGER           . integerp)
                    (LIST              . listp)
                    (NULL              . null)
                    (NUMBER            . numberp)
                    (NUMBER            . numberp)
                    (PACKAGE           . packagep)
                    (RATIONAL          . rationalp)
                    (REAL              . realp)
                    (SIMPLE-BIT-VECTOR . simple-bit-vector-p)
                    (SIMPLE-STRING     . simple-string-p)
                    (SIMPLE-VECTOR     . simple-vector-p)
                    (STREAM            . streamp)
                    (STRING            . stringp)
                    (SYMBOL            . symbolp)))))

(define-compiler-macro typep (&whole form &rest args)
  (if (= (length args) 2) ; no environment arg
      (let* ((object (%car args))
             (type-specifier (%cadr args))
             (type (and (consp type-specifier)
                        (eq (%car type-specifier) 'QUOTE)
                        (%cadr type-specifier)))
             (predicate (and type (predicate-for-type type))))
        (if predicate
            `(,predicate ,object)
            `(%typep ,@args)))
      form))

(define-compiler-macro subtypep (&whole form &rest args)
  (if (= (length args) 2)
      `(%subtypep ,@args)
      form))

(define-compiler-macro funcall (&whole form &rest args)
  (let ((callee (car args)))
    (if (and (>= *speed* *debug*)
             (consp callee)
             (eq (%car callee) 'function)
             (symbolp (cadr callee))
             (not (special-operator-p (cadr callee)))
             (not (macro-function (cadr callee) sys:*compile-file-environment*))
             (memq (symbol-package (cadr callee))
                   (list (find-package "CL") (find-package "SYS"))))
        `(,(cadr callee) ,@(cdr args))
        form)))

(define-compiler-macro byte (size position)
  `(cons ,size ,position))

(define-compiler-macro byte-size (bytespec)
  `(car ,bytespec))

(define-compiler-macro byte-position (bytespec)
  `(cdr ,bytespec))

(define-source-transform concatenate (&whole form result-type &rest sequences)
  (if (equal result-type '(quote STRING))
      `(sys::concatenate-to-string (list ,@sequences))
      form))

(define-source-transform ldb (&whole form bytespec integer)
  (if (and (consp bytespec)
           (eq (%car bytespec) 'byte)
           (= (length bytespec) 3))
      (let ((size (%cadr bytespec))
            (position (%caddr bytespec)))
        `(%ldb ,size ,position ,integer))
      form))

(define-source-transform find (&whole form item sequence &key from-end test test-not start end key)
  (cond ((and (>= (length form) 3) (null start) (null end))
         (cond ((and (stringp sequence)
                     (null from-end)
                     (member test '(#'eql #'char=) :test #'equal)
                     (null test-not)
                     (null key))
                `(string-find ,item ,sequence))
               (t
                (let ((item-var (gensym))
                      (seq-var (gensym)))
                  `(let ((,item-var ,item)
                         (,seq-var ,sequence))
                     (if (listp ,seq-var)
                         (list-find* ,item-var ,seq-var ,from-end ,test ,test-not 0 (length ,seq-var) ,key)
                         (vector-find* ,item-var ,seq-var ,from-end ,test ,test-not 0 (length ,seq-var) ,key)))))))
        (t
         form)))

(define-source-transform adjoin (&whole form &rest args)
  (if (= (length args) 2)
      `(adjoin-eql ,(first args) ,(second args))
      form))

(define-compiler-macro catch (&whole form tag &rest args)
  (declare (ignore tag))
  (if (and (null (cdr args))
           (constantp (car args)))
      (car args)
      form))

(define-compiler-macro string= (&whole form &rest args)
  (if (= (length args) 2)
      `(sys::%%string= ,@args)
      form))

(define-compiler-macro <= (&whole form &rest args)
  (cond ((and (= (length args) 3)
              (numberp (first args))
              (numberp (third args))
              (= (first args) (third args)))
         `(= ,(second args) ,(first args)))
        (t
         form)))

(in-package "EXTENSIONS")

(export '(precompile-form precompile))

(unless (find-package "PRECOMPILER")
  (make-package "PRECOMPILER"
                :nicknames '("PRE")
                :use '("COMMON-LISP" "EXTENSIONS" "SYSTEM")))

(in-package "PRECOMPILER")

(defvar *in-jvm-compile* nil)

(defvar *local-variables* nil)

(declaim (ftype (function (t) t) find-varspec))
(defun find-varspec (sym)
  (dolist (varspec *local-variables*)
    (when (eq sym (car varspec))
      (return varspec))))

(declaim (ftype (function (t) t) precompile1))
(defun precompile1 (form)
  (cond ((symbolp form)
         (let ((varspec (find-varspec form)))
           (cond ((and varspec (eq (second varspec) :symbol-macro))
                  (precompile1 (copy-tree (third varspec))))
                 ((null varspec)
                  (let ((expansion (expand-macro form)))
                    (if (eq expansion form)
                        form
                        (precompile1 expansion))))
                 (t
                  form))))
        ((atom form)
         form)
        (t
         (let ((op (%car form))
               handler)
           (when (symbolp op)
             (cond ((setf handler (get op 'precompile-handler))
                    (return-from precompile1 (funcall handler form)))
                   ((local-macro-function op)
                    (let ((result (expand-local-macro (precompile-cons form))))
                      (return-from precompile1 (if (equal result form)
                                                   result
                                                   (precompile1 result)))))
                   ((macro-function op *compile-file-environment*)
                    (return-from precompile1 (precompile1 (expand-macro form))))
                   ((special-operator-p op)
                    (error "PRECOMPILE1: unsupported special operator ~S." op))))
           (precompile-function-call form)))))

(defun precompile-identity (form)
  (declare (optimize speed))
  form)

(declaim (ftype (function (t) cons) precompile-cons))
(defun precompile-cons (form)
  (cons (car form) (mapcar #'precompile1 (cdr form))))

(declaim (ftype (function (t t) t) precompile-function-call))
(defun precompile-function-call (form)
  (let ((op (car form)))
    (when (and (consp op) (eq (%car op) 'LAMBDA))
      (return-from precompile-function-call
                   (cons (precompile-lambda op)
                         (mapcar #'precompile1 (cdr form)))))
    (when (or (not *in-jvm-compile*) (notinline-p op))
      (return-from precompile-function-call (precompile-cons form)))
    (when (source-transform op)
      (let ((new-form (expand-source-transform form)))
        (when (neq new-form form)
          (return-from precompile-function-call (precompile1 new-form)))))
    (when *enable-inline-expansion*
      (let ((expansion (inline-expansion op)))
        (when expansion
          (let ((explain *explain*))
            (when (and explain (memq :calls explain))
              (format t ";   inlining call to ~S~%" op)))
          (return-from precompile-function-call (precompile1 (expand-inline form expansion))))))
    (cons op (mapcar #'precompile1 (cdr form)))))

(defun precompile-locally (form)
  (let ((*inline-declarations* *inline-declarations*))
    (process-optimization-declarations (cdr form))
  (cons 'LOCALLY (mapcar #'precompile1 (cdr form)))))

(defun precompile-block (form)
  (let ((args (cdr form)))
    (if (null (cdr args))
        nil
        (list* 'BLOCK (car args) (mapcar #'precompile1 (cdr args))))))

(defun precompile-dolist (form)
  (if *in-jvm-compile*
      (precompile1 (macroexpand form))
      (cons 'DOLIST (cons (mapcar #'precompile1 (cadr form))
                          (mapcar #'precompile1 (cddr form))))))

(defun precompile-dotimes (form)
  (if *in-jvm-compile*
      (precompile1 (macroexpand form))
      (cons 'DOTIMES (cons (mapcar #'precompile1 (cadr form))
                           (mapcar #'precompile1 (cddr form))))))

(defun precompile-do/do*-vars (varlist)
  (let ((result nil))
    (dolist (varspec varlist)
      (if (atom varspec)
          (push varspec result)
          (case (length varspec)
            (1
             (push (%car varspec) result))
            (2
             (let* ((var (%car varspec))
                    (init-form (%cadr varspec)))
               (unless (symbolp var)
                 (error 'type-error))
               (push (list var (precompile1 init-form))
                     result)))
            (3
             (let* ((var (%car varspec))
                    (init-form (%cadr varspec))
                    (step-form (%caddr varspec)))
               (unless (symbolp var)
                 (error 'type-error))
               (push (list var (precompile1 init-form) (precompile1 step-form))
                     result))))))
    (nreverse result)))

(defun precompile-do/do*-end-form (end-form)
  (let ((end-test-form (car end-form))
        (result-forms (cdr end-form)))
    (list* end-test-form (mapcar #'precompile1 result-forms))))

(defun precompile-do/do* (form)
  (if *in-jvm-compile*
      (precompile1 (macroexpand form))
      (list* (car form)
             (precompile-do/do*-vars (cadr form))
             (precompile-do/do*-end-form (caddr form))
             (mapcar #'precompile1 (cdddr form)))))

(defun precompile-do-symbols (form)
  (list* (car form) (cadr form) (mapcar #'precompile1 (cddr form))))

(defun precompile-load-time-value (form)
  form)

(defun precompile-progn (form)
  (let ((body (cdr form)))
    (if (eql (length body) 1)
        (let ((res (precompile1 (%car body))))
          ;; If the result turns out to be a bare symbol, leave it wrapped
          ;; with PROGN so it won't be mistaken for a tag in an enclosing
          ;; TAGBODY.
          (if (symbolp res)
              (list 'progn res)
              res))
        (cons 'PROGN (mapcar #'precompile1 body)))))

(defun precompile-progv (form)
  (if (< (length form) 3)
      (compiler-error "Not enough arguments for ~S." 'progv)
      (list* 'PROGV (mapcar #'precompile1 (%cdr form)))))

(defun precompile-setf (form)
  (let ((place (second form)))
    (cond ((and (consp place)
                (local-macro-function (%car place)))
           (let ((expansion (expand-local-macro place)))
             (precompile1 (list* 'SETF expansion (cddr form)))))
          ((and (consp place)
                (eq (%car place) 'VALUES))
           (setf form
                 (list* 'SETF
                        (list* 'VALUES
                               (mapcar #'precompile1 (%cdr place)))
                        (cddr form)))
           (precompile1 (expand-macro form)))
          ((symbolp place)
           (let ((varspec (find-varspec place)))
             (if (and varspec (eq (second varspec) :symbol-macro))
                 (precompile1 (list* 'SETF (copy-tree (third varspec)) (cddr form)))
                 (precompile1 (expand-macro form)))))
          (t
           (precompile1 (expand-macro form))))))

(defun precompile-setq (form)
  (let* ((args (cdr form))
         (len (length args)))
    (when (oddp len)
      (error 'simple-program-error
             :format-control "Odd number of arguments to SETQ."))
    (if (= len 2)
        (let* ((sym (%car args))
               (val (%cadr args))
               (varspec (find-varspec sym)))
          (if (and varspec (eq (second varspec) :symbol-macro))
              (precompile1 (list 'SETF (copy-tree (third varspec)) val))
              (list 'SETQ sym (precompile1 val))))
        (let ((result ()))
          (loop
            (when (null args)
              (return))
            (push (precompile-setq (list 'SETQ (car args) (cadr args))) result)
            (setq args (cddr args)))
          (setq result (nreverse result))
          (push 'PROGN result)
          result))))

(defun precompile-psetf (form)
  (setf form
        (list* 'PSETF
               (mapcar #'precompile1 (cdr form))))
  (precompile1 (expand-macro form)))

(defun precompile-psetq (form)
  ;; Make sure all the vars are symbols.
  (do* ((rest (cdr form) (cddr rest))
        (var (car rest)))
       ((null rest))
    (unless (symbolp var)
      (error 'simple-error
             :format-control "~S is not a symbol."
             :format-arguments (list var))))
  ;; Delegate to PRECOMPILE-PSETF so symbol macros are handled correctly.
  (precompile-psetf form))

(defun rewrite-aux-vars-process-decls (forms arg-vars aux-vars)
  (declare (ignore aux-vars))
  (let ((lambda-decls nil)
        (let-decls nil))
    (dolist (form forms)
      (unless (and (consp form) (eq (car form) 'DECLARE)) ; shouldn't happen
        (return))
      (dolist (decl (cdr form))
        (case (car decl)
          ((OPTIMIZE DECLARATION DYNAMIC-EXTENT FTYPE INLINE NOTINLINE)
           (push (list 'DECLARE decl) lambda-decls))
          (SPECIAL
           (dolist (name (cdr decl))
             (if (memq name arg-vars)
                 (push (list 'DECLARE (list 'SPECIAL name)) lambda-decls)
                 (push (list 'DECLARE (list 'SPECIAL name)) let-decls))))
          (TYPE
           (dolist (name (cddr decl))
             (if (memq name arg-vars)
                 (push (list 'DECLARE (list 'TYPE (cadr decl) name)) lambda-decls)
                 (push (list 'DECLARE (list 'TYPE (cadr decl) name)) let-decls))))
          (t
           (dolist (name (cdr decl))
             (if (memq name arg-vars)
                 (push (list 'DECLARE (list (car decl) name)) lambda-decls)
                 (push (list 'DECLARE (list (car decl) name)) let-decls)))))))
    (setq lambda-decls (nreverse lambda-decls))
    (setq let-decls (nreverse let-decls))
    (values lambda-decls let-decls)))

(defun rewrite-aux-vars (form)
  (multiple-value-bind (body decls doc)
      (parse-body (cddr form))
    (declare (ignore doc)) ; FIXME
    (let* ((lambda-list (cadr form))
           (lets (cdr (memq '&AUX lambda-list)))
           aux-vars)
      (dolist (form lets)
        (cond ((consp form)
               (push (%car form) aux-vars))
              (t
               (push form aux-vars))))
      (setq aux-vars (nreverse aux-vars))
      (setq lambda-list (subseq lambda-list 0 (position '&AUX lambda-list)))
      (multiple-value-bind (lambda-decls let-decls)
          (rewrite-aux-vars-process-decls decls (lambda-list-names lambda-list) aux-vars)
        `(lambda ,lambda-list ,@lambda-decls (let* ,lets ,@let-decls ,@body))))))

(defun maybe-rewrite-lambda (form)
  (let* ((lambda-list (cadr form)))
    (when (memq '&AUX lambda-list)
      (setq form (rewrite-aux-vars form))
      (setq lambda-list (cadr form)))
    (multiple-value-bind (body decls doc)
        (parse-body (cddr form))
      (let* ((declared-specials (process-special-declarations decls))
             (specials nil))
        ;; Scan for specials.
        (let ((keyp nil))
          (dolist (var lambda-list)
            (cond ((eq var '&KEY)
                   (setq keyp t))
                  ((atom var)
                   (when (or (special-variable-p var) (memq var declared-specials))
                     (push var specials)))
                  ((not keyp) ;; e.g. "&optional (*x* 42)"
                   (setq var (%car var))
                   (when (or (special-variable-p var) (memq var declared-specials))
                     (push var specials)))
                  ;; Keyword parameters.
                  ((atom (%car var)) ;; e.g. "&key (a 42)"
                   ;; Not special.
                   )
                  (t
                   ;; e.g. "&key ((:x *x*) 42)"
                   (setq var (second (%car var))) ;; *x*
                   (when (or (special-variable-p var) (memq var declared-specials))
                     (push var specials))))))
        (when specials
          ;; For each special...
          (dolist (special specials)
            (let ((sym (gensym)))
              (let ((res nil)
                    (keyp nil))
                ;; Walk through the lambda list and replace each occurrence.
                (dolist (var lambda-list)
                  (cond ((eq var '&KEY)
                         (setq keyp t)
                         (push var res))
                        ((atom var)
                         (when (eq var special)
                           (setq var sym))
                         (push var res))
                        ((not keyp) ;; e.g. "&optional (*x* 42)"
                         (when (eq (%car var) special)
                           (setf (car var) sym))
                         (push var res))
                        ((atom (%car var)) ;; e.g. "&key (a 42)"
                         (push var res))
                        (t
                         ;; e.g. "&key ((:x *x*) 42)"
                         (when (eq (second (%car var)) special)
                           (setf (second (%car var)) sym))
                         (push var res))))
                (setq lambda-list (nreverse res)))
              (setq body (list (append (list 'LET* (list (list special sym))) body))))))
        `(lambda ,lambda-list ,@decls ,@(when doc `(,doc)) ,@body)))))

(defun precompile-lambda (form)
  (setq form (maybe-rewrite-lambda form))
  (let ((body (cddr form))
        (*inline-declarations* *inline-declarations*))
    (process-optimization-declarations body)
    (list* 'LAMBDA (cadr form) (mapcar #'precompile1 body))))

(defun precompile-named-lambda (form)
  (let ((lambda-form (list* 'LAMBDA (caddr form) (cdddr form))))
    (setf lambda-form (maybe-rewrite-lambda lambda-form))
    (let ((body (cddr lambda-form))
          (*inline-declarations* *inline-declarations*))
      (process-optimization-declarations body)
      (list* 'NAMED-LAMBDA (cadr form) (cadr lambda-form)
             (mapcar #'precompile1 body)))))

(defun precompile-defun (form)
  (if *in-jvm-compile*
      (precompile1 (expand-macro form))
      form))

(defun define-local-macro (name lambda-list body)
  (let* ((form (gensym))
         (env (gensym))
         (body (sys::parse-defmacro lambda-list form body name 'macrolet
                                    :environment env))
         (expander `(lambda (,form ,env) (block ,name ,body)))
         (compiled-expander (sys::%compile nil expander)))
    (coerce-to-function (or compiled-expander expander))))

(defvar *local-functions-and-macros* ())

(defun local-macro-function (name)
  (getf *local-functions-and-macros* name))

(defun expand-local-macro (form)
  (let ((expansion (funcall (local-macro-function (car form)) form nil)))
    ;; If the expansion turns out to be a bare symbol, wrap it with PROGN so it
    ;; won't be mistaken for a tag in an enclosing TAGBODY.
    (if (symbolp expansion)
        (list 'PROGN expansion)
        expansion)))

(defun precompile-macrolet (form)
  (let ((*local-functions-and-macros* *local-functions-and-macros*)
        (macros (cadr form)))
    (dolist (macro macros)
      (let ((name (car macro))
            (lambda-list (cadr macro))
            (forms (cddr macro)))
        (push (define-local-macro name lambda-list forms) *local-functions-and-macros*)
        (push name *local-functions-and-macros*)))
    (multiple-value-bind (body decls)
        (parse-body (cddr form) nil)
      `(locally ,@decls ,@(mapcar #'precompile1 body)))))

;; "If the restartable-form is a list whose car is any of the symbols SIGNAL,
;; ERROR, CERROR, or WARN (or is a macro form which macroexpands into such a
;; list), then WITH-CONDITION-RESTARTS is used implicitly to associate the
;; indicated restarts with the condition to be signaled." So we need to
;; precompile the restartable form before macroexpanding RESTART-CASE.
(defun precompile-restart-case (form)
  (let ((new-form (list* 'RESTART-CASE (precompile1 (cadr form)) (cddr form))))
    (precompile1 (macroexpand new-form sys:*compile-file-environment*))))

(defun precompile-symbol-macrolet (form)
  (let ((*local-variables* *local-variables*)
        (defs (cadr form)))
    (dolist (def defs)
      (let ((sym (car def))
            (expansion (cadr def)))
        (when (special-variable-p sym)
          (error 'program-error
                 :format-control "Attempt to bind the special variable ~S with SYMBOL-MACROLET."
                 :format-arguments (list sym)))
        (push (list sym :symbol-macro expansion) *local-variables*)))
    (multiple-value-bind (body decls)
        (parse-body (cddr form) nil)
      (when decls
        (let ((specials ()))
          (dolist (decl decls)
            (when (eq (car decl) 'DECLARE)
              (dolist (declspec (cdr decl))
                (when (eq (car declspec) 'SPECIAL)
                  (setf specials (append specials (cdr declspec)))))))
          (when specials
            (let ((syms (mapcar #'car (cadr form))))
              (dolist (special specials)
                (when (memq special syms)
                  (error 'program-error
                         :format-control "~S is a symbol-macro and may not be declared special."
                         :format-arguments (list special))))))))
      `(locally ,@decls ,@(mapcar #'precompile1 body)))))

(defun precompile-the (form)
  (list 'THE
        (second form)
        (precompile1 (third form))))

(defun precompile-truly-the (form)
  (list 'TRULY-THE
        (second form)
        (precompile1 (third form))))

(defun precompile-let/let*-vars (vars)
  (let ((result nil))
    (dolist (var vars)
      (cond ((consp var)
;;              (when (> (length var) 2)
;;                (error 'program-error
;;                       :format-control "The LET/LET* binding specification ~S is invalid."
;;                       :format-arguments (list var)))
             (let ((v (%car var))
                   (expr (cadr var)))
               (unless (symbolp v)
                 (error 'simple-type-error
                        :format-control "The variable ~S is not a symbol."
                        :format-arguments (list v)))
               (push (list v (precompile1 expr)) result)
               (push (list v :variable) *local-variables*)))
            (t
             (push var result)
             (push (list var :variable) *local-variables*))))
    (nreverse result)))

(defun precompile-let (form)
  (let ((*local-variables* *local-variables*))
    (list* 'LET
           (precompile-let/let*-vars (cadr form))
           (mapcar #'precompile1 (cddr form)))))

;; (LET* ((X 1)) (LET* ((Y 2)) (LET* ((Z 3)) (+ X Y Z)))) =>
;; (LET* ((X 1) (Y 2) (Z 3)) (+ X Y Z))
(defun maybe-fold-let* (form)
  (if (and (= (length form) 3)
           (consp (%caddr form))
           (eq (%car (%caddr form)) 'LET*))
      (let ((third (maybe-fold-let* (%caddr form))))
        (list* 'LET* (append (%cadr form) (cadr third)) (cddr third)))
      form))

(defun precompile-let* (form)
  (setf form (maybe-fold-let* form))
  (let ((*local-variables* *local-variables*))
    (list* 'LET*
           (precompile-let/let*-vars (cadr form))
           (mapcar #'precompile1 (cddr form)))))

(defun precompile-case (form)
  (if *in-jvm-compile*
      (precompile1 (macroexpand form))
      (let* ((keyform (cadr form))
             (clauses (cddr form))
             (result (list (precompile1 keyform))))
        (dolist (clause clauses)
          (push (precompile-case-clause clause) result))
        (cons (car form) (nreverse result)))))

(defun precompile-case-clause (clause)
  (let ((keys (car clause))
        (forms (cdr clause)))
    (cons keys (mapcar #'precompile1 forms))))

(defun precompile-cond (form)
  (if *in-jvm-compile*
      (precompile1 (macroexpand form))
      (let ((clauses (cdr form))
            (result nil))
        (dolist (clause clauses)
          (push (precompile-cond-clause clause) result))
        (cons 'COND (nreverse result)))))

(defun precompile-cond-clause (clause)
  (let ((test (car clause))
        (forms (cdr clause)))
    (cons (precompile1 test) (mapcar #'precompile1 forms))))

(defun precompile-local-function-def (def)
  (let ((name (car def))
        (arglist (cadr def))
        (body (cddr def)))
    ;; Macro names are shadowed by local functions.
    (push nil *local-functions-and-macros*)
    (push name *local-functions-and-macros*)
    (list* name arglist (mapcar #'precompile1 body))))

(defun precompile-local-functions (defs)
  (let ((result nil))
    (dolist (def defs (nreverse result))
      (push (precompile-local-function-def def) result))))

(defun find-use (name expression)
  (cond ((atom expression)
         nil)
        ((eq (%car expression) name)
         t)
        ((consp name)
         t) ;; FIXME Recognize use of SETF functions!
        (t
         (or (find-use name (%car expression))
             (find-use name (%cdr expression))))))

(defun precompile-flet/labels (form)
  (let ((*local-functions-and-macros* *local-functions-and-macros*)
        (operator (car form))
        (locals (cadr form))
        (body (cddr form)))
    (dolist (local locals)
      (let* ((name (car local))
             (used-p (find-use name body)))
        (unless used-p
          (when (eq operator 'LABELS)
            (dolist (local locals)
              (when (neq name (car local))
                (when (find-use name (cddr local))
                  (setf used-p t)
                  (return))
                ;; Scope of defined function names includes &AUX parameters (LABELS.7B).
                (let ((aux-vars (cdr (memq '&aux (cadr local)))))
                  (when (and aux-vars (find-use name aux-vars)
                             (setf used-p t)
                             (return))))))))
        (unless used-p
          (format t "; Note: deleting unused local function ~A ~S~%" operator name)
          (let* ((new-locals (remove local locals :test 'eq))
                 (new-form
                  (if new-locals
                      (list* operator new-locals body)
                      (list* 'PROGN body))))
            (return-from precompile-flet/labels (precompile1 new-form))))))
    (list* (car form)
           (precompile-local-functions locals)
           (mapcar #'precompile1 body))))

(defun precompile-function (form)
  (if (and (consp (cadr form)) (eq (caadr form) 'LAMBDA))
      (list 'FUNCTION (precompile-lambda (%cadr form)))
      form))

(defun precompile-if (form)
  (let ((args (cdr form)))
    (case (length args)
      (2
       (let ((test (precompile1 (%car args))))
         (cond ((null test)
                nil)
               (;;(constantp test)
                (eq test t)
                (precompile1 (%cadr args)))
               (t
                (list 'IF
                      test
                      (precompile1 (%cadr args)))))))
      (3
       (let ((test (precompile1 (%car args))))
         (cond ((null test)
                (precompile1 (%caddr args)))
               (;;(constantp test)
                (eq test t)
                (precompile1 (%cadr args)))
               (t
                (list 'IF
                      test
                      (precompile1 (%cadr args))
                      (precompile1 (%caddr args)))))))
      (t
       (error "wrong number of arguments for IF")))))

(defun precompile-when (form)
  (if *in-jvm-compile*
      (precompile1 (macroexpand form))
      (precompile-cons form)))

(defun precompile-unless (form)
  (if *in-jvm-compile*
      (precompile1 (macroexpand form))
      (precompile-cons form)))

;; MULTIPLE-VALUE-BIND is handled explicitly by the JVM compiler.
(defun precompile-multiple-value-bind (form)
  (let ((vars (cadr form))
        (values-form (caddr form))
        (body (cdddr form)))
    (list* 'MULTIPLE-VALUE-BIND
           vars
           (precompile1 values-form)
           (mapcar #'precompile1 body))))

;; MULTIPLE-VALUE-LIST is handled explicitly by the JVM compiler.
(defun precompile-multiple-value-list (form)
  (list 'MULTIPLE-VALUE-LIST (precompile1 (cadr form))))

(defun precompile-nth-value (form)
  (if *in-jvm-compile*
      (precompile1 (macroexpand form))
      form))

(defun precompile-return (form)
  (if *in-jvm-compile*
      (precompile1 (macroexpand form))
      (list 'RETURN (precompile1 (cadr form)))))

(defun precompile-return-from (form)
  (list 'RETURN-FROM (cadr form) (precompile1 (caddr form))))

(defun precompile-tagbody (form)
  (do ((body (cdr form) (cdr body))
       (result ()))
      ((null body) (cons 'TAGBODY (nreverse result)))
    (if (atom (car body))
        (push (car body) result)
        (push (precompile1 (car body)) result))))

(defun precompile-eval-when (form)
  (list* 'EVAL-WHEN (cadr form) (mapcar #'precompile1 (cddr form))))

(defun precompile-unwind-protect (form)
  (list* 'UNWIND-PROTECT
         (precompile1 (cadr form))
         (mapcar #'precompile1 (cddr form))))

;; EXPAND-MACRO is like MACROEXPAND, but EXPAND-MACRO quits if *IN-JVM-COMPILE*
;; is false and a macro is encountered that is also implemented as a special
;; operator, so interpreted code can use the special operator implementation.
(defun expand-macro (form)
  (loop
    (unless *in-jvm-compile*
      (when (and (consp form)
                 (symbolp (%car form))
                 (special-operator-p (%car form)))
        (return-from expand-macro form)))
    (multiple-value-bind (result expanded)
        (macroexpand-1 form *compile-file-environment*)
      (unless expanded
        (return-from expand-macro result))
      (setf form result))))

(declaim (ftype (function (t t) t) precompile-form))
(defun precompile-form (form in-jvm-compile)
  (let ((*in-jvm-compile* in-jvm-compile)
        (*inline-declarations* *inline-declarations*)
        (*local-functions-and-macros* ()))
    (precompile1 form)))

(defun install-handler (symbol &optional handler)
  (declare (type symbol symbol))
  (let ((handler (or handler
                     (find-symbol (sys::%format nil "PRECOMPILE-~A" (symbol-name symbol))
                                  'precompiler))))
    (unless (and handler (fboundp handler))
      (error "No handler for ~S." symbol))
    (setf (get symbol 'precompile-handler) handler)))

(defun install-handlers ()
  (mapcar #'install-handler '(BLOCK
                              CASE
                              COND
                              DOLIST
                              DOTIMES
                              EVAL-WHEN
                              FUNCTION
                              IF
                              LAMBDA
                              MACROLET
                              MULTIPLE-VALUE-BIND
                              MULTIPLE-VALUE-LIST
                              NAMED-LAMBDA
                              NTH-VALUE
                              PROGN
                              PROGV
                              PSETF
                              PSETQ
                              RESTART-CASE
                              RETURN
                              RETURN-FROM
                              SETF
                              SETQ
                              SYMBOL-MACROLET
                              TAGBODY
                              UNWIND-PROTECT
                              UNLESS
                              WHEN))

  (dolist (pair '((ECASE                precompile-case)

                  (AND                  precompile-cons)
                  (OR                   precompile-cons)

                  (CATCH                precompile-cons)
                  (MULTIPLE-VALUE-CALL  precompile-cons)
                  (MULTIPLE-VALUE-PROG1 precompile-cons)

                  (DO                   precompile-do/do*)
                  (DO*                  precompile-do/do*)

                  (LET                  precompile-let)
                  (LET*                 precompile-let*)

                  (LOCALLY              precompile-locally)

                  (FLET                 precompile-flet/labels)
                  (LABELS               precompile-flet/labels)

                  (LOAD-TIME-VALUE      precompile-load-time-value)

                  (DECLARE              precompile-identity)
;;                   (DEFMETHOD            precompile-identity)
                  (DEFUN                precompile-defun)
                  (GO                   precompile-identity)
                  (QUOTE                precompile-identity)
                  (THE                  precompile-the)
                  (THROW                precompile-cons)
                  (TRULY-THE            precompile-truly-the)))
    (install-handler (first pair) (second pair))))

(install-handlers)

(in-package #:system)

(defun precompile (name &optional definition)
  (unless definition
    (setq definition (or (and (symbolp name) (macro-function name))
                         (fdefinition name))))
  (let (expr result)
    (cond ((functionp definition)
           (multiple-value-bind (form closure-p)
             (function-lambda-expression definition)
             (unless form
;;                (format t "; No lambda expression available for ~S.~%" name)
               (return-from precompile (values nil t t)))
             (when closure-p
               (format t "; Unable to compile function ~S defined in non-null lexical environment.~%" name)
               (finish-output)
               (return-from precompile (values nil t t)))
             (setq expr form)))
          ((and (consp definition) (eq (%car definition) 'lambda))
           (setq expr definition))
          (t
;;            (error 'type-error)))
           (format t "Unable to precompile ~S.~%" name)
           (return-from precompile (values nil t t))))
    (setf result (coerce-to-function (precompile-form expr nil)))
    (when (and name (functionp result))
      (%set-lambda-name result name)
      (set-call-count result (call-count definition))
      (let ((*warn-on-redefinition* nil))
        (if (and (symbolp name) (macro-function name))
            (let ((mac (make-macro name result)))
              (%set-arglist mac (arglist (symbol-function name)))
              (setf (fdefinition name) mac))
            (progn
              (setf (fdefinition name) result)
              (%set-arglist result (arglist definition))))))
    (values (or name result) nil nil)))

(defun precompile-package (pkg &key verbose)
  (dolist (sym (package-symbols pkg))
    (when (fboundp sym)
      (unless (special-operator-p sym)
        (let ((f (fdefinition sym)))
          (unless (compiled-function-p f)
            (when verbose
              (format t "Precompiling ~S~%" sym)
              (finish-output))
            (precompile sym))))))
  t)

(defun %compile (name definition)
  (if (and name (fboundp name) (%typep (symbol-function name) 'generic-function))
      (values name nil nil)
      (precompile name definition)))

;; ;; Redefine EVAL to precompile its argument.
;; (defun eval (form)
;;   (%eval (precompile-form form nil)))

;; ;; Redefine DEFMACRO to precompile the expansion function on the fly.
;; (defmacro defmacro (name lambda-list &rest body)
;;   (let* ((form (gensym "WHOLE-"))
;;          (env (gensym "ENVIRONMENT-")))
;;     (multiple-value-bind (body decls)
;;         (parse-defmacro lambda-list form body name 'defmacro :environment env)
;;       (let ((expander `(lambda (,form ,env) ,@decls (block ,name ,body))))
;;         `(progn
;;            (let ((macro (make-macro ',name
;;                                     (or (precompile nil ,expander) ,expander))))
;;              ,@(if (special-operator-p name)
;;                    `((put ',name 'macroexpand-macro macro))
;;                    `((fset ',name macro)))
;;              (%set-arglist macro ',lambda-list)
;;              ',name))))))

;; Make an exception just this one time...
(when (get 'defmacro 'macroexpand-macro)
  (fset 'defmacro (get 'defmacro 'macroexpand-macro))
  (remprop 'defmacro 'macroexpand-macro))

(defvar *defined-functions*)

(defvar *undefined-functions*)

(defun note-name-defined (name)
  (when (boundp '*defined-functions*)
    (push name *defined-functions*))
  (when (and (boundp '*undefined-functions*) (not (null *undefined-functions*)))
    (setf *undefined-functions* (remove name *undefined-functions*))))

;; Redefine DEFUN to precompile the definition on the fly.
(defmacro defun (name lambda-list &body body &environment env)
  (note-name-defined name)
  (multiple-value-bind (body decls doc)
      (parse-body body)
    (let* ((block-name (fdefinition-block-name name))
           (lambda-expression `(named-lambda ,name ,lambda-list ,@decls ,@(when doc `(,doc))
                                             (block ,block-name ,@body))))
      (cond (*compile-file-truename*
             `(fset ',name ,lambda-expression))
            (t
             (when (and env (empty-environment-p env))
               (setf env nil))
             (when (null env)
               (setf lambda-expression (precompile-form lambda-expression nil)))
             `(%defun ',name ,lambda-expression))))))
