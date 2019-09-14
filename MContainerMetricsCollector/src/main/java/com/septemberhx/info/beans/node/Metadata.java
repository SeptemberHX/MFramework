package com.septemberhx.info.beans.node;

import javax.annotation.Generated;
import com.google.gson.annotations.SerializedName;

@Generated("com.robohorse.robopojogenerator")
public class Metadata{

	@SerializedName("name")
	private String name;

	@SerializedName("creationTimestamp")
	private String creationTimestamp;

	@SerializedName("selfLink")
	private String selfLink;

	public void setName(String name){
		this.name = name;
	}

	public String getName(){
		return name;
	}

	public void setCreationTimestamp(String creationTimestamp){
		this.creationTimestamp = creationTimestamp;
	}

	public String getCreationTimestamp(){
		return creationTimestamp;
	}

	public void setSelfLink(String selfLink){
		this.selfLink = selfLink;
	}

	public String getSelfLink(){
		return selfLink;
	}

	@Override
 	public String toString(){
		return 
			"Metadata{" + 
			"name = '" + name + '\'' + 
			",creationTimestamp = '" + creationTimestamp + '\'' + 
			",selfLink = '" + selfLink + '\'' + 
			"}";
		}
}