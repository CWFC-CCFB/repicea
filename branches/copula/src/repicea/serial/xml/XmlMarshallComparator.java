/*
 * This file is part of the repicea-util library.
 *
 * Copyright (C) 2009-2015 Mathieu Fortin for Rouge Epicea.
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class XmlMarshallComparator {
	
	
	class ComparisonIndex {
		
		final String className1;
		final int refHashCode1;
		final String className2;
		final int refHashCode2;
		
		ComparisonIndex(XmlMarshallComparator comparator, XmlList list1, XmlList list2) {
			this.className1 = list1.className;
			this.refHashCode1 = list1.refHashCode;
			this.className2 = list2.className;
			this.refHashCode2 = list2.refHashCode;
			if (!comparator.hasObjectBeenRegistered(className1, refHashCode1)) {
				comparator.registerObject(className1, refHashCode1, list1);
			}
			if (!comparator.hasObjectBeenRegistered(className2, refHashCode2)) {
				comparator.registerObject(className2, refHashCode2, list2);
			}
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == null || !(obj instanceof ComparisonIndex)) {
				return false;
			} else {
				ComparisonIndex index = (ComparisonIndex) obj;
				if (className1.equals(index.className1)) {
					if (refHashCode1 == index.refHashCode1) {
						if (className2.equals(index.className2)) {
							if (refHashCode2 == index.refHashCode2) {
								return true;
							}
						}
					}
				}
				if (className2.equals(index.className1)) {		// we try the other way around
					if (refHashCode2 == index.refHashCode1) {
						if (className1.equals(index.className2)) {
							if (refHashCode1 == index.refHashCode2) {
								return true;
							}
						}
					}
				}
			}
			return false;
		}
	}
	
	private final List<ComparisonIndex> comparisonIndices = new ArrayList<ComparisonIndex>();
	private Map<String, Map<Integer, XmlList>> registeredObjects;
	
	protected XmlMarshallComparator() {
		registeredObjects = new HashMap<String, Map<Integer, XmlList>>();
	}
	
	private void registerObject(String className, int hashCode, XmlList obj) {
		if (!registeredObjects.containsKey(className)) {
			registeredObjects.put(className, new HashMap<Integer, XmlList>());
		}
		registeredObjects.get(className).put(hashCode, obj);
	}
	
	private boolean hasObjectBeenRegistered(String className, int hashCode) {
		if (registeredObjects.containsKey(className)) {
			return registeredObjects.get(className).containsKey(hashCode);
		} else {
			return false;
		}
	}
	
	private XmlList retrieveObject(String className, int hashCode) {
		return registeredObjects.get(className).get(hashCode);
	}

	
	private boolean isComparisonRegistered(XmlList list1, XmlList list2) {
		ComparisonIndex index = new ComparisonIndex(this, list1, list2);
		if (comparisonIndices.contains(index)) {
			return true;
		} else {
			comparisonIndices.add(index);
			return false;
		}
	}


	protected boolean compareTheseTwoObjects(Object obj1, Object obj2) {
		if (obj1 == null && obj2 == null) {
			return true;
		} else if (obj1 == null) {
			return false;
		} else if (obj2 == null) { 
			return false;
		} else {
			XmlMarshaller marshaller1 = new XmlMarshaller();
			XmlList list1 = marshaller1.marshall(obj1);
			
			XmlMarshaller marshaller2 = new XmlMarshaller();
			XmlList list2 = marshaller2.marshall(obj2);
			
			return compareTheseTwoXmlList(list1, list2);
		}
	}
	
	private boolean compareTheseTwoXmlList(XmlList list1, XmlList list2) {
		if (list1 == null && list2 == null) {
			return true;
		} else if (list1 == null) {
			return false;
		} else if (list2 == null) {
			return false;
		} else {
			if (isComparisonRegistered(list1, list2)) {
				return true;
			} else {
				list1 = retrieveObject(list1.className, list1.refHashCode);	//	 we retrieve the first object to be serialized
				list2 = retrieveObject(list2.className, list2.refHashCode);	//	 we retrieve the first object to be serialized
				if (!list1.className.equals(list2.className)) {
					return false;
				} else if (list1.isPrimitive != list2.isPrimitive) {
					return false; 
				} else if (list1.isArray != list2.isArray) {	// we do not compare the hashcode which is likely different
					return false;
				} else if (list1.list.size() != list1.list.size()) {
					return false;
				} else {
					for (int i = 0; i < list1.list.size(); i++) {
						XmlEntry entry1 = list1.list.get(i);
						XmlEntry entry2 = list2.list.get(i);
						if (!entry1.fieldName.equals(entry2.fieldName)) {
							return false;
						} else {
							if (entry1.value == null && entry2.value == null) {
								return true;
							} else if (entry1.value == null) {
								return false;
							} else if (entry2.value == null) {
								return false;
							} else {
								if (entry1.value  instanceof XmlList && entry2.value instanceof XmlList) {
									if (!compareTheseTwoXmlList((XmlList) entry1.value, (XmlList) entry2.value)) {
										return false;
									}
								} else if (entry1.value instanceof XmlList) {
									return false;
								} else if (entry2.value instanceof XmlList) {
									return false;
								} else if (!entry1.value.equals(entry2.value)) {
									return false;
								}
							}
						}
					}
				}
			}
		} 
		return true;
	}
	
}
