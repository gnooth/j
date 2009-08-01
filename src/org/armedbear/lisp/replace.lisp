;;; replace.lisp
;;;
;;; Copyright (C) 2003-2005 Peter Graves
;;; $Id: replace.lisp,v 1.5 2005/07/30 17:52:19 piso Exp $
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

(in-package #:system)

(eval-when (:compile-toplevel :load-toplevel :execute)
  (defmacro seq-dispatch (sequence list-form array-form)
    `(if (listp ,sequence)
         ,list-form
         ,array-form)))

(eval-when (:compile-toplevel :execute)

  ;;; If we are copying around in the same vector, be careful not to copy the
  ;;; same elements over repeatedly.  We do this by copying backwards.
  (defmacro mumble-replace-from-mumble ()
    `(if (and (eq target-sequence source-sequence) (> target-start source-start))
         (let ((nelts (min (- target-end target-start) (- source-end source-start))))
           (do ((target-index (+ (the fixnum target-start) (the fixnum nelts) -1)
                              (1- target-index))
                (source-index (+ (the fixnum source-start) (the fixnum nelts) -1)
                              (1- source-index)))
               ((= target-index (the fixnum (1- target-start))) target-sequence)
             (declare (fixnum target-index source-index))
             (setf (aref target-sequence target-index)
                   (aref source-sequence source-index))))
         (do ((target-index target-start (1+ target-index))
              (source-index source-start (1+ source-index)))
             ((or (= target-index (the fixnum target-end))
                  (= source-index (the fixnum source-end)))
              target-sequence)
           (declare (fixnum target-index source-index))
           (setf (aref target-sequence target-index)
                 (aref source-sequence source-index)))))

  (defmacro list-replace-from-list ()
    `(if (and (eq target-sequence source-sequence) (> target-start source-start))
         (let ((new-elts (subseq source-sequence source-start
                                 (+ (the fixnum source-start)
                                    (the fixnum
                                         (min (- (the fixnum target-end)
                                                 (the fixnum target-start))
                                              (- (the fixnum source-end)
                                                 (the fixnum source-start))))))))
           (do ((n new-elts (cdr n))
                (o (nthcdr target-start target-sequence) (cdr o)))
               ((null n) target-sequence)
             (rplaca o (car n))))
         (do ((target-index target-start (1+ target-index))
              (source-index source-start (1+ source-index))
              (target-sequence-ref (nthcdr target-start target-sequence)
                                   (cdr target-sequence-ref))
              (source-sequence-ref (nthcdr source-start source-sequence)
                                   (cdr source-sequence-ref)))
             ((or (= target-index (the fixnum target-end))
                  (= source-index (the fixnum source-end))
                  (null target-sequence-ref) (null source-sequence-ref))
              target-sequence)
           (declare (fixnum target-index source-index))
           (rplaca target-sequence-ref (car source-sequence-ref)))))

  (defmacro list-replace-from-mumble ()
    `(do ((target-index target-start (1+ target-index))
          (source-index source-start (1+ source-index))
          (target-sequence-ref (nthcdr target-start target-sequence)
                               (cdr target-sequence-ref)))
         ((or (= target-index (the fixnum target-end))
              (= source-index (the fixnum source-end))
              (null target-sequence-ref))
          target-sequence)
       (declare (fixnum source-index target-index))
       (rplaca target-sequence-ref (aref source-sequence source-index))))

  (defmacro mumble-replace-from-list ()
    `(do ((target-index target-start (1+ target-index))
          (source-index source-start (1+ source-index))
          (source-sequence (nthcdr source-start source-sequence)
                           (cdr source-sequence)))
         ((or (= target-index (the fixnum target-end))
              (= source-index (the fixnum source-end))
              (null source-sequence))
          target-sequence)
       (declare (fixnum target-index source-index))
       (setf (aref target-sequence target-index) (car source-sequence))))

  ) ; eval-when

;;; The support routines for REPLACE are used by compiler transforms, so we
;;; worry about dealing with end being supplied as or defaulting to nil
;;; at this level.

(defun list-replace-from-list* (target-sequence source-sequence target-start
                                                target-end source-start source-end)
  (when (null target-end) (setq target-end (length target-sequence)))
  (when (null source-end) (setq source-end (length source-sequence)))
  (list-replace-from-list))

(defun list-replace-from-vector* (target-sequence source-sequence target-start
                                                  target-end source-start source-end)
  (when (null target-end) (setq target-end (length target-sequence)))
  (when (null source-end) (setq source-end (length source-sequence)))
  (list-replace-from-mumble))

(defun vector-replace-from-list* (target-sequence source-sequence target-start
                                                  target-end source-start source-end)
  (when (null target-end) (setq target-end (length target-sequence)))
  (when (null source-end) (setq source-end (length source-sequence)))
  (mumble-replace-from-list))

(defun vector-replace-from-vector* (target-sequence source-sequence
                                                    target-start target-end source-start
                                                    source-end)
  (when (null target-end) (setq target-end (length target-sequence)))
  (when (null source-end) (setq source-end (length source-sequence)))
  (mumble-replace-from-mumble))

(defun %replace (target-sequence source-sequence target-start target-end source-start source-end)
  (declare (type (integer 0 #.most-positive-fixnum) target-start target-end source-start source-end))
  (seq-dispatch target-sequence
                (seq-dispatch source-sequence
                              (list-replace-from-list)
                              (list-replace-from-mumble))
                (seq-dispatch source-sequence
                              (mumble-replace-from-list)
                              (mumble-replace-from-mumble))))

;;; REPLACE cannot default end arguments to the length of sequence since it
;;; is not an error to supply nil for their values.  We must test for ends
;;; being nil in the body of the function.
(defun replace (target-sequence source-sequence &key
                                ((:start1 target-start) 0)
                                ((:end1 target-end))
                                ((:start2 source-start) 0)
                                ((:end2 source-end)))
  "The target sequence is destructively modified by copying successive
elements into it from the source sequence."
  (let ((target-end (or target-end (length target-sequence)))
	(source-end (or source-end (length source-sequence))))
    (%replace target-sequence source-sequence target-start target-end source-start source-end)))
