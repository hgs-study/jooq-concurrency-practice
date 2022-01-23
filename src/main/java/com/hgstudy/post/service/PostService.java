package com.hgstudy.post.service;

import com.hgstudy.post.domain.Post;
import com.hgstudy.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.exception.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostService {

    private final PostRepository postRepository;

    public Post findById(Long id){
        return postRepository.findById(id);
    }

    public void plusHitById(Long id){
        postRepository.plusHitById(id);
    }

    @Transactional
    public void plusHitOptimisticById(Long id){
        try {
            postRepository.plusHitOptimisticById(id);
        }catch (DataAccessException e ) {
            this.plusHitOptimisticById(id);
        }
    }

    public void plusHitPessimisticById(Long id){
        postRepository.plusHitPessimisticById(id);
    }
}
