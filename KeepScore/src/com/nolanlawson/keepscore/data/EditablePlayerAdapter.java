package com.nolanlawson.keepscore.data;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.nolanlawson.keepscore.OrganizePlayersActivity;
import com.nolanlawson.keepscore.R;
import com.nolanlawson.keepscore.db.PlayerScore;
import com.nolanlawson.keepscore.helper.DialogHelper;
import com.nolanlawson.keepscore.helper.PlayerColor;
import com.nolanlawson.keepscore.util.Callback;
import com.nolanlawson.keepscore.widget.PlayerColorView;
import com.nolanlawson.keepscore.widget.dragndrop.DragSortListView.DropListener;

public class EditablePlayerAdapter extends ArrayAdapter<PlayerScore> implements DropListener {

    private List<PlayerScore> items;
    private Runnable onChangeListener;
    private Callback<PlayerScore> onDeleteListener;
    private View currentlyDragging;

    public EditablePlayerAdapter(Context context, List<PlayerScore> items) {
        super(context, R.layout.editable_player, items);
        this.items = items;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public boolean isEnabled(int position) {
        return false;
    }

    /**
     * Runnable to call when the data changes
     * 
     * @param runnable
     */
    public void setOnChangeListener(Runnable runnable) {
        this.onChangeListener = runnable;
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
        if (onChangeListener != null) {
            onChangeListener.run();
        }
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {

        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.editable_player, parent, false);
        }

        final PlayerScore playerScore = getItem(position);
        TextView nameTextView = (TextView) view.findViewById(R.id.text_player_name);
        TextView numberTextView = (TextView) view.findViewById(R.id.text_player_number);
        TextView scoreTextView = (TextView) view.findViewById(R.id.text_player_score);
        PlayerColorView playerColorView = (PlayerColorView) view.findViewById(R.id.player_color_image);

        nameTextView.setText(playerScore.toDisplayName(getContext()));
        numberTextView.setText('#' + Integer.toString(playerScore.getPlayerNumber() + 1));
        scoreTextView.setText(Long.toString(playerScore.getScore()));

        playerColorView.setPlayerColor(playerScore.getPlayerColor());
        // listen for player color clicks
        playerColorView.setOnClickListener(new OnClickListener() {
            
            private PlayerColor chosenColor;
            
            @Override
            public void onClick(View v) {
                DialogHelper.showColorChooserDialog(getContext(), playerScore.getPlayerColor(), new Callback<PlayerColor>() {

                    @Override
                    public void onCallback(PlayerColor playerColor) {
                        // color changed
                        chosenColor = playerColor;
                        
                    }
                }, new Runnable() {
                    
                    @Override
                    public void run() {
                        // color selected
                        if (chosenColor == null) {
                            return;
                        }
                        playerScore.setPlayerColor(chosenColor);
                        notifyDataSetChanged();
                    }
                });
            }
        });
        
        // add listener to the delete button
        ImageButton deleteButton = (ImageButton) view.findViewById(R.id.button_delete_player);
        deleteButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                // delete
                remove(playerScore);
                resetPlayerNumbers();
                notifyDataSetChanged();
                if (onDeleteListener != null) {
                    onDeleteListener.onCallback(playerScore);
                }
            }
        });
        // user is not allowed to delete the final 2 users
        deleteButton.setEnabled(getCount() > OrganizePlayersActivity.MIN_NUM_PLAYERS);
        ImageButton editButton = (ImageButton) view.findViewById(R.id.button_edit_player);
        editButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // edit
                DialogHelper.showPlayerNameDialog(getContext(), R.string.title_change_name, playerScore.getName(),
                        playerScore.getPlayerNumber(), new Callback<String>() {

                            @Override
                            public void onCallback(String input) {
                                playerScore.setName(input);
                                notifyDataSetChanged();
                            }
                        });
            }
        });

        view.setVisibility(currentlyDragging == view ? View.INVISIBLE : View.VISIBLE);

        return view;
    }

    @Override
    public int getItemViewType(int position) {
        return position == 0 ? 0 : 1; // first one is 'add', the rest are
        // different
    }

    @Override
    public int getViewTypeCount() {
        return 2;// one for 'add', one for the others
    }

    public void shuffleAndRefreshView() {

        Collections.shuffle(items);
        resetPlayerNumbers();
        notifyDataSetChanged();
    }
    
    public void sortAndRefreshView(Comparator<PlayerScore> comparator) {
        Collections.sort(items, comparator);
        resetPlayerNumbers();
        notifyDataSetChanged();
    }

    private void resetPlayerNumbers() {
        for (int i = 0; i < items.size(); i++) {
            items.get(i).setPlayerNumber(i);
        }
    }

    public void setOnDeleteListener(Callback<PlayerScore> callback) {
        this.onDeleteListener = callback;

    }

    public List<PlayerScore> getItems() {
        return items;
    }

    @Override
    public void drop(int from, int to) {
        PlayerScore temp = items.get(from);
        items.remove(from);
        items.add(to, temp);

        resetPlayerNumbers();
        notifyDataSetChanged();
    }

}
