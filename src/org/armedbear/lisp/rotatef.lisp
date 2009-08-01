;;; rotatef.lisp
;;;
;;; Copyright (C) 2004 Peter Graves
;;; $Id: rotatef.lisp,v 1.2 2004/09/07 15:39:43 piso Exp $
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

(eval-when (:compile-toplevel)
  (require '#:collect))

(defmacro rotatef (&rest args &environment env)
  (when args
    (collect ((let*-bindings) (mv-bindings) (setters) (getters))
      (dolist (arg args)
	(multiple-value-bind (temps subforms store-vars setter getter)
	    (get-setf-expansion arg env)
	  (loop
	    for temp in temps
	    for subform in subforms
	    do (let*-bindings `(,temp ,subform)))
	  (mv-bindings store-vars)
	  (setters setter)
	  (getters getter)))
      (setters nil)
      (getters (car (getters)))
      (labels ((thunk (mv-bindings getters)
		 (if mv-bindings
		     `((multiple-value-bind ,(car mv-bindings) ,(car getters)
			 ,@(thunk (cdr mv-bindings) (cdr getters))))
		     (setters))))
	`(let* ,(let*-bindings)
	   ,@(thunk (mv-bindings) (cdr (getters))))))))
