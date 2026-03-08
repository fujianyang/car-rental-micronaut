package my.demo.exception;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.server.exceptions.ExceptionHandler;
import jakarta.inject.Singleton;
import my.demo.dto.ErrorResponse;

@Singleton
public class ReservationNotFoundExceptionHandler
    implements ExceptionHandler<ReservationNotFoundException, HttpResponse<ErrorResponse>> {

    @Override
    public HttpResponse<ErrorResponse> handle(
        HttpRequest request,
        ReservationNotFoundException exception)
    {
        return HttpResponse.status(HttpStatus.NOT_FOUND)
            .body(new ErrorResponse(exception.getMessage()));
    }
}