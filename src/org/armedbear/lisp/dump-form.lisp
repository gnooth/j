;;; dump-form.lisp
;;;
;;; Copyright (C) 2004-2007 Peter Graves <peter@armedbear.org>
;;; $Id: dump-form.lisp,v 1.6 2007/05/31 19:11:33 piso Exp $
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
;;; Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.

(in-package "SYSTEM")

(export 'dump-form)

(declaim (ftype (function (cons stream) t) dump-cons))
(defun dump-cons (object stream)
  (cond ((and (eq (car object) 'QUOTE) (= (length object) 2))
         (%stream-write-char #\' stream)
         (dump-object (%cadr object) stream))
        (t
         (%stream-write-char #\( stream)
         (loop
           (dump-object (%car object) stream)
           (setf object (%cdr object))
           (when (null object)
             (return))
           (when (> (charpos stream) 80)
             (%stream-terpri stream))
           (%stream-write-char #\space stream)
           (when (atom object)
             (%stream-write-char #\. stream)
             (%stream-write-char #\space stream)
             (dump-object object stream)
             (return)))
         (%stream-write-char #\) stream))))

(declaim (ftype (function (t stream) t) dump-vector))
(defun dump-vector (object stream)
  (write-string "#(" stream)
  (let ((length (length object)))
    (when (> length 0)
      (dotimes (i (1- length))
        (declare (type index i))
        (dump-object (aref object i) stream)
        (when (> (charpos stream) 80)
          (%stream-terpri stream))
        (%stream-write-char #\space stream))
      (dump-object (aref object (1- length)) stream))
    (%stream-write-char #\) stream)))

(declaim (ftype (function (t stream) t) dump-instance))
(defun dump-instance (object stream)
  (multiple-value-bind (creation-form initialization-form)
      (make-load-form object)
    (write-string "#." stream)
    (if initialization-form
        (let* ((instance (gensym))
               load-form)
          (setf initialization-form
                (subst instance object initialization-form))
          (setf initialization-form
                (subst instance (list 'quote instance) initialization-form
                       :test #'equal))
          (setf load-form `(progn
                             (let ((,instance ,creation-form))
                               ,initialization-form
                               ,instance)))
          (dump-object load-form stream))
        (dump-object creation-form stream))))

(declaim (ftype (function (t stream) t) dump-object))
(defun dump-object (object stream)
  (cond ((consp object)
         (dump-cons object stream))
        ((stringp object)
         (%stream-output-object object stream))
        ((bit-vector-p object)
         (%stream-output-object object stream))
        ((vectorp object)
         (dump-vector object stream))
        ((or (structure-object-p object) ;; FIXME instance-p
             (standard-object-p object)
             (java:java-object-p object))
         (dump-instance object stream))
        (t
         (%stream-output-object object stream))))

(declaim (ftype (function (t stream) t) dump-form))
(defun dump-form (form stream)
  (let ((*print-fasl* t)
        (*print-level* nil)
        (*print-length* nil)
        (*print-circle* nil)
        (*print-structure* t))
    (dump-object form stream)))

(provide 'dump-form)
