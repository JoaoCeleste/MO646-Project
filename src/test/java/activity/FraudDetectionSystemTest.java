package activity;

import org.junit.Before;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;


public class FraudDetectionSystemTest {
    private FraudDetectionSystem fraudDetectionSystem;
    private LocalDateTime currentTime;

    @BeforeEach
    public void setUp() {
        fraudDetectionSystem = new FraudDetectionSystem();
        currentTime = LocalDateTime.now();
    }

    @Test
    public void testTransactionAmountAboveThreshold() {
        // transação com valor acima de 10.000 (Regra 1)
        FraudDetectionSystem.Transaction transaction = new FraudDetectionSystem.Transaction(10001, currentTime, "BR");
        FraudDetectionSystem.FraudCheckResult result = fraudDetectionSystem.checkForFraud(transaction, new ArrayList<>(), new ArrayList<>());

        Assertions.assertTrue(result.isFraudulent);
        Assertions.assertFalse(result.isBlocked);
        Assertions.assertTrue(result.verificationRequired);
        Assertions.assertEquals(50, result.riskScore);
    }

    @Test
    public void testExcessiveTransactionsInLastHour() {
        // mais de 10 transações na última hora (Regra 2)
        ArrayList<FraudDetectionSystem.Transaction> previousTransactions = new ArrayList<>();
        for (int i = 0; i < 11; i++) {
            FraudDetectionSystem.Transaction transaction = new FraudDetectionSystem.Transaction(100, currentTime.minusMinutes(i * 5), "BR");
            previousTransactions.add(transaction);
        }

        FraudDetectionSystem.Transaction transaction = new FraudDetectionSystem.Transaction(100, currentTime, "BR");
        FraudDetectionSystem.FraudCheckResult result = fraudDetectionSystem.checkForFraud(transaction, previousTransactions, new ArrayList<>());

        Assertions.assertFalse(result.isFraudulent);
        Assertions.assertTrue(result.isBlocked);
        Assertions.assertFalse(result.verificationRequired);
        Assertions.assertEquals(30, result.riskScore);
    }

    @Test
    public void testLocationChangeWithinShortTimeFrame() {
        // mudança de localização em um curto espaço de tempo (Regra 3)
        FraudDetectionSystem.Transaction transaction1 = new FraudDetectionSystem.Transaction(100, currentTime.minusMinutes(10), "BR");
        FraudDetectionSystem.Transaction transaction2 = new FraudDetectionSystem.Transaction(100, currentTime, "USA");
        FraudDetectionSystem.FraudCheckResult result = fraudDetectionSystem.checkForFraud(transaction2, List.of(transaction1), new ArrayList<>());

        Assertions.assertTrue(result.isFraudulent);
        Assertions.assertFalse(result.isBlocked);
        Assertions.assertTrue(result.verificationRequired);
        Assertions.assertEquals(20, result.riskScore);
    }

    @Test
    public void testBlacklistedLocation() {
        // localização na lista de locais bloqueados (Regra 4)
        FraudDetectionSystem.Transaction transaction = new FraudDetectionSystem.Transaction(100, currentTime, "HighRiskLocation");
        FraudDetectionSystem.FraudCheckResult result = fraudDetectionSystem.checkForFraud(transaction, new ArrayList<>(), List.of("HighRiskLocation"));

        Assertions.assertFalse(result.isFraudulent);
        Assertions.assertTrue(result.isBlocked);
        Assertions.assertFalse(result.verificationRequired);
        Assertions.assertEquals(100, result.riskScore);
    }

    @Test
    public void testNoFraudDetected() {
        // nenhuma fraude detectada
        FraudDetectionSystem.Transaction transaction = new FraudDetectionSystem.Transaction(100, currentTime, "BR");
        FraudDetectionSystem.FraudCheckResult result = fraudDetectionSystem.checkForFraud(transaction, new ArrayList<>(), new ArrayList<>());

        Assertions.assertFalse(result.isFraudulent);
        Assertions.assertFalse(result.isBlocked);
        Assertions.assertFalse(result.verificationRequired);
        Assertions.assertEquals(0, result.riskScore);
    }
}
