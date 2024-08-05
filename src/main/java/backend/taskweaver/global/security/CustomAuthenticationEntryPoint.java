package backend.taskweaver.global.security;

import backend.taskweaver.global.code.ErrorCode;
import backend.taskweaver.global.code.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        String exception = (String) request.getAttribute("exception");

        if(exception == null) {
            setResponse(response, ErrorCode.INTERNAL_SERVER_ERROR);
        } else if (exception.equals(ErrorCode.INVALID_JWT_ERROR.getMessage())) {
            setResponse(response, ErrorCode.INVALID_JWT_ERROR);
        } else if (exception.equals(ErrorCode.EXPIRED_JWT_ERROR.getMessage())) {
            setResponse(response, ErrorCode.EXPIRED_JWT_ERROR);
        } else if (exception.equals(ErrorCode.UNSUPPORTED_JWT_TOKEN.getMessage())) {
            setResponse(response, ErrorCode.UNSUPPORTED_JWT_TOKEN);
        } else if (exception.equals(ErrorCode.USER_AUTH_ERROR.getMessage())) {
            setResponse(response, ErrorCode.USER_AUTH_ERROR);
        } else if(exception.equals(ErrorCode.TOKEN_MISSING_ERROR.getMessage())) {
            setResponse(response, ErrorCode.TOKEN_MISSING_ERROR);
        } else if(exception.equals(ErrorCode.MEMBER_LOGGED_OUT.getMessage())) {
            setResponse(response, ErrorCode.MEMBER_LOGGED_OUT);
        } else if (exception.equals(ErrorCode.MEMBER_NOT_FOUND.getMessage())) {
            setResponse(response, ErrorCode.MEMBER_NOT_FOUND);
        }
    }

    private void setResponse(HttpServletResponse response, ErrorCode errorCode) throws IOException {
        ErrorResponse error = ErrorResponse.of(errorCode, errorCode.getMessage());
        response.setCharacterEncoding("UTF-8");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(errorCode.getStatus());
        response.getWriter().write(new ObjectMapper().writeValueAsString(error));
    }
}
