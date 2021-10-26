package com.obdx.meezan.THSCard;

public class Constants {
  
  public static int MG_CONNECTION_TIMEOUT = 30000;
  public static int MG_CONNECTION_READ_TIMEOUT = 30000;
  public static int MG_CONNECTION_RETRY_COUNT = 3;
  public static int MG_CONNECTION_RETRY_INTERVAL = 10000;

  public static String WALLET_PROVIDER_ID_USED = "WP_HCESANDBOX";
  public static String MG_CONNECTION_URL_USED = "https://wallet-lab.cbp.gemalto.com/mg";
  public static String MG_TRANSACTION_HISTORY_CONNECTION_URL_USED = "https://wallet-lab.cbp.gemalto.com/mg";

  public static String mg_notification_channel_id = "MG_SDK_NOTIFICATION";
  public static String mg_notification_message = "This notification is posted to run internal operation of mg sdk";
  
  public static String app_name = "helloapp";


  public static final String ACTION_RELOAD_CARDS = "com.gemalto.test.app.ACTION_RELOAD_CARDS";
public static final boolean IS_PFP_ENABLED = false;

}
