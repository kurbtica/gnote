package org.openjfx.sio2E4.util;

import org.openjfx.sio2E4.model.User;

public class UserValidator {

    public static boolean validateEmail(String email) {
        return email.matches("^[\\w\\d._%+-]+@[\\w\\d.-]+\\.[A-Za-z]{2,}$");
    }

    public static boolean validatePhone(String phone) {
        return phone.matches("^(0|\\+33)[1-9](\\d{2}){4}$");
    }

    public static boolean validateUser(User user) {
        return !user.getNom().isEmpty()
                && !user.getPrenom().isEmpty()
                && !user.getEmail().isEmpty() && validateEmail(user.getEmail())
                && !user.getTelephone().isEmpty() && validatePhone(user.getTelephone())
                && !(user.getRole() == null);
    }
}
