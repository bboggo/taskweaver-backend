package backend.taskweaver.global.security;

import backend.taskweaver.domain.member.entity.Member;
import backend.taskweaver.domain.member.repository.MemberRepository;
import backend.taskweaver.global.code.ErrorCode;
import backend.taskweaver.global.exception.handler.BusinessExceptionHandler;
import backend.taskweaver.global.exception.handler.JwtTokenException;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Order(0)
@Component
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final TokenProvider tokenProvider;
    private final MemberRepository memberRepository;
    private final RedisTemplate<String, String> redisTemplate;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            String accessToken = parseBearerToken(request, HttpHeaders.AUTHORIZATION);    // parseBearerToken() 변경에 따른 수정
            User user = parseUserSpecification(accessToken);
            Member member = memberRepository.findById(Long.valueOf(user.getUsername()))
                    .orElseThrow(()-> new BusinessExceptionHandler(ErrorCode.MEMBER_NOT_FOUND));

            // 로그아웃된 회원일 때
            if (!redisTemplate.hasKey(member.getEmail())) {
                throw new BusinessExceptionHandler(ErrorCode.MEMBER_LOGGED_OUT);
            }

            AbstractAuthenticationToken authenticated = UsernamePasswordAuthenticationToken.authenticated(user, accessToken, user.getAuthorities());
            authenticated.setDetails(new WebAuthenticationDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authenticated);
        } catch (ExpiredJwtException e) {
            request.setAttribute("exception", ErrorCode.EXPIRED_JWT_ERROR.getMessage());
        } catch (MalformedJwtException | SignatureException e) {
            request.setAttribute("exception", ErrorCode.INVALID_JWT_ERROR.getMessage());
        } catch (UnsupportedJwtException e) {
            request.setAttribute("exception", ErrorCode.UNSUPPORTED_JWT_TOKEN.getMessage());
        } catch (AuthenticationException | NullPointerException e) {
            request.setAttribute("exception", ErrorCode.USER_AUTH_ERROR.getMessage());
//        } catch (JwtTokenException e) {
//            request.setAttribute("exception", ErrorCode.TOKEN_MISSING_ERROR.getMessage());
        } catch (BusinessExceptionHandler e) {
            request.setAttribute("exception", e.getErrorCode().getMessage());
        }

        filterChain.doFilter(request, response);
    }

    private String parseBearerToken(HttpServletRequest request, String headerName) {
        return Optional.ofNullable(request.getHeader(headerName))
                .filter(token -> token.substring(0, 7).equalsIgnoreCase("Bearer "))
                .map(token -> token.substring(7))
                .orElse(null);
    }

    private User parseUserSpecification(String token) {
        String[] split = Optional.ofNullable(token)
                .filter(subject -> subject.length() >= 10)
                .map(tokenProvider::validateTokenAndGetSubject)
//                .orElseThrow(() -> new JwtTokenException(ErrorCode.TOKEN_MISSING_ERROR))
                .orElseThrow(() -> new BusinessExceptionHandler(ErrorCode.TOKEN_MISSING_ERROR))
                .split(":");
        System.out.println(split[0]);
        System.out.println(split[1]);

        return new User(split[0], "", List.of(new SimpleGrantedAuthority(split[1])));
    }


    private void reissueAccessToken(HttpServletRequest request, HttpServletResponse response, Exception exception) {
        try {
            String refreshToken = parseBearerToken(request, "Refresh-Token");
            if (refreshToken == null) {
                throw exception;
            }
            String oldAccessToken = parseBearerToken(request, HttpHeaders.AUTHORIZATION);
            tokenProvider.validateRefreshToken(refreshToken, oldAccessToken);
            String newAccessToken = tokenProvider.recreateAccessToken(oldAccessToken);
            User user = parseUserSpecification(newAccessToken);
            AbstractAuthenticationToken authenticated = UsernamePasswordAuthenticationToken.authenticated(user, newAccessToken, user.getAuthorities());
            authenticated.setDetails(new WebAuthenticationDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authenticated);

            response.setHeader("New-Access-Token", newAccessToken);
        } catch (Exception e) {
            request.setAttribute("exception", e);
        }
    }
}
