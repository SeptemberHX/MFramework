package com.septemberhx.info.beans.node;

import java.util.List;
import javax.annotation.Generated;
import com.google.gson.annotations.SerializedName;

@Generated("com.robohorse.robopojogenerator")
public class MNodesMetricsResponse{

	@SerializedName("metadata")
	private Metadata metadata;

	@SerializedName("apiVersion")
	private String apiVersion;

	@SerializedName("kind")
	private String kind;

	@SerializedName("items")
	private List<MNodeMetrics> items;

	public void setMetadata(Metadata metadata){
		this.metadata = metadata;
	}

	public Metadata getMetadata(){
		return metadata;
	}

	public void setApiVersion(String apiVersion){
		this.apiVersion = apiVersion;
	}

	public String getApiVersion(){
		return apiVersion;
	}

	public void setKind(String kind){
		this.kind = kind;
	}

	public String getKind(){
		return kind;
	}

	public void setItems(List<MNodeMetrics> items){
		this.items = items;
	}

	public List<MNodeMetrics> getItems(){
		return items;
	}

	@Override
 	public String toString(){
		return 
			"MNodesMetricsResponse{" + 
			"metadata = '" + metadata + '\'' + 
			",apiVersion = '" + apiVersion + '\'' + 
			",kind = '" + kind + '\'' + 
			",items = '" + items + '\'' + 
			"}";
		}
}