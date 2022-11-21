package com.example.jwt.config.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.example.jwt.config.auth.PrincipalDetails;
import com.example.jwt.model.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;

//    /login 요청을 하면 로그인 시도를 위해서 실행되는 함수
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        System.out.println("JwtAuthenticationFilter : 로그인 시도중");
        try {
            ObjectMapper om = new ObjectMapper();
            User user = om.readValue(request.getInputStream(), User.class);
            System.out.println(user);
            System.out.println("=============================");
            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword());

//            PrincipalDetailsService의 loadUserByUsername() 메서드가 실행됨
            Authentication authentication =
                    authenticationManager.authenticate(authenticationToken);

            PrincipalDetails principalDetails = (PrincipalDetails) authentication.getPrincipal();
            System.out.println(principalDetails.getUser());
            System.out.println("로그인 완료된 계정 아이디 : " + principalDetails.getUsername());
            return authentication;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

//    attemptAuthentication 로그인 요청 후 인증이 완료되면 해당 메서드 실행
//    JWT 토큰을 만들어서 리턴해주기
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {

        System.out.println("successfullAuthentication 실행 ");
        PrincipalDetails principalDetails = (PrincipalDetails) authResult.getPrincipal();
        String jwtToken = JWT.create()
                .withSubject("cos토큰")
                .withExpiresAt(new Date(System.currentTimeMillis() + (60000 * 10))) //10분뒤 만료
                .withClaim("id", principalDetails.getUser().getId())
                .withClaim("username", principalDetails.getUser().getUsername())
                .sign(Algorithm.HMAC512("cos"));

        response.addHeader("Authorization", "Bearer " + jwtToken);
    }
}
