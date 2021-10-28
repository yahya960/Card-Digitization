package com.obdx.meezan.THSCard;

import android.annotation.SuppressLint;
import android.app.Application;
import android.os.AsyncTask;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.gemalto.mfs.mwsdk.cdcvm.BiometricsSupport;
import com.gemalto.mfs.mwsdk.cdcvm.DeviceCVMEligibilityChecker;
import com.gemalto.mfs.mwsdk.cdcvm.DeviceCVMEligibilityResult;
import com.gemalto.mfs.mwsdk.cdcvm.DeviceKeyguardSupport;
import com.gemalto.mfs.mwsdk.dcm.AbstractWalletPinService;
import com.gemalto.mfs.mwsdk.dcm.DigitalizedCard;
import com.gemalto.mfs.mwsdk.dcm.DigitalizedCardDetails;
import com.gemalto.mfs.mwsdk.dcm.DigitalizedCardErrorCodes;
import com.gemalto.mfs.mwsdk.dcm.DigitalizedCardManager;
import com.gemalto.mfs.mwsdk.dcm.PaymentType;
import com.gemalto.mfs.mwsdk.dcm.WalletPinEventListener;
import com.gemalto.mfs.mwsdk.dcm.WalletPinManager;
import com.gemalto.mfs.mwsdk.dcm.cdcvm.DeviceCVMManager;
import com.gemalto.mfs.mwsdk.dcm.exception.WalletPinException;
import com.gemalto.mfs.mwsdk.exception.DeviceCVMException;
import com.gemalto.mfs.mwsdk.mobilegateway.MGCardEnrollmentService;
import com.gemalto.mfs.mwsdk.mobilegateway.MobileGatewayError;
import com.gemalto.mfs.mwsdk.mobilegateway.MobileGatewayManager;
import com.gemalto.mfs.mwsdk.mobilegateway.enrollment.InputMethod;
import com.gemalto.mfs.mwsdk.mobilegateway.enrollment.IssuerData;
import com.gemalto.mfs.mwsdk.mobilegateway.enrollment.TermsAndConditionSession;
import com.gemalto.mfs.mwsdk.mobilegateway.enrollment.TermsAndConditions;
import com.gemalto.mfs.mwsdk.mobilegateway.listener.CardEligibilityListener;
import com.gemalto.mfs.mwsdk.mobilegateway.utils.MGCardInfoEncryptor;
import com.gemalto.mfs.mwsdk.payment.CHVerificationMethod;
import com.gemalto.mfs.mwsdk.provisioning.ProvisioningServiceManager;
import com.gemalto.mfs.mwsdk.provisioning.model.EnrollmentStatus;
import com.gemalto.mfs.mwsdk.provisioning.sdkconfig.EnrollingBusinessService;
import com.gemalto.mfs.mwsdk.provisioning.sdkconfig.ProvisioningBusinessService;
import com.gemalto.mfs.mwsdk.utils.async.AbstractAsyncHandler;
import com.gemalto.mfs.mwsdk.utils.async.AsyncResult;
import com.gemalto.mfs.mwsdk.utils.chcodeverifier.CHCodeVerifier;
import com.google.firebase.iid.FirebaseInstanceId;
import com.obdx.meezan.MainActivity;
import com.obdx.meezan.THSCard.history.HistoryActivity;
import com.obdx.meezan.THSCard.model.MyDigitalCard;
import com.obdx.meezan.THSCard.util.TokenReplenishmentRequestor;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;


//import com.obdx.meezan.THSCard.payment.contactless.ContactlessPayListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * This class echoes a string called from JavaScript.
 */
public class THSCard extends CordovaPlugin implements CardEligibilityListener {
    private static final int NAME = 0;
    private static final int KEY = 1;
    private static final int VALUE = 2;

    private static final String MISSING_KEY = "Missing key";
    private static final String FAILED_TO_WRITE = "Failed to write";
    public static final int STORAGE_COMPONENT_ERROR = 1012;
    public static final String STORAGE_COMPONENT_EXCEPTION_KEY ="SECURE_STORAGE_ERROR";
    private View key_pad_fragment;

    public static CallbackContext callbackObject;
//    private ContactlessPayListener contactlessPayListener;
    private static TermsAndConditions termsAndConditions;

    private static final String PUBLIC_KEY_USED="30820122300d06092a864886f70d01010105000382010f003082010a0282010100c56e1e17bda05f906b0f3a6c61464c12464102c2c5e70fce5aa6a0d382e0b0bc83d3d91330b55469c7f6d83dda8d4649a2290c144aea781ea1b3fed5658cd57e49b7f2a87eabfb7ff89f0aa49cb16d1fca3409e48215e23510d5112aae21fe2f2e9d4c28bde41c6ff7fcb92ba90c1c3d2ddca19e2d91db54f5c8b4b0d5747c995b9603fb4d0f8db204f1a86d7732065c631275df13bc5fb949daf4c3e0dbdc79f9ae6bf04bf3f981d1792e1311ae3b244e1983be9b53969cbc2753782d081f8e2f4864e46c4555f4658843a54e7c6589f9c9d4b961db767ed8f5c0273fc18aac13f3f341262378423c2d7c9360ba1d39aea02688583516cc864484d17bf6a4430203010001";
    private static final String SUBJECT_IDENTIFIER_USED="022d11dfdbba94d85fe4cc97e46c71828d969c0f";
    final List<MyDigitalCard> totalCards =  new ArrayList<>();
    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("coolMethod")) {
            String message = args.getString(0);
            this.coolMethod(message, callbackContext);
            return true;
        }

        if (action.equals("initMGSDK")) {
            cordova.getThreadPool().execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        try {
                            Context context = cordova.getActivity().getApplicationContext();
                            SDKHelper.initSDKs(context, callbackContext);
                            return;
                        } catch (Exception e) {
                            callbackContext.error(e.getMessage());
                        }
                        callbackContext.error("failed to execute");
                    } catch (Exception e) {
                        callbackContext.error(e.getMessage());
                    }
                }
            });

            return true;
        }

        if (action.equals("addCard")) {
            cordova.getThreadPool().execute(new Runnable() {
                @Override
                public void run() {
                    try {

                        String cardNumber = args.getString(1);
                        String expiry = args.getString(2);
                        String cvv = args.getString(3);

                        THSCard.callbackObject = callbackContext;
                        boolean success = addCard(cardNumber, expiry, cvv);
//                        loadCards(callbackObject);

                        if (success) {
//                            callbackContext.success("card is eligible");
                            return;
                        }

                        callbackContext.error("failed to execute");
                    } catch (Exception e) {
                        callbackContext.error(e.getMessage());
                    }
                }
            });

            return true;
        }

        if (action.equals("acceptterms")) {
            cordova.getThreadPool().execute(new Runnable() {
                @Override
                public void run() {
                    try {

                        THSCard.callbackObject = callbackContext;
                        Context context = cordova.getActivity().getApplicationContext();
                        proceedDigitize(context);
                        return;

//                        if (success) {
//                            callbackContext.success("card is digitized");
//                        }

//                        callbackContext.error("failed to execute");
                    } catch (Exception e) {
                        callbackContext.error(e.getMessage());
                    }
                }
            });

            return true;
        }

        if (action.equals("cardLoad")) {
            cordova.getThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                THSCard.callbackObject = callbackContext;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    cordova.getActivity().getMainExecutor().execute(() -> {
                        loadCards(callbackContext);
                    });
                }
            }
            });

////            loadCards(callbackContext);
//            THSCard.callbackObject = callbackContext;
//            AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
//                @Override
//                protected void onPostExecute(Void unused) {
//                    if (totalCards.size() != 0) {
//                        callbackContext.success(totalCards.size());
//                    }
//                }
//
//                @Override
//                protected Void doInBackground(Void... voids) {
//                    loadCards(callbackContext);
//                    return null;
//                }
//
//            };
////                    boolean success =;
//            task.execute();
return true;
        }
        if (action.equals("getTransactionHistory")) {
            cordova.getThreadPool().execute(new Runnable() {
                @Override
                public void run() {
                    try {
//                        HistoryActivity historyActivity = new HistoryActivity(to)

                        return;

//                        if (success) {
//                            callbackContext.success("card is digitized");
//                        }

//                        callbackContext.error("failed to execute");
                    } catch (Exception e) {
                        callbackContext.error(e.getMessage());
                    }
                }
            });

            return true;
        }
        return true;
    }





    private void coolMethod(String message, CallbackContext callbackContext) {
        if (message != null && message.length() > 0) {
            callbackContext.success(message);
        } else {
            callbackContext.error("Expected one non-empty string argument.");
        }
    }

    private void proceedDigitize(Context context) {
        MGCardEnrollmentService enrollmentService  = MobileGatewayManager.INSTANCE.getCardEnrollmentService();
        TermsAndConditionSession tcs = THSCard.termsAndConditions.accept();
        Log.i("tcs", tcs.toString());
        CardDigitize cd = new CardDigitize(context);
        enrollmentService.digitizeCard(tcs, null, cd);
    }

    @Override
    public void onSuccess(TermsAndConditions termsAndConditions, IssuerData issuerData) {
        THSCard.termsAndConditions = termsAndConditions;
        THSCard.callbackObject.success(termsAndConditions.getText());
        Activity activity = cordova.getActivity();
        proceedDigitize(activity);
    }

    @Override
    public void onError(MobileGatewayError mobileGatewayError) {
        THSCard.callbackObject.error(mobileGatewayError.getMessage());
    }

    public boolean addCard(String cardNumber, String expiry, String cvv) {

        byte[] pubKeyBytes = MGCardInfoEncryptor.parseHex(THSCard.PUBLIC_KEY_USED);
        byte[] subKeyBytes = MGCardInfoEncryptor.parseHex(THSCard.SUBJECT_IDENTIFIER_USED);
        byte[] panBytes = cardNumber.trim().replace(" ", "").getBytes();
        byte[] expBytes = expiry.getBytes();
        byte[] cvvBytes = cvv.getBytes();
        byte[] encData = MGCardInfoEncryptor.encrypt(pubKeyBytes, subKeyBytes, panBytes, expBytes, cvvBytes);

        MGCardEnrollmentService enrollmentService = MobileGatewayManager.INSTANCE.getCardEnrollmentService();

        //InputMethod.BANK_APP is required for GreenFlow
        enrollmentService.checkCardEligibility(encData, InputMethod.MANUAL, "en", this, getDeviceSerial());
        return true;
    }

    private String getDeviceSerial() {
        return Settings.Secure.getString(cordova.getActivity().getContentResolver(), Settings.Secure.ANDROID_ID);
    }


    private void loadCards(CallbackContext callbackContext) {
        DigitalizedCardManager.getAllCards(new AbstractAsyncHandler<String[]>() {
            @Override
            public void onComplete(AsyncResult<String[]> asyncResult) {
//                swipeRefreshLayout.setRefreshing(false);
//                toggleProgress(false);
                List<MyDigitalCard> cards = new ArrayList<>();
                if (asyncResult.isSuccessful()) {
                    List<DigitalizedCard> allCards = new ArrayList<>();
                    for (String token : asyncResult.getResult()) {
                        allCards.add(DigitalizedCardManager.getDigitalizedCard(token));
                    }

                    List<MyDigitalCard> mCards = new ArrayList<>();
                    for (DigitalizedCard card : allCards) {
                        MyDigitalCard mCard = new MyDigitalCard(card);
                        mCard.setDigitalizedCardId(DigitalizedCardManager.getDigitalCardId(card.getTokenizedCardID()));

                        //check default
                        mCard.setDefaultCardFlag(card.isDefault(PaymentType.CONTACTLESS, null).waitToComplete().getResult());
                        DigitalizedCardDetails digitalizedCardDetails=card.getCardDetails(null).waitToComplete().getResult();
                        mCard.setRemotePaymentSupported(digitalizedCardDetails.isPaymentTypeSupported(PaymentType.DSRP));
                        //get card status
                        mCard.setCardStatus(card.getCardState(null).waitToComplete().getResult());

                        //Check if the card needs replenishment of tokens
                        TokenReplenishmentRequestor.replenish(mCard.getCardStatus(), card.getTokenizedCardID());

                            mCards.add(mCard);
                    }

//                    DisplayCards displayCards =new DisplayCards(cordova.getContext(),
////                            this,
//                            mCards,
//                            DisplayCards.DisplayType.CARD_LIST_MAIN,callbackContext);
//                    totalCards.add( displayCards.showCards(0));
//                    Log.d("Total Cards",totalCards.toString());
                    THSCard.callbackObject.success(mCards.toString());


//                    findViewById(R.id.no_cards).setVisibility((mCards.size() > 0) ? View.GONE : View.VISIBLE);

//                    recyclerView.setAdapter(new CardListAdapter(
//                            getApplicationContext(),
//                            MainActivity.this,
//                            mCards,
//                            CardListAdapter.DisplayType.CARD_LIST_MAIN));



                } else {
                    int errorCode = asyncResult.getErrorCode();

                    if (STORAGE_COMPONENT_ERROR == errorCode) {

                        HashMap<String, Object> additionalInformation = asyncResult.getAdditionalInformation();

                        if (additionalInformation != null && additionalInformation.size() > 0) {
                            Object additionalObject = additionalInformation.get(STORAGE_COMPONENT_EXCEPTION_KEY);
                            if (additionalObject != null && additionalObject instanceof Exception) {
                                Exception exception = (Exception) additionalObject;
//                                AppLogger.e(TAG, "Get All cards failed because" + exception.getMessage());
                                exception.printStackTrace();
                                //In production app, this event to be sent to Analytics server. Exception stack trace can be sent to analytics, if available.
                                //If exception stack trace is not possible, please send atleast the exception message. (e.getMessage() to analytics
                            }
                        }
                        // the production MPA can retry again instead.
//                        AppLogger.e(TAG, "Failed to reload the card list due to secure storage: " + asyncResult.getErrorMessage());
                        // if issue, persists even after certain number of retries. Recommend to do the following
                        // 1. Send a specific error event that retry failed
                        // 2. SDK APIs cannot be used in this user session anymore. so block all SDK usage from this point onward
                    }

                    else if (errorCode == DigitalizedCardErrorCodes.CD_CVM_REQUIRED) {
//
//                        AppLogger.d(TAG, "CD_CVM_REQUIRED");


                        DeviceCVMEligibilityResult result =
                                DeviceCVMEligibilityChecker.checkDeviceEligibility(cordova.getContext());

                        if (result.getBiometricsSupport() == BiometricsSupport.SUPPORTED) {
                            //to use fingerprint. Be sure to check for device support
                            try {
                                DeviceCVMManager.INSTANCE.initialize(CHVerificationMethod.BIOMETRICS);
                            } catch (DeviceCVMException e) {
                                e.printStackTrace();
                            }
                        } else if (result.getDeviceKeyguardSupport() == DeviceKeyguardSupport.SUPPORTED) {
                            //to use device key guard
                            try {
                                DeviceCVMManager.INSTANCE.initialize(CHVerificationMethod.DEVICE_KEYGUARD);
                            } catch (DeviceCVMException e) {
                                e.printStackTrace();
                            }
                        } else {
                            //throw new RuntimeException("Device not suitable for demo");
                            //Force To use Wallet PIN
//                            enablePin();
                        }
                    }
                }

            }

        });

    }

//    private void enablePin() {
//        WalletPinManager.getInstance().bindAbstractWalletPinService(new AbstractWalletPinService() {
//            @Override
//            public void onSetWalletPin(CHCodeVerifier chCodeVerifier) {
////                key_pad_fragment.setVisibility(View.VISIBLE);
////                fabAdd.hide();
//
//                //To use secure keypad provided use this
//                setupGSK_43(chCodeVerifier, SecureInputType.SET_PIN);
//
//                //To custom input pin, use below.
////                SecureCodeInputer inputer = chCodeVerifier.getSecureCodeInputer();
////                for(byte pinByte : "YOURPIN".getBytes()){
////                    inputer.input(pinByte);
////                }
////                inputer.finish();
//            }
//
//            @Override
//            public void onVerifyWalletPin(CHCodeVerifier chCodeVerifier) {
//
//            }
//
//            @Override
//            public WalletPinEventListener setupListener() {
//                return walletPinEventListener;
//            }
//        });
//        try {
//            WalletPinManager.getInstance().invokeSetWalletPin();
//        } catch (WalletPinException e) {
//            e.printStackTrace();
//        }
//    }





//    public ContactlessPayListener getContactlessPayListener() {
//        return contactlessPayListener;
//    }


}
