package mg.erpnext.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    public ModelAndView handleException(HttpServletRequest request, Exception ex) {
        LOGGER.error("Error processing request [{}]: {}", request.getRequestURI(), ex.getMessage(), ex);
        ModelAndView mav = new ModelAndView();
        mav.addObject("errorMessage", "Une erreur est survenue : " + ex.getMessage());
        mav.setViewName("error");
        return mav;
    }
}