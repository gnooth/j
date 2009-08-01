;;; read-conditional.lisp
;;;
;;; Copyright (C) 2005-2007 Peter Graves
;;; $Id: read-conditional.lisp,v 1.3 2007/02/22 16:03:48 piso Exp $
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

(defun read-feature (stream)
  (let* ((*package* +keyword-package+)
         (*read-suppress* nil))
    (if (featurep (read stream t nil t))
        #\+ #\-)))

(defun read-conditional (stream subchar int)
  (declare (ignore int))
  (if (eql subchar (read-feature stream))
      (read stream t nil t)
      (let ((*read-suppress* t))
        (read stream t nil t)
        (values))))

(set-dispatch-macro-character #\# #\+ #'read-conditional +standard-readtable+)
(set-dispatch-macro-character #\# #\- #'read-conditional +standard-readtable+)
