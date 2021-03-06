package com.jjoseba.pecsmobile.ui.cards;

import android.content.Context;
import android.view.View;

import com.jjoseba.pecsmobile.adapter.CardGridAdapter;

public class CardTempPECS extends ButtonCard {
    @Override
    public void inflateCard(CardGridAdapter.CardViewHolder holder, Context ctx) {

        holder.cardFrame.setBackgroundColor(this.getCardColor());
        holder.cardFrame.setVisibility(View.VISIBLE);
        //holder.buttonImage.setImageResource(R.drawable.keyboard);
        holder.buttonImage.setVisibility(View.GONE);
        //holder.buttonImage.setBackgroundColor(CardGridAdapter.bgOverlayColor);
        holder.label.setText(this.getLabel());
        holder.label.setTextColor(this.getCardColor());
    }
}
