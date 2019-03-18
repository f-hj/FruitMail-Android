package fr.fruitice.mail;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;

public class OAuthCallback extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_oauth_callback);

        Uri data = getIntent().getData();
        if (data != null && !TextUtils.isEmpty(data.getScheme())) {
            String token = data.toString().replace("fr.fruitice.mail:/oauthCallback#access_token=", "").replace("&token_type=Bearer", "");
            Log.d("res", data.toString());
            Log.d("token", token);

            SharedPreferences sharedPref = getSharedPreferences("fruitmail", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString("token", token);
            Log.d("LoginActivity", token);

            //made this sync for other activity
            editor.apply();

            String fireToken = FirebaseInstanceId.getInstance().getToken();
            new Query(OAuthCallback.this, token).post("/addFirebase", "{\"token\":\"" + fireToken + "\"}");

            startActivity(new Intent(OAuthCallback.this, MainActivity.class));
            finish();
       }
    }
}
