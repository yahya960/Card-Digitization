package com.obdx.meezan.THSCard;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.gemalto.mfs.mwsdk.mobilegateway.MobileGatewayError;
import com.gemalto.mfs.mwsdk.mobilegateway.MobileGatewayManager;
import com.gemalto.mfs.mwsdk.mobilegateway.enrollment.IDVMethod;
import com.gemalto.mfs.mwsdk.mobilegateway.enrollment.IDVMethodSelector;
import com.gemalto.mfs.mwsdk.mobilegateway.enrollment.PendingCardActivation;
import com.gemalto.mfs.mwsdk.mobilegateway.listener.MGDigitizationListener;
import com.gemalto.mfs.mwsdk.provisioning.ProvisioningServiceManager;
import com.gemalto.mfs.mwsdk.provisioning.listener.EnrollingServiceListener;
import com.gemalto.mfs.mwsdk.provisioning.model.EnrollmentStatus;
import com.gemalto.mfs.mwsdk.provisioning.model.ProvisioningServiceError;
import com.gemalto.mfs.mwsdk.provisioning.sdkconfig.EnrollingBusinessService;
import com.gemalto.mfs.mwsdk.provisioning.sdkconfig.ProvisioningBusinessService;
import com.gemalto.mfs.mwsdk.utils.chcodeverifier.CHCodeVerifier;
import com.gemalto.mfs.mwsdk.utils.chcodeverifier.SecureCodeInputer;
import com.google.firebase.FirebaseApp;
import com.google.firebase.iid.FirebaseInstanceId;

public class CardDigitize implements MGDigitizationListener, EnrollingServiceListener {

    private static SharedPreferences sp;
    private static final String FIREBASE_ID = "firebase_id";
    byte[] activationCode;
    public Context context;

    public CardDigitize(Context context){
        this.context = context;
    }

    @Override
    public void onCPSActivationCodeAcquired(String id, byte[] code) {
        String firebaseToken = "";
        if(!TextUtils.isEmpty(CardDigitize.getFirebaseId(this.context))){
            firebaseToken = CardDigitize.getFirebaseId(this.context);
        }else{
            firebaseToken = FirebaseInstanceId.getInstance().getToken();
        }

        if (TextUtils.isEmpty(firebaseToken)) {
            throw new RuntimeException("Firebase token is null ");
        }
        EnrollingBusinessService enrollingService = ProvisioningServiceManager.getEnrollingBusinessService();
        ProvisioningBusinessService provisioningBusinessService = ProvisioningServiceManager.getProvisioningBusinessService();

        this.activationCode = new byte[code.length];
        for (int i = 0; i < code.length; i++) {
            activationCode[i] = code[i];
        }

        //WalletID of MG SDK is userID of CPS SDK Enrollment process
        String userId = MobileGatewayManager.INSTANCE.getCardEnrollmentService().getWalletId();

        EnrollmentStatus status = enrollingService.isEnrolled();
        Log.i("CardDigitize","FireBaseToken is "+ firebaseToken);
        Log.i("CardDigitize","EnrollmentStatus is "+ status);
        switch (status) {
            case ENROLLMENT_NEEDED:
                enrollingService.enroll(userId, firebaseToken, "en", this);
                break;
            case ENROLLMENT_IN_PROGRESS:
                enrollingService.continueEnrollment("en", this);
                break;
            case ENROLLMENT_COMPLETE:
                provisioningBusinessService.sendActivationCode(this);
                break;
        }


    }

    public static String getFirebaseId(Context context){
        sp = context.getSharedPreferences(FIREBASE_ID, AppCompatActivity.MODE_PRIVATE);
        String fireBaseId = sp.getString(FIREBASE_ID, "" );
        return fireBaseId;
    }

    @Override
    public void onSelectIDVMethod(IDVMethodSelector idvMethodSelector) {
        //For demo purpose, we skip and select the first one
        IDVMethod firstMethod = idvMethodSelector.getIdvMethodList()[0];
        idvMethodSelector.select(firstMethod.getId());
    }

    @Override
    public void onActivationRequired(PendingCardActivation pendingCardActivation) {
//        toggleProgress(false);
//        Toast.makeText(this, ".onActivationRequired() :", Toast.LENGTH_SHORT).show();
//        if (pendingCardActivation.getState() == PendingCardActivationState.WEB_3DS_NEEDED) {
//            ThreeDSFragment frag = new ThreeDSFragment();
//            frag.pendingCardActivation = pendingCardActivation;
//            switchFragment(frag, true);
//        } else if(pendingCardActivation.getState() == PendingCardActivationState.OTP_NEEDED){
//            //OTP is hardcoded one.
//            pendingCardActivation.activate(Constants.OTP_YELLOW_FLOW.getBytes(), this);
//        }
        THSCard.callbackObject.error(pendingCardActivation.getState().toString());
    }

    @Override
    public void onComplete(final String s) {
//        new Handler(Looper.getMainLooper()).(new Runnable() {
//            @Override
//            public void run() {
//                THSCard.callbackObject.success("Card Digitized Token: " + s);
//            }
//        });
        Log.d("Card Digitize", s);
    }

    @Override
    public void onError(String s, final MobileGatewayError mobileGatewayError) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                THSCard.callbackObject.error(mobileGatewayError.getMessage());
            }
        });
    }

    @Override
    public void onCodeRequired(CHCodeVerifier chCodeVerifier) {
        SecureCodeInputer inputer = chCodeVerifier.getSecureCodeInputer();
        for (byte i : activationCode) {
            inputer.input(i);
        }
        inputer.finish();

        //wipe after use
        for (int i = 0; i < activationCode.length; i++) {
            activationCode[i] = 0;
        }
    }

    @Override
    public void onStarted() {

    }

    @Override
    public void onError(ProvisioningServiceError provisioningServiceError) {
        THSCard.callbackObject.error(provisioningServiceError.getErrorMessage());
    }

    @Override
    public void onComplete() {

    }
}
