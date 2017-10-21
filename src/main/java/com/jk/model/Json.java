package com.jk.model;

import java.io.Serializable;

public class Json implements Serializable{

	private static final long serialVersionUID = -1245837475929065940L;

	private boolean success = false;
	
	private String msg = "";
	
	private Object object = null;

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public Object getObject() {
		return object;
	}

	public void setObject(Object object) {
		this.object = object;
	}
	
}
