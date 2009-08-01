;;; write-sequence.lisp
;;;
;;; Copyright (C) 2004-2005 Peter Graves
;;; $Id: write-sequence.lisp,v 1.9 2005/08/14 23:21:35 piso Exp $
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

(defun write-sequence (sequence stream &key (start 0) end)
  (declare (type stream stream))
  (declare (type index start))
  (unless (>= start 0)
    (error 'simple-type-error
           :datum start
           :expected-type '(integer 0)))
  (if end
      (unless (and (integerp end) (>= end 0))
        (error 'simple-type-error
               :datum end
               :expected-type '(integer 0)))
      (setf end (length sequence)))
  (let ((end (the fixnum end))
        (stream-element-type (stream-element-type stream)))
    (cond ((eq stream-element-type 'character)
           (if (stringp sequence)
               (%write-string sequence stream start end)
               (do* ((i start (1+ i)))
                    ((>= i end) sequence)
                 (declare (type index i))
                 (write-char (elt sequence i) stream))))
          ((equal stream-element-type '(unsigned-byte 8))
           (if (and (vectorp sequence)
                    (equal (array-element-type sequence) '(unsigned-byte 8)))
               (write-vector-unsigned-byte-8 sequence stream start end)
               (do* ((i start (1+ i)))
                    ((>= i end) sequence)
                 (declare (type index i))
                 (write-8-bits (elt sequence i) stream))))
          (t
           (do* ((i start (1+ i)))
                ((>= i end) sequence)
             (declare (type index i))
             (write-byte (elt sequence i) stream)))))
  sequence)
