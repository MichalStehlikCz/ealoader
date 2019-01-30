package com.provys.ealoader;

import org.apache.logging.log4j.Level;
import picocli.CommandLine;

public class EALoaderCliApplication {

    public static void main(String[] args) {
        CommandLine commandLine = new CommandLine(new EALoaderInitializer());
        commandLine.registerConverter(Level.class, Level::valueOf);
        commandLine.parseWithHandlers(new CommandLine.RunLast().useOut(System.out)
                , (CommandLine.IExceptionHandler2) (new CommandLine.DefaultExceptionHandler().useErr(System.err)), args);
        System.out.println("Load successfully finished");
    }

}
