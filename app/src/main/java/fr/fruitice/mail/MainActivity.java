package fr.fruitice.mail;

import android.app.Activity;
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
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.Editable;
import android.text.TextWatcher;
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
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.NoConnectionError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.Volley;
import com.google.firebase.FirebaseApp;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import fr.fruitice.mail.Objects.Folder;
import fr.fruitice.mail.Objects.Folders;
import fr.fruitice.mail.Objects.Mail;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private List<Mail> mailList;
    private RecyclerView recyclerView;
    private MailAdapter mAdapter;
    private SwipeRefreshLayout mRefresh;
    LinearLayoutManager mLayoutManager;
    private EditText search;
    boolean loading;
    private Map<MenuItem, String> map = new HashMap<MenuItem, String>();

    private String category = "new";
    private String folder = "inbox";

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


        final NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setItemIconTintList(null);

        View head = navigationView.getHeaderView(0);
        search = (EditText) head.findViewById(R.id.searchText);

        final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        drawer.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {

            }

            @Override
            public void onDrawerOpened(@NonNull View drawerView) {

            }

            @Override
            public void onDrawerClosed(@NonNull View drawerView) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                //Find the currently focused view, so we can grab the correct window token from it.
                View view = getCurrentFocus();
                if (view == null) {
                    view = (View) findViewById(android.R.id.content).getRootView();
                }

                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

                Log.d("search", "clear focus");
                search.clearFocus();
            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        });

        toggle.syncState();

        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                Log.d("search", "" + charSequence);
                final Menu menu = navigationView.getMenu();
                if (menu == null || menu.size() < 3) {
                    return;
                }
                for (int k = 0; k < menu.size(); k++) {
                    final Menu subMenu = menu.getItem(k).getSubMenu();
                    for (int j = 0; j < subMenu.size(); j++) {
                        if (!subMenu.getItem(j).getTitle().toString().contains(charSequence + "")) {
                            subMenu.getItem(j).setVisible(false);
                        } else {
                            subMenu.getItem(j).setVisible(true);
                        }
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        search.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (drawer.isDrawerOpen(GravityCompat.START) && !hasFocus) {
                    Log.d("search", "request focus");
                    v.requestFocus();
                }
            }
        });

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
                Folders folders = gson.fromJson(data, Folders.class);

                final Menu menu = navigationView.getMenu();

                SubMenu freshMenu = menu.addSubMenu("Fresh");
                int i = 0;
                for (Folder folder: folders.fresh) {
                    MenuItem mi = freshMenu.add(folder.name);
                    setIcon(folder, mi, "new", i == 0);
                    i++;
                }
                Log.d("0 s", "" + i);
                SubMenu readMenu = menu.addSubMenu("Read");
                for (Folder folder: folders.read) {
                    MenuItem mi = readMenu.add(folder.name);
                    setIcon(folder, mi, "read", i == 0);
                }
                SubMenu doneMenu = menu.addSubMenu("Completed");
                for (Folder folder: folders.done) {
                    MenuItem mi = doneMenu.add(folder.name);
                    setIcon(folder, mi, "done", i == 0);
                }
            }
        }.get("/v2/folders");

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

    public void setIcon(Folder folder, MenuItem mi, String cat, boolean checked) {
        mi.setCheckable(true);
        mi.setChecked(checked);
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
        map.put(mi, cat);
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
        }.get("/msgs/" + category + "/" + folder + "?nb=10&from=" + from);
    }

    public void getMessages(String category, String folder) {
        if (!Objects.equals(category, this.category) || !Objects.equals(folder, this.folder)) {
            mailList.clear();
            mAdapter.notifyDataSetChanged();
            this.category = category;
            this.folder = folder;
            getMessages();
        }
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
                view.setBackgroundColor(Color.rgb(220, 220, 220));
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

                    if (!loading) {
                        if ((visibleItemCount + pastVisiblesItems) >= totalItemCount) {
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
        String cat = map.get(item);
        final NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        uncheckAllMenuItems(navigationView);

        getMessages(cat, item.getTitle() + "");

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void uncheckAllMenuItems(NavigationView navigationView) {
        final Menu menu = navigationView.getMenu();
        for (int i = 0; i < menu.size(); i++) {
            MenuItem item = menu.getItem(i);
            if (item.hasSubMenu()) {
                SubMenu subMenu = item.getSubMenu();
                for (int j = 0; j < subMenu.size(); j++) {
                    MenuItem subMenuItem = subMenu.getItem(j);
                    subMenuItem.setChecked(false);
                }
            } else {
                item.setChecked(false);
            }
        }
    }
}
