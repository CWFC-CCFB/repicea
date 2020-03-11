package repicea.lang;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import repicea.util.ObjectUtility;

public class REpiceaSystemTests {

	@Test
	public void addToClassPathSimpleTest() throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, IOException {
		String rootPath = ObjectUtility.getTrueRootPath(REpiceaSystem.class);
		File rp = new File(rootPath);
		File aboveRootPath = rp.getParentFile();
		String path = aboveRootPath.getAbsolutePath() +
				File.separator + "externallibraries" +
				File.separator + "mrnf-foresttools.jar";
		REpiceaSystem.addToClassPath(path);
		ClassLoader cl = ClassLoader.getSystemClassLoader();
		try {
			cl.loadClass("canforservutility.biosim.BioSimClient");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			Assert.fail();
		}
	}
	
	@Test
	public void getURLsFromClassPath() {
		List<String> list = REpiceaSystem.getClassPathURLs();
	}

}
