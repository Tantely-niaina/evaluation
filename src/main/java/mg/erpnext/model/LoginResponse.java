package mg.erpnext.model;

public class LoginResponse {
    private String message;
    private String redirect;

    public LoginResponse(String message, String redirect) {
        this.message = message;
        this.redirect = redirect;
    }

    public String getMessage() {
        return message;
    }

    public String getRedirect() {
        return redirect;
    }
}
