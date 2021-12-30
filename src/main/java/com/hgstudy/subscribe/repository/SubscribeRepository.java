package com.hgstudy.subscribe.repository;

import com.hgstudy.subscribe.domain.Subscribe;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import static jooq.dsl.tables.Subscribe.SUBSCRIBE;

@Repository
@RequiredArgsConstructor
public class SubscribeRepository {
    private final DSLContext context;

    public void save(Subscribe subscribe){
        context
            .insertInto(SUBSCRIBE,
                    SUBSCRIBE.REGISTER,
                    SUBSCRIBE.SERVICE,
                    SUBSCRIBE.CONTENT_TYPE,
                    SUBSCRIBE.CONTENT_IDX)
            .values(subscribe.getRegister(),
                    subscribe.getServiceType().toString(),
                    subscribe.getContentType().toString(),
                    subscribe.getContentIdx())
            .execute();
    }

    public Subscribe findByRegister(String register){
        return context
                .selectFrom(SUBSCRIBE)
                .where(SUBSCRIBE.REGISTER.eq(register))
                .fetchOneInto(Subscribe.class);
    }

    public void updateHit(Long hit, String register){
        context
            .update(SUBSCRIBE)
            .set(SUBSCRIBE.HIT, hit)
            .where(SUBSCRIBE.REGISTER.eq(register))
            .execute();
    }

    public void deleteByRegister(String register){
        context
            .deleteFrom(SUBSCRIBE)
            .where(SUBSCRIBE.REGISTER.eq(register))
            .execute();
    }

}
