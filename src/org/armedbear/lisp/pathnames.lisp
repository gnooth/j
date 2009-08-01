;;; pathnames.lisp
;;;
;;; Copyright (C) 2003-2007 Peter Graves
;;; $Id: pathnames.lisp,v 1.29 2007/03/04 17:44:53 piso Exp $
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

(export '(logical-host-p))

(defun pathname-host (pathname &key (case :local))
  (%pathname-host pathname case))

(defun pathname-device (pathname &key (case :local))
  (%pathname-device pathname case))

(defun pathname-directory (pathname &key (case :local))
  (%pathname-directory pathname case))

(defun pathname-name (pathname &key (case :local))
  (%pathname-name pathname case))

(defun pathname-type (pathname &key (case :local))
  (%pathname-type pathname case))

(defun wild-pathname-p (pathname &optional field-key)
  (%wild-pathname-p pathname field-key))

(defun component-match-p (thing wild ignore-case)
  (cond ((eq wild :wild)
         t)
        ((null wild)
         t)
        ((and (stringp wild) (position #\* wild))
         (error "Unsupported wildcard pattern: ~S" wild))
        (ignore-case
         (equalp thing wild))
        (t
         (equal thing wild))))

(defun directory-match-components (thing wild ignore-case)
  (loop
    (cond ((endp thing)
           (return (or (endp wild) (equal wild '(:wild-inferiors)))))
          ((endp wild)
           (return nil)))
    (let ((x (car thing))
          (y (car wild)))
      (when (eq y :wild-inferiors)
        (return t))
      (unless (component-match-p x y ignore-case)
        (return nil))
      (setf thing (cdr thing)
            wild  (cdr wild)))))

(defun directory-match-p (thing wild ignore-case)
  (cond ((eq wild :wild)
         t)
        ((null wild)
         t)
        ((and ignore-case (equalp thing wild))
         t)
        ((equal thing wild)
         t)
        ((and (null thing) (equal wild '(:absolute :wild-inferiors)))
         t)
        ((and (consp thing) (consp wild))
         (if (eq (%car thing) (%car wild))
             (directory-match-components (%cdr thing) (%cdr wild) ignore-case)
             nil))
        (t
         nil)))

(defun pathname-match-p (pathname wildcard)
  (setf pathname (pathname pathname)
        wildcard (pathname wildcard))
  (unless (component-match-p (pathname-host pathname) (pathname-host wildcard) nil)
    (return-from pathname-match-p nil))
  (let* ((windows-p (featurep :windows))
         (ignore-case (or windows-p (typep pathname 'logical-pathname))))
    (cond ((and windows-p
                (not (component-match-p (pathname-device pathname)
                                        (pathname-device wildcard)
                                        ignore-case)))
           nil)
          ((not (directory-match-p (pathname-directory pathname)
                                   (pathname-directory wildcard)
                                   ignore-case))
           nil)
          ((not (component-match-p (pathname-name pathname)
                                   (pathname-name wildcard)
                                   ignore-case))
           nil)
          ((not (component-match-p (pathname-type pathname)
                                   (pathname-type wildcard)
                                   ignore-case))
           nil)
          (t
           t))))

(defun wild-p (component)
  (or (eq component :wild)
      (and (stringp component)
           (position #\* component))))

(defun casify (thing case)
  (typecase thing
    (string
     (case case
       (:upcase (string-upcase thing))
       (:downcase (string-downcase thing))
       (t thing)))
    (list
     (let (result)
       (dolist (component thing (nreverse result))
         (push (casify component case) result))))
    (t
     thing)))

(defun split-directory-components (directory)
  (declare (optimize safety))
  (declare (type list directory))
  (unless (memq (car directory) '(:absolute :relative))
    (error "Ill-formed directory list: ~S" directory))
  (let (result sublist)
    (push (car directory) result)
    (dolist (component (cdr directory))
      (cond ((memq component '(:wild :wild-inferiors))
             (when sublist
               (push (nreverse sublist) result)
               (setf sublist nil))
             (push component result))
            (t
             (push component sublist))))
    (when sublist
      (push (nreverse sublist) result))
    (nreverse result)))

(defun translate-component (source from to &optional case)
  (declare (ignore from))
  (cond ((or (eq to :wild) (null to))
         ;; "If the piece in TO-WILDCARD is :WILD or NIL, the piece in source
         ;; is copied into the result."
         (casify source case))
        ((and to (not (wild-p to)))
        ;; "If the piece in TO-WILDCARD is present and not wild, it is copied
        ;; into the result."
         to)
        (t
         ;; "Otherwise, the piece in TO-WILDCARD might be a complex wildcard
         ;; such as "foo*bar" and the piece in FROM-WILDCARD should be wild;
         ;; the portion of the piece in SOURCE that matches the wildcard
         ;; portion of the piece in FROM-WILDCARD replaces the wildcard portion
         ;; of the piece in TO-WILDCARD and the value produced is used in the
         ;; result."
         ;; FIXME
         (error "Unsupported wildcard pattern: ~S" to))))

(defun translate-directory-components (source from to case)
  (cond ((null to)
         nil
         )
        ((memq (car to) '(:absolute :relative))
         (cons (car to)
               (translate-directory-components (cdr source) (cdr from) (cdr to) case))
         )
        ((eq (car to) :wild)
         (if (eq (car from) :wild)
             ;; Grab the next chunk from SOURCE.
             (append (casify (car source) case)
                     (translate-directory-components (cdr source) (cdr from) (cdr to) case))
             (error "Unsupported case 1: ~S ~S ~S" source from to))
         )
        ((eq (car to) :wild-inferiors)
         ;; Grab the next chunk from SOURCE.
         (append (casify (car source) case)
                 (translate-directory-components (cdr source) (cdr from) (cdr to) case))
         )
        (t
         ;; "If the piece in TO-WILDCARD is present and not wild, it is copied
         ;; into the result."
         (append (casify (car to) case)
                 (translate-directory-components source from (cdr to) case))
         )
        ))

(defun translate-directory (source from to case)
  ;; FIXME The IGNORE-CASE argument to DIRECTORY-MATCH-P should not be nil on
  ;; Windows or if the source pathname is a logical pathname.
  ;; FIXME We can canonicalize logical pathnames to upper case, so we only need
  ;; IGNORE-CASE for Windows.
  (cond ((null source)
         to)
        ((equal source '(:absolute))
         (remove :wild-inferiors to))
        (t
         (translate-directory-components (split-directory-components source)
                                         (split-directory-components from)
                                         (split-directory-components to)
                                         case))))

;; "The resulting pathname is TO-WILDCARD with each wildcard or missing field
;; replaced by a portion of SOURCE."
(defun translate-pathname (source from-wildcard to-wildcard &key)
  (unless (pathname-match-p source from-wildcard)
    (error "~S and ~S do not match." source from-wildcard))
  (let* ((source (pathname source))
         (from   (pathname from-wildcard))
         (to     (pathname to-wildcard))
         (device (if (typep 'to 'logical-pathname)
                     :unspecific
                     (translate-component (pathname-device source)
                                          (pathname-device from)
                                          (pathname-device to))))
         (case   (and (typep source 'logical-pathname)
                      (or (featurep :unix) (featurep :windows))
                      :downcase)))
    (make-pathname :host      (pathname-host to)
                   :device    (cond ((typep to 'logical-pathname)
                                     :unspecific)
                                    ((eq device :unspecific)
                                     nil)
                                    (t
                                     device))
                   :directory (translate-directory (pathname-directory source)
                                                   (pathname-directory from)
                                                   (pathname-directory to)
                                                   case)
                   :name      (translate-component (pathname-name source)
                                                   (pathname-name from)
                                                   (pathname-name to)
                                                   case)
                   :type      (translate-component (pathname-type source)
                                                   (pathname-type from)
                                                   (pathname-type to)
                                                   case)
                   :version   (if (null (pathname-host from))
                                  (if (eq (pathname-version to) :wild)
                                      (pathname-version from)
                                      (pathname-version to))
                                  (translate-component (pathname-version source)
                                                       (pathname-version from)
                                                       (pathname-version to))))))

(defun logical-host-p (canonical-host)
  (multiple-value-bind (translations present)
      (gethash canonical-host *logical-pathname-translations*)
    (declare (ignore translations))
    present))

(defun logical-pathname-translations (host)
  (multiple-value-bind (translations present)
      (gethash (canonicalize-logical-host host) *logical-pathname-translations*)
    (unless present
      (error 'type-error
             :datum host
             :expected-type '(and string (satisfies logical-host-p))))
    translations))

(defun canonicalize-logical-pathname-translations (translations host)
  (let (result)
    (dolist (translation translations (nreverse result))
      (let ((from (car translation))
            (to (cadr translation)))
        (push (list (if (typep from 'logical-pathname)
                        from
                        (parse-namestring from host))
                    (pathname to))
              result)))))

(defun %set-logical-pathname-translations (host translations)
  (setf host (canonicalize-logical-host host))
  ;; Avoid undefined host error in CANONICALIZE-LOGICAL-PATHNAME-TRANSLATIONS.
  (unless (logical-host-p host)
    (setf (gethash host *logical-pathname-translations*) nil))
  (setf (gethash host *logical-pathname-translations*)
        (canonicalize-logical-pathname-translations translations host)))

(defsetf logical-pathname-translations %set-logical-pathname-translations)

(defun translate-logical-pathname (pathname &key)
  (typecase pathname
    (logical-pathname
     (let* ((host (pathname-host pathname))
            (translations (logical-pathname-translations host)))
       (dolist (translation translations
                            (error 'file-error
                                   :pathname pathname
                                   :format-control "No translation for ~S"
                                   :format-arguments (list pathname)))
         (let ((from-wildcard (car translation))
               (to-wildcard (cadr translation)))
           (when (pathname-match-p pathname from-wildcard)
             (return (translate-logical-pathname
                      (translate-pathname pathname from-wildcard to-wildcard))))))))
    (pathname pathname)
    (t
     (translate-logical-pathname (pathname pathname)))))

(defun load-logical-pathname-translations (host)
  (declare (type string host))
  (multiple-value-bind (ignore found)
      (gethash (canonicalize-logical-host host)
               *logical-pathname-translations*)
    (declare (ignore ignore))
    (unless found
      (error "The logical host ~S was not found." host))))

(defun logical-pathname (pathspec)
  (typecase pathspec
    (logical-pathname pathspec)
    (string
     (%make-logical-pathname pathspec))
    (stream
     (let ((result (pathname pathspec)))
       (if (typep result 'logical-pathname)
           result
           (error 'simple-type-error
                  :datum result
                  :expected-type 'logical-pathname))))
    (t
     (error 'type-error
            :datum pathspec
            :expected-type '(or logical-pathname string stream)))))

(defun parse-namestring (thing
                         &optional host (default-pathname *default-pathname-defaults*)
                         &key (start 0) end junk-allowed)
  (declare (ignore junk-allowed)) ; FIXME
  (cond ((eq host :unspecific)
         (setf host nil))
        (host
         (setf host (canonicalize-logical-host host))))
  (typecase thing
    (stream
     (values (pathname thing) start))
    (pathname
     (values thing start))
    (string
     (unless end
       (setf end (length thing)))
     (%parse-namestring (subseq thing start end) host default-pathname))
    (t
     (error 'type-error
            :format-control "~S cannot be converted to a pathname."
            :format-arguments (list thing)))))
