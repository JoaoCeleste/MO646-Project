package activity;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.time.LocalDateTime;

public class FlightBookingSystemTest {
    private FlightBookingSystem flightBookingSystem;
    private LocalDateTime bookingTime;
    private LocalDateTime departureTime;

    @BeforeEach
    public void setup() {
        flightBookingSystem = new FlightBookingSystem();
        bookingTime = LocalDateTime.of(2024, 10, 1, 12, 0);
        departureTime = LocalDateTime.of(2024, 10, 2, 12, 0);
    }

    @Test
    public void testNotEnoughSeatsAvailable() {
        // não há assentos suficientes
        FlightBookingSystem.BookingResult result = flightBookingSystem.bookFlight(5, bookingTime, 4, 500.00, 50, false, departureTime, 0);
        Assertions.assertFalse(result.confirmation);
        Assertions.assertEquals(0, result.totalPrice, 0.01);
        Assertions.assertEquals(0, result.refundAmount, 0.01);
        Assertions.assertFalse(result.pointsUsed);
    }

    @Test
    public void testDynamicPricing() {
        // precificação dinâmica com base nas vendas anteriores
        FlightBookingSystem.BookingResult result = flightBookingSystem.bookFlight(3, bookingTime, 100, 500.00, 50, false, departureTime, 0);
        double expectedPriceFactor = (50 / 100.0) * 0.8;
        double expectedTotalPrice = 500.00 * expectedPriceFactor * 3;
        Assertions.assertTrue(result.confirmation);
        Assertions.assertEquals(expectedTotalPrice, result.totalPrice, 0.01);
        Assertions.assertEquals(0, result.refundAmount);
        Assertions.assertFalse(result.pointsUsed);
    }

    @Test
    public void testLastMinuteFeeApplied() {
        // taxa de última hora (menos de 24 horas antes da partida)
        LocalDateTime lastMinuteBookingTime = departureTime.minusHours(23);
        FlightBookingSystem.BookingResult result = flightBookingSystem.bookFlight(2, lastMinuteBookingTime, 100, 500.00, 50, false, departureTime, 0);
        double expectedPriceFactor = (50 / 100.0) * 0.8;
        double expectedTotalPrice = (500.00 * expectedPriceFactor * 2) + 100;  // Adiciona taxa de $100
        Assertions.assertTrue(result.confirmation);
        Assertions.assertEquals(expectedTotalPrice, result.totalPrice, 0.01);
        Assertions.assertEquals(0, result.refundAmount);
        Assertions.assertFalse(result.pointsUsed);
    }

    @Test
    public void testGroupDiscountApplied() {
        // desconto para grupos (mais de 4 passageiros)
        FlightBookingSystem.BookingResult result = flightBookingSystem.bookFlight(5, bookingTime, 100, 500.00, 50, false, departureTime, 0);
        double expectedPriceFactor = (50 / 100.0) * 0.8;
        double expectedTotalPrice = (500.00 * expectedPriceFactor * 5) * 0.95;  // 5% de desconto
        Assertions.assertTrue(result.confirmation);
        Assertions.assertEquals(expectedTotalPrice, result.totalPrice, 0.01);
        Assertions.assertEquals(0, result.refundAmount);
        Assertions.assertFalse(result.pointsUsed);
    }

    @Test
    public void testRewardPointsUsed() {
        // uso de pontos de recompensa
        int rewardPoints = 5000;
        FlightBookingSystem.BookingResult result = flightBookingSystem.bookFlight(2, bookingTime, 100, 500.00, 50, false, departureTime, rewardPoints);
        double expectedPriceFactor = (50 / 100.0) * 0.8;
        double basePrice = 500.00 * expectedPriceFactor * 2;
        double expectedTotalPrice = basePrice - (rewardPoints * 0.01);  // Pontos de recompensa usados (rewardPoints * 0.01)
        Assertions.assertTrue(result.confirmation);
        Assertions.assertEquals(expectedTotalPrice, result.totalPrice, 0.01);
        Assertions.assertEquals(0, result.refundAmount);
        Assertions.assertTrue(result.pointsUsed);
    }

    @Test
    public void testCancellationFullRefund() {
        // cancelamento com reembolso total (mais de 48 horas antes da partida)
        FlightBookingSystem.BookingResult result = flightBookingSystem.bookFlight(2, bookingTime, 100, 500.00, 50, true, departureTime.plusHours(49), 0);
        double expectedPriceFactor = (50 / 100.0) * 0.8;
        double expectedTotalPrice = 500.00 * expectedPriceFactor * 2;
        Assertions.assertFalse(result.confirmation);
        Assertions.assertEquals(0, result.totalPrice);
        Assertions.assertEquals(expectedTotalPrice, result.refundAmount, 0.01);
        Assertions.assertFalse(result.pointsUsed);
    }

    @Test
    public void testCancellationPartialRefund() {
        // cancelamento com reembolso parcial (menos de 48 horas antes da partida)
        FlightBookingSystem.BookingResult result = flightBookingSystem.bookFlight(2, bookingTime, 100, 500.00, 50, true, departureTime.minusHours(24), 0);
        double expectedPriceFactor = (50 / 100.0) * 0.8;
        double expectedTotalPrice = (500.00 * expectedPriceFactor * 2) + 100; // last minute fee added because of the 24 hour rule
        double expectedRefundAmount = expectedTotalPrice * 0.5;  // Reembolso de 50%
        Assertions.assertFalse(result.confirmation);
        Assertions.assertEquals(0, result.totalPrice);
        Assertions.assertEquals(expectedRefundAmount, result.refundAmount, 0.01);
        Assertions.assertFalse(result.pointsUsed);
    }

    @Test
    public void testBookingWithoutRewardPointsAndNoDiscounts() {
        // Cenário: reserva padrão sem desconto ou pontos de recompensa
        FlightBookingSystem.BookingResult result = flightBookingSystem.bookFlight(1, bookingTime, 100, 500.00, 10, false, departureTime, 0);
        double expectedPriceFactor = (10 / 100.0) * 0.8;
        double expectedTotalPrice = 500.00 * expectedPriceFactor;
        Assertions.assertTrue(result.confirmation);
        Assertions.assertEquals(expectedTotalPrice, result.totalPrice, 0.01);
        Assertions.assertEquals(0, result.refundAmount);
        Assertions.assertFalse(result.pointsUsed);
    }

    @Test
    public void testSeatAvailable(){
        FlightBookingSystem.BookingResult result = flightBookingSystem.bookFlight(5, bookingTime, 5, 200.0, 50, false, departureTime, 0);
        Assertions.assertTrue(result.confirmation);
    }

    @Test
    public void testSeatExceeded(){
        FlightBookingSystem.BookingResult result = flightBookingSystem.bookFlight(6, bookingTime, 5, 200.0, 50, false, departureTime, 0);
        Assertions.assertFalse(result.confirmation);

    }

    @Test
    public void testDiscountNotApplied(){
        FlightBookingSystem.BookingResult result = flightBookingSystem.bookFlight(4, bookingTime, 5, 200.0, 50, false, departureTime, 0);
        Assertions.assertEquals(320.00, result.totalPrice);
    }

    @Test
    public void testDiscountApplied(){
        FlightBookingSystem.BookingResult result = flightBookingSystem.bookFlight(5, bookingTime, 5, 200.0, 50, false, departureTime, 0);
        Assertions.assertEquals(380.00, result.totalPrice);
    }

    @Test
    public void testCancelationValid(){
        FlightBookingSystem.BookingResult result = flightBookingSystem.bookFlight(4, bookingTime, 5, 200.0, 50, true, bookingTime.plusHours(48), 0);
        Assertions.assertEquals(320.00, result.refundAmount);

    }

    @Test 
    public void testCancelationInvalid(){
        FlightBookingSystem.BookingResult result = flightBookingSystem.bookFlight(4, bookingTime, 5, 200.0, 50, true, bookingTime.plusHours(24), 0);
        Assertions.assertEquals(160.00, result.refundAmount);

    }
}
