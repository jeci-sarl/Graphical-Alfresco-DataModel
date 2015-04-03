package fr.jeci.alfresco.gadm;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.Map.Entry;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

public class LoadFileModel implements ConfigConstant, FontAwesome {

	/* Graph Style */
	private static final String EDGE_PARENT_STYLE = "[ dir=back, weight=2]";
	private static final String EDGE_ASPECT_STYLE = "[color=\"orange\", style=dotted, dir=back, weight=0.5]";
	private final static String TYPE_TITLE = "<FONT COLOR=\"BLACK\">{}</FONT>";
	private final static String TABLE_OPEN = "<TABLE BORDER=\"1\" CELLBORDER=\"0\" CELLSPACING=\"0\" CELLPADDING=\"2\">";
	private final static String TABLE_CLOSE = "</TABLE>";
	private final static String TITLE_TD_STYLE_TYPE = "CELLPADDING=\"1.5\" CELLSPACING=\"0\"  BGCOLOR=\"lightgoldenrod\"";
	private final static String TITLE_TD_STYLE_ASPECT = "CELLPADDING=\"1.5\" CELLSPACING=\"0\"  BGCOLOR=\"lightblue\"";

	private Map<String, StringBuilder> typeLabel;
	private Map<String, StringBuilder> aspectLabel;
	private XMLEventReader eventReader;

	private String type = null;
	private String aspect = null;
	private Stack<String> xpath = null;
	private PrintStream out;
	private XMLEvent event;

	public LoadFileModel(InputStream in, final PrintStream out) throws XMLStreamException {
		final XMLInputFactory inputFactory = XMLInputFactory.newInstance();
		this.eventReader = inputFactory.createXMLEventReader(in);
		this.out = out;

		this.typeLabel = new HashMap<>();
		this.aspectLabel = new HashMap<>();
		this.xpath = new Stack<>();
	}

	public void loadModelFile() throws FileNotFoundException, XMLStreamException {
		while (eventReader.hasNext()) {
			event = eventReader.nextEvent();
			if (event.isStartElement()) {
				printStartElt();
			}
			if (event.isEndElement()) {
				printEndElt();
			}
		}
	}

	private void printStartElt() throws XMLStreamException {
		StartElement startElement = event.asStartElement();
		String startName = startElement.getName().getLocalPart();
		xpath.add(startName);
		if (type == null && startName.equals("type") && xpath.elementAt(xpath.size() - 2).equals("types")) {
			type = startElement.getAttributeByName(QName.valueOf("name")).getValue();
			StringBuilder label = new StringBuilder();
			label.append(TABLE_OPEN);
			label.append(rowTitle(type, TITLE_TD_STYLE_TYPE));
			label.append("\n<HR/><TR><TD></TD></TR>");

			typeLabel.put(type, label);
		}

		if (startName.equals("aspects")) {
			// out.println("\n/* Aspects */\nsubgraph cluster_aspects {");
			out.println("\n/* Aspects */\n");
		}

		if (aspect == null && startName.equals("aspect") && xpath.elementAt(xpath.size() - 2).equals("aspects")) {
			aspect = startElement.getAttributeByName(QName.valueOf("name")).getValue();
			StringBuilder label = new StringBuilder();
			label.append(TABLE_OPEN);
			label.append(rowTitle(aspect, TITLE_TD_STYLE_ASPECT));
			label.append("\n<HR/><TR><TD></TD></TR>");

			aspectLabel.put(aspect, label);
		}

		if (type != null) {
			event = eventReader.nextEvent();

			if (startName.equals("parent")) {
				linkParent();
			}
			if (startName.equals("property")) {
				typeLabel.get(type).append(printProperty(startElement));
			}
			if (startName.equals("aspect")) {
				linkAspect();
			}
		}

		if (aspect != null) {
			event = eventReader.nextEvent();

			if (startName.equals("property")) {
				aspectLabel.get(aspect).append(printProperty(startElement));
			}
		}
	}

	private void linkAspect() {
		String laspect = event.asCharacters().getData();
		out.println("\"" + laspect + "\" -> \"" + type + "\" " + EDGE_ASPECT_STYLE + ";");
	}

	private void linkParent() {
		String parent = event.asCharacters().getData();
		out.println("\"" + parent + "\" -> \"" + type + "\" " + EDGE_PARENT_STYLE + ";");
	}

	private void printEndElt() {
		EndElement endElement = event.asEndElement();
		String endName = endElement.getName().getLocalPart();
		String pop = xpath.pop();
		if (!pop.equals(endName)) {
			System.err.println(pop + " != " + endName);
		}

		if (endName.equals("type") && xpath.elementAt(xpath.size() - 1).equals("types")) {
			typeLabel.get(type).append(TABLE_CLOSE);
			type = null;
		}

		if (endName.equals("types")) {
			printTypeNodes();
		}

		if (endName.equals("aspect") && xpath.elementAt(xpath.size() - 1).equals("aspects")) {
			aspectLabel.get(aspect).append(TABLE_CLOSE);
			aspect = null;
		}

		if (endName.equals("aspects")) {
			printAspectNodes();
		}
	}

	private void printTypeNodes() {
		out.println("\n/* Types */");
		for (Entry<String, StringBuilder> entry : typeLabel.entrySet()) {
			out.print("\"" + entry.getKey() + "\"");
			out.println(" [shape=none, margin=0, label=<" + entry.getValue().toString() + ">];\n\n");
		}
	}

	private void printAspectNodes() {
		out.println("\n/* Aspect */");
		for (Entry<String, StringBuilder> entry : aspectLabel.entrySet()) {
			out.print("\"" + entry.getKey() + "\"");
			out.println(" [shape=none, margin=0, label=<" + entry.getValue().toString() + ">];\n\n");
		}
	}

	private StringBuilder printProperty(StartElement startElement) throws XMLStreamException {
		String prop = startElement.getAttributeByName(QName.valueOf("name")).getValue();
		String title = null, dtype = null;
		boolean mandatory = false;
		List<String> flags = new ArrayList<>(3);

		while (eventReader.hasNext()) {
			event = eventReader.nextEvent();
			if (event.isEndElement()) {
				String endLab = event.asEndElement().getName().getLocalPart();
				if (endLab.equals("property")) {
					break;
				}
			}
			else if (event.isStartElement()) {
				String startLab = event.asStartElement().getName().getLocalPart();
				switch (startLab) {
				case "title":
					event = eventReader.nextEvent();
					title = event.asCharacters().getData();
					break;

				case "type":
					event = eventReader.nextEvent();
					dtype = event.asCharacters().getData();
					break;

				case "mandatory":
					Attribute enforced = event.asStartElement().getAttributeByName(QName.valueOf("enforced"));
					mandatory = enforced != null && Boolean.parseBoolean(enforced.getValue());
					event = eventReader.nextEvent();
					mandatory = mandatory && Boolean.parseBoolean(event.asCharacters().getData());
					break;

				case "index":
					Attribute index = event.asStartElement().getAttributeByName(QName.valueOf("enabled"));
					if (index != null && Boolean.parseBoolean(index.getValue())) {
						flags.add(CHAR_INDEX);
					}
					break;

				case "protected":
					event = eventReader.nextEvent();
					if (Boolean.parseBoolean(event.asCharacters().getData())) {
						flags.add(CHAR_PROTECTED);
					}
					break;

				case "multiple":
					event = eventReader.nextEvent();
					if (Boolean.parseBoolean(event.asCharacters().getData())) {
						flags.add(CHAR_MULTIPLE);
					}
					break;

				case "constraints":
					flags.add(CHAR_CONSTAINTS);
					break;

				default:
					break;
				}

			}

		}

		return rowMd(prop, title, dtype, flags.toArray(new String[] {}), mandatory);
	}

	private static StringBuilder rowTitle(String labelType, String style) {
		StringBuilder sb = new StringBuilder();
		sb.append("\n<TR><TD ").append(style);
		sb.append(" COLSPAN=\"").append(DataModelToDOT.cfgMdCol).append("\">");
		sb.append(labelType).append("</TD></TR>");

		return sb;
	}

	private static StringBuilder rowMd(String labelMd, String titleMd, String typeMd, String[] flag, boolean mandatory) {
		StringBuilder row = new StringBuilder("\n<TR><TD>");
		if (mandatory) {
			row.append("<FONT  FACE=\"Bandal\">").append(CHAR_MANDATORY).append("</FONT>");
		}
		row.append("</TD><TD ALIGN=\"LEFT\">");

		switch (DataModelToDOT.cfgMdAff) {
		case CFG_MD_QNAME:
		default:
			row.append(labelMd).append("</TD>");
			break;

		case CFG_MD_TITLE:
			row.append(titleMd).append("</TD>");
			break;

		case CFG_MD_BOTH:
			row.append(titleMd).append("</TD><TD>");
			row.append(labelMd).append("</TD>");
			break;
		}

		row.append("<TD ALIGN=\"RIGHT\">");
		for (String f : flag) {
			row.append("<FONT  FACE=\"Bandal\">").append(f).append("</FONT>");
		}

		row.append("</TD><TD>");

		row.append(typeMd).append("</TD></TR>");
		return row;
	}

}
