package com.obdx.meezan.THSCard;
import android.annotation.SuppressLint;
import android.content.Context;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.gemalto.mfs.mwsdk.dcm.DigitalizedCard;
import com.gemalto.mfs.mwsdk.dcm.DigitalizedCardDetails;
import com.gemalto.mfs.mwsdk.dcm.DigitalizedCardManager;
import com.gemalto.mfs.mwsdk.utils.async.AbstractAsyncHandler;
import com.gemalto.mfs.mwsdk.utils.async.AsyncResult;
import com.obdx.meezan.R;
import com.obdx.meezan.THSCard.model.MyDigitalCard;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.PluginResult;

import java.util.ArrayList;
import java.util.List;

public class DisplayCards {


    public enum DisplayType {
        CARD_LIST_MAIN,
        CARD_LIST_CHOOSER
    }

    private Context context;
    private List<MyDigitalCard> cards;
//    private CardListAdapterCallback callback;
    private final DisplayType displayType;
    private static CallbackContext callbackObject;

    public DisplayCards(Context context, List<MyDigitalCard> cards, final DisplayType displayType,final CallbackContext callbackContext) {
        this.context = context;
        this.cards = cards;
//        this.callback = (CardListAdapterCallback) callback;
        this.displayType = displayType;
        this.callbackObject = callbackContext;
    }

    public MyDigitalCard showCards(int position){

//        MyViewHolder mHolder = (MyViewHolder) holder;
//        final MyDigitalCard call;
//        LMyDigitalCard mCards = new ArrayList<>();
        final MyDigitalCard card = cards.get(position);
        int numberOfPayment = card.getCardStatus().getNumberOfPaymentsLeft();
        DigitalizedCard dCard = DigitalizedCardManager.getDigitalizedCard(card.getTokenId());
//        dCard.getCardDetails(new AbstractAsyncHandler<DigitalizedCardDetails>() {
//            @SuppressLint("HandlerLeak")
//            @Override
//            public void onComplete(AsyncResult<DigitalizedCardDetails> asyncResult) {
//
//                if(asyncResult.isSuccessful()) {
//                    Log.d("callback",callbackObject.getCallbackId());
//                    mCards.add(card);
//                call = new MyDigitalCard(dCard)
//                    final String tvInfoText1 =
//                            "<b>Token ID: </b> <font color='blue'>" + card.getTokenId() + "</font> <br>"
//                                    + "<b>Digital Card ID: </b><font color='blue'>" + card.getDigitalizedCardId() + "</font><br>"
//                                    + "<b>Default: </b><font color='blue'>" + card.isDefaultCardFlag() + "</font><br>"
//                                    + "<b>Card State: </b><font color='blue'>" + card.getCardStatus().getState() + "</font><br>"
//                                    + "<b>Payment Remaining: </b><font color='blue'>" + numberOfPayment + "</font><br>"
//                                    + "<b>PAN Expiry: </b><font color='blue'>" + asyncResult.getResult().getPanExpiry() + "</font><br>"
//                                    + "<b>DPAN LastDigit: </b><font color='blue'>" + asyncResult.getResult().getLastFourDigitsOfDPAN()+"" + "</font><br>"
//                                    +  "<b>FPAN LastDigit: </b><font color='blue'>" + asyncResult.getResult().getLastFourDigits()+"" + "</font><br>";

//                    callbackObject.success(tvInfoText1);
//                    call.setTokenId(card.getTokenId());
//                    call.setDigitalizedCardId(card.getDigitalizedCardId());
//                    call.setDefaultCardFlag(card.isDefaultCardFlag());
//                    call.setCardStatus(card.getCardStatus());


//                    Log.d("Card Details", card.toString());
//                }
//
//            }
//        });
//        callbackObject.success(card.toString());
        return card;
    }

//    private class MyViewHolder extends RecyclerView.ViewHolder {
//        TextView tvInfo;
//        CardView cardPayment;
//        ImageButton imgBtnDelete, imgBtnSetDefault, imgBtnGetHistory,imgPayment;
//        RadioButton radioButton;
//        ImageView btnDsrp;

//        public MyViewHolder(View itemView) {
//            super(itemView);
//            itemView.setTag(this);
//            tvInfo = itemView.findViewById(R.id.tvCardInfo);
//            imgBtnDelete = itemView.findViewById(R.id.imgBtnDelete);
//            imgPayment = itemView.findViewById(R.id.imgBtnPayment);
//            imgBtnSetDefault = itemView.findViewById(R.id.imgBtnSetDefault);
//            imgBtnGetHistory = itemView.findViewById(R.id.imgBtnGetHistory);
//            cardPayment = itemView.findViewById(R.id.card);
//            radioButton = itemView.findViewById(R.id.radioButtonListView);
//            btnDsrp = itemView.findViewById(R.id.btnDsrp);
//        }
//    }

//    public interface CardListAdapterCallback {
//        void onBtnDelete(MyDigitalCard card);
//
//        void onPaymentClicked(MyDigitalCard card);
//
//        void setDefaultCardAction(MyDigitalCard card);
//
//        void onBtnHistoryClicked(MyDigitalCard card);
//
//        void onLongClickCard(MyDigitalCard card);
//
//        void onCheckboxClicked(MyDigitalCard card);
//
//        void onDsrpClicked(MyDigitalCard card);
//    }
}
