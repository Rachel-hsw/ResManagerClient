package com.resmanager.client.model;

import java.io.Serializable;

public class ResultModel implements Serializable {

	private static final long serialVersionUID = 1L;
	private String result;
	private String description;
	private String path;
	private String DriverTask;
//  resultmodel.result = "false";
   // resultmodel.description = "您本月没有消耗流量";


	public String getDriverTask() {
		return DriverTask;
	}

	public void setDriverTask(String driverTask) {
		DriverTask = driverTask;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	/**
	 * @return result
	 */
	public String getResult() {
		return result;
	}

	/**
	 * @param result
	 *            要设置的 result
	 */
	public void setResult(String result) {
		this.result = result;
	}

	/**
	 * @return description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description
	 *            要设置的 description
	 */
	public void setDescription(String description) {
		this.description = description;
	}
}
