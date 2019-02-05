package com.provys.ealoader;

import com.provys.ealoader.executor.EALoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configurator;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.sparx.Repository;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Objects;

@ApplicationScoped
public class RunEALoader implements Runnable {

    @Nonnull
    private static final Logger LOG = LogManager.getLogger(RunEALoader.class.getName());
    @Nonnull
    private final EALoader eaLoader;
    @Nullable
    private String provysAddress;
    @Nullable
    private String provysUser;
    @Nullable
    private String provysPwd;
    @Nullable
    private String eaAddress;

    @Inject
    RunEALoader(EALoader eaLoader) {
        this.eaLoader = Objects.requireNonNull(eaLoader);
    }

    RunEALoader setProvysAddress(String provysAddress) {
        this.provysAddress = provysAddress;
        return this;
    }

    RunEALoader setProvysUser(String provysUser) {
        this.provysUser = provysUser;
        return this;
    }

    RunEALoader setProvysPwd(String provysPwd) {
        this.provysPwd = provysPwd;
        return this;
    }

    RunEALoader setEaAddress(String eaAddress) {
        this.eaAddress = eaAddress;
        return this;
    }

    private static void addLoggerShutdownHook() {
        Logger logger = LogManager.getRootLogger();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Shutting down - closing application");
            if (LogManager.getContext() instanceof LoggerContext) {
                logger.debug("Shutting down log4j2");
                Configurator.shutdown((LoggerContext) LogManager.getContext());
            } else
                logger.warn("Unable to shutdown log4j2");
        }));
    }

    @Override
    public void run() {
        addLoggerShutdownHook();
        try (Connection provysConnection = DriverManager.getConnection("jdbc:oracle:thin:@" + provysAddress,
                provysUser, provysPwd);
             DSLContext dslContext = DSL.using(provysConnection, SQLDialect.ORACLE12C)) {
            Repository eaRepository = null;
            try {
                // Create a repository object - This will create a new instance of EA
                eaRepository = new Repository();
                // Attempt to open the provided file
                if (eaRepository.OpenFile(eaAddress)) {
                    eaLoader.run(dslContext, eaRepository);
                } else {
                    // If the file couldn't be opened then notify the user
                    throw new RuntimeException("EA was unable to open the file '" + eaAddress + '\'');
                }
            } finally {
                if (eaRepository != null) {
                    // Clean up
                    eaRepository.CloseFile();
                    eaRepository.Exit();
                    eaRepository.destroy();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to connect to PROVYS database", e);
        }
        LOG.info("Load of metadata to Enterprise Architect repository finished");
    }
}
