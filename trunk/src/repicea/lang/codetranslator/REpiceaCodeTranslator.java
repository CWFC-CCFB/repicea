/*
 * This file is part of the repicea library.
 *
 * Copyright (C) 2009-2018 Mathieu Fortin for Rouge Epicea.
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
package repicea.lang.codetranslator;

/**
 * This interface ensures that the instance can process request (String) coming from other 
 * languages and can convert them into Java objects or methods.
 * @author Mathieu Fortin - December 2018
 */
public interface REpiceaCodeTranslator {

	/**
	 * This method processes a request from another language. 
	 * @param request a String
	 * @return an Object instance or null
	 * @throws Exception should throw an exception if anything goes wrong
	 */
	public Object processCode(String request) throws Exception;

}
