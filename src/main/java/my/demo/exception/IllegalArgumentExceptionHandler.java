package my.demo.exception;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.server.exceptions.ExceptionHandler;
import jakarta.inject.Singleton;
import my.demo.dto.ErrorResponse;

@Singleton
public class IllegalArgumentExceptionHandler
    implements ExceptionHandler<IllegalArgumentException, HttpResponse<ErrorResponse>> {

    @Override
    public HttpResponse<ErrorResponse> handle(
        HttpRequest request,
        IllegalArgumentException exception)
    {
        return HttpResponse.status(HttpStatus.BAD_REQUEST)
            .body(new ErrorResponse(exception.getMessage()));
    }
}