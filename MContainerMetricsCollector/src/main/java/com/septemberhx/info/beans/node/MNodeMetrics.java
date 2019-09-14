package com.septemberhx.info.beans.node;

import javax.annotation.Generated;
import com.google.gson.annotations.SerializedName;

@Generated("com.robohorse.robopojogenerator")
public class MNodeMetrics {

	@SerializedName("metadata")
	private Metadata metadata;

	@SerializedName("usage")
	private Usage usage;

	@SerializedName("window")
	private String window;

	@SerializedName("timestamp")
	private String timestamp;

	public void setMetadata(Metadata metadata){
		this.metadata = metadata;
	}

	public Metadata getMetadata(){
		return metadata;
	}

	public void setUsage(Usage usage){
		this.usage = usage;
	}

	public Usage getUsage(){
		return usage;
	}

	public void setWindow(String window){
		this.window = window;
	}

	public String getWindow(){
		return window;
	}

	public void setTimestamp(String timestamp){
		this.timestamp = timestamp;
	}

	public String getTimestamp(){
		return timestamp;
	}

	@Override
 	public String toString(){
		return 
			"ItemsItem{" + 
			"metadata = '" + metadata + '\'' + 
			",usage = '" + usage + '\'' + 
			",window = '" + window + '\'' + 
			",timestamp = '" + timestamp + '\'' + 
			"}";
		}
}