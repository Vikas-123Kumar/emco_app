package test.emco.app.screens;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.databinding.DataBindingUtil;

import com.thingclips.smart.android.user.api.IRegisterCallback;
import com.thingclips.smart.android.user.api.IResetPasswordCallback;
import com.thingclips.smart.android.user.bean.User;
import com.thingclips.smart.home.sdk.ThingHomeSdk;

import test.emco.app.R;
import test.emco.app.databinding.ActivityUpdatePasswordBinding;

public class ActivityUpdatePassword extends AppCompatActivity implements View.OnClickListener {
    ActivityUpdatePasswordBinding binding;
    String mail = "", str_otp = "", str_country_code = "91", str_password = "", from = "";
    Handler handler = new Handler(Looper.getMainLooper());
    private EditText[] otpEditTexts;
    int back_click = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_update_password);
        mail = getIntent().getStringExtra("mail");
        from = getIntent().getStringExtra("from");
        str_password = getIntent().getStringExtra("password");
        init();
        startTimer();
    }

    public void init() {
        binding.txtMessage.setText(getString(R.string.please_check_your_mail) + mail + getString(R.string.we_send_you_the_verification_code));
        binding.btnforgetpassword.setOnClickListener(this);
        otpEditTexts = new EditText[6];
        otpEditTexts[0] = binding.otpEditText1;
        otpEditTexts[1] = binding.otpEditText2;
        otpEditTexts[2] = binding.otpEditText3;
        otpEditTexts[3] = binding.otpEditText4;
        otpEditTexts[4] = binding.otpEditText5;
        otpEditTexts[5] = binding.otpEditText6;
        for (int i = 0; i < otpEditTexts.length; i++) {
            final int index = i;
            otpEditTexts[i].setOnTouchListener((v, event) -> {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    EditText editText = (EditText) v;
                    // Ensure the cursor is always at the end of the text
                    editText.setSelection(editText.getText().length());
                }
                return false; // Return false to allow normal behavior (focus, keyboard)
            });

            otpEditTexts[i].addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    Log.d("fgh s", s.length() + "");
                    if (s.length() == 1) {
                        if (index < otpEditTexts.length - 1) {
                            otpEditTexts[index + 1].requestFocus();
                        }
                    } else if (s.length() == 0) {
                        if (index > 0) {
                            otpEditTexts[index - 1].requestFocus();
                        }
                    }
                }
            });
            otpEditTexts[i].setOnKeyListener((v, keyCode, event) -> {
                if (keyCode == KeyEvent.KEYCODE_DEL && event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (index > 0 && otpEditTexts[index].getText().length() == 0) {
                        // Move to the previous field if backspace is pressed on an empty field
                        otpEditTexts[index - 1].requestFocus();
                        return true; // Prevent further backspace handling
                    }
                }
                return false;
            });
        }
    }

    private String readOTP() {
        StringBuilder otp = new StringBuilder();
        for (EditText editText : otpEditTexts) {
            otp.append(editText.getText().toString());
        }
        return otp.toString();
    }

    public void startTimer() {
        new Thread(() -> {
            for (int i = 29; i >= 0; i--) {
                int finalI = i;
                handler.post(() -> binding.txtOtpTime.setText("Please wait 00:" + finalI));

                try {
                    Thread.sleep(1000); // 1 second delay
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                }
            });
        }).start();
    }

    public void verifyOTPSDkForget() {
        IResetPasswordCallback callback = new IResetPasswordCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(ActivityUpdatePassword.this,
                        "Password updated successfully.",
                        Toast.LENGTH_LONG
                ).show();
                startActivity(new Intent(ActivityUpdatePassword.this, ActivityLogin.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));

            }

            @Override
            public void onError(String code, String error) {
                Toast.makeText(
                        ActivityUpdatePassword.this,
                        "Reset Password error:" + error,
                        Toast.LENGTH_LONG
                ).show();
            }
        };

        ThingHomeSdk.getUserInstance().resetEmailPassword(
                str_country_code,
                mail,
                str_otp,
                str_password,
                callback
        );

    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.btnforgetpassword) {
            str_otp = readOTP();
            if (str_otp.length() == 6) {
                if (from != null && from.equals("signup")) {
                    verifyOTPSDk();
                } else {
                    if (back_click == 0) {
                        back_click++;
                        binding.llOtpVerify.setVisibility(View.GONE);
                        binding.llUpdatePass.setVisibility(View.VISIBLE);
                        binding.txtHeader.setText(R.string.set_new_password);
                    } else {
                        verifyOTPSDkForget();
                    }
                }
            } else {
                Toast.makeText(this, "Please enter 6 digit otp", Toast.LENGTH_LONG).show();
            }


        } else if (id == R.id.imgBack) {
            if (back_click == 0) {
                finish();
            } else {
                back_click = 0;
                binding.llOtpVerify.setVisibility(View.VISIBLE);
                binding.llUpdatePass.setVisibility(View.GONE);
                binding.txtHeader.setText(R.string.verify_account);
            }
        }
    }

    public void verifyOTPSDk() {
        Log.d("vikas ", str_password + " " + mail + " " + str_country_code + " " + str_otp);
        IRegisterCallback callback = new IRegisterCallback() {
            @Override
            public void onSuccess(User user) {
                Log.d("fgh user id", user.getUid());
                Toast.makeText(ActivityUpdatePassword.this, "Account register successfully.", Toast.LENGTH_LONG).show();
                startActivity(new Intent(ActivityUpdatePassword.this, ActivityLogin.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));

            }

            //in1745681721816KjeDk
            @Override
            public void onError(String code, String error) {
                Toast.makeText(
                        ActivityUpdatePassword.this,
                        "Register error:" + error,
                        Toast.LENGTH_LONG
                ).show();
            }
        };

        ThingHomeSdk.getUserInstance().registerAccountWithEmail(
                str_country_code,
                mail,
                str_password,
                str_otp,
                callback
        );

    }
}