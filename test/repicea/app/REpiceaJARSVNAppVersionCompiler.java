package repicea.app;

import repicea.util.ObjectUtility;

public class REpiceaJARSVNAppVersionCompiler extends AbstractAppVersionCompiler {

	private static final String APP_URL = "https://svn.code.sf.net/p/repiceasource/svn/trunk";
	private static String Version_Filename = ObjectUtility.getPackagePath(REpiceaJARSVNAppVersionCompiler.class).replace("bin", "src") + REpiceaJARSVNAppVersion.ShortFilename;
	
	public REpiceaJARSVNAppVersionCompiler() {
		super();
	}
	
	public static void main(String args[]) {
		REpiceaJARSVNAppVersionCompiler compiler = new REpiceaJARSVNAppVersionCompiler();
		try {
			compiler.createRevisionFile(APP_URL, Version_Filename);
			System.out.println("Revision file successfully updated!");
		} catch (Exception e) {
			System.out.println("Error while updating revision file!");
		}
	}
	
	
}
