package com.jjoseba.pecsmobile.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.ImageButton;

import com.jjoseba.pecsmobile.R;
import com.jjoseba.pecsmobile.adapter.SelectedCardsAdapter;
import com.jjoseba.pecsmobile.fragment.CardsPage;
import com.jjoseba.pecsmobile.fragment.NewCardFragment;
import com.jjoseba.pecsmobile.model.Card;
import com.jjoseba.pecsmobile.ui.CardsGridListener;
import com.jjoseba.pecsmobile.ui.NewCardListener;
import com.jjoseba.pecsmobile.ui.cards.CardPECS;
import com.jjoseba.pecsmobile.ui.cards.CardTempPECS;
import com.jjoseba.pecsmobile.ui.dialog.EditCardDialog;
import com.jjoseba.pecsmobile.ui.dialog.HiddenInputDialog;
import com.jjoseba.pecsmobile.ui.viewpager.EnableableViewPager;
import com.jjoseba.pecsmobile.ui.viewpager.ZoomOutPageTransformer;
import com.soundcloud.android.crop.Crop;

import org.lucasr.twowayview.TwoWayView;

import java.util.ArrayList;
import java.util.HashMap;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class CardsActivity extends FragmentActivity implements TextToSpeech.OnInitListener, CardsGridListener, NewCardListener, ViewPager.OnPageChangeListener {

    private static final boolean FADE_IN = true;
    private static final boolean FADE_OUT = false;

    private EnableableViewPager mPager;
    private ScreenSlidePagerAdapter mPagerAdapter;
    private ZoomOutPageTransformer mPageTransformer;
    private HashMap<Card, CardsPage> cardPages = new HashMap<Card, CardsPage>();
    private int mLastPage;
    private NewCardFragment newCardFragment;
    private View newCardContainer;
    private boolean newCardIsHiding = false;

    protected ArrayList<Card> navigationCards = new ArrayList<Card>();
    protected ArrayList<Card> selectedCards = new ArrayList<Card>();
    private TwoWayView selectedCardsList;
    private SelectedCardsAdapter selectedCardsAdapter;

    private TextToSpeech myTTS;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        setContentView(R.layout.activity_cards);

        //Initial card -- change this in the future
        navigationCards.add(new CardPECS());

        mPager = (EnableableViewPager) findViewById(R.id.pager);
        mPagerAdapter = new ScreenSlidePagerAdapter(this.getSupportFragmentManager());
        mPageTransformer = new ZoomOutPageTransformer();

        mPager.setAdapter(mPagerAdapter);
        mPager.setPageTransformer(false, mPageTransformer);
        mPager.setPagingEnabled(false);
        mPager.setOnPageChangeListener(this);
        mLastPage = 0;

        newCardContainer = findViewById(R.id.newCardContainer);
        newCardFragment = (NewCardFragment) getSupportFragmentManager().findFragmentById(R.id.new_card_fragment);
        newCardFragment.setNewCardListener(this);

        selectedCardsAdapter = new SelectedCardsAdapter(this, selectedCards);
        selectedCardsList = (TwoWayView) findViewById(R.id.selected_cards_list);
        selectedCardsList.setAdapter(selectedCardsAdapter);
        selectedCardsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i = new Intent(CardsActivity.this, ShowCardsActivity.class);
                i.putExtra("result", selectedCards);
                startActivity(i);
            }
        });

        ImageButton removeCardBtn = (ImageButton) findViewById(R.id.removeLastCard);
        removeCardBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedCards.size() > 0){
                    selectedCards.remove(selectedCards.size()-1);
                    selectedCardsAdapter.notifyDataSetChanged();
                }
            }
        });

        myTTS = new TextToSpeech(this, this);
    }

    @Override
    protected void onResume(){

        super.onResume();
        selectedCards.clear();
        selectedCardsAdapter.notifyDataSetChanged();
    }

    @Override
    public void onBackPressed() {
        if (newCardContainer.getVisibility() == View.VISIBLE){
            if (newCardFragment.isColorPickerVisible()){
             newCardFragment.hideColorPicker();
            }
            else if (!newCardIsHiding){
                animateCardContainer(FADE_OUT);
            }
        }
        else if (mPager.getCurrentItem() == 0) {
            // If the user is currently looking at the first step, allow the system to handle the
            // Back button. This calls finish() on this activity and pops the back stack.
            super.onBackPressed();

        } else {
            // Otherwise, select the previous step.
            mPager.setCurrentItem(mPager.getCurrentItem() - 1);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {

        super.onSaveInstanceState(outState);
        outState.putSerializable("cards", navigationCards);
        //outState.putSerializable("fragments", cardPages);
        outState.putInt("currentPage", mLastPage);

    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {

        mLastPage = savedInstanceState.getInt("currentPage", mLastPage);
        ArrayList<Card> cards = (ArrayList<Card>) savedInstanceState.getSerializable("cards");
        if (cards != null && cards.size() > 0){
            navigationCards = cards;
        }
    }

    @Override
    public void onCardSelected(Card clicked) {

        if (clicked.isCategory()){
            int target = mPager.getCurrentItem() + 1;
            navigationCards.add(clicked);
            mPagerAdapter.notifyDataSetChanged();
            mPager.setCurrentItem(target, true);
            mPager.setPagingEnabled(true);
        }
        else{
            if (!selectedCards.contains(clicked)){
                selectedCards.add(clicked);
                selectedCardsAdapter.notifyDataSetChanged();
            }

        }

    }

    @Override
    public void onAddCardButton(Card clicked){
        animateCardContainer(FADE_IN);
        newCardFragment.resetForm(clicked);
    }

    @Override
    public void onTempCardButton() {

        HiddenInputDialog dialog = new HiddenInputDialog();
        dialog.setOnTextListener(new HiddenInputDialog.InputListener() {
            @Override
            public void onText(String cardLabel) {
                CardTempPECS card = new CardTempPECS();
                card.setCardColor(navigationCards.get(navigationCards.size() - 1).getHexCardColor());
                card.setLabel(cardLabel);
                selectedCards.add(card);
                selectedCardsAdapter.notifyDataSetChanged();
            }
        });
        dialog.show(this.getFragmentManager(), "Hidden");
    }

    @Override
    public void onCardLongClick(final Card cardPressed) {
        final EditCardDialog dialog = new EditCardDialog(this, cardPressed);
        dialog.show();

        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface d) {
                if (dialog.hasDataChanged()){
                    Card currentCard = navigationCards.get(mLastPage);
                    CardsPage currentPage = cardPages.get(currentCard);
                    currentPage.notifyCardChanged(cardPressed, dialog.isCardDeleted());
                }
            }
        });
    }

    private void animateCardContainer(final boolean show) {

        //if the card is already hiding, don't launch the animation again
        if (!show && newCardIsHiding) return;

        newCardContainer.setVisibility(View.VISIBLE);
        Animation fadeAnimation = new AlphaAnimation(show?0:1, show?1:0);
        fadeAnimation.setInterpolator(new DecelerateInterpolator()); //add this
        fadeAnimation.setDuration(600);
        fadeAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }
            @Override
            public void onAnimationEnd(Animation animation) {
                if (!show){
                    newCardContainer.setVisibility(View.GONE);
                    newCardIsHiding = false;
                }
            }
            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
        if (!show) newCardIsHiding = true;
        newCardContainer.startAnimation(fadeAnimation);
    }

    @Override
    public void onPageSelected(int page) {
        int currentPage = mPager.getCurrentItem();

        if (currentPage < mLastPage){
            // swiping back --> remove last page
            Card lastPageCard = navigationCards.get(mLastPage);
            mPagerAdapter.removeFragment(lastPageCard);
            navigationCards.remove(mLastPage);
            mPagerAdapter.notifyDataSetChanged();

            if ( currentPage == 0){
                mPager.setPagingEnabled(false);
            }
        }
        mLastPage = currentPage;
    }

    @Override
    public void onPageScrolled(int i, float v, int i2) {  }

    @Override
    public void onPageScrollStateChanged(int i) {  }

    @Override
    public void onNewCard(Card card) {
        animateCardContainer(FADE_OUT);
        Card currentCard = navigationCards.get(mLastPage);
        CardsPage currentPage = cardPages.get(currentCard);
        currentPage.addCard(card);
    }

    @Override
    public void onCancel() {
        animateCardContainer(FADE_OUT);
    }

    @Override
    public void onInit(int status) {

    }

    private class ScreenSlidePagerAdapter extends FragmentPagerAdapter {

        private final FragmentManager mFragmentManager;

        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
            this.mFragmentManager = fm;
        }

        @Override
        public Fragment getItem(int position) {
            Card card = navigationCards.get(position);
            CardsPage page = cardPages.get(card);
            if (page == null){
                page = CardsPage.newInstance(card);
                cardPages.put(card, page);
            }
            return page;
        }

        @Override
        public int getItemPosition(Object object){
            return POSITION_NONE;
        }

        public void removeFragment(Card cardToRemove){
            CardsPage page = cardPages.get(cardToRemove);
            cardPages.remove(cardToRemove);
            mFragmentManager.beginTransaction().remove(page).commit();
        }

        @Override
        public int getCount() {
            return navigationCards.size();
        }

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == Crop.REQUEST_CROP && resultCode == RESULT_OK) {
            newCardFragment.notifySuccessfulCrop();
        }
    }
}
