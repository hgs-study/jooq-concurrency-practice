package com.hgstudy.subscribe.repository;

import com.hgstudy.subscribe.domain.Subscribe;
import jooq.dsl.tables.records.SubscribeRecord;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.conf.Settings;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Repository;


import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static jooq.dsl.tables.Subscribe.SUBSCRIBE;

@Repository
@RequiredArgsConstructor
public class SubscribeRepository {
    private final DSLContext context;

    private static final String driverClassName = "com.mysql.cj.jdbc.Driver";
    private static final String username = "root";
    private static final String password = "hgs3164";
    private static final String url = "jdbc:mysql://localhost:3306/jooq_test";

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

    public void testSave(Subscribe subscribe){
        SubscribeRecord subscribeRecord = context.newRecord(SUBSCRIBE);

        subscribeRecord.setRegister(subscribe.getRegister());
        subscribeRecord.setService(subscribe.getServiceType().toString());
        subscribeRecord.setContentType(subscribe.getContentType().toString());
        subscribeRecord.setContentIdx(subscribe.getContentIdx());
        subscribeRecord.store();
    }

    public Subscribe findByRegister(String register){
        return context
                .selectFrom(SUBSCRIBE)
                .where(SUBSCRIBE.REGISTER.eq(register))
                .fetchOneInto(Subscribe.class);
    }

    public Subscribe findOptimisticLockByRegister(String register){
        return getOptimisticContext()
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

    public void updateOptimisticLockHit(Long hit, String register){
        getOptimisticContext()
            .update(SUBSCRIBE)
            .set(SUBSCRIBE.HIT, hit)
            .where(SUBSCRIBE.REGISTER.eq(register))
            .execute();
    }

    public void plusHitPessimistic(String register){
        context.transaction(configuration ->{
            Subscribe subscribe = DSL.using(configuration)
                                    .selectFrom(SUBSCRIBE)
                                    .where(SUBSCRIBE.REGISTER.eq(register))
                                    .forUpdate()
                                    .fetchOneInto(Subscribe.class);

            Long hit = subscribe.getHit();

            DSL.using(configuration)
                    .update(SUBSCRIBE)
                    .set(SUBSCRIBE.HIT, hit + 1L)
                    .where(SUBSCRIBE.REGISTER.eq(register))
                    .execute();
        });
    }

    public void plusHitPessimistic_02(String register){
//        [[[Pessimistic - Share Lock]]]
//        context.transaction(configuration ->{
//            Subscribe subscribe = DSL.using(configuration)
//                                    .selectFrom(SUBSCRIBE)
//                                    .where(SUBSCRIBE.REGISTER.eq(register))
//                                    .forShare()
//                                    .fetchOneInto(Subscribe.class);
//
//            Long hit = subscribe.getHit();
//
//            DSL.using(configuration)
//                    .update(SUBSCRIBE)
//                    .set(SUBSCRIBE.HIT, hit + 1L)
//                    .where(SUBSCRIBE.REGISTER.eq(register))
//                    .execute();
//        });

//      [[[Optimistic && Pessimistic]]]
//      참고 : https://www.jooq.org/doc/latest/manual/sql-execution/crud-with-updatablerecords/optimistic-locking/
        getOptimisticContext().transaction(configuration ->{
            Subscribe subscribe = DSL.using(configuration)
                                    .selectFrom(SUBSCRIBE)
                                    .where(SUBSCRIBE.REGISTER.eq(register))
//                                    .forUpdate()
                                    .fetchOneInto(Subscribe.class);

            Long hit = subscribe.getHit();

            //update 구문 앞에 select절로 forUpdate절이 들어가면 뒤에 update 절은 pessimistic Lock 수행
            DSL.using(configuration)
                    .update(SUBSCRIBE)
                    .set(SUBSCRIBE.HIT, hit + 1L)
                    .where(SUBSCRIBE.REGISTER.eq(register))
                    .execute();
        });
    }

    public void plusHitOptimistic(String register){

        getOptimisticContext().transaction(configuration -> {
            SubscribeRecord subscribeRecord = DSL.using(configuration)
                    .fetchOne(SUBSCRIBE, SUBSCRIBE.REGISTER.eq(register));

            subscribeRecord.setHit(subscribeRecord.getHit() + 1L);
            subscribeRecord.store();
        });
    }

    public void deleteByRegister(String register){
        context
            .deleteFrom(SUBSCRIBE)
            .where(SUBSCRIBE.REGISTER.eq(register))
            .execute();
    }


    private DSLContext getOptimisticContext(){
        DSLContext optimisticContext = null;
        Connection connection = null;

        try {
            Class.forName(driverClassName);
            connection = DriverManager.getConnection(url,username,password);
            connection.setAutoCommit(false);
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
        optimisticContext = DSL.using(connection, SQLDialect.MYSQL, new Settings().withExecuteWithOptimisticLocking(true));

        return optimisticContext;
    }

}
