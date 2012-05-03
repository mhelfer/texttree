package com.sbj.texttree.domain;

import java.util.List;

public class TextTree implements Comparable<TextTree>{ 
	public long id;
	public String name;
	public List<TreeContact> treeContacts;
	
	public TextTree() { }
	
	public TextTree(final String name) {
		this.name = name;
	}
	
	public String toString() { 
		String size = "0";
		if(treeContacts != null) { 
			size = String.valueOf(treeContacts.size());
		}
		return name + "( " + size + " )";
	}

	@Override
	public int compareTo(TextTree another) {
		return name.toLowerCase().compareTo(another.name.toLowerCase());
	}
	
	 
}
