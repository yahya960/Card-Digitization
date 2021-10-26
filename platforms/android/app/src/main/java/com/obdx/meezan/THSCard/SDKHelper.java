package com.obdx.meezan.THSCard;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.gemalto.mfs.mwsdk.mobilegateway.MGConfigurationChangeReceiver;
import com.gemalto.mfs.mwsdk.mobilegateway.MGConnectionConfiguration;
import com.gemalto.mfs.mwsdk.mobilegateway.MGSDKConfigurationState;
import com.gemalto.mfs.mwsdk.mobilegateway.MGTransactionHistoryConfiguration;
import com.gemalto.mfs.mwsdk.mobilegateway.MGWalletConfiguration;
import com.gemalto.mfs.mwsdk.mobilegateway.MobileGatewayManager;
import com.gemalto.mfs.mwsdk.mobilegateway.exception.MGConfigurationException;
import com.gemalto.mfs.mwsdk.mobilegateway.exception.MGStorageConfigurationException;
import com.gemalto.mfs.mwsdk.payment.cdcvm.DeviceCVMPreEntryReceiver;
import com.gemalto.mfs.mwsdk.payment.sdkconfig.SDKDataController;
import com.gemalto.mfs.mwsdk.payment.sdkconfig.SDKInitializer;
import com.gemalto.mfs.mwsdk.provisioning.ProvisioningServiceManager;
import com.gemalto.mfs.mwsdk.provisioning.listener.PushServiceListener;
import com.gemalto.mfs.mwsdk.provisioning.listener.WalletSecureEnrollmentListener;
import com.gemalto.mfs.mwsdk.provisioning.model.ProvisioningServiceError;
import com.gemalto.mfs.mwsdk.provisioning.model.ProvisioningServiceMessage;
import com.gemalto.mfs.mwsdk.provisioning.model.WalletSecureEnrollmentError;
import com.gemalto.mfs.mwsdk.provisioning.model.WalletSecureEnrollmentState;
import com.gemalto.mfs.mwsdk.provisioning.sdkconfig.ProvisioningBusinessService;
import com.gemalto.mfs.mwsdk.provisioning.sdkconfig.WalletSecureEnrollmentBusinessService;
import com.gemalto.mfs.mwsdk.sdkconfig.SDKControllerListener;
import com.gemalto.mfs.mwsdk.sdkconfig.SDKError;
import com.gemalto.mfs.mwsdk.sdkconfig.SDKInitializeErrorCode;
import com.gemalto.mfs.mwsdk.sdkconfig.SDKSetupProgressState;
import com.google.firebase.FirebaseApp;
import com.google.firebase.iid.FirebaseInstanceId;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;

import java.io.IOException;

import static com.gemalto.mfs.mwsdk.mobilegateway.MGSDKConfigurationState.NOT_CONFIGURED;

public class SDKHelper extends CordovaPlugin {
    public static final String CPS_SENDER_ID = "188445501380";

    private static SharedPreferences sp;
    private static CallbackContext callbackObject;

    private static DeviceCVMPreEntryReceiver mPreEntryReceiver;

    private static MGConfigurationChangeReceiver configurationChangeReceiver;

    public SDKHelper(){

    }

    public static void initMGSDKCall(final Context context) {
        //init MG SDK
        try {
            SDKHelper.initMGSDK(context);
        } catch (MGConfigurationException e) {

        }
        //set up sync between MG and CPS
        SDKHelper.setConfigurationChangeReceiver(new MGConfigurationChangeReceiver());
        LocalBroadcastManager.getInstance(context).registerReceiver(SDKHelper.getConfigurationChangeReceiver(),
                new IntentFilter("com.gemalto.mfs.action.MGConfigurationChanged"));
    }

    public static String initMGSDK(Context context) throws MGConfigurationException {
        MGSDKConfigurationState configurationState = MobileGatewayManager.INSTANCE.getConfigurationState();
        if (configurationState != NOT_CONFIGURED) {
            return "Configured Already";
        }

        //Configure MG configuration
        MGConnectionConfiguration connectionConfiguration = new MGConnectionConfiguration
                .Builder()
                .setConnectionParameters(com.obdx.meezan.THSCard.Constants.MG_CONNECTION_URL_USED,
                        com.obdx.meezan.THSCard.Constants.MG_CONNECTION_TIMEOUT,
                        com.obdx.meezan.THSCard.Constants.MG_CONNECTION_READ_TIMEOUT)
                .setRetryParameters(com.obdx.meezan.THSCard.Constants.MG_CONNECTION_RETRY_COUNT,
                        com.obdx.meezan.THSCard.Constants.MG_CONNECTION_RETRY_INTERVAL)
                .build();

        //Configure wallet configuration
        MGWalletConfiguration walletConfiguration = new MGWalletConfiguration
                .Builder()
                .setWalletParameters(com.obdx.meezan.THSCard.Constants.WALLET_PROVIDER_ID_USED)
                .setNotification(com.obdx.meezan.THSCard.NotificationUtil.getNotification(context,
                        com.obdx.meezan.THSCard.Constants.mg_notification_message,
                        com.obdx.meezan.THSCard.Constants.mg_notification_channel_id))
                .build();

        //Configure Transaction History
        MGTransactionHistoryConfiguration transactionConfiguration = new MGTransactionHistoryConfiguration
                .Builder()
                .setConnectionParameters(
                        com.obdx.meezan.THSCard.Constants.MG_TRANSACTION_HISTORY_CONNECTION_URL_USED)
                .build();
        try {
            if (configurationState == NOT_CONFIGURED) {
                MobileGatewayManager.INSTANCE.configure(context, connectionConfiguration
                        , walletConfiguration, transactionConfiguration);
                return "Configured Successfully";
            }
            return "Configured Already";
        } catch (MGStorageConfigurationException e) {
            // AppLogger.e("MG Config", "", e);
            throw e;
        } catch (MGConfigurationException exception) {
            // AppLogger.e("MG Config", "MG MGConfigurationException " + exception.getLocalizedMessage(), exception);
            throw exception;
        }
        // AppLogger.d("MG Config", "MG Configuration initialised");
    }

    public static void initCPSSDK(final Context context, final InitCPSSDKCallback initCPSSDKCallback, final CallbackContext callbackContext) {


        SDKControllerListener sdkControllerListener = SDKHelper.createSDKControllerListenerObject(context,
                initCPSSDKCallback,
                callbackContext);

        SDKInitializer.INSTANCE.initialize(context, sdkControllerListener,
                com.obdx.meezan.THSCard.NotificationUtil.getNotification(context,
                        "This notification is posted to run internal operation of cps sdk",
                        "CPS_SDK_NOTIFICATION"));
    }

    private static SDKControllerListener createSDKControllerListenerObject(final Context context, final InitCPSSDKCallback initCPSSDKCallback, final boolean isUIUpdateNeeded, final boolean isRetry) {
        return new SDKControllerListener() {
            @Override
            public void onError(SDKError<SDKInitializeErrorCode> initializeError) {

                if (initializeError.getErrorCode() == SDKInitializeErrorCode.SDK_INITIALIZED) {
//                    AppLogger.e("Service", "SDK already initialized ");
                    // There is Snackbar used on the MainActivity to indicate the init result
                    //Toast.makeText(context.getApplicationContext(), "Initialization completed", Toast.LENGTH_LONG).show();
//                    broadcastInitComplete(context, isUIUpdateNeeded);
                    //This is for registering Pre-Entry Receiver and trigger wallet Secure Enrollment flow and must be done only after initialization is completed
                    if (initCPSSDKCallback != null) {
                        initCPSSDKCallback.doAction();
                    }

                } else if (SDKInitializeErrorCode.SDK_INITIALIZING_IN_PROGRESS == initializeError.getErrorCode()) {
                    //Ignore as initialization is happening elsewhere
                } else if (SDKInitializeErrorCode.INTERNAL_COMPONENT_ERROR == initializeError.getErrorCode() ||
                        SDKInitializeErrorCode.SDK_INIT_FAILED == initializeError.getErrorCode() ||
                        SDKInitializeErrorCode.STORAGE_COMPONENT_ERROR == initializeError.getErrorCode() ||
                        SDKInitializeErrorCode.INVALID_PREVIOUS_VERSION == initializeError.getErrorCode() ||
                        SDKInitializeErrorCode.ASM_INIT_ERROR == initializeError.getErrorCode() ||
                        SDKInitializeErrorCode.ASM_MIGRATION_ERROR == initializeError.getErrorCode()) {
//                    AppLogger.e("Service", "Initialization failed" + initializeError.getErrorCode().name());
//                    AppLogger.e("Service", "Initialization failed" + initializeError.getErrorMessage());

                    if (isRetry) {
                        retryInitialization(context, initCPSSDKCallback, isUIUpdateNeeded);
                    } else {
                        //This is already retry....No need to retry again.... Ensure SDK APIs are not used after this point.
                        //In this training app, we close the application.

                        // broadcastInitFailed(context);
                        try {
                            SDKDataController.INSTANCE.wipeAll(context);
                            new Handler().postDelayed(() -> retryInitialization(context, initCPSSDKCallback, isUIUpdateNeeded), 2500);
                        } catch (Exception e) {
//                            AppLogger.e(TAG, e.getMessage());
                        }
                    }

                } else {
//                    AppLogger.e("Service", "Initialization failed");
//
//                    // There is Snackbar used on the MainActivity to indicate the init result
//                    // Toast.makeText(context.getApplicationContext(), "Initialization failed", Toast.LENGTH_LONG).show();
//                    broadcastInitFailed(context);
                }
            }

            @Override
            public void onSetupProgress(SDKSetupProgressState sdkSetupProgressState, String s) {
//                AppLogger.d("Service", "onSetupProgress completed");
            }

            @Override
            public void onSetupComplete() {

//                AppLogger.d(AppConstants.APP_TAG, "Initialization " + AppConstants.ENDED);
                // There is Snackbar used on the MainActivity to indicate the init result
                // Toast.makeText(context.getApplicationContext(), "Initialization completed", Toast.LENGTH_LONG).show();
//                broadcastInitComplete(context, isUIUpdateNeeded);
                //This is for registering Pre-Entry Receiver and must be done only after initialization is completed
                if (initCPSSDKCallback != null) {
                    initCPSSDKCallback.doAction();
                }

            }
        };
    }
    private static void retryInitialization(Context context, InitCPSSDKCallback initCPSSDKCallback, boolean isUIUpdateNeeded) {
        SDKControllerListener sdkControllerListener = createSDKControllerListenerObject(context, initCPSSDKCallback, isUIUpdateNeeded, false);
//        AppLogger.d(AppConstants.APP_TAG, "Retry Initialization " + AppConstants.STARTED);
        SDKInitializer.INSTANCE.initialize(context, sdkControllerListener,
                NotificationUtil.getNotification(context,
                        "This notification is posted to run internal operation of cps sdk",
                        "CPS_SDK_NOTIFICATION"));
    }

    public static void initCPSSDK(final Context context, final InitCPSSDKCallback initCPSSDKCallback, final boolean isUIUpdateNeeded) {


        SDKControllerListener sdkControllerListener = createSDKControllerListenerObject(context,
                initCPSSDKCallback,
                isUIUpdateNeeded,
                true);

//        AppLogger.d(AppConstants.APP_TAG, "Initialization " + AppConstants.STARTED);
        SDKInitializer.INSTANCE.initialize(context, sdkControllerListener,
                NotificationUtil.getNotification(context,
                        "This notification is posted to run internal operation of cps sdk",
                       "CPS_SDK_NOTIFICATION"));
    }

    private static SDKControllerListener createSDKControllerListenerObject(final Context context, final InitCPSSDKCallback initCPSSDKCallback, final CallbackContext callbackContext) {
        return new SDKControllerListener() {
            @Override
            public void onError(SDKError<SDKInitializeErrorCode> initializeError) {
                callbackContext.error(initializeError.getErrorMessage());
                if (initializeError.getErrorCode() == SDKInitializeErrorCode.SDK_INITIALIZED) {
                    // There is Snackbar used on the MainActivity to indicate the init result
                    //This is for registering Pre-Entry Receiver and trigger wallet Secure Enrollment flow and must be done only after initialization is completed
                    if (initCPSSDKCallback != null) {
                        initCPSSDKCallback.doAction();
                    }

                } else if (SDKInitializeErrorCode.SDK_INITIALIZING_IN_PROGRESS == initializeError.getErrorCode()) {
                    //Ignore as initialization is happening elsewhere
                } else if (SDKInitializeErrorCode.INTERNAL_COMPONENT_ERROR == initializeError.getErrorCode() ||
                        SDKInitializeErrorCode.SDK_INIT_FAILED == initializeError.getErrorCode() ||
                        SDKInitializeErrorCode.STORAGE_COMPONENT_ERROR == initializeError.getErrorCode() ||
                        SDKInitializeErrorCode.INVALID_PREVIOUS_VERSION == initializeError.getErrorCode() ||
                        SDKInitializeErrorCode.ASM_INIT_ERROR == initializeError.getErrorCode() ||
                        SDKInitializeErrorCode.ASM_MIGRATION_ERROR == initializeError.getErrorCode()) {

                } else {

                    // There is Snackbar used on the MainActivity to indicate the init result
                }
            }

            @Override
            public void onSetupProgress(SDKSetupProgressState sdkSetupProgressState, String s) {
//                callbackContext.success("onSetupProgress");
            }

            @Override
            public void onSetupComplete() {
//                callbackContext.success("onSetupComplete");
                // There is Snackbar used on the MainActivity to indicate the init result
                //This is for registering Pre-Entry Receiver and must be done only after initialization is completed
                if (initCPSSDKCallback != null) {
                    initCPSSDKCallback.doAction();
                }

            }
        };
    }

    public interface InitCPSSDKCallback {
        void doAction();
    }

    public static void performWalletSecureEnrollmentFlow(final Context context, final CallbackContext callbackContext) {

        WalletSecureEnrollmentBusinessService wseService = ProvisioningServiceManager.getWalletSecureEnrollmentBusinessService();

        WalletSecureEnrollmentListener listener = new WalletSecureEnrollmentListener() {
            @Override
            public void onProgressUpdate(WalletSecureEnrollmentState wseState) {
                if (wseState == WalletSecureEnrollmentState.WSE_COMPLETED) {
                    //completed ready to go to cardlist
                    callbackContext.success(wseState.toString());
                }
            }

            @Override
            public void onError(WalletSecureEnrollmentError wbDynamicKeyRenewalServiceError) {
                callbackContext.error(
                        " -->HttpStatusCode: "+wbDynamicKeyRenewalServiceError.getHttpStatusCode() +
                                " -->ErrorMessage: " + wbDynamicKeyRenewalServiceError.getErrorMessage() +
                                " -->toString: " + wbDynamicKeyRenewalServiceError.toString() +
                                " -->CpsErrorCode: " + wbDynamicKeyRenewalServiceError.getCpsErrorCode() +
                                " -->SdkErrorCode: " + wbDynamicKeyRenewalServiceError.getSdkErrorCode()
                );
            }
        };
        //get State
        WalletSecureEnrollmentState state = wseService.getState();
        if (state == WalletSecureEnrollmentState.WSE_REQUIRED) {
            //call renewal
            wseService.startWalletSecureEnrollment(listener);
        } else if (state == WalletSecureEnrollmentState.WSE_COMPLETED || state == WalletSecureEnrollmentState.WSE_NOT_REQUIRED) {
            callbackContext.success(state.toString());
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {

                }
            });
        }
    }

    public static void performWalletSecureEnrollmentFlow(final Context context) {

//        AppLogger.d(AppConstants.APP_TAG, "performWalletSecureEnrollmentFlow" + AppConstants.STARTED);
        WalletSecureEnrollmentBusinessService wseService = ProvisioningServiceManager.getWalletSecureEnrollmentBusinessService();

        //get State
        WalletSecureEnrollmentState state = wseService.getState();
        if (state == WalletSecureEnrollmentState.WSE_REQUIRED) {
            //call renewal
            wseService.startWalletSecureEnrollment(new WalletSecureEnrollmentListener() {
                @Override
                public void onProgressUpdate(WalletSecureEnrollmentState wseState) {
//                    AppLogger.d(AppConstants.APP_TAG, "performWalletSecureEnrollmentFlow - onProgressUpdate: " + wseState);

//                    Toast.makeText(context, "performWalletSecureEnrollmentFlow - " + wseState,
//                            Toast.LENGTH_SHORT).show();

                    if (wseState == WalletSecureEnrollmentState.WSE_COMPLETED) {
                        //completed ready to go to cardlist

//                        Toast.makeText(context, "performWalletSecureEnrollmentFlow - WSE_COMPLETED",
//                                Toast.LENGTH_SHORT).show();
//                        broadcastInitComplete(context, true);
                    }
                }

                @Override
                public void onError(WalletSecureEnrollmentError wbDynamicKeyRenewalServiceError) {
//                    AppLogger.d(AppConstants.APP_TAG, "performWalletSecureEnrollmentFlow onError cps error code: " + wbDynamicKeyRenewalServiceError.getCpsErrorCode());
//                    AppLogger.d(AppConstants.APP_TAG, "performWalletSecureEnrollmentFlow onError cps error code: " + wbDynamicKeyRenewalServiceError.getSdkErrorCode());
//                    AppLogger.d(AppConstants.APP_TAG, "performWalletSecureEnrollmentFlow onError message: " + wbDynamicKeyRenewalServiceError.getErrorMessage());

                    //display to user, cant enroll
//                    Toast.makeText(context, "performWalletSecureEnrollmentFlow - onError: " + wbDynamicKeyRenewalServiceError.getErrorMessage(),
//                            Toast.LENGTH_SHORT).show();
//                    broadcastInitComplete(context, true);
                }
            });
        } else if (state == WalletSecureEnrollmentState.WSE_COMPLETED || state == WalletSecureEnrollmentState.WSE_NOT_REQUIRED) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
//                    Toast.makeText(context, "performWalletSecureEnrollmentFlow: " + state,
//                            Toast.LENGTH_SHORT).show();
//                    broadcastInitComplete(context, true);
                }
            });
        }
    }

    private static void registerPreFpEntry(final Context context) {
        IntentFilter filter = new IntentFilter(Intent.ACTION_USER_PRESENT);
        mPreEntryReceiver = new DeviceCVMPreEntryReceiver();
        mPreEntryReceiver.init();
        ContextWrapper cw = new ContextWrapper(context);
        cw.registerReceiver(mPreEntryReceiver, filter);
    }

    public static void initSDKs(final Context context, final CallbackContext callbackContext) {
        Thread initThread=new Thread(() -> {
            SDKHelper.InitCPSSDKCallback initCPSSDKCallback = new SDKHelper.InitCPSSDKCallback() {
                @Override
                public void doAction() {
                    //init MG SDK
                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            //Firebase API has limitation when there are multiple sender ID, the onNewToken is triggered only for default SENDER_ID.
                            // So it is prudent to check for updatePushToken regularly after SDK initialization as well.
                            //And it is prudent to check for updatePushToken just before card enrollment process begin as well.
                            SDKHelper.updateFirebaseToken(context);
                            SDKHelper.initMGSDKCall(context);
                            SDKHelper.performWalletSecureEnrollmentFlow(context, callbackContext);
                            SDKHelper.registerPreFpEntry(context);
                        }
                    },400);
                }
            };
            FirebaseApp.initializeApp(context);
            SDKHelper.initCPSSDK(context,initCPSSDKCallback,callbackContext);
        });
        initThread.start();
    }

    public static void updateFirebaseToken(Context context){
        try {
            String cpsFirebaseToken = FirebaseInstanceId.getInstance().getToken(CPS_SENDER_ID, "FCM");
            Log.i("cpsFirebaseToken","cpsFirebaseToken=-2=3-=3-2=-3=2-3=2-3=02=03=23-=294=9=4249>> "+cpsFirebaseToken);
            if (cpsFirebaseToken == null) {
            } else {
                if (SDKHelper.isCPSFirebaseTokenChanged(context, cpsFirebaseToken)) {
                    ProvisioningBusinessService provisioningBusinessService = ProvisioningServiceManager.getProvisioningBusinessService();
                    provisioningBusinessService.updatePushToken(cpsFirebaseToken, new PushServiceListener() {
                        @Override
                        public void onError(ProvisioningServiceError provisioningServiceError) {
                            Log.i("cpsFirebaseToken",provisioningServiceError.getErrorMessage());
                        }

                        @Override
                        public void onUnsupportedPushContent(Bundle bundle) {
                        }

                        @Override
                        public void onServerMessage(String s, ProvisioningServiceMessage provisioningServiceMessage) {
                        }

                        @Override
                        public void onComplete() {
                            SharedPreferenceUtils.saveCPSFirebaseToken(context, cpsFirebaseToken);
                        }
                    });
                } else {
                }
            }
        }catch (IOException e ){

        } catch (Exception e) {
        }
    }

    private static boolean isCPSFirebaseTokenChanged(Context context,String newCPSFirebaseToken) {
        String existingCPSFirebaseToken = SDKHelper.getCPSFirebaseToken(context);
        if(existingCPSFirebaseToken==null || existingCPSFirebaseToken.isEmpty()){
            return true;
        }
        if(existingCPSFirebaseToken.equalsIgnoreCase(newCPSFirebaseToken)){
            return false;
        }
        return true;
    }

    public static String getCPSFirebaseToken(Context context) {
        sp = context.getApplicationContext().getSharedPreferences("default_card_table_name", AppCompatActivity.MODE_PRIVATE);
        String cpsFirebaseToken = sp.getString("firebase_token_key", null);
        return cpsFirebaseToken;
    }

    public static MGConfigurationChangeReceiver getConfigurationChangeReceiver() {
        return configurationChangeReceiver;
    }

    public static void setConfigurationChangeReceiver(MGConfigurationChangeReceiver configurationChangeReceiver) {
        SDKHelper.configurationChangeReceiver = configurationChangeReceiver;
    }

}
