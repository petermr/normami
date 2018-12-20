package org.contentmine.ami;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.Arrays;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Test;

import junit.framework.Assert;
//import picocli.CommandLine.Command;
//import picocli.CommandLine.Parameters;
//import picocli.CommandLine.Options;

public class AMICLITest {
	private static final Logger LOG = Logger.getLogger(AMICLITest.class);
	static {
		LOG.setLevel(Level.DEBUG);
	}

	@Test
	public void testApacheExample() {
		// create the command line parser
		CommandLineParser parser = new DefaultParser();
		CommandLine commandLine;
	
		// create the Options
		Options options = new Options();
		options.addOption( "a", "all", false, "do not hide entries starting with ." );
		options.addOption( "A", "almost-all", false, "do not list implied . and .." );
		options.addOption( "b", "escape", false, "print octal escapes for nongraphic "
		                                         + "characters" );
		options.addOption( OptionBuilder.withLongOpt( "block-size" )
		                                .withDescription( "use SIZE-byte blocks" )
		                                .hasArg()
		                                .withArgName("SIZE")
		                                .create() );
		options.addOption( "B", "ignore-backups", false, "do not list implied entried "
		                                                 + "ending with ~");
		options.addOption( "c", false, "with -lt: sort by, and show, ctime (time of last " 
		                               + "modification of file status information) with "
		                               + "-l:show ctime and sort by name otherwise: sort "
		                               + "by ctime" );
		Option optx = new Option( "C", false, "list entries by columns" );
		optx.setArgs(3);
		optx.setArgName("pingo1 [pingo2...]");
		options.addOption(optx);

		assertEquals("usage: app [-a] [-A] [-b] [-B] [--block-size <SIZE>] [-c]"
				+ " [-C <pingo1\n       [pingo2...]>]\n", 
        		createHelpString(new AMICLI(), options, 80, "app"));

		
		String[] args = new String[]{ "--block-size=10" };
	    String opt = "block-size";
		parseRun(parser, options, args, opt);
		LOG.debug("=========");
		
		args = "--block-size 10".split("\\s+");
	    opt = "block-size";
		parseRun(parser, options, args, opt);
		LOG.debug("=========");

		opt = "C";
		args = new String[] {"-C", "one", "two", "three", "--block-size", "999"};
		parseRun(parser, options, args, opt);
		LOG.debug("=========");
		opt = "block-size";
		args = new String[] {"-C", "one", "two", "three", "--block-size", "999"};
		parseRun(parser, options, args, opt);
		LOG.debug("=========");
		opt = "C";
		args = new String[] {"-C", "one", "two", "three", "four", "--block-size", "987"};
		commandLine = parseRun(parser, options, args, opt);
		commandLine.getOptionValue("C");
		LOG.debug("=========");
		opt = "--block-size";
		args = new String[] {"-C", "one", "two", "three", "four", "--block-size", "987"};
		parseRun(parser, options, args, opt);
		LOG.debug("=========");

	}
	
	@Test
	public void testType() {
		
		Options options = new Options();
		boolean hasArg = true;
		boolean stopAtError = true;
		CommandLine commandLine = null;
		String iOpt = "i";
		options.addOption( iOpt, "int", hasArg, "integer value" );
		Option opti = options.getOption(iOpt);
		opti.setType(String.class);
		String[] args = "-i 3".split("\\s+");
		try {
			commandLine = new DefaultParser().parse(options, args, stopAtError);
			Object value = commandLine.getOptionObject(iOpt);
			LOG.debug("v: "+value);
		} catch (ParseException e) {
			LOG.error("parse "+e);
		}
		opti = options.getOption(iOpt);
		opti.setType(Number.class); // MUST be Number (Integer doesn't work); returns a Long
		opti.setArgName("INT"); // not sure where used?
		LOG.debug(opti+"; "+opti.getValue());
		parseCommandLineQuietly(options, stopAtError, iOpt, args);
		args = "-i 3.4".split("\\s+");
		parseCommandLineQuietly(options, stopAtError, iOpt, args);
		// OK - everything is a Strin
		opti.setType(String.class);
		Object value = getValueQuietly(options, stopAtError, commandLine, iOpt, args);
		LOG.debug("v: "+value+"; "+value.getClass());


		args = "-i junk".split("\\s+");
		opti.setType(Integer.class);
		try {
			commandLine = new DefaultParser().parse(options, args, stopAtError);
			value = commandLine.getOptionObject(iOpt);
			
			Assert.assertNull(value);
		} catch (ParseException e) {
			LOG.error("parse "+e);
		}

	}

    // This test ensures the options are properly sorted
    // See https://issues.apache.org/jira/browse/CLI-131
    @Test
    public void testPrintUsage()
    {
        Option optionA = new Option("a", "first arg description");
        Option optionB = new Option("b", "second arg description");
        Option optionC = new Option("c", "chuff", true, "number of okapis in the project. "
        		+ "Okapis are striped and secretive and this is largely irrelevant\n"
        		+ "       I can add newlines\n                   but not spaces (by default)");
        Options opts = new Options();
        opts.addOption(optionA);
        opts.addOption(optionB);
        opts.addOption(optionC);
        assertEquals("usage: command syntax [-a] [-b] [-c <arg>]"+ "\n", // 7 spaces
        		createHelpString(new AMICLI(), opts, 50, "command syntax"));
    }

    // ==============
    
	private Object getValueQuietly(Options options, boolean stopAtError, CommandLine commandLine, String iOpt,
			String[] args) {
		parseCommandLineQuietly(options, stopAtError, iOpt, args);
		return commandLine.getOptionObject(iOpt);
	}

	private CommandLine parseCommandLineQuietly(Options options, boolean stopAtError, String iOpt, String[] args) {
		CommandLine commandLine = null;
		try {
			commandLine = new DefaultParser().parse(options, args, stopAtError);
		} catch (ParseException e) {
			LOG.error("parse "+e);
		}
		return commandLine;
	}
	
	private String createHelpString(AMICLI amiCli, Options options, int width, String syntax) {
		HelpFormatter helpFormatter = new HelpFormatter();
		helpFormatter.printHelp("This is a command", options);
        ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
        PrintWriter printWriter = new PrintWriter(bytesOut);
		helpFormatter.printUsage(printWriter, width, syntax, options);
        printWriter.close();
		return bytesOut.toString();
	}

	private CommandLine parseRun(CommandLineParser parser, Options options, String[] args, String opt) {
		 CommandLine commandLine = null;
		 try {
		    commandLine = parser.parse( options, args );
			if( commandLine.hasOption( opt ) ) {
		        String[] optionValues = commandLine.getOptionValues( opt );
				System.out.println( "opts: "+ (optionValues == null ? null : Arrays.asList(optionValues)));
		    }
			Object o = commandLine.getParsedOptionValue(opt);
			LOG.debug(">parsedOptionValue> "+o);
			o = commandLine.getOptionObject(opt);
			LOG.debug(">"+opt+"> "+o);
		}
		catch( ParseException exp ) {
		    System.out.println( "Unexpected exception:" + exp.getMessage() );
		}
		return commandLine;
	}
	
//	@Test
//	public void testCommandLineBuilding() {
//		String argsXML = ""
//				+ "<option short='a' long='aardvark' name='AARD' type='Integer.class' count='2' mandatory='yes'>"
//				+ "  <description>This is a large animal that eats ants and can count up to 2</decription>"
//				+ "</option>";
//		
//        assertEquals("usage: command syntax [-a] [-b] [-c <arg>]"+ "\n", // 7 spaces
//        		createHelpString(opts, 50, "command syntax"));
//
//	}

	
}
