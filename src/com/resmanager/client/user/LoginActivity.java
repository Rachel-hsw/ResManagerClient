/**   
 * @Title: LoginActivity.java 
 * @Package com.resmanager.client.user 
 * @Description:用户登录
 * @author ShenYang  
 * @date 2015-11-24 下午3:08:25 
 * @version V1.0   
 */
package com.resmanager.client.user;

import com.resmanager.client.R;
import com.resmanager.client.home.DisplayMonthDateAsyncTask;
import com.resmanager.client.home.HomePageActivity;
import com.resmanager.client.model.ResultModel;
import com.resmanager.client.model.UserModel;
import com.resmanager.client.system.SPHelper;
import com.resmanager.client.user.ImeiLastAsyncTask.ImeiListener;
import com.resmanager.client.user.UserLoginAsyncTask.LoginListener;
import com.resmanager.client.utils.ContactsUtils;
import com.resmanager.client.utils.Tools;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * @ClassName: LoginActivity
 * @Description: 用户登录
 * @author ShenYang
 * @date 2015-11-24 下午3:08:25
 * 
 */
public class LoginActivity extends Activity implements OnClickListener {
	private EditText user_account_edit, user_pass_edit;
	private CheckBox auto_login_check;
	private TextView ver_txt;
	 private String MonthDate;
	 private LinearLayout up;
	 private LinearLayout uptwo;
	 private LinearLayout hidden;
	private SharedPreferences pref;
    public SharedPreferences mSharedPreferences;
    private CheckBox rememberPass;
	private String imei;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.user_login_page);
		findViewById(R.id.login_btn).setOnClickListener(this);
		user_account_edit = (EditText) findViewById(R.id.user_account_edit);
		user_pass_edit = (EditText) findViewById(R.id.user_pass_edit);
		auto_login_check = (CheckBox) findViewById(R.id.auto_login_check);
		ver_txt = (TextView) findViewById(R.id.ver_txt);
		ver_txt.setText("版本号:v" + Tools.getVersionName(this));
		up=(LinearLayout) findViewById(R.id.up);
		uptwo=(LinearLayout) findViewById(R.id.uptwo);
		hidden=(LinearLayout) findViewById(R.id.hidden);
	    rememberPass = (CheckBox) findViewById(R.id.remember_pass);
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
	    setRemember();
	    
	  //该Activity的最外层Layout
		 final LinearLayout father = (LinearLayout) findViewById(R.id.father);// private LinearLayout activityRootView;
		//给该layout设置监听，监听其布局发生变化事件
		 father.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
	            @Override
	            public void onGlobalLayout() {
	            	//比较Activity根布局与当前布局的大小
	                int heightDiff = father.getRootView().getHeight() - father.getHeight();
	                if (heightDiff > dpToPx(LoginActivity.this, 200)) { // if more than 200 dp, it's probably a keyboard...
	                    // ... do something here
	                    Log.d("TAG","aaaa");//显示
	               	 LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams( ViewGroup.LayoutParams.MATCH_PARENT,
				                ViewGroup.LayoutParams.WRAP_CONTENT);
				        lp.setMargins(0, 0, 0, 0);//left top right button
				       up.setLayoutParams(lp);
				       uptwo.setLayoutParams(lp);
				       hidden.setVisibility(View.GONE);
	                }else {
	                    Log.d("TAG","bbbb");//消失
	               	 LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams( ViewGroup.LayoutParams.MATCH_PARENT,
				                ViewGroup.LayoutParams.WRAP_CONTENT);
				        lp.setMargins(0,45, 0, 0);//left top right button
				       up.setLayoutParams(lp);
				       lp.setMargins(0, 50, 0, 0);
				       uptwo.setLayoutParams(lp);
				       hidden.setVisibility(View.VISIBLE);

	                }
	            }
	        });
	    }
//	    COMPLEX_UNIT_DIP
	    public static float dpToPx(Context context, float valueInDp) {
	        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
	        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, valueInDp, metrics);

	}
	    /**
	     * 设置记住用户名和密码
	     */
	    private void setRemember() {
	        if (isRemembered()) {
	            String nameFromLog = mSharedPreferences.getString("nameFromLog","");
	            String passFromLog = mSharedPreferences.getString("passFromLog","");
	            user_account_edit.setText(nameFromLog);
	            user_pass_edit.setText(passFromLog);
	            rememberPass.setChecked(true);
	        }
	    }

	    /**
	     *  获取记住密码的状态
	     */
	    private boolean isRemembered() {
	        return mSharedPreferences.getBoolean("isRemembered",false);
	    }
	    /**
	     *  保存用户数据到Share
	     */
	    private void savedUsers() {
	        SharedPreferences.Editor editor = mSharedPreferences.edit();
	        editor.putString("nameFromLog",user_account_edit.getText().toString().trim());
	        editor.putString("passFromLog",user_pass_edit.getText().toString().trim());  
	        if (rememberPass.isChecked()) {
	            editor.putBoolean("isRemembered", true);
	        }else {
	            editor.putBoolean("isRemembered", false);
	        }
	        editor.commit();
	    }
	 
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.login_btn:
			savedUsers();
			Login();
			break;

		default:
			break;
		}
	}

	/**
	 * 
	 * @Title: Login
	 * @Description: 登录
	 * @param 设定文件
	 * @return void 返回类型
	 * @throws
	 */
	private void Login() {
		final String account = user_account_edit.getText().toString();
		final String pass = user_pass_edit.getText().toString();
		 imei = Tools.getIMEI(this);//A0000055CA0558//862348030051486//863671032042354
		if (account.equals("")) {
			Tools.showToast(this, "请输入工号");
		} else if (pass.equals("")) {
			Tools.showToast(this, "请输入密码");
		} else {
			UserLoginAsyncTask userLoginAsyncTask = new UserLoginAsyncTask(this, account, pass, imei);
			userLoginAsyncTask.setLoginListener(new LoginListener() {

				@Override
				public void loginResult(UserModel userModel) {
					
					if (userModel != null) {
						Log.i("hsws", "hsws");
						if (userModel.getResult().equals("true")) {
							if (auto_login_check.isChecked()) {
								SPHelper.getInstance(LoginActivity.this).saveUserInfo(account, pass);
							}
							
							ContactsUtils.USER_KEY = userModel.getUserKey();
						/*	//hsw
							ImeiLastAsyncTask imeiLastAsyncTask=new ImeiLastAsyncTask(LoginActivity.this,String.valueOf(ContactsUtils.userDetailModel.getUserId()), imei);
							imeiLastAsyncTask.setLoginListener(new ImeiListener() {
								
								@Override
								public void loginResult(UserModel userModel) {
									// TODO Auto-generated method stub
									
								}
							});
							imeiLastAsyncTask.execute();*/
							Intent intent = new Intent(LoginActivity.this, HomePageActivity.class);
							startActivity(intent);
							
							LoginActivity.this.finish();
						} else {
							Tools.showToast(LoginActivity.this, userModel.getDescription());
						}
					} else {
						 /*判断是否有网络
						  *  ConnectivityManager connectivityManager = (ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);
						  NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
						  if (connectivityManager==null||networkInfo==ni) {
							
						}*/
						Log.i("hsww", "hsww");
						Tools.showToast(LoginActivity.this, "登录失败，请检查网络");
					}
				}
			});
			userLoginAsyncTask.execute();
		}
	}
}
