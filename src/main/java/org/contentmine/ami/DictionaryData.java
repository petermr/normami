package org.contentmine.ami;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.contentmine.ami.AMIDictionary.DictionaryFileFormat;
import org.contentmine.ami.AMIDictionary.InputFormat;
import org.contentmine.ami.AMIDictionary.Operation;

class DictionaryData {
	static final Logger LOG = Logger.getLogger(DictionaryData.class);
	static {
		LOG.setLevel(Level.DEBUG);
	}

    String[]      dataCols;
    String[]      dictionary;
    String        dictionaryTopname;
	String        href;
    String[]      hrefCols;
    InputFormat   informat;
    String        input;
	String        linkCol;
	String[]      log4j;
	String        nameCol;
    Operation     operation;
    DictionaryFileFormat[]  outformats;
    String        splitCol=",";
	String        termCol;
    String[]      terms;

}
