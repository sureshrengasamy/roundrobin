package com.roundrobin.common;


import static com.roundrobin.common.LoggerUtils.append;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.NoHandlerFoundException;

import com.roundrobin.api.Error;
import com.roundrobin.api.Response;
import com.roundrobin.exception.AbstractException;
import com.roundrobin.exception.ClientException;
import com.roundrobin.exception.ServerException;

@ControllerAdvice
public class ExceptionMapper {
  private final Logger logger = LoggerFactory.getLogger(ExceptionMapper.class);

  @Autowired
  private ClientMessages messages;

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<Response<String>> handleMessageNotReadableException(HttpMessageNotReadableException e) {
    Response<String> response =
        new Response<>(new Error(ErrorCode.UNPARSABLE_INPUT, messages.getErrorMessage(ErrorCode.UNPARSABLE_INPUT)));
    return log(HttpStatus.BAD_REQUEST, response, e);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Response<String>> handleBindException(MethodArgumentNotValidException ex) {
    List<Error> errors = ex.getBindingResult().getAllErrors().stream().map(e -> {
      String message = e.getDefaultMessage();
      if (e instanceof FieldError) {
        return new Error(ErrorCode.INVALID_FIELD, ((FieldError) e).getField() + ": " + message);
      } else {
        return new Error(ErrorCode.INVALID_FIELD, message);
      }
    }).collect(Collectors.toList());
    for (ObjectError oe : ex.getBindingResult().getAllErrors()) {
      System.out.print(oe);
    }
    return log(HttpStatus.BAD_REQUEST, new Response<>(errors), ex);
  }

  @ExceptionHandler(ClientException.class)
  public ResponseEntity<Response<String>> handleClientException(ClientException e) {
    Response<String> response = new Response<>(new Error(e.getCode(), messages.getErrorMessage(e.getCode())));
    return log(HttpStatus.BAD_REQUEST, response, e);
  }

  @ExceptionHandler(ServerException.class)
  public ResponseEntity<Response<String>> handleServerException(ServerException e) {
    Response<String> response = new Response<>(new Error(e.getCode(), messages.getErrorMessage(e.getCode())));
    return log(HttpStatus.INTERNAL_SERVER_ERROR, response, e);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<Response<String>> handleException(Exception e) {
    Response<String> response =
        new Response<>(new Error(ErrorCode.INTERNAL_ERROR, messages.getErrorMessage(ErrorCode.INTERNAL_ERROR)));
    return log(HttpStatus.INTERNAL_SERVER_ERROR, response, e);
  }

  @ExceptionHandler({HttpRequestMethodNotSupportedException.class, NoHandlerFoundException.class})
  public ResponseEntity<Response<String>> handleMethodNotSupported(Exception e) {
    Response<String> response =
        new Response<>(new Error(ErrorCode.INVALID_URL, messages.getErrorMessage(ErrorCode.INVALID_URL)));
    return log(HttpStatus.NOT_FOUND, response, e);
  }

  private ResponseEntity<Response<String>> log(HttpStatus status, Response<String> response, Exception e) {
    response.setUuid(UUID.randomUUID().toString());
    response.setTimestamp(System.currentTimeMillis());
    if (e instanceof AbstractException) {
      AbstractException ge = (AbstractException) e;
      logger.error(
          append(Constants.ERROR_CODE, ge.getCode() + "") + "," + append(Constants.ERROR_MESSAGE, ge.getMessage()) + ","
              + append(Constants.ERROR_UUID, response.getUuid()) + ",Exception:" + ge,
          ge);
    } else {
      logger.error("Exception:" + e, e);
    }

    return new ResponseEntity<>(response, status);
  }
}
