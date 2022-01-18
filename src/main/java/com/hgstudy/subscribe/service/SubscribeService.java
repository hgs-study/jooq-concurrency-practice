package com.hgstudy.subscribe.service;

import com.hgstudy.subscribe.domain.Subscribe;
import com.hgstudy.subscribe.repository.SubscribeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.exception.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscribeService {

    private final SubscribeRepository subscribeRepository;

    public Subscribe findByRegister(String register){
        return subscribeRepository.findByRegister(register);
    }

    public Subscribe findOptimisticLockByRegister(String register){
        return subscribeRepository.findOptimisticLockByRegister(register);
    }

    public void updateHit(Long hit, String register){
        subscribeRepository.updateHit(hit, register);
    }

    public void updateOptimisticLockHit(Long hit, String register){
        subscribeRepository.updateOptimisticLockHit(hit, register);
    }

    public void deleteByRegister(String register){
        subscribeRepository.deleteByRegister(register);
    }

    public void plusHitPessimistic(String register){
        subscribeRepository.plusHitPessimistic(register);
    }

    @Transactional
    public void testPlusHitOptimistic(String register){
        try {
            subscribeRepository.plusHitOptimistic(register);
        }catch (DataAccessException e ) {
            this.testPlusHitOptimistic(register);
        }
    }
}
