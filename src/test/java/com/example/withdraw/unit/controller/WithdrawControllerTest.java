package com.example.withdraw.unit.controller;

import com.example.withdraw.controller.WithdrawController;
import com.example.withdraw.dto.WithdrawErrorResponse;
import com.example.withdraw.dto.WithdrawRequest;
import com.example.withdraw.dto.WithdrawResponse;
import com.example.withdraw.entity.TransactionStatus;
import com.example.withdraw.entity.WithdrawError;
import com.example.withdraw.service.WithdrawService;
import io.vavr.control.Either;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WithdrawControllerTest {

    @Mock
    private WithdrawService withdrawService;

    @InjectMocks
    private WithdrawController controller;

    @Test
    void approvedWithdrawalReturnsOkWithResponseBody() {
        WithdrawRequest request = new WithdrawRequest("user-1", new BigDecimal("30.00"), "1234567812345678");
        WithdrawResponse response = new WithdrawResponse(
                "tx-1", TransactionStatus.APPROVED, new BigDecimal("30.00"), "1234567812345678");
        when(withdrawService.withdraw(any())).thenReturn(Either.right(response));

        ResponseEntity<?> result = controller.withdraw(request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isEqualTo(response);
    }

    @Test
    void unknownUserMapsToNotFound() {
        WithdrawRequest request = new WithdrawRequest("unknown-user", new BigDecimal("30.00"), "1234567812345678");
        when(withdrawService.withdraw(any())).thenReturn(Either.left(WithdrawError.INVALID_USER));

        ResponseEntity<?> result = controller.withdraw(request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(result.getBody()).isEqualTo(new WithdrawErrorResponse("INVALID_USER"));
    }

    @Test
    void invalidAmountMapsToBadRequest() {
        WithdrawRequest request = new WithdrawRequest("user-1", BigDecimal.ZERO, "1234567812345678");
        when(withdrawService.withdraw(any())).thenReturn(Either.left(WithdrawError.INVALID_AMOUNT));

        ResponseEntity<?> result = controller.withdraw(request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(result.getBody()).isEqualTo(new WithdrawErrorResponse("INVALID_AMOUNT"));
    }

    @Test
    void cardNotOwnedMapsToForbidden() {
        WithdrawRequest request = new WithdrawRequest("user-1", new BigDecimal("30.00"), "9999999999999999");
        when(withdrawService.withdraw(any())).thenReturn(Either.left(WithdrawError.CARD_NOT_OWNED));

        ResponseEntity<?> result = controller.withdraw(request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(result.getBody()).isEqualTo(new WithdrawErrorResponse("CARD_NOT_OWNED"));
    }

    @Test
    void insufficientFundsMapsToUnprocessableEntity() {
        WithdrawRequest request = new WithdrawRequest("user-1", new BigDecimal("100.01"), "1234567812345678");
        when(withdrawService.withdraw(any())).thenReturn(Either.left(WithdrawError.INSUFFICIENT_FUNDS));

        ResponseEntity<?> result = controller.withdraw(request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(result.getBody()).isEqualTo(new WithdrawErrorResponse("INSUFFICIENT_FUNDS"));
    }
}
