package mg.erpnext.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpSession;

@Controller
public class DashboardController {

    private static final Logger LOGGER = LoggerFactory.getLogger(DashboardController.class);
    private static final String CONTEXT_PATH = "/erpnext";

    @GetMapping("/dashboard")
    public ModelAndView showDashboard(HttpSession session) {
        LOGGER.info("Accessing /erpnext/dashboard for user in session: {}", session.getAttribute("user"));
        
        if (session.getAttribute("user") == null) {
            LOGGER.warn("No user in session, redirecting to /erpnext/login");
            return new ModelAndView("redirect:" + CONTEXT_PATH + "/login");
        }

        ModelAndView modelAndView = new ModelAndView("dashboard");
        LOGGER.debug("Rendering dashboard for user: {}", session.getAttribute("user"));
        return modelAndView;
    }
}