;;; debug.lisp
;;;
;;; Copyright (C) 2003-2007 Peter Graves
;;; $Id: debug.lisp,v 1.34 2007/02/23 10:07:06 piso Exp $
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

;;; Adapted from SBCL.

(in-package #:extensions)

(export '(*debug-condition* *debug-level* show-restarts))

(defvar *debug-condition* nil)

(defvar *debug-level* 0)

(in-package #:system)

(defun show-restarts (restarts stream)
  (when restarts
    (fresh-line stream)
    (%format stream "Restarts:~%")
    (let ((max-name-len 0))
      (dolist (restart restarts)
        (let ((name (restart-name restart)))
          (when name
            (let ((len (length (princ-to-string name))))
              (when (> len max-name-len)
                (setf max-name-len len))))))
      (let ((count 0))
        (dolist (restart restarts)
          (let ((name (restart-name restart))
                (report-function (restart-report-function restart)))
            (%format stream "  ~D: ~A" count name)
            (when (functionp report-function)
              (dotimes (i (1+ (- max-name-len (length (princ-to-string name)))))
                (write-char #\space stream))
              (funcall report-function stream))
            (terpri stream))
          (incf count))))))

(defun internal-debug ()
  (if (fboundp 'tpl::repl)
      (let* ((current-debug-io
              (if (typep *debug-io* 'synonym-stream)
                  (symbol-value (synonym-stream-symbol *debug-io*))
                  *debug-io*))
             (in (two-way-stream-input-stream current-debug-io))
             (out (two-way-stream-output-stream current-debug-io)))
        (loop
          (tpl::repl in out)))
      (quit)))

(defun debug-loop ()
  (let ((*debug-level* (1+ *debug-level*)))
    (show-restarts (compute-restarts) *debug-io*)
    (internal-debug)))

(defun invoke-debugger-report-condition (condition)
  (when condition
    (fresh-line *debug-io*)
    (with-standard-io-syntax
      (let ((*print-structure* nil))
        (when (and *load-truename* (streamp *load-stream*))
          (simple-format *debug-io*
                         "Error loading ~A at line ~D (offset ~D)~%"
                         *load-truename*
                         (stream-line-number *load-stream*)
                         (stream-offset *load-stream*)))
        (simple-format *debug-io*
                       (if (fboundp 'tpl::repl)
                           "Debugger invoked on condition of type ~A:~%"
                           "Unhandled condition of type ~A:~%")
                       (type-of condition))
        (simple-format *debug-io* "  ~A~%" condition)))))

(defun invoke-debugger (condition)
  (let ((*saved-backtrace* (backtrace-as-list)))
    (when *debugger-hook*
      (let ((hook-function *debugger-hook*)
            (*debugger-hook* nil))
        (funcall hook-function condition hook-function)))
    (invoke-debugger-report-condition condition)
    (unless (fboundp 'tpl::repl)
      (quit))
    (let ((original-package *package*))
      (with-standard-io-syntax
        (let ((*package* original-package)
              (*print-readably* nil) ; Top-level default.
              (*print-structure* nil)
              (*debug-condition* condition)
              (level *debug-level*))
          (clear-input)
          (if (> level 0)
              (with-simple-restart (abort "Return to debug level ~D." level)
                (debug-loop))
              (debug-loop)))))))

(defun break (&optional (format-control "BREAK called") &rest format-arguments)
  (let ((*debugger-hook* nil)) ; Specifically required by ANSI.
    (with-simple-restart (continue "Return from BREAK.")
      (invoke-debugger
       (%make-condition 'simple-condition
                        (list :format-control format-control
                              :format-arguments format-arguments))))
    nil))
