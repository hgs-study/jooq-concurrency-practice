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
public class SubscribeConcurrencyTest {

    @Autowired
    private SubscribeService subscribeService;

    @Autowired
    private SubscribeRepository subscribeRepository;

    private static final String REGISTER = "HyunGunSoo";
    private static int THREAD_LENGTH = 1;

    @BeforeEach
    public void setUp(){
        final Subscribe subscribe = new Subscribe(REGISTER, ServiceType.NEWS, ContentType.ARTICLE, 100L);
        subscribeRepository.testSave(subscribe);
    }

    @AfterEach
    public void deleteSetUpData(){
        subscribeService.deleteByRegister(REGISTER);
    }

    @DisplayName("락 안걸고 동시성 테스트")
    @Test
    public void 락_안걸고_동시성_테스트() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(THREAD_LENGTH);
        List<Thread> workers = Stream
                                .generate(() -> new Thread(new NoLockWorker(countDownLatch)))
                                .limit(THREAD_LENGTH)
                                .collect(Collectors.toList());

        workers.forEach(Thread::start);
        countDownLatch.await();

        final Long findHit = subscribeService.findByRegister(REGISTER).getHit();
        assertEquals(findHit, Long.valueOf(THREAD_LENGTH));
    }

    @DisplayName("Optimistic Lock 걸고 동시성 테스트")
    @Test
    public void Optimistic_Lock_걸고_동시성_테스트() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(THREAD_LENGTH);
        long startTime = System.currentTimeMillis();
        List<Thread> workers = Stream
                .generate(() -> new Thread(new OptimisticLockWorker(countDownLatch)))
                .limit(THREAD_LENGTH)
                .collect(Collectors.toList());

        workers.forEach(Thread::start);
        countDownLatch.await();

        final Long findHit = subscribeService.findByRegister(REGISTER).getHit();
        System.out.println("latency = " + (System.currentTimeMillis() - startTime) +" m/s");
        assertEquals(findHit, Long.valueOf(THREAD_LENGTH));
    }

    @DisplayName("Pessimistic Lock 걸고 동시성 테스트")
    @Test
    public void Pessimistic_Lock_걸고_동시성_테스트() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(THREAD_LENGTH);
        long startTime = System.currentTimeMillis();

        List<Thread> workers = Stream
                                .generate(() -> new Thread(new PessimisticLockWorker(countDownLatch)))
                                .limit(THREAD_LENGTH)
                                .collect(Collectors.toList());

        workers.forEach(Thread::start);
        countDownLatch.await();

        final Long findHit = subscribeService.findByRegister(REGISTER).getHit();
        System.out.println("latency = " + (System.currentTimeMillis() - startTime) +" m/s");
        assertEquals(findHit, Long.valueOf(THREAD_LENGTH));
    }

    public class NoLockWorker implements Runnable{
        private CountDownLatch countDownLatch;

        public NoLockWorker(CountDownLatch countDownLatch){
            this.countDownLatch = countDownLatch;
        }

        @Override
        public void run() {
            Long findHit = subscribeService.findByRegister(REGISTER).getHit();
            findHit = findHit + 1L;

            subscribeService.updateHit(findHit, REGISTER);
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
            subscribeService.plusHitPessimistic(REGISTER);
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
            subscribeService.testPlusHitOptimistic(REGISTER);
            countDownLatch.countDown();
        }
    }
}
