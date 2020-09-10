
package com.franznkemaka;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Color;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.BaseActivityEventListener;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.InstallState;
import com.google.android.play.core.install.InstallStateUpdatedListener;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.InstallStatus;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.google.android.play.core.tasks.Task;

import java.util.Objects;

import static android.app.Activity.RESULT_OK;

public class RNInAppUpdateModule extends ReactContextBaseJavaModule implements InstallStateUpdatedListener, LifecycleEventListener {
    private AppUpdateManager appUpdateManager;
    private static ReactApplicationContext reactContext;
    private static final int DEFAULT_STALE_DAYS = 5;
    private static final int REQUEST_CODE = 0;
    private Callback updateDownloadedCallback;

    private final ActivityEventListener mActivityEventListener = new BaseActivityEventListener() {
        @Override
        public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent intent) {
            if (requestCode == REQUEST_CODE) {
                if (resultCode != RESULT_OK) {
                    System.out.println("Update flow failed! Result code: " + resultCode);
                    // If the update is cancelled or fails,
                    // you can request to start the update again.
                }
            }
        }
    };

    RNInAppUpdateModule(ReactApplicationContext context) {
        super(context);
        reactContext = context;
        reactContext.addActivityEventListener(mActivityEventListener);
        reactContext.addLifecycleEventListener(this);
    }

    @NonNull
    @Override
    public String getName() {
        return "RNInAppUpdate";
    }

    @ReactMethod
    public void checkUpdate(@Nullable ReadableMap config) {
        int staleDays = config.hasKey("staleDays") ? config.getInt("staleDays") : DEFAULT_STALE_DAYS;        
        appUpdateManager = AppUpdateManagerFactory.create(reactContext);
        appUpdateManager.registerListener(this);
        Task<AppUpdateInfo> appUpdateInfoTask = appUpdateManager.getAppUpdateInfo();
        appUpdateInfoTask.addOnSuccessListener(appUpdateInfo -> {
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                    && appUpdateInfo.clientVersionStalenessDays() != null
                    && appUpdateInfo.clientVersionStalenessDays() > staleDays
                    && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                try {
                    appUpdateManager.startUpdateFlowForResult(
                            appUpdateInfo,
                            AppUpdateType.IMMEDIATE,
                            reactContext.getCurrentActivity(),
                            REQUEST_CODE);
                } catch (IntentSender.SendIntentException e) {
                    e.printStackTrace();
                }
            }else{
                if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                        && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) {
                    try {
                        appUpdateManager.startUpdateFlowForResult(
                                appUpdateInfo,
                                AppUpdateType.FLEXIBLE,
                                reactContext.getCurrentActivity(),
                                REQUEST_CODE);
                    } catch (IntentSender.SendIntentException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    @ReactMethod
    public void onUpdateDownloaded(Callback updateDownloadedCallback) {
        this.updateDownloadedCallback = updateDownloadedCallback;        
    }

    @ReactMethod
    public void forceCompleteUpdate() {
        appUpdateManager.completeUpdate();
    }

    @Override
    public void onStateUpdate(InstallState state) {
        if (state.installStatus() == InstallStatus.DOWNLOADED) {
            handleUpdateDownloaded();
        }
    }

    private void handleUpdateDownloaded() {
        if(this.updateDownloadedCallback != null){
            this.updateDownloadedCallback.invoke();
        };
    }

    @Override
    public void onHostResume() {
        if (appUpdateManager != null) {
            appUpdateManager
                    .getAppUpdateInfo()
                    .addOnSuccessListener(
                            appUpdateInfo -> {
                                if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                                    handleUpdateDownloaded();
                                }
                                if (appUpdateInfo.updateAvailability()
                                        == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                                    try {
                                        appUpdateManager.startUpdateFlowForResult(
                                                appUpdateInfo,
                                                AppUpdateType.IMMEDIATE,
                                                reactContext.getCurrentActivity(),
                                                REQUEST_CODE);
                                    } catch (IntentSender.SendIntentException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
        }
    }

    @Override
    public void onHostPause() {

    }

    @Override
    public void onHostDestroy() {
        if (appUpdateManager != null) {
            appUpdateManager.unregisterListener(this);
        }
    }
}