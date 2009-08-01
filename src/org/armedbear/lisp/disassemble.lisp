;;; disassemble.lisp
;;;
;;; Copyright (C) 2005 Peter Graves
;;; $Id: disassemble.lisp,v 1.4 2005/12/24 16:34:19 piso Exp $
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

(require '#:clos)

(defun disassemble (arg)
  (require-type arg '(OR FUNCTION
                      SYMBOL
                      (CONS (EQL SETF) (CONS SYMBOL NULL))
                      (CONS (EQL LAMBDA) LIST)))
  (let ((function (cond ((functionp arg)
                         arg)
                        ((symbolp arg)
                         (or (macro-function arg) (symbol-function arg))))))
    (when (typep function 'generic-function)
      (setf function (mop::funcallable-instance-function function)))
    (when (functionp function)
      (unless (compiled-function-p function)
        (setf function (compile nil function)))
      (when (getf (function-plist function) 'class-bytes)
        (with-input-from-string
          (stream (disassemble-class-bytes (getf (function-plist function) 'class-bytes)))
          (loop
            (let ((line (read-line stream nil)))
              (unless line (return))
              (write-string "; ")
              (write-string line)
              (terpri))))
        (return-from disassemble)))
    (%format t "; Disassembly is not available.~%")))
