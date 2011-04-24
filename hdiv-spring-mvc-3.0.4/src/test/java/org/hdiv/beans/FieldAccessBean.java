package org.hdiv.beans;

public class FieldAccessBean
{
	public String name;

	protected int age;

	private TestBean spouse;


	public String getName() {
		return name;
	}

	public int getAge() {
		return age;
	}

	public TestBean getSpouse() {
		return spouse;
	}

}
