package com.hgstudy.post;

import com.hgstudy.content.constant.ContentType;
import com.hgstudy.post.domain.Post;
import com.hgstudy.post.repository.PostRepository;
import com.hgstudy.post.service.PostService;
import com.hgstudy.service.constant.ServiceType;
import com.hgstudy.subscribe.SubscribeConcurrencyTest;
import com.hgstudy.subscribe.domain.Subscribe;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class PostConcurrencyTest {

    @Autowired
    private PostService postService;

    @Autowired
    private PostRepository postRepository;

    private static Long BASE_ID = 1L;
    private static int THREAD_COUNT = 50;

//    @BeforeEach
//    public void setUp(){
//        final Post Post = new Post("테스트 제목", "테스트 내용", 0L);
//        postRepository.save(Post);
//    }

    @DisplayName("락 안걸고 동시성 테스트")
    @Test
    public void 락_안걸고_동시성_테스트() throws InterruptedException {
        final CountDownLatch countDownLatch = new CountDownLatch(THREAD_COUNT);
        final List<Thread> workers = Stream
                                        .generate(() -> new Thread(new NoLockWorker(countDownLatch)))
                                        .limit(THREAD_COUNT)
                                        .collect(Collectors.toList());

        workers.forEach(Thread::start);
        countDownLatch.await();

        final Long findHit = postService.findById(BASE_ID).getHit();
        assertEquals(findHit, Long.valueOf(THREAD_COUNT));
    }

    @Transactional
    @DisplayName("Optimistic Lock 걸고 동시성 테스트")
    @Test
    public void Optimistic_Lock_걸고_동시성_테스트() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(THREAD_COUNT);
        List<Thread> workers = Stream
                                    .generate(() -> new Thread(new OptimisticLockWorker(countDownLatch)))
                                    .limit(THREAD_COUNT)
                                    .collect(Collectors.toList());

        workers.forEach(Thread::start);
        countDownLatch.await();

        final Long findHit = postService.findById(BASE_ID).getHit();
        assertEquals(findHit, Long.valueOf(THREAD_COUNT));
    }


    @DisplayName("Pessimistic Lock 걸고 동시성 테스트")
    @Test
    public void Pessimistic_Lock_걸고_동시성_테스트() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(THREAD_COUNT);

        List<Thread> workers = Stream
                                    .generate(() -> new Thread(new PessimisticLockWorker(countDownLatch)))
                                    .limit(THREAD_COUNT)
                                    .collect(Collectors.toList());

        workers.forEach(Thread::start);
        countDownLatch.await();

        final Long findHit = postService.findById(BASE_ID).getHit();
        assertEquals(findHit, Long.valueOf(THREAD_COUNT));
    }


    public class NoLockWorker implements Runnable{
        private CountDownLatch countDownLatch;

        public NoLockWorker(CountDownLatch countDownLatch){
            this.countDownLatch = countDownLatch;
        }

        @Override
        public void run() {
            postService.plusHitById(BASE_ID);
            countDownLatch.countDown();
        }
    }

    public class OptimisticLockWorker implements Runnable{
        private CountDownLatch countDownLatch;

        public OptimisticLockWorker(CountDownLatch countDownLatch){
            this.countDownLatch = countDownLatch;
        }

        @Override
        public void run() {
            postService.plusHitOptimisticById(BASE_ID);
            countDownLatch.countDown();
        }
    }

    public class PessimisticLockWorker implements Runnable{
        private CountDownLatch countDownLatch;

        public PessimisticLockWorker(CountDownLatch countDownLatch){
            this.countDownLatch = countDownLatch;
        }

        @Override
        public void run() {
            postService.plusHitPessimisticById(BASE_ID);
            countDownLatch.countDown();
        }
    }


}
