package com.example.hgh_assessment_prabesh_bhattarai.service.impl;

import com.example.hgh_assessment_prabesh_bhattarai.dto.request.CreateTrekOrderRequest;
import com.example.hgh_assessment_prabesh_bhattarai.dto.request.CreateTrekkerRequest;
import com.example.hgh_assessment_prabesh_bhattarai.entity.TrekOrder;
import com.example.hgh_assessment_prabesh_bhattarai.entity.Trekker;
import com.example.hgh_assessment_prabesh_bhattarai.exception.ConflictException;
import com.example.hgh_assessment_prabesh_bhattarai.exception.NotFoundException;
import com.example.hgh_assessment_prabesh_bhattarai.repository.TrekOrderRepository;
import com.example.hgh_assessment_prabesh_bhattarai.repository.TrekkerRepository;
import com.example.hgh_assessment_prabesh_bhattarai.service.TrekOrderService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TrekOrderServiceImpl implements TrekOrderService {

    private final TrekOrderRepository orderRepository;
    private final TrekkerRepository trekkerRepository;

    public TrekOrderServiceImpl(TrekOrderRepository orderRepository, TrekkerRepository trekkerRepository) {
        this.orderRepository = orderRepository;
        this.trekkerRepository = trekkerRepository;
    }

    @Override
    @Transactional
    public TrekOrder create(CreateTrekOrderRequest request) {
        if (orderRepository.existsByOrderRef(request.orderRef())) {
            throw new ConflictException("Order ref " + request.orderRef() + " already exists");
        }
        if (request.startsAt() != null && request.endsAt() != null
                && !request.endsAt().isAfter(request.startsAt())) {
            throw new ConflictException("endsAt must be after startsAt");
        }
        TrekOrder order = new TrekOrder();
        order.setOrderRef(request.orderRef());
        order.setTrekName(request.trekName());
        order.setStartsAt(request.startsAt());
        order.setEndsAt(request.endsAt());
        return orderRepository.save(order);
    }

    @Override
    @Transactional(readOnly = true)
    public TrekOrder get(Long orderId) {
        return orderRepository.findDetailById(orderId)
                .orElseThrow(() -> NotFoundException.order(orderId));
    }

    @Override
    @Transactional
    public Trekker addTrekker(Long orderId, CreateTrekkerRequest request) {
        TrekOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> NotFoundException.order(orderId));

        Trekker trekker = new Trekker();
        trekker.setTrekOrder(order);
        trekker.setFullName(request.fullName());
        trekker.setPhone(request.phone());
        return trekkerRepository.save(trekker);
    }
}
