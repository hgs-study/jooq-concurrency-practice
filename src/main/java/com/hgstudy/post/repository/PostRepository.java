package com.hgstudy.post.repository;

import com.hgstudy.post.domain.Post;
import jooq.dsl.tables.records.PostRecord;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.conf.Settings;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static jooq.dsl.tables.Post.POST;

@Slf4j
@Repository
@RequiredArgsConstructor
public class PostRepository {
    private final DSLContext context;

    private static final String DRIVER_CLASS_NAME = "com.mysql.cj.jdbc.Driver";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "hgs3164";
    private static final String URL = "jdbc:mysql://localhost:3306/jooq_test";

    public Post findById(Long id){
        return context
                .selectFrom(POST)
                .where(POST.ID.eq(id))
                .fetchOneInto(Post.class);
    }

    public void save(Post post){
        context
            .insertInto(POST,
                    POST.TITLE,
                    POST.CONTENT,
                    POST.HIT)
            .values(post.getTitle(),
                    post.getContent(),
                    post.getHit())
            .execute();
    }

    public void plusHitById(Long id){
        context.transaction(configuration ->{
            final Post post = DSL.using(configuration)
                                .selectFrom(POST)
                                .where(POST.ID.eq(id))
                                .fetchOneInto(Post.class);

            final Long hit = post.getHit();

            DSL.using(configuration)
                    .update(POST)
                    .set(POST.HIT, hit + 1L)
                    .where(POST.ID.eq(id))
                    .execute();
        });
    }

    public void plusHitOptimisticById(Long id){
        try(Connection connection = DriverManager.getConnection(URL, USERNAME, PASSWORD)){
            DSL.using(connection, SQLDialect.MYSQL, new Settings().withExecuteWithOptimisticLocking(true))
                    .transaction(configuration -> {
                        PostRecord postRecord = DSL.using(configuration)
                                                  .fetchOne(POST, POST.ID.eq(id));

                        postRecord.setHit(postRecord.getHit() + 1L);
                        postRecord.store();
                    });
        } catch (SQLException e) {
            log.info("error code : {}, error : {}", e.getErrorCode(), e);
        }
    }

    public void plusHitPessimisticById(Long id){
        context.transaction(configuration ->{
            final Post post = DSL.using(configuration)
                                .selectFrom(POST)
                                .where(POST.ID.eq(id))
                                .forUpdate()
                                .fetchOneInto(Post.class);

            final Long hit = post.getHit();

            DSL.using(configuration)
                    .update(POST)
                    .set(POST.HIT, hit + 1L)
                    .where(POST.ID.eq(id))
                    .execute();
        });
    }
}
