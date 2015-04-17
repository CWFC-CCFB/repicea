package repicea.simulation.processsystem;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import repicea.util.ObjectUtility;

public class ProcessSystemTest {

	private static enum FakeEnum {class1, class2}
	
	/**
	 * This test loads a file and checks if the process system can be read.
	 */
	@Test
	public void loadSystemTest() {
		String filename = ObjectUtility.getPackagePath(getClass()) + "systemTest1.prl";
		SystemManager manager = new SystemManager();
		try {
			manager.load(filename);
			System.out.println("Process system systemTest1.prl read!");
		} catch (IOException e) {
			Assert.fail();
		}
	}

	
	/**
	 * This test if the amount in input is equal to the sum of the amounts in output.
	 */
	@SuppressWarnings("rawtypes")
	@Test
	public void matterBalanceTest() {
		String filename = ObjectUtility.getPackagePath(getClass()) + "systemTest1.prl";
		SystemManager manager = new SystemManager();
		try {
			manager.load(filename);
			System.out.println("Process system systemTest1.prl read!");
		} catch (IOException e) {
			Assert.fail();
		}

		List<Processor> primaryProcessors = manager.getPrimaryProcessor();
		ProcessUnit<FakeEnum> inputUnit = new ProcessUnit<FakeEnum>();
		inputUnit.getAmountMap().put(FakeEnum.class1, 100d);
		inputUnit.getAmountMap().put(FakeEnum.class2, 100d);
		List<ProcessUnit> processUnits = new ArrayList<ProcessUnit>();
		processUnits.add(inputUnit);
		Collection<ProcessUnit> outputProcessUnits = primaryProcessors.get(0).doProcess(processUnits);
		double sumClass1 = 0d;
		double sumClass2 = 0d;
		for (ProcessUnit processUnit : outputProcessUnits) {
			sumClass1 += (Double) processUnit.getAmountMap().get(FakeEnum.class1);
			sumClass2 += (Double) processUnit.getAmountMap().get(FakeEnum.class2);
		}
		Assert.assertEquals(100d, sumClass1, 1E-10);
		Assert.assertEquals(100d, sumClass2, 1E-10);
	}

	
}
