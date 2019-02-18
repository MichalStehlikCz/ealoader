package com.provys.ealoader;

import com.provys.common.exception.RegularException;
import com.provys.ealoader.executor.EALoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configurator;
import org.eclipse.microprofile.config.spi.ConfigProviderResolver;
import org.eclipse.microprofile.config.spi.ConfigSource;
import org.sparx.Repository;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;
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

    @SuppressWarnings("CdiUnproxyableBeanTypesInspection")
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
        ConfigProviderResolver.instance().registerConfig(
                ConfigProviderResolver.instance().getBuilder().forClassLoader(getClass().getClassLoader()).
                        withSources(new CommandLineParamsSource(provysAddress, provysUser, provysPwd)).build(),
                getClass().getClassLoader());
        Repository eaRepository = null;
        try {
            // Create a repository object - This will create a new instance of EA
            eaRepository = new Repository();
            // Attempt to open the provided file
            if (eaRepository.OpenFile(eaAddress)) {
                eaLoader.run(eaRepository);
            } else {
                // If the file couldn't be opened then notify the user
                throw new RegularException(LOG, "EALOADER_CANNOTOPENREPOSITORY",
                        "EA was unable to open the file '" + eaAddress + '\'');
            }
        } finally {
            if (eaRepository != null) {
                // Clean up
                eaRepository.CloseFile();
                eaRepository.Exit();
                eaRepository.destroy();
            }
        }
        LOG.info("Load of metadata to Enterprise Architect repository finished");
    }

    public static class CommandLineParamsSource implements ConfigSource {

        private final Map<String, String> properties = new HashMap<>(3);

        CommandLineParamsSource(String url, String user, String pwd) {
            properties.put("PROVYSDB_URL", url);
            properties.put("PROVYSDB_USER", user);
            properties.put("PROVYSDB_PWD", pwd);
        }

        @Override
        public int getOrdinal() {
            return 900;
        }

        @Override
        public Map<String, String> getProperties() {
            return properties;
        }

        @Override
        public String getValue(String key) {
            return properties.get(key);
        }

        @Override
        public String getName() {
            return "CommandLineParams";
        }
    }
}
