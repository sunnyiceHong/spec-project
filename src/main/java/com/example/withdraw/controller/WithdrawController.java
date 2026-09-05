package com.example.withdraw.controller;

import com.example.withdraw.dto.WithdrawErrorResponse;
import com.example.withdraw.dto.WithdrawRequest;
import com.example.withdraw.dto.WithdrawResponse;
import com.example.withdraw.entity.WithdrawError;
import com.example.withdraw.service.WithdrawService;
import io.vavr.control.Either;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST adapter that exposes the withdrawal service over HTTP.
 *
 * <p>The controller is a thin adapter: it validates the request DTO, delegates to
 * {@link WithdrawService}, and translates the {@link Either} result into an HTTP
 * response. All business rules live in the service, never here.</p>
 */
@RestController
@RequestMapping("/api")
public class WithdrawController {

    private final WithdrawService withdrawService;

    public WithdrawController(WithdrawService withdrawService) {
        this.withdrawService = withdrawService;
    }

    @PostMapping("/withdraw")
    public ResponseEntity<?> withdraw(@Valid @RequestBody WithdrawRequest request) {
        Either<WithdrawError, WithdrawResponse> result = withdrawService.withdraw(request);
        if (result.isRight()) {
            return ResponseEntity.ok(result.get());
        }
        WithdrawError error = result.getLeft();
        return ResponseEntity.status(toHttpStatus(error))
                .body(new WithdrawErrorResponse(error.name()));
    }

    private HttpStatus toHttpStatus(WithdrawError error) {
        return switch (error) {
            case INVALID_USER -> HttpStatus.NOT_FOUND;
            case INVALID_AMOUNT, INVALID_CARD_NUMBER -> HttpStatus.BAD_REQUEST;
            case CARD_NOT_OWNED -> HttpStatus.FORBIDDEN;
            case INSUFFICIENT_FUNDS -> HttpStatus.UNPROCESSABLE_ENTITY;
        };
    }
}
