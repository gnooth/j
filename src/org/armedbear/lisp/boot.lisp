;;; boot.lisp
;;;
;;; Copyright (C) 2003-2007 Peter Graves <peter@armedbear.org>
;;; $Id: boot.lisp,v 1.243 2007/04/30 23:51:24 piso Exp $
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

(sys:%in-package "SYSTEM")

(setq *load-verbose*     nil)
(setq *autoload-verbose* nil)

;; Redefined in macros.lisp.
(defmacro in-package (name)
  (list '%in-package (string name)))

(defmacro lambda (lambda-list &rest body)
  (list 'function (list* 'lambda lambda-list body)))

(defmacro named-lambda (name lambda-list &rest body)
  (list 'function (list* 'named-lambda name lambda-list body)))

;; Redefined in macros.lisp.
(defmacro return (&optional result)
  (list 'return-from nil result))

;; Redefined in precompiler.lisp.
(defmacro defun (name lambda-list &rest body)
  (let ((block-name (fdefinition-block-name name)))
    (list '%defun
          (list 'quote name)
          (list 'lambda lambda-list (list* 'block block-name body)))))

;; Redefined in macros.lisp.
(defmacro defconstant (name initial-value &optional docstring)
  (list '%defconstant (list 'quote name) initial-value docstring))

;; Redefined in macros.lisp.
(defmacro defparameter (name initial-value &optional docstring)
  (list '%defparameter (list 'quote name) initial-value docstring))

(defmacro declare (&rest ignored) nil)

(in-package #:extensions)

(export '(%car %cdr %cadr %caddr))

(defmacro %car (x)
  (list 'car (list 'truly-the 'cons x)))

(defmacro %cdr (x)
  (list 'cdr (list 'truly-the 'cons x)))

(defmacro %cadr (x)
  (list '%car (list '%cdr x)))

(defmacro %caddr (x)
  (list '%car (list '%cdr (list '%cdr x))))

(in-package #:system)

;; Redefined in precompiler.lisp.
(defun eval (form)
  (%eval form))

;; Redefined in pprint.lisp.
(defun terpri (&optional output-stream)
  (%terpri output-stream))

;; Redefined in pprint.lisp.
(defun fresh-line (&optional output-stream)
  (%fresh-line output-stream))

;; Redefined in pprint.lisp.
(defun write-char (character &optional output-stream)
  (%write-char character output-stream))

(in-package #:extensions)

;; Redefined in pprint.lisp.
(defun charpos (stream)
  (sys::stream-charpos stream))

;; Redefined in pprint.lisp.
(defun (setf charpos) (new-value stream)
  (sys::stream-%set-charpos stream new-value))

(export 'charpos '#:extensions)

;; Redefined in precompiler.lisp.
(defun precompile (name &optional definition)
  (declare (ignore name definition))
  nil)

(export 'precompile '#:extensions)

(in-package #:system)

(defun simple-format (destination control-string &rest args)
  (apply *simple-format-function* destination control-string args))

(export 'simple-format '#:system)

;; INVOKE-DEBUGGER is redefined in debug.lisp.
(defun invoke-debugger (condition)
  (sys::%format t "~A~%" condition)
  (ext:quit))

(load-system-file "autoloads")
(load-system-file "early-defuns")
(load-system-file "backquote")
(load-system-file "destructuring-bind")
(load-system-file "defmacro")
(load-system-file "setf")
(load-system-file "fdefinition")
(load-system-file "featurep")
(load-system-file "read-conditional")
(load-system-file "macros")

(defun make-package (package-name &key nicknames use)
  (%make-package package-name nicknames use))

;;; Reading circular data: the #= and ## reader macros (from SBCL)

;;; Objects already seen by CIRCLE-SUBST.
(defvar *sharp-equal-circle-table*)

;; This function is kind of like NSUBLIS, but checks for circularities and
;; substitutes in arrays and structures as well as lists. The first arg is an
;; alist of the things to be replaced assoc'd with the things to replace them.
(defun circle-subst (old-new-alist tree)
  (cond ((not (typep tree
                     '(or cons (array t) structure-object standard-object)))
         (let ((entry (find tree old-new-alist :key #'second)))
           (if entry (third entry) tree)))
        ((null (gethash tree *sharp-equal-circle-table*))
         (setf (gethash tree *sharp-equal-circle-table*) t)
         (cond
          ((typep tree 'structure-object)
           (do ((i 0 (1+ i))
                (end (structure-length tree)))
               ((= i end))
             (let* ((old (structure-ref tree i))
                    (new (circle-subst old-new-alist old)))
               (unless (eq old new)
                 (structure-set tree i new)))))
;;           ((typep tree 'standard-object)
;;            (do ((i 1 (1+ i))
;;                 (end (%instance-length tree)))
;;                ((= i end))
;;              (let* ((old (%instance-ref tree i))
;;                     (new (circle-subst old-new-alist old)))
;;                (unless (eq old new)
;;                  (setf (%instance-ref tree i) new)))))
          ((arrayp tree)
           (do ((i 0 (1+ i))
                (end (array-total-size tree)))
               ((>= i end))
             (let* ((old (row-major-aref tree i))
                    (new (circle-subst old-new-alist old)))
               (unless (eq old new)
                 (setf (row-major-aref tree i) new)))))
         (t
          (let ((a (circle-subst old-new-alist (car tree)))
                (d (circle-subst old-new-alist (cdr tree))))
            (unless (eq a (car tree))
              (rplaca tree a))
            (unless (eq d (cdr tree))
              (rplacd tree d)))))
        tree)
  (t tree)))

;;; Sharp-equal works as follows. When a label is assigned (i.e. when
;;; #= is called) we GENSYM a symbol is which is used as an
;;; unforgeable tag. *SHARP-SHARP-ALIST* maps the integer tag to this
;;; gensym.
;;;
;;; When SHARP-SHARP encounters a reference to a label, it returns the
;;; symbol assoc'd with the label. Resolution of the reference is
;;; deferred until the read done by #= finishes. Any already resolved
;;; tags (in *SHARP-EQUAL-ALIST*) are simply returned.
;;;
;;; After reading of the #= form is completed, we add an entry to
;;; *SHARP-EQUAL-ALIST* that maps the gensym tag to the resolved
;;; object. Then for each entry in the *SHARP-SHARP-ALIST, the current
;;; object is searched and any uses of the gensysm token are replaced
;;; with the actual value.

(defvar *sharp-sharp-alist* ())

(defun sharp-equal (stream ignore label)
  (declare (ignore ignore))
  (when *read-suppress* (return-from sharp-equal (values)))
  (unless label
    (error 'reader-error
           :stream stream
           :format-control "Missing label for #="))
  (when (or (assoc label *sharp-sharp-alist*)
            (assoc label *sharp-equal-alist*))
    (error 'reader-error
           :stream stream
           :format-control "Multiply defined label: #~D="
           :format-arguments (list label)))
  (let* ((tag (gensym))
         (*sharp-sharp-alist* (acons label tag *sharp-sharp-alist*))
         (obj (read stream t nil t)))
    (when (eq obj tag)
      (error 'reader-error
             :stream stream
             :format-control "Must tag something more than just #~D#"
             :format-arguments (list label)))
    (push (list label tag obj) *sharp-equal-alist*)
    (let ((*sharp-equal-circle-table* (make-hash-table :test 'eq :size 20)))
      (circle-subst *sharp-equal-alist* obj))))

(defun sharp-sharp (stream ignore label)
  (declare (ignore ignore))
  (when *read-suppress* (return-from sharp-sharp nil))
  (unless label
    (error 'reader-error :stream stream :format-control "Missing label for ##"))
  (let ((entry (assoc label *sharp-equal-alist*)))
    (if entry
        (third entry)
        (let ((pair (assoc label *sharp-sharp-alist*)))
          (unless pair
            (error 'reader-error
                   :stream stream
                   :format-control "Object is not labelled #~S#"
                   :format-arguments (list label)))
          (cdr pair)))))

(set-dispatch-macro-character #\# #\= #'sharp-equal +standard-readtable+)
(set-dispatch-macro-character #\# #\# #'sharp-sharp +standard-readtable+)

(copy-readtable +standard-readtable+ *readtable*)

;; SYS::%COMPILE is redefined in precompiler.lisp.
(defun sys::%compile (name definition)
  (values (if name name definition) nil nil))

(load-system-file "inline")
(load-system-file "proclaim")
(load-system-file "arrays")
(load-system-file "compiler-macro")
(load-system-file "subtypep")
(load-system-file "typep")
(load-system-file "compiler-error")
(load-system-file "source-transform")
(load-system-file "precompiler")

(precompile-package "PRECOMPILER")
(precompile-package "EXTENSIONS")
(precompile-package "SYSTEM")
(precompile-package "COMMON-LISP")

(load-system-file "signal")
(load-system-file "list")
(load-system-file "sequences")
(load-system-file "error")
(load-system-file "defpackage")
(load-system-file "define-modify-macro")

;;; Package definitions.
(defpackage "FORMAT" (:use "CL" "EXT"))

(defpackage "XP"
  (:use "CL")
  (:export
   #:output-pretty-object))

(defconstant lambda-list-keywords
  '(&optional &rest &key &aux &body &whole &allow-other-keys &environment))

(load-system-file "require")
(load-system-file "defstruct")
(load-system-file "restart")
(load-system-file "late-setf")
(load-system-file "debug")
(load-system-file "print")
(load-system-file "pprint-dispatch")
(load-system-file "pprint")
(load-system-file "defsetf")

(defun preload-package (pkg)
  (%format t "Preloading ~S~%" (find-package pkg))
  (dolist (sym (package-symbols pkg))
    (when (autoloadp sym)
      (resolve sym))))

(unless (featurep :j)
  (load-system-file "top-level")
  (unless *noinform*
    (%format t "Startup completed in ~A seconds.~%"
             (float (/ (ext:uptime) 1000)))))
