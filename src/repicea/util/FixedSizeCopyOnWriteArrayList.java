/*
 * This file is part of the repicea-util library.
 *
 * Copyright (C) 2009-2012 Mathieu Fortin for Rouge Epicea.
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
package repicea.util;

import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;

public class FixedSizeCopyOnWriteArrayList<E> extends CopyOnWriteArrayList<E> {

	private static final long serialVersionUID = 20111231L;
	
	private int maximumSize;
		
	public FixedSizeCopyOnWriteArrayList(int maximumSize) throws IllegalArgumentException {
		super();
		setMaximumSize(maximumSize);
	}
	
	public FixedSizeCopyOnWriteArrayList(Collection<? extends E> collection) {
		super(collection);
		setMaximumSize(collection.size());
	}
	
	public FixedSizeCopyOnWriteArrayList(E[] toCopyIn) {
		super(toCopyIn);
		setMaximumSize(toCopyIn.length);
	}

	
	private void setMaximumSize(int maxSize) throws IllegalArgumentException {
		if (maxSize < 1) {
			throw new IllegalArgumentException("The maximum size must be equal to or greater than 1");
		}
		this.maximumSize = maxSize;
	}
	
	@Override
	public boolean add(E obj) {
		boolean returnValue = super.add(obj);
		while (size() > maximumSize) {
			remove(0);
		}
		return returnValue;
	}

	@Override
	public void add(int index, E obj) {
		super.add(index, obj);
		while (size() > maximumSize) {
			remove(0);
		}
	}

	@Override
	public boolean addAll(int index, Collection<? extends E> collection) {
		boolean returnValue = super.addAll(index, collection);
		while (size() > maximumSize) {
			remove(0);
		}
		return returnValue;
	}
	
	@Override
	public boolean addAll(Collection<? extends E> collection) {
		boolean returnValue = super.addAll(collection);
		while (size() > maximumSize) {
			remove(0);
		}
		return returnValue;
	}
	
	@Override
	public boolean addIfAbsent(E obj) {
		boolean returnValue = super.addIfAbsent(obj);
		while (size() > maximumSize) {
			remove(0);
		}
		return returnValue;
	}
	
	@Override
	public int addAllAbsent(Collection<? extends E> collection) {
		int returnValue = super.addAllAbsent(collection);
		while (size() > maximumSize) {
			remove(0);
		}
		return returnValue;
	}
	
}
