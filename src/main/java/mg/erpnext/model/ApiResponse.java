package mg.erpnext.model;

public class ApiResponse<T> {
    private T data;
    private String error;
    private boolean success;

    public ApiResponse() {
        this.success = true;
    }

    public ApiResponse(T data) {
        this.data = data;
        this.success = true;
    }

    public ApiResponse(String error) {
        this.error = error;
        this.success = false;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}