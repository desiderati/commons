/*
 * Copyright (c) 2020 - Felipe Desiderati
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial
 * portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package io.herd.common.exception;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.client.ApiException;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.hibernate.exception.ConstraintViolationException;
import org.jetbrains.annotations.NotNull;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.rest.webmvc.RepositoryRestController;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.transaction.TransactionException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpStatusCodeException;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;

@Slf4j
@ControllerAdvice(annotations = {RestController.class, RepositoryRestController.class})
public class ExceptionHandlingController implements MessageSourceAware {

    private static final String DEFAULT_MESSAGE =
        "The server encountered an unexpected condition that prevented it from fulfilling the request!";

    private static final String DEFAULT_VALIDATION_ERROR_MESSAGE =
        "There was an error while validating the request parameters!";

    private static final HttpStatus DEFAULT_HTTP_STATUS = HttpStatus.INTERNAL_SERVER_ERROR;

    @Setter
    private MessageSource messageSource;

    private ModelMapper modelMapper;

    @Autowired
    public ExceptionHandlingController(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseExceptionDTO> handleException(HttpServletRequest request, Exception ex) {
        ResponseEntity<ResponseExceptionDTO> responseEntity = handleApiExceptionCopycat(request, ex);
        if (responseEntity != null) {
            return responseEntity;
        }

        Throwable rootCause = ExceptionUtils.getRootCause(ex);
        if (rootCause instanceof javax.validation.ConstraintViolationException) {
            return handleJavaxConstraintViolationException(
                request, (javax.validation.ConstraintViolationException) rootCause);
        }

        return handleHttpErrorException(request, DEFAULT_HTTP_STATUS, ex);
    }

    /**
     * Cases where the ApiException is in another package than the default.
     */
    private ResponseEntity<ResponseExceptionDTO> handleApiExceptionCopycat(HttpServletRequest request, Exception ex) {
        if (!(ex instanceof ApiException) && ex.getClass().getName().endsWith("ApiException")) {
            ApiException apiException = modelMapper.map(ex, ApiException.class);
            if (apiException.getCode() != 0 && apiException.getResponseBody() != null
                && !apiException.getResponseHeaders().isEmpty()) {

                log.info("Transforming " + ex.getClass().getName() + " into " + ApiException.class.getName());
                return handleApiException(request, apiException);
            }
        }
        return null;
    }

    @ExceptionHandler(HttpStatusCodeException.class)
    public ResponseEntity<ResponseExceptionDTO> handleHttpStatusCodeException(HttpServletRequest request,
                                                                               HttpStatusCodeException  ex) {
        return handleSwaggerException(request, ex, ex.getRawStatusCode() + " " + ex.getStatusText(),
            ex.getResponseBodyAsString(), ex.getResponseHeaders());
    }

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ResponseExceptionDTO> handleApiException(HttpServletRequest request, ApiException ex) {
        return handleSwaggerException(request, ex, ex.getMessage(), ex.getResponseBody(), ex.getResponseHeaders());
    }

    private ResponseEntity<ResponseExceptionDTO> handleSwaggerException(HttpServletRequest request, Exception exception,
            // We had to extract these parameters because the exceptions don't inherit the same parent class.
            String exceptionMessage,
            String responseBody,
            Map<String, List<String>> responseHeaders) {

        StringBuilder remoteException = new StringBuilder(exceptionMessage);
        if (responseHeaders != null) {
            remoteException.append(System.lineSeparator()).append("Headers:").append(System.lineSeparator());
            for (final Map.Entry<String, List<String>> entry : responseHeaders.entrySet()) {
                remoteException.append("  ").append(entry.getKey()).append(": ")
                    .append(StringUtils.join(entry.getValue(), ", ")).append(System.lineSeparator());
            }
        }

        ValidationResponseExceptionDTO responseException = null;
        if (responseBody != null) {
            remoteException.append("Body:").append(System.lineSeparator());
            try {
                TypeReference<ValidationResponseExceptionDTO> typeRef = new TypeReference<ValidationResponseExceptionDTO>(){};
                ObjectMapper mapper = new ObjectMapper();
                responseException = mapper.readValue(responseBody, typeRef);
                String prettyPrintedResponseBody =
                    mapper.writerWithDefaultPrettyPrinter().writeValueAsString(responseException);
                remoteException.append(prettyPrintedResponseBody);
            } catch (IOException ioex) {
                log.warn("Is was not possible deserialize API exception body to response exception!", ioex);
                remoteException.append(responseBody);
            }
        }

        // We cannot use instaceOf because if the ApiException body has at least one property equal to one of
        // the properties of the ResponseExceptionDTO class, the deserialized object will be valid.
        if (responseException != null && responseException.getType().equals(ResponseExceptionDTO.class.getName())) {
            String errorCode = responseException.getErrorCode();
            String errorMsg = responseException.getMessage();
            UUID uuid = logMessage(request, remoteException.toString());
            return ResponseEntity.status(responseException.getStatus()).body(
                new ResponseExceptionDTO(uuid, errorCode, errorMsg, responseException.getStatus()));
        }

        // Regardless of the return status of the Swagger request, we will generate the same standard error
        // for our clients.
        return handleException(request, exception);
    }

    @ExceptionHandler({IllegalArgumentApplicationException.class, IllegalStateApplicationException.class})
    public ResponseEntity<ResponseExceptionDTO> handleIllegalArgumentStateApplicationException(
            HttpServletRequest request, ApplicationException ex) {
        return handleApplicationException(request, HttpStatus.NOT_ACCEPTABLE, ex);
    }

    @ExceptionHandler(SecurityApplicationException.class)
    public ResponseEntity<ResponseExceptionDTO> handleSecurityApplicationException(
        HttpServletRequest request, SecurityApplicationException ex) {
        return handleApplicationException(request, HttpStatus.UNAUTHORIZED, ex);
    }

    @ExceptionHandler(ResourceNotFoundApplicationException.class)
    public ResponseEntity<ResponseExceptionDTO> handleResourceNotFoundApplicationException(
            HttpServletRequest request, ResourceNotFoundApplicationException ex) {
        return handleApplicationException(request, HttpStatus.NOT_FOUND, ex);
    }

    @ExceptionHandler(ApplicationException.class)
    public ResponseEntity<ResponseExceptionDTO> handleApplicationException(
            HttpServletRequest request, ApplicationException ex) {
        return handleApplicationException(request, DEFAULT_HTTP_STATUS, ex);
    }

    @NonNull
    private ResponseEntity<ResponseExceptionDTO> handleApplicationException(
            HttpServletRequest request, HttpStatus httpStatus, ApplicationException ex) {
        return handleException(request, httpStatus, ex, ex.getArgs());
    }

    @ExceptionHandler(RestApiException.class)
    public ResponseEntity<ResponseExceptionDTO> handleRestApiException(
            HttpServletRequest request, RestApiException ex) {
        HttpStatus httpStatus = getHttpStatus(ex);
        return handleException(request, httpStatus, ex, ex.getArgs());
    }

    /**
     * Throwed when used the @{@link org.springframework.validation.annotation.Validated} annotation.
     */
    @ExceptionHandler(javax.validation.ConstraintViolationException.class)
    public ResponseEntity<ResponseExceptionDTO> handleJavaxConstraintViolationException(
            HttpServletRequest request, javax.validation.ConstraintViolationException ex) {

        String errorCode = "validation_error_msg";
        String errorMsg = getMessage(request.getLocale(), errorCode, DEFAULT_VALIDATION_ERROR_MESSAGE);
        UUID uuid = logMessage(request, errorMsg, null);
        List<String> errors = new ArrayList<>();
        for (final ConstraintViolation<?> constraintViolation : ex.getConstraintViolations()) {
            String fieldErrorMsg = constraintViolation.getPropertyPath() + ":" + constraintViolation.getMessage();
            errors.add(fieldErrorMsg);
            log.error("Validation: " + fieldErrorMsg);
        }

        ValidationResponseExceptionDTO responseExpection =
            new ValidationResponseExceptionDTO(
                uuid, errorCode, errorMsg, HttpStatus.BAD_REQUEST.value(), errors.toArray(new String[]{}));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseExpection);
    }

    /**
     * Throwed when used the  @{@link javax.validation.Valid} annotation.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResponseExceptionDTO> handleMethodArgumentNotValidException(
            HttpServletRequest request, MethodArgumentNotValidException ex) {

        String errorCode = "validation_error_msg";
        String errorMsg = getMessage(request.getLocale(), errorCode, DEFAULT_VALIDATION_ERROR_MESSAGE);
        UUID uuid = logMessage(request, errorMsg, null);
        List<String> errors = new ArrayList<>();
        for (final FieldError error : ex.getBindingResult().getFieldErrors()) {
            String fieldErrorMsg = error.getField() + ":" + error.getDefaultMessage();
            errors.add(fieldErrorMsg);
            log.error("Validation: " + fieldErrorMsg);
        }

        ValidationResponseExceptionDTO responseExpection =
            new ValidationResponseExceptionDTO(
                uuid, errorCode, errorMsg, HttpStatus.BAD_REQUEST.value(), errors.toArray(new String[]{}));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseExpection);
    }

    @ExceptionHandler(TransactionException.class)
    public ResponseEntity<ResponseExceptionDTO> handleTransactionException(
            HttpServletRequest request, TransactionException ex) {

        ConstraintViolationException constraintEx = getConstraintViolationException(ex.getCause());
        if (constraintEx != null) {
            return handleConstraintViolationException(request, constraintEx);
        } else {
            return handleException(request, ex);
        }
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ResponseExceptionDTO> handleDataIntegrityViolationException(
            HttpServletRequest request, DataIntegrityViolationException ex) {

        ConstraintViolationException constraintEx = getConstraintViolationException(ex.getCause());
        if (constraintEx != null) {
            return handleConstraintViolationException(request, constraintEx);
        } else {
            return handleException(request, ex);
        }
    }

    private ResponseEntity<ResponseExceptionDTO> handleConstraintViolationException(
            HttpServletRequest request, ConstraintViolationException ex) {

        String errorCode = getConstraintViolationErrorCode(ex);
        String errorMsg = getMessage(request.getLocale(), errorCode, DEFAULT_MESSAGE);
        UUID uuid = logMessage(request, errorMsg, ex);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
            new ResponseExceptionDTO(uuid, errorCode, errorMsg, HttpStatus.CONFLICT.value()));
    }

    @ExceptionHandler(EmptyResultDataAccessException.class)
    public ResponseEntity<ResponseExceptionDTO> handleEmptyResultDataAccessException(
            HttpServletRequest request, EmptyResultDataAccessException ex) {
        return handleHttpErrorException(request, HttpStatus.NOT_FOUND, ex);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ResponseExceptionDTO> handleResourceNotFoundException(
            HttpServletRequest request, ResourceNotFoundException ex) {
        return handleHttpErrorException(request, HttpStatus.NOT_FOUND, ex);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ResponseExceptionDTO> handleHttpRequestMethodNotSupportedException(
            HttpServletRequest request, HttpRequestMethodNotSupportedException ex) {
        return handleHttpErrorException(request, HttpStatus.METHOD_NOT_ALLOWED, ex);
    }

    @NotNull
    protected ResponseEntity<ResponseExceptionDTO> handleHttpErrorException(
            HttpServletRequest request, HttpStatus httpStatus, Exception ex) {

        String errorCode = "http_error_" + httpStatus.value();
        String errorMsg = getMessage(request.getLocale(), errorCode, DEFAULT_MESSAGE);
        UUID uuid = logMessage(request, errorMsg, ex);
        return ResponseEntity.status(httpStatus).body(
            new ResponseExceptionDTO(uuid, errorCode, errorMsg, httpStatus.value()));
    }

    private ResponseEntity<ResponseExceptionDTO> handleException(
            HttpServletRequest request, HttpStatus httpStatus, Exception ex, Serializable... args) {

        String errorCode = ex.getMessage();
        String errorMsg = getMessage(request.getLocale(), errorCode, DEFAULT_MESSAGE, args);
        UUID uuid = logMessage(request, errorMsg, ex);
        return ResponseEntity.status(httpStatus).body(
            new ResponseExceptionDTO(uuid, errorCode, errorMsg, httpStatus.value()));
    }

    private UUID logMessage(HttpServletRequest request, String errorMessage) {
        return logMessage(request, errorMessage, null);
    }

    private UUID logMessage(HttpServletRequest request, String errorMessage, Exception ex) {
        UUID uuid = UUID.randomUUID();
        log.error("Requested URL: " + request.getRequestURL());
        log.error("UUID: " + uuid + " | " + errorMessage, ex);
        return uuid;
    }

    private HttpStatus getHttpStatus(Exception ex) {
        ResponseStatus responseStatus =
            AnnotatedElementUtils.findMergedAnnotation(ex.getClass(), ResponseStatus.class);
        if (responseStatus != null) {
            return responseStatus.code();
        } else if (ex.getCause() instanceof Exception) {
            ex = (Exception) ex.getCause();
            return getHttpStatus(ex);
        }
        return DEFAULT_HTTP_STATUS;
    }

    private ConstraintViolationException getConstraintViolationException(Throwable throwable) {
        if (throwable == null) {
            return null;
        } else if (throwable instanceof ConstraintViolationException) {
            return (ConstraintViolationException) throwable;
        } else {
            return getConstraintViolationException(throwable.getCause());
        }
    }

    private String getConstraintViolationErrorCode(ConstraintViolationException ex) {
        switch (ex.getSQLState()) {
            case "23502":
                return "not_null_" + ex.getConstraintName() + "_exception";

            case "23505":
                return "unique_" + ex.getConstraintName() + "_exception";

            default:
                return "http_error_" + HttpStatus.CONFLICT.value();
        }
    }

    private String getMessage(Locale locale, String code, String defaultMessage, Serializable... args) {
        return messageSource != null
            ? messageSource.getMessage(code, args, defaultMessage, locale)
            : defaultMessage;
    }
}
