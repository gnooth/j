;;; reduce.lisp
;;;
;;; Copyright (C) 2003-2005 Peter Graves
;;; $Id: reduce.lisp,v 1.3 2005/08/23 12:21:10 piso Exp $
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

;;; Adapted from OpenMCL.

(in-package #:system)

(defmacro list-reduce (function sequence start end initial-value ivp key)
  (let ((what `(if ,key (funcall ,key (car sequence)) (car sequence))))
    `(let ((sequence (nthcdr ,start ,sequence)))
       (do ((count (if ,ivp ,start (1+ ,start)) (1+ count))
            (sequence (if ,ivp sequence (cdr sequence))
                      (cdr sequence))
            (value (if ,ivp ,initial-value ,what)
                   (funcall ,function value ,what)))
           ((= count ,end) value)))))


(defmacro list-reduce-from-end (function sequence start end
                                         initial-value ivp key)
  (let ((what `(if ,key (funcall ,key (car sequence)) (car sequence))))
    `(let ((sequence (nthcdr (- (length ,sequence) ,end) (reverse ,sequence))))
       (do ((count (if ,ivp ,start (1+ ,start)) (1+ count))
            (sequence (if ,ivp sequence (cdr sequence))
                      (cdr sequence))
            (value (if ,ivp ,initial-value ,what)
                   (funcall ,function ,what value)))
           ((= count ,end) value)))))


(defun reduce (function sequence &key from-end (start 0)
                        end (initial-value nil ivp) key)
  (unless end (setq end (length sequence)))
  (if (= end start)
      (if ivp initial-value (funcall function))
      (if (listp sequence)
          (if from-end
              (list-reduce-from-end function sequence start end initial-value ivp key)
              (list-reduce function sequence start end initial-value ivp key))
          (let* ((disp (if from-end -1 1))
                 (index (if from-end (1- end) start))
                 (terminus (if from-end (1- start) end))
                 (value (if ivp initial-value
                            (let ((elt (aref sequence index)))
                              (setf index (+ index disp))
                              (if key (funcall key elt) elt))))
                 (element nil))
            (do* ()
                 ((= index terminus) value)
              (setf element (aref sequence index)
                    index (+ index disp)
                    element (if key (funcall key element) element)
                    value (funcall function
                                   (if from-end element value)
                                   (if from-end value element))))))))
