package test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;


@RunWith(Suite.class)
@SuiteClasses({	repicea.util.StringSplitterTests.class,
				repicea.serial.xml.XmlSerializationTest.class,
				repicea.serial.xml.XmlComparatorTest.class,
				repicea.serial.cloner.SerialClonerTest.class})
public class AllUtilityTests {}
