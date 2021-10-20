package com.example.merchanttestsdkap;


import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.example.merchanttestsdkap.managers.SettingsManager;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;

import company.tap.gosellapi.GoSellSDK;
import company.tap.gosellapi.internal.api.callbacks.GoSellError;
import company.tap.gosellapi.internal.api.models.Authorize;
import company.tap.gosellapi.internal.api.models.Charge;
import company.tap.gosellapi.internal.api.models.PhoneNumber;
import company.tap.gosellapi.internal.api.models.SaveCard;
import company.tap.gosellapi.internal.api.models.SavedCard;
import company.tap.gosellapi.internal.api.models.Token;
import company.tap.gosellapi.open.buttons.PayButtonView;
import company.tap.gosellapi.open.controllers.SDKSession;
import company.tap.gosellapi.open.controllers.ThemeObject;
import company.tap.gosellapi.open.delegate.SessionDelegate;
import company.tap.gosellapi.open.enums.AppearanceMode;
import company.tap.gosellapi.open.enums.CardType;
import company.tap.gosellapi.open.enums.TransactionMode;
import company.tap.gosellapi.open.models.CardsList;
import company.tap.gosellapi.open.models.Customer;
import company.tap.gosellapi.open.models.Receipt;
import company.tap.gosellapi.open.models.TapCurrency;


public class MainActivity extends AppCompatActivity implements SessionDelegate {

    private final int SDK_REQUEST_CODE = 1001;
    private SDKSession sdkSession;
    private PayButtonView payButtonView;
    private SettingsManager settingsManager;
    private ProgressDialog progress;

    // not used
    private static RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private static RecyclerView recyclerView;
    private static ArrayList<SavedCard> data;
    static View.OnClickListener myOnClickListener;
    private static ArrayList<Integer> removedItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startSDK();
    }

    private void startSDK() {
        /**
         * Required step.
         * Configure SDK with your Secret API key and App Bundle name registered with tap company.
         */
        configureApp();

        /**
         * Optional step
         * Here you can configure your app theme (Look and Feel).
         */
        configureSDKThemeObject();

        /**
         * Required step.
         * Configure SDK Session with all required data.
         */
        configureSDKSession();

        /**
         * Required step.
         * Choose between different SDK modes
         */
        configureSDKMode();

        /**
         * If you included Tap Pay Button then configure it first, if not then ignore this step.
         */
        initPayButton();
    }

    private void configureApp() {
        GoSellSDK.init(this, "sk_test_kovrMB0mupFJXfNZWx6Etg5y","company.tap.goSellSDKExample");  // to be replaced by merchant
        GoSellSDK.setLocale("en");
    }

    private void configureSDKThemeObject() {

        ThemeObject.getInstance()
                .setAppearanceMode(AppearanceMode.WINDOWED_MODE)
                .setSdkLanguage("en")
              //  .setHeaderFont(Typeface.createFromAsset(getAssets(), "fonts/poppins_regular.ttf"))
                .setHeaderTextColor(getResources().getColor(R.color.black1))
                .setHeaderTextSize(17)
                .setHeaderBackgroundColor(getResources().getColor(R.color.french_gray_new))


                //.setCardInputFont(Typeface.createFromAsset(getAssets(), "fonts/poppins_regular.ttf"))
                .setCardInputTextColor(getResources().getColor(R.color.black))
                .setCardInputInvalidTextColor(getResources().getColor(R.color.red))
                .setCardInputPlaceholderTextColor(getResources().getColor(R.color.gray))


                .setSaveCardSwitchOffThumbTint(getResources().getColor(R.color.french_gray_new))
                .setSaveCardSwitchOnThumbTint(getResources().getColor(R.color.vibrant_green))
                .setSaveCardSwitchOffTrackTint(getResources().getColor(R.color.french_gray))
                .setSaveCardSwitchOnTrackTint(getResources().getColor(R.color.vibrant_green_pressed))

                .setScanIconDrawable(getResources().getDrawable(R.drawable.btn_card_scanner_normal))

                .setPayButtonResourceId(R.drawable.btn_pay_selector)  //btn_pay_merchant_selector
              //  .setPayButtonFont(Typeface.createFromAsset(getAssets(), "fonts/poppins_regular.ttf"))

                .setPayButtonDisabledTitleColor(getResources().getColor(R.color.white))
                .setPayButtonEnabledTitleColor(getResources().getColor(R.color.white))
                .setPayButtonTextSize(14)
                .setPayButtonLoaderVisible(true)
                .setPayButtonSecurityIconVisible(true);

    }

    private void configureSDKSession() {

        // Instantiate SDK Session
        if (sdkSession == null) sdkSession = new SDKSession();   //** Required **

        // pass your activity as a session delegate to listen to SDK internal payment process follow
        sdkSession.addSessionDelegate(this);    //** Required **

        // initiate PaymentDataSource
        sdkSession.instantiatePaymentDataSource();    //** Required **

        // set transaction currency associated to your account
        // sdkSession.setTransactionCurrency(new TapCurrency(SharedPrefManager.getUserCurrency(getApplicationContext())));    //** Required **
        sdkSession.setTransactionCurrency(new TapCurrency("KWD"));    //** Required **

        // Using static CustomerBuilder method available inside TAP Customer Class you can populate TAP Customer object and pass it to SDK
        sdkSession.setCustomer(getCustomer());    //** Required **

        // Set Total Amount. The Total amount will be recalculated according to provided Taxes and Shipping
        // sdkSession.setAmount(new BigDecimal(SharedPrefManager.getTotalAmmount(getApplicationContext())));  //** Required **
        sdkSession.setAmount(new BigDecimal(1));  //** Required **

        // Set Payment Items array list
        sdkSession.setPaymentItems(new ArrayList<>());// ** Optional ** you can pass empty array list

        // Set Taxes array list
        sdkSession.setTaxes(new ArrayList<>());// ** Optional ** you can pass empty array list

        // Set Shipping array list
        sdkSession.setShipping(new ArrayList<>());// ** Optional ** you can pass empty array list

        // Post URL
        sdkSession.setPostURL(""); // ** Optional **

        // Payment Description
        sdkSession.setPaymentDescription(""); //** Optional **

        // Payment Extra Info
        sdkSession.setPaymentMetadata(new HashMap<>());// ** Optional ** you can pass empty array hash map

        // Payment Reference
        sdkSession.setPaymentReference(null); // ** Optional ** you can pass null

        // Payment Statement Descriptor
        sdkSession.setPaymentStatementDescriptor(""); // ** Optional **

        // Enable or Disable Saving Card
        sdkSession.isUserAllowedToSaveCard(false); //  ** Required ** you can pass boolean

        // Enable or Disable 3DSecure
        sdkSession.isRequires3DSecure(true);

        //Set Receipt Settings [SMS - Email ]
        sdkSession.setReceiptSettings(new Receipt(false, false)); // ** Optional ** you can pass Receipt object or null

        // Set Authorize Action
        sdkSession.setAuthorizeAction(null); // ** Optional ** you can pass AuthorizeAction object or null

        sdkSession.setDestination(null); // ** Optional ** you can pass Destinations object or null

        sdkSession.setMerchantID(null); // ** Optional ** you can pass merchant id or null

        sdkSession.setDefaultCardHolderName("AHLAAM "); // ** Optional ** you can pass defaultCardHolderName

        sdkSession.isUserAllowedToEnableCardHolderName(true);
     //   sdkSession.setCardType(CardType.DEBIT);

        /**
         * Use this method where ever you want to show TAP SDK Main Screen.
         * This method must be called after you configured SDK as above
         * This method will be used in case of you are not using TAP PayButton in your activity.
         */
        //sdkSession.start(this);
    }

    private void startSDKWithUI() {
        if (sdkSession != null) {
            TransactionMode trx_mode = (settingsManager != null) ? settingsManager.getTransactionsMode("key_sdk_transaction_mode") : TransactionMode.PURCHASE;
            // set transaction mode [TransactionMode.PURCHASE - TransactionMode.AUTHORIZE_CAPTURE - TransactionMode.SAVE_CARD - TransactionMode.TOKENIZE_CARD ]
            sdkSession.setTransactionMode(TransactionMode.SAVE_CARD);    //** Required **
            // if you are not using tap button then start SDK using the following call
            // sdkSession.start(this);
        }
    }

    private void configureSDKMode() {

        /**
         * You have to choose only one Mode of the following modes:
         * Note:-
         *      - In case of using PayButton, then don't call sdkSession.start(this); because the SDK will start when user clicks the tap pay button.
         */
        //////////////////////////////////////////////////////    SDK with UI //////////////////////
        /**
         * 1- Start using  SDK features through SDK main activity (With Tap CARD FORM)
         */
        startSDKWithUI();

        //////////////////////////////////////////////////////    SDK Tokenization without UI //////////////////////
        /**
         * 2- Start using  SDK to tokenize your card without using SDK main activity (Without Tap CARD FORM)
         * After the SDK finishes card tokenization, it will notify this activity with tokenization result in either
         * cardTokenizedSuccessfully(@NonNull String token) or sdkError(@Nullable GoSellError goSellError)
         */
        //          startSDKTokenizationWithoutUI();
        //        sdkSession.start(this);

        //////////////////////////////////////////////////////    SDK Saving card without UI //////////////////////
        /**
         *  3- Start using  SDK to save your card without using SDK main activity ((Without Tap CARD FORM))
         *  After the SDK finishes card tokenization, it will notify this activity with save card result in either
         *  cardSaved(@NonNull Charge charge) or sdkError(@Nullable GoSellError goSellError)
         *
         */
        //         startSDKSavingCardWithoutUI();
        //       sdkSession.start(this);
    }

    private void initPayButton() {

        payButtonView = findViewById(R.id.payButtonId);
        if (ThemeObject.getInstance().getPayButtonFont() != null)
            payButtonView.setupFontTypeFace(ThemeObject.getInstance().getPayButtonFont());
        if (ThemeObject.getInstance().getPayButtonDisabledTitleColor() != 0 && ThemeObject.getInstance().getPayButtonEnabledTitleColor() != 0)
            payButtonView.setupTextColor(ThemeObject.getInstance().getPayButtonEnabledTitleColor(),
                    ThemeObject.getInstance().getPayButtonDisabledTitleColor());
        if (ThemeObject.getInstance().getPayButtonTextSize() != 0)
            payButtonView.getPayButton().setTextSize(ThemeObject.getInstance().getPayButtonTextSize());
        if (ThemeObject.getInstance().isPayButtSecurityIconVisible())
            payButtonView.getSecurityIconView().setVisibility(ThemeObject.getInstance().isPayButtSecurityIconVisible() ? View.VISIBLE : View.INVISIBLE);
        if (ThemeObject.getInstance().getPayButtonResourceId() != 0)
            payButtonView.setBackgroundSelector(ThemeObject.getInstance().getPayButtonResourceId());

        if (sdkSession != null) {
            TransactionMode trx_mode = sdkSession.getTransactionMode();
            if (trx_mode != null) {

                if (TransactionMode.SAVE_CARD == trx_mode || TransactionMode.SAVE_CARD_NO_UI == trx_mode) {
                    payButtonView.getPayButton().setText(getString(company.tap.gosellapi.R.string.save_card));
                } else if (TransactionMode.TOKENIZE_CARD == trx_mode || TransactionMode.TOKENIZE_CARD_NO_UI == trx_mode) {
                    payButtonView.getPayButton().setText(getString(company.tap.gosellapi.R.string.tokenize));
                } else {
                    payButtonView.getPayButton().setText(getString(company.tap.gosellapi.R.string.pay));
                }
            } else {
                configureSDKMode();
            }
            sdkSession.setButtonView(payButtonView, this, SDK_REQUEST_CODE);
        }
    }

    private void listSavedCards() {
        if (sdkSession != null)
            sdkSession.listAllCards("CUSTOMER_ID", this);
    }


// override session delegate

    @Override
    public void paymentSucceed(@NonNull Charge charge) {

        System.out.println("Payment Succeeded : " + charge.getStatus());
        System.out.println("Payment Succeeded : " + charge.getDescription());
        System.out.println("Payment Succeeded : " + charge.getResponse().getMessage());
        System.out.println("Payment Succeeded : exp object mnth  " + charge.getCard().getExpiry().getMonth());
        System.out.println("Payment Succeeded : exp object year " + charge.getCard().getExpiry().getYear());
        //saveCustomerRefInSession(charge);
        //configureSDKSession();
        //showDialog(charge.getId(),charge.getResponse().getMessage(),company.tap.gosellapi.R.drawable.ic_checkmark_normal);
        //showDialog("",""+R.string.PaymentSucceded,company.tap.gosellapi.R.drawable.ic_checkmark_normal);
      /* Call<ConfirmPaymentResponse> call = Client.getmInstance().getApi().confirmPayment(
                SharedPrefManager.getUserToken(getApplicationContext()),
                SharedPrefManager.getLang(getApplicationContext()),
                "SUCCESS",
                "",
                "",
                charge.getReference().getTrack(),
                charge.getReceipt().getId()
        );*//*

      //  Toast.makeText(getApplicationContext(),R.string.PaymentSucceded,Toast.LENGTH_LONG).show();
        final Thread myThreadInternet = new Thread() {

            @Override
            public void run() {
                try {
                    sleep(2500);
                    showDialog(charge.getId(),charge.getResponse().getMessage(),company.tap.gosellapi.R.drawable.icon_failed);
                    Intent t = new Intent(getApplicationContext(),HomeActivity.class);
                    startActivity(t);
                    finish();

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        myThreadInternet.start();*/
    }

    @Override
    public void paymentFailed(@Nullable Charge charge) {
        System.out.println("Payment Failed : " + charge.getStatus());
        System.out.println("Payment Failed : " + charge.getDescription());
        System.out.println("Payment Failed : " + charge.getResponse().getMessage());


        showDialog(charge.getId(), charge.getResponse().getMessage(), company.tap.gosellapi.R.drawable.icon_failed);
    }

    @Override
    public void authorizationSucceed(@NonNull Authorize authorize) {
        System.out.println("Authorize Succeeded : " + authorize.getStatus());
        System.out.println("Authorize Succeeded : " + authorize.getResponse().getMessage());

        if (authorize.getCard() != null) {
            System.out.println("Payment Authorized Succeeded : first six : " + authorize.getCard().getFirstSix());
            System.out.println("Payment Authorized Succeeded : last four: " + authorize.getCard().getLast4());
            System.out.println("Payment Authorized Succeeded : card object : " + authorize.getCard().getObject());
        }

        System.out.println("##############################################################################");
        if (authorize.getAcquirer() != null) {
            System.out.println("Payment Authorized Succeeded : acquirer id : " + authorize.getAcquirer().getId());
            System.out.println("Payment Authorized Succeeded : acquirer response code : " + authorize.getAcquirer().getResponse().getCode());
            System.out.println("Payment Authorized Succeeded : acquirer response message: " + authorize.getAcquirer().getResponse().getMessage());
        }
        System.out.println("##############################################################################");
        if (authorize.getSource() != null) {
            System.out.println("Payment Authorized Succeeded : source id: " + authorize.getSource().getId());
            System.out.println("Payment Authorized Succeeded : source channel: " + authorize.getSource().getChannel());
            System.out.println("Payment Authorized Succeeded : source object: " + authorize.getSource().getObject());
            System.out.println("Payment Authorized Succeeded : source payment method: " + authorize.getSource().getPaymentMethodStringValue());
            System.out.println("Payment Authorized Succeeded : source payment type: " + authorize.getSource().getPaymentType());
            System.out.println("Payment Authorized Succeeded : source type: " + authorize.getSource().getType());
        }

        System.out.println("##############################################################################");
        if (authorize.getExpiry() != null) {
            System.out.println("Payment Authorized Succeeded : expiry type :" + authorize.getExpiry().getType());
            System.out.println("Payment Authorized Succeeded : expiry period :" + authorize.getExpiry().getPeriod());
        }


        configureSDKSession();
        showDialog(authorize.getId(), authorize.getResponse().getMessage(), company.tap.gosellapi.R.drawable.ic_checkmark_normal);
    }

    @Override
    public void authorizationFailed(Authorize authorize) {
        System.out.println("Authorize Failed : " + authorize.getStatus());
        System.out.println("Authorize Failed : " + authorize.getDescription());
        System.out.println("Authorize Failed : " + authorize.getResponse().getMessage());
        showDialog(authorize.getId(), authorize.getResponse().getMessage(), company.tap.gosellapi.R.drawable.icon_failed);
    }


    @Override
    public void cardSaved(@NonNull Charge charge) {
        // Cast charge object to SaveCard first to get all the Card info.
        if (charge instanceof SaveCard) {
            System.out.println("Card Saved Succeeded : first six digits : " + ((SaveCard) charge).getCard().getFirstSix() + "  last four :" + ((SaveCard) charge).getCard().getLast4());
        }
        System.out.println("Card Saved Succeeded : " + charge.getStatus());
        System.out.println("Card Saved Succeeded : " + charge.getCard().getBrand());
        System.out.println("Card Saved Succeeded : " + charge.getDescription());
        System.out.println("Card Saved Succeeded : " + charge.getResponse().getMessage());
        System.out.println("Card Saved Succeeded cardIssuer : " +  ((SaveCard) charge).getCardIssuer().getName());
        System.out.println("Card Saved Succeeded ID : " +  ((SaveCard) charge).getCardIssuer().getId());
        showDialog(charge.getId(), charge.getStatus().toString(), company.tap.gosellapi.R.drawable.ic_checkmark_normal);
    }

    @Override
    public void cardSavingFailed(@NonNull Charge charge) {
        System.out.println("Card Saved Failed : " + charge.getStatus());
        System.out.println("Card Saved Failed : " + charge.getDescription());
        System.out.println("Card Saved Failed : " + charge.getResponse().getMessage());
        showDialog(charge.getId(), charge.getStatus().toString(), company.tap.gosellapi.R.drawable.icon_failed);
    }

    @Override
    public void cardTokenizedSuccessfully(@NonNull Token token) {
        System.out.println("Card Tokenized Succeeded : ");
        System.out.println("Token card : " + token.getCard().getFirstSix() + " **** " + token.getCard().getLastFour());
        System.out.println("Token card : " + token.getCard().getFingerprint() + " **** " + token.getCard().getFunding());
        System.out.println("Token card : " + token.getCard().getId() + " ****** " + token.getCard().getName());
        System.out.println("Token card : " + token.getCard().getAddress() + " ****** " + token.getCard().getObject());
        System.out.println("Token card : " + token.getCard().getExpirationMonth() + " ****** " + token.getCard().getExpirationYear());

        showDialog(token.getId(), "Token", company.tap.gosellapi.R.drawable.ic_checkmark_normal);
    }

    @Override
    public void savedCardsList(@NonNull CardsList cardsList) {
        if (cardsList != null && cardsList.getCards() != null) {
            System.out.println(" Card List Response Code : " + cardsList.getResponseCode());
            System.out.println(" Card List Top 10 : " + cardsList.getCards().size());
            System.out.println(" Card List Has More : " + cardsList.isHas_more());
            System.out.println("----------------------------------------------");
            for (SavedCard sc : cardsList.getCards()) {
                System.out.println(sc.getBrandName());
            }
            System.out.println("----------------------------------------------");

        }

    }


    @Override
    public void sdkError(@Nullable GoSellError goSellError) {
        if (progress != null)
            progress.dismiss();
        if (goSellError != null) {
            System.out.println("SDK Process Error : " + goSellError.getErrorBody());
            System.out.println("SDK Process Error : " + goSellError.getErrorMessage());
            System.out.println("SDK Process Error : " + goSellError.getErrorCode());
            showDialog(goSellError.getErrorCode() + "", goSellError.getErrorMessage(), company.tap.gosellapi.R.drawable.icon_failed);
        }
    }


    @Override
    public void sessionIsStarting() {
        System.out.println(" Session Is Starting.....");
    }

    @Override
    public void sessionHasStarted() {
        System.out.println(" Session Has Started .......");
    }


    @Override
    public void sessionCancelled() {
        Log.d("MainActivity", "Session Cancelled.........");
    }

    @Override
    public void sessionFailedToStart() {
        Log.d("MainActivity", "Session Failed to start.........");
    }


    @Override
    public void invalidCardDetails() {
        System.out.println(" Card details are invalid....");
    }

    @Override
    public void backendUnknownError(String message) {
        System.out.println("Backend Un-Known error.... : " + message);
    }

    @Override
    public void invalidTransactionMode() {
        System.out.println(" invalidTransactionMode  ......");
    }

    @Override
    public void invalidCustomerID() {
        if (progress != null)
            progress.dismiss();
        System.out.println("Invalid Customer ID .......");

    }

    @Override
    public void userEnabledSaveCardOption(boolean saveCardEnabled) {
        System.out.println("userEnabledSaveCardOption :  " + saveCardEnabled);
    }


// needed for Demo only

    private Customer getCustomer() { // test customer id cus_Kh1b4220191939i1KP2506448

        Customer customer = (settingsManager != null) ? settingsManager.getCustomer() : null;

        PhoneNumber phoneNumber = customer != null ? customer.getPhone() : new PhoneNumber("965", "******");

        //
      /*  return new Customer.CustomerBuilder(null).email(SharedPrefManager.getPaymentEmail(getApplicationContext())).firstName(SharedPrefManager.getPaymentFirstName(getApplicationContext()))
                .lastName(SharedPrefManager.getPaymentLastName(getApplicationContext())).metadata("").phone(new PhoneNumber(SharedPrefManager.getPaymentCountryCode(getApplicationContext()),SharedPrefManager.getPaymentPhone(getApplicationContext())))
                .middleName("-").build();*/

        return new Customer.CustomerBuilder("").email("email@gmail.com").firstName("firstname")
                .lastName("").phone(new PhoneNumber("965", "69045932"))
                .middleName("-").build();
    }

    private void showDialog(String chargeID, String msg, int icon) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;
        PopupWindow popupWindow;
        try {
            LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            if (inflater != null) {

                View layout = inflater.inflate(company.tap.gosellapi.R.layout.charge_status_layout, findViewById(
                        company.tap.gosellapi.R.id.popup_element));

                popupWindow = new PopupWindow(layout, width, 250, true);

                ImageView status_icon = layout.findViewById(company.tap.gosellapi.R.id.status_icon);
                TextView statusText = layout.findViewById(company.tap.gosellapi.R.id.status_text);
                TextView chargeText = layout.findViewById(company.tap.gosellapi.R.id.charge_id_txt);
                status_icon.setImageResource(icon);
// status_icon.setVisibility(View.INVISIBLE);
                chargeText.setText(chargeID);
                statusText.setText((msg != null && msg.length() > 30) ? msg.substring(0, 29) : msg);

                popupWindow.showAtLocation(layout, Gravity.TOP, 0, 50);
                popupWindow.getContentView().startAnimation(AnimationUtils.loadAnimation(this, R.anim.popup_show));

                setupTimer(popupWindow);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupTimer(PopupWindow popupWindow) {
        // Hide after some seconds
        final Handler handler = new Handler();
        final Runnable runnable = () -> {
            if (popupWindow.isShowing()) {
                popupWindow.dismiss();
            }
        };

        popupWindow.setOnDismissListener(() -> handler.removeCallbacks(runnable));

        handler.postDelayed(runnable, 4000);
    }

    public void startpay(View view) {
        sdkSession.start(this);
    }

    public void cancelSession(View view) {
        sdkSession.cancelSession(this);
    }
}