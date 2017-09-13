/**
 * @Title: HomePageActivity.java
 * @Package com.resmanager.client.home
 * @Description: 主界面
 * @author ShenYang
 * @date 2015-11-24 下午4:56:08
 * @version V1.0
 */
package com.resmanager.client.home;

import android.R.integer;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.resmanager.client.R;
import com.resmanager.client.camera.FocalLengthAnsyncTask;
import com.resmanager.client.camera.FocalLengthAnsyncTask.GetFocusListener;
import com.resmanager.client.camera.MediaButtonReceiver;
import com.resmanager.client.common.TopContainActivity;
import com.resmanager.client.flag.FlagManager;
import com.resmanager.client.flag.FlagSearch;
import com.resmanager.client.home.DisplayMonthDateAsyncTask.DisplayMonthListener;
import com.resmanager.client.home.GetGoodsListAsyncTask.GetGoodsListListener;
import com.resmanager.client.map.ChooseLocationDriverActivity;
import com.resmanager.client.map.GetLocationService;
import com.resmanager.client.model.AdModel;
import com.resmanager.client.model.FocusModelList;
import com.resmanager.client.model.GoodsListModel;
import com.resmanager.client.model.PowerModel;
import com.resmanager.client.model.ResultModel;
import com.resmanager.client.model.UserDetailModel;
import com.resmanager.client.model.UserDetailResult;
import com.resmanager.client.model.UserModel;
import com.resmanager.client.order.AllOrderList;
import com.resmanager.client.order.ChoosePkgTypeDialog;
import com.resmanager.client.order.OrderListFilter;
import com.resmanager.client.order.OrderMainNewActivity;
import com.resmanager.client.stock.StockList;
import com.resmanager.client.system.SPHelper;
import com.resmanager.client.system.TrafficService;
import com.resmanager.client.user.GetUserDetailAsyncTask;
import com.resmanager.client.user.GetUserDetailAsyncTask.GetUserDetailListener;
import com.resmanager.client.user.ImeiLastAsyncTask;
import com.resmanager.client.user.ImeiLastAsyncTask.ImeiListener;
import com.resmanager.client.user.LoginActivity;
import com.resmanager.client.user.UpdateTokenAsyncTask;
import com.resmanager.client.user.balance.BalanceActivity;
import com.resmanager.client.user.message.GetUnReadMsgNumAsyncTask;
import com.resmanager.client.user.message.GetUnReadMsgNumAsyncTask.GetUnReadListenr;
import com.resmanager.client.user.message.MessageList;
import com.resmanager.client.user.order.MyOrderListNew;
import com.resmanager.client.user.order.SearchOrderActivity;
import com.resmanager.client.user.order.delivery.MyDaiYunOrderList;
import com.resmanager.client.user.order.unloading.DriverList;
import com.resmanager.client.user.order.unloading.MyUploadingOrderList;
import com.resmanager.client.user.order.unloading.UploadingActivity;
import com.resmanager.client.user.order.unloading.UploadingTrailAsyncTask;
import com.resmanager.client.user.order.unloading.UploadingTrailAsyncTask.UploadingTrailListener;
import com.resmanager.client.user.recyle.ChooseCustomerActivity;
import com.resmanager.client.utils.ContactsUtils;
import com.resmanager.client.utils.Tools;
import com.resmanager.client.view.CustomDialog;
import com.resmanager.client.view.CustomDialog.ToDoListener;
import com.squareup.picasso.Picasso;
import com.tencent.android.tpush.XGIOperateCallback;
import com.tencent.android.tpush.XGPushConfig;
import com.tencent.android.tpush.XGPushManager;
import com.tencent.android.tpush.service.XGPushService;






import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author ShenYang
 * @ClassName: HomePageActivity
 * @Description:主界面
 * @date 2015-11-24 下午4:56:08
 */
@SuppressLint({"InflateParams", "HandlerLeak", "ClickableViewAccessibility"})
public class HomePageActivity extends TopContainActivity implements OnTouchListener, OnItemClickListener {
    private GridView orpGrid;
    private OrpGridAdapter orpGridAdapter;
    private long mPressedTime = 0;
    private TextView name_txt, work_number_txt, leavel_txt;
    private ViewPager AdViewPager;
    private ImageView[] imageViews = null;
    private ImageView imageView = null;
    private ViewGroup group;
    private TextView desc;
    private AtomicInteger what = new AtomicInteger(0);
    private boolean isContinue = true;
    private CustomDialog dialog;
    private int SWITCH_QR_CODE=1;//送货扫描二维码是否显示的开关,1是不显示
    private String MonthDate="";
    private boolean myflag=false;
    
    private final Handler viewHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            AdViewPager.setCurrentItem(msg.what);
            super.handleMessage(msg);
        }
    };
    private List<AdModel> baseAdModels;
    private List<PowerModel> pms = new ArrayList<PowerModel>();
    private CustomDialog cm, noticeDialog;
    private ArrayList<Integer> ids = new ArrayList<>();
    private SharedPreferences.Editor editor;
	protected String renwuliang="0";
    private String daihuishou_s="0";
    private String daihuishou_b="0";
	private String start_month;
	private String end_month;

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
        topView.findViewById(R.id.title_left_img).setVisibility(View.INVISIBLE);
        ImageView titleRightImg = (ImageView) topView.findViewById(R.id.title_right_img);
        titleRightImg.setVisibility(View.VISIBLE);
        titleRightImg.setImageResource(R.drawable.exit);
        titleRightImg.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                signOut();
            }
        });
        return topView;
    }

    /**
     * @return void 返回类型
     * @throws
     * @Title: signOut
     * @Description: 退出登录
     */
    private void signOut() {
        if (cm == null) {
            cm = new CustomDialog(this, R.style.myDialogTheme);
            cm.setContentText("是否退出当前登录用户");
            cm.setToDoListener(new ToDoListener() {

                @Override
                public void doSomething() {
                   /* SPHelper.getInstance(HomePageActivity.this).deleteSp();*/
                    Intent loginIntent = new Intent(HomePageActivity.this, LoginActivity.class);
                    startActivity(loginIntent);
                    startOrStopService(1);
                    finish();
                }
            });
        }
        cm.show();
    }

    /** 
     * @return void 返回类型
     * @throws
     * @Title: showNoticeDialog
     * @Description: 提示框
     */
    public void showNoticeDialog() {
        if (noticeDialog == null) {
            noticeDialog = new CustomDialog(this, R.style.myDialogTheme);
            noticeDialog.setContentText("当前模块正在吐血开发中。。。");
            noticeDialog.setToDoListener(new ToDoListener() {

                @Override
                public void doSomething() {
                    noticeDialog.dismiss();
                }
            });
        }
        noticeDialog.show();
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
        View contentView = inflater.inflate(R.layout.home_page, null);
        orpGrid = (GridView) contentView.findViewById(R.id.orp_grid);
        name_txt = (TextView) contentView.findViewById(R.id.name_txt);
        work_number_txt = (TextView) contentView.findViewById(R.id.work_number_txt);
        leavel_txt = (TextView) contentView.findViewById(R.id.leavel_txt);
        AdViewPager = (ViewPager) contentView.findViewById(R.id.adv_pager);
        group = (ViewGroup) contentView.findViewById(R.id.view_group);
        desc = (TextView) contentView.findViewById(R.id.desc);
        getUserDetail();
        // Tools.getNetWorkState(this);
        baseAdModels = initAdData();
        initAdViewPager(baseAdModels);
     
        return contentView;
    }
    
    //上传
    private void ImeiLast(){
        String  imei = Tools.getIMEI(this);
        ImeiLastAsyncTask imeiLastAsyncTask=new ImeiLastAsyncTask(HomePageActivity.this,String.valueOf(ContactsUtils.userDetailModel.getUserId()), imei);
		imeiLastAsyncTask.setLoginListener(new ImeiListener() {
			
			@Override
			public void loginResult(UserModel userModel) {
				// TODO Auto-generated method stub
				
			}
		});
		imeiLastAsyncTask.execute();
    }
  //获取本月流量
  	private void getMonthDate(){
  	   long currentTime=System.currentTimeMillis();//long now = android.os.SystemClock.uptimeMillis();  
	    SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");  
	    Date d1=new Date(currentTime);  
	    String time=format.format(d1);  //DataDisplay
       String a[] = time.split("-");  
       String date=a[0]+a[1];//201709
    /*   String daytime=a[2];//07 15:59:21
       String b[] = daytime.split(" ");  
       String day=b[0];
       int day_int=Integer.valueOf(day);*/
     
  /*     if (day_int>=23) {
    	   start_month=a[1];
	}else{
		start_month=String.valueOf(Integer.valueOf(a[1])-1);
		if ((Integer.valueOf(a[1])-1)<10) {
			start_month="0"+start_month;
		}
		
	}
        end_month=String.valueOf(Integer.valueOf(start_month)+1);
        if (Integer.valueOf(start_month)+1<10) {
        	 end_month="0"+end_month;
		}*/
       
       int www=ContactsUtils.userDetailModel.getUserId();
        String hhh=String.valueOf(www);
        String imei = Tools.getIMEI(this);
       /* start_month=a[0]+"-"+start_month;//2017-08
        end_month=a[0]+"-"+end_month;//2017-09
        
*/        start_month="2017-08";
          end_month="2017-09"; 
        DisplayMonthDateAsyncTask displayMonthDateAsyncTask=new DisplayMonthDateAsyncTask(HomePageActivity.this,date,hhh,imei,start_month,end_month);
       displayMonthDateAsyncTask.setDisplayMonthListener(new DisplayMonthListener() {
			
			@Override
			public void displayMonthResult(ResultModel rm) {
				// TODO Auto-generated method stub
				//{"result":"true","description":"1.30649978","UserKey":null,"DriverTask":"0,0,287"}
				if (rm != null) {
					if (rm.getResult().equals("true")) {			
					//	Tools.showToast(HomePageActivity.this,rm.getDescription());
						MonthDate=rm.getDescription().trim();	
						String[] split=rm.getDriverTask().trim().split(",");
						renwuliang=split[0];
					    daihuishou_s=split[1];
                        daihuishou_b=split[2];
						String intNumber = MonthDate.substring(0,MonthDate.indexOf("."));
						int MB=Integer.parseInt(intNumber);	
						//Invalid int: "27.0653039216995"
						if ((MB/1024)>=1) {
							float GB=(float) (MB/1024.0);
							MonthDate=GB+"GB";
						}else{
							MonthDate=intNumber+"MB";
						}
					} else {
						//Tools.showToast(HomePageActivity.this, rm.getDescription());
						MonthDate=rm.getDescription();	
					}
				
					if(MonthDate!=null&&!MonthDate.equals("")){
						 showDialog2(MonthDate,renwuliang,daihuishou_s,daihuishou_b);
                        }
				} else {
					Tools.showToast(HomePageActivity.this, "无法连接对方数据库");
				}	
			}
		});
       displayMonthDateAsyncTask.execute();
  	}
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    	
        // 2.36（不包括）之前的版本需要调用以下2行代码
        Intent service = new Intent(this, XGPushService.class);
        startService(service);
    	Log.i("monthdate","monthdate111");
        // Intent service1 = new Intent(this, ListenMediaBtuttonService.class);
        // startService(service1);
        // 获得AudioManager对象
        AudioManager mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        // 构造一个ComponentName，指向MediaoButtonReceiver类
        // 下面为了叙述方便，我直接使用ComponentName类来替代MediaoButtonReceiver类
        ComponentName mbCN = new ComponentName(getPackageName(), MediaButtonReceiver.class.getName());
        // 注册一个MedioButtonReceiver广播监听
        mAudioManager.registerMediaButtonEventReceiver(mbCN);
        
    /*	Intent startIntent = new Intent(HomePageActivity.this, TrafficService.class);
 		startService(startIntent); // 启动流量统计服务
*/       
        // 取消注册的方法
        // mAudioManager.unregisterMediaButtonEventReceiver(mbCN);
   
        /*SharedPreferences  pref = PreferenceManager.getDefaultSharedPreferences(this);
    	 myflag = pref.getBoolean("myflag", false);
    	 Log.i("myflag","myflag"+myflag);
        if(myflag==false){
        
     		SharedPreferences.Editor editor = pref.edit();
			editor.putBoolean("myflag", true);
			editor.commit();*/
			
      /*  }*/
 
    }
    @Override
    protected void onStart() {
    	// TODO Auto-generated method stub
    	super.onStart();
    	Intent startIntent = new Intent(HomePageActivity.this, TrafficService.class);
 		startService(startIntent); // 启动流量统计服务
    }
 /*   *//**
     * 带按钮功能的提示对话框
     *
     * @param message
     *//*
    public void showDialog2(String message) {
        new AlertDialog.Builder(this)
                .setTitle("本月已用流量")
                .setMessage(message)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .show();
    }
*/

    /**
       * 带按钮功能的提示对话框
       *
       * @param message
       */
      public void showDialog2(String message,String renwuliang,String daihuishou_s,String daihuishou_b) {
          new AlertDialog.Builder(this)
                  .setTitle("本手机本月已用流量:"+message)
                  .setMessage("您本月的任务量为："+renwuliang+"\r\n已经回收小桶量为："+daihuishou_s+"\r\n已经回收大桶量为："+daihuishou_b)
                  .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                      @Override
                      public void onClick(DialogInterface dialogInterface, int i) {

                      }
                  })
                  .show();
      }
  	
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            long mNowTime = System.currentTimeMillis();// 获取第一次按键时间
            if ((mNowTime - mPressedTime) > 2000) {// 比较两次按键时间差
            	Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
                mPressedTime = mNowTime;
            } else {
              	if (dialog == null) {
        			dialog = new CustomDialog(this, R.style.myDialogTheme);
        			dialog.setContentText("是否关闭Gps，请前往设置关闭GPS");
        			dialog.setToDoListener(new CustomDialog.ToDoListener() {

        				@Override
        				public void doSomething() {
        					Intent intent = new Intent(
        							Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        					startActivityForResult(intent, 0); // 设置完成后返回到原来的界面
        					dialog.dismiss();
        					HomePageActivity.this.finish();
        					System.exit(0);
        				}
        			});
        			dialog.setCancelBtnListener(new CustomDialog.CancelBtnListener() {

        				@Override
        				public void cancel() {
        					dialog.dismiss();
        					finish();
        					HomePageActivity.this.finish();
        					System.exit(0);
        				}
        			});
        		}dialog.show();
           
                
            }
        } else if (KeyEvent.KEYCODE_ENTER == event.getKeyCode() && KeyEvent.ACTION_DOWN == event.getAction()) {

            return true;
        }
        return false;
    }
    /**
     * 用来判断服务是否运行.
     * @param className 判断的服务名字
     * @return true 在运行 false 不在运行
     */
    public static boolean isServiceRunning(Context mContext,String className) {
        boolean isRunning = false;
      
ActivityManager activityManager = (ActivityManager)
mContext.getSystemService(Context.ACTIVITY_SERVICE); 
        List<ActivityManager.RunningServiceInfo> serviceList 
        = activityManager.getRunningServices(90);
      
       if (!(serviceList.size()>0)) {
            return false;
        }
        for (int i=0; i<serviceList.size(); i++) {
          
            Log.i("ll", "lltj"+serviceList.get(i).service.getClassName());
            if (serviceList.get(i).service.getClassName().equals(className) == true) {
            	Log.i("ll", "lltj"+serviceList.get(i).service.getClassName());
                isRunning = true;
                break;
            }
        }
        return isRunning;
    }
    /**
     * @param
     * @return void 返回类型
     * @throws
     * @Title: getUserDetail
     * @Description: 获取用户信息
     */
    private void getUserDetail() {

    	 Log.i("hsw1---------","#hsw2");
        GetUserDetailAsyncTask getUserDetailAsyncTask = new GetUserDetailAsyncTask(this, ContactsUtils.USER_KEY);
        getUserDetailAsyncTask.setGetUserDetailListener(new GetUserDetailListener() {
        
            @Override
            public void getUserDetailResult(UserDetailResult userDetailModel) {
           	 Log.i("hsw2---------","#hsw2");
                if (userDetailModel != null) {
               	 Log.i("hsw3---------","#hsw2");
                    if (userDetailModel.getResult().equals("true")) {
                        startOrStopService(0);
                        ContactsUtils.userDetailModel = userDetailModel.getData();

                        // 开启logcat输出，方便debug，发布时请关闭
                        XGPushConfig.enableDebug(HomePageActivity.this, true);
                        // 如果需要知道注册是否成功，请使用registerPush(getApplicationContext(),
                        // XGIOperateCallback)带callback版本
                        // 如果需要绑定账号，请使用registerPush(getApplicationContext(),account)版本
                        // 具体可参考详细的开发指南
                        // 传递的参数为ApplicationContext
                        //信鸽
                        XGPushManager.registerPush(HomePageActivity.this, new XGIOperateCallback() {

                            @Override
                            public void onSuccess(Object arg0, int arg1) {
                                new UpdateTokenAsyncTask(arg0).execute();
                            }

                            @Override
                            public void onFail(Object arg0, int arg1, String arg2) {

                            }
                        });
                        FocalLengthAnsyncTask f = new FocalLengthAnsyncTask();
                        f.setGetFocusListener(new GetFocusListener() {

                            @Override
                            public void getFocusResult(FocusModelList focusModelList) {
                                if (focusModelList != null) {
                                    if (focusModelList.getResult().equals("true") && focusModelList.getData().size() > 0) {
                                        ContactsUtils.focusModel = focusModelList.getData().get(0);
                                    }
                                }
                            }
                        });
                        f.execute();
                        new GetPicWsAsyncTask().execute();
                        XGPushManager.setTag(HomePageActivity.this, String.valueOf(ContactsUtils.userDetailModel.getUserId()));
                        setUserInfo(userDetailModel.getData());
                        String[] powers = userDetailModel.getData().getPower().split(",");
                        getMonthDate(); 
                        ImeiLast();
                        for (int i = 0; i < powers.length; i++) {
                            PowerModel pm;
                            int powerId = 0;
                            if (!powers[i].equals("")) {
                                String power=powers[i].replace("\r\n", "");
                            	powerId = Integer.parseInt(power); 
                              /*  powerId = Integer.parseInt(powers[i]); */
                                Log.i("hsw7---------",String.valueOf(i) );
                                Log.i("hsw---------",String.valueOf(powerId) );
                            }
                            if (!ids.contains(powerId)) {
                                ids.add(powerId);
                                if (powerId == ContactsUtils.POWER_ALL_ORDER) {
                                	//查看全部订单权限20000023
                                    pm = new PowerModel();
                                    pm.setPowerID(ContactsUtils.POWER_ALL_ORDER);
                                    pm.setPowerName(R.string.all_order_str);
                                    pm.setShowNum(false);
                                    pm.setPowerImg(R.drawable.all_order_ico);
                                    pms.add(pm);
                                } else if (powerId == ContactsUtils.POWER_FLAG) {
                                	
                                		// 标签授权操作20000027
                                    pm = new PowerModel();
                                    pm.setPowerID(ContactsUtils.POWER_FLAG);
                                    pm.setShowNum(false);
                                    pm.setPowerName(R.string.search_tag_str);
                                    pm.setPowerImg(R.drawable.tag_search);
                                    pms.add(pm);
                                   
                                    	
                                   
                                } else if (powerId == ContactsUtils.POWER_KUCUN) {
                                	// 库存查看20000028
                                /*    pm = new PowerModel();
                                    pm.setPowerID(ContactsUtils.POWER_KUCUN);
                                    pm.setShowNum(false);
                                    pm.setPowerName(R.string.t_stock_str);
                                    pm.setPowerImg(R.drawable.t_stock_ico);
                                    pms.add(pm);*/
                                } else if (powerId == ContactsUtils.POWER_DRIVE) {
                                    //送货20000029
                                    pm = new PowerModel();
                                    pm.setPowerID(ContactsUtils.POWER_DRIVE);
                                    pm.setShowNum(false);
                                    pm.setPowerName(R.string.delivery_str);
                                    pm.setPowerImg(R.drawable.delivery_ico);
                                    pms.add(pm);
                                } else if (powerId == ContactsUtils.POWER_UPLOADING) {
                                	// // 卸货20000030
                                    pm = new PowerModel();
                                    pm.setPowerID(ContactsUtils.POWER_UPLOADING);
                                    pm.setPowerName(R.string.unloading_str);
                                    pm.setShowNum(false);
                                    pm.setPowerImg(R.drawable.unloading_ico);
                                    pms.add(pm);
                                } else if (powerId == ContactsUtils.POWER_RESOURCE_RECYLE) {
                                	//回收20000031
                                    pm = new PowerModel();
                                    pm.setPowerID(ContactsUtils.POWER_RESOURCE_RECYLE);
                                    pm.setPowerName(R.string.recyle_str);
                                    pm.setPowerImg(R.drawable.path_ico);
                                    pm.setShowNum(false);
                                    pms.add(pm);
                                }
                               /* else if (powerId == ContactsUtils.POWER_TUIHUI) {
                                	//退回20000075
                                    pm = new PowerModel();
                                    pm.setPowerID(ContactsUtils.POWER_TUIHUI);
                                    pm.setPowerName(R.string.tuihui_str);
                                    pm.setPowerImg(R.drawable.path_ico);
                                    pm.setShowNum(false);
                                    pms.add(pm);
                                }*/else if (powerId == ContactsUtils.POWER_MY_ORDER) {
                                	//我的订单20000032
                                    pm = new PowerModel();
                                    pm.setPowerID(ContactsUtils.POWER_MY_ORDER);
                                    pm.setShowNum(false);
                                    pm.setPowerName(R.string.my_order_str);
                                    pm.setPowerImg(R.drawable.my_order_ico);
                                    pms.add(pm);
                                } else if (powerId == ContactsUtils.POWER_MY_MESSAGE) {
                                	// 我的消息20000033
                                    pm = new PowerModel();
                                    pm.setPowerID(ContactsUtils.POWER_MY_MESSAGE);
                                    pm.setPowerName(R.string.message_str);
                                    pm.setShowNum(true);
                                    pm.setPowerImg(R.drawable.message_ico);
                                    pms.add(pm);
                                } else if (powerId == ContactsUtils.POWER_MY_TRADE) {
                                	// 我的结算20000034
                                    pm = new PowerModel();
                                    pm.setPowerID(ContactsUtils.POWER_MY_TRADE);
                                    pm.setPowerName(R.string.balance_str);
                                    pm.setShowNum(false);
                                    pm.setPowerImg(R.drawable.balance_ico);
                                    pms.add(pm);
                                } else if (powerId == ContactsUtils.POWER_LOCATION) {
                                	//20000055;// 位置查看
                                    pm = new PowerModel();
                                    pm.setPowerID(ContactsUtils.POWER_LOCATION);
                                    pm.setPowerName(R.string.location_str);
                                    pm.setShowNum(false);
                                    pm.setPowerImg(R.drawable.user_location);
                                    pms.add(pm);
                                } else if (powerId == ContactsUtils.POWER_DAIXIEHUO) {
                                	//20000067;// 代卸货
                                    pm = new PowerModel();
                                    pm.setPowerID(ContactsUtils.POWER_DAIXIEHUO);
                                    pm.setPowerName(R.string.daixuehuo_str);
                                    pm.setShowNum(false);
                                    pm.setPowerImg(R.drawable.daixiehuo);
                                    pms.add(pm);
                                } else if (powerId == ContactsUtils.POWER_DAITUIHUI) {
                                	//20000094;// 代退回
                                    pm = new PowerModel();
                                    pm.setPowerID(ContactsUtils.POWER_DAITUIHUI);
                                    pm.setPowerName(R.string.daituihui_str);
                                    pm.setShowNum(false);
                                    pm.setPowerImg(R.drawable.daixiehuo);
                                    pms.add(pm);
                                }else if (powerId == ContactsUtils.POWER_TUIHUI) {
                                	//20000073;// 退回
                                    pm = new PowerModel();
                                    pm.setPowerID(ContactsUtils.POWER_TUIHUI);
                                    pm.setPowerName(R.string.tuihui_str);
                                    pm.setShowNum(false);
                                    pm.setPowerImg(R.drawable.ruku);
                                    pms.add(pm);
                                }else if (powerId == ContactsUtils.POWER_ZONGHE) {
                                	// 20000091综合查询
                                    pm = new PowerModel();
                                    pm.setPowerID(ContactsUtils.POWER_ZONGHE);
                                    pm.setPowerName(R.string.search_zonghe_str);
                                    pm.setShowNum(false);
                                    pm.setPowerImg(R.drawable.tag_search);
                                    pms.add(pm);
                                }/*
                                else if (powerId == ContactsUtils.POWER_CHUKU) {
                                	//20000074;// 发货出库
                                    pm = new PowerModel();
                                    pm.setPowerID(ContactsUtils.POWER_CHUKU);
                                    pm.setPowerName(R.string.chuku_str);
                                    pm.setShowNum(false);
                                    pm.setPowerImg(R.drawable.ruku);
                                    pms.add(pm);
                                }*/
                            }
                            if (orpGridAdapter == null) {
                                orpGridAdapter = new OrpGridAdapter(HomePageActivity.this, pms);
                                orpGrid.setAdapter(orpGridAdapter);
                                orpGrid.setOnItemClickListener(HomePageActivity.this);
                            }
                        }
                    } else {
                        Tools.showToast(HomePageActivity.this, userDetailModel.getDescription());
                        finish();
                    }
                } else {
                    Tools.showToast(HomePageActivity.this, "用户信息获取失败，请检查网络");
                    finish();
                }
            }
        });
        getUserDetailAsyncTask.execute();
      
    }

    /**
     * @param
     * @return void 返回类型
     * @throws
     * @Title: getUnReadMsgNum
     * @Description: 获取未读消息数量
     */
    private void getUnReadMsgNum() {
        GetUnReadMsgNumAsyncTask getUnReadMsgNumAsyncTask = new GetUnReadMsgNumAsyncTask();
        getUnReadMsgNumAsyncTask.setGetUnReadListenr(new GetUnReadListenr() {

            @Override
            public void getUnReadResult(ResultModel resultModel) {
                if (resultModel != null) {
                    if (resultModel.getResult().equals("true")) {
                        String resultNum = resultModel.getDescription();
                        if (orpGridAdapter != null) {
                            orpGridAdapter.notifyMsgNum(resultNum);
                        }
                    }
                }
            }
        });
        getUnReadMsgNumAsyncTask.execute();
    }

    /**
     * @param @param userDetailModel
     * @return void 返回类型
     * @throws
     * @Title: setUserInfo
     * @Description: 设置用户信息
     */
    private void setUserInfo(UserDetailModel userDetailModel) {
        getUnReadMsgNum();
        name_txt.setText("姓名:" + userDetailModel.getNickName());
        work_number_txt.setText("工号:" + userDetailModel.getUserName());
        // String uleavel = "";
        // if (userDetailModel.getUserType().equals("0")) {
        // uleavel = "普通用户";
        //
        // } else {
        // uleavel = "驾驶员";
        // }
        leavel_txt.setText("级别:" + userDetailModel.getRoleName());
    }

    private List<AdModel> initAdData() {
        List<AdModel> adModels = new ArrayList<AdModel>();
        String[] strs = {"春节放假通知", "杭州线路修路通知"};
        String[] urls = {"http://lxs.cncn.com/61420/n523930", "http://zjnews.zjol.com.cn/system/2015/04/16/020605147.shtml"};
        String[] picUrls = {"http://cdn.pcbeta.attachment.inimc.com/data/attachment/forum/201302/09/1201219iyq6dd45iqgylgg.jpg",
                "http://img.jdzj.com/UserDocument/2014a/aoyuwuliu/Picture/201441412150.jpg"};
        int[] ids = {1, 2};
        for (int i = 0; i < ids.length; i++) {
            AdModel adModel = new AdModel();
            adModel.setAID(ids[i]);
            adModel.setADName(strs[i]);
            adModel.setADUrl(urls[i]);
            adModel.setADPicUrl(picUrls[i]);
            adModels.add(adModel);
        }
        return adModels;
    }

    /**
     * @Description:初始化顶部广告轮播ViewPager
     * @version:v1.0
     * @author:ShenYang
     * @date:2014-10-30 上午8:33:36
     */
    private void initAdViewPager(List<AdModel> adModels) {// 用于存放需要显示的View
        List<View> advPics = new ArrayList<View>();
        for (int i = 0; i < adModels.size(); i++) {
            desc.setText(adModels.get(0).getADName());
            final AdModel homeCarousel = adModels.get(i);
            ImageView networkImageView = new ImageView(this);
            networkImageView.setScaleType(ImageView.ScaleType.FIT_XY);
            Picasso.with(this).load(homeCarousel.getADPicUrl()).error(R.drawable.default_img).placeholder(R.drawable.default_img).into(networkImageView);
            networkImageView.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    Intent webViewIntent = new Intent(HomePageActivity.this, CustomWebView.class);
                    webViewIntent.putExtra("url", homeCarousel.getADUrl());
                    HomePageActivity.this.startActivity(webViewIntent);
                }
            });
            advPics.add(networkImageView);
        }

        // 根据需要显示的图片数量来生成需要显示的点
        imageViews = new ImageView[advPics.size()];
        for (int i = 0; i < advPics.size(); i++) {
            imageView = new ImageView(this);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(10, 10);
            lp.setMargins(5, 0, 5, 0);
            imageView.setLayoutParams(lp);
            imageViews[i] = imageView;
            if (i == 0) {
                // 默认第一个为选中状态
                imageViews[i].setImageResource(R.drawable.banner_dot_focus);
            } else {
                // 未选中状态
                imageViews[i].setImageResource(R.drawable.banner_dot_custom);
            }
            group.addView(imageViews[i]);
        }
        AdViewPager.setAdapter(new AdViewPagerAdapter(advPics));
        AdViewPager.setOnPageChangeListener(new GuidePageChangeListener());
        autoPlayThread.start();
        AdViewPager.setOnTouchListener(this);
    }

    /**
     * @Description:顶部轮播自动播放线程
     * @version:v1.0
     * @author:ShenYang
     * @date:2014-10-30 上午8:31:45
     */
    private Thread autoPlayThread = new Thread(new Runnable() {
        @Override
        public void run() {
            while (true) {
                // 如果目前状态是自动轮播状态时，则执行自动轮播
                if (isContinue) {
                    viewHandler.sendEmptyMessage(what.get());
                    whatOption();
                }
            }

        }
    });

    /**
     * @className:com.xtwl.jy.client.activity.mainpage.GuidePageChangeListener
     * @description:ViewPager页面滚动监听，用于显示dot点的状态
     * @version:v1.0.0
     * @date:2014-10-30 上午8:33:12
     * @author:ShenYang
     */
    private final class GuidePageChangeListener implements OnPageChangeListener {

        @Override
        public void onPageScrollStateChanged(int arg0) {
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {

        }

        @Override
        public void onPageSelected(int arg0) {
            // 设置当前选中的位置
            what.getAndSet(arg0);
            desc.setText(baseAdModels.get(arg0).getADName());
            for (int i = 0; i < imageViews.length; i++) { // 遍历，如果当前显示和选中的为同一个，就将dot选中为选中状态
                imageViews[arg0].setImageResource(R.drawable.banner_dot_focus);
                if (arg0 != i) {
                    imageViews[i].setImageResource(R.drawable.banner_dot_custom);
                }
            }

        }

    }

    /**
     * @Description:设置每间隔5秒滚动一次
     * @version:v1.0
     * @author:ShenYang
     * @date:2014-10-30 上午8:32:50
     */
    private void whatOption() {
        what.incrementAndGet();
        if (what.get() > imageViews.length - 1) {
            what.getAndAdd(-4);
        }
        try {
            Thread.sleep(5000);// 线程休息4秒
        } catch (InterruptedException e) {

        }
    }

    /**
     * @Description:触摸屏幕事件监听
     * @version:v1.0
     * @author:ShenYang
     * @date:2014-10-30 上午8:31:45
     */
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                // 在手动移动viewpager的时候，不让其进行自动滚动
                isContinue = false;
                break;
            case MotionEvent.ACTION_UP:
                // 当手指离开屏幕时，进行自动轮播
                isContinue = true;
                break;
            default:
                isContinue = true;
                break;
        }
        return false;
    }
    private void isGpsOpenDialog() {
		if (dialog == null) {
			dialog = new CustomDialog(this, R.style.myDialogTheme);
			dialog.setContentText("GPS未打开，请前往设置打开GPS");
			dialog.setToDoListener(new CustomDialog.ToDoListener() {

				@Override
				public void doSomething() {
					Intent intent = new Intent(
							Settings.ACTION_LOCATION_SOURCE_SETTINGS);
					startActivityForResult(intent, 0); // 设置完成后返回到原来的界面
					dialog.dismiss();
				}
			});
			dialog.setCancelBtnListener(new CustomDialog.CancelBtnListener() {

				@Override
				public void cancel() {
					dialog.dismiss();
					finish();
				}
			});
		}
		dialog.show();
	}

    @Override
    public void onItemClick(AdapterView<?> v, View arg1, int pos, long arg3) {
        if (ContactsUtils.userDetailModel != null) {
            PowerModel pm = (PowerModel) v.getAdapter().getItem(pos);
            switch (pm.getPowerID()) {
                case ContactsUtils.POWER_ALL_ORDER:
                    // 订单
               
                	if(ContactsUtils.userDetailModel.getUserType().equals("3")){
                	/*	Intent myOrderIntent = new Intent(HomePageActivity.this,
            					OrderMainNewActivity.class);
            			myOrderIntent.putExtra("UserType",
            					"0");//0
            			myOrderIntent.putExtra("DayType", -1);//-1
            			myOrderIntent.putExtra("Days", "");
            			myOrderIntent.putExtra("Town", "");
            			myOrderIntent.putExtra("Saleoid", "");// 订单号
            			myOrderIntent.putExtra("ordercustomer", "");// 订单号
            			myOrderIntent.putExtra("userId", "");// 用户ID
            			myOrderIntent.putExtra("startDate", "");// 开始日期
            			myOrderIntent.putExtra("endDate", "");// 结束日期
            			myOrderIntent.putExtra("Packtype", "");// 包装物类型
            			myOrderIntent
            					.putExtra("salername", ContactsUtils.userDetailModel.getNickName());// 包装物类型//孟庆宇
            			setResult(ContactsUtils.SEARCH_ORDER_RESULT, myOrderIntent);//33
            			HomePageActivity.this.startActivity(myOrderIntent);*/
   
                	
                	   Intent myOrderIntent = new Intent(HomePageActivity.this, AllOrderList.class);
              			myOrderIntent.putExtra("DayType", -1);//-1
              			myOrderIntent.putExtra("orderState", -1);//-1
              			myOrderIntent.putExtra("isUsed",-1);//-1
              			myOrderIntent.putExtra("Days", "");
              			myOrderIntent.putExtra("Town", "");
              			myOrderIntent.putExtra("Saleoid", "");// 订单号
              			myOrderIntent.putExtra("ordercustomer", "");// 订单号
              			myOrderIntent.putExtra("userId", "");// 用户ID
              			myOrderIntent.putExtra("startDate", "");// 开始日期
              			myOrderIntent.putExtra("endDate", "");// 结束日期
              			myOrderIntent.putExtra("pageSize","15");// 
              			myOrderIntent.putExtra("saler",ContactsUtils.userDetailModel.getNickName());////孟庆宇
                          HomePageActivity.this.startActivity(myOrderIntent);
                	}else{
                    Intent orderIntent = new Intent(HomePageActivity.this, OrderMainNewActivity.class);
                    HomePageActivity.this.startActivity(orderIntent);
                    }
                    break;
                case ContactsUtils.POWER_FLAG:
                	//标签授权，标签查询
                    /*    Intent flagIntent = new Intent(HomePageActivity.this, FlagManager.class);*/
                    Intent flagIntent = new Intent(HomePageActivity.this, FlagSearch.class);
                        HomePageActivity.this.startActivity(flagIntent);
                        break;
					
                  
                case ContactsUtils.POWER_KUCUN:
                	//库存
                    // showNoticeDialog();
                    Intent stockIntent = new Intent(HomePageActivity.this, StockList.class);
                    HomePageActivity.this.startActivity(stockIntent);
                    break;
                case ContactsUtils.POWER_DRIVE:
                    // 送货
                	if (Tools.isOPen(this)) {
                    Intent deliveryIntent = new Intent(HomePageActivity.this, MyDaiYunOrderList.class);
                    HomePageActivity.this.startActivity(deliveryIntent);
                    }else {
                         isGpsOpenDialog();
            		}
                    break;
                case ContactsUtils.POWER_UPLOADING:
                    // 卸货
                    Intent uploadingIntent = new Intent(HomePageActivity.this, MyUploadingOrderList.class);
                    HomePageActivity.this.startActivity(uploadingIntent);
                    break;

                case ContactsUtils.POWER_RESOURCE_RECYLE:
                	//回收
                    Intent recyleIntent = new Intent(HomePageActivity.this, ChooseCustomerActivity.class);
                    recyleIntent.putExtra("father", "recyle");
                    HomePageActivity.this.startActivity(recyleIntent);
                    // showNoticeDialog();
                    break;
                case ContactsUtils.POWER_TUIHUI:
                	//退回
                    Intent tuihuiIntent = new Intent(HomePageActivity.this, ChooseCustomerActivity.class);
                    tuihuiIntent.putExtra("father", "tuihui");
                    HomePageActivity.this.startActivity(tuihuiIntent);
                    // showNoticeDialog();
                    break;
               case ContactsUtils.POWER_ZONGHE:
                	//退回审核
            	 //综合查询
					Intent searchIntent = new Intent(HomePageActivity.this, AllOrderList.class);
					searchIntent.putExtra("orderState", ContactsUtils.ORDER_ALL);
					searchIntent.putExtra("isUsed", ContactsUtils.ORDER_NOGUOLU);
					if (ContactsUtils.userDetailModel.getUserType().equals("4")) {
						searchIntent.putExtra("saler",
								ContactsUtils.userDetailModel.getNickName() + "");
					} else {
						searchIntent.putExtra("saler", "");
					}
				       HomePageActivity.this.startActivity(searchIntent);
					break;
                	// showNoticeDialog();
              
                    
                case ContactsUtils.POWER_MY_ORDER:
                    // 我的订单
                    Intent myOrderIntent = new Intent(HomePageActivity.this, MyOrderListNew.class);
                    myOrderIntent.putExtra("orderstate", ContactsUtils.ORDER_ALL);
                    HomePageActivity.this.startActivity(myOrderIntent);
                    break;
                case ContactsUtils.POWER_MY_MESSAGE:
                	//// 我的消息
                    // showNoticeDialog();
                    Intent messageIntent = new Intent(HomePageActivity.this, MessageList.class);
                    HomePageActivity.this.startActivity(messageIntent);
                    break;
                case ContactsUtils.POWER_MY_TRADE:
                	//// 我的结算
                    Intent myTradeIntent = new Intent(HomePageActivity.this, BalanceActivity.class);
                    HomePageActivity.this.startActivity(myTradeIntent);
                    break;
                case ContactsUtils.POWER_LOCATION:
                    // 位置查看
              /*      Intent locationIntent = new Intent(HomePageActivity.this, MapInitalActivity.class);*/
                	 Intent locationIntent = new Intent(HomePageActivity.this, ChooseLocationDriverActivity.class);
                    HomePageActivity.this.startActivity(locationIntent);
                    break;

                case ContactsUtils.POWER_DAIXIEHUO:
                    // 代卸货
                    Intent daixiehuoIntent = new Intent(HomePageActivity.this, DriverList.class);
                    daixiehuoIntent.putExtra("father", "daixiehuo");
                    HomePageActivity.this.startActivity(daixiehuoIntent);
                    break;
                case ContactsUtils.POWER_DAITUIHUI:
                    // 代退回
                    Intent daituihuiIntent = new Intent(HomePageActivity.this, DriverList.class);
                    daituihuiIntent.putExtra("father", "daituihui");
                    HomePageActivity.this.startActivity(daituihuiIntent);
                    break;
                case ContactsUtils.POWER_RUKU:
                	//发货入库
                /*	 Intent deliveryRuKuIntent = new Intent(HomePageActivity.this, MyDaiYunOrderList.class);
                     HomePageActivity.this.startActivity(deliveryRuKuIntent);*/
                  if (ContactsUtils.focusModel != null) {
                        GetGoodsListAsyncTask ggl = new GetGoodsListAsyncTask(HomePageActivity.this, ContactsUtils.focusModel.getPL_BH());

                        ggl.setGetGoodsListListener(new GetGoodsListListener() {

                            @Override
                            public void getGoodsListResult(GoodsListModel goodsListModel) {
                                if (goodsListModel != null) {
                                    if (goodsListModel.getResult().equals("true")) {
                                        ChoosePkgTypeDialog choosePkgTypeDialog = new ChoosePkgTypeDialog(HomePageActivity.this, R.style.myDialogTheme,
                                                goodsListModel.getData());
                                        choosePkgTypeDialog.show();
                                    } else {
                                        Tools.showToast(HomePageActivity.this, goodsListModel.getDescription());
                                    }

                                } else {
                                    Tools.showToast(HomePageActivity.this, "商品规格获取失败，请检查网络");
                                }
                            }
                        });
                        ggl.execute();
                    } else {
                        Tools.showToast(HomePageActivity.this, "该用户未绑定产线，请先绑定产线后操作");
                    }
                    // Intent rukuIntent = new Intent(HomePageActivity.this,
                    // CameraActivity.class);
                    // HomePageActivity.this.startActivity(rukuIntent);
                   break;
                default:
                    break;
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);// 必须要调用这句
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        getUnReadMsgNum();
    }

    /**
     * @param @param flag
     * @return void 返回类型
     * @throws
     * @Title: startOrStopService
     * @Description: 打开或者关闭服务
     */
    private void startOrStopService(int flag) {
        Intent serviceIntent = new Intent(this, GetLocationService.class);
        switch (flag) {
            case 0:
                // 如果服务不在运行就打开服务
                if (!Tools.isServiceWork(this, "com.resmanager.client.map.GetLocationService")) {
                    // 打开服务
                    startService(serviceIntent);
                }
                break;
            case 1:
                // 如果服务在运行就停止服务
                if (Tools.isServiceWork(this, "com.resmanager.client.map.GetLocationService")) {
                    // 停止服务
                    stopService(serviceIntent);
                }

                break;

            default:
                break;
        }
    }

}
