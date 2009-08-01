;;; upgraded-complex-part-type.lisp
;;;
;;; Copyright (C) 2004-2005 Peter Graves
;;; $Id: upgraded-complex-part-type.lisp,v 1.5 2005/03/13 01:48:21 piso Exp $
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

(defun upgraded-complex-part-type (typespec &optional environment)
  (declare (ignore environment))
  (if (subtypep typespec 'REAL)
      typespec
      (error 'simple-error
             :format-control "The type ~S is not a subtype of ~S."
             :format-arguments (list typespec 'REAL))))
