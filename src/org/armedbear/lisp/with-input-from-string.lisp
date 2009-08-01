;;; with-input-from-string.lisp
;;;
;;; Copyright (C) 2004-2005 Peter Graves
;;; $Id: with-input-from-string.lisp,v 1.3 2005/01/19 14:56:49 piso Exp $
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

;;; Adapted from CMUCL.

(in-package "SYSTEM")

(defmacro with-input-from-string ((var string &key index start end) &body body)
  (multiple-value-bind (forms decls) (parse-body body)
    `(let ((,var
            ,(cond ((null end)
                    `(make-string-input-stream ,string ,(or start 0)))
                   ((symbolp end)
                    `(if ,end
                         (make-string-input-stream ,string
                                                   ,(or start 0)
                                                   ,end)
                         (make-string-input-stream ,string
                                                   ,(or start 0))))
                   (t
                    `(make-string-input-stream ,string
                                               ,(or start 0)
                                               ,end)))))
       ,@decls
       (unwind-protect
        (multiple-value-prog1
          (progn ,@forms)
          ,@(when index
              `((setf ,index (string-input-stream-current ,var)))))
        (close ,var)))))
