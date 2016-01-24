/*
 * This file is part of the repicea library.
 *
 * Copyright (C) 2009-2016 Mathieu Fortin for Rouge Epicea.
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
package repicea.net.server;

import java.io.Serializable;
import java.security.InvalidParameterException;

public class ServerConfiguration implements Serializable {

	private static final long serialVersionUID = 20111222L;
	
	protected final int numberOfClientThreads;
	protected final int outerPort;
	protected final int innerPort;
	
	public ServerConfiguration(int numberOfClientThreads, int outerPort, int innerPort) {
		if (numberOfClientThreads < 0 || numberOfClientThreads > 10) {
			throw new InvalidParameterException("Number of client threads should be between 1 and 10");
		} else {
			this.numberOfClientThreads = numberOfClientThreads;
		}
		if (outerPort < 1024 || outerPort > 49151) {
			throw new InvalidParameterException("The outer port must be between 1024 and 49151");
		} else {
			this.outerPort = outerPort;
		}
		if (innerPort < 1024 || innerPort > 49151) {
			throw new InvalidParameterException("The inner port must be between 1024 and 49151");
		} else {
			this.innerPort = innerPort;
		}
	}


}	