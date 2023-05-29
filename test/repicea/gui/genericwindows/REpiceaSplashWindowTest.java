package repicea.gui.genericwindows;

import java.io.IOException;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import repicea.util.JarUtility;
import repicea.util.ObjectUtility;

public class REpiceaSplashWindowTest {

	
	public static void main(String[] args) {
		String packagePath = ObjectUtility.getRelativePackagePath(REpiceaSplashWindowTest.class);
		String iconPath =  packagePath + "SplashImage.jpg";
		String filePath = JarUtility.getJarFileImInIfAny(REpiceaSplashWindowTest.class);
		String version;
		if (filePath != null) {
			try {
				Manifest m = JarUtility.getManifestFromThisJarFile(filePath);
				version = m.getMainAttributes().get(Attributes.Name.SPECIFICATION_VERSION).toString();				
			} catch (IOException e) {
				version = "Unknown";			
			}
		} else {
			version = "Unknown";			
		}
		
		String bottomSplashWindowString = "Version " + version;
		new REpiceaSplashWindow(iconPath, 3, null, -1, bottomSplashWindowString, 12);
		
		new REpiceaSplashWindow(iconPath, 3, null, 300, bottomSplashWindowString, 18);
		
		new REpiceaSplashWindow(iconPath, 3, null, 500, bottomSplashWindowString, 16);

	}
	
}
