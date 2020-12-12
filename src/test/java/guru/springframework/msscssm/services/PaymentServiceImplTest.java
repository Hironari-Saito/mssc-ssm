package guru.springframework.msscssm.services;

import guru.springframework.msscssm.domain.Payment;
import guru.springframework.msscssm.domain.PaymentEvent;
import guru.springframework.msscssm.domain.PaymentState;
import guru.springframework.msscssm.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.statemachine.StateMachine;
import org.springframework.test.annotation.Repeat;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PaymentServiceImplTest {

    @Autowired
    PaymentService paymentService;
    @Autowired
    PaymentRepository paymentRepository;

    Payment payment;

    @BeforeEach
    void setUp() {
        payment = Payment.builder()
                .amount(new BigDecimal("12.99"))
                .build();
    }

    @Transactional
    @RepeatedTest(10)
    void testNew2Auth() {

        Payment savedPayment = paymentService.newPayment(payment);
        assertEquals(PaymentState.NEW, savedPayment.getState());

        StateMachine<PaymentState, PaymentEvent> sm = paymentService.preAuth(savedPayment.getId());

        Payment preAuthedPaymend = paymentRepository.getOne(savedPayment.getId());
        System.out.println(preAuthedPaymend);

        assertTrue(PaymentState.PRE_AUTH.equals(sm.getState().getId()) || PaymentState.PRE_AUTH_ERROR.equals(sm.getState().getId()));


        if(PaymentState.PRE_AUTH.equals(sm.getState().getId())) {
            StateMachine<PaymentState, PaymentEvent> authSm = paymentService.authorizePayment(savedPayment.getId());
            assertTrue(PaymentState.AUTH.equals(authSm.getState().getId())
                    || PaymentState.AUTH_ERROR.equals(authSm.getState().getId()));
        }
    }

}