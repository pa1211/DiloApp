package com.jjoseba.pecsmobile.fragment;

import android.Manifest;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.jjoseba.pecsmobile.R;
import com.jjoseba.pecsmobile.app.DBHelper;
import com.jjoseba.pecsmobile.model.Card;
import com.jjoseba.pecsmobile.ui.cards.CardPECS;
import com.jjoseba.pecsmobile.ui.dialog.ImageDialog;
import com.jjoseba.pecsmobile.ui.NewCardListener;
import com.jjoseba.pecsmobile.util.FileUtils;
import com.jjoseba.pecsmobile.util.ImageUtils;
import com.larswerkman.holocolorpicker.ColorPicker;
import com.larswerkman.holocolorpicker.SaturationBar;
import com.larswerkman.holocolorpicker.ValueBar;
import com.soundcloud.android.crop.Crop;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import java.io.File;

public class NewCardFragment extends Fragment {

    private static final float EXTRA_TRANSLATION = 300f;
    private static final long ANIM_DURATION = 800;
    public static final int REQUEST_IMAGE = 1;
    public static final int REQUEST_CAMERA = 2;
    public static final int REQUEST_READ_CONTACTS = 111;

    private ColorPicker picker;
    private View colorPickerContainer;
    private View cardFrame;
    private View colorBucket;
    private ImageView cardImage;
    private int previousColor = Card.DEFAULT_COLOR;

    private EditText cardTitleTextView;
    private TextView cardTextImage;
    private Switch switchCategory;
    private Switch switchDisabled;

    private boolean disableOkButton = false;
    private NewCardListener listener;
    private int parentCard;

    private boolean textAsImage = false;

    private String cardImagePath;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.activity_new_card, container, false);

        cardFrame = view.findViewById(R.id.card_frame);
        colorBucket = view.findViewById(R.id.colorBucket);
        cardTitleTextView = (EditText) view.findViewById(R.id.et_title);
        cardTitleTextView.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {  }
            public void afterTextChanged(Editable s) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length()>0){
                    String text = s.toString();
                    cardTextImage.setText(text.toUpperCase());
                    if(!text.equals(text.toUpperCase()))
                    {
                        text=text.toUpperCase();
                        cardTitleTextView.setText(text);
                        cardTitleTextView.setSelection(text.length());
                    }
                }

            }
        });
        cardTextImage = (TextView) view.findViewById(R.id.card_imageText);
        switchCategory = (Switch) view.findViewById(R.id.sw_category);
        switchDisabled = (Switch) view.findViewById(R.id.sw_disabled);
        Button saveButton = (Button) view.findViewById(R.id.saveButton);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (disableOkButton) {
                    //the colorPicker is visible, so we select the current color
                    selectColor();
                } else {
                    if (validateForm()) {
                        CardPECS newCard = new CardPECS();
                        newCard.setCardColor(String.format("#%06X", (0xFFFFFF & previousColor)));
                        newCard.animateOnAppear = true;
                        newCard.setLabel(cardTitleTextView.getText().toString());
                        newCard.setAsCategory(switchCategory.isChecked());
                        newCard.setDisabled(switchDisabled.isChecked());
                        if (textAsImage){
                            cardTextImage.setTextColor(previousColor);
                            newCard.setImageFilename(ImageUtils.saveViewImage(cardTextImage));
                            cardTextImage.setTextColor(0x000000);
                        }
                        else if (cardImagePath != null){
                            newCard.setImageFilename(FileUtils.copyFileToInternal(cardImagePath));
                        }

                        DBHelper db = DBHelper.getInstance(NewCardFragment.this.getActivity());
                        db.addCard(parentCard, newCard);

                        if (listener != null) {
                            listener.onNewCard(newCard);
                        }
                    }

                }
            }
        });

        cardImage = (ImageView) view.findViewById(R.id.card_image);
        cardImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeCardImage();
            }
        });

        Button cancelButton = (Button) view.findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isColorPickerVisible()){
                    colorPickerContainer.setVisibility(View.INVISIBLE);
                    hideColorPicker();
                }
                if (listener != null){ listener.onCancel(); }
            }
        });

        colorPickerContainer = view.findViewById(R.id.pickerContainer);
        colorPickerContainer.setOnTouchListener(new View.OnTouchListener() {
            //Cancelamos la propagación del pulsado cuando colorPickerContainer es visible
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return (colorPickerContainer.getVisibility() == View.VISIBLE);
            }
        });

        picker = (ColorPicker) colorPickerContainer.findViewById(R.id.picker);
        picker.addSaturationBar((SaturationBar) colorPickerContainer.findViewById(R.id.saturationbar) );
        picker.addValueBar((ValueBar) colorPickerContainer.findViewById(R.id.valuebar));

        Button selectColorBtn = (Button) colorPickerContainer.findViewById(R.id.select_color_btn);
        selectColorBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { selectColor(); }
        });

        ImageButton pickColor = (ImageButton) view.findViewById(R.id.pickColorButton);
        pickColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showColorPicker();
            }
        });

        return view;
    }

    private void changeCardImage() {
        if (ContextCompat.checkSelfPermission(this.getActivity(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            Log.d("Permissions", "Aaagggg");
            // No explanation needed, we can request the permission.
            requestPermissions(
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_READ_CONTACTS);
            /*
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(actContext,
                    Manifest.permission.READ_CONTACTS)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

            }*/
        }
        else{
            final ImageDialog dialog = new ImageDialog(NewCardFragment.this);
            dialog.show();
            dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface d) {
                    if (dialog.isTextForImage()){
                        setTextForImage();
                    }
                }
            });
        }
    }

    private boolean validateForm() {
        String label = cardTitleTextView.getText().toString();
        if (label.trim().length() == 0){
            String errMessage = "Hay que introducir un texto a la tarjeta!";
            RelativeSizeSpan scaleStyle = new RelativeSizeSpan (1.8f);
            SpannableStringBuilder ssbuilder = new SpannableStringBuilder(errMessage);
            ssbuilder.setSpan(scaleStyle, 0, errMessage.length(), 0);

            cardTitleTextView.requestFocus();
            cardTitleTextView.setError(ssbuilder);
            return false;
        }
        else return true;

    }

    private void selectColor(){
        int selectedColor = picker.getColor();
        picker.setOldCenterColor(selectedColor);
        hideColorPicker();
        changeColor(selectedColor, true);
    }

    public void showColorPicker(){
        toggleColorPicker(true);
        disableOkButton = true;
    }

    public void hideColorPicker(){
        toggleColorPicker(false);
        disableOkButton = false;
    }

    public boolean isColorPickerVisible(){
        return colorPickerContainer.getVisibility() == View.VISIBLE;
    }

    public void setNewCardListener(NewCardListener newCardListener){
        listener = newCardListener;
    }

    protected void changeColor(int colorTo, boolean animate){
        int colorFrom = previousColor;
        if (animate){
            ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
            colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

                @Override
                public void onAnimationUpdate(ValueAnimator animator) {
                    int animColor = (Integer)animator.getAnimatedValue();
                    cardFrame.setBackgroundColor(animColor);
                    colorBucket.setBackgroundColor(animColor);
                    cardTextImage.setTextColor(animColor);
                }
            });
            colorAnimation.setDuration(ANIM_DURATION).start();
        }
        else{
            cardFrame.setBackgroundColor(colorTo);
            colorBucket.setBackgroundColor(colorTo);
            cardTextImage.setTextColor(colorTo);
        }

        previousColor = colorTo;
    }
    protected void toggleColorPicker(boolean show){

        float translation = - colorPickerContainer.getMeasuredHeight() - EXTRA_TRANSLATION;
        float initY  =  show ? translation : 0f;
        float finalY = !show ? translation : 0f;

        TranslateAnimation anim = new TranslateAnimation(
                TranslateAnimation.ABSOLUTE, 0f,
                TranslateAnimation.ABSOLUTE, 0f,
                TranslateAnimation.ABSOLUTE, initY,
                TranslateAnimation.ABSOLUTE, finalY);
        anim.setDuration(ANIM_DURATION);
        anim.setFillAfter(false);

        if (show) { colorPickerContainer.setVisibility(View.VISIBLE); }
        colorPickerContainer.startAnimation(anim);
        if (!show) { colorPickerContainer.setVisibility(View.INVISIBLE); }

    }

     @Override
     public void onActivityResult(int requestCode, int resultCode,
                                  Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
         if(resultCode == Activity.RESULT_OK){
             switch(requestCode) {

                 case REQUEST_IMAGE:
                     Uri selectedImage = imageReturnedIntent.getData();
                     Picasso.with(this.getActivity()).load(selectedImage).into(cardImage);
                     hideTextForImage();
                     cardImagePath = FileUtils.copyFileTemp(this.getActivity(), selectedImage);
                     Uri cardImageUri = Uri.fromFile(new File(cardImagePath));

                     Crop.of(cardImageUri, FileUtils.getTempImageURI())
                         .asSquare()
                         .withMaxSize(300, 300)
                         .start(this.getActivity());
                     break;

                 case REQUEST_CAMERA:
                     Uri tempUri = FileUtils.getTempImageURI();
                     cardImagePath = FileUtils.copyFileTemp(this.getActivity(), tempUri);
                     Picasso.with(this.getActivity()).load(tempUri).memoryPolicy(MemoryPolicy.NO_CACHE).into(cardImage);

                     hideTextForImage();
                     Crop.of(tempUri, tempUri)
                             .asSquare()
                             .withMaxSize(300, 300)
                             .start(this.getActivity());
                     break;

             }
         }

    }

    private void setTextForImage(){
        textAsImage = true;
        cardTextImage.setVisibility(View.VISIBLE);
        cardTextImage.setAllCaps(true);
        cardTextImage.setTextColor(picker.getColor());
        cardTextImage.setText(cardTitleTextView.getText());
        cardImage.setImageDrawable(null);
    }

    private void hideTextForImage(){
        cardTextImage.setVisibility(View.GONE);
        textAsImage = false;
    }

    public void resetForm(Card clicked) {
        changeColor(clicked.getCardColor(), false);
        textAsImage = false;
        picker.setColor(clicked.getCardColor());
        cardTextImage.setVisibility(View.GONE);
        cardTextImage.setText("");
        cardTitleTextView.setText("");
        switchCategory.setChecked(false);
        switchDisabled.setChecked(false);
        cardImage.setImageDrawable(null);
        this.parentCard = clicked.getCardId();
    }

    public void notifySuccessfulCrop(){
        Log.d("Crop", "Loading:" + cardImagePath);
        cardImagePath = FileUtils.copyFileTemp(getActivity(), FileUtils.getTempImageURI());
        File image = new File(cardImagePath);
        Log.d("Crop", image.exists()?"Exists!":"noooooo");
        Picasso.with(getActivity()).load(image).memoryPolicy(MemoryPolicy.NO_CACHE).error(R.drawable.empty).into(cardImage);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        Log.d("permissions", "Yaaay!");
        switch (requestCode) {
            case REQUEST_READ_CONTACTS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    changeCardImage();
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {
                    Log.d("Newcard", "Permission denied!");
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                break;
            }

        }
    }
}
