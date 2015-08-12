package repicea.app;

import org.junit.Assert;
import org.junit.Test;

import repicea.util.ObjectUtility;

public class REpiceaJARSVNAppVersionCompiler extends AbstractAppVersionCompiler {

	private static final String APP_URL = "https://svn.code.sf.net/p/repiceasource/svn/trunk";
	private static String Version_Filename = ObjectUtility.getPackagePath(REpiceaJARSVNAppVersionCompiler.class).replace("bin", "src") + REpiceaJARSVNAppVersion.ShortFilename;
	
	public REpiceaJARSVNAppVersionCompiler() {
		super();
	}
	
	@Test
	public void createRevisionFile() {
		REpiceaJARSVNAppVersionCompiler compiler = new REpiceaJARSVNAppVersionCompiler();
		try {
			compiler.createRevisionFile(APP_URL, Version_Filename);
		} catch (Exception e) {
			Assert.fail("Failed to compile revision number");
		}
	}
}
