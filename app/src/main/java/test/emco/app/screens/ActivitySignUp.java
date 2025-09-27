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
import test.emco.app.databinding.ActivitySignUpBinding;
import test.emco.app.util.NetworkConnectionCheck;

public class ActivitySignUp extends AppCompatActivity implements View.OnClickListener {
    ActivitySignUpBinding binding;
    String email_address = "", str_password = "", str_country_code = "91";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_sign_up);
        init();
    }

    public void init() {
        binding.btnRegister.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.btnRegister) {
            if (checkValidation()) {
                if (NetworkConnectionCheck.isConnected(this)) {
                    getVerificationCode(email_address, str_country_code);
                } else {
                    Toast.makeText(this, "Please check your internet connection", Toast.LENGTH_LONG).show();

                }
            }
        }
    }

    public boolean checkValidation() {
        email_address = binding.edtEmail.getText().toString();
        str_password = binding.edtpassword.getText().toString();
        if (email_address.trim().isEmpty()) {
            Toast.makeText(this, "Please enter email", Toast.LENGTH_LONG).show();
            return false;
        } else if (str_password.trim().isEmpty()) {
            Toast.makeText(this, "Please enter password", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }


    public void getVerificationCode(String strAccount, String strCountryCode) {
        ThingHomeSdk.getUserInstance().sendVerifyCodeWithUserName(strAccount, "", strCountryCode, 1, new IResultCallback() {
            @Override
            public void onError(String code, String error) {
                Toast.makeText(ActivitySignUp.this, "getValidateCode error:" + error, Toast.LENGTH_LONG).show();

            }

            @Override
            public void onSuccess() {
                Toast.makeText(ActivitySignUp.this, "Got validateCode", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(ActivitySignUp.this, ActivityUpdatePassword.class).putExtra("mail", email_address).putExtra("password", str_password).putExtra("code", strCountryCode).putExtra("from", "signup"));
            }
        });

    }
}