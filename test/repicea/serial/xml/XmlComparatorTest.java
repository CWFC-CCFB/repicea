package repicea.serial.xml;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class XmlComparatorTest {

	private static class FakeClass {
		@SuppressWarnings("unused")
		double param1;
		@SuppressWarnings("unused")
		String param2;
		
		FakeClass(int param1, String param2) {
			this.param1 = param1;
			this.param2 = param2;
		}
	}

	
	private static class FakeClassWithList {
		@SuppressWarnings("unused")
		double param1;
		@SuppressWarnings("unused")
		String param2;
		List<Object> list;
		
		FakeClassWithList(int param1, String param2) {
			this.param1 = param1;
			this.param2 = param2;
		}
	}

	
	@Test
	public void SimpleComparisonTest() {
		FakeClass obj1 = new FakeClass(1, "instance1");
		FakeClass obj2 = new FakeClass(1, "instance1");
		boolean comparable = XmlMarshallingUtilities.areTheseTwoObjectsComparable(obj1, obj2);
		Assert.assertTrue(comparable);
	}
	
	@Test
	public void SimpleComparisonTestWithOneNull() {
		FakeClass obj1 = new FakeClass(1, "instance1");
		boolean comparable = XmlMarshallingUtilities.areTheseTwoObjectsComparable(obj1, null);
		Assert.assertTrue(!comparable);
	}

	
	@Test
	public void SimpleComparisonTestWithBothNull() {
		boolean comparable = XmlMarshallingUtilities.areTheseTwoObjectsComparable(null, null);
		Assert.assertTrue(comparable);
	}

	@Test
	public void ComparisonTestWithList() {
		FakeClassWithList obj1 = new FakeClassWithList(1, "instance1");
		FakeClassWithList obj2 = new FakeClassWithList(1, "instance1");
		boolean comparable = XmlMarshallingUtilities.areTheseTwoObjectsComparable(obj1, obj2);
		Assert.assertTrue(comparable);
	}

	@Test
	public void ComparisonTestWithList2() {
		FakeClassWithList obj1 = new FakeClassWithList(1, "instance1");
		FakeClassWithList obj2 = new FakeClassWithList(1, "instance1");
		obj2.list = new ArrayList<Object>();
		boolean comparable = XmlMarshallingUtilities.areTheseTwoObjectsComparable(obj1, obj2);
		Assert.assertTrue(!comparable);
	}

	@Test
	public void ComparisonTestWithListAndEmbeddedObject() {
		FakeClassWithList obj1 = new FakeClassWithList(1, "instance1");
		FakeClassWithList obj2 = new FakeClassWithList(1, "instance1");
		obj1.list = new ArrayList<Object>();
		obj1.list.add(obj1);
		obj2.list = new ArrayList<Object>();
		obj2.list.add(obj2);
		boolean comparable = XmlMarshallingUtilities.areTheseTwoObjectsComparable(obj1, obj2);
		Assert.assertTrue(comparable);
	}

	@Test
	public void ExtendedComparisonTestWithListAndEmbeddedObject() {
		FakeClassWithList obj1 = new FakeClassWithList(1, "instance1");
		FakeClassWithList obj2 = new FakeClassWithList(1, "instance1");
		FakeClassWithList obj3 = new FakeClassWithList(2, "instance2");
		obj1.list = new ArrayList<Object>();
		obj1.list.add(obj1);
		obj1.list.add(obj3);
		obj1.list.add(obj1);
		obj2.list = new ArrayList<Object>();
		obj2.list.add(obj2);
		obj2.list.add(obj3);
		obj2.list.add(obj3);
		boolean comparable = XmlMarshallingUtilities.areTheseTwoObjectsComparable(obj1, obj2);
		Assert.assertTrue(!comparable);
	}

	
	@Test
	public void SuperExtendedComparisonTestWithListAndEmbeddedObject() {
		FakeClassWithList obj1 = new FakeClassWithList(1, "instance1");
		FakeClassWithList obj2 = new FakeClassWithList(1, "instance1");
		FakeClassWithList obj3 = new FakeClassWithList(2, "instance2");
		FakeClassWithList obj4 = new FakeClassWithList(2, "instance2");
		obj1.list = new ArrayList<Object>();
		obj2.list = new ArrayList<Object>();
		obj3.list = new ArrayList<Object>();
		obj4.list = new ArrayList<Object>();
		obj1.list.add(obj1);
		obj1.list.add(obj3);
		obj1.list.add(obj2);
		obj1.list.add(obj4);
		obj2.list.add(obj2);
		obj2.list.add(obj4);
		obj2.list.add(obj1);
		obj2.list.add(obj3);
		boolean comparable = XmlMarshallingUtilities.areTheseTwoObjectsComparable(obj1, obj2);
		Assert.assertTrue(comparable);
	}

	@Test
	public void SuperDuperExtendedComparisonTestWithListAndEmbeddedObject() {
		FakeClassWithList obj1 = new FakeClassWithList(1, "instance1");
		FakeClassWithList obj2 = new FakeClassWithList(1, "instance1");
		FakeClassWithList obj3 = new FakeClassWithList(2, "instance2");
		FakeClassWithList obj4 = new FakeClassWithList(2, "instance2");
		obj1.list = new ArrayList<Object>();
		obj2.list = new ArrayList<Object>();
		obj3.list = new ArrayList<Object>();
		obj4.list = new ArrayList<Object>();
		obj1.list.add(obj1);
		obj1.list.add(obj3);
		
		obj2.list.add(obj2);
		obj2.list.add(obj4);
		
		obj3.list.add(obj2);
		obj3.list.add(obj4);
		
		obj4.list.add(obj1);
		obj4.list.add(obj3);
		boolean comparable = XmlMarshallingUtilities.areTheseTwoObjectsComparable(obj1, obj2);
		Assert.assertTrue(comparable);
	}

}
