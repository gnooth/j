;;; psetf.lisp
;;;
;;; Copyright (C) 2003-2005 Peter Graves
;;; $Id: psetf.lisp,v 1.3 2005/02/02 16:54:13 piso Exp $
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

;;; From CMUCL.

(in-package #:system)

(require '#:collect)

(defmacro psetf (&rest args &environment env)
  "This is to SETF as PSETQ is to SETQ.  Args are alternating place
   expressions and values to go into those places.  All of the subforms and
   values are determined, left to right, and only then are the locations
   updated.  Returns NIL."
  (collect ((let*-bindings) (mv-bindings) (setters))
           (do ((a args (cddr a)))
               ((endp a))
             (when (endp (cdr a))
               (error 'simple-program-error
                      :format-control "Odd number of arguments to PSETF."))
             (multiple-value-bind
               (dummies vals newval setter getter)
               (get-setf-expansion (car a) env)
               (declare (ignore getter))
               (let*-bindings (mapcar #'list dummies vals))
               (mv-bindings (list newval (cadr a)))
               (setters setter)))
           (labels ((thunk (let*-bindings mv-bindings)
                           (if let*-bindings
                               `(let* ,(car let*-bindings)
                                  (multiple-value-bind ,@(car mv-bindings)
                                    ,(thunk (cdr let*-bindings) (cdr mv-bindings))))
                               `(progn ,@(setters) nil))))
                   (thunk (let*-bindings) (mv-bindings)))))
