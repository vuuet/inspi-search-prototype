package org.inspilab.prototype.search;

import java.util.ArrayList;

public class Keyword 
{
	private String id;
	private String value;
	private ArrayList<String> childrenList;
	private ArrayList<String> parentsList;
	private ArrayList<String> spellingAlternateList;
	
	
	
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public String getValue() {
		return value;
	}
	
	public void setValue(String value) {
		this.value = value;
	}
	
	public ArrayList<String> getChildrenList() {
		return childrenList;
	}
	
	public void setChildrenList(ArrayList<String> childrenList) {
		this.childrenList = childrenList;
	}
	
	public ArrayList<String> getParentsList() {
		return parentsList;
	}
	
	public void setParentsList(ArrayList<String> parentsList) {
		this.parentsList = parentsList;
	}
	
	public ArrayList<String> getSpellingAlternateList() {
		return spellingAlternateList;
	}
	
	public void setSpellingAlternateList(ArrayList<String> spellingAlternateList) {
		this.spellingAlternateList = spellingAlternateList;
	}
	
	
}
