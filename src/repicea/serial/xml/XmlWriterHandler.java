/*
 * This file is part of the repicea library.
 *
 * Copyright (C) 2024His Majesty the King in right of Canada
 * Author: Mathieu Fortin, Canadian Forest Service.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed with the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * Please see the license at http://www.gnu.org/copyleft/lesser.html.
 */
package repicea.serial.xml;

import java.io.IOException;
import java.io.OutputStream;
import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.xml.stream.XMLStreamException;

/**
 * An internal class to write objects into an
 * XML format.
 * @author Mathieu Fortin - December 2024
 */
final class XmlWriterHandler {
	
	private final static Map<Class<?>, String> XMLTYPEMAP = new HashMap<Class<?>, String>();
	static {
		XMLTYPEMAP.put(Byte.class, "xsi:type=\"xs:byte\"");
		XMLTYPEMAP.put(byte.class, "xsi:type=\"xs:byte\"");
		XMLTYPEMAP.put(Short.class, "xsi:type=\"xs:short\"");
		XMLTYPEMAP.put(short.class, "xsi:type=\"xs:short\"");
		XMLTYPEMAP.put(Character.class, "xsi:type=\"xs:unsignedShort\"");
		XMLTYPEMAP.put(char.class, "xsi:type=\"xs:unsignedShort\"");
		XMLTYPEMAP.put(Integer.class, "xsi:type=\"xs:int\"");
		XMLTYPEMAP.put(int.class, "xsi:type=\"xs:int\"");
		XMLTYPEMAP.put(Long.class, "xsi:type=\"xs:long\"");
		XMLTYPEMAP.put(long.class, "xsi:type=\"xs:long\"");
		XMLTYPEMAP.put(Float.class, "xsi:type=\"xs:float\"");
		XMLTYPEMAP.put(float.class, "xsi:type=\"xs:float\"");
		XMLTYPEMAP.put(Double.class, "xsi:type=\"xs:double\"");
		XMLTYPEMAP.put(double.class, "xsi:type=\"xs:double\"");
		XMLTYPEMAP.put(Boolean.class, "xsi:type=\"xs:boolean\"");
		XMLTYPEMAP.put(boolean.class, "xsi:type=\"xs:boolean\"");
		XMLTYPEMAP.put(String.class, "xsi:type=\"xs:string\"");
		XMLTYPEMAP.put(XmlList.class, "xsi:type=\"xmlList\"");
	}
	
	static final Map<String, String> SPECIAL_CHARACTERS = new LinkedHashMap<String, String>();
	static {
		SPECIAL_CHARACTERS.put("&", "&amp;");
		SPECIAL_CHARACTERS.put("<", "&lt;");
		SPECIAL_CHARACTERS.put(">", "&gt;");
		SPECIAL_CHARACTERS.put("\"", "&quot;");
	}

	static final int BUFFER_SIZE = 10000;
	final StringBuilder sb;
	final OutputStream os;
	
	XmlWriterHandler(XmlList root, OutputStream os) throws XMLStreamException, IOException {
		if (os == null) {
			throw new InvalidParameterException("The os argument cannot be null!");
		}
		this.os = os;
		sb = new StringBuilder();
		addStringToStringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>");
		processOpeningTag(XmlReaderHandler.XMLLIST_TAG);
		processList(root);
		processClosingTag(XmlReaderHandler.XMLLIST_TAG);
		triggerWriteToOutput();
	}

	void processSimpleTag(String tag, Object value) throws XMLStreamException {
		processOpeningTag(tag);
		addStringToStringBuilder(value.toString());
		processClosingTag(tag);
	}

	void triggerWriteToOutput() throws XMLStreamException {
		String xmlString = sb.toString();
		try {
			os.write(xmlString.getBytes("UTF-8"));
		} catch (Exception e) {
			throw new XMLStreamException(e.getMessage());
		}
	}
	
	void addStringToStringBuilder(String str) throws XMLStreamException {
		sb.append(str);
		if (sb.length() > BUFFER_SIZE) {
			triggerWriteToOutput();
			sb.delete(0, sb.length());
		}
	}
	
	void processOpeningTag(String tag, String attribute) throws XMLStreamException {
		addStringToStringBuilder("<");
		addStringToStringBuilder(tag);
		if (attribute != null) {
			addStringToStringBuilder(" ");
			addStringToStringBuilder(attribute);
		}
		addStringToStringBuilder(">");
	}

	void processOpeningTag(String tag) throws XMLStreamException {
		processOpeningTag(tag, null);
	}

	void processClosingTag(String tag)  throws XMLStreamException{
		addStringToStringBuilder("</");
		addStringToStringBuilder(tag);
		addStringToStringBuilder(">");
	}
	
	void processList(XmlList xmlList) throws XMLStreamException {
		processSimpleTag(XmlReaderHandler.CLASSNAME_TAG, xmlList.className);
		processSimpleTag(XmlReaderHandler.REFHASHCODE_TAG, xmlList.refHashCode);
		processSimpleTag(XmlReaderHandler.ISARRAY_TAG, xmlList.isArray);
		processSimpleTag(XmlReaderHandler.ISPRIMITIVE_TAG, xmlList.isPrimitive);
		for (XmlEntry entry : xmlList.list) {
			processEntry(entry);
		}
	}

	void processEntry(XmlEntry entry) throws XMLStreamException {
		processOpeningTag(XmlReaderHandler.LIST_TAG);
		processSimpleTag(XmlReaderHandler.FIELDNAME_TAG, entry.fieldName);
		if (entry.value != null) {
			Class<?> clazz = entry.value.getClass();
			String type = XMLTYPEMAP.get(clazz);
			if (type == null) {
				throw new XMLStreamException("Class " + clazz.getName() + " does not have any associated type!");
			}
			processOpeningTag(XmlReaderHandler.VALUE_TAG, type);
			if (entry.value instanceof XmlList) {
				processList((XmlList) entry.value);
			} else {
				if (clazz.equals(String.class)) {
					addStringToStringBuilder(changeSpecialCharactersIfNeedBe((String) entry.value));
				} else {
					addStringToStringBuilder(entry.value.toString());
				}
			}
			processClosingTag(XmlReaderHandler.VALUE_TAG);
		}
		processClosingTag(XmlReaderHandler.LIST_TAG);
	}

	private String changeSpecialCharactersIfNeedBe(String value) {
		for (String sp : SPECIAL_CHARACTERS.keySet()) {
			if (value.contains(sp)) {
				value = value.replace(sp, SPECIAL_CHARACTERS.get(sp));
			}
		}
		return value;
	}

	
}
