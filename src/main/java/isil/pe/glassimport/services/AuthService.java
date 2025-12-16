package isil.pe.glassimport.services;

import isil.pe.glassimport.dto.request.RegisterRequest;
import isil.pe.glassimport.dto.response.AuthResponse;
import isil.pe.glassimport.dto.response.JwtPayload;
import isil.pe.glassimport.entity.User;
import isil.pe.glassimport.repository.UserRepository;
import isil.pe.glassimport.utils.CookiesUtil;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuthService {
    @Autowired
    private JwtService jwtService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CookiesUtil cookiesUtil;

    public AuthResponse login(HttpServletResponse res, String username, String password) {

        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        System.out.println(userDetails.getUsername());



        User user = userRepository.findByEmailOrUsername(username,userDetails.getUsername()).orElseThrow(()-> new UsernameNotFoundException("User not found"));

        JwtPayload jwtPayload = new JwtPayload(user.getId().toString(),user.getEmail(), List.of());

        long accessTokenExpiration = 7 * 24 * 60 * 60; //15min
        long refreshTokenExpiration = 7 * 24 * 60 * 60; //7 dias

        String accessToken = jwtService.generateToken(jwtPayload,accessTokenExpiration);

        String refreshToken = jwtService.generateToken(jwtPayload, refreshTokenExpiration);


        cookiesUtil.setCookie(res, "accessToken", accessToken,  (int) accessTokenExpiration * 60, null);

        cookiesUtil.setCookie(res, "refreshToken", refreshToken, (int) refreshTokenExpiration * 60, null);

        return AuthResponse.fromUser(user,accessToken);

    }


    public AuthResponse register(HttpServletResponse res, RegisterRequest request) {

        User user = new User();

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        user.setEmail(request.email());
        user.setUsername(request.username());
        user.setPassword(encoder.encode(request.password()));

        userRepository.save(user);
        return login(res, request.email(), request.password());
    }
}
