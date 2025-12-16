package isil.pe.glassimport.config;


import com.auth0.jwt.JWT;
import isil.pe.glassimport.services.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthorization extends OncePerRequestFilter {
    @Autowired
    private JwtService jwtService;
    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        //1.- Obtener el jwt de las cookies

        String accessToken = extractToken(request, "accessToken");
//        String refreshToken = extractToken(request, "refreshToken");

        //2.- Validar si el token es valido

        if(accessToken != null && jwtService.validateToken(accessToken)){

            //3.- Extraer el email
            String email = JWT.decode(accessToken).getClaim("email").asString();


            //4.- buscar el usuario por email o usuario, en la clase AuthenticationManager
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);


            //5.- Setear el objeto Authentcation dentro del SecurityContext
            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                SecurityContextHolder.getContext().setAuthentication(authToken);
            }

        }


        filterChain.doFilter(request, response);


    }



    private String extractToken(HttpServletRequest request, String cookieName){
        Cookie[] cookies = request.getCookies();
        if(cookies != null){
            for(Cookie cookie : cookies){
                if(cookie.getName().equals(cookieName)){
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}
