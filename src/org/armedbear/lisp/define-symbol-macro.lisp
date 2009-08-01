;;; define-symbol-macro.lisp
;;;
;;; Copyright (C) 2003-2004 Peter Graves
;;; $Id: define-symbol-macro.lisp,v 1.3 2004/08/03 16:47:49 piso Exp $
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

(in-package "SYSTEM")

(defun %define-symbol-macro (symbol expansion)
  (setf (symbol-value symbol) (make-symbol-macro expansion))
  symbol)

(defmacro define-symbol-macro (symbol expansion)
  (when (special-variable-p symbol)
    (error 'program-error "~S has already been defined as a global variable." symbol))
  `(eval-when (:compile-toplevel :load-toplevel :execute)
     (%define-symbol-macro ',symbol ',expansion)))
