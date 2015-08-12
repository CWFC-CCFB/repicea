package repicea.app;

import org.junit.Test;

import repicea.util.ObjectUtility;

public class REpiceaJARSVNAppVersion extends AbstractAppVersionCompiler {

	private static final String APP_URL = "https://svn.code.sf.net/p/repiceasource/svn/trunk";
	private static String Version_Filename = ObjectUtility.getRootPath(REpiceaJARSVNAppVersion.class) + "revision";
	
	public REpiceaJARSVNAppVersion() {
		super(APP_URL, Version_Filename);
	}

	@Test
	public void createVersionFile() {
		new REpiceaJARSVNAppVersion();
	}
}
