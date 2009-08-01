;;; substitute.lisp
;;;
;;; Copyright (C) 2003 Peter Graves
;;; $Id: substitute.lisp,v 1.7 2003/08/25 18:22:58 piso Exp $
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


(in-package "COMMON-LISP")

(export '(substitute substitute-if substitute-if-not))

;;; From CMUCL.

(defmacro real-count (count)
  `(cond ((null ,count) most-positive-fixnum)
         ((sys::fixnump ,count) (if (minusp ,count) 0 ,count))
         ((integerp ,count) (if (minusp ,count) 0 most-positive-fixnum))
         (t ,count)))

(defun list-substitute* (pred new list start end count key test test-not old)
  (let* ((result (list nil))
	 elt
	 (splice result)
	 (list list))           ; Get a local list for a stepper.
    (do ((index 0 (1+ index)))
      ((= index start))
      (setq splice (cdr (rplacd splice (list (car list)))))
      (setq list (cdr list)))
    (do ((index start (1+ index)))
      ((or (= index end) (null list) (= count 0)))
      (setq elt (car list))
      (setq splice
	    (cdr (rplacd splice
			 (list
			  (cond
			   ((case pred
                              (normal
                               (if test-not
                                   (not
                                    (funcall test-not old (sys::apply-key key elt)))
                                   (funcall test old (sys::apply-key key elt))))
                              (if (funcall test (sys::apply-key key elt)))
                              (if-not (not (funcall test (sys::apply-key key elt)))))
			    (setq count (1- count))
			    new)
                           (t elt))))))
      (setq list (cdr list)))
    (do ()
      ((null list))
      (setq splice (cdr (rplacd splice (list (car list)))))
      (setq list (cdr list)))
    (cdr result)))


;;; Replace old with new in sequence moving from left to right by incrementer
;;; on each pass through the loop. Called by all three substitute functions.
(defun vector-substitute* (pred new sequence incrementer left right length
                                start end count key test test-not old)
  (let ((result (sys::make-sequence-like sequence length))
	(index left))
    (do ()
      ((= index start))
      (setf (aref result index) (aref sequence index))
      (setq index (+ index incrementer)))
    (do ((elt))
      ((or (= index end) (= count 0)))
      (setq elt (aref sequence index))
      (setf (aref result index)
	    (cond ((case pred
                     (normal
                      (if test-not
                          (not (funcall test-not old (sys::apply-key key elt)))
                          (funcall test old (sys::apply-key key elt))))
                     (if (funcall test (sys::apply-key key elt)))
                     (if-not (not (funcall test (sys::apply-key key elt)))))
		   (setq count (1- count))
		   new)
		  (t elt)))
      (setq index (+ index incrementer)))
    (do ()
      ((= index right))
      (setf (aref result index) (aref sequence index))
      (setq index (+ index incrementer)))
    result))

(defmacro subst-dispatch (pred)
  `(if (listp sequence)
       (if from-end
           (nreverse (list-substitute* ,pred new (reverse sequence)
                                       (- length end)
                                       (- length start)
                                       count key test test-not old))
           (list-substitute* ,pred new sequence start end count key test test-not
                             old))
       (if from-end
           (vector-substitute* ,pred new sequence -1 (1- length)
                               -1 length (1- end)
                               (1- start) count key test test-not old)
           (vector-substitute* ,pred new sequence 1 0 length length
                               start end count key test test-not old))))


(defun substitute (new old sequence &key from-end (test #'eql) test-not
                       (start 0) count end key)
  (let* ((length (length sequence))
	 (end (or end length))
	 (count (real-count count)))
    (subst-dispatch 'normal)))


(defun substitute-if (new test sequence &key from-end (start 0) end count key)
  (let* ((length (length sequence))
	 (end (or end length))
	 (count (real-count count))
	 test-not
	 old)
    (subst-dispatch 'if)))


(defun substitute-if-not (new test sequence &key from-end (start 0)
                              end count key)
  (let* ((length (length sequence))
	 (end (or end length))
	 (count (real-count count))
	 test-not
	 old)
    (subst-dispatch 'if-not)))
