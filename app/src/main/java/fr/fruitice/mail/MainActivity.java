package fr.fruitice.mail;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.RemoteInput;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.NotificationCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SubMenu;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.NoConnectionError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import fr.fruitice.mail.Objects.Folder;
import fr.fruitice.mail.Objects.Mail;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private List<Mail> mailList;
    private RecyclerView recyclerView;
    private MailAdapter mAdapter;
    private SwipeRefreshLayout mRefresh;
    LinearLayoutManager mLayoutManager;
    boolean loading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences sharedPref = getSharedPreferences("fruitmail", MODE_PRIVATE);
        String token = sharedPref.getString("token", null);

        if (token == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, WriteMailActivity.class);
                startActivity(intent);
                //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                //        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        final NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setItemIconTintList(null);

        loading = false;

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);

        mRefresh = (SwipeRefreshLayout) findViewById(R.id.content_main);
        mRefresh.setRefreshing(true);
        setMessages();
        mRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getNewMessages();
            }
        });

        mRefresh.setColorSchemeResources(
                R.color.colorPrimary
        );



        new Query(this) {
            @Override
            public void result(String data) {

                Gson gson = new Gson();
                Type listType = new TypeToken<List<Folder>>(){}.getType();
                List<Folder> folders = gson.fromJson(data, listType);

                final Menu menu = navigationView.getMenu();

                int i = 0;

                //TODO: add all & unread

                for (Folder folder: folders) {
                    MenuItem mi = menu.add(folder.name);
                    mi.setCheckable(true);

                    if (i == 0) {
                        mi.setChecked(true);
                    }

                    if (folder.stick != null) {
                        switch (folder.stick) {
                            case "peterriver":
                                mi.setIcon(R.drawable.stick_peterriver);
                                break;
                            case "orange":
                                mi.setIcon(R.drawable.stick_orange);
                                break;
                            case "sunflower":
                                mi.setIcon(R.drawable.stick_sunflower);
                                break;
                            case "emerald":
                                mi.setIcon(R.drawable.stick_emerald);
                                break;
                            case "concret":
                                mi.setIcon(R.drawable.stick_concret);
                                break;
                            case "alizarin":
                                mi.setIcon(R.drawable.stick_alizarin);
                                break;
                            case "wetasphalt":
                                mi.setIcon(R.drawable.stick_wetasphalt);
                                break;
                            case "amethyst":
                                mi.setIcon(R.drawable.stick_amethyst);
                                break;
                            case "turquoise":
                                mi.setIcon(R.drawable.stick_turquoise);
                                break;
                        }
                    }
                    i++;
                }
            }
        }.get("/folders");

    }

    public void getNewMessages() {
        long from = 0;
        if (mailList.size() != 0) {
            from = mailList.get(0).date;
        }
        new Query(this) {
            @Override
            public void result(String data) {
                int size = mailList.size();
                Log.d("result", data);
                Gson gson = new Gson();
                Type listType = new TypeToken<List<Mail>>(){}.getType();
                List<Mail> newL = gson.fromJson(data, listType);
                mailList.addAll(0, newL);
                mAdapter.notifyItemRangeInserted(0, newL.size());
                mRefresh.setRefreshing(false);
                recyclerView.scrollToPosition(0);
            }

            @Override
            public void error(VolleyError error) {
                super.error(error);
            }
        }.get("/msgs?nb=10&direction=future&from=" + from);
    }

    public void getMessages() {
        mRefresh.setRefreshing(true);
        long from = 0;
        if (mailList.size() != 0) {
            from = mailList.get(mailList.size() - 1).date;
        }
        new Query(this) {
            @Override
            public void result(String data) {
                int size = mailList.size();
                Log.d("result", "done");
                Gson gson = new Gson();
                Type listType = new TypeToken<List<Mail>>(){}.getType();
                List<Mail> newL = gson.fromJson(data, listType);
                mailList.addAll(newL);
                mAdapter.notifyItemRangeInserted(size, 10);
                mRefresh.setRefreshing(false);
                loading = false;
            }

            @Override
            public void error(VolleyError error) {
                super.error(error);
            }
        }.get("/msgs?nb=10&from=" + from);
    }

    public void setMessages() {

        mailList = new ArrayList<Mail>();
        mAdapter = new MailAdapter(mailList);

        mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);

        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT /*| ItemTouchHelper.RIGHT*/) {

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                int p = viewHolder.getLayoutPosition();
                TextView sub = (TextView) viewHolder.itemView.findViewById(R.id.subject);
                Toast.makeText(MainActivity.this, sub.getText() + " " + direction, Toast.LENGTH_SHORT).show();
                if (direction == ItemTouchHelper.LEFT) {
                    mailList.remove(p);
                    mAdapter.notifyItemRemoved(p);
                }
            }

        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);

        recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(MainActivity.this, recyclerView, new RecyclerItemClickListener.OnItemClickListener() {
            @Override public void onItemClick(View view, int position) {
                // do whatever
                //Toast.makeText(MainActivity.this, mailList.get(position).subject, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MainActivity.this, MailActivity.class);
                intent.putExtra("mail", mailList.get(position));
                startActivity(intent);
            }

            @Override public void onLongItemClick(View view, int position) {
                // do whatever
            }
        }));

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener()
        {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy)
            {
                if(dy > 0) //check for scroll down
                {
                    int visibleItemCount = mLayoutManager.getChildCount();
                    int totalItemCount = mLayoutManager.getItemCount();
                    int pastVisiblesItems = mLayoutManager.findFirstVisibleItemPosition();

                    if (!loading)
                    {
                        if ( (visibleItemCount + pastVisiblesItems) >= totalItemCount)
                        {
                            loading = true;
                            Log.v("...", "Last Item Wow !");
                            getMessages();
                        }
                    }
                }
            }
        });

        getMessages();

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        item.getTitle();

        Snackbar.make(findViewById(R.id.recyclerView), "Switch to: " + item.getTitle(), Snackbar.LENGTH_SHORT)
                .setAction("Action", null).show();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
