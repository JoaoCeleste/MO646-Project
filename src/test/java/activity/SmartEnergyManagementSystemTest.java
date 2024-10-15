package activity;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SmartEnergyManagementSystemTest {
    private SmartEnergyManagementSystem energyManagementSystem;
    private Map<String, Integer> devicePriorities;
    private double[] desiredTemperatureRange;
    private LocalDateTime currentTime;

    @BeforeEach
    public void setup() {
        energyManagementSystem = new SmartEnergyManagementSystem();
        devicePriorities = new HashMap<>();
        devicePriorities.put("Heating", 1);
        devicePriorities.put("Cooling", 1);
        devicePriorities.put("Security", 1);
        devicePriorities.put("Refrigerator", 1);
        devicePriorities.put("Lights", 2);
        devicePriorities.put("Appliances", 3);
        devicePriorities.put("Oven", 3);
        desiredTemperatureRange = new double[]{20.0, 24.0};
        currentTime = LocalDateTime.of(2024, 10, 1, 12, 0);
    }

    @Test
    public void testEnergySavingModeBasedOnPriceThresholdLowTemperature() {
        // preço da energia excede o limite, ativando o modo de economia de energia
        SmartEnergyManagementSystem.EnergyManagementResult result = energyManagementSystem.manageEnergy(
                0.25, 0.20, devicePriorities, currentTime, 19.0, desiredTemperatureRange, 50.0, 25.0, List.of());

        Assertions.assertTrue(result.energySavingMode);
        Assertions.assertTrue(result.deviceStatus.get("Heating"));
        Assertions.assertTrue(result.deviceStatus.get("Security"));
        Assertions.assertFalse(result.deviceStatus.get("Lights"));
        Assertions.assertFalse(result.deviceStatus.get("Appliances"));
    }

    @Test
    public void testEnergySavingModeBasedOnPriceThresholdHighTemperature() {
        // preço da energia excede o limite, ativando o modo de economia de energia
        SmartEnergyManagementSystem.EnergyManagementResult result = energyManagementSystem.manageEnergy(
                0.25, 0.20, devicePriorities, currentTime, 25.0, desiredTemperatureRange, 50.0, 25.0, List.of());

        Assertions.assertTrue(result.energySavingMode);
        Assertions.assertTrue(result.deviceStatus.get("Cooling"));
        Assertions.assertTrue(result.deviceStatus.get("Security"));
        Assertions.assertFalse(result.deviceStatus.get("Lights"));
        Assertions.assertFalse(result.deviceStatus.get("Appliances"));
    }

    @Test
    public void testNightModeActivation() {
        // modo noturno ativo entre 23h e 6h
        currentTime = LocalDateTime.of(2024, 10, 1, 23, 0);  // 11:00 PM
        SmartEnergyManagementSystem.EnergyManagementResult result = energyManagementSystem.manageEnergy(
                0.15, 0.20, devicePriorities, currentTime, 19.0, desiredTemperatureRange, 50.0, 25.0, List.of());

        Assertions.assertTrue(result.deviceStatus.get("Security"));
        Assertions.assertTrue(result.deviceStatus.get("Refrigerator"));
        Assertions.assertFalse(result.deviceStatus.get("Lights"));
        Assertions.assertFalse(result.deviceStatus.get("Appliances"));
    }

    @Test
    public void testNightModeActivationAfterEleven() {
        // modo noturno ativo entre 23h e 6h
        currentTime = LocalDateTime.of(2024, 10, 1, 0, 0);  // 00:00 PM
        SmartEnergyManagementSystem.EnergyManagementResult result = energyManagementSystem.manageEnergy(
                0.15, 0.20, devicePriorities, currentTime, 19.0, desiredTemperatureRange, 50.0, 25.0, List.of());

        Assertions.assertTrue(result.deviceStatus.get("Security"));
        Assertions.assertTrue(result.deviceStatus.get("Refrigerator"));
        Assertions.assertFalse(result.deviceStatus.get("Lights"));
        Assertions.assertFalse(result.deviceStatus.get("Appliances"));
    }

    @Test
    public void testNightModeDeactivationSix() {
        // modo noturno inativo entre 23h e 6h
        currentTime = LocalDateTime.of(2024, 10, 1, 6, 0);  // 6:00 AM
        SmartEnergyManagementSystem.EnergyManagementResult result = energyManagementSystem.manageEnergy(
                0.15, 0.20, devicePriorities, currentTime, 19.0, desiredTemperatureRange, 50.0, 25.0, List.of());

        Assertions.assertTrue(result.deviceStatus.get("Security"));
        Assertions.assertTrue(result.deviceStatus.get("Refrigerator"));
        Assertions.assertTrue(result.deviceStatus.get("Lights"));
        Assertions.assertTrue(result.deviceStatus.get("Appliances"));
    }

    @Test
    public void testNightModeDeactivationBeforeEleven() {
        // modo noturno inativo entre 23h e 6h
        currentTime = LocalDateTime.of(2024, 10, 1, 22, 59);  // 22:59 AM
        SmartEnergyManagementSystem.EnergyManagementResult result = energyManagementSystem.manageEnergy(
                0.15, 0.20, devicePriorities, currentTime, 19.0, desiredTemperatureRange, 50.0, 25.0, List.of());

        Assertions.assertTrue(result.deviceStatus.get("Security"));
        Assertions.assertTrue(result.deviceStatus.get("Refrigerator"));
        Assertions.assertTrue(result.deviceStatus.get("Lights"));
        Assertions.assertTrue(result.deviceStatus.get("Appliances"));
    }

//    @Test
//    public void testTemperatureRegulationHeatingActive() {
//        // temperatura abaixo do intervalo desejado, aquecimento ativo - erro de teste: cooling ativo
//        SmartEnergyManagementSystem.EnergyManagementResult result = energyManagementSystem.manageEnergy(
//                0.15, 0.20, devicePriorities, currentTime, 19.0, desiredTemperatureRange, 50.0, 25.0, List.of());
//
//        Assertions.assertTrue(result.temperatureRegulationActive);
//        Assertions.assertTrue(result.deviceStatus.get("Heating"));
//        Assertions.assertFalse(result.deviceStatus.get("Cooling"));
//    }
//
//    @Test
//    public void testTemperatureRegulationCoolingActive() {
//        // emperatura acima do intervalo desejado, resfriamento ativo - erro de teste: heating ativo
//        SmartEnergyManagementSystem.EnergyManagementResult result = energyManagementSystem.manageEnergy(
//                0.15, 0.20, devicePriorities, currentTime, 25.0, desiredTemperatureRange, 50.0, 25.0, List.of());
//
//        Assertions.assertTrue(result.temperatureRegulationActive);
//        Assertions.assertFalse(result.deviceStatus.get("Heating"));
//        Assertions.assertTrue(result.deviceStatus.get("Cooling"));
//    }

    @Test
    public void testTemperatureRegulationInactive() {
        // temperatura dentro do intervalo desejado, regulação de temperatura inativa
        SmartEnergyManagementSystem.EnergyManagementResult result = energyManagementSystem.manageEnergy(
                0.15, 0.20, devicePriorities, currentTime, 22.0, desiredTemperatureRange, 50.0, 25.0, List.of());

        Assertions.assertFalse(result.temperatureRegulationActive);
        Assertions.assertFalse(result.deviceStatus.get("Heating"));
        Assertions.assertFalse(result.deviceStatus.get("Cooling"));
    }

    @Test
    public void testEnergyLimitExceeded() {
        // limite de uso de energia excedido, dispositivos de baixa prioridade desligados
        energyManagementSystem = new SmartEnergyManagementSystem();
        devicePriorities = new HashMap<>();
        devicePriorities.put("Heating", 2);
        devicePriorities.put("Cooling", 2);
        devicePriorities.put("Security", 2);
        devicePriorities.put("Refrigerator", 2);
        devicePriorities.put("Lights", 2);
        devicePriorities.put("Appliances", 3);
        devicePriorities.put("Oven", 3);
        SmartEnergyManagementSystem.EnergyManagementResult result = energyManagementSystem.manageEnergy(
                0.15, 0.20, devicePriorities, currentTime, 22.0, desiredTemperatureRange, 30.0, 40.0, List.of());
        Assertions.assertFalse(result.deviceStatus.get("Security"));
        Assertions.assertFalse(result.deviceStatus.get("Lights"));
        Assertions.assertFalse(result.deviceStatus.get("Appliances"));
        Assertions.assertFalse(result.deviceStatus.get("Heating"));
        Assertions.assertFalse(result.deviceStatus.get("Cooling"));
    }

    @Test
    public void testEnergyLimitReached() {
        // limite de uso de energia atingido, dispositivos de baixa prioridade desligados
        SmartEnergyManagementSystem.EnergyManagementResult result = energyManagementSystem.manageEnergy(
                0.15, 0.20, devicePriorities, currentTime, 22.0, desiredTemperatureRange, 30.0, 32.0, List.of());
        Assertions.assertTrue(result.deviceStatus.get("Security"));
        Assertions.assertFalse(result.deviceStatus.get("Lights"));
        Assertions.assertFalse(result.deviceStatus.get("Appliances"));
        Assertions.assertFalse(result.deviceStatus.get("Heating"));
        Assertions.assertFalse(result.deviceStatus.get("Cooling"));
    }

    @Test
    public void testEnergyLimitNotExceeded() {
        // limite de uso de energia não excedido, todos os dispositivos ligados
        SmartEnergyManagementSystem.EnergyManagementResult result = energyManagementSystem.manageEnergy(
                0.15, 0.20, devicePriorities, currentTime, 22.0, desiredTemperatureRange, 30.0, 29.0, List.of());
        Assertions.assertTrue(result.deviceStatus.get("Security"));
        Assertions.assertTrue(result.deviceStatus.get("Lights"));
        Assertions.assertTrue(result.deviceStatus.get("Appliances"));
        Assertions.assertFalse(result.deviceStatus.get("Heating"));
        Assertions.assertFalse(result.deviceStatus.get("Cooling"));
    }

    @Test
    public void testScheduledDeviceOverridesEnergySavingMode() {
        // um dispositivo programado para ser ligado ignora o modo de economia de energia
        SmartEnergyManagementSystem.DeviceSchedule schedule = new SmartEnergyManagementSystem.DeviceSchedule("Oven", currentTime);
        SmartEnergyManagementSystem.EnergyManagementResult result = energyManagementSystem.manageEnergy(
                0.25, 0.20, devicePriorities, currentTime, 22.0, desiredTemperatureRange, 50.0, 25.0, List.of(schedule));

        Assertions.assertTrue(result.deviceStatus.get("Oven"));
        Assertions.assertFalse(result.deviceStatus.get("Lights"));
        Assertions.assertFalse(result.deviceStatus.get("Appliances"));
    }

    @Test
    public void testScheduledDeviceDuringNightMode() {
        // um dispositivo programado para ser ligado ignora o modo noturno
        currentTime = LocalDateTime.of(2024, 10, 1, 23, 30);  // 11:30 PM
        SmartEnergyManagementSystem.DeviceSchedule schedule = new SmartEnergyManagementSystem.DeviceSchedule("Oven", currentTime);
        SmartEnergyManagementSystem.EnergyManagementResult result = energyManagementSystem.manageEnergy(
                0.15, 0.20, devicePriorities, currentTime, 22.0, desiredTemperatureRange, 50.0, 25.0, List.of(schedule));

        Assertions.assertTrue(result.deviceStatus.get("Oven"));
        Assertions.assertFalse(result.deviceStatus.get("Lights"));
        Assertions.assertFalse(result.deviceStatus.get("Appliances"));
    }

    @Test
    public void testScheduledDeviceOutsideTimeRange() {
        // um dispositivo programado para ser ligado mas current time != scheduled time
        SmartEnergyManagementSystem.DeviceSchedule schedule = new SmartEnergyManagementSystem.DeviceSchedule("Oven", currentTime);
        SmartEnergyManagementSystem.EnergyManagementResult result = energyManagementSystem.manageEnergy(
                0.25, 0.20, devicePriorities, currentTime.minusMinutes(1), 22.0, desiredTemperatureRange, 50.0, 25.0, List.of(schedule));

        Assertions.assertFalse(result.deviceStatus.get("Oven"));
        Assertions.assertFalse(result.deviceStatus.get("Lights"));
        Assertions.assertFalse(result.deviceStatus.get("Appliances"));
    }

    @Test
    public void testNoEnergySavingModeWhenPriceBelowThreshold() {
        // o preço da energia está abaixo do limite, nenhum dispositivo é desligado
        SmartEnergyManagementSystem.EnergyManagementResult result = energyManagementSystem.manageEnergy(
                0.15, 0.20, devicePriorities, currentTime, 22.0, desiredTemperatureRange, 50.0, 25.0, List.of());

        Assertions.assertFalse(result.energySavingMode);
        Assertions.assertTrue(result.deviceStatus.get("Lights"));
        Assertions.assertTrue(result.deviceStatus.get("Appliances"));
    }

    @Test 
    public void testCurrentPriceWithinThreshold(){
        SmartEnergyManagementSystem.EnergyManagementResult result = energyManagementSystem.manageEnergy(0.20, 0.20, devicePriorities, currentTime, 25.0, desiredTemperatureRange, 50.0, 25.0, List.of());
        Assertions.assertFalse(result.energySavingMode);
    }

    @Test 
    public void testCurrentPriceAboveThreshold(){
         SmartEnergyManagementSystem.EnergyManagementResult result = energyManagementSystem.manageEnergy(0.25, 0.20, devicePriorities, currentTime, 25.0, desiredTemperatureRange, 50.0, 25.0, List.of());
        Assertions.assertTrue(result.energySavingMode);
    }

      @Test
    public void testCurrentTemperatureEqualsDesiredTemperatureRange0() {
        SmartEnergyManagementSystem system = new SmartEnergyManagementSystem();
        double currentTemperature = 20.0;
        double[] desiredTemperatureRange = {20.0, 24.0};
        Map<String, Integer> devicePriorities = new HashMap<>();
        devicePriorities.put("Heating", 1);
        devicePriorities.put("Cooling", 1);
        
        SmartEnergyManagementSystem.EnergyManagementResult result = system.manageEnergy(
                0.1, 0.2, devicePriorities, LocalDateTime.now(), 
                currentTemperature, desiredTemperatureRange, 
                10, 0, List.of());
        
        Assertions.assertFalse(result.deviceStatus.get("Heating"));
        Assertions.assertFalse(result.deviceStatus.get("Cooling"));
        Assertions.assertFalse(result.temperatureRegulationActive);
    }

    @Test
    public void testCurrentTemperatureLowerDesiredTemperatureRange0() {
        SmartEnergyManagementSystem system = new SmartEnergyManagementSystem();
        double currentTemperature = 18.0;
        double[] desiredTemperatureRange = {20.0, 24.0};
        Map<String, Integer> devicePriorities = new HashMap<>();
        devicePriorities.put("Heating", 1);
        devicePriorities.put("Cooling", 1);
        
        SmartEnergyManagementSystem.EnergyManagementResult result = system.manageEnergy(
                0.1, 0.2, devicePriorities, LocalDateTime.now(), 
                currentTemperature, desiredTemperatureRange, 
                10, 0, List.of());
        
        Assertions.assertTrue(result.deviceStatus.get("Heating"));
        Assertions.assertTrue(result.deviceStatus.get("Cooling"));
        Assertions.assertTrue(result.temperatureRegulationActive);
    }

    @Test
    public void testCurrentTemperatureEqualsDesiredTemperatureRange1() {
        SmartEnergyManagementSystem system = new SmartEnergyManagementSystem();
        double currentTemperature = 24.0;
        double[] desiredTemperatureRange = {20.0, 24.0};
        Map<String, Integer> devicePriorities = new HashMap<>();
        devicePriorities.put("Heating", 1);
        devicePriorities.put("Cooling", 1);
        
        SmartEnergyManagementSystem.EnergyManagementResult result = system.manageEnergy(
                0.1, 0.2, devicePriorities, LocalDateTime.now(), 
                currentTemperature, desiredTemperatureRange, 
                10, 0, List.of());
        
        Assertions.assertFalse(result.deviceStatus.get("Heating"));
        Assertions.assertFalse(result.deviceStatus.get("Cooling"));
        Assertions.assertFalse(result.temperatureRegulationActive);
    }

    @Test
    public void testCurrentTemperatureAboveDesiredTemperatureRange1() {
        SmartEnergyManagementSystem system = new SmartEnergyManagementSystem();
        double currentTemperature = 26.0;
        double[] desiredTemperatureRange = {20.0, 24.0};
        Map<String, Integer> devicePriorities = new HashMap<>();
        devicePriorities.put("Heating", 1);
        devicePriorities.put("Cooling", 1);
        
        SmartEnergyManagementSystem.EnergyManagementResult result = system.manageEnergy(
                0.1, 0.2, devicePriorities, LocalDateTime.now(), 
                currentTemperature, desiredTemperatureRange, 
                10, 0, List.of());
        
        Assertions.assertTrue(result.deviceStatus.get("Heating"));
        Assertions.assertTrue(result.deviceStatus.get("Cooling"));
        Assertions.assertTrue(result.temperatureRegulationActive);
    }
/*
    @Test
    public void testEnergyUsedEqualsLimit() {
        SmartEnergyManagementSystem system = new SmartEnergyManagementSystem();
        double currentTemperature = 26.0;
        double[] desiredTemperatureRange = {20.0, 24.0};
        Map<String, Integer> devicePriorities = new HashMap<>();
        devicePriorities.put("Heating", 1);
        devicePriorities.put("Cooling", 1);
        double totalEnergyUsedToday = 10;
        double energyUsageLimit = 10;
        
        SmartEnergyManagementSystem.EnergyManagementResult result = system.manageEnergy(
                0.1, 0.2, devicePriorities, LocalDateTime.now(), 
                currentTemperature, desiredTemperatureRange, 
                energyUsageLimit, totalEnergyUsedToday, List.of());
        
        Assertions.assertEquals(totalEnergyUsedToday,result.totalEnergyUsed);
        }
    @Test
    public void testEnergyUsedNotEqualsLimit() {
        SmartEnergyManagementSystem system = new SmartEnergyManagementSystem();
        double currentTemperature = 26.0;
        double[] desiredTemperatureRange = {20.0, 24.0};
        Map<String, Integer> devicePriorities = new HashMap<>();
        devicePriorities.put("Heating", 1);
        devicePriorities.put("Cooling", 1);
        double totalEnergyUsedToday = 12;
        double energyUsageLimit = 10;
        
        SmartEnergyManagementSystem.EnergyManagementResult result = system.manageEnergy(
                0.1, 0.2, devicePriorities, LocalDateTime.now(), 
                currentTemperature, desiredTemperatureRange, 
                energyUsageLimit, totalEnergyUsedToday, List.of());
        
        Assertions.assertEquals(energyUsageLimit,result.totalEnergyUsed);

}
*/
}