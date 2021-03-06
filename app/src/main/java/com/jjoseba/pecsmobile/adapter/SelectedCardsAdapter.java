package com.jjoseba.pecsmobile.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.jjoseba.pecsmobile.R;
import com.jjoseba.pecsmobile.model.Card;
import com.jjoseba.pecsmobile.ui.cards.CardTempPECS;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;


public class SelectedCardsAdapter  extends BaseAdapter {

    private ArrayList<Card> cards;
    private Context ctx;

    public SelectedCardsAdapter(Context context, ArrayList<Card> cards){
        this.cards = cards;
        this.ctx = context;
    }

    @Override
    public int getCount() {
        return cards.size();
    }

    @Override
    public Object getItem(int position) {
        return cards.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    protected class CardViewHolder{
        TextView label;
        ImageView image;
        View cardFrame;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        CardViewHolder holder;
        Card card = cards.get(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(ctx).inflate(R.layout.card_small, parent, false);

            // Set up the ViewHolder
            holder = new CardViewHolder();
            holder.image = (ImageView) convertView.findViewById(R.id.card_image);
            holder.label = (TextView) convertView.findViewById(R.id.card_label);
            holder.cardFrame = convertView.findViewById(R.id.card_frame);

            convertView.setTag(holder);
        } else {
            holder = (CardViewHolder) convertView.getTag();
        }

        holder.cardFrame.setBackgroundColor(card.getCardColor());
        holder.cardFrame.setVisibility(View.VISIBLE);
        if (card instanceof CardTempPECS){
            //holder.image.setVisibility(View.INVISIBLE);
            holder.image.setImageDrawable(null);
            holder.label.setVisibility(View.VISIBLE);
            holder.label.setTextColor(card.getCardColor());
            holder.label.setText(card.getLabel());
        }
        else{
            holder.image.setVisibility(View.VISIBLE);
            holder.label.setVisibility(View.GONE);
            File imageFile = new File(card.getImagePath());
            Picasso.with(ctx).load(imageFile).placeholder(R.drawable.empty).error(R.drawable.empty).into(holder.image);
        }

        return convertView;

    }
}
