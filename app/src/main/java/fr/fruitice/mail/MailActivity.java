package fr.fruitice.mail;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;

import com.google.gson.Gson;

import org.w3c.dom.Text;

import fr.fruitice.mail.Objects.Address;
import fr.fruitice.mail.Objects.Attachment;
import fr.fruitice.mail.Objects.Mail;

public class MailActivity extends AppCompatActivity {

    Mail mail;
    String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mail);

        SharedPreferences sharedPref = getSharedPreferences("fruitmail", MODE_PRIVATE);
        token = sharedPref.getString("token", null);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mail = (Mail) getIntent().getSerializableExtra("mail");
        if (mail == null) {
            String id = getIntent().getStringExtra("id");
            new Query(this) {
                @Override
                public void result(String data) {
                    Log.d("mail", data);
                    Gson gson = new Gson();
                    mail = gson.fromJson(data, Mail.class);
                    setMail();
                }
            }.get("/msg/" + id);
            //get mail
        } else {
            setMail();
        }
        //Log.d("mail", mail.subject);

        setTitle("");



        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(MailActivity.this, WriteMailActivity.class);
                intent.setAction(Intent.ACTION_SENDTO);
                String[] list = new String[mail.from.size()];
                int i = 0;
                for (Address add : mail.from) {
                    list[i] = add.address;
                    i++;
                }
                intent.putExtra(Intent.EXTRA_EMAIL, list);
                intent.putExtra(Intent.EXTRA_SUBJECT, mail.subject);
                intent.putExtra(Intent.EXTRA_TEXT, "\r\n\r\n---\r\n" + mail.text);
                intent.putExtra(Intent.EXTRA_REFERRER, mail.messageId);

                startActivity(intent);
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_read_mail, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_web:
                TextView text = (TextView) findViewById(R.id.text);
                TouchyWebView web = (TouchyWebView) findViewById(R.id.web);
                text.setVisibility(View.INVISIBLE);
                web.setVisibility(View.VISIBLE);
                web.loadData(mail.html, "text/html; charset=utf-8", "utf-8");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void setMail() {
        TextView subject = (TextView) findViewById(R.id.subject);
        TextView from = (TextView) findViewById(R.id.from);
        TextView to = (TextView) findViewById(R.id.to);
        TextView text = (TextView) findViewById(R.id.text);
        TouchyWebView web = (TouchyWebView) findViewById(R.id.web);
        Button button = (Button) findViewById(R.id.button);
        subject.setText(mail.subject);
        from.setText(mail.from.get(0).name);
        if (mail.to == null && mail.envelopeTo == null) {
            to.setText("undefined");
        } else if (mail.to != null) {
            to.setText(mail.to.get(0).name + " <" + mail.to.get(0).address + ">");
        } else if (mail.envelopeTo != null) {
            to.setText(mail.envelopeTo.get(0).name + " <" + mail.envelopeTo.get(0).address + ">");
        }
        text.setText(mail.text);
        web.setVisibility(View.INVISIBLE);
        //web.getSettings().setLoadWithOverviewMode(true);
        //web.getSettings().setUseWideViewPort(true);
        web.getSettings().setSupportZoom(true);
        //web.getSettings().setBuiltInZoomControls(true);
        if (mail.attachments == null || mail.attachments.size() == 0) {
            button.setVisibility(View.INVISIBLE);
        } else if (mail.attachments.size() != 0) {
            button.setText(mail.attachments.size() + " pieces");
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MailActivity.this);
                    builder.setTitle("Attachments");
                    String[] list = new String[mail.attachments.size()];
                    int i  = 0;
                    for (Attachment attachment : mail.attachments) {
                        list[i] = attachment.fileName;
                        i++;
                    }
                    builder.setItems(list, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(Intent.ACTION_VIEW,
                                    Uri.parse("https://mail-2.fruitice.fr/attachment/" + mail.id + "/" +
                                            mail.attachments.get(which).contentId + "?token=" + token));
                            startActivity(intent);
                        }
                    });
                    builder.show();
                }
            });
        }
    }
}
