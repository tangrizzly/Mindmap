package com.mindmap.model;

public class Response {
	String status;
	Object data;
	String msg;

	public Response(String status, Object data, String msg) {
		this.status = status;
		this.data = data;
		this.msg = msg;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}
}
