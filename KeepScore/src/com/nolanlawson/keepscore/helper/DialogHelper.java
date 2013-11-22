package com.nolanlawson.keepscore.helper;

import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;

import com.nolanlawson.keepscore.R;
import com.nolanlawson.keepscore.SettingsActivity;
import com.nolanlawson.keepscore.util.Callback;
import com.nolanlawson.keepscore.util.IntegerUtil;
import com.nolanlawson.keepscore.util.StringUtil;

/**
 * Utilities for building up the delta dialog.
 * 
 * @author nolan
 * 
 */
public class DialogHelper {

    public static interface ResultListener<T> {

	public void onResult(T result);

    }

    public static void showAdditionalDeltasDialog(boolean positive,
	    final ResultListener<Integer> resultListener, final Context context) {

	LayoutInflater layoutInflater = (LayoutInflater) context
		.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	View view = layoutInflater.inflate(R.layout.delta_popup, null);

	prepareDeltaView(view, context);

	final EditText editText = (EditText) view
		.findViewById(android.R.id.edit);
	editText.setSelection(0, editText.getText().length()); // highlight by
							       // default for
							       // easier
							       // deletion

	new AlertDialog.Builder(context)
		.setCancelable(true)
		.setTitle(positive ? R.string.title_add : R.string.title_subtract)
		.setPositiveButton(android.R.string.ok,
			new DialogInterface.OnClickListener() {

			    @Override
			    public void onClick(DialogInterface dialog,
				    int which) {

				if (resultListener != null) {
				    int result = IntegerUtil.parseIntOrZero(editText.getText());

				    resultListener.onResult(result);
				}

				dialog.dismiss();

			    }
			})
		.setNeutralButton(R.string.button_customize,
			new DialogInterface.OnClickListener() {

			    @Override
			    public void onClick(DialogInterface dialog,
				    int which) {
				dialog.dismiss();
				Intent intent = new Intent(context,
					SettingsActivity.class);
				intent.putExtra(
					SettingsActivity.EXTRA_SCROLL_TO_CONFIGURATIONS,
					true);
				context.startActivity(intent);

			    }
			}).setNegativeButton(android.R.string.cancel, null)
		.setView(view).show();

    }

    private static void prepareDeltaView(View view, Context context) {
	// set the buttons based on the preferences

	int button1Value = PreferenceHelper
		.getPopupDeltaButtonValue(0, context);
	int button2Value = PreferenceHelper
		.getPopupDeltaButtonValue(1, context);
	int button3Value = PreferenceHelper
		.getPopupDeltaButtonValue(2, context);
	int button4Value = PreferenceHelper
		.getPopupDeltaButtonValue(3, context);

	Button button1 = (Button) view.findViewById(android.R.id.button1);
	Button button2 = (Button) view.findViewById(android.R.id.button2);
	Button button3 = (Button) view.findViewById(android.R.id.button3);
	Button button4 = (Button) view.findViewById(R.id.button4);
	EditText editText = (EditText) view.findViewById(android.R.id.edit);

	button1.setText(IntegerUtil.toCharSequenceWithSign(button1Value));
	button2.setText(IntegerUtil.toCharSequenceWithSign(button2Value));
	button3.setText(IntegerUtil.toCharSequenceWithSign(button3Value));
	button4.setText(IntegerUtil.toCharSequenceWithSign(button4Value));

	button1.setOnClickListener(incrementingOnClickListener(editText,
		button1Value));
	button2.setOnClickListener(incrementingOnClickListener(editText,
		button2Value));
	button3.setOnClickListener(incrementingOnClickListener(editText,
		button3Value));
	button4.setOnClickListener(incrementingOnClickListener(editText,
		button4Value));

    }

    private static OnClickListener incrementingOnClickListener(
	    final EditText editText, final int delta) {

	return new OnClickListener() {

	    @Override
	    public void onClick(View v) {

		int editTextValue = IntegerUtil.parseIntOrZero(editText.getText());
		editText.setText(Integer.toString(editTextValue + delta));
	    }
	};
    }

    public static void showPlayerNameDialog(final Context context,
	    final int titleResId, final String startingValue, final int newPlayerNumber, 
	    final Callback<String> onResult) {

	LayoutInflater inflater = (LayoutInflater) context
		.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

	final AutoCompleteTextView editText = (AutoCompleteTextView) inflater
		.inflate(R.layout.change_player_name, null, false);
	editText.setHint(context.getString(R.string.text_player) + " "
		+ (newPlayerNumber + 1));
	editText.setText(StringUtil.nullToEmpty(startingValue));

	new AlertDialog.Builder(context)
		.setTitle(titleResId)
		.setView(editText)
		.setCancelable(true)
		.setPositiveButton(android.R.string.ok,
			new DialogInterface.OnClickListener() {

			    @Override
			    public void onClick(DialogInterface dialog,
				    int which) {

				String newName = StringUtil
					.nullToEmpty(editText.getText()
						.toString());

				onResult.onCallback(newName);
				dialog.dismiss();

			    }
			}).setNegativeButton(android.R.string.cancel, null)
		.show();

	// fetch suggestions in the background to avoid jankiness
	new AsyncTask<Void, Void, List<String>>() {

	    @Override
	    protected List<String> doInBackground(Void... params) {
		return PlayerNameHelper.getPlayerNameSuggestions(context);
	    }

	    @Override
	    protected void onPostExecute(List<String> result) {
		super.onPostExecute(result);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(
			context, R.layout.simple_dropdown_small, result);
		editText.setAdapter(adapter);
	    }

	}.execute((Void) null);

    }
}
