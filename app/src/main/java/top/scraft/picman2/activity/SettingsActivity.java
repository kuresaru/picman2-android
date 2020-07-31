package top.scraft.picman2.activity;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import top.scraft.picman2.R;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        SettingsFragment settingsFragment = new SettingsFragment(this);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, settingsFragment)
                .commit();
    }


    public static class SettingsFragment extends PreferenceFragmentCompat {

        private final SettingsActivity settingsActivity;

        public SettingsFragment(SettingsActivity settingsActivity) {
            this.settingsActivity = settingsActivity;
        }

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
            findPreference("server").setOnPreferenceChangeListener((preference, newValue) -> {
                Toast.makeText(settingsActivity, "更改服务器地址需要重启软件生效", Toast.LENGTH_SHORT).show();
                return true;
            });
        }

    }

}
