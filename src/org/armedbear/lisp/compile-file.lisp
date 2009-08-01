;;; compile-file.lisp
;;;
;;; Copyright (C) 2004-2006 Peter Graves
;;; $Id: compile-file.lisp,v 1.119 2006/03/17 01:33:42 piso Exp $
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
;;; Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.

(in-package #:system)

(require '#:jvm)

(defvar *fbound-names*)

(defvar *class-number*)

(defvar *output-file-pathname*)

(declaim (ftype (function () t) next-classfile-name))
(defun next-classfile-name ()
  (let ((name (%format nil "~A-~D"
                       (substitute #\_ #\. (pathname-name *output-file-pathname*))
                       (incf *class-number*))))
    (namestring (merge-pathnames (make-pathname :name name :type "cls")
                                 *output-file-pathname*))))

(defmacro report-error (&rest forms)
  `(handler-case (progn ,@forms)
     (compiler-unsupported-feature-error (condition)
       (fresh-line)
       (%format t "; UNSUPPORTED-FEATURE: ~A~%" condition)
       (values nil condition))))

;; Dummy function. Should never be called.
(defun dummy (&rest ignored)
  (declare (ignore ignored))
  (assert nil))

(declaim (ftype (function (t) t) verify-load))
(defun verify-load (classfile)
  (and classfile
       (let ((*load-truename* *output-file-pathname*))
         (report-error
          (load-compiled-function classfile)))))

(declaim (ftype (function (t stream) t) process-defconstant))
(defun process-defconstant (form stream)
  ;; "If a DEFCONSTANT form appears as a top level form, the compiler
  ;; must recognize that [the] name names a constant variable. An
  ;; implementation may choose to evaluate the value-form at compile
  ;; time, load time, or both. Therefore, users must ensure that the
  ;; initial-value can be evaluated at compile time (regardless of
  ;; whether or not references to name appear in the file) and that
  ;; it always evaluates to the same value."
  (eval form)
  (cond ((structure-object-p (third form))
         (multiple-value-bind (creation-form initialization-form)
             (make-load-form (third form))
           (dump-form (list 'DEFCONSTANT (second form) creation-form) stream)))
        (t
         (dump-form form stream)))
  (%stream-terpri stream))

(declaim (ftype (function (t) t) note-toplevel-form))
(defun note-toplevel-form (form)
  (when *compile-print*
    (fresh-line)
    (princ "; ")
    (let ((*print-length* 2)
          (*print-level* 2)
          (*print-pretty* nil))
      (prin1 form))
    (terpri)))

(declaim (ftype (function (t stream t) t) process-toplevel-form))
(defun process-toplevel-form (form stream compile-time-too)
  (cond ((atom form)
         (when compile-time-too
           (eval form)))
        (t
         (let ((operator (%car form)))
           (case operator
             (MACROLET
              (process-toplevel-macrolet form stream compile-time-too)
              (return-from process-toplevel-form))
             ((IN-PACKAGE DEFPACKAGE)
              (note-toplevel-form form)
              (setf form (precompile-form form nil))
              (eval form)
              ;; Force package prefix to be used when dumping form.
              (let ((*package* +keyword-package+))
                (dump-form form stream))
              (%stream-terpri stream)
              (return-from process-toplevel-form))
             ((DEFVAR DEFPARAMETER)
              (note-toplevel-form form)
              (if compile-time-too
                  (eval form)
                  ;; "If a DEFVAR or DEFPARAMETER form appears as a top level form,
                  ;; the compiler must recognize that the name has been proclaimed
                  ;; special. However, it must neither evaluate the initial-value
                  ;; form nor assign the dynamic variable named NAME at compile
                  ;; time."
                  (let ((name (second form)))
                    (%defvar name))))
             (DEFCONSTANT
              (note-toplevel-form form)
              (process-defconstant form stream)
              (return-from process-toplevel-form))
             (DEFUN
              (note-toplevel-form form)
              (let* ((name (second form))
                     (block-name (fdefinition-block-name name))
                     (lambda-list (third form))
                     (body (nthcdr 3 form))
                     (*speed* *speed*)
                     (*space* *space*)
                     (*safety* *safety*)
                     (*debug* *debug*))
                (multiple-value-bind (body decls doc)
                    (parse-body body)
                  (let* ((expr `(lambda ,lambda-list ,@decls (block ,block-name ,@body)))
                         (classfile-name (next-classfile-name))
                         (classfile (report-error
                                     (jvm:compile-defun name expr nil classfile-name)))
                         (compiled-function (verify-load classfile)))
                    (cond (compiled-function
                           (setf form
                                 `(fset ',name
                                        (load-compiled-function ,(file-namestring classfile))
                                        ,*source-position*
                                        ',lambda-list
                                        ,doc))
                           (when compile-time-too
                             (fset name compiled-function)))
                          (t
                           ;; FIXME This should be a warning or error of some sort...
                           (format *error-output* "; Unable to compile function ~A~%" name)
                           (let ((precompiled-function (precompile-form expr nil)))
                             (setf form
                                   `(fset ',name
                                          ,precompiled-function
                                          ,*source-position*
                                          ',lambda-list
                                          ,doc)))
                           (when compile-time-too
                             (eval form)))))
                  (when (and (symbolp name) (eq (get name '%inline) 'INLINE))
                    ;; FIXME Need to support SETF functions too!
                    (setf (inline-expansion name)
                          (jvm::generate-inline-expansion block-name lambda-list body))
                    (dump-form `(setf (inline-expansion ',name) ',(inline-expansion name))
                               stream)
                    (%stream-terpri stream)))
                (push name jvm::*functions-defined-in-current-file*)
                (note-name-defined name)
                ;; If NAME is not fbound, provide a dummy definition so that
                ;; getSymbolFunctionOrDie() will succeed when we try to verify that
                ;; functions defined later in the same file can be loaded correctly.
                (unless (fboundp name)
                  (setf (fdefinition name) #'dummy)
                  (push name *fbound-names*))))
             ((DEFGENERIC DEFMETHOD)
              (note-toplevel-form form)
              (note-name-defined (second form))
              (let ((*compile-print* nil))
                (process-toplevel-form (macroexpand-1 form *compile-file-environment*)
                                       stream compile-time-too))
              (return-from process-toplevel-form))
             (DEFMACRO
              (note-toplevel-form form)
              (let ((name (second form)))
                (eval form)
                (let* ((expr (function-lambda-expression (macro-function name)))
                       (classfile-name (next-classfile-name))
                       (classfile
                        (ignore-errors
                         (jvm:compile-defun nil expr nil classfile-name))))
                  (if (verify-load classfile)
                      (progn
                        (setf form
                              (if (special-operator-p name)
                                  `(put ',name 'macroexpand-macro
                                        (make-macro ',name
                                                    (load-compiled-function
                                                     ,(file-namestring classfile))))
                                  `(fset ',name
                                         (make-macro ',name
                                                     (load-compiled-function
                                                      ,(file-namestring classfile)))
                                         ,*source-position*
                                         ',(third form)))))
                      ;; FIXME error or warning
                      (format *error-output* "; Unable to compile macro ~A~%" name)))))
             (DEFTYPE
              (note-toplevel-form form)
              (eval form))
             (EVAL-WHEN
              (multiple-value-bind (ct lt e)
                  (parse-eval-when-situations (cadr form))
                (let ((new-compile-time-too (or ct
                                                (and compile-time-too e)))
                      (body (cddr form)))
                  (cond (lt
                         (process-toplevel-progn body stream new-compile-time-too))
                        (new-compile-time-too
                         (eval `(progn ,@body)))))
                (return-from process-toplevel-form)))
             (LOCALLY
              ;; FIXME Need to handle special declarations too!
              (let ((*speed* *speed*)
                    (*safety* *safety*)
                    (*debug* *debug*)
                    (*space* *space*)
                    (*inline-declarations* *inline-declarations*))
                (multiple-value-bind (forms decls)
                    (parse-body (cdr form) nil)
                  (process-optimization-declarations decls)
                  (process-toplevel-progn forms stream compile-time-too)
                  (return-from process-toplevel-form))))
             (PROGN
              (process-toplevel-progn (cdr form) stream compile-time-too)
              (return-from process-toplevel-form))
             (DECLARE
              (compiler-style-warn "Misplaced declaration: ~S" form))
             (t
              (when (and (symbolp operator)
                         (macro-function operator *compile-file-environment*))
                (note-toplevel-form form)
                ;; Note that we want MACROEXPAND-1 and not MACROEXPAND here, in
                ;; case the form being expanded expands into something that needs
                ;; special handling by PROCESS-TOPLEVEL-FORM (e.g. DEFMACRO).
                (let ((*compile-print* nil))
                  (process-toplevel-form (macroexpand-1 form *compile-file-environment*)
                                         stream compile-time-too))
                (return-from process-toplevel-form))

              (when compile-time-too
                (eval form))

              (cond ((eq operator 'QUOTE)
;;                      (setf form (precompile-form form nil))
                     (return-from process-toplevel-form)
                     )
                    ((eq operator 'PUT)
                     (setf form (precompile-form form nil)))
                    ((eq operator 'COMPILER-DEFSTRUCT)
                     (setf form (precompile-form form nil)))
                    ((eq operator 'PROCLAIM)
                     (setf form (precompile-form form nil)))
                    ((and (memq operator '(EXPORT REQUIRE PROVIDE SHADOW))
                          (or (keywordp (second form))
                              (and (listp (second form))
                                   (eq (first (second form)) 'QUOTE))))
                     (setf form (precompile-form form nil)))
                    ((eq operator 'IMPORT)
                     (setf form (precompile-form form nil))
                     ;; Make sure package prefix is printed when symbols are imported.
                     (let ((*package* +keyword-package+))
                       (dump-form form stream))
                     (%stream-terpri stream)
                     (return-from process-toplevel-form))
                    ((and (eq operator '%SET-FDEFINITION)
                          (eq (car (second form)) 'QUOTE)
                          (consp (third form))
                          (eq (%car (third form)) 'FUNCTION)
                          (symbolp (cadr (third form))))
                     (setf form (precompile-form form nil)))
;;                     ((memq operator '(LET LET*))
;;                      (let ((body (cddr form)))
;;                        (if (dolist (subform body nil)
;;                              (when (and (consp subform) (eq (%car subform) 'DEFUN))
;;                                (return t)))
;;                            (setf form (convert-toplevel-form form))
;;                            (setf form (precompile-form form nil)))))
                    ((eq operator 'mop::ensure-method)
                     (setf form (convert-ensure-method form)))
                    ((and (symbolp operator)
                          (not (special-operator-p operator))
                          (null (cdr form)))
                     (setf form (precompile-form form nil)))
                    (t
;;                      (setf form (precompile-form form nil))
                     (note-toplevel-form form)
                     (setf form (convert-toplevel-form form))
                     )))))))
  (when (consp form)
    (dump-form form stream)
    (%stream-terpri stream)))

(declaim (ftype (function (t) t) convert-ensure-method))
(defun convert-ensure-method (form)
  (c-e-m-1 form :function)
  (c-e-m-1 form :fast-function)
  (precompile-form form nil))

(declaim (ftype (function (t t) t) c-e-m-1))
(defun c-e-m-1 (form key)
  (let* ((tail (cddr form))
         (function-form (getf tail key)))
    (when (and function-form (consp function-form)
               (eq (%car function-form) 'FUNCTION))
      (let ((lambda-expression (cadr function-form)))
        (let* ((*speed* *speed*)
               (*space* *space*)
               (*safety* *safety*)
               (*debug* *debug*))
          (let* ((classfile-name (next-classfile-name))
                 (classfile (report-error
                             (jvm:compile-defun nil lambda-expression nil classfile-name)))
                 (compiled-function (verify-load classfile)))
            (cond (compiled-function
                   (setf (getf tail key)
                         `(load-compiled-function ,(file-namestring classfile))))
                  (t
                   ;; FIXME This should be a warning or error of some sort...
                   (format *error-output* "; Unable to compile method~%")))))))))

(declaim (ftype (function (t) t) convert-toplevel-form))
(defun convert-toplevel-form (form)
  (let* ((expr `(lambda () ,form))
         (classfile-name (next-classfile-name))
         (classfile (report-error (jvm:compile-defun nil expr nil classfile-name)))
         (compiled-function (verify-load classfile)))
    (setf form
          (if compiled-function
              `(funcall (load-compiled-function ,(file-namestring classfile)))
              (precompile-form form nil)))))


(defun process-toplevel-macrolet (form stream compile-time-too)
  (let ((*compile-file-environment* (make-environment *compile-file-environment*)))
    (dolist (definition (cadr form))
      (environment-add-macro-definition *compile-file-environment*
                                        (car definition)
                                        (make-macro (car definition)
                                                    (make-expander-for-macrolet definition))))
    (dolist (body-form (cddr form))
      (process-toplevel-form body-form stream compile-time-too))))

(declaim (ftype (function (t stream t) t) process-toplevel-progn))
(defun process-toplevel-progn (forms stream compile-time-too)
  (dolist (form forms)
    (process-toplevel-form form stream compile-time-too)))

;;; Adapted from SBCL.
;;; Parse an EVAL-WHEN situations list, returning three flags,
;;; (VALUES COMPILE-TOPLEVEL LOAD-TOPLEVEL EXECUTE), indicating
;;; the types of situations present in the list.
(defun parse-eval-when-situations (situations)
  (when (or (not (listp situations))
	    (set-difference situations
			    '(:compile-toplevel
			      compile
			      :load-toplevel
			      load
			      :execute
			      eval)))
    (error "Bad EVAL-WHEN situation list: ~S." situations))
  (values (intersection '(:compile-toplevel compile) situations)
	  (intersection '(:load-toplevel load) situations)
	  (intersection '(:execute eval) situations)))

(defun compile-file (input-file
                     &key
                     output-file
                     ((:verbose *compile-verbose*) *compile-verbose*)
                     ((:print *compile-print*) *compile-print*)
                     external-format)
  (declare (ignore external-format)) ; FIXME
  (unless (or (and (probe-file input-file) (not (file-directory-p input-file)))
              (pathname-type input-file))
    (let ((pathname (merge-pathnames (make-pathname :type "lisp") input-file)))
      (when (probe-file pathname)
        (setf input-file pathname))))
  (setf output-file (if output-file
                        (merge-pathnames output-file *default-pathname-defaults*)
                        (compile-file-pathname input-file)))
  (let* ((*output-file-pathname* output-file)
         (type (pathname-type output-file))
         (temp-file (merge-pathnames (make-pathname :type (concatenate 'string type "-tmp"))
                                     output-file))
         (warnings-p t)
         (failure-p t))
    (with-open-file (in input-file :direction :input)
      (let* ((*compile-file-pathname* (pathname in))
             (*compile-file-truename* (truename in))
             (*source* *compile-file-truename*)
             (*class-number* 0)
             (namestring (namestring *compile-file-truename*))
             (start (get-internal-real-time))
             elapsed)
        (when *compile-verbose*
          (format t "; Compiling ~A ...~%" namestring))
        (with-compilation-unit ()
          (with-open-file (out temp-file :direction :output :if-exists :supersede)
            (let ((*readtable* *readtable*)
                  (*package* *package*)
                  (*speed* *speed*)
                  (*space* *space*)
                  (*safety* *safety*)
                  (*debug* *debug*)
                  (*explain* *explain*)
                  (jvm::*functions-defined-in-current-file* '())
                  (*fbound-names* '()))
              (write "; -*- Mode: Lisp -*-" :escape nil :stream out)
              (%stream-terpri out)
              (let ((*package* (find-package '#:cl)))
                (write (list 'init-fasl :version *fasl-version*) :stream out)
                (%stream-terpri out)
                (write (list 'setq '*source* *compile-file-truename*) :stream out)
                (%stream-terpri out))
              (loop
                (let* ((*source-position* (file-position in))
                       (jvm::*source-line-number* (stream-line-number in))
                       (form (read in nil in))
                       (*compiler-error-context* form))
                  (when (eq form in)
                    (return))
                  (process-toplevel-form form out nil)))
              (dolist (name *fbound-names*)
                (fmakunbound name))))
          (cond ((zerop (+ jvm::*errors* jvm::*warnings* jvm::*style-warnings*))
                 (setf warnings-p nil failure-p nil))
                ((zerop (+ jvm::*errors* jvm::*warnings*))
                 (setf failure-p nil))))
        (rename-file temp-file output-file)

        (when *compile-file-zip*
          (let ((zipfile (concatenate 'string (namestring output-file) ".zip"))
                (pathnames ()))
            (dotimes (i *class-number*)
              (let* ((file-namestring (%format nil "~A-~D.cls"
                                               (substitute #\_ #\. (pathname-name output-file))
                                               (1+ i)))
                     (pathname (merge-pathnames file-namestring output-file)))
                (when (probe-file pathname)
                  (push pathname pathnames))))
            (setf pathnames (nreverse pathnames))
            (let ((load-file (merge-pathnames (make-pathname :type "_")
                                              output-file)))
              (rename-file output-file load-file)
              (push load-file pathnames))
            (zip zipfile pathnames)
            (dolist (pathname pathnames)
              (let ((truename (probe-file pathname)))
                (when truename
                  (delete-file truename))))
            (rename-file zipfile output-file)))

        (setf elapsed (/ (- (get-internal-real-time) start) 1000.0))
        (when *compile-verbose*
          (format t "~&; Wrote ~A (~A seconds)~%" (namestring output-file) elapsed))))
    (values (truename output-file) warnings-p failure-p)))

(defun compile-file-if-needed (input-file &rest allargs &key force-compile)
  (setf input-file (truename input-file))
  (cond (force-compile
         (remf allargs :force-compile)
         (apply 'compile-file input-file allargs))
        (t
         (let* ((source-write-time (file-write-date input-file))
                (output-file       (or (getf allargs :output-file)
                                       (compile-file-pathname input-file)))
                (target-write-time (and (probe-file output-file)
                                        (file-write-date output-file))))
           (if (or (null target-write-time)
                   (<= target-write-time source-write-time))
               (apply 'compile-file input-file allargs)
               output-file)))))

(provide 'compile-file)
