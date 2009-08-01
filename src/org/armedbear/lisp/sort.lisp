;;; sort.lisp
;;;
;;; Copyright (C) 2003-2005 Peter Graves
;;; $Id: sort.lisp,v 1.8 2005/12/30 16:55:24 piso Exp $
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

(defun sort (sequence predicate &key key)
  (if (listp sequence)
      (sort-list sequence predicate key)
      (quick-sort sequence 0 (length sequence) predicate key)))

(defun stable-sort (sequence predicate &key key)
  (if (listp sequence)
      (sort-list sequence predicate key)
      (quick-sort sequence 0 (length sequence) predicate key)))

;; Adapted from SBCL.
(declaim (ftype (function (list) cons) last-cons-of))
(defun last-cons-of (list)
  (loop
    (let ((rest (rest list)))
      (if rest
          (setf list rest)
          (return list)))))

;; Adapted from OpenMCL.
(defun merge-lists (list1 list2 pred key)
  (declare (optimize (speed 3) (safety 0)))
  (if (null key)
      (merge-lists-no-key list1 list2 pred)
      (cond ((null list1)
             (values list2 (last-cons-of list2)))
            ((null list2)
             (values list1 (last-cons-of list1)))
            (t
             (let* ((result (cons nil nil))
                    (p result)               ; p points to last cell of result
                    (key1 (funcall key (car list1)))
                    (key2 (funcall key (car list2))))
               (declare (type list p))
               (loop
                 (cond ((funcall pred key2 key1)
                        (rplacd p list2)     ; append the lesser list to last cell of
                        (setf p (cdr p))     ;   result.  Note: test must bo done for
                        (pop list2)          ;   list2 < list1 so merge will be
                        (unless list2        ;   stable for list1
                          (rplacd p list1)
                          (return (values (cdr result) (last-cons-of p))))
                        (setf key2 (funcall key (car list2))))
                       (t
                        (rplacd p list1)
                        (setf p (cdr p))
                        (pop list1)
                        (unless list1
                          (rplacd p list2)
                          (return (values (cdr result) (last-cons-of p))))
                        (setf key1 (funcall key (car list1)))))))))))

(defun merge-lists-no-key (list1 list2 pred)
  (declare (optimize (speed 3) (safety 0)))
  (cond ((null list1)
         (values list2 (last-cons-of list2)))
        ((null list2)
         (values list1 (last-cons-of list1)))
        (t
         (let* ((result (cons nil nil))
                (p result)                   ; p points to last cell of result
                (key1 (car list1))
                (key2 (car list2)))
           (declare (type list p))
           (loop
             (cond ((funcall pred key2 key1)
                    (rplacd p list2)         ; append the lesser list to last cell of
                    (setf p (cdr p))         ;   result.  Note: test must bo done for
                    (pop list2)              ;   list2 < list1 so merge will be
                    (unless list2            ;   stable for list1
                      (rplacd p list1)
                      (return (values (cdr result) (last-cons-of p))))
                    (setf key2 (car list2)))
                   (t
                    (rplacd p list1)
                    (setf p (cdr p))
                    (pop list1)
                    (unless list1
                      (rplacd p list2)
                      (return (values (cdr result) (last-cons-of p))))
                    (setf key1 (car list1)))))))))

;;; SORT-LIST uses a bottom up merge sort.  First a pass is made over
;;; the list grabbing one element at a time and merging it with the next one
;;; form pairs of sorted elements.  Then n is doubled, and elements are taken
;;; in runs of two, merging one run with the next to form quadruples of sorted
;;; elements.  This continues until n is large enough that the inner loop only
;;; runs for one iteration; that is, there are only two runs that can be merged,
;;; the first run starting at the beginning of the list, and the second being
;;; the remaining elements.

(defun sort-list (list pred key)
  (when (or (eq key #'identity) (eq key 'identity))
    (setf key nil))
  (let ((head (cons nil list)) ; head holds on to everything
        (n 1)                  ; bottom-up size of lists to be merged
        unsorted               ; unsorted is the remaining list to be
                               ;   broken into n size lists and merged
        list-1                 ; list-1 is one length n list to be merged
        last                   ; last points to the last visited cell
        )
    (declare (type fixnum n))
    (loop
      ;; start collecting runs of n at the first element
      (setf unsorted (cdr head))
      ;; tack on the first merge of two n-runs to the head holder
      (setf last head)
      (let ((n-1 (1- n)))
        (declare (type fixnum n-1))
        (loop
          (setf list-1 unsorted)
          (let ((temp (nthcdr n-1 list-1))
                list-2)
            (cond (temp
                   ;; there are enough elements for a second run
                   (setf list-2 (cdr temp))
                   (setf (cdr temp) nil)
                   (setf temp (nthcdr n-1 list-2))
                   (cond (temp
                          (setf unsorted (cdr temp))
                          (setf (cdr temp) nil))
                         ;; the second run goes off the end of the list
                         (t (setf unsorted nil)))
                   (multiple-value-bind (merged-head merged-last)
                       (merge-lists list-1 list-2 pred key)
                     (setf (cdr last) merged-head)
                     (setf last merged-last))
                   (if (null unsorted) (return)))
                  ;; if there is only one run, then tack it on to the end
                  (t (setf (cdr last) list-1)
                     (return)))))
        (setf n (+ n n))
        ;; If the inner loop only executed once, then there were only enough
        ;; elements for two runs given n, so all the elements have been merged
        ;; into one list.  This may waste one outer iteration to realize.
        (if (eq list-1 (cdr head))
            (return list-1))))))

;;; From ECL.
(defun quick-sort (seq start end pred key)
  (unless key (setq key #'identity))
  (if (<= end (1+ start))
      seq
      (let* ((j start) (k end) (d (elt seq start)) (kd (funcall key d)))
        (block outer-loop
          (loop (loop (decf k)
                  (unless (< j k) (return-from outer-loop))
                  (when (funcall pred (funcall key (elt seq k)) kd)
                    (return)))
            (loop (incf j)
              (unless (< j k) (return-from outer-loop))
              (unless (funcall pred (funcall key (elt seq j)) kd)
                (return)))
            (let ((temp (elt seq j)))
              (setf (elt seq j) (elt seq k)
                    (elt seq k) temp))))
        (setf (elt seq start) (elt seq j)
              (elt seq j) d)
        (quick-sort seq start j pred key)
        (quick-sort seq (1+ j) end pred key))))

;;; From ECL.
(defun merge (result-type sequence1 sequence2 predicate
                          &key key
                          &aux (l1 (length sequence1)) (l2 (length sequence2)))
  (unless key (setq key #'identity))
  (do ((newseq (make-sequence result-type (+ l1 l2)))
       (j 0 (1+ j))
       (i1 0)
       (i2 0))
    ((and (= i1 l1) (= i2 l2)) newseq)
    (cond ((and (< i1 l1) (< i2 l2))
           (cond ((funcall predicate
                           (funcall key (elt sequence1 i1))
                           (funcall key (elt sequence2 i2)))
                  (setf (elt newseq j) (elt sequence1 i1))
                  (incf i1))
                 ((funcall predicate
                           (funcall key (elt sequence2 i2))
                           (funcall key (elt sequence1 i1)))
                  (setf (elt newseq j) (elt sequence2 i2))
                  (incf i2))
                 (t
                  (setf (elt newseq j) (elt sequence1 i1))
                  (incf i1))))
          ((< i1 l1)
           (setf (elt newseq j) (elt sequence1 i1))
           (incf i1))
          (t
           (setf (elt newseq j) (elt sequence2 i2))
           (incf i2)))))
