package my.demo.exception;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.server.exceptions.ExceptionHandler;
import jakarta.inject.Singleton;
import my.demo.dto.ErrorResponse;

@Singleton
public class NoCarAvailableExceptionHandler
    implements ExceptionHandler<NoCarAvailableException, HttpResponse<ErrorResponse>> {

    @Override
    public HttpResponse<ErrorResponse> handle(
        HttpRequest request,
        NoCarAvailableException exception
    ) {
        return HttpResponse.status(HttpStatus.CONFLICT)
            .body(new ErrorResponse(exception.getMessage()));
    }
}