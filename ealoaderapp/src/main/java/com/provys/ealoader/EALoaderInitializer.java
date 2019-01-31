package com.provys.ealoader;

import com.provys.ealoader.executor.EALoadExecutor;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.builder.api.AppenderComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilderFactory;
import org.apache.logging.log4j.core.config.builder.impl.BuiltConfiguration;
import org.sparx.Repository;
import picocli.CommandLine;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@CommandLine.Command(description = "Load catalogue dtaa to Enterprise Architect database", name="ealoader",
        mixinStandardHelpOptions = true, version = "0.9")
class EALoaderInitializer implements Runnable {

    @CommandLine.Option(names = {"-p", "--provysdb"},
            description = "Provys database connect string (localhost:1521:PVYS)", defaultValue = "localhost:60002:PVYS")
    private String provysAddress;

    @CommandLine.Option(names = {"--provysuser"}, description = "Provys DB user", defaultValue = "ealoader")
    private String provysUser;

    @CommandLine.Option(names = {"--provyspwd"}, description = "Provys DB user password", defaultValue = "heslo")
    private String provysPwd;

    @CommandLine.Option(names = {"-e", "--eaproject"}, description = "Enterprise architect project",
            defaultValue = "provys_ea --- DBType=3;Connect=Provider=OraOLEDB.Oracle.1;Password=ker;" +
                    "Persist Security Info=True;User ID=ker;Data Source=enterprise_architect;LazyLoad=1;")
    private String eaAddress;

    @CommandLine.Option(names = {"-l", "--logfile"}, description = "Log file")
    private File logFile;

    @CommandLine.Option(names = {"--loglevel"}, description = "Log level", defaultValue = "ERROR")
    private Level logLevel;

    /**
     * Configure logger based on command line arguments (logfile and loglevel)
     */
    private void configureLogger() {
        ConfigurationBuilder<BuiltConfiguration> builder = ConfigurationBuilderFactory.newConfigurationBuilder();
        builder.setConfigurationName("RootLogger");
        AppenderComponentBuilder appenderBuilder;
        if (logFile != null) {
            appenderBuilder = builder.newAppender("Log", "File").
                    addAttribute("fileName", logFile.getPath());
        } else {
            appenderBuilder = builder.newAppender("Log", "Console")
                    .addAttribute("target", ConsoleAppender.Target.SYSTEM_OUT);
        }
        appenderBuilder.add(builder.newLayout("PatternLayout").
                addAttribute("pattern", "%d{yyyy-MM-dd HH:mm:ss.SSS} %-5level %-10t %-50c{-2} %msg%n%throwable"));
        builder.add(appenderBuilder);
        builder.add(builder.newRootLogger(logLevel).
                add(builder.newAppenderRef("Log")).
                addAttribute("additivity", true));
        builder.add(builder.newLogger("org.jboss.weld", Level.WARN).
                addAttribute("additivity", true));
        Configurator.initialize(builder.build());
        final Logger logger = LogManager.getLogger(EALoaderInitializer.class);
        logger.info("LoggerInit: Logger initialized: " +
                ((logFile != null) ? "file " + logFile.getPath() : "console") + ", level " + logLevel);
    }

    /**
     * Open connection to PROVYS database, open Enterprise Architect instance and pass them both to loader
     */
    @Override
    public void run() {
        configureLogger();
        var loadExecutor = new EALoadExecutor();
        try (Connection provysConnection = DriverManager.getConnection("jdbc:oracle:thin:@" + provysAddress, provysUser, provysPwd)) {
            Repository eaRepository = null;
            try {
                // Create a repository object - This will create a new instance of EA
                eaRepository = new Repository();
                // Attempt to open the provided file
                if (eaRepository.OpenFile(eaAddress)) {
                    loadExecutor.run(provysConnection, eaRepository);
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
    }

}
