package mg.erpnext.controller;

import org.springframework.http.ResponseEntity;
import mg.erpnext.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import mg.erpnext.service.AuthService.AuthResult;import mg.erpnext.model.LoginRequest;
import mg.erpnext.model.LoginResponse;
import jakarta.servlet.http.HttpSession;

@Controller
public class LoginController {

    private static final String CONTEXT_PATH = "/erpnext";

    @Autowired
    private AuthService authService;

    @GetMapping("/login")
    public String showLoginPage(HttpSession session) {
        // Si l'utilisateur est déjà connecté, rediriger vers le dashboard
        if (session.getAttribute("user") != null) {
            return "redirect:" + CONTEXT_PATH + "/dashboard";
        }
        return "login";
    }

    @PostMapping("/api/login")
    @ResponseBody
    public LoginResponse login(@RequestBody LoginRequest loginRequest, HttpSession session) {
        try {
            AuthResult authResult = authService.authenticate(loginRequest);
            ResponseEntity<String> response = authResult.getResponse();

            if (response.getStatusCode().is2xxSuccessful() && authResult.getSid() != null) {
                session.setAttribute("user", loginRequest.getUsr());
                session.setAttribute("frappe_sid", authResult.getSid());
                return new LoginResponse("Connexion réussie", CONTEXT_PATH + "/dashboard");
            } else {
                return new LoginResponse("Échec de la connexion: identifiants invalides", null);
            }
        } catch (Exception e) {
            return new LoginResponse("Erreur lors de la connexion: " + e.getMessage(), null);
        }
    }
}