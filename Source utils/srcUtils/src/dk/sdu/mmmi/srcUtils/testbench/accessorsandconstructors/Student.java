/*
 * Featureous is distributed under the GPLv3 license.
 *
 * University of Southern Denmark, 2011
 */
package dk.sdu.mmmi.srcUtils.testbench.accessorsandconstructors;

public class Student {
	private String name;
	private String surname;
	private int age;
	
	public int getAge() {
		return age;
	}
	public String getName() {
		return name;
	}
	public String getSurname() {
		return surname;
	}
	public void setAge(int age) {
		this.age = age;
	}
	public void setName(String name) {
		this.name = name;
	}
	public void setSurname(String surname) {
		this.surname = surname;
	}
	
	public Student(){
		this("?", "?", 18);
	}
	
	public Student(String name){
		this(name, "?", 18);
		}
	
	public Student(String name, String surname){
		this(name, surname, 18);
	}
	
	public Student(String name, String surname, int age){
		this.name = name;
		this.surname = surname;
		this.age = age;
	}
	
	public void readBook(Book b){
		b.getContent();
	}
	
}
