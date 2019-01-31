package com.provys.ealoader.executor;

import org.sparx.Repository;

import javax.enterprise.context.ApplicationScoped;
import java.sql.Connection;

@ApplicationScoped
public class EALoadExecutor {

    public void run(Connection connection, Repository eaRepository) {
        System.out.println("Run");
    }
}
