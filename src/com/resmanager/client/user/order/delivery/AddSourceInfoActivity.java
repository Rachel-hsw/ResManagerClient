/**   
 * @Title: AddSourceInfoActivity.java 
 * @Package com.resmanager.client.user.order.delivery 
 * @Description: 添加货物明细
 * @author ShenYang   
 * @date 2015-12-22 下午3:52:38 
 * @version V1.0   
 */
package com.resmanager.client.user.order.delivery;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.opengl.Visibility;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;

import com.amap.api.services.core.PoiItem;
import com.amap.api.services.poisearch.PoiResult;
import com.resmanager.client.R;
import com.resmanager.client.camera.TakePhoteActivity;
import com.resmanager.client.camera.UploadCameraImgAsyncTask;
import com.resmanager.client.camera.UploadCameraImgAsyncTask.UploadPicListner;
import com.resmanager.client.common.TopContainActivity;
import com.resmanager.client.main.photoalbum.Bimp;
import com.resmanager.client.model.GoodsListModel;
import com.resmanager.client.model.GoodsModel;
import com.resmanager.client.model.LocationModel;
import com.resmanager.client.model.Order;
import com.resmanager.client.model.RecylePicModel;
import com.resmanager.client.model.ResultModel;
import com.resmanager.client.model.ScanBimpModel;
import com.resmanager.client.scan.CatpureActivity;
import com.resmanager.client.system.QueryConfigAsyncTask;
import com.resmanager.client.system.QueryConfigAsyncTask.GetqueryConfigListener;
import com.resmanager.client.system.QueryConfigResult;
import com.resmanager.client.user.balance.BalanceRecyleSourceGrid;
import com.resmanager.client.user.balance.BalanceSourcePicGridAdapter;
import com.resmanager.client.user.order.UploadCache;
import com.resmanager.client.user.order.delivery.QueryLablecodeAsyncTask.QueryLablecodeListener;
import com.resmanager.client.user.order.delivery.UploadImageAsyncTask.UploadResourceListener;
import com.resmanager.client.user.order.delivery.UploadImagePathAsyncTask.UploadResourcePathListener;
import com.resmanager.client.user.order.goods.GetGoodsByOrderIDAsyncTask;
import com.resmanager.client.user.order.goods.GoodsListActivity;
import com.resmanager.client.user.order.goods.GetGoodsByOrderIDAsyncTask.GetGoodsByOrderIdListener;
import com.resmanager.client.utils.ContactsUtils;
import com.resmanager.client.utils.DESUtils;
import com.resmanager.client.utils.FileUtils;
import com.resmanager.client.utils.LocationUtils;
import com.resmanager.client.utils.Tools;
import com.resmanager.client.utils.LocationUtils.PoiSearchListener;
import com.resmanager.client.view.CustomDialog;
import com.resmanager.client.view.CustomDialog.ToDoListener;
import com.squareup.picasso.Picasso;

/**
 * @ClassName: AddSourceInfoActivity
 * @Description: 添加货物明细
 * @author ShenYang
 * @date 2015-12-22 下午3:52:38
 * 
 */
@SuppressLint({ "InflateParams", "HandlerLeak" })
public class AddSourceInfoActivity extends TopContainActivity implements OnClickListener {
	private ImageView resource_img;// 货物图片
	private RadioGroup recyle_isneed_rb;// 是否需要回收
	/*private RadioButton need_rb, noneed_rb;*/
	private TextView scan_flag_txt, choose_resource_type_txt;
	private Button uploading_btn;
	private ScanBimpModel scanBimpModel = new ScanBimpModel();// 扫描类
	private CustomDialog noticeDialog;
	// 是否需要回收0否，1是，默认需要回收
	private String workId;
	private String orderIds;
	private LocationModel locationModel = null;
	private TextView click_to_take;
	//需要回收-隐藏单选按钮
	private int FLAG=1;
	//流水线是否显示照片
	private int Switch6;
	//判断是小桶、大桶或者油罐de标记
	private int P_ID;
	//判断小桶、大桶或者油罐是否需要拍照标记
	private int TP_P_ID1;
	private int TP_P_ID2;
	private int TP_P_ID3;
	private int resoureceType = 0;// 0:不拍照，新流程 发货出库，1拍照 旧流程 送货 
	private int SWITCH_QR_CODE=1;//送货扫描二维码是否显示的开关,1是不显示

	private ArrayList<Order> orders;
	// private LinearLayout take_img_layout;
	private ArrayList<GoodsModel> goodsModels;
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
				}
				break;

			default:
				break;
			}
		};
	};

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.title_left_img:
			this.finish();
			break;
		case R.id.resource_img:
			Tools.takePhoto(this);
			break;
		case R.id.uploading_btn:
			//点击确认按钮
			checkUploading();// 检查上传
			break;
		case R.id.choose_resource_type_txt:
			if (goodsModels != null && goodsModels.size() > 0) {
				Intent chooseGoodsIntent = new Intent(AddSourceInfoActivity.this, GoodsListActivity.class);
				chooseGoodsIntent.putExtra("goodsModels", goodsModels);
				startActivityForResult(chooseGoodsIntent, ContactsUtils.CHOOSE_GOODS_RESULT);
			} else {
				Tools.showToast(this, "暂无货物规格信息");
			}
			break;
		case R.id.scan_flag_txt:
			//
		/*	Intent scanIntent = new Intent(this, CatpureActivity.class);
			scanIntent.putExtra("flagType", -1);// 没有用到
			startActivityForResult(scanIntent, ContactsUtils.SCAN_RESULT);*/
			Intent scanIntent = new Intent(this, DeliverQingDanActivity.class);
			scanIntent.putExtra("orders", orders);
			scanIntent.putExtra("orderIds", orderIds);
			startActivityForResult(scanIntent, ContactsUtils.SCAN_RESULT);
		/*	startActivity(scanIntent);*/
			break;
		default:
			break;
		}
	}

	/**
	 * 
	 * @Title: checkUploading
	 * @Description: 检查桶信息
	 * @param 设定文件
	 * @return void 返回类型
	 * @throws
	 */
	private void checkUploading() {
		String noticeStr = "";
		if ((scanBimpModel.getBmp() == null || (scanBimpModel.getBmpPath().equals("")))&&FLAG!=0) {
			noticeStr = "请添加货物图片";
		} else if (scanBimpModel.getLabelCode().equals("")&&SWITCH_QR_CODE!=1) {
			noticeStr = "请添加货物二维码";
		} else if (scanBimpModel.getResourceTypeId().equals("")) {
			noticeStr = "请选择货物规格";
		} else {
			/*scanBimpModel.setRecyle(isneedrecyle);// 设置是否需要回收
*/			if (noticeDialog == null) {
				noticeDialog = new CustomDialog(this, R.style.myDialogTheme);
				noticeDialog.setContentText("请确认货物信息是否填写完毕");
				noticeDialog.setToDoListener(new ToDoListener() {

					@Override
					public void doSomething() {
						noticeDialog.dismiss();
						// 上传
						/*if(FLAG==0){
							Log.i("-----------", "hsw51");
							uploadImg1(scanBimpModel.getRelative_path(), DESUtils.decrypt(scanBimpModel.getLabelCode()), scanBimpModel.getResourceTypeId(),
									scanBimpModel.getIsRecyle());
						}else{*/
						uploadImg(scanBimpModel.getBmp(), /*DESUtils.decrypt(scanBimpModel.getLabelCode())*/scanBimpModel.getLabelCode(), scanBimpModel.getResourceTypeId(),
								scanBimpModel.getIsRecyle());
						Log.i("hswscanBimpModel.getLabelCode()", "hsw"+scanBimpModel.getLabelCode());
						Log.i("hswscanBimpModel.getResourceTypeId()", "hsw"+scanBimpModel.getResourceTypeId());
					}
				});
			}
			noticeDialog.show();
		}
		if (!noticeStr.equals("")) {
			Tools.showToast(this, noticeStr);
		}
	}

	/*
	 * (非 Javadoc) <p>Title: getTopView</p> <p>Description: </p>
	 * 
	 * @return
	 * 
	 * @see com.resmanager.client.common.TopContainActivity#getTopView()
	 */
	@SuppressLint("InflateParams")
	@Override
	protected View getTopView() {
		return inflater.inflate(R.layout.custom_title_bar, null);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		locationModel = new LocationModel();
		//hsw
		orders = (ArrayList<Order>) getIntent().getExtras().getSerializable("orders");
		orderIds = getIntent().getExtras().getString("orderIds");
		workId = getIntent().getExtras().getString("workId");// 批次号
		ImageView leftImg = (ImageView) topView.findViewById(R.id.title_left_img);
		leftImg.setOnClickListener(this);
		TextView titleContent = (TextView) topView.findViewById(R.id.title_content);
		titleContent.setText("添加货物");
		uploading_btn = (Button) centerView.findViewById(R.id.uploading_btn);
		resource_img = (ImageView) centerView.findViewById(R.id.resource_img);
	//	recyle_isneed_rb = (RadioGroup) centerView.findViewById(R.id.recyle_isneed_rb);
	
		scan_flag_txt = (TextView) centerView.findViewById(R.id.scan_flag_txt);
	/*    if (SWITCH_QR_CODE==1) {
	    	scan_flag_txt.setVisibility(View.GONE);	
		}*/
	//	need_rb = (RadioButton) centerView.findViewById(R.id.need_rb);
	//	noneed_rb = (RadioButton) centerView.findViewById(R.id.noneed_rb);
		scan_flag_txt.setOnClickListener(this);
		choose_resource_type_txt = (TextView) centerView.findViewById(R.id.choose_resource_type_txt);
		choose_resource_type_txt.setOnClickListener(this);
		uploading_btn = (Button) centerView.findViewById(R.id.uploading_btn);
		click_to_take = (TextView) centerView.findViewById(R.id.click_to_take);
		uploading_btn.setOnClickListener(this);
		resource_img.setOnClickListener(this);
		getGoodsByOrderIds();
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
	////流水线是否显示照片
		QueryConfigAsyncTask queryConfigAsyncTask6 = new QueryConfigAsyncTask(this, 6);
		queryConfigAsyncTask6.setqueryConfigListener(new GetqueryConfigListener() {
			
		

			@Override
			public void getqueryConfigResult(QueryConfigResult queryConfigResult) {
				// TODO Auto-generated method stub
				if (queryConfigResult != null) {
					if (queryConfigResult.getResult().equals("true")) {
	//{"data":[{"id":"3","name":"是否验证流水线标签","state":"1"}],"result":"true","description":"读取成功","UserKey":null}
						Switch6=queryConfigResult.getData().get(0).getState();
						}else {
						Tools.showToast(AddSourceInfoActivity.this,queryConfigResult.getDescription());
					}
				} else {
					Tools.showToast(AddSourceInfoActivity.this, "样式获取失败，请检查网络");
				}
			}
			
	
		});
		queryConfigAsyncTask6.execute();
		//小桶是否拍照 0否，1是
		QueryConfigAsyncTask queryConfigAsyncTask1 = new QueryConfigAsyncTask(this, 1);
		queryConfigAsyncTask1.setqueryConfigListener(new GetqueryConfigListener() {
			@Override
			public void getqueryConfigResult(QueryConfigResult queryConfigResult) {
				// TODO Auto-generated method stub
				if (queryConfigResult != null) {
					if (queryConfigResult.getResult().equals("true")) {
	//{"data":[{"id":"3","name":"小桶是否走添加货物流程","state":"0"}],"result":"true","description":"读取成功","UserKey":null}
						TP_P_ID1=queryConfigResult.getData().get(0).getState();
					Log.i("","hsw"+TP_P_ID1);
						}else {
						Tools.showToast(AddSourceInfoActivity.this,queryConfigResult.getDescription());
					}
				} else {
					Tools.showToast(AddSourceInfoActivity.this, "样式获取失败，请检查网络");
				}
			}
			
	
		});
		queryConfigAsyncTask1.execute();
		//大桶是否拍照 0否，1是
		QueryConfigAsyncTask queryConfigAsyncTask2 = new QueryConfigAsyncTask(this, 2);
		queryConfigAsyncTask2.setqueryConfigListener(new GetqueryConfigListener() {
			@Override
			public void getqueryConfigResult(QueryConfigResult queryConfigResult) {
				// TODO Auto-generated method stub
				if (queryConfigResult != null) {
					if (queryConfigResult.getResult().equals("true")) {
	//{"data":[{"id":"3","name":"小桶是否走添加货物流程","state":"0"}],"result":"true","description":"读取成功","UserKey":null}
						TP_P_ID2=queryConfigResult.getData().get(0).getState();
					Log.i("","hsw"+TP_P_ID2);
						}else {
						Tools.showToast(AddSourceInfoActivity.this,queryConfigResult.getDescription());
					}
				} else {
					Tools.showToast(AddSourceInfoActivity.this, "样式获取失败，请检查网络");
				}
			}
			
	
		});
		queryConfigAsyncTask2.execute();
		//油罐是否拍照 0否，1是
		QueryConfigAsyncTask queryConfigAsyncTask3 = new QueryConfigAsyncTask(this, 3);
		queryConfigAsyncTask3.setqueryConfigListener(new GetqueryConfigListener() {
			@Override
			public void getqueryConfigResult(QueryConfigResult queryConfigResult) {
				// TODO Auto-generated method stub
				if (queryConfigResult != null) {
					if (queryConfigResult.getResult().equals("true")) {
	//{"data":[{"id":"3","name":"小桶是否走添加货物流程","state":"0"}],"result":"true","description":"读取成功","UserKey":null}
						TP_P_ID3=queryConfigResult.getData().get(0).getState();
					Log.i("","hsw"+TP_P_ID3);
						}else {
						Tools.showToast(AddSourceInfoActivity.this,queryConfigResult.getDescription());
					}
				} else {
					Tools.showToast(AddSourceInfoActivity.this, "样式获取失败，请检查网络");
				}
			}
			
	
		});
		queryConfigAsyncTask3.execute();
	
	}

	@Override
	protected View getCenterView() {
		return inflater.inflate(R.layout.scan_info_upload, null);
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

		case ContactsUtils.CHOOSE_GOODS_RESULT:
			if (data != null) {
				GoodsModel goodsModel = (GoodsModel) data.getExtras().getSerializable("goodsModel");
				scanBimpModel.setResourceTypeId(goodsModel.getGoodsnameID());
				scanBimpModel.setResourceTypeName(goodsModel.getGoodsName());
				scanBimpModel.setPackageType(goodsModel.getPackagetype());
				choose_resource_type_txt.setText(goodsModel.getGoodsName());
				choose_resource_type_txt.setTextColor(getResources().getColor(R.color.red));
			}
			break;
		case ContactsUtils.SCAN_RESULT:	
			if (data != null) {
				String flagContent = data.getStringExtra("result");// 加密后的二维码内容，解密后传递给接口进行校验
				P_ID= data.getIntExtra("P_ID", 7);
				Log.i("hswpid", "hsw"+P_ID);
			    scan_flag_txt.setTextColor(getResources().getColor(R.color.middle_gray));
				                try {
					              scan_flag_txt.setText(Tools.getShowLabelCode(flagContent));
					                scan_flag_txt.setTextColor(getResources().getColor(R.color.red));              
					                flagContent=DESUtils.decrypt(flagContent);//此处传过去的labelcode为NYWL-000798-00模式
					                //Tools.getShowLabelCode(flagContent);//此处传过去的labelcode为000798模式
					                Log.i("---hsw1--","hsw+flagContent:"+flagContent);	  
					                scanBimpModel.setLabelCode(flagContent);
					            	if(P_ID==3){
					            		//油罐
					            		if(TP_P_ID3==1){//默认值1
					            			//拍照
					            			// 0:不拍照，新流程 发货出库，1拍照 旧流程 送货 0200
					            			resoureceType = 1;
					            		}else{
					            			//不拍，库中查询
					            			resoureceType = 0;
					            			queryLableCode(flagContent);
					            		}
					            	}else if(P_ID==2){
					            		//大桶---------------------------------------------------------
                                       if(TP_P_ID2==1){	
                                    	 //拍照
                                    	   resoureceType = 1;
					            		}else{
					            			//不拍，库中查询
					            			resoureceType = 0;
					            			queryLableCode(flagContent);
					            		}
					            	}else{//小桶
                                       if(TP_P_ID1==1){	//默认值1
                                    	 //拍照
                                    	   resoureceType = 1; 
					            		}else{
					            			//不拍，库中查询
					            			resoureceType = 0;
					            			queryLableCode(flagContent);
					            		}
					            		
					            	}
					
				} catch (Exception e) {
					Tools.showToast(this, "非法的二维码标签");
					e.printStackTrace();
				}
						}
				
			
			break;

		}
	}

	private void queryLableCode(String flagContent ) {
		// TODO Auto-generated method stub
		QueryLablecodeAsyncTask queryLablecodeAsyncTask = new QueryLablecodeAsyncTask(this,flagContent );
		queryLablecodeAsyncTask.setQueryLablecodeListener(new QueryLablecodeListener() {
			
			@Override
			public void QueryLablecodeResult(DeliveryPicListModel deliveryPicListModel) {
				if (deliveryPicListModel != null) {
					if (deliveryPicListModel.getResult().equals("true")) {	
						Tools.showToast(AddSourceInfoActivity.this, "检索到该标签");
						ArrayList<DeliveryPicModel> deliverPicModels = deliveryPicListModel.getData();
						DeliveryPicModel deliverPicModel = deliverPicModels.get(0);
						//图片路径
						String url=deliverPicModel.getOriginal_Path()+deliverPicModel.getPL_Fname()+".jpg";
						/*//虚假数据
						String url=String.valueOf(deliverPicModel.getOriginal_Path()+"20160920-06-1-0001-182501-0003-000203.jpg");*/
						if(Switch6==1){
							//查询到了显示图片
						Picasso.with(AddSourceInfoActivity.this).load(url).placeholder(R.drawable.default_img).error(R.drawable.default_img).into(resource_img);
					    resource_img.setClickable(false);
					    }
						else{
					    resource_img.setVisibility(View.GONE);
					    }
						
					    FLAG=0;
						/*	scanBimpModel.setRelative_path(url);*/
					} else {
						//未读取到数据
						Tools.showToast(AddSourceInfoActivity.this, deliveryPicListModel.getDescription());
						resource_img.setClickable(false);
					}
				} else {
					Tools.showToast(AddSourceInfoActivity.this, "上传失败，请检查网络");
				}
			}
		});
	queryLablecodeAsyncTask.execute();
	}

	/**
	 * HANDLER
	 */
	Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 1:
				Bitmap bm = (Bitmap) msg.obj;
				if (bm != null) {

					resource_img.setImageBitmap(bm);
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
					scanBimpModel.setBmp(bm);
					scanBimpModel.setBmpPath(path);
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
	 * 
	 * @Title: uploadImg
	 * @Description: 上传图片
	 * @param @param path 设定文件
	 * @return void 返回类型
	 * @throws
	 */
	public void uploadImg(Bitmap bmp, String flagContent, final String goodsId, int isRecyle) {
		
		
		UploadImageAsyncTask uploadImageAsyncTask = new UploadImageAsyncTask(this, bmp, flagContent, workId, goodsId, isRecyle, this.orderIds,
				this.locationModel.getName(), this.locationModel.getAddress(), String.valueOf(this.locationModel.getLng()), String.valueOf(this.locationModel
						.getLat()), resoureceType);
		
		Log.i("resoureceType", "hsw"+resoureceType);
		uploadImageAsyncTask.setUploadResourceListener(new UploadResourceListener() {

			@Override
			public void uploadResult(ResultModel resultModel, Bitmap bmp, String flagContent) {
				if (resultModel != null) {
					if (resultModel.getResult().equals("true")) {
						if (DeliveryActivity.selectSkuMap.containsKey(goodsId)) {
							int num = DeliveryActivity.selectSkuMap.get(goodsId);
							num += 1;
							DeliveryActivity.selectSkuMap.put(goodsId, num);
						}
						UploadCache.scanBimpModels.add(scanBimpModel);// 上传成功后添加至缓存
						Tools.showToast(AddSourceInfoActivity.this, "货物添加成功");
						Intent intent = new Intent(AddSourceInfoActivity.this, DeliverySourceGrid.class);
						intent.putExtra("workId", workId);
						intent.putExtra("orderIds", orderIds);
						startActivity(intent);
						finish();
					} else {
						//库中未检索到此标签
						Tools.showToast(AddSourceInfoActivity.this, resultModel.getDescription());
					}
				} else {
					Tools.showToast(AddSourceInfoActivity.this, "货物添加失败，请检查网络");
				}
			}
		});
		uploadImageAsyncTask.execute();
	}
	/**
	 * 
	 * @Title: uploadImg
	 * @Description: 上传图片
	 * @param @param path 设定文件
	 * @return void 返回类型
	 * @throws
	 *//*
	public void uploadImg1(String url, String flagContent, final String goodsId, int isRecyle) {
		int resoureceType = 0;// 0:油桶，1油罐
		if (goodsModels.size() > 1) {
			if (goodsModels.get(0).getP_ID() == 3) {
				resoureceType = 1;
			}
		}

		UploadImagePathAsyncTask uploadImagePathAsyncTask=new UploadImagePathAsyncTask(this, url, flagContent, workId, goodsId, isRecyle, this.orderIds,this.locationModel.getName(), this.locationModel.getAddress(),
				String.valueOf(this.locationModel.getLng()), String.valueOf(this.locationModel.getLat()), resoureceType);
		uploadImagePathAsyncTask.setUploadResourcePathListener(new UploadResourcePathListener() {
			
			@Override
			public void uploadPathResult(ResultModel resultModel, 	String flagContent) {
				Log.i("-----------", "hsw53");
				if (resultModel != null) {
					if (resultModel.getResult().equals("true")) {
						if (DeliveryActivity.selectSkuMap.containsKey(goodsId)) {
							int num = DeliveryActivity.selectSkuMap.get(goodsId);
							num += 1;
							DeliveryActivity.selectSkuMap.put(goodsId, num);
						}
						UploadCache.scanBimpModels.add(scanBimpModel);// 上传成功后添加至缓存
						Tools.showToast(AddSourceInfoActivity.this, "货物添加成功");
						Intent intent = new Intent(AddSourceInfoActivity.this, DeliverySourceGrid.class);
						intent.putExtra("workId", workId);
						intent.putExtra("orderIds", orderIds);
						startActivity(intent);
						finish();
					} else {
						//该批次中无此标签
						Tools.showToast(AddSourceInfoActivity.this, resultModel.getDescription()+"发货出库");
					}
				} else {
					Tools.showToast(AddSourceInfoActivity.this, "货物添加失败，请检查网络");
				}
			
			}
		});
		uploadImagePathAsyncTask.execute();
	}*/

	private void getGoodsByOrderIds() {
		GetGoodsByOrderIDAsyncTask getGoodsByOrderIDAsyncTask = new GetGoodsByOrderIDAsyncTask(this, orderIds.toString(), ContactsUtils.ORP_NONE);
		getGoodsByOrderIDAsyncTask.setGetGoodsByOrderIdListener(new GetGoodsByOrderIdListener() {

			@Override
			public void getGoodsResult(GoodsListModel goodsListModel, int orpType) {
				if (goodsListModel != null) {
					if (goodsListModel.getResult().equals("true")) {
						goodsModels = goodsListModel.getData();
						if (goodsModels != null && goodsModels.size() > 0) {
							GoodsModel goodsModel = goodsModels.get(0);
						/*	for(int i=0;i< goodsModels.size();i++){
									P_ID=goodsModels.get(i).getP_ID();
							}
						Log.i("pid", "hsw"+P_ID);*/
							Log.i("hswhh","hsw"+String.valueOf(P_ID));
							if (goodsModels.size() == 1) {
								// 默认显示
								scanBimpModel.setResourceTypeId(goodsModel.getGoodsnameID());
								scanBimpModel.setResourceTypeName(goodsModel.getGoodsName());
								scanBimpModel.setPackageType(goodsModel.getPackagetype());
								choose_resource_type_txt.setText(goodsModel.getGoodsName());
								choose_resource_type_txt.setTextColor(getResources().getColor(R.color.red));
							}
							if (goodsModel.getP_ID() == 1 || goodsModel.getP_ID() == 2) {
								//hsw
								//小桶、大桶还是油罐的标志获取 P_ID 1小桶 2 大桶 3 油罐
								/*click_to_take.setVisibility(View.VISIBLE);*/
								resource_img.setVisibility(View.VISIBLE);
							} else {
								click_to_take.setVisibility(View.VISIBLE);
								resource_img.setVisibility(View.VISIBLE);
							}
							  if(goodsModel.getRecycle()==1){
								  scanBimpModel.setIsRecyle(1);
								 /* need_rb.setVisibility(View.VISIBLE);
							      noneed_rb.setVisibility(View.GONE);*/
								
								}else if(goodsModel.getRecycle()==0){
								  scanBimpModel.setIsRecyle(0);
							     /* noneed_rb.setVisibility(View.VISIBLE);
							      need_rb.setVisibility(View.GONE);*/
								}
						}
					} else {
						Tools.showToast(AddSourceInfoActivity.this, goodsListModel.getDescription());
					}
				} else {
					Tools.showToast(AddSourceInfoActivity.this, "订单产品获取失败，请检查网络");
				}
			}
		});
		getGoodsByOrderIDAsyncTask.execute();
	}

}
