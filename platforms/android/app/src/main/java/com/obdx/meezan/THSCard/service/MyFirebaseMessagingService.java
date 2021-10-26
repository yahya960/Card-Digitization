package com.obdx.meezan.THSCard.service;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Process;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.gemalto.mfs.mwsdk.dcm.DigitalizedCardManager;
import com.gemalto.mfs.mwsdk.provisioning.ProvisioningServiceManager;
import com.gemalto.mfs.mwsdk.provisioning.listener.PushServiceListener;
import com.gemalto.mfs.mwsdk.provisioning.model.KnownMessageCode;
import com.gemalto.mfs.mwsdk.provisioning.model.ProvisioningServiceError;
import com.gemalto.mfs.mwsdk.provisioning.model.ProvisioningServiceMessage;
import com.gemalto.mfs.mwsdk.provisioning.sdkconfig.ProvisioningBusinessService;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.obdx.meezan.App;
import com.obdx.meezan.THSCard.Constants;
import com.obdx.meezan.THSCard.SDKHelper;
import com.obdx.meezan.THSCard.SharedPreferenceUtils;
import com.obdx.meezan.THSCard.util.TokenReplenishmentRequestor;

import java.util.Map;



public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = MyFirebaseMessagingService.class.getSimpleName();
    private static final String FIREBASE_ID = "firebase_id";


    @Override
    public void onNewToken(@NonNull String s) {

        super.onNewToken(s);
        Log.i(TAG,"Token Refresh is "+s);
       SharedPreferenceUtils.setFirebaseId(this,s);

        //Firebase API has limitation when there are multiple sender ID, the onNewToken is triggered only for default SENDER_ID.
        // So it is prudent to check for updatePushToken regularly after SDK initialization as well.
        //And it is prudent to check for updatePushToken just before card enrollment process begin as well.
        SDKHelper.updateFirebaseToken(this.getApplicationContext());
    }

    public void initSDKs(final Context context, final boolean isUIUpdateNeeded) {
        if (Constants.IS_PFP_ENABLED) {
//            AppLogger.d(AppConstants.APP_TAG, "initSDKsForPFP");
            SDKHelper.InitCPSSDKCallback initCPSSDKCallback = new SDKHelper.InitCPSSDKCallback() {
                @Override
                public void doAction() {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Process.setThreadPriority(Process.THREAD_PRIORITY_LOWEST);
                            //Firebase API has limitation when there are multiple sender ID, the onNewToken is triggered only for default SENDER_ID.
                            // So it is prudent to check for updatePushToken regularly after SDK initialization as well.
                            //And it is prudent to check for updatePushToken just before card enrollment process begin as well.
                            SDKHelper.updateFirebaseToken(context);
                            //init MG SDK
                            SDKHelper.initMGSDKCall(context);

                            //perform Wallet Secure Enrollment
                            SDKHelper.performWalletSecureEnrollmentFlow(context);
                        }
                    }).start();
                }
            };
            SDKHelper.initCPSSDK(context, initCPSSDKCallback,isUIUpdateNeeded);

        }
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
//        AppLogger.d(TAG, remoteMessage.getData().toString());

        initSDKs(MyFirebaseMessagingService.
                this.getApplicationContext(),false);


        super.onMessageReceived(remoteMessage);

        Bundle bundle = new Bundle();

        Map<String, String> data = remoteMessage.getData();
        if (data == null) {
            //We are only handling data messages from FCM.
            //Not interested in other types of messages.
            return;
        }

        String sender = "";
        String action = "";
        String digitalCardID = "";
        if (!data.isEmpty()) {
            for (String key : data.keySet()) {
//                AppLogger.d(TAG, key + " ---|--- " + data.get(key));

                if (null != data.get(key)) {
                    bundle.putString(key, data.get(key));
                    if (key.equalsIgnoreCase("sender")) {
                        sender = data.get(key);
                    }
                    if (key.equalsIgnoreCase("action")) {
                        action = data.get(key);
                    }
                    if (key.equalsIgnoreCase("digitalCardID")) {
                        digitalCardID = data.get(key);
                    }

                }

            }
        }
        if (sender.equalsIgnoreCase("CPS")) {
            final String isPushNotiDisabled = SharedPreferenceUtils.isPushNotiDisabled(this.getApplicationContext());
            Log.i(TAG, "action "+ action + "ddigitalCardID "+ digitalCardID);
            if (isPushNotiDisabled.equals("") || isPushNotiDisabled.isEmpty()){
                Log.i(TAG, "action "+ action + "ddigitalCardID "+ digitalCardID);
                ProvisioningBusinessService provService = ProvisioningServiceManager.getProvisioningBusinessService();
                provService.processIncomingMessage(bundle, new PushServiceListener() {

                    @Override
                    public void onError(ProvisioningServiceError provisioningServiceError) {
////        Toast.makeText(this, provisioningServiceError.getErrorMessage(), Toast.LENGTH_SHORT).show();
//
//        ProvisioningServiceErrorCodes errCode = provisioningServiceError.getSdkErrorCode();
//        switch (errCode) {
//            case COMMON_COMM_ERROR:
//            case COMMON_NO_INTERNET:
////                if (provisionRetryCounter == MAX_RETRY) {
////                    provisionRetryCounter = 0;
////                    AppLogger.e(TAG, "Error during provisioning session :" + provisioningServiceError.getErrorMessage());
////                } else {
////                    retryProvisioning();
////                    provisionRetryCounter++;
////                }
//                break;
//            default:
////                provisionRetryCounter = 0;
////                AppLogger.e(TAG, "Error during provisioning session :" + provisioningServiceError.getErrorMessage());
//                        break;
                    }


                @Override
                public void onUnsupportedPushContent(Bundle bundle) {
//                    Toast.makeText(this, "Impossible .onUnsupportedPushContent()", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onServerMessage(String s, ProvisioningServiceMessage provisioningServiceMessage) {
                    String messageCode = provisioningServiceMessage.getMsgCode();
                    if (messageCode == null) {
//            AppLogger.e(TAG, "messageCode is null for some reason");
                        return;
                    }
                    Log.e("ServerMessageCode",provisioningServiceMessage.getMsgCode().toString());
//                    Toast.makeText(this, provisioningServiceMessage.getMsgCode(), Toast.LENGTH_SHORT).show();
                    switch (messageCode) {
                        case KnownMessageCode.REQUEST_INSTALL_CARD:
                            // 1st push notification for installing card
                        case KnownMessageCode.REQUEST_REPLENISH_KEYS:
                            // 2nd push notification for installing payment keys and subsequent replenishments
                        case KnownMessageCode.REQUEST_RESUME_CARD:
                            // card resumed
                        case KnownMessageCode.REQUEST_SUSPEND_CARD:
                            // card suspended
                        case KnownMessageCode.REQUEST_RENEW_CARD:
                            //token renewed (profile update)
                        case KnownMessageCode.REQUEST_DELETE_CARD:
                            //card deleted.
//                            LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(Constants.ACTION_RELOAD_CARDS));
                            break;
                        default:
//                AppLogger.e(TAG, "Other events: Not handling");
                    }
                }

                @Override
                public void onComplete() {
//                    Toast.makeText(this, "Completed processing message", Toast.LENGTH_SHORT).show();
                }
                });
            }

        } else if ("MG".equalsIgnoreCase(sender)) {
            if (action != null && action.equalsIgnoreCase("MG:ReplenishmentNeededNotification")) {
                if (digitalCardID != null && !digitalCardID.isEmpty()) {
                    final String tokenizedCardId = DigitalizedCardManager.getTokenizedCardId(digitalCardID);
                    if (tokenizedCardId != null && !tokenizedCardId.isEmpty()) {
                        TokenReplenishmentRequestor.forceReplenish(tokenizedCardId);
                    }
                }
            }
        } else if (sender.equalsIgnoreCase("TNS")) {

        }

    }
}
