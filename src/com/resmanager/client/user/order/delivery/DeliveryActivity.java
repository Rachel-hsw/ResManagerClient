/**   
 * @Title: DeliveryActivity.java 
 * @Package com.resmanager.client.user.order 
 * @Description:发货
 * @author ShenYang  
 * @date 2015-12-6 下午4:45:53 
 * @version V1.0   
 */
package com.resmanager.client.user.order.delivery;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.amap.api.services.core.PoiItem;
import com.amap.api.services.poisearch.PoiResult;
import com.resmanager.client.R;
import com.resmanager.client.common.TopContainActivity;
import com.resmanager.client.main.photoalbum.Bimp;
import com.resmanager.client.map.GetLocationService;
import com.resmanager.client.model.GoodsListModel;
import com.resmanager.client.model.GoodsModel;
import com.resmanager.client.model.GoodsPackageQtyListModel;
import com.resmanager.client.model.GoodsPackageQtyModel;
import com.resmanager.client.model.LocationModel;
import com.resmanager.client.model.Order;
import com.resmanager.client.model.ResultModel;
import com.resmanager.client.model.ScanBimpModel;
import com.resmanager.client.model.TempScanBimpModel;
import com.resmanager.client.model.TempScanBimpModels;
import com.resmanager.client.order.OrderDetailActivity;
import com.resmanager.client.order.OrderListAdapter;
import com.resmanager.client.system.QueryConfigAsyncTask;
import com.resmanager.client.system.QueryConfigResult;
import com.resmanager.client.system.QueryConfigAsyncTask.GetqueryConfigListener;
import com.resmanager.client.user.order.ChooseLocationActivity;
import com.resmanager.client.user.order.UploadCache;
import com.resmanager.client.user.order.delivery.ConfirnDeliveryAsyncTask.DeliveryListener;
import com.resmanager.client.user.order.delivery.DeleteAllDeliveryPhotoAsyncTask.DelAllDeliveryListener;
import com.resmanager.client.user.order.delivery.DeliveryContinueAsyncTask.DeliveryContinueListener;
import com.resmanager.client.user.order.delivery.GetGoodsCountByOrderIDSAsyncTask.GetGoodsCountListener;
import com.resmanager.client.user.order.delivery.UploadImageAsyncTask.UploadResourceListener;
import com.resmanager.client.user.order.goods.GetGoodsByOrderIDAsyncTask;
import com.resmanager.client.user.order.goods.GetGoodsByOrderIDAsyncTask.GetGoodsByOrderIdListener;
import com.resmanager.client.utils.ContactsUtils;
import com.resmanager.client.utils.DESUtils;
import com.resmanager.client.utils.LocationUtils;
import com.resmanager.client.utils.LocationUtils.PoiSearchListener;
import com.resmanager.client.utils.Tools;
import com.resmanager.client.view.CustomDialog;
import com.resmanager.client.view.CustomDialog.CancelBtnListener;
import com.resmanager.client.view.CustomDialog.ToDoListener;
import com.resmanager.client.view.DefineListView;

/**
 * 发货
 * Author ShenYang
 * create at 2016/10/25 14:37
 */
@SuppressLint({ "InflateParams", "HandlerLeak" })
public class DeliveryActivity extends TopContainActivity implements OnClickListener {
	private Button add_source_btn, delivery_btn;
	private ArrayList<Order> orders;
	private DefineListView order_list, goods_package_count_list;
	private TextView location_str_txt;
	private LocationModel locationModel;
	private String workID = "";// 批次号，由客户端生成
	private StringBuffer orderBuffer; 
	private CustomDialog confirnDeliveryDialog, exitDialog, continueDialog;
	private EditText remark_txt;
	public static int NUM = 0;
	//小桶是否走添加货物流程。0为否，1是是
	private int Switch1;
	//是否使用手机每隔两秒定位上传
	private int Switch7;
	private int Switch8=0;
	private ArrayList<GoodsModel> goodsModels;
	private int ENTER=1;
	private int SWITCH_QR_CODE=1;//送货扫描二维码是否显示的开关,1是不显示
	private Bitmap uploading_id = null;
	private ImageView add_upload_id_img;
	public static Map<String, Integer> skuMap = new HashMap<String, Integer>();
	public static Map<String, Integer> selectSkuMap = new HashMap<String, Integer>();
	public static ArrayList<GoodsPackageQtyModel> data;
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
			//添加货物信息
			// 跳转至查看当前上传图片的界面
			if (NUM > 0) {
				if (UploadCache.scanBimpModels.size() > 0) {
					Intent deliveryIntent = new Intent(DeliveryActivity.this, DeliverySourceGrid.class);
					deliveryIntent.putExtra("workId", this.workID);
					deliveryIntent.putExtra("orderIds", this.orderBuffer.toString());
					startActivity(deliveryIntent);
				} else {
					Intent addIntent = new Intent(DeliveryActivity.this, AddSourceInfoActivity.class);
					addIntent.putExtra("workId", this.workID);
					addIntent.putExtra("orderIds", this.orderBuffer.toString());
					//hsw
					addIntent.putExtra("orders", orders);
					startActivity(addIntent);
				}
			} else {
				Tools.showToast(this, "货物数量为0，不能发货");
			}
			break;
		case R.id.location_str_txt:
			Intent chooseLocationIntent = new Intent(DeliveryActivity.this, ChooseLocationActivity.class);
			chooseLocationIntent.putExtra("current_location", location_str_txt.getText().toString());
			startActivityForResult(chooseLocationIntent, ContactsUtils.CHOOSE_LOCATION_RESULT);
			break;
		case R.id.delivery_btn:
		
			//必须在此处捕捉异常，否则添加货物信息为空的情况下程序会崩掉
			try{
			if (this.locationModel.getLat()== null || this.locationModel.getLng() == null ||this.locationModel.getAddress().equals("")) {
				Tools.showToast(this, "位置获取失败，请稍后再试");
			} else if (uploading_id == null) {
				Tools.showToast(this, "请上传发货单照片");
			}  else if (NUM != 0 && UploadCache.scanBimpModels.size() != NUM&&SWITCH_QR_CODE==2) {
				Tools.showToast(this, "实际发货数与订单货物数量不符");
			}else {
				showConfirmDialog();
			}
			}catch (Exception e) {
				e.printStackTrace();
			}
			break;
		case R.id.add_upload_id_img:
			Tools.takePhoto(this);
			break;
		default:
			break;
		}
	}

	private Handler mHandler = new Handler() {
		@SuppressWarnings("unchecked")
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
				List<PoiItem> pois = (List<PoiItem>) msg.obj;
				if (pois.size() > 0) {
					PoiItem poiItem = pois.get(0);
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


	/**
	 * 弹出对话框让用户最后一次选择
	 */
	private void showContinueDialog(final ArrayList<TempScanBimpModel> data, String orderids) {
		if (continueDialog == null) {
			continueDialog = new CustomDialog(this, R.style.myDialogTheme);
			String[] ids = orderids.split(",");
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < ids.length; i++) {
				sb.append(ids[i] + "<br>");
			}
			String noticestr = "该订单您之前上传过发货图片数据，<br>相关订单号:<br>" + sb.toString() + "是否继续上传？";
			continueDialog.setContentText(noticestr, false);
			continueDialog.setOkCancelBtnText("继续", "清空");
			continueDialog.setToDoListener(new ToDoListener() {

				@Override
				public void doSomething() {
					for (TempScanBimpModel tempScanBimpModel : data) {
						ScanBimpModel scanBimpModel = new ScanBimpModel();
						scanBimpModel.setLabelCode(DESUtils.encrypt(tempScanBimpModel.getLabelCode()));
						scanBimpModel.setBmpPath(tempScanBimpModel.getOriginal_Path());
						scanBimpModel.setThumbPath(tempScanBimpModel.getThumb_Path());
						scanBimpModel.setPackageType(tempScanBimpModel.getPackagetype());
						scanBimpModel.setResourceTypeName(tempScanBimpModel.getGoodsName());
						UploadCache.scanBimpModels.add(scanBimpModel);
						add_source_btn.setTextColor(getResources().getColor(R.color.orange_color));
						add_source_btn.setText("点击查看，已添加" + UploadCache.scanBimpModels.size() + "/" + NUM);
					}
				}
			});
			continueDialog.setCancelBtnListener(new CancelBtnListener() {

				@Override
				public void cancel() {
					deleteAllPic();
				}
			});
		}
		continueDialog.show();
	}

	/**
	 * 弹出对话框让用户最后一次选择
	 */
	private void showConfirmDialog() {
		if (confirnDeliveryDialog == null) {
			confirnDeliveryDialog = new CustomDialog(this, R.style.myDialogTheme);
			confirnDeliveryDialog.setContentText("确认开始送货？");
			confirnDeliveryDialog.setToDoListener(new ToDoListener() {

				@Override
				public void doSomething() {
					confirnDelivery();
				}
			});
		}
		confirnDeliveryDialog.show();
	}

	/**
	 * 弹出确认退出对话框让用户选择
	 */
	private void showExitDialog() {
		if (exitDialog == null) {
			exitDialog = new CustomDialog(this, R.style.myDialogTheme);
			exitDialog.setContentText("是否确认退出发货界面？");
			exitDialog.setToDoListener(new ToDoListener() {

				@Override
				public void doSomething() {
					// deleteAllPic();
					finish();
				}
			});
		}
		exitDialog.show();
	}

	/**
	 * 
	 * @Title: deleteAllPic
	 * @Description: 如果未发货退出的话，则删除所有的图片
	 * @param 设定文件
	 * @return void 返回类型
	 * @throws
	 */
	private void deleteAllPic() {
		DeleteAllDeliveryPhotoAsyncTask deleteAllDeliveryPhotoAsyncTask = new DeleteAllDeliveryPhotoAsyncTask(this, workID);
		deleteAllDeliveryPhotoAsyncTask.setDelAllDeliveryListener(new DelAllDeliveryListener() {

			@Override
			public void delAllDeliveryResult(ResultModel resultModel) {
				Tools.showToast(DeliveryActivity.this, "已清空");
			}
		});
		deleteAllDeliveryPhotoAsyncTask.execute();
	}

	/**
	 * 
	 * @Title: deliveryContinue
	 * @Description: 发货续传
	 * @param 设定文件
	 * @return void 返回类型
	 * @throws
	 */
	private void deliveryContinue() {
		DeliveryContinueAsyncTask deliveryContinueAsyncTask = new DeliveryContinueAsyncTask(this.orderBuffer.toString());
		deliveryContinueAsyncTask.setDeliveryContinueListener(new DeliveryContinueListener() {

			@Override
			public void deliveryContinueResult(TempScanBimpModels deliveryScanListModel) {
				if (deliveryScanListModel != null) {
					if (deliveryScanListModel.getResult().equals("true")) {
						if (deliveryScanListModel.getData() != null && deliveryScanListModel.getData().size() > 0) {
							//待调试
							workID = deliveryScanListModel.getData().get(0).getWorkID();
							showContinueDialog(deliveryScanListModel.getData(), deliveryScanListModel.getDescription());
						}
					}
				}
			}
		});
		deliveryContinueAsyncTask.execute();
	}
	
	/**
	 * 
	 * @Title: uploadImg
	 * @Description: 上传图片
	 * @param @param path 设定文件
	 * @return void 返回类型
	 * @throws
	 */
	public void uploadImg(Bitmap bmp, String flagContent, final String goodsId, int isRecyle) {
		int resoureceType = 0;// 0:油桶，1油罐
		Log.i("image", "dayin"+"1111111111111111111");
		UploadImageAsyncTask uploadImageAsyncTask = new UploadImageAsyncTask(this, bmp, flagContent, workID, goodsId, isRecyle, this.orderBuffer.toString(),
				this.locationModel.getName(), this.locationModel.getAddress(), String.valueOf(this.locationModel.getLng()), String.valueOf(this.locationModel
						.getLat()), resoureceType);
		
	
		uploadImageAsyncTask.setUploadResourceListener(new UploadResourceListener() {

			@Override
			public void uploadResult(ResultModel resultModel, Bitmap bmp, String flagContent) {
				if (resultModel != null) {	Log.i("image", "dayin"+"1222222");
					if (resultModel.getResult().equals("true")) {
						Tools.showToast(DeliveryActivity.this, resultModel.getDescription());
						
					} else {
						//库中未检索到此标签
						Tools.showToast(DeliveryActivity.this, resultModel.getDescription());
					}
				} else {
					Tools.showToast(DeliveryActivity.this, "货物添加失败，请检查网络");
				}
			}
		});
		uploadImageAsyncTask.execute();
	}

	/**
	 * 
	 * @Title: confirnDelivery
	 * @Description: 确认发货
	 * @param 设定文件
	 * @return void 返回类型
	 * @throws
	 */
	public void confirnDelivery() {
		try{
		 long time=System.currentTimeMillis();//long now = android.os.SystemClock.uptimeMillis();  
	        SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");  
	        Date d1=new Date(time);  
	        String t1=format.format(d1);  
	        
	  		  StringBuffer goodsId = new StringBuffer();
	  		  if (data!=null) {
	  			for (int i = 0; i < data.size(); i++) {
	  				if (i + 1 == data.size()) {
	  					goodsId.append(data.get(i).getGoodsnameID());
	  				} else {
	  					goodsId.append(data.get(i).getGoodsnameID() + ",");
	  				}

	  			}
			}
	  			
if (SWITCH_QR_CODE==1) {
	uploadImg(uploading_id,"",goodsId.toString(),0);
}
	 	
		ConfirnDeliveryAsyncTask confirnDeliveryAsyncTask = new ConfirnDeliveryAsyncTask(this, workID, orderBuffer.toString(), this.locationModel.getName(),
				this.locationModel.getAddress(), String.valueOf(this.locationModel.getLng()), String.valueOf(this.locationModel.getLat()), remark_txt.getText()
						.toString().trim(),uploading_id,SWITCH_QR_CODE,t1);
		confirnDeliveryAsyncTask.setDeliveryListener(new DeliveryListener() {

			@Override
			public void deliveryResult(ResultModel rm) {
				if (rm != null) {
					if (rm.getResult().equals("true")) {
						Tools.showToast(DeliveryActivity.this, rm.getDescription());
						/*//这个标志没用上
						if(Switch8==1){	ContactsUtils.ISUPLOAD_LOC = true;// 开始上传位置
					
						if (ContactsUtils.USER_KEY != null && !ContactsUtils.USER_KEY.equals("")) {
							Intent service = new Intent(DeliveryActivity.this, GetLocationService.class);
							startService(service);
						}}*/
						finish();
					} else {
						Tools.showToast(DeliveryActivity.this, rm.getDescription());
					}
				} else {
					Tools.showToast(DeliveryActivity.this, "发货失败，请检查网络");
				}
			}
		});
		confirnDeliveryAsyncTask.execute();

	}catch(Exception e){
		e.printStackTrace();
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
		View contentView = inflater.inflate(R.layout.delivery_new, null);
		return contentView;
	}
	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		DeliveryActivity.NUM = 0; // 数量清空
		orders = (ArrayList<Order>) getIntent().getExtras().getSerializable("orders");
		topView.findViewById(R.id.title_left_img).setOnClickListener(this);
		TextView titleContent = (TextView) topView.findViewById(R.id.title_content);
		titleContent.setText("发货");
		//待调试
		workID = Tools.getGUID();
		locationModel = new LocationModel();
		orderBuffer = new StringBuffer();
		for (int i = 0; i < orders.size(); i++) {
			NUM += orders.get(i).getQuantity();
			if (i + 1 == orders.size()) {
				orderBuffer.append(orders.get(i).getOrderID());
			} else {
				orderBuffer.append(orders.get(i).getOrderID() + ",");
			}

		}

		delivery_btn = (Button) centerView.findViewById(R.id.delivery_btn);
		delivery_btn.setOnClickListener(this);
		add_source_btn = (Button) centerView.findViewById(R.id.add_source_btn);
		  if (SWITCH_QR_CODE==1) {
			  add_source_btn.setVisibility(View.GONE);	
			}
		add_source_btn.setOnClickListener(this);
		add_upload_id_img = (ImageView) centerView.findViewById(R.id.add_upload_id_img);
		add_upload_id_img.setOnClickListener(this);
		order_list = (DefineListView) centerView.findViewById(R.id.order_list);
		goods_package_count_list = (DefineListView) centerView.findViewById(R.id.goods_package_count_list);
		OrderListAdapter orderListAdapter = new OrderListAdapter(this, orders, false);
		order_list.setAdapter(orderListAdapter);
		order_list.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> v, View arg1, int position, long arg3) {
				Order order = (Order) v.getAdapter().getItem(position);
				Intent intent = new Intent(DeliveryActivity.this, OrderDetailActivity.class);
				intent.putExtra("orderId", order.getOrderID());
				startActivity(intent);
			}
		});
		location_str_txt = (TextView) centerView.findViewById(R.id.location_str_txt);
		//如果 定位数据不为空
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
		location_str_txt.setOnClickListener(this);
		remark_txt = (EditText) centerView.findViewById(R.id.remark_txt);
		deliveryContinue();
		getGoodsCount();
	//是否使用手机定位
			QueryConfigAsyncTask queryConfigAsyncTask7 = new QueryConfigAsyncTask(this, 7);
			queryConfigAsyncTask7.setqueryConfigListener(new GetqueryConfigListener() {
				@Override
				public void getqueryConfigResult(QueryConfigResult queryConfigResult) {
					// TODO Auto-generated method stub
					if (queryConfigResult != null) {
						if (queryConfigResult.getResult().equals("true")) {
		//{"data":[{"id":"3","name":"是否验证流水线标签","state":"1"}],"result":"true","description":"读取成功","UserKey":null}
							Switch7=queryConfigResult.getData().get(0).getState();
							}else {
							Tools.showToast(DeliveryActivity.this,queryConfigResult.getDescription());
						}
					} else {
						Tools.showToast(DeliveryActivity.this, "样式获取失败，请检查网络");
					}
				}
				
		
			});
			queryConfigAsyncTask7.execute();
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
		default:
			break;
		}
	}

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
				String s=Tools.getImageByte(uploading_id).toString();
				add_upload_id_img.setImageBitmap(bm);
			}
			break;
		}
		super.handleMessage(msg);
	}
};
	/**
	 * 105&116&114&103&72&75&75&75&78&76&76&72&75&75&
	 * 
	 * @Title: getGoodsCount
	 * @Description:获取货物个数
	 * @param 设定文件
	 * @return void 返回类型
	 * @throws
	 */
	private void getGoodsCount() {
		GetGoodsCountByOrderIDSAsyncTask getGoodsCountByOrderIDSAsyncTask = new GetGoodsCountByOrderIDSAsyncTask(this, orderBuffer.toString());
		getGoodsCountByOrderIDSAsyncTask.setGetGoodsCountListener(new GetGoodsCountListener() {

			@Override
			public void getGoodsCountResult(GoodsPackageQtyListModel goodsPackageQtyListModel) {
				if (goodsPackageQtyListModel != null) {
					if (goodsPackageQtyListModel.getResult().equals("true")) {
						data = goodsPackageQtyListModel.getData();
						for (int i = 0; i < data.size(); i++) {
							GoodsPackageQtyModel goodsPackageQtyModel = data.get(i);
							skuMap.put(goodsPackageQtyModel.getGoodsnameID(), goodsPackageQtyModel.getQuantity());
							selectSkuMap.put(goodsPackageQtyModel.getGoodsnameID(), 0);
							if (goodsPackageQtyModel.getPackagetype().equals("油罐")) {
								 add_source_btn.setVisibility(View.VISIBLE);	
								 SWITCH_QR_CODE=2;
							}
						}
						GoodsPkgCountListAdapter goodsPkgCountListAdapter = new GoodsPkgCountListAdapter(DeliveryActivity.this, data);
						goods_package_count_list.setAdapter(goodsPkgCountListAdapter);
					} else {
						Tools.showToast(DeliveryActivity.this, goodsPackageQtyListModel.getDescription());
					}
				} else {
					Tools.showToast(DeliveryActivity.this, "发货明细获取失败，请检查网络");
				}
			}
		});
		getGoodsCountByOrderIDSAsyncTask.execute();
	}

	@Override
	public void onBackPressed() {
		showExitDialog();
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (NUM != 0) {
			if (UploadCache.scanBimpModels.size() > 0) {
				add_source_btn.setTextColor(getResources().getColor(R.color.orange_color));
				add_source_btn.setText("点击查看，已添加" + UploadCache.scanBimpModels.size() + "/" + NUM);
			} else {
				add_source_btn.setTextColor(getResources().getColor(R.color.middle_gray));
				add_source_btn.setText("添加货物信息");
			}
		}
	}
	

	@Override
	protected void onDestroy() {
		super.onDestroy();
		UploadCache.resetBimp();// 重置缓存
		
	}

}
