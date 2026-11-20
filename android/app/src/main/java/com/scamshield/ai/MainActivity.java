package com.scamshield.ai;

import com.getcapacitor.BridgeActivity;
import com.scamshield.ai.calls.CallScreeningPlugin;
import com.scamshield.ai.calls.CallerAlertNotifications;
import com.scamshield.ai.calls.CallerSpeechAlerts;
import com.scamshield.ai.calls.ScreeningStore;
import com.scamshield.ai.notifications.NotificationScanningPlugin;
import com.scamshield.ai.settings.SpokenLanguagePlugin;

public class MainActivity extends BridgeActivity {
    @Override
    public void onCreate(android.os.Bundle savedInstanceState) {
        registerPlugin(CallScreeningPlugin.class);
        registerPlugin(NotificationScanningPlugin.class);
        registerPlugin(SpokenLanguagePlugin.class);
        super.onCreate(savedInstanceState);
        CallerAlertNotifications.INSTANCE.ensureChannel(this);
        if (ScreeningStore.INSTANCE.spokenAlertsEnabled(this)) CallerSpeechAlerts.INSTANCE.prepare(this);
    }
}
