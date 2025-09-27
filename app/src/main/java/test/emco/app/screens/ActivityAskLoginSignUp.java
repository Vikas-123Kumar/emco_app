package test.emco.app.screens;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.databinding.DataBindingUtil;

import com.thingclips.smart.home.sdk.ThingHomeSdk;

import test.emco.app.R;
import test.emco.app.databinding.ActivityAskLoginSignUpBinding;

public class ActivityAskLoginSignUp extends AppCompatActivity implements View.OnClickListener {
    ActivityAskLoginSignUpBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_ask_login_sign_up);
        init();
    }

    public void init() {
        binding.btnLogin.setOnClickListener(this);
        binding.btnSignUp.setOnClickListener(this);
        if (ThingHomeSdk.getUserInstance().isLogin()) {
            startActivity(new Intent(this, ActivityHome.class));
            finish();
        }
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.btnLogin) {
            startActivity(new Intent(this, ActivityLogin.class));
        } else if (id == R.id.btnSignUp) {
            startActivity(new Intent(this, ActivitySignUp.class));
        }
    }
}