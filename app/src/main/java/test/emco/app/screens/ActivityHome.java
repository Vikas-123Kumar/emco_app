package test.emco.app.screens;

import android.animation.ValueAnimator;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import test.emco.app.R;

public class ActivityHome extends AppCompatActivity {
    private LinearLayout bottomNav;
    private TextView textSelected;
    private int selectedIndex = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        bottomNav = findViewById(R.id.bottomNav);
        //textSelected = findViewById(R.id.textSelected);

        setupBottomNav();
    }

    private void setupBottomNav() {
        for (int i = 0; i < bottomNav.getChildCount(); i++) {
            View navItem = bottomNav.getChildAt(i);
            int index = i;

            LinearLayout itemLayout = navItem.findViewById(R.id.navItemLayout);
            ImageView icon = navItem.findViewById(R.id.navIcon);
            TextView label = navItem.findViewById(R.id.navLabel);

            // Set icons and labels here
            switch (i) {
                case 0:
                    icon.setImageResource(R.drawable.home_2);
                    label.setText("Home");
                    break;
                case 1:
                    icon.setImageResource(R.drawable.airpod);
                    label.setText("Devices");
                    break;
                case 2:
                    icon.setImageResource(R.drawable.camera);
                    label.setText("Camera");
                    break;
                case 3:
                    icon.setImageResource(R.drawable.profile);
                    label.setText("Profile");
                    break;
            }

            itemLayout.setOnClickListener(v -> {
                updateSelection(index);
            });

            // Initialize with default selection
            if (i == selectedIndex) {
                expandItem(itemLayout, label);
                itemLayout.setBackgroundResource(R.drawable.nav_item_bg_red);

            } else {
                collapseItem(itemLayout, label);
                itemLayout.setBackgroundResource(R.drawable.nav_item_bg);
            }
        }
    }

    private void updateSelection(int newIndex) {
        if (newIndex == selectedIndex) return;

        // Reset previous
        View prevItem = bottomNav.getChildAt(selectedIndex);
        collapseItem(prevItem.findViewById(R.id.navItemLayout), prevItem.findViewById(R.id.navLabel));
        prevItem.findViewById(R.id.navItemLayout).setBackgroundColor(R.drawable.nav_item_bg);

        // Expand new
        View newItem = bottomNav.getChildAt(newIndex);
        expandItem(newItem.findViewById(R.id.navItemLayout), newItem.findViewById(R.id.navLabel));
        newItem.findViewById(R.id.navItemLayout).setBackgroundResource(R.drawable.nav_item_bg_red);

        selectedIndex = newIndex;
        //textSelected.setText("Selected: " + ((TextView)newItem.findViewById(R.id.navLabel)).getText());
    }

    private void expandItem(View itemLayout, TextView label) {
        label.setVisibility(View.VISIBLE);
        ViewGroup.LayoutParams params = itemLayout.getLayoutParams();
        ValueAnimator animator = ValueAnimator.ofInt(itemLayout.getWidth(), dpToPx(120));
        animator.addUpdateListener(animation -> {
            params.width = (int) animation.getAnimatedValue();
            itemLayout.setLayoutParams(params);
        });
        animator.setDuration(300);
        animator.start();
    }

    private void collapseItem(View itemLayout, TextView label) {
        label.setVisibility(View.GONE);
        ViewGroup.LayoutParams params = itemLayout.getLayoutParams();
        ValueAnimator animator = ValueAnimator.ofInt(itemLayout.getWidth(), dpToPx(48));
        animator.addUpdateListener(animation -> {
            params.width = (int) animation.getAnimatedValue();
            itemLayout.setLayoutParams(params);
        });
        animator.setDuration(300);
        animator.start();
    }

    private int dpToPx(int dp) {
        return (int)(dp * getResources().getDisplayMetrics().density);
    }
}