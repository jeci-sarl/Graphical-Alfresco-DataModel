package fr.jeci.alfresco.gadm;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.PrintStream;

import javax.xml.stream.XMLStreamException;

import org.apache.commons.io.IOUtils;

public class DataModelToDOT implements ConfigConstant {

	/* Output file or buffer */
	private PrintStream out;

	/* user Configuration */
	static int cfgMdAff;
	static int cfgMdCol;
	static int cfgLayout;
	static int cfgPrintAspect;

	/**
	 * Constructor
	 * 
	 * @param out
	 *           Output file or buffer for ex. System.out
	 */
	public DataModelToDOT(final PrintStream out) {
		this.out = out;

		/* Default value */
		DataModelToDOT.cfgMdAff = CFG_MD_QNAME;
		DataModelToDOT.cfgMdCol = (cfgMdAff == CFG_MD_BOTH ? 5 : 4);
		DataModelToDOT.cfgLayout = LAYOUT_DOT;
		DataModelToDOT.cfgPrintAspect = PRT_ASPECT_NOT_ORPH;

		printHeadGraph();
	}

	private void printHeadGraph() {
		out.println("digraph G {");
//		out.println("rankdir=LR;");

		if (DataModelToDOT.cfgLayout == LAYOUT_TWOPI) {
			out.println("layout=\"twopi\";");
			out.println("ranksep=1.5;");
			out.println("overlap=\"scale\";");
		}
		if (DataModelToDOT.cfgLayout == LAYOUT_FDP) {
			out.println("layout=\"twopi\";");
			out.println("K=1;");			out.println("ranksep=1.5;");

			out.println("overlap=\"scale\";");
		}

		out.println("");
	}

	private void printFootGraph() {
		out.println("");
		out.println("/* Generate with https://github.com/jeci-sarl/Graphical-Alfresco-DataModel/ */");
		out.println("}");
	}

	public void loadModelFile(File xml) throws FileNotFoundException, XMLStreamException {
		InputStream in = new FileInputStream(xml);
		try {
			loadModelFile(in);
		}
		finally {
			IOUtils.closeQuietly(in);
		}
	}

	public void loadModelFile(InputStream in) throws FileNotFoundException, XMLStreamException {
		LoadFileModel lfm = new LoadFileModel(in, out);
		lfm.loadModelFile();
	}

	public void close() {
		printFootGraph();
	}

	public static void main(String[] args) throws FileNotFoundException, XMLStreamException {
		InputStream is = DataModelToDOT.class.getResourceAsStream("/alfresco/model/contentModel.xml");

		// PrintStream out = System.out;
		PrintStream out = new PrintStream("mdd_alfresco.dot");

		try {

			DataModelToDOT pa = new DataModelToDOT(out);
			pa.loadModelFile(is);
			pa.close();

		}
		finally {
			IOUtils.closeQuietly(is);
			IOUtils.closeQuietly(out);
		}
	}

}
