package activity;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
            FraudDetectionSystem.Transaction transaction = new FraudDetectionSystem.Transaction(100, currentTime.minusMinutes(i * 2), "BR");
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
    public void testExcessiveTransactionsNotInLastHour() {
        // mais de 10 transações, mas não na última hora
        ArrayList<FraudDetectionSystem.Transaction> previousTransactions = new ArrayList<>();
        for (int i = 0; i < 11; i++) {
            FraudDetectionSystem.Transaction transaction = new FraudDetectionSystem.Transaction(100, currentTime.minusMinutes(i * 30), "BR");
            previousTransactions.add(transaction);
        }

        FraudDetectionSystem.Transaction transaction = new FraudDetectionSystem.Transaction(100, currentTime, "BR");
        FraudDetectionSystem.FraudCheckResult result = fraudDetectionSystem.checkForFraud(transaction, previousTransactions, new ArrayList<>());

        Assertions.assertFalse(result.isFraudulent);
        Assertions.assertFalse(result.isBlocked);
        Assertions.assertFalse(result.verificationRequired);
        Assertions.assertEquals(0, result.riskScore);
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

    @Test
    public void testMultipleRulesTriggeredAll() {
        // várias regras acionadas
        ArrayList<FraudDetectionSystem.Transaction> previousTransactions = new ArrayList<>();
        for (int i = 0; i < 11; i++) {
            FraudDetectionSystem.Transaction transaction = new FraudDetectionSystem.Transaction(100, currentTime.minusMinutes(i * 5), "BR");
            previousTransactions.add(transaction);
        }

        FraudDetectionSystem.Transaction transaction = new FraudDetectionSystem.Transaction(10001, currentTime, "USA");
        FraudDetectionSystem.FraudCheckResult result = fraudDetectionSystem.checkForFraud(transaction, previousTransactions, List.of("USA"));

        Assertions.assertTrue(result.isFraudulent);
        Assertions.assertTrue(result.isBlocked);
        Assertions.assertTrue(result.verificationRequired);
        Assertions.assertEquals(100, result.riskScore);
    }

    @Test
    public void testMultipleRules12Triggered() {
        // regras 1 e 2 acionadas
        ArrayList<FraudDetectionSystem.Transaction> previousTransactions = new ArrayList<>();
        for (int i = 0; i < 11; i++) {
            FraudDetectionSystem.Transaction transaction = new FraudDetectionSystem.Transaction(100, currentTime.minusMinutes(i * 5), "BR");
            previousTransactions.add(transaction);
        }

        FraudDetectionSystem.Transaction transaction = new FraudDetectionSystem.Transaction(10001, currentTime, "BR");
        FraudDetectionSystem.FraudCheckResult result = fraudDetectionSystem.checkForFraud(transaction, previousTransactions, new ArrayList<>());

        Assertions.assertTrue(result.isFraudulent);
        Assertions.assertTrue(result.isBlocked);
        Assertions.assertTrue(result.verificationRequired);
        Assertions.assertEquals(80, result.riskScore);
    }

    @Test
    public void testMultipleRules13Triggered() {
        // regras 1 e 3 acionadas
        FraudDetectionSystem.Transaction transaction1 = new FraudDetectionSystem.Transaction(10001, currentTime.minusMinutes(10), "BR");
        FraudDetectionSystem.Transaction transaction2 = new FraudDetectionSystem.Transaction(100, currentTime, "USA");
        FraudDetectionSystem.FraudCheckResult result = fraudDetectionSystem.checkForFraud(transaction1, List.of(transaction2), new ArrayList<>());

        Assertions.assertTrue(result.isFraudulent);
        Assertions.assertFalse(result.isBlocked);
        Assertions.assertTrue(result.verificationRequired);
        Assertions.assertEquals(70, result.riskScore);
    }

    @Test
    public void testMultipleRules14Triggered() {
        // regras 1 e 4 acionadas
        FraudDetectionSystem.Transaction transaction = new FraudDetectionSystem.Transaction(10001, currentTime, "HighRiskLocation");
        FraudDetectionSystem.FraudCheckResult result = fraudDetectionSystem.checkForFraud(transaction, new ArrayList<>(), List.of("HighRiskLocation"));

        Assertions.assertTrue(result.isFraudulent);
        Assertions.assertTrue(result.isBlocked);
        Assertions.assertTrue(result.verificationRequired);
        Assertions.assertEquals(100, result.riskScore); // max risk score = 100
    }

    @Test
    public void testMultipleRules23Triggered() {
        // regras 2 e 3 acionadas
        ArrayList<FraudDetectionSystem.Transaction> previousTransactions = new ArrayList<>();
        for (int i = 0; i < 11; i++) {
            FraudDetectionSystem.Transaction transaction = new FraudDetectionSystem.Transaction(100, currentTime.minusMinutes(i * 2), "BR");
            previousTransactions.add(transaction);
        }

        FraudDetectionSystem.Transaction transaction = new FraudDetectionSystem.Transaction(100, currentTime, "USA");
        FraudDetectionSystem.FraudCheckResult result = fraudDetectionSystem.checkForFraud(transaction, previousTransactions, new ArrayList<>());

        Assertions.assertTrue(result.isFraudulent);
        Assertions.assertTrue(result.isBlocked);
        Assertions.assertTrue(result.verificationRequired);
        Assertions.assertEquals(50, result.riskScore);
    }

    @Test
    public void testMultipleRules24Triggered() {
        // regras 2 e 4 acionadas
        ArrayList<FraudDetectionSystem.Transaction> previousTransactions = new ArrayList<>();
        for (int i = 0; i < 11; i++) {
            FraudDetectionSystem.Transaction transaction = new FraudDetectionSystem.Transaction(100, currentTime.minusMinutes(i * 2), "BR");
            previousTransactions.add(transaction);
        }

        FraudDetectionSystem.Transaction transaction = new FraudDetectionSystem.Transaction(100, currentTime, "HighRiskLocation");
        FraudDetectionSystem.FraudCheckResult result = fraudDetectionSystem.checkForFraud(transaction, previousTransactions, List.of("HighRiskLocation"));

        Assertions.assertTrue(result.isFraudulent);
        Assertions.assertTrue(result.isBlocked);
        Assertions.assertTrue(result.verificationRequired);
        Assertions.assertEquals(100, result.riskScore);
    }


    @Test
    public void testTransactionLimit(){
        ArrayList<FraudDetectionSystem.Transaction> previousTransactions = new ArrayList<>();
        FraudDetectionSystem.Transaction transaction1 = new FraudDetectionSystem.Transaction(10000, currentTime.minusMinutes(10), "BR");
         FraudDetectionSystem.FraudCheckResult result = fraudDetectionSystem.checkForFraud(transaction1, previousTransactions, List.of("HighRiskLocation"));
        Assertions.assertFalse(result.isFraudulent);
    }

     @Test
    public void testTransactionAboveLimit(){
        ArrayList<FraudDetectionSystem.Transaction> previousTransactions = new ArrayList<>();
        FraudDetectionSystem.Transaction transaction1 = new FraudDetectionSystem.Transaction(10001, currentTime.minusMinutes(10), "BR");
        FraudDetectionSystem.FraudCheckResult result = fraudDetectionSystem.checkForFraud(transaction1, previousTransactions, List.of("HighRiskLocation"));
        Assertions.assertTrue(result.isFraudulent);
    }

    @Test
    public void testTransactionCountLimit(){
        ArrayList<FraudDetectionSystem.Transaction> previousTransactions = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            FraudDetectionSystem.Transaction transaction = new FraudDetectionSystem.Transaction(10000, currentTime.minusMinutes(10), "BR");
            previousTransactions.add(transaction);
        }
        FraudDetectionSystem.Transaction transaction = new FraudDetectionSystem.Transaction(10000, currentTime.minusMinutes(10), "BR");
        FraudDetectionSystem.FraudCheckResult result = fraudDetectionSystem.checkForFraud(transaction, previousTransactions, List.of("HighRiskLocation"));
        Assertions.assertFalse(result.isBlocked);
    }


  @Test
    public void testTransactionCountAboveLimit(){
        ArrayList<FraudDetectionSystem.Transaction> previousTransactions = new ArrayList<>();
        for (int i = 0; i < 11; i++) {
            FraudDetectionSystem.Transaction transaction = new FraudDetectionSystem.Transaction(10000, currentTime.minusMinutes(10), "BR");
            previousTransactions.add(transaction);
        }
        FraudDetectionSystem.Transaction transaction = new FraudDetectionSystem.Transaction(10000, currentTime.minusMinutes(10), "BR");
        FraudDetectionSystem.FraudCheckResult result = fraudDetectionSystem.checkForFraud(transaction, previousTransactions, List.of("HighRiskLocation"));
        Assertions.assertTrue(result.isBlocked);
    }

    @Test
    public void testLocationChangeWithin30Minutes() {
        ArrayList<FraudDetectionSystem.Transaction> previousTransactions = new ArrayList<>();
        FraudDetectionSystem.Transaction transaction1 = new FraudDetectionSystem.Transaction(5000, currentTime.minusMinutes(29), "US");
        previousTransactions.add(transaction1);

        FraudDetectionSystem.Transaction transaction = new FraudDetectionSystem.Transaction(5000, currentTime, "BR");
        FraudDetectionSystem.FraudCheckResult result = fraudDetectionSystem.checkForFraud(transaction, previousTransactions, List.of("HighRiskLocation"));

        Assertions.assertTrue(result.isFraudulent);
    }

    @Test
    public void testLocationSameWithin30Minutes() {
        ArrayList<FraudDetectionSystem.Transaction> previousTransactions = new ArrayList<>();
        FraudDetectionSystem.Transaction transaction1 = new FraudDetectionSystem.Transaction(5000, currentTime.minusMinutes(29), "BR");
        previousTransactions.add(transaction1);

        FraudDetectionSystem.Transaction transaction = new FraudDetectionSystem.Transaction(5000, currentTime, "BR");
        FraudDetectionSystem.FraudCheckResult result = fraudDetectionSystem.checkForFraud(transaction, previousTransactions, List.of("HighRiskLocation"));

        Assertions.assertFalse(result.isFraudulent);
    }

    @Test
    public void testLocationChangeOutside30Minutes() {
        ArrayList<FraudDetectionSystem.Transaction> previousTransactions = new ArrayList<>();
        FraudDetectionSystem.Transaction transaction1 = new FraudDetectionSystem.Transaction(5000, currentTime.minusMinutes(31), "US");
        previousTransactions.add(transaction1);

        FraudDetectionSystem.Transaction transaction = new FraudDetectionSystem.Transaction(5000, currentTime, "BR");
        FraudDetectionSystem.FraudCheckResult result = fraudDetectionSystem.checkForFraud(transaction, previousTransactions, List.of("HighRiskLocation"));

        Assertions.assertFalse(result.isFraudulent);
    }

    @Test
    public void testLocationSameOutside30Minutes() {
        ArrayList<FraudDetectionSystem.Transaction> previousTransactions = new ArrayList<>();
        FraudDetectionSystem.Transaction transaction1 = new FraudDetectionSystem.Transaction(5000, currentTime.minusMinutes(31), "BR");
        previousTransactions.add(transaction1);

        FraudDetectionSystem.Transaction transaction = new FraudDetectionSystem.Transaction(5000, currentTime, "BR");
        FraudDetectionSystem.FraudCheckResult result = fraudDetectionSystem.checkForFraud(transaction, previousTransactions, List.of("HighRiskLocation"));

        Assertions.assertFalse(result.isFraudulent);
    }

    @Test
public void testLocationChangeExactly30Minutes() {
    ArrayList<FraudDetectionSystem.Transaction> previousTransactions = new ArrayList<>();
    FraudDetectionSystem.Transaction transaction1 = new FraudDetectionSystem.Transaction(5000, currentTime.minusMinutes(30), "US");
    previousTransactions.add(transaction1);

    FraudDetectionSystem.Transaction transaction = new FraudDetectionSystem.Transaction(5000, currentTime, "BR");
    FraudDetectionSystem.FraudCheckResult result = fraudDetectionSystem.checkForFraud(transaction, previousTransactions, List.of("HighRiskLocation"));

    Assertions.assertFalse(result.isFraudulent);
}

    @Test
public void testTransactionExactly60Minutes() {
    ArrayList<FraudDetectionSystem.Transaction> previousTransactions = new ArrayList<>();
    FraudDetectionSystem.Transaction transaction1 = new FraudDetectionSystem.Transaction(5000, currentTime.minusMinutes(60), "BR");
    previousTransactions.add(transaction1);

    FraudDetectionSystem.Transaction transaction = new FraudDetectionSystem.Transaction(5000, currentTime, "BR");
    FraudDetectionSystem.FraudCheckResult result = fraudDetectionSystem.checkForFraud(transaction, previousTransactions, List.of("HighRiskLocation"));

    Assertions.assertFalse(result.isBlocked);
}

    @Test
    public void testTransactionWithin60Minutes() {
        ArrayList<FraudDetectionSystem.Transaction> previousTransactions = new ArrayList<>();
        FraudDetectionSystem.Transaction transaction1 = new FraudDetectionSystem.Transaction(5000, currentTime.minusMinutes(59), "BR");
        previousTransactions.add(transaction1);

        FraudDetectionSystem.Transaction transaction = new FraudDetectionSystem.Transaction(5000, currentTime, "BR");
        FraudDetectionSystem.FraudCheckResult result = fraudDetectionSystem.checkForFraud(transaction, previousTransactions, List.of("HighRiskLocation"));

        Assertions.assertFalse(result.isBlocked);
    }

    @Test
    public void testTransactionOutside60Minutes() {
        ArrayList<FraudDetectionSystem.Transaction> previousTransactions = new ArrayList<>();
        FraudDetectionSystem.Transaction transaction1 = new FraudDetectionSystem.Transaction(5000, currentTime.minusMinutes(61), "BR");
        previousTransactions.add(transaction1);

        FraudDetectionSystem.Transaction transaction = new FraudDetectionSystem.Transaction(5000, currentTime, "BR");
        FraudDetectionSystem.FraudCheckResult result = fraudDetectionSystem.checkForFraud(transaction, previousTransactions, List.of("HighRiskLocation"));

        Assertions.assertFalse(result.isBlocked);
    }

    @Test
    public void testMultipleTransactionsExactly60Minutes() {
        ArrayList<FraudDetectionSystem.Transaction> previousTransactions = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            previousTransactions.add(new FraudDetectionSystem.Transaction(5000, currentTime.minusMinutes(60), "BR"));
        }

        FraudDetectionSystem.Transaction transaction = new FraudDetectionSystem.Transaction(5000, currentTime, "BR");
        FraudDetectionSystem.FraudCheckResult result = fraudDetectionSystem.checkForFraud(transaction, previousTransactions, List.of("HighRiskLocation"));

        Assertions.assertFalse(result.isBlocked);
    }

    @Test
    public void testMultipleTransactionsWithin60Minutes() {
        ArrayList<FraudDetectionSystem.Transaction> previousTransactions = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            previousTransactions.add(new FraudDetectionSystem.Transaction(5000, currentTime.minusMinutes(59), "BR"));
        }

        FraudDetectionSystem.Transaction transaction = new FraudDetectionSystem.Transaction(5000, currentTime, "BR");
        FraudDetectionSystem.FraudCheckResult result = fraudDetectionSystem.checkForFraud(transaction, previousTransactions, List.of("HighRiskLocation"));

        Assertions.assertFalse(result.isBlocked);
    }

    @Test
    public void testMultipleTransactionsOutside60Minutes() {
        ArrayList<FraudDetectionSystem.Transaction> previousTransactions = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            previousTransactions.add(new FraudDetectionSystem.Transaction(5000, currentTime.minusMinutes(61), "BR"));
        }

        FraudDetectionSystem.Transaction transaction = new FraudDetectionSystem.Transaction(5000, currentTime, "BR");
        FraudDetectionSystem.FraudCheckResult result = fraudDetectionSystem.checkForFraud(transaction, previousTransactions, List.of("HighRiskLocation"));

        Assertions.assertFalse(result.isBlocked);
    }

    @Test
    public void testMixedTransactionsWithinAndOutside60Minutes() {
        ArrayList<FraudDetectionSystem.Transaction> previousTransactions = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            previousTransactions.add(new FraudDetectionSystem.Transaction(5000, currentTime.minusMinutes(59), "BR"));
        }
        for (int i = 0; i < 2; i++) {
            previousTransactions.add(new FraudDetectionSystem.Transaction(5000, currentTime.minusMinutes(61), "BR"));
        }

        FraudDetectionSystem.Transaction transaction = new FraudDetectionSystem.Transaction(5000, currentTime, "BR");
        FraudDetectionSystem.FraudCheckResult result = fraudDetectionSystem.checkForFraud(transaction, previousTransactions, List.of("HighRiskLocation"));

        Assertions.assertFalse(result.isBlocked);
    }


}
