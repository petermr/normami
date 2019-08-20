package org.contentmine.ami.tools.option.search;

import java.util.ArrayList;
import java.util.List;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command()
public class ProcessTreeTool {
    @Option(names = {"--processtree"},
    		arity = "0",
            description = " use new processTree style of processing")
	protected boolean processTree = true;
}
