package com.hgstudy.subscribe;

import com.hgstudy.content.constant.ContentType;
import com.hgstudy.service.constant.ServiceType;
import com.hgstudy.subscribe.domain.Subscribe;
import com.hgstudy.subscribe.repository.SubscribeRepository;
import com.hgstudy.subscribe.service.SubscribeService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class SubscribeTest {

    @Autowired
    private SubscribeService subscribeService;

    @Autowired
    private SubscribeRepository subscribeRepository;

    private static final String REGISTER = "HyunGunSoo";
    private static Long THREAD_COUNT = 0L;

    @BeforeEach
    public void setUp(){
        final Subscribe subscribe = new Subscribe(REGISTER, ServiceType.NEWS, ContentType.ARTICLE, 100L);
        subscribeRepository.save(subscribe);
    }

    @AfterEach
    public void deleteSetUpData(){
        subscribeService.deleteByRegister(REGISTER);
    }

    @DisplayName("락 안걸고 동시성 테스트")
    @Test
    public void 락_안걸고_동시성_테스트() throws InterruptedException {
        final int THREAD_LENGTH = 1000;

        CountDownLatch countDownLatch = new CountDownLatch(THREAD_LENGTH);
        List<Thread> workers = Stream
                                .generate(() -> new Thread(new Worker(countDownLatch)))
                                .limit(THREAD_LENGTH)
                                .collect(Collectors.toList());

        workers.forEach(Thread::start);
        countDownLatch.await();

        final Long findHit = subscribeService.findByRegister(REGISTER).getHit();
        assertEquals(findHit, Long.valueOf(THREAD_LENGTH));
    }

    public class Worker implements Runnable{
        private CountDownLatch countDownLatch;

        public Worker(CountDownLatch countDownLatch){
            this.countDownLatch = countDownLatch;
        }

        @Override
        public void run() {
            THREAD_COUNT = THREAD_COUNT + 1L;
            subscribeService.updateHit(THREAD_COUNT, REGISTER);
            countDownLatch.countDown();
        }
    }



}
