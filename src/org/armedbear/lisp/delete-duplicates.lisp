;;; delete-duplicates.lisp
;;;
;;; Copyright (C) 2003 Peter Graves
;;; $Id: delete-duplicates.lisp,v 1.5 2003/08/25 18:22:58 piso Exp $
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

;;; From CMUCL.

(defun list-delete-duplicates* (list test test-not key from-end start end)
  (let ((handle (cons nil list)))
    (do ((current (nthcdr start list) (cdr current))
	 (previous (nthcdr start handle))
	 (index start (1+ index)))
      ((or (and end (= index end)) (null current))
       (cdr handle))
      (if (do ((x (if from-end
		      (nthcdr (1+ start) handle)
		      (cdr current))
		  (cdr x))
	       (i (1+ index) (1+ i)))
            ((or (null x)
                 (and (not from-end) end (= i end))
                 (eq x current))
             nil)
	    (if (if test-not
		    (not (funcall test-not
				  (sys::apply-key key (car current))
				  (sys::apply-key key (car x))))
		    (funcall test
			     (sys::apply-key key (car current))
			     (sys::apply-key key (car x))))
		(return t)))
	  (rplacd previous (cdr current))
	  (setq previous (cdr previous))))))


(defun vector-delete-duplicates* (vector test test-not key from-end start end
					 &optional (length (length vector)))
  (when (null end) (setf end (length vector)))
  (do ((index start (1+ index))
       (jndex start))
    ((= index end)
     (do ((index index (1+ index))		; copy the rest of the vector
          (jndex jndex (1+ jndex)))
       ((= index length)
        (shrink-vector vector jndex)
        vector)
       (setf (aref vector jndex) (aref vector index))))
    (setf (aref vector jndex) (aref vector index))
    (unless (position (sys::apply-key key (aref vector index)) vector :key key
		      :start (if from-end start (1+ index)) :test test
		      :end (if from-end jndex end) :test-not test-not)
      (setq jndex (1+ jndex)))))


(defun delete-duplicates (sequence &key (test #'eql) test-not (start 0) from-end
                                   end key)
  (if (listp sequence)
      (if sequence
          (list-delete-duplicates* sequence test test-not key from-end start end))
      (vector-delete-duplicates* sequence test test-not key from-end start end)))
