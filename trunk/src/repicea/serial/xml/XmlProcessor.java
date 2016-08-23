package repicea.serial.xml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

class XmlProcessor {

	private static final Map<String, String> ProblematicCharacters = new HashMap<String, String>();
	static {
		ProblematicCharacters.put("&lt;", "<");
		ProblematicCharacters.put("&gt;", ">");
		ProblematicCharacters.put("&amp;", "&");
		ProblematicCharacters.put("&apos;", "'");
		ProblematicCharacters.put("&quot;", "\"");
	}
	
	private static enum Type {Opening, Closing, Header, END_OF_MARKERS}
	private Vector<Object> openedInstances;

	final InputStream is;
	String str;

	XmlProcessor(File file) throws FileNotFoundException {
		this(new FileInputStream(file)); 
	}

	XmlProcessor(InputStream is) {
		this.is = is;
		openedInstances = new Vector<Object>();
	}

	XmlList unmarshall() throws IOException {
		byte[] buffer = new byte[1000];
		int nbRead = 0;
		str = null; 
		while ((nbRead = is.read(buffer)) != -1) {
			if (str == null) {
				str = new String(buffer, 0, nbRead, "UTF-8"); // for UTF-8 encoding
			} else {
				str = str.concat(new String(buffer, 0, nbRead, "UTF-8")); // for UTF-8 encoding
			}
		}

		XmlList list = createXmlList(str);
		return list;
	}

	private XmlList createXmlList(String str) throws XmlMarshallException {
		String marker;
		do {
			marker = extractTag();
		} while (getTagType(marker) != Type.Opening);

		if (!marker.equals("xmlList")) {	
			throw new XmlMarshallException("Unable to properly read the file!");
		}

		XmlList xmlList = new XmlList(); 
		fillList(xmlList);

		return xmlList;
	}

	String fillList(XmlList xmlList) throws XmlMarshallException {
		String marker = extractTag();
		while (getTagType(marker) == Type.Opening) {
			marker = processStartTag(marker, xmlList);
			if (marker.equals("/value")) {
				return marker;
			}
			marker = extractTag();
			if (marker.equals(Type.END_OF_MARKERS.name())) {
				if (!openedInstances.isEmpty()) {
					throw new XmlMarshallException("There are still some opened instances!");
				} else {
					return marker;
				}
			}
		} 
		if (!marker.equals("/value") && !marker.equals("/list")) {
			throw new XmlMarshallException("Not closing xmlList!");
		}
		return marker;
	}

	private String processStartTag(String markup, XmlList xmlList) throws XmlMarshallException {
		String value = extractContent();
		if (markup.equals("className")) {
			xmlList.className = value;
			return extractTag();
		} else if (markup.equals("refHashCode")) {
			xmlList.refHashCode = Integer.parseInt(value);
			return extractTag();
		} else if (markup.equals("isArray")) {
			xmlList.isArray = Boolean.parseBoolean(value);
			return extractTag();
		} else if (markup.equals("isPrimitive")) {
			xmlList.isPrimitive = Boolean.parseBoolean(value);
			return extractTag();
		} else if (markup.equals("list")) {
			do {
				markup = addEntryToInternalList(xmlList, markup);
				if (!markup.equals("/list")) {
					throw new XmlMarshallException("Not closing entry!");
				}
				markup = extractTag();
			} while (markup.equals("list"));
			return markup;
		}
		return null;
	}

	private String addEntryToInternalList(XmlList xmlList, String tag) throws XmlMarshallException {
		XmlEntry entry = new XmlEntry();
		xmlList.list.add(entry);
		tag = extractTag();
		if (tag.equals("fieldName")) {
			entry.fieldName = extractContent();
			if (getTagType(extractTag()) != Type.Closing) {
				throw new XmlMarshallException("Unable to properly read the file!");
			}
		}
		tag = extractTag();
		if (tag.equals("xmlList")) {
			XmlList subXmlList = new XmlList();
			entry.value = subXmlList; 
			tag = fillList(subXmlList);
		} else if (tag.equals("xs:int")) {
			entry.value = Integer.parseInt(extractContent());
			tag = extractTag();
		} else if (tag.equals("xs:double")){
			entry.value = Double.parseDouble(extractContent());
			tag = extractTag();
		} else if (tag.equals("xs:string")){
			entry.value = extractContent();
			tag = extractTag();
		} else if (tag.equals("xs:float")){
			entry.value = Float.parseFloat(extractContent());
			tag = extractTag();
		} else if (tag.endsWith("xs:boolean")) {
			entry.value = Boolean.parseBoolean(extractContent());
			tag = extractTag();
		} else if (tag.equals("/list")) {	// value is null
			return tag;
		}
		//			//			marker = extractMarker();
		if (!tag.equals("/value")) {
			throw new XmlMarshallException("Not closing value!");
		}
		tag = extractTag();
		return tag;
	}

	private String extractTag() {
		String tag;
		int pointerStart = str.indexOf("<");
		int pointerEnd = str.indexOf(">");
		if (pointerStart == -1) {
			return Type.END_OF_MARKERS.name();
		}
		tag = str.substring(pointerStart + 1, pointerEnd);
		str = str.substring(pointerEnd + 1);
		if (tag.startsWith("/")) {	// closing
			openedInstances.remove(tag.substring(1));
		} else if (!tag.startsWith("?")) {	// opening
			if (tag.startsWith("value")) {
				openedInstances.insertElementAt("value", 0);
				tag = tag.substring(tag.indexOf("xsi:type=") + 10);
				tag = tag.substring(0, tag.indexOf("\""));
			} else {
				openedInstances.insertElementAt(tag, 0);
			}
		}
		return tag;
	}

	private String extractContent() {
		String value;
		int pointerEnd = str.indexOf("<");
		if (pointerEnd == 0) {
			return "";
		} else {
			value = str.substring(0, pointerEnd);
			str = str.substring(pointerEnd);
			for (String pc : ProblematicCharacters.keySet()) {
				value = value.replaceAll(pc, ProblematicCharacters.get(pc));
			}
			return value;
		}
	}

	private Type getTagType(String marker) {
		if (marker.startsWith("/")) {	// closing
			return Type.Closing;
		} else if (marker.startsWith("?")) { // header
			return Type.Header;
		} else if (marker.equals(Type.END_OF_MARKERS.name())) {
			return Type.END_OF_MARKERS;
		} else {
			return Type.Opening;
		}
	} 
}


