package com.sora.crashbandicootlauncher;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    private static final int REQUEST_GAME_IMAGE = 1;
    private static final String PREFERENCES = "launcher";
    private static final String GAME_URI = "game_uri";

    private Uri gameUri;
    private TextView gameSelection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(getColor(R.color.background));
        getWindow().setNavigationBarColor(getColor(R.color.background));
        setContentView(createContent());

        String savedUri = getPreferences(MODE_PRIVATE).getString(GAME_URI, null);
        if (savedUri != null) {
            gameUri = Uri.parse(savedUri);
            showGameSelection();
        }
    }

    private LinearLayout createContent() {
        int padding = getResources().getDimensionPixelSize(R.dimen.screen_padding);
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setGravity(Gravity.CENTER_VERTICAL);
        content.setPadding(padding, padding, padding, padding);
        content.setBackgroundColor(getColor(R.color.background));

        TextView title = new TextView(this);
        title.setText(R.string.title);
        title.setTextColor(getColor(R.color.orange));
        title.setTextSize(28);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        content.addView(title);

        TextView subtitle = new TextView(this);
        subtitle.setText(R.string.subtitle);
        subtitle.setTextColor(getColor(R.color.muted));
        subtitle.setTextSize(14);
        LinearLayout.LayoutParams subtitleParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        subtitleParams.topMargin = getResources().getDimensionPixelSize(R.dimen.small_gap);
        subtitleParams.bottomMargin = getResources().getDimensionPixelSize(R.dimen.section_gap);
        content.addView(subtitle, subtitleParams);

        TextView selectionLabel = new TextView(this);
        selectionLabel.setText(R.string.selected_image);
        selectionLabel.setTextColor(getColor(R.color.foreground));
        selectionLabel.setTextSize(16);
        content.addView(selectionLabel);

        gameSelection = new TextView(this);
        gameSelection.setText(R.string.no_image_selected);
        gameSelection.setTextColor(getColor(R.color.muted));
        gameSelection.setTextSize(14);
        gameSelection.setMaxLines(3);
        LinearLayout.LayoutParams selectionParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        selectionParams.topMargin = getResources().getDimensionPixelSize(R.dimen.small_gap);
        selectionParams.bottomMargin = getResources().getDimensionPixelSize(R.dimen.section_gap);
        content.addView(gameSelection, selectionParams);

        Button chooseButton = new Button(this);
        chooseButton.setText(R.string.choose_image);
        chooseButton.setOnClickListener(view -> chooseGameImage());
        content.addView(chooseButton, fullWidthParams());

        Button launchButton = new Button(this);
        launchButton.setText(R.string.launch_game);
        launchButton.setTextColor(getColor(R.color.launch_text));
        launchButton.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(getColor(R.color.orange)));
        launchButton.setOnClickListener(view -> launchGame());
        LinearLayout.LayoutParams launchParams = fullWidthParams();
        launchParams.topMargin = getResources().getDimensionPixelSize(R.dimen.small_gap);
        content.addView(launchButton, launchParams);

        return content;
    }

    private LinearLayout.LayoutParams fullWidthParams() {
        return new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    private void chooseGameImage() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        try {
            startActivityForResult(intent, REQUEST_GAME_IMAGE);
        } catch (ActivityNotFoundException error) {
            Toast.makeText(this, R.string.file_picker_unavailable, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != REQUEST_GAME_IMAGE || resultCode != RESULT_OK
                || data == null || data.getData() == null) {
            return;
        }

        Uri selectedUri = data.getData();
        int grantFlags = data.getFlags() & Intent.FLAG_GRANT_READ_URI_PERMISSION;
        try {
            getContentResolver().takePersistableUriPermission(selectedUri, grantFlags);
            getPreferences(MODE_PRIVATE).edit().putString(GAME_URI, selectedUri.toString()).apply();
        } catch (SecurityException error) {
            Toast.makeText(this, R.string.selection_not_persisted, Toast.LENGTH_LONG).show();
        }
        gameUri = selectedUri;
        showGameSelection();
    }

    private void showGameSelection() {
        String displayName = getDisplayName(gameUri);
        gameSelection.setText(displayName == null ? gameUri.toString() : displayName);
    }

    private String getDisplayName(Uri uri) {
        try (Cursor cursor = getContentResolver().query(
                uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int nameColumn = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (nameColumn >= 0) {
                    return cursor.getString(nameColumn);
                }
            }
        } catch (SecurityException error) {
            Toast.makeText(this, R.string.image_name_unavailable, Toast.LENGTH_LONG).show();
            return uri.getLastPathSegment();
        }
        return uri.getLastPathSegment();
    }

    private void launchGame() {
        if (gameUri == null) {
            Toast.makeText(this, R.string.choose_image_first, Toast.LENGTH_SHORT).show();
            return;
        }

        Intent openImage = new Intent(Intent.ACTION_VIEW);
        openImage.setDataAndType(gameUri, "*/*");
        openImage.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        try {
            startActivity(Intent.createChooser(openImage, getString(R.string.choose_emulator)));
        } catch (ActivityNotFoundException error) {
            Toast.makeText(this, R.string.emulator_unavailable, Toast.LENGTH_LONG).show();
        } catch (SecurityException error) {
            Toast.makeText(this, R.string.image_access_unavailable, Toast.LENGTH_LONG).show();
        }
    }
}
