package com.septemberhx.info.beans.pod;

import java.util.List;
import javax.annotation.Generated;
import com.google.gson.annotations.SerializedName;

@Generated("com.robohorse.robopojogenerator")
public class MPodMetrics {

	@SerializedName("metadata")
	private Metadata metadata;

	@SerializedName("containers")
	private List<ContainersItem> containers;

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

	public void setContainers(List<ContainersItem> containers){
		this.containers = containers;
	}

	public List<ContainersItem> getContainers(){
		return containers;
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
			",containers = '" + containers + '\'' + 
			",window = '" + window + '\'' + 
			",timestamp = '" + timestamp + '\'' + 
			"}";
		}
}