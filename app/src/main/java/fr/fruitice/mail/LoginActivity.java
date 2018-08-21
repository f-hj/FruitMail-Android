package fr.fruitice.mail;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.FirebaseApp;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.Gson;

import fr.fruitice.mail.Objects.LoginReturn;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        final EditText user = (EditText) findViewById(R.id.user);
        final EditText pass = (EditText) findViewById(R.id.pass);

        pass.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {

                final ProgressDialog progressDialog = new ProgressDialog(LoginActivity.this);
                progressDialog.setIndeterminate(true);
                progressDialog.setMessage("Sending...");
                progressDialog.show();

                //JSON BY HAND!!!
                new Query(LoginActivity.this) {
                    @Override
                    public void result(String data) {
                        Log.d("LoginActivity", data);
                        progressDialog.cancel();
                        Gson gson = new Gson();
                        LoginReturn login = gson.fromJson(data, LoginReturn.class);
                        if (login.token != null) {
                            SharedPreferences sharedPref = getSharedPreferences("fruitmail", MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPref.edit();
                            editor.putString("token", login.token);
                            Log.d("LoginActivity", login.token);

                            //made this sync for other activity
                            editor.apply();

                            String token = FirebaseInstanceId.getInstance().getToken();
                            new Query(LoginActivity.this, login.token).post("/addFirebase", "{\"token\":\"" + token + "\"}");

                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                            finish();
                        } else {
                            new AlertDialog.Builder(LoginActivity.this)
                                    .setTitle("Error")
                                    .setMessage(data)
                                    .show();
                        }
                    }
                }.post("/login", "{\"user\":\"" + user.getText() + "\",\"pass\":\"" + pass.getText() + "\"}");
                return true;
            }
        });
    }
}
