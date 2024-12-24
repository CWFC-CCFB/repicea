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

//	final XMLStreamWriter xsw;
	
//	XmlWriterHandler(XMLStreamWriter xsw, XmlList root) throws XMLStreamException {
//		this.xsw = xsw;	
//		this.xsw.writeStartDocument();
//		this.xsw.writeStartElement(XmlReaderHandler.XMLLIST_TAG);
//		processList(root);
//		this.xsw.writeEndElement();
//		this.xsw.writeEndDocument();
//	}
//	
//	void processList(XmlList xmlList) throws XMLStreamException {
//		xsw.writeStartElement(XmlReaderHandler.CLASSNAME_TAG);
//		xsw.writeCharacters(xmlList.className);
//		xsw.writeEndElement();
//		xsw.writeStartElement(XmlReaderHandler.REFHASHCODE_TAG);
//		xsw.writeCharacters(xmlList.refHashCode + "");
//		xsw.writeEndElement();
//		xsw.writeStartElement(XmlReaderHandler.ISARRAY_TAG);
//		xsw.writeCharacters(xmlList.isArray + "");
//		xsw.writeEndElement();
//		xsw.writeStartElement(XmlReaderHandler.ISPRIMITIVE_TAG);
//		xsw.writeCharacters(xmlList.isPrimitive + "");
//		xsw.writeEndElement();
//		for (XmlEntry entry : xmlList.list) {
//			processEntry(entry);
//		}
//	}
//	
//	
//	
//	void processEntry(XmlEntry entry) throws XMLStreamException {
//		xsw.writeStartElement(XmlReaderHandler.LIST_TAG);
//		xsw.writeStartElement(XmlReaderHandler.FIELDNAME_TAG);
//		xsw.writeCharacters(entry.fieldName);
//		xsw.writeEndElement();
//		if (entry.value != null) {
//			xsw.writeStartElement(XmlReaderHandler.VALUE_TAG);
//			Class<?> clazz = entry.value.getClass();
//			String type = XMLTYPEMAP.get(clazz);
//			if (type == null) {
//				throw new XMLStreamException("Class " + clazz.getName() + " does not have any associated type!");
//			}
//			xsw.writeAttribute("xsi:type", type);
//			if (entry.value instanceof XmlList) {
//				processList((XmlList) entry.value);
//			} else {
//				xsw.writeCharacters(entry.value.toString());
//			}
//			xsw.writeEndElement();
//		}
//		xsw.writeEndElement();
//	}
	
	final StringBuilder sb;
	
	XmlWriterHandler(XmlList root) throws XMLStreamException {
		sb = new StringBuilder();
		sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>");
		processOpeningTag(XmlReaderHandler.XMLLIST_TAG);
		processList(root);
		processClosingTag(XmlReaderHandler.XMLLIST_TAG);
	}

	void processSimpleTag(String tag, Object value) {
		processOpeningTag(tag);
		sb.append(value.toString());
		processClosingTag(tag);
		
	}

	void processOpeningTag(String tag, String attribute) {
		sb.append("<");
		sb.append(tag);
		if (attribute != null) {
			sb.append(" ");
			sb.append(attribute);
		}
		sb.append(">");
	}

	void processOpeningTag(String tag) {
		processOpeningTag(tag, null);
	}

	void processClosingTag(String tag) {
		sb.append("</");
		sb.append(tag);
		sb.append(">");
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
					sb.append(changeSpecialCharactersIfNeedBe((String) entry.value));
				} else {
					sb.append(entry.value.toString());
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
