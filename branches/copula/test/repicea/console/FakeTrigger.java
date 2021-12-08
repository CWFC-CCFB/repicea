package repicea.console;

import repicea.lang.REpiceaSystem;
import repicea.multiprocess.JavaProcessWrapper;

public class FakeTrigger extends Trigger {


	@Override
	protected JavaProcessWrapper createProcessWrapper() {return null;}

	@Override
	protected String getSettingMemoryFilename() {
		return REpiceaSystem.getJavaIOTmpDir() + "test.ser";
	}

	@Override
	protected String getName() {
		return "FakeTrigger";
	}

	@Override
	protected String getTitle() {
		return "I am the fake trigger!";
	}
	

	public static void main(String[] args) {
		new FakeTrigger().startApplication();
	}

}
