package net.milanaleksic.mcs.util;

import net.milanaleksic.mcs.ApplicationManager;
import net.milanaleksic.mcs.Startup;
import org.apache.log4j.Logger;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * User: Milan Aleksic
 * Date: 8/6/11
 * Time: 7:54 PM
 */
public class ProgramArgsService implements InitializingBean {

    private static final Logger log = Logger.getLogger(ProgramArgsService.class);

    @Autowired private ApplicationManager applicationManager;

    @Override
    public void afterPropertiesSet() throws Exception {
        ProgramArgs programArgs = getApplicationArgs(Startup.getProgramArgs());
        applicationManager.setProgramArgs(programArgs);
    }

    private static ProgramArgs getApplicationArgs(String[] args) {
        ProgramArgs programArgs = new ProgramArgs();
        CmdLineParser parser = new CmdLineParser(programArgs);
        try {
            parser.parseArgument(args);
            log.info("Program arguments: " + programArgs);
        } catch (CmdLineException e) {
            log.error("Command line arguments could not have been read", e);
            parser.printUsage(System.err);
            return null;
        }
        return programArgs;
    }

}
