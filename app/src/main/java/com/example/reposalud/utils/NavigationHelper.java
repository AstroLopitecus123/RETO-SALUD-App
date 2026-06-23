package com.example.reposalud.utils;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.content.res.ColorStateList;
import android.graphics.Color;
import com.example.reposalud.R;
import com.example.reposalud.activities.HomeActivity;
import com.example.reposalud.activities.AgendarCitaActivity;
import com.example.reposalud.activities.ListaDoctoresActivity;
import com.example.reposalud.activities.ProfileActivity;

public class NavigationHelper {

    public static void setupBottomNavigation(Activity activity) {
        View btnHome = activity.findViewById(R.id.btnHome);
        View btnAppointments = activity.findViewById(R.id.btnAppointments);
        View btnDoctors = activity.findViewById(R.id.btnDoctors);
        View btnProfile = activity.findViewById(R.id.btnProfile);

        if (btnHome != null) {
            btnHome.setOnClickListener(v -> {
                if (!(activity instanceof HomeActivity)) {
                    Intent intent = new Intent(activity, HomeActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    activity.startActivity(intent);
                }
            });
        }

        if (btnAppointments != null) {
            btnAppointments.setOnClickListener(v -> {
                if (!(activity instanceof AgendarCitaActivity)) {
                    Intent intent = new Intent(activity, AgendarCitaActivity.class);
                    activity.startActivity(intent);
                }
            });
        }

        // HELIAN CAMBIO ESTO
        if (btnDoctors != null) {
            btnDoctors.setOnClickListener(v -> {
                if (!(activity instanceof ListaDoctoresActivity)) {
                    Intent intent = new Intent(activity, ListaDoctoresActivity.class);
                    activity.startActivity(intent);
                }
            });
        }
        //

        if (btnProfile != null) {
            btnProfile.setOnClickListener(v -> {
                if (!(activity instanceof ProfileActivity)) {
                    Intent intent = new Intent(activity, ProfileActivity.class);
                    activity.startActivity(intent);
                }
            });
        }

        updateSelection(activity);
    }

    private static void updateSelection(Activity activity) {
        int activeColor = activity.getResources().getColor(R.color.primary_green);
        int inactiveColor = activity.getResources().getColor(R.color.light_text);
        int white = activity.getResources().getColor(R.color.white);

        resetButton(activity, R.id.containerHome, inactiveColor);
        resetButton(activity, R.id.containerAppointments, inactiveColor);
        resetButton(activity, R.id.containerDoctors, inactiveColor);
        resetButton(activity, R.id.containerProfile, inactiveColor);

        if (activity instanceof HomeActivity) {
            highlightButton(activity, R.id.containerHome, activeColor, white);
        } else if (activity instanceof AgendarCitaActivity) {
            highlightButton(activity, R.id.containerAppointments, activeColor, white);

        // HELIAN CAMBIÓ AQUI
        } else if (activity instanceof ListaDoctoresActivity) {
            highlightButton(activity, R.id.containerDoctors, activeColor, white);
        //

        } else if (activity instanceof ProfileActivity) {
            highlightButton(activity, R.id.containerProfile, activeColor, white);
        }
    }

    private static void resetButton(View container, int color) {
        if (container instanceof LinearLayout) {
            LinearLayout layout = (LinearLayout) container;
            layout.setBackgroundTintList(ColorStateList.valueOf(Color.TRANSPARENT));
            for (int i = 0; i < layout.getChildCount(); i++) {
                View child = layout.getChildAt(i);
                if (child instanceof ImageView) {
                    ((ImageView) child).setColorFilter(color);
                } else if (child instanceof TextView) {
                    ((TextView) child).setTextColor(color);
                    child.setVisibility(View.GONE);
                }
            }
        }
    }

    private static void resetButton(Activity activity, int containerId, int color) {
        View container = activity.findViewById(containerId);
        resetButton(container, color);
    }

    private static void highlightButton(Activity activity, int containerId, int activeColor, int whiteColor) {
        View container = activity.findViewById(containerId);
        if (container instanceof LinearLayout) {
            LinearLayout layout = (LinearLayout) container;
            layout.setBackgroundTintList(ColorStateList.valueOf(activeColor));
            for (int i = 0; i < layout.getChildCount(); i++) {
                View child = layout.getChildAt(i);
                if (child instanceof ImageView) {
                    ((ImageView) child).setColorFilter(whiteColor);
                } else if (child instanceof TextView) {
                    ((TextView) child).setTextColor(whiteColor);
                    child.setVisibility(View.VISIBLE);
                }
            }
        }
    }
}
