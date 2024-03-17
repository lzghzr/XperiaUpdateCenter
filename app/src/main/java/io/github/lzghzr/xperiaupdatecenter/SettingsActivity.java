package io.github.lzghzr.xperiaupdatecenter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Insets;
import android.os.Build;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;

@SuppressWarnings("deprecation")
public class SettingsActivity extends Activity {
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_settings);
    checkXperia();
    checkXSharedPreferences();
    if (savedInstanceState == null) {
      getFragmentManager().beginTransaction()
          .add(R.id.fragment_container, new SettingsFragment()).commit();
    }
  }

  private void checkXperia() {
    if (!Build.BRAND.equalsIgnoreCase("Sony")) {
      new AlertDialog.Builder(this)
          .setTitle(R.string.brand_error)
          .setMessage(R.string.brand_not_supported)
          .setPositiveButton(android.R.string.ok, (dialog12, which) -> finish())
          .show();
    }
  }

  @SuppressLint("WorldReadableFiles")
  private void checkXSharedPreferences() {
    try {
      getSharedPreferences("code", Context.MODE_WORLD_READABLE);
    } catch (SecurityException exception) {
      new AlertDialog.Builder(this)
          .setTitle(R.string.config_error)
          .setMessage(R.string.config_not_supported)
          .setPositiveButton(android.R.string.ok, (dialog12, which) -> {
            deleteSharedPreferences("code");
            finish();
          })
          .show();
    }
  }

  public static class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
    @Override
    public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      getPreferenceManager().setSharedPreferencesName("code");
      addPreferencesFromResource(R.xml.prefs);

      String model = Build.MODEL.replace("-", "_");
      @SuppressLint("DiscouragedApi")
      int modelID = getResources().getIdentifier(model, "array", "io.github.lzghzr.xperiaupdatecenter");
      String[] modelList = modelID == 0 ? new String[0] : getResources().getStringArray(modelID);

      String[] entries = new String[modelList.length + 1];
      entries[0] = "default";
      System.arraycopy(modelList, 0, entries, 1, modelList.length);

      String[] entryValues = new String[modelList.length + 1];
      entryValues[0] = "";
      for (int i = 0; i < modelList.length; i++) {
        entryValues[i + 1] = modelList[i].split(" ")[0];
      }

      Preference codePref = findPreference("code");
      setPrefEntries(codePref, entries, entryValues);
      updatePrefSummary(codePref);

      Preference customCodePref = findPreference("custom_code");
      updatePrefSummary(customCodePref);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
      view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
      view.setOnApplyWindowInsetsListener((v, windowInsets) -> {
        Insets insets = windowInsets.getInsets(WindowInsets.Type.systemBars());
        ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
        mlp.leftMargin = insets.left;
        mlp.bottomMargin = insets.bottom;
        mlp.rightMargin = insets.right;
        mlp.topMargin = insets.top;
        v.setLayoutParams(mlp);
        return WindowInsets.CONSUMED;
      });
      super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
      updatePrefSummary(findPreference(key));
    }

    @Override
    public void onResume() {
      super.onResume();
      getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
      super.onPause();
      getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    private void setPrefEntries(Preference pref, CharSequence[] entries, CharSequence[] entryValues) {
      if (pref instanceof ListPreference listPref) {
        listPref.setEntries(entries);
        listPref.setEntryValues(entryValues);
      }
    }

    private void updatePrefSummary(Preference pref) {
      if (pref instanceof ListPreference listPref) {
        pref.setSummary(listPref.getEntry());
      } else if (pref instanceof EditTextPreference editTextPref) {
        pref.setSummary(editTextPref.getText());
      }
    }
  }
}
