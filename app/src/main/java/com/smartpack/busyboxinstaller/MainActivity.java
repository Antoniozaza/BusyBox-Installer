/*
 * Copyright (C) 2020-2021 sunilpaulmathew <sunil.kde@gmail.com>
 *
 * This file is part of BusyBox Installer: A one-click BusyBox installation utility for Android.
 *
 */

package com.smartpack.busyboxinstaller;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.SubMenu;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.app.ActivityCompat;

import com.facebook.ads.AdSize;
import com.facebook.ads.AdView;
import com.smartpack.busyboxinstaller.utils.RootUtils;
import com.smartpack.busyboxinstaller.utils.Utils;

import java.lang.ref.WeakReference;

/*
 * Created by sunilpaulmathew <sunil.kde@gmail.com> on April 11, 2020
 */

public class MainActivity extends AppCompatActivity {

    private boolean mExit;
    private Handler mHandler = new Handler();
    private LinearLayout mInstall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Initialize App Theme & FaceBook Ads
        Utils.initializeAppTheme(this);
        Utils.initializeFaceBookAds(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Utils.mInstallText = findViewById(R.id.install_text);
        Utils.refreshTitles();

        mInstall = findViewById(R.id.install);
        mInstall.setVisibility(View.VISIBLE);
        AppCompatImageButton settings = findViewById(R.id.settings_menu);
        settings.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(this, settings);
            Menu menu = popupMenu.getMenu();
            if (Utils.existFile("/system/xbin/bb_version")) {
                menu.add(Menu.NONE, 1, Menu.NONE, getString(R.string.remove));
            }
            menu.add(Menu.NONE, 2, Menu.NONE, getString(R.string.dark_theme)).setCheckable(true)
                    .setChecked(Utils.getBoolean("dark_theme", true, this));
            if (Utils.existFile("/system/xbin/busybox_" + Utils.version)) {
                menu.add(Menu.NONE, 3, Menu.NONE, getString(R.string.list_applets));
                menu.add(Menu.NONE, 4, Menu.NONE, getString(R.string.version));
            }
            SubMenu about = menu.addSubMenu(Menu.NONE, 0, Menu.NONE, getString(R.string.about));
            about.add(Menu.NONE, 5, Menu.NONE, getString(R.string.share));
            about.add(Menu.NONE, 6, Menu.NONE, getString(R.string.source_code));
            about.add(Menu.NONE, 7, Menu.NONE, getString(R.string.support_group));
            if (Utils.isNotDonated(this)) {
                about.add(Menu.NONE, 8, Menu.NONE, getString(R.string.donations));
            }
            about.add(Menu.NONE, 9, Menu.NONE, getString(R.string.about));
            popupMenu.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case 0:
                        break;
                    case 1:
                        removeBusyBox();
                        break;
                    case 2:
                        switchTheme();
                        break;
                    case 3:
                        new AlertDialog.Builder(this)
                                .setIcon(R.mipmap.ic_launcher)
                                .setTitle(R.string.list_applets)
                                .setMessage(getString(R.string.list_applets_summary, Utils.getAppletsList().replace("\n", "\n - ")))
                                .setPositiveButton(R.string.cancel, (dialog, which) -> {
                                })
                                .show();
                        break;
                    case 4:
                        new AlertDialog.Builder(this)
                                .setIcon(R.mipmap.ic_launcher)
                                .setTitle(R.string.busybox_version)
                                .setMessage(Utils.getBusyBoxVersion())
                                .setPositiveButton(R.string.cancel, (dialog, which) -> {
                                })
                                .show();
                        break;
                    case 5:
                        shareApp();
                        break;
                    case 6:
                        Utils.launchUrl("https://github.com/SmartPack/BusyBox-Installer", this);
                        break;
                    case 7:
                        Utils.launchUrl("https://t.me/smartpack_kmanager", this);
                        break;
                    case 8:
                        donateToMe();
                        break;
                    case 9:
                        aboutDialog();
                        break;
                }
                return false;
            });
            popupMenu.show();
        });

        // Initialize Banner Ads
        if (Utils.isNotDonated(this)) {
            AdView mAdView = new AdView(this, "660748211413849_660748534747150", AdSize.BANNER_HEIGHT_50);
            LinearLayout adContainer = findViewById(R.id.banner_container);
            adContainer.addView(mAdView);
            mAdView.loadAd();
        }
    }

    public void installDialog(View view) {
        if (!RootUtils.rootAccess()) {
            Utils.snackbar(mInstall, getString(R.string.no_root_message));
            return;
        }
        if (!Utils.checkWriteStoragePermission(this)) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
            Utils.snackbar(mInstall, getString(R.string.no_permission_message));
            return;
        }
        AlertDialog.Builder install = new AlertDialog.Builder(this);
        install.setIcon(R.mipmap.ic_launcher);
        if (Utils.getArch().equals("aarch64") || Utils.getArch().equals("armv7l") || Utils.getArch().equals("i686")) {
            if (Utils.existFile("/system/xbin/bb_version")) {
                if (Utils.readFile("/system/xbin/bb_version").equals(Utils.version)) {
                    install.setTitle(R.string.updated_message);
                    install.setMessage(getString(R.string.install_busybox_latest, Utils.version));
                    install.setPositiveButton(R.string.cancel, (dialog, which) -> {
                    });
                } else {
                    install.setTitle(R.string.update_busybox);
                    install.setMessage(getString(R.string.install_busybox_update, Utils.version));
                    install.setNegativeButton(R.string.cancel, (dialog, which) -> {
                    });
                    install.setPositiveButton(R.string.update, (dialog, which) -> {
                        Utils.installBusyBox(new WeakReference<>(this));
                    });
                }
            } else {
                install.setTitle(R.string.install_busybox);
                install.setMessage((getString(R.string.install_busybox_message, Utils.version)));
                install.setNegativeButton(R.string.cancel, (dialog, which) -> {
                });
                install.setPositiveButton(R.string.install, (dialog, which) -> {
                    Utils.installBusyBox(new WeakReference<>(this));
                });
            }
        } else {
            install.setTitle(R.string.upsupported);
            install.setMessage(getString(R.string.install_busybox_unavailable, Utils.getArch()));
            install.setPositiveButton(R.string.cancel, (dialog, which) -> {
            });
        }
        install.show();
    }

    public void removeBusyBox() {

            new AlertDialog.Builder(this)
                    .setMessage(getString(R.string.remove_busybox_message, Utils.version))
                    .setNegativeButton(R.string.cancel, (dialog, which) -> {
                    })
                    .setPositiveButton(R.string.remove, (dialog, which) -> {
                        Utils.removeBusyBox(new WeakReference<>(this));
                    })
                    .show();
    }

    public void donateToMe() {
        new AlertDialog.Builder(this)
                .setIcon(R.mipmap.ic_launcher)
                .setTitle(getString(R.string.support_developer))
                .setMessage(getString(R.string.support_developer_message))
                .setNeutralButton(getString(R.string.cancel), (dialog1, id1) -> {
                })
                .setPositiveButton(getString(R.string.donation_app), (dialogInterface, i) -> {
                    Utils.launchUrl("https://play.google.com/store/apps/details?id=com.smartpack.donate", this);
                })
                .show();
    }

    public void switchTheme() {
        if (Utils.getBoolean("dark_theme", true, this)) {
            Utils.saveBoolean("dark_theme", false, this);
            Utils.snackbar(mInstall, getString(R.string.switch_theme, getString(R.string.light)));
        } else {
            Utils.snackbar(mInstall, getString(R.string.switch_theme, getString(R.string.dark)));
            Utils.saveBoolean("dark_theme", true, this);
        }
        Utils.sleep(1);
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    public void shareApp() {
        Intent shareapp = new Intent();
        shareapp.setAction(Intent.ACTION_SEND);
        shareapp.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
        shareapp.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_app, "v" + BuildConfig.VERSION_NAME));
        shareapp.setType("text/plain");
        Intent shareIntent = Intent.createChooser(shareapp, null);
        startActivity(shareIntent);
    }

    public void aboutDialog() {
        new AlertDialog.Builder(this)
        .setIcon(R.mipmap.ic_launcher_round)
                .setCancelable(false)
        .setTitle(getString(R.string.app_name) + "\nv" + BuildConfig.VERSION_NAME)
                .setMessage(R.string.about_summary)
                .setPositiveButton(R.string.cancel, (dialog, which) -> {
                })
                .show();
    }

    @Override
    public void onStart() {
        super.onStart();

        if (!RootUtils.rootAccess() || !Utils.checkWriteStoragePermission(this)
                || !Utils.getBoolean("update_dialogue", true, this)) {
            return;
        }

        if (Utils.getArch().equals("aarch64") || Utils.getArch().equals("armv7l") || Utils.getArch().equals("i686")) {
            if (Utils.existFile("/system/xbin/bb_version") && !Utils.readFile("/system/xbin/bb_version").equals(Utils.version)) {
                View checkBoxView = View.inflate(this, R.layout.rv_checkbox, null);
                CheckBox checkBox = checkBoxView.findViewById(R.id.checkbox);
                checkBox.setText(getString(R.string.hide));
                checkBox.setChecked(false);
                checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (isChecked) {
                        Utils.saveBoolean("update_dialogue", false, this);
                    }
                });

                AlertDialog.Builder update = new AlertDialog.Builder(this);
                update.setIcon(R.mipmap.ic_launcher);
                update.setTitle(getString(R.string.update_busybox));
                update.setMessage(getString(R.string.install_busybox_update, Utils.version));
                update.setCancelable(false);
                update.setView(checkBoxView);
                update.setNegativeButton(R.string.cancel, (dialog, which) -> {
                });
                update.setPositiveButton(R.string.update, (dialog, which) -> Utils.installBusyBox(new WeakReference<>(this)));
                update.show();
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (mExit) {
            mExit = false;
            super.onBackPressed();
        } else {
            Utils.snackbar(mInstall, getString(R.string.press_back));
            mExit = true;
            mHandler.postDelayed(() -> mExit = false, 2000);
        }
    }

}