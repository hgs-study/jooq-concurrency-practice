/*
 * This file is generated by jOOQ.
 */
package jooq.dsl;


import jooq.dsl.tables.Subscribe;
import jooq.dsl.tables.records.SubscribeRecord;

import org.jooq.TableField;
import org.jooq.UniqueKey;
import org.jooq.impl.DSL;
import org.jooq.impl.Internal;


/**
 * A class modelling foreign key relationships and constraints of tables in 
 * jooq_test.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Keys {

    // -------------------------------------------------------------------------
    // UNIQUE and PRIMARY KEY definitions
    // -------------------------------------------------------------------------

    public static final UniqueKey<SubscribeRecord> KEY_SUBSCRIBE_PRIMARY = Internal.createUniqueKey(Subscribe.SUBSCRIBE, DSL.name("KEY_subscribe_PRIMARY"), new TableField[] { Subscribe.SUBSCRIBE.IDX }, true);
}