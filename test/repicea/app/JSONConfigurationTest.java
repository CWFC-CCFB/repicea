package repicea.app;

import java.io.IOException;

import org.junit.Assert;
//import org.junit.Ignore;
import org.junit.Test;

import repicea.util.ObjectUtility;

public class JSONConfigurationTest {
	
	@Test
	public void test01ConfigConstructor() {
		String JSONfilename = ObjectUtility.getPackagePath(getClass()) + "processConfig.json";		
		
		JSONConfiguration local; 
		
		try {
			local = new JSONConfiguration(JSONfilename);			
		} catch (IOException e) {
			e.printStackTrace();
			Assert.fail();
			return;
		}				
		
		Long maxProcessingThreads = (Long)local.get("processing/maxThreads", null);
		Assert.assertNotNull("Ensure that maxProcessingThreads key has been found", maxProcessingThreads);
		
		Long maxProcessingMemoryLimitMB = (Long)local.get("processing/maxMemoryLimitMB", null);
		Assert.assertNotNull("Ensure that maxProcessingMemoryLimitMB key has been found", maxProcessingMemoryLimitMB);				
	}
	
	@Test
	public void test02ConfigDelayedLoadTest() {
		String JSONfilename = ObjectUtility.getPackagePath(getClass()) + "processConfig.json";		
		
		JSONConfiguration local; 
		
		try {
			local = new JSONConfiguration(JSONfilename);			
		} catch (IOException e) {
			e.printStackTrace();
			Assert.fail();
			return;
		}						
		
		Long maxProcessingThreads = (Long)local.get("processing/maxThreads", null);
		Assert.assertNotNull("Ensure that maxProcessingThreads key has been found", maxProcessingThreads);
		
		Long maxProcessingMemoryLimitMB = (Long)local.get("processing/maxMemoryLimitMB", null);
		Assert.assertNotNull("Ensure that maxProcessingMemoryLimitMB key has been found", maxProcessingMemoryLimitMB);				
	}
	
	@Test
	public void test03GlobalConfigDeepCopy() {
		String JSONfilename = ObjectUtility.getPackagePath(getClass()) + "processConfig.json";		
		
		JSONConfiguration local;
		
		try {
			local = new JSONConfiguration(JSONfilename);			
			JSONConfigurationGlobal.setInstance(local);
		} catch (IOException e) {
			e.printStackTrace();
			Assert.fail();
			return;
		}				
		
		// get a value from the local configuration 
		Long value = (Long)local.get("processing/deepcopytest/deepcopytestvalue", null);
		Assert.assertNotNull("Ensure that maxProcessingThreads key has been found", value);
		
		// change the value in the local config
		long newValue = value + 1;
		local.put("processing/deepcopytest/deepcopytestvalue", newValue);
		
		// get the same value from the global config
		Long globalvalue = (Long)JSONConfigurationGlobal.getInstance().get("processing/deepcopytest/deepcopytestvalue", null);
		Assert.assertNotNull("Ensure that maxProcessingThreads key has been found in global config", globalvalue);
		
		Assert.assertEquals("Make sure the value in the global config is still the initial value", value, globalvalue);
	}
	
	@Test
	public void test04KeyStoreHierarchical() {
					
		JSONConfiguration local = new JSONConfiguration();
		
		final String storedValue = "The answer is 42";
		
		local.put("level1/level2/testkey", storedValue);
				
		String retrieved = (String)local.get("level1/level2/testkey", "");
		Assert.assertEquals("Ensure that the retrieved key is equal to the stored value", storedValue, retrieved);						
	}
	
	@Test
	public void test05GlobalReferenceKeyStore() {
		
		final String storedValue = "The answer is 42";
		
		JSONConfiguration local = JSONConfigurationGlobal.getInstance(); 
		
		local.put("testkey", storedValue);		
							
		String retrieved = (String)JSONConfigurationGlobal.getInstance().get("testkey", "");
		
		Assert.assertEquals("Ensure that the retrieved key is equal to the stored value", storedValue, retrieved);						
	}
	
	@Test
	public void test05JarEmbeddedConfig() {

		String JSONfilename = ObjectUtility.getRelativePackagePath(JSONConfiguration.class) + "uselessFakeConfig.json";		
		
		JSONConfiguration local;
		
		try {
			local = new JSONConfiguration(JSONfilename);			
			JSONConfigurationGlobal.setInstance(local);
		} catch (IOException e) {
			e.printStackTrace();
			Assert.fail();
			return;
		}				

		local = JSONConfigurationGlobal.getInstance();
		Object value = local.get("cacheCleanupEntryExpiresAfterSec", null);
		Assert.assertTrue("Testing if the value is a long", value.getClass().equals(Long.class));
		Assert.assertEquals("Testing the value", 600L, value);
		
	}

}
