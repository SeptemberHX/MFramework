package com.septemberhx.info.beans.pod;

import javax.annotation.Generated;
import com.google.gson.annotations.SerializedName;

@Generated("com.robohorse.robopojogenerator")
public class Usage{

	@SerializedName("memory")
	private String memory;

	@SerializedName("cpu")
	private String cpu;

	public void setMemory(String memory){
		this.memory = memory;
	}

	public String getMemory(){
		return memory;
	}

	public void setCpu(String cpu){
		this.cpu = cpu;
	}

	public String getCpu(){
		return cpu;
	}

	@Override
 	public String toString(){
		return 
			"Usage{" + 
			"memory = '" + memory + '\'' + 
			",cpu = '" + cpu + '\'' + 
			"}";
		}
}