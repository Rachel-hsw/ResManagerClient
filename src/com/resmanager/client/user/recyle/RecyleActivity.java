/**   
  * @Title: RecyleActivity.java 
 * @Package com.resmanager.client.user.recyle 
 * @Description: 回收界面 
 * @author ShenYang  
 * @date 2016-1-5 上午10:43:34 
 * @version V1.0   
 */
package com.resmanager.client.user.recyle;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.amap.api.services.core.PoiItem;
import com.amap.api.services.poisearch.PoiResult;
import com.resmanager.client.R;
import com.resmanager.client.common.TopContainActivity;
import com.resmanager.client.main.photoalbum.Bimp;
import com.resmanager.client.model.CustomerModel;
import com.resmanager.client.model.LocationModel;
import com.resmanager.client.model.RecyleTempListModel;
import com.resmanager.client.model.RecyleTempModel;
import com.resmanager.client.model.ResultModel;
import com.resmanager.client.model.ScanBimpModel;
import com.resmanager.client.user.order.UploadCache;
import com.resmanager.client.user.order.unloading.UploadingActivity;
import com.resmanager.client.user.order.unloading.UploadingTrailAsyncTask;
import com.resmanager.client.user.order.unloading.UploadingTrailAsyncTask.UploadingTrailListener;
import com.resmanager.client.user.recyle.GetRecyleNumberAsyncTask.GetRecyleNumberListener;
import com.resmanager.client.user.recyle.RecoveryConfirmAsyncTask.RecoveryListener;
import com.resmanager.client.user.recyle.RecoveryContinueAsyncTask.RecyleContinueListener;
import com.resmanager.client.user.recyle.RecoveryImageAsyncTask.UploadRecyleResourceListener;
import com.resmanager.client.utils.ContactsUtils;
import com.resmanager.client.utils.LocationUtils;
import com.resmanager.client.utils.LocationUtils.PoiSearchListener;
import com.resmanager.client.utils.Tools;
import com.resmanager.client.view.CustomDialog;
import com.resmanager.client.view.CustomDialog.CancelBtnListener;
import com.resmanager.client.view.CustomDialog.ToDoListener;

/**
 * @ClassName: RecyleActivity
 * @Description: 回收界面
 * @author ShenYang
 * @date 2016-1-5 上午10:43:34
 * 
 */
@SuppressLint({ "InflateParams", "HandlerLeak" })
public class RecyleActivity extends TopContainActivity implements OnClickListener {
	private TextView location_str_txt, customer_name;
	private TextView add_source_btn;
	private LocationModel locationModel;
	private CustomerModel customerModel;
	private String workId;
	private ImageView recyle_img;
	private EditText recyle_remark_txt;
	
	private EditText dhs_small_remarktxt;
	private EditText dhs_small;
	private EditText dhs_big_remarktxt;
	private EditText dhs_big;
	private EditText qianshou_man_edit;
	private EditText qianshou_man_phone_edit;
    private  String recyle_number;
    private String dhs_big_num,dhs_small_num;
	private String WarehouseName;
	private EditText ysddh;
	private Bitmap uploading_id = null;
	private CustomDialog exitDialog, confirmUploadingDialog, continueRecyleDialog;
	private int SWITCH_QR_CODE=1;//送货扫描二维码是否显示的开关,1是不显示
	private Handler mHandler = new Handler() {
		@SuppressWarnings("unchecked")
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
				List<PoiItem> pois = (List<PoiItem>) msg.obj;
				if (pois.size() > 0) {
					PoiItem poiItem = pois.get(0);
					for (int i = 0; i < pois.size(); i++) {
						PoiItem tempItem = pois.get(i);
						if (tempItem.getTitle().equals(customerModel.getCustomerName())) {
							poiItem = tempItem;
						}
					}
					locationModel.setAddress(poiItem.getSnippet());
					locationModel.setName(poiItem.getTitle());
					locationModel.setLat(poiItem.getLatLonPoint().getLatitude());
					locationModel.setLng(poiItem.getLatLonPoint().getLongitude());
					location_str_txt.setTextColor(getResources().getColor(R.color.orange_color));
					location_str_txt.setText(poiItem.getTitle());
				}
				break;

			default:
				break;
			}
		};
	};
	private TextView choose_warehouse;
	private String input_dhs_small;
	private String input_dhs_big;
	/**
	 * 弹出对话框让用户最后一次选择
	 */
	private void showConfirmDialog1() {
		if (confirmUploadingDialog == null) {
			confirmUploadingDialog = new CustomDialog(this, R.style.myDialogTheme);
			confirmUploadingDialog.setContentText("您确认回收"+(Integer.parseInt(input_dhs_big)+Integer.parseInt(input_dhs_small))+"个桶吗？");
			confirmUploadingDialog.setToDoListener(new ToDoListener() {

				@Override
				public void doSomething() {
					confirmRecyle();		
					confirmUploadingDialog.dismiss();
				}
			});
		}
		confirmUploadingDialog.show();
	}
	
	
	/**
	 * 弹出对话框让用户最后一次选择
	 */
	private void showConfirmDialog() {
		if (confirmUploadingDialog == null) {
			confirmUploadingDialog = new CustomDialog(this, R.style.myDialogTheme);
			confirmUploadingDialog.setContentText("确认提交回收？");
			confirmUploadingDialog.setToDoListener(new ToDoListener() {

				@Override
				public void doSomething() {
					confirmRecyle();
					confirmUploadingDialog.dismiss();
				}
			});
		}
		confirmUploadingDialog.show();
	}

	/**
	 * 弹出确认退出对话框让用户选择
	 */
	private void showExitDialog() {
		if (exitDialog == null) {
			exitDialog = new CustomDialog(this, R.style.myDialogTheme);
			exitDialog.setContentText("是否确认退出回收界面？");
			exitDialog.setToDoListener(new ToDoListener() {

				@Override
				public void doSomething() {
					exitDialog.dismiss();
					finish();
				}
			});
		}
		exitDialog.show();
	}

	/**
	 * 弹出是否继续回收对话框
	 */
	private void showContinueRecyleDialog(final ArrayList<RecyleTempModel> recyleTempModels) {
		if (continueRecyleDialog == null) {
			continueRecyleDialog = new CustomDialog(this, R.style.myDialogTheme);
			continueRecyleDialog.setContentText("有上次未完成的回收，是否继续");
			continueRecyleDialog.setToDoListener(new ToDoListener() {

				@Override
				public void doSomething() {
					workId = recyleTempModels.get(0).getWorkID();
					for (int i = 0; i < recyleTempModels.size(); i++) {
						RecyleTempModel recyleTempModel = recyleTempModels.get(i);
						if (recyleTempModel.getWorkID().equals(workId)) {
							ScanBimpModel scanBimpModel = new ScanBimpModel();
							scanBimpModel.setBmpPath(recyleTempModel.getOriginal_Path() + "");
							scanBimpModel.setThumbPath(recyleTempModel.getThumb_Path() + "");
							scanBimpModel.setLabelCode(recyleTempModel.getLabels() + "");
							UploadCache.scanBimpModels.add(scanBimpModel);
						}
					}
					updateNum();
					continueRecyleDialog.dismiss();

				}
			});
			continueRecyleDialog.setCancelBtnListener(new CancelBtnListener() {

				@Override
				public void cancel() {
					// 清空
					recyleCancel(recyleTempModels.get(0).getWorkID());
					continueRecyleDialog.dismiss();
				}
			});
		}
		continueRecyleDialog.show();
	}

	/**
	 * 
	 * @Title: confirmRecyle
	 * @Description:确认回收
	 * @param 设定文件
	 * @return void 返回类型
	 * @throws
	 */
	private void confirmRecyle() {
	
			RecoveryConfirmAsyncTask recoveryConfirmAsyncTask = new RecoveryConfirmAsyncTask(RecyleActivity.this, workId, locationModel.getName(),
					locationModel.getAddress(), String.valueOf(locationModel.getLng()), String.valueOf(locationModel.getLat()), recyle_remark_txt.getText()
							.toString(), customerModel.getCustomerID(), customerModel.getCustomerName(), uploading_id,SWITCH_QR_CODE,dhs_small.getText().toString(),
							dhs_big.getText().toString(),dhs_small_remarktxt.getText().toString(),dhs_big_remarktxt.getText().toString(),qianshou_man_edit.getText().toString() ,qianshou_man_phone_edit.getText().toString(),WarehouseName,ysddh.getText().toString());
			recoveryConfirmAsyncTask.setRecoveryListener(new RecoveryListener() {

				@Override
				public void recoveryResult(ResultModel rm) {
					if (rm != null) {
						if (rm.getResult().equals("true")) {
							Tools.showToast(RecyleActivity.this, "回收成功");
							finish();
						} else {
							Tools.showToast(RecyleActivity.this, rm.getDescription());
						}
					} else {
						Tools.showToast(RecyleActivity.this, "回收失败，请检查网络");
					}
				}
			});
			recoveryConfirmAsyncTask.execute();
		
	}

	/*
	 * (非 Javadoc) <p>Title: onClick</p> <p>Description: </p>
	 * 
	 * @param arg0
	 * 
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 */
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.title_left_img:
			showExitDialog();
			break;
		case R.id.add_source_btn:
			if (UploadCache.scanBimpModels.size() == 0) {
				Intent addIntent = new Intent(this, AddRecyleSource.class);
				addIntent.putExtra("customerId", customerModel.getCustomerID());
				addIntent.putExtra("workId", workId);
				startActivity(addIntent);
			} else {
				Intent addIntent = new Intent(this, RecyleSourceGrid.class);
				addIntent.putExtra("customerId", customerModel.getCustomerID());
				addIntent.putExtra("workId", workId);
				startActivity(addIntent);
			}
			break;
		case R.id.recyle_btn:
			uploadRecyleImg();
			Pattern pattern = Pattern.compile("^[0-9]*$"); 
			Matcher matcher = pattern.matcher(dhs_small.getText().toString().trim());  
		
			Pattern pattern1 = Pattern.compile("^[0-9]*$");  
			Matcher matcher1 = pattern1.matcher(dhs_big.getText().toString().trim());  
			
			Pattern pattern2 = Pattern.compile("^[0-9]*$");  
			Matcher matcher2 = pattern2.matcher(qianshou_man_phone_edit.getText().toString().trim());  
			if("".equals(dhs_small.getText().toString().trim())){
				input_dhs_small="0";
			}else{
				input_dhs_small=dhs_small.getText().toString().trim();
			}
			if("".equals(dhs_big.getText().toString().trim())){
				input_dhs_big="0";
			}else{
				input_dhs_big=dhs_big.getText().toString().trim();
			}
			if (uploading_id == null) {
				Tools.showToast(this, "请上传回收单照片");
			}else if (this.locationModel.getLat()== null || this.locationModel.getLng() == null ||this.locationModel.getAddress().equals("")) {
				Tools.showToast(this, "位置获取失败，请稍后再试");
			}else if (("").equals(WarehouseName)||null==WarehouseName) {
				Tools.showToast(this, "请选择回收仓库");
			}else if (("").equals(ysddh.getText().toString().trim())) {
				Tools.showToast(this, "请填写原始订单号");
			}else if (("").equals(qianshou_man_edit.getText().toString().trim())||("").equals(qianshou_man_phone_edit.getText().toString().trim())) {
				Tools.showToast(this, "请完善签收人信息");
			}else if (("").equals(dhs_small.getText().toString().trim())&&("").equals(dhs_big.getText().toString().trim())) {
				Tools.showToast(this, "回收数量不能为空");
			}else if(!("").equals(dhs_small.getText().toString().trim())&&!matcher.matches()){	
						Tools.showToast(this, "小桶：请填写数字");			
			}/*else if(!dhs_small.getText().toString().trim().equals("")&&Integer.parseInt(dhs_small.getText().toString().trim())>Integer.parseInt(dhs_small_num)){
			
					Tools.showToast(this, "所填小桶回收数不能超出待回收数");
				
			}*/else if(!("").equals(dhs_big.getText().toString().trim())&&!matcher1.matches()) {
						Tools.showToast(this, "大桶：请填写数字");
			}/*else if(!dhs_big.getText().toString().trim().equals("")&&Integer.parseInt(dhs_big.getText().toString().trim())>Integer.parseInt(dhs_big_num)){
				
				Tools.showToast(this, "所填大桶回收数不能超出待回收数");		
		   }*/else if(!matcher2.matches()){
					Tools.showToast(this, "签收人电话：请填写数字");  
				
		   }else if(!("").equals(dhs_big.getText().toString().trim())&&(Integer.parseInt(input_dhs_big)+Integer.parseInt(input_dhs_small))>(Integer.parseInt(dhs_small_num)+Integer.parseInt(dhs_big_num))){
			   showConfirmDialog1();
				//Tools.showToast(this, "所填大桶回收数不能超出待回收数");		
		   }else{showConfirmDialog();}
			break;
		case R.id.recyle_img:
			Tools.takePhoto(this);
			break;
		case R.id.choose_warehouse:
			Intent intent= new Intent(RecyleActivity.this, ChooseWarehouseActivity.class);
			startActivityForResult(intent, ContactsUtils.CHOOSE_LABEL_RESULT);
			break;
		default:
			break;
		}
	}

	/*
	 * (非 Javadoc) <p>Title: getTopView</p> <p>Description: </p>
	 * 
	 * @return
	 * 
	 * @see com.resmanager.client.common.TopContainActivity#getTopView()
	 */
	@Override
	protected View getTopView() {
		View topView = inflater.inflate(R.layout.custom_title_bar, null);
		return topView;
	}

	/*
	 * (非 Javadoc) <p>Title: getCenterView</p> <p>Description: </p>
	 * 
	 * @return
	 * 
	 * @see com.resmanager.client.common.TopContainActivity#getCenterView()
	 */
	@Override
	protected View getCenterView() {
		return inflater.inflate(R.layout.recyle_layout, null);
	}
	/**
	 * // * // * @Title: uploadImg // * @Description: 上传图片 // * @param @param
	 * path 设定文件 // * @param type // * 1:二维码上传，2：遗失二维码 // * @return void 返回类型 //
	 * * @throws //
	 */
	public void uploadRecyleImg() {

		String recPId = Tools.getGUID();// 图片ID。用户删除时服务器对应
		String flagContent="";
		Bitmap tempbmp=null;
		RecoveryImageAsyncTask recoveryImageAsyncTask = new RecoveryImageAsyncTask(this, workId, flagContent, tempbmp, recPId);
		recoveryImageAsyncTask.setUploadRecyleResourceListener(new UploadRecyleResourceListener() {

			@Override
			public void uploadRecyleResult(ResultModel resultModel, Bitmap bmp, String flagContent, String RecPID) {
			/*	if (resultModel != null) {
					if (resultModel.getResult().equals("true")) {
						Tools.showToast(RecyleActivity.this, "旧桶添加成功");
					} else {
						Tools.showToast(RecyleActivity.this, resultModel.getDescription());
					}
				} else {
					Tools.showToast(RecyleActivity.this, "旧桶添加失败，请检查网络");
				}*/
			}
		});
		recoveryImageAsyncTask.execute();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		workId = Tools.getGUID();
/*		uploadRecyleImg();*/
		customerModel = (CustomerModel) getIntent().getSerializableExtra("customerModel");
		
		
		
		
		locationModel = new LocationModel();
		ImageView leftImg = (ImageView) topView.findViewById(R.id.title_left_img);
		leftImg.setOnClickListener(this);
		TextView titleContent = (TextView) topView.findViewById(R.id.title_content);
		titleContent.setText("回收");
		location_str_txt = (TextView) centerView.findViewById(R.id.location_str_txt);
		location_str_txt.setOnClickListener(this);
		customer_name = (TextView) centerView.findViewById(R.id.customer_name);
		customer_name.setText(customerModel.getCustomerName());
		ysddh=(EditText) centerView.findViewById(R.id.ysddh);
		dhs_small = (EditText) centerView.findViewById(R.id.dhs_small);	
		dhs_big = (EditText) centerView.findViewById(R.id.dhs_big);
		dhs_small_remarktxt = (EditText) centerView.findViewById(R.id.dhs_small_remarktxt);
		dhs_big_remarktxt = (EditText) centerView.findViewById(R.id.dhs_big_remarktxt);
		qianshou_man_edit = (EditText) centerView.findViewById(R.id.qianshou_man_edit);
		qianshou_man_phone_edit = (EditText) centerView.findViewById(R.id.qianshou_man_phone_edit);
		choose_warehouse=(TextView)centerView.findViewById(R.id.choose_warehouse);
		choose_warehouse.setOnClickListener(this);
		
	  GetRecyleNumberAsyncTask getRecyleNumberAsyncTask=new GetRecyleNumberAsyncTask(this, customerModel.getCustomerID());
		getRecyleNumberAsyncTask.setGetRecyleNumberListener(new GetRecyleNumberListener() {
			
			@Override
			public void getRecyleNumberResult(ResultModel rm) {
				
				if (rm != null) {
					if (rm.getResult().equals("true")) {
						    recyle_number=rm.getDescription();	
						    if(recyle_number!=null&&!("").equals(recyle_number)){
							String[] aa =recyle_number.split(",");
							dhs_big_num=aa[0];
							dhs_small_num=aa[1];
							dhs_big.setHint("待回收数"+aa[0]+"个");
							dhs_small.setHint("待回收数"+aa[1]+"个");
							}
					} else {
						Tools.showToast(RecyleActivity.this, rm.getDescription());
					}
				} else {
					Tools.showToast(RecyleActivity.this, "待回收数获取失败");
				}	
			}
		});
		getRecyleNumberAsyncTask.execute();
	
		
		
		add_source_btn = (TextView) centerView.findViewById(R.id.add_source_btn);
		   if (SWITCH_QR_CODE==1) {
			   add_source_btn.setVisibility(View.GONE);	
			}
		add_source_btn.setOnClickListener(this);
		centerView.findViewById(R.id.recyle_btn).setOnClickListener(this);
		recyle_img = (ImageView) centerView.findViewById(R.id.recyle_img);
		recyle_img.setOnClickListener(this);
		recyle_remark_txt = (EditText) centerView.findViewById(R.id.recyle_remark_txt);
		// if (ContactsUtils.baseAMapLocation != null && ContactsUtils.poiItem
		// != null) {
		// locationModel.setAddress(ContactsUtils.poiItem.getSnippet());
		// locationModel.setName(ContactsUtils.poiItem.getTitle());
		// locationModel.setLat(ContactsUtils.poiItem.getLatLonPoint().getLatitude());
		// locationModel.setLng(ContactsUtils.poiItem.getLatLonPoint().getLongitude());
		// location_str_txt.setTextColor(getResources().getColor(R.color.orange_color));
		// location_str_txt.setText(ContactsUtils.poiItem.getTitle());
		// }
		if (ContactsUtils.baseAMapLocation != null) {
			LocationUtils locationUtils = new LocationUtils(this);
			locationUtils.searchRound(this, ContactsUtils.baseAMapLocation.getLatitude(), ContactsUtils.baseAMapLocation.getLongitude(), 0,
					ContactsUtils.ORP_NONE, new PoiSearchListener() {
						@Override
						public void onPoiSearched(PoiResult poiResult, int resultCode, int orpType) {
							if (resultCode == 0) {
								List<PoiItem> pois = poiResult.getPois();
								Message msg = new Message();
								msg.what = 0;
								msg.obj = pois;
								mHandler.sendMessage(msg);
							}
						}
					});
		}
		recyleContinue();
	}

	private String[] split(String recyle_number2, char c) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * HANDLER
	 */
	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 1:
				Bitmap bm = (Bitmap) msg.obj;
				if (bm != null) {
					uploading_id = bm;
					recyle_img.setImageBitmap(bm);
				}
				break;
			}
			super.handleMessage(msg);
		}
	};


	/**
	 * 
	 * @Title: loading
	 * @Description: 异步加载图片
	 * @param @param path 设定文件
	 * @return void 返回类型
	 * @throws
	 */
	public void loading(final String path) {
		new Thread(new Runnable() {
			public void run() {
				try {
					Bitmap bm = Bimp.revitionImageSize(path);
					Message message = new Message();
					message.what = 1;
					message.obj = bm;
					handler.sendMessage(message);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}).start();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case ContactsUtils.TAKE_PHOTO_RESULT:
			if (data != null) {
				Bundle extras = data.getExtras();
				if (extras != null) {
					String path = extras.getString("image_path");
					loading(path);
				}
			}
			break;
		}
		switch (resultCode) {
		case ContactsUtils.CHOOSE_LOCATION_RESULT:
			if (data != null) {
				PoiItem poiInfo = (PoiItem) data.getParcelableExtra("poiInfo");
				locationModel.setAddress(poiInfo.getSnippet());
				locationModel.setName(poiInfo.getTitle());
				locationModel.setLat(poiInfo.getLatLonPoint().getLatitude());
				locationModel.setLng(poiInfo.getLatLonPoint().getLongitude());
				location_str_txt.setText(poiInfo.getTitle());
				location_str_txt.setTextColor(getResources().getColor(R.color.orange_color));
			}
			break;
		case ContactsUtils.CHOOSE_LABEL_RESULT:
			if (data != null) {
				WarehouseName = data.getExtras().getString("WarehouseName");
				choose_warehouse.setText(WarehouseName);
			}
			break;
		default:
			break;
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		UploadCache.resetBimp();
	}

	/**
	 * 
	 * @Title: recyleContinue
	 * @Description: 回收续传
	 * @param 设定文件
	 * @return void 返回类型
	 * @throws
	 */
	private void recyleContinue() {
		RecoveryContinueAsyncTask recoveryContinueAsyncTask = new RecoveryContinueAsyncTask(this, customerModel.getCustomerID());
		recoveryContinueAsyncTask.setRecyleContinueListener(new RecyleContinueListener() {

			@Override
			public void recyleContinueResult(RecyleTempListModel recyleTempListModel) {
				if (recyleTempListModel != null) {
					if (recyleTempListModel.getResult().equals("true")) {
						showContinueRecyleDialog(recyleTempListModel.getData());
					}
				}
			}
		});
		recoveryContinueAsyncTask.execute();
	}

	/**
	 * 
	 * @Title: recyleContinue
	 * @Description: 取消续传
	 * @param 设定文件
	 * @return void 返回类型
	 * @throws
	 */
	private void recyleCancel(String workid) {
		RecoveryCancelAsyncTask recoveryCancelAsyncTask = new RecoveryCancelAsyncTask(this, workid);
		recoveryCancelAsyncTask.execute();
	}

	@Override
	protected void onResume() {
		super.onResume();
		updateNum();
	}

	private void updateNum() {
		if (UploadCache.scanBimpModels.size() > 0) {
			add_source_btn.setTextColor(getResources().getColor(R.color.orange_color));
			add_source_btn.setText("已上传 " + UploadCache.scanBimpModels.size() + " 张照片");
		} else {
			add_source_btn.setTextColor(getResources().getColor(R.color.light_gray));
			add_source_btn.setText("点击添加货物信息");
		}
	}
}
