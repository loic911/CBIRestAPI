package org.cbir.retrieval.service.exception;

import org.cbir.retrieval.web.rest.dto.ExceptionJSON;
import org.cbir.retrieval.web.rest.dto.StorageJSON;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by lrollus on 01/03/15.
 */
@ControllerAdvice
class GlobalDefaultExceptionHandler {
    public static final String DEFAULT_ERROR_VIEW = "error";

    @ExceptionHandler(value = CBIRException.class)
    public ResponseEntity<ExceptionJSON> defaultErrorHandler(HttpServletRequest req, Exception e) throws Exception {
        System.out.println("****************************************");
        System.out.println("****************************************");
        System.out.println("****************************************");
        System.out.println("*****************"+e);
        System.out.println("****************************************");
        System.out.println("****************************************");
        System.out.println("****************************************");

        // If the exception is annotated with @ResponseStatus rethrow it and let
        // the framework handle it - like the OrderNotFoundException example
        // at the start of this post.
        // AnnotationUtils is a Spring Framework utility class.
//        if (AnnotationUtils.findAnnotation(e.getClass(), ResponseStatus.class) != null)
//            throw e;

        // Otherwise setup and send the user to a default error-view.
        if(e instanceof CBIRException) {
            return new ResponseEntity<ExceptionJSON>(new ExceptionJSON((CBIRException)e), ((CBIRException) e).getStatus());
        } else {
            throw e;
        }

    }
}
