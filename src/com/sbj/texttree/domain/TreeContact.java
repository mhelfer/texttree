package com.sbj.texttree.domain;

public class TreeContact { 
	public long id;
	
	public String contactName;
	public String contactPhone;
	
	public TreeContact() { }
	
	public TreeContact(final String contactName, final String contactPhone) {
		this.contactName = contactName;
		this.contactPhone = contactPhone;
	}
	
	public String toString() { 
		return contactName + " - " + contactPhone;
	}
}