package net.milanaleksic.mcs.application.config;

import org.apache.log4j.Logger;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

/**
 * User: Milan Aleksic
 * Date: 8/6/11
 * Time: 7:54 PM
 */
public class ProgramArgsService {

    private static final Logger log = Logger.getLogger(ProgramArgsService.class);

    private static ProgramArgs programArgs;

    public static void setProgramArgs(String[] programArgs) {
        ProgramArgsService.programArgs = getApplicationArgs(programArgs);
    }

    private static ProgramArgs getApplicationArgs(String[] args) {
        if (args == null)
            return new ProgramArgs();
        ProgramArgs programArgs = new ProgramArgs();
        CmdLineParser parser = new CmdLineParser(programArgs);
        try {
            parser.parseArgument(args);
            if (log.isInfoEnabled())
                log.info("Program arguments: " + programArgs); //NON-NLS
        } catch (CmdLineException e) {
            log.error("Command line arguments could not have been read", e); //NON-NLS
            parser.printUsage(System.err);
            return null;
        }
        return programArgs;
    }

    public ProgramArgs getProgramArgs() {
        return programArgs;
    }
}
