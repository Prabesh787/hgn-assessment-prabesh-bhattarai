package com.example.hgh_assessment_prabesh_bhattarai.service.impl;

import com.example.hgh_assessment_prabesh_bhattarai.dto.request.CreateDeviceRequest;
import com.example.hgh_assessment_prabesh_bhattarai.entity.Device;
import com.example.hgh_assessment_prabesh_bhattarai.exception.ConflictException;
import com.example.hgh_assessment_prabesh_bhattarai.repository.DeviceRepository;
import com.example.hgh_assessment_prabesh_bhattarai.service.DeviceService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DeviceServiceImpl implements DeviceService {

    private final DeviceRepository deviceRepository;

    public DeviceServiceImpl(DeviceRepository deviceRepository) {
        this.deviceRepository = deviceRepository;
    }

    @Override
    @Transactional
    public Device register(CreateDeviceRequest request) {
        if (deviceRepository.existsByDeviceSerial(request.deviceSerial())) {
            throw new ConflictException("Device serial " + request.deviceSerial() + " is already registered");
        }
        Device device = new Device();
        device.setDeviceSerial(request.deviceSerial());
        return deviceRepository.save(device);
    }
}
