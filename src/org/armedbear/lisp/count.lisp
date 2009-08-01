;;; count.lisp
;;;
;;; Copyright (C) 2003 Peter Graves
;;; $Id: count.lisp,v 1.2 2003/07/02 17:55:58 piso Exp $
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

;;; From CMUCL.

(defmacro vector-count-if (not-p from-end-p predicate sequence)
  (let ((next-index (if from-end-p '(1- index) '(1+ index)))
        (pred `(funcall ,predicate (sys::apply-key key (aref ,sequence index)))))
    `(let ((%start ,(if from-end-p '(1- end) 'start))
           (%end ,(if from-end-p '(1- start) 'end)))
       (do ((index %start ,next-index)
            (count 0))
           ((= index %end) count)
         (,(if not-p 'unless 'when) ,pred
           (setq count (1+ count)))))))

(defmacro list-count-if (not-p from-end-p predicate sequence)
  (let ((pred `(funcall ,predicate (sys::apply-key key (pop sequence)))))
    `(let ((%start ,(if from-end-p '(- length end) 'start))
           (%end ,(if from-end-p '(- length start) 'end))
           (sequence ,(if from-end-p '(reverse sequence) 'sequence)))
       (do ((sequence (nthcdr %start ,sequence))
            (index %start (1+ index))
            (count 0))
           ((or (= index %end) (null sequence)) count)
         (,(if not-p 'unless 'when) ,pred
           (setq count (1+ count)))))))

(defun count (item sequence &key from-end (test #'eql test-p) (test-not nil test-not-p)
		   (start 0) end key)
  (when (and test-p test-not-p)
    (error "test and test-not both supplied"))
  (let* ((length (length sequence))
	 (end (or end length)))
    (let ((%test (if test-not-p
		     (lambda (x)
		       (not (funcall test-not item x)))
		     (lambda (x)
		       (funcall test item x)))))
      (if (listp sequence)
          (if from-end
              (list-count-if nil t %test sequence)
              (list-count-if nil nil %test sequence))
          (if from-end
              (vector-count-if nil t %test sequence)
              (vector-count-if nil nil %test sequence))))))

(defun count-if (test sequence &key from-end (start 0) end key)
  (let* ((length (length sequence))
	 (end (or end length)))
    (if (listp sequence)
        (if from-end
            (list-count-if nil t test sequence)
            (list-count-if nil nil test sequence))
        (if from-end
            (vector-count-if nil t test sequence)
            (vector-count-if nil nil test sequence)))))

(defun count-if-not (test sequence &key from-end (start 0) end key)
  (let* ((length (length sequence))
	 (end (or end length)))
    (if (listp sequence)
        (if from-end
            (list-count-if t t test sequence)
            (list-count-if t nil test sequence))
        (if from-end
            (vector-count-if t t test sequence)
            (vector-count-if t nil test sequence)))))
