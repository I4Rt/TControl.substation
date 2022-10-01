package com.i4rt.temperaturecontrol.controllers.controllerAdvice;

import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

@ControllerAdvice
public class MainExceptionController {

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ModelAndView handleError405(HttpServletRequest request, Exception e) {
        ModelAndView mav = new ModelAndView("/403");
        mav.addObject("exception", e);
        return mav;
    }
}
