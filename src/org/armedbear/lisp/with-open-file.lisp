;;; with-open-file.lisp
;;;
;;; Copyright (C) 2004 Peter Graves
;;; $Id: with-open-file.lisp,v 1.1 2004/02/02 02:14:46 piso Exp $
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

(defmacro with-open-file (&rest args)
  (let ((var (caar args))
        (open-args (cdar args))
        (body (cdr args))
        (abortp (gensym)))
    (multiple-value-bind (forms decls) (parse-body body)
      `(let ((,var (open ,@open-args))
             (,abortp t))
         ,@decls
         (unwind-protect
          (multiple-value-prog1
           (progn ,@forms)
           (setq ,abortp nil))
          (when ,var
            (close ,var :abort ,abortp)))))))
