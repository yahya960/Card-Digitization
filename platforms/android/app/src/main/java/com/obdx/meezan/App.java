package com.obdx.meezan;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.multidex.MultiDexApplication;

import com.gemalto.mfs.mwsdk.mobilegateway.MGConfigurationChangeReceiver;
import com.gemalto.mfs.mwsdk.payment.cdcvm.DeviceCVMPreEntryReceiver;
import com.gemalto.mfs.mwsdk.provisioning.ProvisioningServiceManager;
import com.gemalto.mfs.mwsdk.provisioning.exception.ExistingRetrySessionException;
import com.gemalto.mfs.mwsdk.provisioning.exception.NoSessionException;
import com.gemalto.mfs.mwsdk.provisioning.listener.PushServiceListener;
import com.gemalto.mfs.mwsdk.provisioning.model.KnownMessageCode;
import com.gemalto.mfs.mwsdk.provisioning.model.ProvisioningServiceError;
import com.gemalto.mfs.mwsdk.provisioning.model.ProvisioningServiceErrorCodes;
import com.gemalto.mfs.mwsdk.provisioning.model.ProvisioningServiceMessage;
import com.gemalto.mfs.mwsdk.provisioning.sdkconfig.ProvisioningBusinessService;
import com.google.firebase.FirebaseApp;
import com.google.firebase.iid.FirebaseInstanceId;
import com.obdx.meezan.THSCard.Constants;
import com.obdx.meezan.THSCard.util.AppExecutors;

public class App extends MultiDexApplication  {
    private AppExecutors appExecutors;
    private MGConfigurationChangeReceiver configurationChangeReceiver;
    private final int MAX_RETRY = 5;
    private int provisionRetryCounter = 0;
    private long paymentStartTime = 0;
    private DeviceCVMPreEntryReceiver mPreEntryReceiver;
    @Override
    public void onCreate() {
        super.onCreate();
        appExecutors = new AppExecutors();
        initFirebase();
        FirebaseInstanceId.getInstance().getToken();
    }

    private void initFirebase() {
//        AppLogger.d(AppConstants.APP_TAG, "FirebaseApp.initializeApp " + AppConstants.STARTED);
        FirebaseApp.initializeApp(App.this);
//        AppLogger.d(AppConstants.APP_TAG, "FirebaseApp.initializeApp " + AppConstants.ENDED);

    }
    /**********************************************************/
    /*                Push Service listener                   */

    /**********************************************************/
//    @Override
//    public void onError(ProvisioningServiceError provisioningServiceError) {
//////        Toast.makeText(this, provisioningServiceError.getErrorMessage(), Toast.LENGTH_SHORT).show();
////
////        ProvisioningServiceErrorCodes errCode = provisioningServiceError.getSdkErrorCode();
////        switch (errCode) {
////            case COMMON_COMM_ERROR:
////            case COMMON_NO_INTERNET:
//////                if (provisionRetryCounter == MAX_RETRY) {
//////                    provisionRetryCounter = 0;
//////                    AppLogger.e(TAG, "Error during provisioning session :" + provisioningServiceError.getErrorMessage());
//////                } else {
//////                    retryProvisioning();
//////                    provisionRetryCounter++;
//////                }
////                break;
////            default:
//////                provisionRetryCounter = 0;
//////                AppLogger.e(TAG, "Error during provisioning session :" + provisioningServiceError.getErrorMessage());
//                break;
//        }
//    }
//
//    @Override
//    public void onUnsupportedPushContent(Bundle bundle) {
//        Toast.makeText(this, "Impossible .onUnsupportedPushContent()", Toast.LENGTH_SHORT).show();
//    }
//
//    @Override
//    public void onServerMessage(String s, ProvisioningServiceMessage provisioningServiceMessage) {
//        String messageCode = provisioningServiceMessage.getMsgCode();
//        if (messageCode == null) {
////            AppLogger.e(TAG, "messageCode is null for some reason");
//            return;
//        }
//        Log.e("ServerMessageCode",provisioningServiceMessage.getMsgCode().toString());
//        Toast.makeText(this, provisioningServiceMessage.getMsgCode(), Toast.LENGTH_SHORT).show();
//        switch (messageCode) {
//            case KnownMessageCode.REQUEST_INSTALL_CARD:
//                // 1st push notification for installing card
//            case KnownMessageCode.REQUEST_REPLENISH_KEYS:
//                // 2nd push notification for installing payment keys and subsequent replenishments
//            case KnownMessageCode.REQUEST_RESUME_CARD:
//                // card resumed
//            case KnownMessageCode.REQUEST_SUSPEND_CARD:
//                // card suspended
//            case KnownMessageCode.REQUEST_RENEW_CARD:
//                //token renewed (profile update)
//            case KnownMessageCode.REQUEST_DELETE_CARD:
//                //card deleted.
//                LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(Constants.ACTION_RELOAD_CARDS));
//                break;
//            default:
////                AppLogger.e(TAG, "Other events: Not handling");
//        }
//    }
//
//    @Override
//    public void onComplete() {
//        Toast.makeText(this, "Completed processing message", Toast.LENGTH_SHORT).show();
//    }
//
//    private void retryProvisioning() {
////        AppLogger.d(TAG, "AppPushServiceListener ->  attempting to retry job ");
//        ProvisioningBusinessService provBs = ProvisioningServiceManager.getProvisioningBusinessService();
//        if (provBs != null) {
//            try {
//                provBs.retrySession(this);
//            } catch (ExistingRetrySessionException e) {
//                e.printStackTrace();
//            } catch (NoSessionException e) {
//                e.printStackTrace();
//            }
//        }
//    }

//    public MGConfigurationChangeReceiver getConfigurationChangeReceiver() {
//        return configurationChangeReceiver;
//    }
//
//    public void setConfigurationChangeReceiver(MGConfigurationChangeReceiver configurationChangeReceiver) {
//        this.configurationChangeReceiver = configurationChangeReceiver;
//    }
//
//    public long getPaymentStartTime() {
//        return paymentStartTime;
//    }
}
