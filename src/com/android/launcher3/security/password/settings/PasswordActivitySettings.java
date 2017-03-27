package com.android.launcher3.security.password.settings;

/**
 * Created by Michele on 20/03/2017.
 */

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.android.launcher3.R;
import com.android.launcher3.Utilities;


public class PasswordActivitySettings extends AppCompatActivity {

    private TextView textView;
    private EditText passwordInput;

    @Override
    @TargetApi(23)
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password);

        textView = (TextView) findViewById(R.id.errorText);
        passwordInput = (EditText) findViewById(R.id.password_input);

        passwordInput.setOnKeyListener(new View.OnKeyListener() {

            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {

                if (keyCode == KeyEvent.KEYCODE_ENTER
                        && event.getAction() == KeyEvent.ACTION_UP) {

                    String password = ((EditText) v).getText().toString();

                    SharedPreferences preferences = getSharedPreferences(Utilities.PASSWORD_SHARED_PREF, MODE_PRIVATE);
                    String passEncrypted = preferences.getString(Utilities.encrypt("password"), Utilities.encrypt("NULLPASS"));
                    String pass = Utilities.decrypt(passEncrypted);
                    if(!password.equals(pass)){
                        textView.setText(getString(R.string.password_failed));
                    }else{
                        textView.setText(getString(R.string.password_succeded));
                        textView.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimaryDarkFinger));
                        Intent returnIntent = new Intent();
                        returnIntent.putExtra("resultPassword", true);
                        setResult(Activity.RESULT_OK, returnIntent);
                        finish();
                    }

                }

                return false;
            }
        });

    }
}