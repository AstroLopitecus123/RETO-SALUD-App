package com.example.reposalud.activities;

import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.example.reposalud.R;

public class BaseActivity extends AppCompatActivity {

    private ConnectivityManager.NetworkCallback networkCallback;
    private ConnectivityManager connectivityManager;
    private boolean isCurrentlyOffline = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Comprobar estado inicial
        isCurrentlyOffline = !com.example.reposalud.utils.NetworkUtils.isNetworkAvailable(this);
        updateOfflineUI(isCurrentlyOffline, false);
        registerNetworkCallback();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterNetworkCallback();
    }

    private void registerNetworkCallback() {
        if (connectivityManager == null) return;

        NetworkRequest networkRequest = new NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build();

        networkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(@NonNull Network network) {
                if (isCurrentlyOffline) {
                    isCurrentlyOffline = false;
                    runOnUiThread(() -> updateOfflineUI(false, true));
                }
            }

            @Override
            public void onLost(@NonNull Network network) {
                if (!isCurrentlyOffline) {
                    isCurrentlyOffline = true;
                    runOnUiThread(() -> updateOfflineUI(true, true));
                }
            }
        };

        try {
            connectivityManager.registerNetworkCallback(networkRequest, networkCallback);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void unregisterNetworkCallback() {
        if (connectivityManager != null && networkCallback != null) {
            try {
                connectivityManager.unregisterNetworkCallback(networkCallback);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void updateOfflineUI(boolean isOffline, boolean showToast) {
        View tvOfflineMode = findViewById(R.id.tvOfflineMode);
        if (tvOfflineMode != null) {
            tvOfflineMode.setVisibility(isOffline ? View.VISIBLE : View.GONE);
        }

        View tvOfflineModeLogin = findViewById(R.id.tvOfflineModeLogin);
        if (tvOfflineModeLogin != null) {
            tvOfflineModeLogin.setVisibility(isOffline ? View.VISIBLE : View.GONE);
        }

        if (showToast) {
            if (isOffline) {
                Toast.makeText(this, "No se detecta una conexión, cambiando a modo sin conexión", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Conexión restaurada, modo online activado", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
