package com.example.summer.utils;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.content.ContextCompat;

import com.example.summer.MainActivity;
import com.example.summer.R;
import com.example.summer.TicketPurchaseActivity;
import com.example.summer.ui.notifications.PersonalInfoActivity;

public class NavigationHelper {

    public static void navigateToTicket(Context context) {
        Intent intent = new Intent(context, TicketPurchaseActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }

    public static void navigateToProfile(Context context) {
        Intent intent = new Intent(context, PersonalInfoActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }

    public static void navigateToHome(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public static boolean hasLocationPermission(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) 
                == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) 
                == PackageManager.PERMISSION_GRANTED;
    }

    public static void startLocationService(Context context) {
        if (!hasLocationPermission(context)) {
            return;
        }
        Intent intent = new Intent(context, com.example.summer.service.LocationService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent);
        } else {
            context.startService(intent);
        }
    }

    public static void stopLocationService(Context context) {
        Intent intent = new Intent(context, com.example.summer.service.LocationService.class);
        context.stopService(intent);
    }
}
