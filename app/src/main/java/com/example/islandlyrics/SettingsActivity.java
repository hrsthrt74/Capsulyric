package com.example.islandlyrics;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.bottomsheet.BottomSheetDialog;

public class SettingsActivity extends BaseActivity {

    // Service & General UI References
    private View itemServiceSwitch;
    private MaterialSwitch switchMaster;
    
    private View itemNotificationPerm;
    private MaterialSwitch switchNotification;
    
    private View itemPostNotificationPerm;
    private MaterialSwitch switchPostNotification;
    
    // Help & Guide UI
    private View itemGuide0Hook;
    
    private View itemWhitelist;
    private View itemBattery;
    private View itemGithub;
    private View itemLogs;
    
    // Appearance UI References
    private View itemLanguage;
    private TextView tvCurrentLanguage;
    private View itemFollowSystem;
    private MaterialSwitch switchFollowSystem;
    private View itemDarkMode;
    private MaterialSwitch switchDarkMode;
    private View itemPureBlack;
    private MaterialSwitch switchPureBlack;
    private View itemDynamicColor;
    private MaterialSwitch switchDynamicColor;
    
    // Developer Mode State
    private int devClickCount = 0;
    private long lastDevClickTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Handle Window Insets
        androidx.core.view.ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            androidx.core.graphics.Insets systemBars = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Bind Views
        itemServiceSwitch = findViewById(R.id.item_service_switch);
        switchMaster = findViewById(R.id.switch_master);
        
        itemNotificationPerm = findViewById(R.id.item_notification_perm);
        switchNotification = findViewById(R.id.switch_notification);
        
        itemPostNotificationPerm = findViewById(R.id.item_post_notification_perm);
        switchPostNotification = findViewById(R.id.switch_post_notification);
        
        itemGuide0Hook = findViewById(R.id.item_guide_0_hook);
        
        // Bind Appearance Views
        itemLanguage = findViewById(R.id.item_language);
        tvCurrentLanguage = findViewById(R.id.tv_current_language);
        itemFollowSystem = findViewById(R.id.item_theme_follow_system);
        switchFollowSystem = findViewById(R.id.switch_theme_follow_system);
        itemDarkMode = findViewById(R.id.item_theme_dark_mode);
        switchDarkMode = findViewById(R.id.switch_theme_dark_mode);
        itemPureBlack = findViewById(R.id.item_theme_pure_black);
        switchPureBlack = findViewById(R.id.switch_theme_pure_black);
        itemDynamicColor = findViewById(R.id.item_theme_dynamic_color);
        switchDynamicColor = findViewById(R.id.switch_theme_dynamic_color);

        itemWhitelist = findViewById(R.id.item_whitelist);
        itemBattery = findViewById(R.id.item_battery);
        itemGithub = findViewById(R.id.item_github);
        itemLogs = findViewById(R.id.item_logs);
        
        TextView tvFooter = findViewById(R.id.tv_footer_version);
        try {
            android.content.pm.PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            String version = pInfo.versionName;
            int code = pInfo.versionCode; 
            tvFooter.setText("v" + version + " (" + code + ") - " + (BuildConfig.DEBUG ? "Alpha Debug" : "Release"));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        // Load Preferences & Init UI
        boolean isEnabled = getSharedPreferences("IslandLyricsPrefs", MODE_PRIVATE).getBoolean("service_enabled", true);
        switchMaster.setChecked(isEnabled);

        initAppearanceUI();        
        initDeveloperMode();
        setupClickListeners();
    }

    private void initDeveloperMode() {
        boolean isDevMode = getSharedPreferences("IslandLyricsPrefs", MODE_PRIVATE).getBoolean("dev_mode_enabled", false);
        itemLogs.setVisibility((BuildConfig.DEBUG || isDevMode) ? View.VISIBLE : View.GONE);
    }

    private void initAppearanceUI() {
        android.content.SharedPreferences prefs = getSharedPreferences("IslandLyricsPrefs", MODE_PRIVATE);
        
        // Language
        String lang = prefs.getString("language_code", "");
        if (lang.equals("en")) tvCurrentLanguage.setText("English");
        else if (lang.equals("zh-CN")) tvCurrentLanguage.setText("简体中文");
        else tvCurrentLanguage.setText(getString(R.string.settings_theme_follow_system)); // Reusing "Follow System" string or "Auto"

        // Theme
        boolean followSystem = prefs.getBoolean("theme_follow_system", true);
        switchFollowSystem.setChecked(followSystem);
        
        boolean darkMode = prefs.getBoolean("theme_dark_mode", false);
        switchDarkMode.setChecked(darkMode);
        switchDarkMode.setEnabled(!followSystem);
        itemDarkMode.setAlpha(followSystem ? 0.5f : 1.0f);

        boolean pureBlack = prefs.getBoolean("theme_pure_black", false);
        switchPureBlack.setChecked(pureBlack);

        boolean dynamicColor = prefs.getBoolean("theme_dynamic_color", true);
        switchDynamicColor.setChecked(dynamicColor);
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkPermissionsAndSyncUI();
    }

    private void setupClickListeners() {
        // --- Service Switch ---
        itemServiceSwitch.setOnClickListener(v -> {
            switchMaster.toggle();
            boolean newState = switchMaster.isChecked();
            getSharedPreferences("IslandLyricsPrefs", MODE_PRIVATE)
                .edit().putBoolean("service_enabled", newState).apply();
        });
        switchMaster.setOnCheckedChangeListener((buttonView, isChecked) -> {
             getSharedPreferences("IslandLyricsPrefs", MODE_PRIVATE)
                .edit().putBoolean("service_enabled", isChecked).apply();
        });

        // --- Notification Permission ---
        itemNotificationPerm.setOnClickListener(v -> {
            boolean hasPermission = NotificationManagerCompat.getEnabledListenerPackages(this).contains(getPackageName());
            if (hasPermission) {
                // If already granted, open settings to allow user to revoke
                startActivity(new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS));
            } else {
                showPrivacyDisclaimer();
            }
        });

        // --- Post Notification Permission (Android 13+) ---
        itemPostNotificationPerm.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= 33) {
                 if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                     // Open App Settings to revoke if they want
                     Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                     intent.setData(Uri.fromParts("package", getPackageName(), null));
                     startActivity(intent);
                 } else {
                     ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
                 }
            } else {
                 android.widget.Toast.makeText(this, "Not required for this Android version", android.widget.Toast.LENGTH_SHORT).show();
            }
        });
        
        // --- Guide ---
        itemGuide0Hook.setOnClickListener(v -> show0HookGuide());

        // --- Appearance Logic ---
        
        // Language
        itemLanguage.setOnClickListener(v -> showLanguageDialog());

        // Follow System
        itemFollowSystem.setOnClickListener(v -> switchFollowSystem.toggle());
        switchFollowSystem.setOnCheckedChangeListener((buttonView, isChecked) -> {
            ThemeHelper.setFollowSystem(this, isChecked);
            initAppearanceUI(); // Refresh UI state (enable/disable dark mode switch)
        });

        // Dark Mode
        itemDarkMode.setOnClickListener(v -> {
            if (switchDarkMode.isEnabled()) switchDarkMode.toggle();
        });
        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (buttonView.isPressed() || itemDarkMode.isPressed()) { // Avoid recursion if set via code
                ThemeHelper.setDarkMode(this, isChecked);
            }
        });

        // Pure Black
        itemPureBlack.setOnClickListener(v -> switchPureBlack.toggle());
        switchPureBlack.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (buttonView.isPressed() || itemPureBlack.isPressed()) {
                ThemeHelper.setPureBlack(this, isChecked);
                recreate();
            }
        });

        // Dynamic Colors
        itemDynamicColor.setOnClickListener(v -> switchDynamicColor.toggle());
        switchDynamicColor.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (buttonView.isPressed() || itemDynamicColor.isPressed()) {
                ThemeHelper.setDynamicColor(this, isChecked);
                recreate(); // Dynamic Colors need Activity recreation
            }
        });

        // --- Other Items ---
        itemWhitelist.setOnClickListener(v -> startActivity(new Intent(this, WhitelistActivity.class)));
        
        itemBattery.setOnClickListener(v -> {
             Intent intent = new Intent(android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
             intent.setData(Uri.parse("package:" + getPackageName()));
             startActivity(intent);
        });
        
        itemGithub.setOnClickListener(v -> {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/FrancoGiudans/Capsulyric")); 
            startActivity(browserIntent);
        });
        
        itemLogs.setOnClickListener(v -> showLogConsole());
        
        // Developer Mode Trigger (Secret)
        TextView tvFooter = findViewById(R.id.tv_footer_version);
        tvFooter.setOnClickListener(v -> {
             long currentTime = System.currentTimeMillis();
             if (currentTime - lastDevClickTime > 1000) {
                 devClickCount = 0;
             }
             devClickCount++;
             lastDevClickTime = currentTime;
             
             if (devClickCount >= 3 && devClickCount < 7) {
                 android.widget.Toast.makeText(this, (7 - devClickCount) + " steps away from developer mode...", android.widget.Toast.LENGTH_SHORT).show();
             } else if (devClickCount == 7) {
                 // Enable Dev Mode
                 getSharedPreferences("IslandLyricsPrefs", MODE_PRIVATE).edit().putBoolean("dev_mode_enabled", true).apply();
                 itemLogs.setVisibility(View.VISIBLE);
                 android.widget.Toast.makeText(this, "Developer Mode Enabled! 👩‍💻", android.widget.Toast.LENGTH_SHORT).show();
             }
        });
    }

    private void showLanguageDialog() {
        String[] languages = {"System Default", "English", "简体中文"};
        String[] codes = {"", "en", "zh-CN"};
        
        new AlertDialog.Builder(this)
            .setTitle(R.string.settings_language)
            .setItems(languages, (dialog, which) -> {
                ThemeHelper.setLanguage(this, codes[which]);
                recreate();
            })
            .show();
    }

    private void checkPermissionsAndSyncUI() {
        // Sync Notification Switch
        boolean listenerGranted = NotificationManagerCompat.getEnabledListenerPackages(this).contains(getPackageName());
        switchNotification.setChecked(listenerGranted);
        
        // Check POST_NOTIFICATIONS (Android 13+) - Independent check
        if (Build.VERSION.SDK_INT >= 33) {
            boolean postGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED;
            switchPostNotification.setChecked(postGranted);
        } else {
            switchPostNotification.setChecked(true); // Always true on older Androids
            switchPostNotification.setEnabled(false);
            itemPostNotificationPerm.setAlpha(0.5f);
        }
    }

    private void show0HookGuide() {
        new AlertDialog.Builder(this)
            .setTitle(R.string.guide_title)
            .setMessage(android.text.Html.fromHtml(getString(R.string.guide_message), android.text.Html.FROM_HTML_MODE_COMPACT))
            .setPositiveButton(android.R.string.ok, null)
            .show();
    }

    private void showPrivacyDisclaimer() {
        new AlertDialog.Builder(this)
            .setTitle(R.string.dialog_privacy_title)
            .setMessage(getString(R.string.dialog_privacy_message))
            .setPositiveButton(R.string.dialog_btn_understand, (dialog, which) -> {
                Intent intent = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
                startActivity(intent);
            })
            .setNegativeButton(R.string.dialog_btn_cancel, null)
            .show();
    }
    
    private void showLogConsole() {
        BottomSheetDialog bottomSheet = new BottomSheetDialog(this);
        
        TextView logView = new TextView(this);
        logView.setPadding(32, 32, 32, 32);
        logView.setTextIsSelectable(true);
        logView.setTypeface(android.graphics.Typeface.MONOSPACE);
        
        androidx.core.widget.NestedScrollView scroll = new androidx.core.widget.NestedScrollView(this);
        scroll.addView(logView);
        
        bottomSheet.setContentView(scroll);
        bottomSheet.show();
        
        AppLogger.getInstance().getLogs().observe(this, logs -> {
            logView.setText(logs);
            scroll.post(() -> scroll.fullScroll(View.FOCUS_DOWN));
        });
    }
}

