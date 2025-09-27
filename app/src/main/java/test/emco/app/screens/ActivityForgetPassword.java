package test.emco.app.screens;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.databinding.DataBindingUtil;

import com.thingclips.smart.home.sdk.ThingHomeSdk;
import com.thingclips.smart.sdk.api.IResultCallback;

import test.emco.app.R;
import test.emco.app.databinding.ActivityForgetPasswordBinding;

public class ActivityForgetPassword extends AppCompatActivity implements View.OnClickListener {
    ActivityForgetPasswordBinding binding;
    String email = "", strCountryCode = "91";
    int mResetPasswordType = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_forget_password);
        init();
    }

    public void init() {
        binding.btnforgetpassword.setOnClickListener(this);
        binding.imgBack.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.imgBack) {
            finish();
        } else if (id == R.id.btnforgetpassword) {
            email = binding.edtEmail.getText().toString();
            if (!email.isEmpty()) {
                sendVerificationCode();
            } else {
                Toast.makeText(this, "Please enter correct email", Toast.LENGTH_LONG).show();
            }
        }
    }

    public void sendVerificationCode() {
        ThingHomeSdk.getUserInstance().sendVerifyCodeWithUserName(
                email,
                "",
                strCountryCode,
                mResetPasswordType,
                new IResultCallback() {
                    @Override
                    public void onError(String code, String error) {
                        Toast.makeText(
                                ActivityForgetPassword.this,
                                "getValidateCode error:" + error,
                                Toast.LENGTH_LONG
                        ).show();
                    }

                    @Override
                    public void onSuccess() {

                        Toast.makeText(
                                ActivityForgetPassword.this,
                                "Got validateCode",
                                Toast.LENGTH_LONG
                        ).show();
                        startActivity(new Intent(ActivityForgetPassword.this, ActivityUpdatePassword.class).putExtra("email", email));
                    }
                });
    }

}