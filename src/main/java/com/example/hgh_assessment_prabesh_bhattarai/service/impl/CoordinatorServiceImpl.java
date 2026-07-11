package com.example.hgh_assessment_prabesh_bhattarai.service.impl;

import com.example.hgh_assessment_prabesh_bhattarai.dto.request.CreateCoordinatorRequest;
import com.example.hgh_assessment_prabesh_bhattarai.entity.Coordinator;
import com.example.hgh_assessment_prabesh_bhattarai.exception.ConflictException;
import com.example.hgh_assessment_prabesh_bhattarai.repository.CoordinatorRepository;
import com.example.hgh_assessment_prabesh_bhattarai.service.CoordinatorService;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CoordinatorServiceImpl implements CoordinatorService {

    private final CoordinatorRepository coordinatorRepository;

    public CoordinatorServiceImpl(CoordinatorRepository coordinatorRepository) {
        this.coordinatorRepository = coordinatorRepository;
    }

    @Override
    @Transactional
    public Coordinator register(CreateCoordinatorRequest request) {
        if (coordinatorRepository.existsByName(request.name())) {
            throw new ConflictException("Coordinator " + request.name() + " is already registered");
        }
        Coordinator coordinator = new Coordinator();
        coordinator.setName(request.name());
        return coordinatorRepository.save(coordinator);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Coordinator> list() {
        return coordinatorRepository.findAll(Sort.by(Sort.Direction.ASC, "name"));
    }
}
