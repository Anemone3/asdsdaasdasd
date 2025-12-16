package isil.pe.glassimport.utils;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

@Component
public class CookiesUtil {


    public void setCookie(HttpServletResponse response, String key, String value, int maxAge, String domain) {
        Cookie cookie = new Cookie(key, value); // key: "accessToken" value: "eyykasdkadk"
        boolean isProduction = domain != null && !domain.contains("localhost");
        //Parameters
        cookie.setMaxAge(maxAge);
        cookie.setSecure(isProduction); // true en Producción para peticiones en HTTPS
        cookie.setHttpOnly(true); // Podría manejar con una variable local

        if (domain != null && !domain.isEmpty()) {
            cookie.setDomain(domain); // También, la URL del frontend
        }

        cookie.setPath("/");

        response.addCookie(cookie);

    }


    public void deleteCookie(HttpServletResponse response, String key) {
        Cookie cookie = new Cookie(key, null);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }
}