package repicea.lang.codetranslator;

import org.junit.Assert;
import org.junit.Test;

public class REnvironmentTests {

	
	@Test
	public void creatingAnArrayTest() throws Exception {
		REnvironment r = new REnvironment();
		System.out.println(int.class.getName());
		Object callback = r.processCode("createarray;int;integer3;integer2");
		Assert.assertTrue("Testing if the callback is formatted for an array", callback.toString().startsWith("JavaObject;[[I"));
	}
	
}
