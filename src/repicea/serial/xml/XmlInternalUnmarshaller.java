package repicea.serial.xml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;

class XmlInternalUnmarshaller {


	private static enum Type {Opening, Closing, Header, END_OF_MARKERS}
	private Vector<Object> openedInstances;

	final InputStream is;
	String str;

	XmlInternalUnmarshaller(File file) throws FileNotFoundException {
		this(new FileInputStream(file)); 
	}

	XmlInternalUnmarshaller(InputStream is) {
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
			marker = extractMarker();
		} while (getMarkerType(marker) != Type.Opening);

		if (!marker.equals("xmlList")) {	
			throw new XmlMarshallException("Unable to properly read the file!");
		}

		XmlList xmlList = new XmlList(); 
		fillList(xmlList);

		return xmlList;
	}

	String fillList(XmlList xmlList) throws XmlMarshallException {
		String marker = extractMarker();
		while (getMarkerType(marker) == Type.Opening) {
			marker = processOpeningMarker(marker, xmlList);
			if (marker.equals("/value")) {
				return marker;
			}
			marker = extractMarker();
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

	private String processOpeningMarker(String marker, XmlList xmlList) throws XmlMarshallException {
		String value = extractValue();
		if (marker.equals("className")) {
			xmlList.className = value;
			return extractMarker();
		} else if (marker.equals("refHashCode")) {
			xmlList.refHashCode = Integer.parseInt(value);
			return extractMarker();
		} else if (marker.equals("isArray")) {
			xmlList.isArray = Boolean.parseBoolean(value);
			return extractMarker();
		} else if (marker.equals("isPrimitive")) {
			xmlList.isPrimitive = Boolean.parseBoolean(value);
			return extractMarker();
		} else if (marker.equals("list")) {
			do {
				marker = addEntryToInternalList(xmlList, marker);
				if (!marker.equals("/list")) {
					throw new XmlMarshallException("Not closing entry!");
				}
				marker = extractMarker();
			} while (marker.equals("list"));
			return marker;
		}
		return null;
	}

	private String addEntryToInternalList(XmlList xmlList, String marker) throws XmlMarshallException {
		XmlEntry entry = new XmlEntry();
		xmlList.list.add(entry);
		marker = extractMarker();
		if (marker.equals("fieldName")) {
			entry.fieldName = extractValue();
			if (getMarkerType(extractMarker()) != Type.Closing) {
				throw new XmlMarshallException("Unable to properly read the file!");
			}
		}
		marker = extractMarker();
		if (marker.equals("xmlList")) {
			XmlList subXmlList = new XmlList();
			entry.value = subXmlList; 
			marker = fillList(subXmlList);
		} else if (marker.equals("xs:int")) {
			entry.value = Integer.parseInt(extractValue());
			marker = extractMarker();
		} else if (marker.equals("xs:double")){
			entry.value = Double.parseDouble(extractValue());
			marker = extractMarker();
		} else if (marker.equals("xs:string")){
			entry.value = extractValue();
			marker = extractMarker();
		} else if (marker.equals("xs:float")){
			entry.value = Float.parseFloat(extractValue());
			marker = extractMarker();
		} else if (marker.equals("/list")) {	// value is null
			return marker;
		}
		//			//			marker = extractMarker();
		if (!marker.equals("/value")) {
			throw new XmlMarshallException("Not closing value!");
		}
		marker = extractMarker();
		return marker;
	}

	private String extractMarker() {
		String marker;
		int pointerStart = str.indexOf("<");
		int pointerEnd = str.indexOf(">");
		if (pointerStart == -1) {
			return Type.END_OF_MARKERS.name();
		}
		marker = str.substring(pointerStart + 1, pointerEnd);
		str = str.substring(pointerEnd + 1);
		if (marker.startsWith("/")) {	// closing
			openedInstances.remove(marker.substring(1));
		} else if (!marker.startsWith("?")) {	// opening
			if (marker.startsWith("value")) {
				openedInstances.insertElementAt("value", 0);
				marker = marker.substring(marker.indexOf("xsi:type=") + 10);
				marker = marker.substring(0, marker.indexOf("\""));
			} else {
				openedInstances.insertElementAt(marker, 0);
			}
		}
		return marker;
	}

	private String extractValue() {
		String value;
		int pointerEnd = str.indexOf("<");
		if (pointerEnd == 0) {
			return "";
		} else {
			value = str.substring(0, pointerEnd);
			str = str.substring(pointerEnd);
			return value;
		}
	}

	private Type getMarkerType(String marker) {
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


