package fr.fruitice.mail;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.gson.Gson;

import java.util.ArrayList;

import fr.fruitice.mail.Objects.Address;
import fr.fruitice.mail.Objects.DomainAddress;
import fr.fruitice.mail.Objects.MailWrited;
import fr.fruitice.mail.Objects.MailWritedReturn;
import fr.fruitice.mail.Objects.NodeMailerInfo;
import fr.fruitice.mail.Objects.UserConfig;

public class WriteMailActivity extends AppCompatActivity {

    MailWrited m;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_mail);

        setTitle("");

        Intent intent = getIntent();
        String mAction = intent.getAction();

        EditText mToView = (EditText) findViewById(R.id.to);
        EditText mSubjectView = (EditText) findViewById(R.id.subject);
        EditText mMessageContentView = (EditText) findViewById(R.id.text);

        if (Intent.ACTION_VIEW.equals(mAction)
                || Intent.ACTION_SENDTO.equals(mAction)
                || Intent.ACTION_SEND.equals(mAction)
                || Intent.ACTION_SEND_MULTIPLE.equals(mAction)) {
            String[] extraStrings = intent.getStringArrayExtra(Intent.EXTRA_EMAIL);
            if (extraStrings != null) {
                String sp = "";
                if (extraStrings.length > 1) {
                    sp = ", ";
                }
                for(String t : extraStrings) {
                    mToView.setText(mToView.getText() + t + sp);
                }
            }
            /*extraStrings = intent.getStringArrayExtra(Intent.EXTRA_CC);
            if (extraStrings != null) {
                addAddresses(mCcView, extraStrings);
            }
            extraStrings = intent.getStringArrayExtra(Intent.EXTRA_BCC);
            if (extraStrings != null) {
                addAddresses(mBccView, extraStrings);
            }*/
            String extraString = intent.getStringExtra(Intent.EXTRA_SUBJECT);
            if (extraString != null) {
                mSubjectView.setText(extraString);
            }

            CharSequence text = intent.getCharSequenceExtra(Intent.EXTRA_TEXT);
            if (text != null) {
                mMessageContentView.setText(text);
            }

            String references = intent.getStringExtra(Intent.EXTRA_REFERRER);
            if (references != null) {
                m = new MailWrited(references);
            }
        }

        new Query(this) {
            @Override
            public void result(String data) {
                Gson gson = new Gson();
                UserConfig userConfig = gson.fromJson(data, UserConfig.class);
                ArrayList<String> spinnerArray = new ArrayList<String>();
                for (DomainAddress domainAddress : userConfig.mails) {
                    spinnerArray.add(domainAddress.name + "@" + domainAddress.domain);
                }
                Spinner spinner = (Spinner) findViewById(R.id.spinner);
                ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(WriteMailActivity.this, android.R.layout.simple_spinner_dropdown_item, spinnerArray);
                spinner.setAdapter(spinnerArrayAdapter);

                TextView name = (TextView) findViewById(R.id.name);
                name.setText(userConfig.defaultName);
            }
        }.get("/userConfig");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_write_mail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_send_mail:

                final ProgressDialog progressDialog = new ProgressDialog(WriteMailActivity.this);
                progressDialog.setIndeterminate(true);
                progressDialog.setMessage("Sending...");
                progressDialog.show();

                if (m == null) {
                    m = new MailWrited();
                }

                TextView name = (TextView) findViewById(R.id.name);
                Spinner spinner = (Spinner) findViewById(R.id.spinner);
                TextView to = (TextView) findViewById(R.id.to);
                TextView text = (TextView) findViewById(R.id.text);
                TextView subject = (TextView) findViewById(R.id.subject);
                m.from.name = name.getText().toString();
                m.from.address = spinner.getSelectedItem().toString();
                String addresses = to.getText().toString();
                String[] list = addresses.split(",");
                for (int i = 0; i < list.length; i++) {
                    m.to.add(new Address(list[i]));
                }
                m.markdown = text.getText().toString();
                m.subject = subject.getText().toString();

                Gson gson = new Gson();

                new Query(this) {
                    @Override
                    public void result(String data) {
                        Log.d("SendMail", data);
                        Gson gson = new Gson();
                        MailWritedReturn ret = gson.fromJson(data, MailWritedReturn.class);
                        if (ret.info == null) {
                            progressDialog.cancel();
                            new AlertDialog.Builder(WriteMailActivity.this)
                                    .setTitle("Error")
                                    .setMessage(data)
                                    .show();
                        } else {
                            progressDialog.cancel();
                            finish();
                        }
                    }
                }.post("/msg", gson.toJson(m));
                break;
            case android.R.id.home:
                finish();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }
}
