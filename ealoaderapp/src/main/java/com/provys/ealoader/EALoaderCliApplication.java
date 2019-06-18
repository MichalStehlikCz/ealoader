package com.provys.ealoader;

import org.apache.logging.log4j.Level;
import picocli.CommandLine;

public class EALoaderCliApplication {

    public static void main(String[] args) {
        int exitCode = new CommandLine(new EALoaderInitializer())
                .registerConverter(Level.class, Level::valueOf)
                .execute(args);
        System.exit(exitCode);
    }

}
