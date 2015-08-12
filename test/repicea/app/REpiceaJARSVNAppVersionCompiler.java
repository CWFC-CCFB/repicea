package repicea.app;

import org.junit.Test;

import repicea.util.ObjectUtility;

public class REpiceaJARSVNAppVersionCompiler extends AbstractAppVersionCompiler {

	private static final String APP_URL = "https://svn.code.sf.net/p/repiceasource/svn/trunk";
	private static String Version_Filename = ObjectUtility.getRootPath(REpiceaJARSVNAppVersionCompiler.class) + "revision.csv";
	
	public REpiceaJARSVNAppVersionCompiler() {
		super();
	}
	
	@Test
	public void createRevisionFile() {
		REpiceaJARSVNAppVersionCompiler compiler = new REpiceaJARSVNAppVersionCompiler();
		compiler.createRevisionFile(APP_URL, Version_Filename);
	}
}
