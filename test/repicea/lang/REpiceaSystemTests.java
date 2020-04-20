package repicea.lang;

import java.io.File;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import repicea.util.ObjectUtility;

public class REpiceaSystemTests {

	@Test
	public void addToClassPathSimpleTest1() throws Exception {
		String pathToTest1 = ObjectUtility.getPackagePath(getClass()).replace("bin", "test") + "addurltest1";
		REpiceaSystem.addToClassPath(pathToTest1);
		ClassLoader cl = ClassLoader.getSystemClassLoader();
		try {
			Class clazz = cl.loadClass("hw.HelloWorldTest1");
			clazz.newInstance();
			System.out.println("Succeeded!");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			Assert.fail();
		}
	}
	
	@Test
	public void getURLsFromClassPath() throws Exception {
		List<String> list = REpiceaSystem.getClassPathURLs();
		Assert.assertTrue("list size", list.size() > 0);
	}


	@Test
	public void addToClassPathSimpleTest2() throws Exception {
		String pathToTest2 = ObjectUtility.getPackagePath(getClass()).replace("bin", "test") + "addurltest2" + File.separator + "helloworldtest2.jar";
		REpiceaSystem.addToClassPath(pathToTest2);
		ClassLoader cl = ClassLoader.getSystemClassLoader();
		try {
			Class clazz = cl.loadClass("hw2.HelloWorldTest2");
			clazz.newInstance();
			System.out.println("Succeeded!");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			Assert.fail();
		}
	}

}