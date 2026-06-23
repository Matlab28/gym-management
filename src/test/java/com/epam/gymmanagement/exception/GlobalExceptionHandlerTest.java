package com.epam.gymmanagement.exception;

import com.epam.gymmanagement.dto.request.RegisterRequestDTO;
import com.epam.gymmanagement.dto.response.MessageResponseDTO;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GlobalExceptionHandlerTest {
    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handlesKnownApplicationExceptions() {
        assertResponse(
                handler.handleNotFoundException(new NotFoundException("missing")),
                HttpStatus.NOT_FOUND,
                "missing"
        );
        assertResponse(
                handler.handleBadRequestException(new BadRequestException("bad request")),
                HttpStatus.BAD_REQUEST,
                "bad request"
        );
        assertResponse(
                handler.handleIllegalArgumentException(new IllegalArgumentException("bad argument")),
                HttpStatus.BAD_REQUEST,
                "bad argument"
        );
    }

    @Test
    void handlesSecurityAndResourceExceptions() {
        assertResponse(
                handler.handleAuthenticationException(new BadCredentialsException("bad credentials")),
                HttpStatus.UNAUTHORIZED,
                "Authentication failed"
        );
        assertResponse(
                handler.handleAccessDeniedException(new AccessDeniedException("denied")),
                HttpStatus.FORBIDDEN,
                "denied"
        );
        assertResponse(
                handler.handleNoResourceFoundException(new NoResourceFoundException(HttpMethod.GET, "/missing", "static")),
                HttpStatus.NOT_FOUND,
                "Resource not found"
        );
    }

    @Test
    void handlesValidationExceptionWithFirstFieldError() throws NoSuchMethodException {
        RegisterRequestDTO request = new RegisterRequestDTO();
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(request, "request");
        bindingResult.addError(new FieldError("request", "email", "Email is required"));

        Method method = GlobalExceptionHandlerTest.class.getDeclaredMethod(
                "validationTarget",
                RegisterRequestDTO.class
        );
        MethodParameter parameter = new MethodParameter(method, 0);
        MethodArgumentNotValidException exception =
                new MethodArgumentNotValidException(parameter, bindingResult);

        assertResponse(
                handler.handleValidationException(exception),
                HttpStatus.BAD_REQUEST,
                "email: Email is required"
        );
    }

    @Test
    void handlesUnexpectedExceptions() {
        assertResponse(
                handler.handleGeneralException(new RuntimeException("boom")),
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal server error"
        );
    }

    @SuppressWarnings("unused")
    private void validationTarget(RegisterRequestDTO request) {
    }

    private void assertResponse(
            ResponseEntity<MessageResponseDTO> response,
            HttpStatus status,
            String message
    ) {
        assertEquals(status, response.getStatusCode());
        assertEquals(message, response.getBody().getMessage());
    }
}
