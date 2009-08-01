;;; swank-package.lisp
;;;
;;; Copyright (C) 2004 Peter Graves
;;; $Id: swank-package.lisp,v 1.11 2006/12/11 04:01:40 piso Exp $
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

(defpackage #:swank
  (:use #:cl)
  (:export #:start-server
           #:completion-set
           #:arglist-for-echo-area
           #:find-definitions-for-function-name
           #:eval-region
           #:eval-string-async
           #:swank-load-file
           #:swank-compile-file
           #:swank-compile-string
           #:swank-describe-symbol
           #:swank-interrupt-lisp))
