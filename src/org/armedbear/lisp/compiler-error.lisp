;;; compiler-error.lisp
;;;
;;; Copyright (C) 2003-2005 Peter Graves
;;; $Id: compiler-error.lisp,v 1.2 2005/07/13 16:17:47 piso Exp $
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

(export '(*compiler-error-context*
          compiler-style-warn
          compiler-warn
          compiler-error
          compiler-unsupported))

(defvar *compiler-error-context* nil)

(defun compiler-style-warn (format-control &rest format-arguments)
  (warn 'style-warning
        :format-control format-control
        :format-arguments format-arguments))

(defun compiler-warn (format-control &rest format-arguments)
  (warn 'warning
        :format-control format-control
        :format-arguments format-arguments))

(defun compiler-error (format-control &rest format-arguments)
  (error 'compiler-error
         :format-control format-control
         :format-arguments format-arguments))

(defun compiler-unsupported (format-control &rest format-arguments)
  (error 'compiler-unsupported-feature-error
         :format-control format-control
         :format-arguments format-arguments))
