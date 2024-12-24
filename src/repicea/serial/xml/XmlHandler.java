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

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * An internal class to parse object serialized using
 * the XmlSerializer class.
 * @author Mathieu Fortin - December 2024
 */
class XmlHandler extends DefaultHandler {

	private static final String XMLLIST_TAG = "xmlList";
	private static final String CLASSNAME_TAG = "className";
	private static final String REFHASHCODE_TAG = "refHashCode";
	private static final String ISARRAY_TAG = "isArray";
	private static final String ISPRIMITIVE_TAG = "isPrimitive";
	
	private static final String LIST_TAG = "list";

	private static final String FIELDNAME_TAG = "fieldName";
	private static final String VALUE_TAG = "value";

	private XmlList root;
	private List<XmlList> xmlLists;
	private List<XmlEntry> xmlEntries;
	private List<String> types;
	private StringBuilder sb;

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		if (sb == null) {
			sb = new StringBuilder();
		} else {
			sb.append(ch, start, length);
		}
	}

	@Override
	public void startDocument() throws SAXException {
		root = null;
		xmlLists = new ArrayList<XmlList>();
		xmlEntries = new ArrayList<XmlEntry>();
		types = new ArrayList<String>();
	}

	@Override
	public void startElement(String uri, String lName, String qName, Attributes attr) throws SAXException {
		switch (qName) {
		case XMLLIST_TAG:
			if (root == null) {
				root = new XmlList();
				xmlLists.add(root);
			} else {
				XmlList newList = new XmlList();
				xmlEntries.get(xmlEntries.size() - 1).value = newList;
				xmlLists.add(newList);
			}
			break;
		case CLASSNAME_TAG:
		case REFHASHCODE_TAG:
		case ISARRAY_TAG:
		case ISPRIMITIVE_TAG:
		case FIELDNAME_TAG:
			sb = new StringBuilder();
			break;
		case LIST_TAG:
			XmlEntry newEntry = new XmlEntry();
			getLatestXmlList().list.add(newEntry);
			xmlEntries.add(newEntry);
			break;
		case VALUE_TAG:
			String type = attr.getValue("xsi:type");
			types.add(type);
			if (type.equals(XMLLIST_TAG)) {
				XmlList newList = new XmlList();
				xmlEntries.get(xmlEntries.size() - 1).value = newList;
				xmlLists.add(newList);
			} else {
				sb = new StringBuilder();
			}
			break;
		}
	}

	private XmlList getLatestXmlList() {
		return xmlLists.get(xmlLists.size() - 1);
	}
	
	private XmlEntry getLatestXmlEntry() {
		return xmlEntries.get(xmlEntries.size() - 1);
	}

	private String getLatestType() {
		return types.get(types.size() - 1);
	}
	
	XmlList getRoot() {return root;}
	
	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		switch (qName) {
		case CLASSNAME_TAG:
			String className = sb.toString();
			getLatestXmlList().className = className;
			break;
		case REFHASHCODE_TAG:
			int refHashCode = Integer.parseInt(sb.toString());
			getLatestXmlList().refHashCode = refHashCode;
			break;
		case ISARRAY_TAG:
			boolean isArray = Boolean.parseBoolean(sb.toString());
			getLatestXmlList().isArray = isArray;
			break;
		case ISPRIMITIVE_TAG:
			boolean isPrimitive = Boolean.parseBoolean(sb.toString());
			getLatestXmlList().isPrimitive = isPrimitive;
			break;
		case LIST_TAG:
			xmlEntries.remove(xmlEntries.size() - 1);	// the entry is closed
			break;
		case FIELDNAME_TAG:
			String fieldName = sb.toString();
			getLatestXmlEntry().fieldName = fieldName;
			break;
		case VALUE_TAG:
			String type = getLatestType();
			if (type.equals(XMLLIST_TAG)) {
				xmlLists.remove(xmlLists.size() - 1); // the xml list is closed
				types.remove(types.size() - 1);
			} else {
				switch(type) {
				case "xs:int":
					getLatestXmlEntry().value = Integer.parseInt(sb.toString());
					types.remove(types.size() - 1);
					break;
				case "xs:double":
					getLatestXmlEntry().value = Double.parseDouble(sb.toString());
					types.remove(types.size() - 1);
					break;
				case "xs:float":
					getLatestXmlEntry().value = Float.parseFloat(sb.toString());
					types.remove(types.size() - 1);
					break;
				case "xs:string":
					getLatestXmlEntry().value = sb.toString();
					types.remove(types.size() - 1);
					break;
				case "xs:boolean":
					getLatestXmlEntry().value = Boolean.parseBoolean(sb.toString());
					types.remove(types.size() - 1);
					break;
				case "xs:byte":
					getLatestXmlEntry().value = Byte.parseByte(sb.toString());
					types.remove(types.size() - 1);
					break;
				case "xs:short":
					getLatestXmlEntry().value = Short.parseShort(sb.toString());
					types.remove(types.size() - 1);
					break;
				case "xs:long":
					getLatestXmlEntry().value = Long.parseLong(sb.toString());
					types.remove(types.size() - 1);
					break;
				case "xs:unsignedShort":
					getLatestXmlEntry().value = (char) Integer.parseInt(sb.toString());
					types.remove(types.size() - 1);
					break;
					
				default:
					throw new InvalidParameterException("This type is not recognized:" + type);
				}
			}
			break;
		case XMLLIST_TAG:
			xmlLists.remove(xmlLists.size() - 1); // the xml list is closed
			break;
		}
	}

}



