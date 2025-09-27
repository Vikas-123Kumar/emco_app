package test.emco.app.screens;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.databinding.DataBindingUtil;

import com.thingclips.smart.android.common.utils.ValidatorUtil;
import com.thingclips.smart.android.user.api.ILoginCallback;
import com.thingclips.smart.android.user.bean.User;
import com.thingclips.smart.home.sdk.ThingHomeSdk;

import test.emco.app.R;
import test.emco.app.databinding.ActivityLoginBinding;
import test.emco.app.util.NetworkConnectionCheck;

public class ActivityLogin extends AppCompatActivity implements View.OnClickListener {
    ActivityLoginBinding binding;
    String str_account_detail = "", str_password = "", str_country_code = "91";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login);
        init();
    }

    public void init() {
        binding.imgBack.setOnClickListener(this);
        binding.btnLogin.setOnClickListener(this);
        binding.txtForgetPass.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.imgBack) {
            finish();
        } else if (id == R.id.btnLogin) {
            if (checkValidation()) {
                if (NetworkConnectionCheck.isConnected(this)) {
                    loginInSDK();
                } else {
                    Toast.makeText(this, R.string.please_check_your_internet_connection, Toast.LENGTH_LONG).show();
                }
            }
        } else if (id == R.id.txt_forget_pass) {
            startActivity(new Intent(this, ActivityForgetPassword.class));
        }
    }

    public boolean checkValidation() {
        str_password = binding.edtpassword.getText().toString();
        str_account_detail = binding.edtEmail.getText().toString();
        if (str_account_detail.isBlank()) {
            Toast.makeText(this, "Please enter email.", Toast.LENGTH_LONG).show();
            return false;
        } else if (str_password.isBlank()) {
            Toast.makeText(this, "Please enter password.", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    public void loginInSDK() {

        ILoginCallback callback = new ILoginCallback() {
            @Override
            public void onSuccess(User user) {
                Toast.makeText(ActivityLogin.this, "Login successfully.", Toast.LENGTH_LONG).show();
                startActivity(new Intent(ActivityLogin.this, ActivityHome.class));
                finish();
            }

            @Override
            public void onError(String code, String error) {
                Toast.makeText(ActivityLogin.this,
                        "code: " + code + "error:" + error,
                        Toast.LENGTH_SHORT).show();
            }
        };
        if (ValidatorUtil.isEmail(str_account_detail)) {
            ThingHomeSdk.getUserInstance().loginWithEmail(str_country_code, str_account_detail, str_password, callback);
        } else {
            //ThingHomeSdk.getUserInstance().loginWithPhonePassword(str_country_code, strAccount, str_password, callback);
        }
    }

    boolean isShow = false;
   /* private void showAndHidePassword() {
        try {
            if (!isShow) {
                binding.edtPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                binding.edtPassword.setSelection(binding.edtPassword.getText().length());
                isShow = true;
                binding.imgShowHidePassword.setImageDrawable(getDrawable(R.drawable.ic_visibility_black));
            } else {
                binding.edtPassword.setTransformationMethod(new PasswordTransformationMethod());
                binding.edtPassword.setSelection(binding.edtPassword.getText().length());
                binding.imgShowHidePassword.setImageDrawable(getDrawable(R.drawable.ic_visibility_off_black));

                isShow = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }*/
}