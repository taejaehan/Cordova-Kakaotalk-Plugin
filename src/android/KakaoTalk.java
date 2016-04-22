package com.htj.plugin.kakao;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.kakao.auth.ApprovalType;
import com.kakao.auth.AuthType;
import com.kakao.auth.IApplicationConfig;
import com.kakao.auth.ISessionCallback;
import com.kakao.auth.ISessionConfig;
import com.kakao.auth.KakaoAdapter;
import com.kakao.auth.KakaoSDK;
import com.kakao.auth.Session;
import com.kakao.kakaolink.AppActionBuilder;
import com.kakao.kakaolink.AppActionInfoBuilder;
import com.kakao.kakaolink.KakaoLink;
import com.kakao.kakaolink.KakaoTalkLinkMessageBuilder;
import com.kakao.network.ErrorResult;
import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.LogoutResponseCallback;
import com.kakao.usermgmt.callback.MeResponseCallback;
import com.kakao.usermgmt.response.model.UserProfile;
import com.kakao.util.KakaoParameterException;
import com.kakao.util.exception.KakaoException;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

public class KakaoTalk extends CordovaPlugin {

	private static final String LOG_TAG = "KakaoTalk";
	private static volatile Activity currentActivity;
	private SessionCallback callback;

	/**
	 * Initialize cordova plugin kakaotalk
	 * @param cordova
	 * @param webView
	 */
	public void initialize(CordovaInterface cordova, CordovaWebView webView)
	{
		Log.v(LOG_TAG, "kakao : initialize");
		super.initialize(cordova, webView);
		currentActivity = this.cordova.getActivity();
		KakaoSDK.init(new KakaoSDKAdapter());
	}

	/**
	 * Execute plugin
	 * @param action
	 * @param options
	 * @param callbackContext
	 */
	public boolean execute(final String action, JSONArray options, final CallbackContext callbackContext) throws JSONException
	{
		Log.v(LOG_TAG, "kakao : execute " + action);
		cordova.setActivityResultCallback(this);
		callback = new SessionCallback(callbackContext);
		Session.getCurrentSession().addCallback(callback);

		if (action.equals("login")) {
			this.login();
			//requestMe(callbackContext);
			return true;
		}else if (action.equals("logout")) {
			this.logout(callbackContext);
			return true;
		}else if (action.equals("share")) {

			try {
				this.share(options, callbackContext);
				return true;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			}
		}
		return false;
	}

	private void share(JSONArray options, final CallbackContext callbackContext) throws KakaoParameterException {

		try {
			final JSONObject parameters = options.getJSONObject(0);

			final Activity activity = this.cordova.getActivity();
			final KakaoLink kakaoLink = KakaoLink.getKakaoLink(activity);
			final KakaoTalkLinkMessageBuilder kakaoTalkLinkMessageBuilder = kakaoLink.createKakaoTalkLinkMessageBuilder();
			cordova.getThreadPool().execute(new Runnable() {
				@Override
				public void run() {
						try {
							if (parameters.has("text")) {
								kakaoTalkLinkMessageBuilder.addText(parameters.getString("text"));
							}
							if (parameters.has("image")) {
								JSONObject imageObj =  parameters.getJSONObject("image");
								Integer imageWidth = 80;
								Integer imageHeight = 80;
								if(imageObj.has("width") && Integer.parseInt(imageObj.getString("width")) > 80){
									imageWidth =  Integer.parseInt(imageObj.getString("width"));
								};
								if(imageObj.has("height") &&  Integer.parseInt(imageObj.getString("height")) > 80){
									imageHeight =  Integer.parseInt(imageObj.getString("height"));
								};
								kakaoTalkLinkMessageBuilder.addImage(imageObj.getString("src"), imageWidth, imageHeight);
							}
							if (parameters.has("weblink")) {
								JSONObject weblinkObj =  parameters.getJSONObject("weblink");
								if(weblinkObj.has("text") && weblinkObj.has("url")){
									kakaoTalkLinkMessageBuilder.addWebLink(weblinkObj.getString("text"), weblinkObj.getString("url"));
								}
							}
							if (parameters.has("applink")) {
								JSONObject applinkObj =  parameters.getJSONObject("applink");
								if(applinkObj.has("text") && applinkObj.has("url")) {
									String applinkParam = "";
									if (parameters.has("params")) {
										JSONObject paramsObj = parameters.getJSONObject("params");
										Log.v(LOG_TAG, "paramsObj : " + paramsObj);
										Iterator keys = paramsObj.keys();
										int i = 0;
										while (keys.hasNext()) {
											String key = keys.next().toString();
											String paramValue = paramsObj.getString(key);
											Log.v(LOG_TAG, "key : " + key);
											Log.v(LOG_TAG, "paramValue : " + paramValue);
											if (paramValue != "") {
												i++;
												if (i > 1) {
													key = "&" + key;
												}
												applinkParam = applinkParam + key + "=" + paramValue;
											}
										}
										;
									}
									Log.v(LOG_TAG, "applinkParam : " + applinkParam);
									kakaoTalkLinkMessageBuilder.addAppButton(applinkObj.getString("text"),
											new AppActionBuilder()
													.addActionInfo(AppActionInfoBuilder
															.createAndroidActionInfoBuilder()
															.setExecuteParam(applinkParam)
															.setMarketParam("referrer=kakaotalklink")
															.build())
													.addActionInfo(AppActionInfoBuilder
															.createiOSActionInfoBuilder()
															.setExecuteParam(applinkParam)
															.build())
													.setUrl(applinkObj.getString("url"))
													.build());
								}
							};
							kakaoTalkLinkMessageBuilder.build();
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, "Exception error : " + e));
							callbackContext.error("Exception error : " + e);
						}

						try {
							kakaoLink.sendMessage(kakaoTalkLinkMessageBuilder, activity);
							callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, "success"));
							callbackContext.success("success");
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, "Exception error : " + e));
							callbackContext.error("Exception error : " + e);
						}
				}
			});
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	}

	/**
	 * Log in
	 */
	private void login()
	{
		cordova.getThreadPool().execute(new Runnable() {
			@Override
			public void run() {
				Session.getCurrentSession().open(AuthType.KAKAO_TALK, currentActivity);
			}
		});
	}

	/**
	 * Log out
	 * @param callbackContext
	 */
	private void logout(final CallbackContext callbackContext)
	{
		cordova.getThreadPool().execute(new Runnable() {
			@Override
			public void run() {
				UserManagement.requestLogout(new LogoutResponseCallback() {
					@Override
					public void onCompleteLogout() {
						Log.v(LOG_TAG, "kakao : onCompleteLogout");
						callbackContext.success();
					}
				});
			}
		});
	}

	/**
	 * On activity result
	 * @param requestCode
	 * @param resultCode
	 * @param intent
	 */
	public void onActivityResult(int requestCode, int resultCode, Intent intent)
	{
		Log.v(LOG_TAG, "kakao : onActivityResult : " + requestCode + ", code: " + resultCode);
		if (Session.getCurrentSession().handleActivityResult(requestCode, resultCode, intent)) {
			return;
		}
		super.onActivityResult(requestCode, resultCode, intent);
	}

	/**
	 * Result
	 * @param userProfile
	 */
	private JSONObject handleResult(UserProfile userProfile)
	{
		Log.v(LOG_TAG, "kakao : handleResult");
		JSONObject response = new JSONObject();
		try {
			response.put("id", userProfile.getId());
			response.put("nickname", userProfile.getNickname());
			response.put("profile_image", userProfile.getProfileImagePath());
		} catch (JSONException e) {
			Log.v(LOG_TAG, "kakao : handleResult error - " + e.toString());
		}
		return response;
	}



	/**
	 * Class SessonCallback
	 */
	private class SessionCallback implements ISessionCallback {

		private CallbackContext callbackContext;

		public SessionCallback(final CallbackContext callbackContext) {
			this.callbackContext = callbackContext;
		}

		@Override
		public void onSessionOpened() {
			Log.v(LOG_TAG, "kakao : SessionCallback.onSessionOpened");
			UserManagement.requestMe(new MeResponseCallback() {
				@Override
				public void onFailure(ErrorResult errorResult) {
					callbackContext.error("kakao : SessionCallback.onSessionOpened.requestMe.onFailure - " + errorResult);
				}

				@Override
				public void onSessionClosed(ErrorResult errorResult) {
					Log.v(LOG_TAG, "kakao : SessionCallback.onSessionOpened.requestMe.onSessionClosed - " + errorResult);
					Session.getCurrentSession().checkAndImplicitOpen();
				}

				@Override
				public void onSuccess(UserProfile userProfile) {
					callbackContext.success(handleResult(userProfile));
				}

				@Override
				public void onNotSignedUp() {
					callbackContext.error("this user is not signed up");
				}
			});
		}

		@Override
		public void onSessionOpenFailed(KakaoException exception) {
			if(exception != null) {
				Log.v(LOG_TAG, "kakao : onSessionOpenFailed" + exception.toString());
			}
		}
	}


	/**
	 * Return current activity
	 */
	public static Activity getCurrentActivity()
	{
		return currentActivity;
	}

	/**
	 * Set current activity
	 */
	public static void setCurrentActivity(Activity currentActivity)
	{
		currentActivity = currentActivity;
	}

	/**
	 * Class KakaoSDKAdapter
	 */
	private static class KakaoSDKAdapter extends KakaoAdapter {

		@Override
		public ISessionConfig getSessionConfig() {
			return new ISessionConfig() {
				@Override
				public AuthType[] getAuthTypes() {
					return new AuthType[] {AuthType.KAKAO_TALK};
				}

				@Override
				public boolean isUsingWebviewTimer() {
					return false;
				}

				@Override
				public ApprovalType getApprovalType() {
					return ApprovalType.INDIVIDUAL;
				}

				@Override
				public boolean isSaveFormData() {
					return true;
				}
			};
		}

		@Override
		public IApplicationConfig getApplicationConfig() {
			return new IApplicationConfig() {
				@Override
				public Activity getTopActivity() {
					return KakaoTalk.getCurrentActivity();
				}

				@Override
				public Context getApplicationContext() {
					return KakaoTalk.getCurrentActivity().getApplicationContext();
				}
			};
		}
	}

}
