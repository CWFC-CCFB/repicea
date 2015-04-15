package repicea.serial.cloner;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.Assert;
import org.junit.Test;

import repicea.serial.Memorizable;
import repicea.serial.MemorizerPackage;

public class SerialClonerTest {

	@SuppressWarnings("serial")
	protected static class FakeMemorizable extends ArrayList<Double> implements Memorizable, Serializable {

		FakeMemorizable() {
			Random random = new Random();
			for (int i = 0; i < 10000; i++) {
				add(random.nextDouble());
			}
		}
		
		
		@Override
		public MemorizerPackage getMemorizerPackage() {
			MemorizerPackage mp = new MemorizerPackage();
			mp.add(this);
			return mp;
		}

		@SuppressWarnings({ "unchecked", "unused", "rawtypes" })
		@Override
		public void unpackMemorizerPackage(MemorizerPackage wasMemorized) {
			List<Double> memorizedList = (List) wasMemorized.remove(0);
		}
		
	}
	
	/*
	 * Ensures that the XML cloning is 10 times faster than the regular serial cloning
	 */
	@Test
	public void testCloningSpeed() {
		FakeMemorizable fakeInstance = new FakeMemorizable();
		BasicSerialCloner basicCloner = new BasicSerialCloner();
		XmlSerialCloner<Object> xmlCloner = new XmlSerialCloner<Object>();
		long start = System.currentTimeMillis();
		basicCloner.cloneThisObject(fakeInstance.getMemorizerPackage());
		long elapsedTimeBasic = System.currentTimeMillis() - start;
		System.out.println("Time for basic cloner: " + elapsedTimeBasic + " msec.");
		start = System.currentTimeMillis();
		xmlCloner.cloneThisObject(fakeInstance);
		long elapsedTimeXml = System.currentTimeMillis() - start;
		System.out.println("Time for xml cloner: " + elapsedTimeXml + " msec.");
		Assert.assertTrue(elapsedTimeXml < elapsedTimeBasic * .1);
	}
	
	
}
