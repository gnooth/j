;;; defmacro.lisp
;;;
;;; Copyright (C) 2003-2006 Peter Graves
;;; $Id: defmacro.lisp,v 1.7 2006/03/17 01:33:42 piso Exp $
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

;;;; Adapted from CMUCL/SBCL.

(in-package #:system)

;; Redefine DEFMACRO to use PARSE-DEFMACRO.
(defmacro defmacro (name lambda-list &rest body)
  (let* ((whole (gensym "WHOLE-"))
         (env   (gensym "ENVIRONMENT-")))
    (multiple-value-bind (body decls)
        (parse-defmacro lambda-list whole body name 'defmacro :environment env)
      (let ((expander `(lambda (,whole ,env) ,@decls ,body)))
        `(progn
           (let ((macro (make-macro ',name
                                    (or (precompile nil ,expander) ,expander))))
             ,@(if (special-operator-p name)
                   `((put ',name 'macroexpand-macro macro))
                   `((fset ',name macro)))
             (%set-arglist macro ',lambda-list)
             ',name))))))
