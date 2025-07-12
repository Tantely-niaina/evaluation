package mg.erpnext.model;

import java.util.ArrayList;
import java.util.List;

public class ParseResult<T> {
    private List<T> validItems = new ArrayList<>();
    private List<String> errors = new ArrayList<>();

    public List<T> getValidItems() {
        return validItems;
    }

    public void addItem(T item) {
        validItems.add(item);
    }

    public List<String> getErrors() {
        return errors;
    }

    public void addError(String error) {
        errors.add(error);
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }
}

