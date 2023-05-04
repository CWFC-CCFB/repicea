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
package repicea.lang;

/**
 * This class is used to control the memory before a OutOfMemoryError object is thrown. The class only has one static method that checks
 * the available memory. If the available memory is below a given threshold, the method throws a ExpectedMemoryCapacityException. This kind
 * of exception is derived from RunTimeException. Consequently, there is no need for the method that implements checkAvailabmeMemory() to throw
 * Exception. <p>
 * 
 * The threshold can be set either in terms of percentage of the total memory (through MAXIMUM_PROPORTION_USED public static member) or in 
 * terms of minimum free space available in MegaBytes (through MINIMUM_FREE_SPACE public static member). By default, MAXIMUM_PROPORTION_USED is set to 0.9 and 
 * MINIMUM_FREE_SPACE is set to 10. <p>
 * 
 * Example of code: 
 * <pre>
 * <code>
 * public void myMethod() {
 * 	try {
 * 		MemoryWatchDog.checkAvailableMemory() 
 * 		...
 * 	} catch (Exception e)
 * 		System.out.println(e.getMessage());
 * 	}
 * }
 * </code>
 * </pre> 
 *
 * @author Mathieu Fortin - March 2011
 */
public class MemoryWatchDog {

	/**
	 * This exception class is thrown when the memory is about to exceed its capacity.
	 * @author Mathieu Fortin - March 2011
	 */
	public static class ExpectedMemoryCapacityException extends RuntimeException {
		
		private static final long serialVersionUID = 20110330L;

		protected ExpectedMemoryCapacityException(String message) {
			super(message);
		}

	}
	
	private static final double MEGA_FACTOR = 1E-6;
	
	/**
	 * The maximum proportion of the memory that can be used before throwing an
	 * ExpectedMemoryCapacityException. A double between 0 to 1. The default value 
	 * is 0.90 .
	 */
	public static double MAXIMUM_PROPORTION_USED = 0.90;

	/**
	 * The minimum memory space that should be free to allow the system to work properly.
	 * A double that represents a number of MegaBytes. The default value is set to 10 Mb.
	 */
	public static double MINIMUM_FREE_SPACE = 10;
	
	
	/**
	 * Maximum memory capacity in MgBytes.
	 */
	private static double MAXIMUM_MEMORY_AVAILABLE = Runtime.getRuntime().maxMemory() * MEGA_FACTOR;;
	
	
	/**
	 * Checks if the available memory is still above the threshold is used. If not, the garbage collector is called and the
	 * test is run again. If the available memory is still below the threshold after calling the garbage collector, an ExpectedMemoryCapacityException is thrown.
	 * @return the number of Mg still available
	 * @throws ExpectedMemoryCapacityException if the maximum memory capacity has been reached
	 */
	public static double checkAvailableMemory() throws ExpectedMemoryCapacityException {
		double currentUsedMemory = checkUsedMemory();
		if (currentUsedMemory > MAXIMUM_PROPORTION_USED * MAXIMUM_MEMORY_AVAILABLE || 
				MAXIMUM_MEMORY_AVAILABLE - currentUsedMemory < MINIMUM_FREE_SPACE) {
			System.gc();
			currentUsedMemory = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) * MEGA_FACTOR;
	
			if (currentUsedMemory > MAXIMUM_PROPORTION_USED * MAXIMUM_MEMORY_AVAILABLE || 
					MAXIMUM_MEMORY_AVAILABLE - currentUsedMemory < MINIMUM_FREE_SPACE) {
				int percentageUsed = (int) (currentUsedMemory / MAXIMUM_MEMORY_AVAILABLE * 100);	
				throw new ExpectedMemoryCapacityException("Warning!!! The memory is used up to " + percentageUsed + "% its full capacity. Please save and close some projects if possible.");
			}			
		}
		return MAXIMUM_MEMORY_AVAILABLE - currentUsedMemory;
	}
	
	

	/**
	 * Checks the memory that is currently used.
	 * @return the memory load in Mg
	 */
	public static double checkUsedMemory() {
		double currentUsedMemory = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) * MEGA_FACTOR;
		return currentUsedMemory;
	}
	
}
