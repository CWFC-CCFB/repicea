/*
 * This file is part of the repicea library.
 *
 * Copyright (C) 2023 His Majesty the King in Right of Canada
 * Author: Mathieu Fortin, Canadian Forest Service 
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed with the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * Please see the license at http://www.gnu.org/copyleft/lesser.html.
 */
package repicea.serial;

import java.util.LinkedHashMap;

/**
 * The Mappable interface ensures the instance can be expressed 
 * as a Map containing the members and their values. <p>
 * This interface is useful for JSON serialization.
 * @author Mathieu Fortin - December 2023
 */
public interface Mappable {

	/**
	 * Provide a LinkedHashMap instance that ensures a proper
	 * JSON or CSV representation of this object.
	 * @return a LinkedHashMap instance
	 */
	public LinkedHashMap<String, Object> getMapRepresentation();
}
