/**   
 * @Title: RecoveryConfirmAsyncTask.java 
 * @Package com.resmanager.client.user.recyle 
 * @Description: 确认回收
 * @author ShenYang  
 * @date 2016-1-8 下午4:57:51 
 * @version V1.0   
 */
package com.resmanager.client.user.recyle;

import java.io.IOException;

import org.xmlpull.v1.XmlPullParserException;

import com.alibaba.fastjson.JSON;
import com.resmanager.client.model.ResultModel;
import com.resmanager.client.utils.CommonView;
import com.resmanager.client.utils.ContactsUtils;
import com.resmanager.client.utils.Tools;
import com.resmanager.client.utils.WebServiceUtil;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;

/**
 * @ClassName: RecoveryConfirmAsyncTask
 * @Description:确认回收
 * @author ShenYang
 * @date 2016-1-8 下午4:57:51
 * 
 */
public class RecoveryConfirmAsyncTask extends AsyncTask<Void, Void, String> {
	private Context context;
	private String workId, MapLocationName, MapSpecificLocation, Longitude, Latitude, Recovery_Remark, CustomerID, CustomerName,small,big,small_remark,big_remark,qianshou_man,qianshou_man_phone;
	private Bitmap recoveryImg;
	private RecoveryListener recoveryListener;
	private Dialog loadingDialog;
	private int SWITCH_QR_CODE;
	private String WarehouseName;
	private String ysddh;

	public RecoveryConfirmAsyncTask(Context context, String workId, String MapLocationName, String MapSpecificLocation, String Longitude, String Latitude,
			String Recovery_Remark, String CustomerID, String CustomerName, Bitmap recoveryImg,int SWITCH_QR_CODE,String small,String big,String small_remark,String big_remark,String qianshou_man,String qianshou_man_phone,String WarehouseName,String ysddh) {
		this.context = context;
		this.workId = workId;
		this.MapLocationName = MapLocationName;
		this.MapSpecificLocation = MapSpecificLocation;
		this.Latitude = Latitude;
		this.Longitude = Longitude;
		this.Recovery_Remark = Recovery_Remark;
		this.CustomerID = CustomerID;
		this.CustomerName = CustomerName;
		this.recoveryImg = recoveryImg;
		this.SWITCH_QR_CODE = SWITCH_QR_CODE;
		this.small=small;
		this.big=big;
		this.small_remark=small_remark;
		this.big_remark=big_remark;
		this.qianshou_man=qianshou_man;
		this.qianshou_man_phone=qianshou_man_phone;
		this.WarehouseName=WarehouseName;
		this.ysddh=ysddh;
	}

	@Override
	protected String doInBackground(Void... arg0) {
		WebServiceUtil ws = new WebServiceUtil(false, ContactsUtils.WS_URL, ContactsUtils.Recovery_Confirm);
		ws.addProperty("UserKey", ContactsUtils.USER_KEY);
		ws.addProperty("WorkID", workId);
		ws.addProperty("MapLocationName", MapLocationName);
		ws.addProperty("MapSpecificLocation", MapSpecificLocation);
		ws.addProperty("Longitude", Longitude);
		ws.addProperty("Latitude", Latitude);
		ws.addProperty("Recovery_Remark", Recovery_Remark);
		ws.addProperty("CustomerID", CustomerID);
		ws.addProperty("CustomerName", CustomerName);
		ws.addProperty("image", Tools.getImageByte(recoveryImg));
		ws.addProperty("fileName", CustomerID + "-" + Tools.getNowTime() + ".jpg");
		ws.addProperty("SWITCH_QR_CODE", SWITCH_QR_CODE);
		ws.addProperty("small", small);
		ws.addProperty("big", big);
		ws.addProperty("small_remark", small_remark);
		ws.addProperty("big_remark", big_remark);
		ws.addProperty("qianshou_man", qianshou_man);
		ws.addProperty("qianshou_man_phone", qianshou_man_phone);
		ws.addProperty("warehouseName", WarehouseName);
		ws.addProperty("ysddh", ysddh);
		
		try {
			String jsonStr = ws.start();
			return jsonStr;
		} catch (IOException e) {
			e.printStackTrace();
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	protected void onPostExecute(String rv) {
		super.onPostExecute(rv);
		if (rv != null) {
			try {
				ResultModel resultModel = JSON.parseObject(rv, ResultModel.class);
				recoveryListener.recoveryResult(resultModel);
			} catch (Exception e) {
				recoveryListener.recoveryResult(null);
				e.printStackTrace();
			}
		} else {
			recoveryListener.recoveryResult(null);
		}

		if (this.loadingDialog.isShowing()) {
			this.loadingDialog.cancel();
			this.loadingDialog = null;
		}
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		if (this.loadingDialog == null) {
			this.loadingDialog = CommonView.LoadingDialog(context);
		}
		if (this.loadingDialog.isShowing() == false) {
			this.loadingDialog.show();
		}
	}

	public RecoveryListener getRecoveryListener() {
		return recoveryListener;
	}

	public void setRecoveryListener(RecoveryListener recoveryListener) {
		this.recoveryListener = recoveryListener;
	}

	public interface RecoveryListener {
		public void recoveryResult(ResultModel resultModel);
	}
}
