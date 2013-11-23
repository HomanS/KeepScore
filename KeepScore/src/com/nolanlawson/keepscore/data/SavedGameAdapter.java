package com.nolanlawson.keepscore.data;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import android.content.Context;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

import com.nolanlawson.keepscore.R;
import com.nolanlawson.keepscore.db.GameSummary;
import com.nolanlawson.keepscore.db.PlayerScore;
import com.nolanlawson.keepscore.util.CollectionUtil;
import com.nolanlawson.keepscore.util.CollectionUtil.FunctionWithIndex;
import com.nolanlawson.keepscore.util.UtilLogger;

public class SavedGameAdapter extends ArrayAdapter<GameSummary> {

    private static UtilLogger log = new UtilLogger(SavedGameAdapter.class);

    private Set<GameSummary> checked = new HashSet<GameSummary>();
    private Runnable onCheckChangedRunnable;

    public SavedGameAdapter(Context context, List<GameSummary> values) {
        super(context, R.layout.saved_game_item, values);
    }

    public Set<GameSummary> getChecked() {
        return checked;
    }

    public void setOnCheckChangedRunnable(Runnable onCheckChangedRunnable) {
        this.onCheckChangedRunnable = onCheckChangedRunnable;
    }

    public void setChecked(Set<GameSummary> checked) {
        this.checked = checked;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {

        // view wrapper optimization per Romain Guy
        final Context context = parent.getContext();
        ViewWrapper viewWrapper;
        if (view == null) {
            LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = vi.inflate(R.layout.saved_game_item, parent, false);
            viewWrapper = new ViewWrapper(view);
            view.setTag(viewWrapper);
        } else {
            viewWrapper = (ViewWrapper) view.getTag();
        }

        TextView titleTextView = viewWrapper.getTitleTextView();
        TextView numPlayersTextView = viewWrapper.getNumPlayersTextView();
        TextView subtitleTextView = viewWrapper.getSubtitleTextView();
        TextView savedTextView = viewWrapper.getSavedTextView();
        CheckBox checkBox = viewWrapper.getCheckBox();

        final GameSummary game = getItem(position);

        StringBuilder gameTitle = new StringBuilder();
        if (!TextUtils.isEmpty(game.getName())) {
            gameTitle.append(game.getName()).append(" ").append(context.getString(R.string.text_game_name_separator))
                    .append(" ");
        }
        // Player 1, Player 2, Player3 etc.
        gameTitle.append(TextUtils.join(", ",
                CollectionUtil.transformWithIndices(game.getPlayerNames(), new FunctionWithIndex<String, CharSequence>() {

                    @Override
                    public CharSequence apply(String playerName, int index) {
                        return PlayerScore.toDisplayName(playerName, index, context);
                    }
                })));

        titleTextView.setText(gameTitle);

        numPlayersTextView.setText(Integer.toString(game.getPlayerNames().size()));
        numPlayersTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                context.getResources().getDimensionPixelSize(game.getPlayerNames().size() >= 10 // two
                                                                                                 // digit
                ? R.dimen.saved_game_num_players_text_size_two_digits
                        : R.dimen.saved_game_num_players_text_size_one_digit));

        int numRounds = game.getNumRounds();
        int roundsResId = numRounds == 1 ? R.string.text_format_rounds_singular : R.string.text_format_rounds;
        String rounds = String.format(context.getString(roundsResId), numRounds);

        subtitleTextView.setText(rounds);

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(getContext().getString(R.string.date_format),
                Locale.getDefault());

        savedTextView.setText(simpleDateFormat.format(new Date(game.getDateSaved())));

        checkBox.setOnCheckedChangeListener(null);
        checkBox.setChecked(checked.contains(game));
        checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    checked.add(game);
                } else {
                    checked.remove(game);
                }
                if (onCheckChangedRunnable != null) {
                    onCheckChangedRunnable.run();
                }
            }
        });

        log.d("saved long is: %s", game.getDateSaved());

        return view;
    }

    private static class ViewWrapper {

        private View view;
        private TextView titleTextView, numPlayersTextView, subtitleTextView, savedTextView;
        private CheckBox checkBox;

        public ViewWrapper(View view) {
            this.view = view;
        }

        public TextView getTitleTextView() {
            if (titleTextView == null) {
                titleTextView = (TextView) view.findViewById(R.id.text_game_title);
            }
            return titleTextView;
        }

        public TextView getNumPlayersTextView() {
            if (numPlayersTextView == null) {
                numPlayersTextView = (TextView) view.findViewById(R.id.text_num_players);
            }
            return numPlayersTextView;
        }

        public TextView getSubtitleTextView() {
            if (subtitleTextView == null) {
                subtitleTextView = (TextView) view.findViewById(R.id.text_game_subtitle);
            }
            return subtitleTextView;
        }

        public TextView getSavedTextView() {
            if (savedTextView == null) {
                savedTextView = (TextView) view.findViewById(R.id.text_date_saved);
            }
            return savedTextView;
        }

        public CheckBox getCheckBox() {
            if (checkBox == null) {
                checkBox = (CheckBox) view.findViewById(android.R.id.checkbox);
            }
            return checkBox;
        }
    }
}
