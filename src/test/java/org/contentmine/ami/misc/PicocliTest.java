package org.contentmine.ami.misc;

import java.io.File;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.util.concurrent.Callable;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

/** simple example of picocli that runs.
 * 
 * @author pm286
 *
 */

@Command(description = "Prints the checksum (MD5 by default) of a file to STDOUT.",
         name = "checksum", mixinStandardHelpOptions = true, version = "checksum 3.0")
public class PicocliTest implements Callable<Void> {

    @Parameters(index = "0", description = "The file whose checksum to calculate.")
    private File file;

    @Option(names = {"-a", "--algorithm"}, description = "MD5, SHA-1, SHA-256, ...")
    private String algorithm = "SHA-1";

    public static void main(String[] args) throws Exception {
    	args = new String[]{"-a", "MD5", "README.md"}; 
        CommandLine.call(new PicocliTest(), args);
    	args = new String[]{}; 
        CommandLine.call(new PicocliTest(), args);
    }

//    @Override
    public Void call() throws Exception {
    	System.out.println("called on "+file+" with "+algorithm);
        byte[] fileContents = Files.readAllBytes(file.toPath());
        byte[] digest = MessageDigest.getInstance(algorithm).digest(fileContents);
        System.out.println(javax.xml.bind.DatatypeConverter.printHexBinary(digest));
        return null;
    }
}
