package com.provys.ealoader.executor;

import org.jooq.DSLContext;
import org.sparx.Repository;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class EALoader {

    public void run(DSLContext dslContext, Repository eaRepository) {
        var executor = new EALoadExecutor(dslContext, eaRepository);
        executor.run();
    }
}
