package activity;

import org.junit.Before;
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
    public void testEnergySavingModeBasedOnPriceThreshold() {
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
    public void testNightModeActivation() {
        // modo noturno ativo entre 23h e 6h
        currentTime = LocalDateTime.of(2024, 10, 1, 23, 30);  // 11:30 PM
        SmartEnergyManagementSystem.EnergyManagementResult result = energyManagementSystem.manageEnergy(
                0.15, 0.20, devicePriorities, currentTime, 19.0, desiredTemperatureRange, 50.0, 25.0, List.of());

        Assertions.assertTrue(result.deviceStatus.get("Security"));
        Assertions.assertTrue(result.deviceStatus.get("Refrigerator"));
        Assertions.assertFalse(result.deviceStatus.get("Lights"));
        Assertions.assertFalse(result.deviceStatus.get("Appliances"));
    }

    @Test
    public void testTemperatureRegulationHeatingActive() {
        // temperatura abaixo do intervalo desejado, aquecimento ativo - erro de teste: cooling ativo
        SmartEnergyManagementSystem.EnergyManagementResult result = energyManagementSystem.manageEnergy(
                0.15, 0.20, devicePriorities, currentTime, 19.0, desiredTemperatureRange, 50.0, 25.0, List.of());

        Assertions.assertTrue(result.temperatureRegulationActive);
        Assertions.assertTrue(result.deviceStatus.get("Heating"));
        Assertions.assertFalse(result.deviceStatus.get("Cooling"));
    }

    @Test
    public void testTemperatureRegulationCoolingActive() {
        // emperatura acima do intervalo desejado, resfriamento ativo - erro de teste: heating ativo
        SmartEnergyManagementSystem.EnergyManagementResult result = energyManagementSystem.manageEnergy(
                0.15, 0.20, devicePriorities, currentTime, 25.0, desiredTemperatureRange, 50.0, 25.0, List.of());

        Assertions.assertTrue(result.temperatureRegulationActive);
        Assertions.assertFalse(result.deviceStatus.get("Heating"));
        Assertions.assertTrue(result.deviceStatus.get("Cooling"));
    }

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
        SmartEnergyManagementSystem.EnergyManagementResult result = energyManagementSystem.manageEnergy(
                0.15, 0.20, devicePriorities, currentTime, 22.0, desiredTemperatureRange, 30.0, 31.0, List.of());
        Assertions.assertTrue(result.deviceStatus.get("Security"));
        Assertions.assertFalse(result.deviceStatus.get("Lights"));
        Assertions.assertFalse(result.deviceStatus.get("Appliances"));
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
    public void testNoEnergySavingModeWhenPriceBelowThreshold() {
        // o preço da energia está abaixo do limite, nenhum dispositivo é desligado
        SmartEnergyManagementSystem.EnergyManagementResult result = energyManagementSystem.manageEnergy(
                0.15, 0.20, devicePriorities, currentTime, 22.0, desiredTemperatureRange, 50.0, 25.0, List.of());

        Assertions.assertFalse(result.energySavingMode);
        Assertions.assertTrue(result.deviceStatus.get("Lights"));
        Assertions.assertTrue(result.deviceStatus.get("Appliances"));
    }
}
