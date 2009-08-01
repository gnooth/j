;;; defsetf.lisp
;;;
;;; Copyright (C) 2003-2005 Peter Graves
;;; $Id: defsetf.lisp,v 1.3 2005/02/05 18:31:33 piso Exp $
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

;;; Adapted from SBCL.

(in-package #:system)

(require '#:collect)

(defun %defsetf (orig-access-form num-store-vars expander)
  (collect ((subforms) (subform-vars) (subform-exprs) (store-vars))
           (dolist (subform (cdr orig-access-form))
             (if (constantp subform)
                 (subforms subform)
                 (let ((var (gensym)))
                   (subforms var)
                   (subform-vars var)
                   (subform-exprs subform))))
           (dotimes (i num-store-vars)
             (store-vars (gensym)))
           (values (subform-vars)
                   (subform-exprs)
                   (store-vars)
                   (funcall expander (cons (subforms) (store-vars)))
                   `(,(car orig-access-form) ,@(subforms)))))

(defmacro defsetf (access-fn &rest rest)
  (cond ((not (listp (car rest)))
	 `(eval-when (:load-toplevel :compile-toplevel :execute)
	    (%define-setf-macro ',access-fn
                                nil
                                ',(car rest)
				,(when (and (car rest) (stringp (cadr rest)))
				   `',(cadr rest)))))
	((and (cdr rest) (listp (cadr rest)))
	 (destructuring-bind
          (lambda-list (&rest store-variables) &body body)
          rest
          (let ((arglist-var (gensym "ARGS-"))
                (access-form-var (gensym "ACCESS-FORM-"))
                (env-var (gensym "ENVIRONMENT-")))
            (multiple-value-bind
              (body doc)
              (parse-defmacro `(,lambda-list ,@store-variables)
                              arglist-var body access-fn 'defsetf
                              :anonymousp t)
              `(eval-when (:load-toplevel :compile-toplevel :execute)
                 (%define-setf-macro
                  ',access-fn
                  #'(lambda (,access-form-var ,env-var)
                     (declare (ignore ,env-var))
                     (%defsetf ,access-form-var ,(length store-variables)
                               #'(lambda (,arglist-var)
                                  (block ,access-fn
                                    ,body))))
                  nil
                  ',doc))))))
	(t
	 (error "Ill-formed DEFSETF for ~S" access-fn))))
