package com.hgstudy.subscribe.service;

import com.hgstudy.subscribe.domain.Subscribe;
import com.hgstudy.subscribe.repository.SubscribeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SubscribeService {

    private final SubscribeRepository subscribeRepository;

    public Subscribe findByRegister(String register){
        return subscribeRepository.findByRegister(register);
    }

    public void updateHit(Long hit, String register){
        subscribeRepository.updateHit(hit, register);
    }

    public void deleteByRegister(String register){
        subscribeRepository.deleteByRegister(register);
    }
}
