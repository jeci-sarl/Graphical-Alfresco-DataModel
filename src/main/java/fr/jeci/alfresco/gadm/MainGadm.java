package fr.jeci.alfresco.gadm;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.PrintStream;

import javax.xml.stream.XMLStreamException;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.IOUtils;

public class MainGadm {
	public static void main(String[] args) {

		Options options = makeOptions();

		CommandLineParser parser = new BasicParser();
		try {
			CommandLine line = parser.parse(options, args);

			if (line.hasOption("h")) {
				help(options);
			}
			else {
				run(options, line);
			}

		}
		catch (ParseException exp) {
			System.err.println("Parsing failed.  Reason: " + exp.getMessage());
		}
		catch (FileNotFoundException e) {
			System.err.println("Error " + e.getMessage());
		}

	}

	private static void help(Options options) {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("gadm [options] <custom-model.xml>",
				"gadm -o MyCustomModel.gv MyCustomModel.xml\ngadm -a contentModel.xml -o contentModel.gv", options,
				"https://github.com/jeci-sarl/Graphical-Alfresco-DataModel");
	}

	private static void run(Options options, CommandLine line) throws FileNotFoundException {
		String output = null;
		if (line.hasOption("output")) {
			output = line.getOptionValue("output");
		}

		String modelFile = null;
		if (line.hasOption("custom-model")) {
			modelFile = line.getOptionValue("custom-model");
		}
		else if (line.getArgs().length > 0) {
			modelFile = line.getArgs()[0];
		}

		String alfModel = null;
		if (line.hasOption("alfresco-model")) {
			alfModel = line.getOptionValue("alfresco-model");
		}

		if (modelFile == null && alfModel == null) {
			System.err.println("No model file to load");
			help(options);
			System.exit(1);
		}

		InputStream is = null;
		if (modelFile != null) {
			is = new FileInputStream(new File(modelFile));
		}
		else {
			is = DataModelToDOT.class.getResourceAsStream("/alfresco/model/" + alfModel);
		}

		PrintStream out = System.out;
		if (output != null) {
			out = new PrintStream(output);
		}

		try {
			DataModelToDOT pa = new DataModelToDOT(out);
			pa.loadModelFile(is);
			pa.close();

		}
		catch (XMLStreamException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally {
			IOUtils.closeQuietly(is);
			IOUtils.closeQuietly(out);
		}

	}

	private static Options makeOptions() {
		Options options = new Options();
		options.addOption("h", "help", false, "Print help");
		options.addOption("o", "output", true, "Print graph into this file");
		options.addOption("c", "custom-model", true, "Use custom data model");
		options.addOption("a", "alfresco-model", true,
				"Use standard Alfresco data model from alfresco/model/ (see alfresco-repository v 5.0.d)");

		return options;
	}
}
