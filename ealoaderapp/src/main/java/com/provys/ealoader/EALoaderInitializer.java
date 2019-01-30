package com.provys.ealoader;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.builder.api.AppenderComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilderFactory;
import org.apache.logging.log4j.core.config.builder.impl.BuiltConfiguration;
import picocli.CommandLine;

import javax.enterprise.inject.se.SeContainer;
import javax.enterprise.inject.se.SeContainerInitializer;
import java.io.File;

@CommandLine.Command(description = "Load catalogue dtaa to Enterprise Architect database", name="ealoader",
        mixinStandardHelpOptions = true, version = "0.9")
class EALoaderInitializer implements Runnable {

    @CommandLine.Option(names = {"-p", "--provysdb"},
            description = "Provys database connect string (localhost:1521:PVYS)", defaultValue = "localhost:60002:PVYS")
    private String provysAddress;

    @CommandLine.Option(names = {"-e", "--eadb"}, description = "Enterprise architect database connect string",
            defaultValue = "localhost:60096:PVYS")
    private String eaAddress;

    @CommandLine.Option(names = {"-k", "--kerpwd"}, required = true, description = "KER user password")
    private String kerPwd;

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

    @Override
    public void run() {
        configureLogger();
        SeContainer container = SeContainerInitializer.newInstance()
                .addProperty("org.jboss.weld.se.archive.isolation", false).initialize();
        RunEALoad runner = container.select(RunEALoad.class).get();
        runner.setProvysAddress(provysAddress).
                setEAAddress(eaAddress).
                setKerPwd(kerPwd).
                run();
    }



}
