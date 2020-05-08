package org.prezdev.notihistory;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.widget.SearchView;
import android.view.View;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.MenuItem;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.widget.TextView;
import android.widget.Toast;

import org.prezdev.notihistory.cache.Cache;
import org.prezdev.notihistory.configuration.Config;
import org.prezdev.notihistory.configuration.preferences.SettingsActivity;
import org.prezdev.notihistory.fragments.dialog.NotificationConfigDialog;
import org.prezdev.notihistory.fragments.AppsFragment;
import org.prezdev.notihistory.fragments.InstalledAppsFragment;
import org.prezdev.notihistory.fragments.NotificationsFragment;
import org.prezdev.notihistory.listeners.bottomNavigation.OnNavigationBottomClickListener;
import org.prezdev.notihistory.listeners.search.OnFocusChangeSearchListener;
import org.prezdev.notihistory.listeners.search.OnSearchListener;
import org.prezdev.notihistory.permission.Permisions;
import org.prezdev.notihistory.permission.RequestCode;
import org.prezdev.notihistory.service.FragmentService;
import org.prezdev.notihistory.service.NotificationService;
import org.prezdev.notihistory.service.impl.FragmentServiceImpl;
import org.prezdev.notihistory.service.impl.NotificationServiceImpl;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private FragmentService fragmentService;
    private static MainActivity mainActivity;
    private SearchView searchView;
    //private FloatingActionButton floatingActionButton;
    private BottomNavigationView bottomNavigationView;
    private NotificationService notificationService;

    @Override
    public void onResume(){
        super.onResume();

        loadFileNotifications();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            if (!NotificationManagerCompat.getEnabledListenerPackages(this).contains(getPackageName())) {
                if(!Config.notificationConfigDialogIsVisible){
                    NotificationConfigDialog notificationConfigDialog = new NotificationConfigDialog();

                    notificationConfigDialog.show(this.getSupportFragmentManager(), "tag");
                }
            }
        }

        if(Cache.showSystemAppsSettingsChange){
            Cache.showSystemAppsSettingsChange = false;

            Cache.updateInstalledAppsCache();

            if(fragmentService.getVisibleFragment() instanceof InstalledAppsFragment){
                fragmentService.load(new InstalledAppsFragment());
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainActivity = this;

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //floatingActionButton = findViewById(R.id.search_float_icon);
        bottomNavigationView = findViewById(R.id.bottomNavigationMain);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);

        NavigationView navigationView = findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);
        TextView lblVersion = headerView.findViewById(R.id.lblAppVersion);

        lblVersion.setText("v"+BuildConfig.VERSION_NAME);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);


        Permisions.checkAppPermissions(this);

        loadFileNotifications();

        fragmentService = new FragmentServiceImpl(this);

        fragmentService.load(Config.homeScreenFragment);

        Cache.updateInstalledAppsCache();


        /*
        Intent intent=new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
        startActivity(intent);*/
    }

    private void loadFileNotifications() {
        if(notificationService == null){
            notificationService = new NotificationServiceImpl(MainActivity.getActivity());
        }

        notificationService.loadFileNotifications();
    }

    @Override
    public void onRequestPermissionsResult(
        int requestCode,
        String[] permissions,
        int[] grantResults
    ) {
        switch (requestCode) {
            case RequestCode.READ_AND_WRITE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    Toast.makeText(getApplicationContext(), "Permisos concedidos", Toast.LENGTH_LONG).show();

                    fragmentService.load(Config.homeScreenFragment);
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
        }
    }

    @Override
    public void onBackPressed() {
        Fragment visibleFragment = fragmentService.getVisibleFragment();

        if(visibleFragment != null && visibleFragment instanceof NotificationsFragment){
            AppsFragment appsFragment = new AppsFragment();

            fragmentService.load(appsFragment);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(MainActivity.this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {

        int id = item.getItemId();
        Fragment fragment = null;

        if (id == R.id.nav_app_notifications) {
            fragment = new AppsFragment();
        }  else if(id == R.id.nav_installed_apps){
            fragment = new InstalledAppsFragment();
        } else if(id == R.id.nav_settings){
            startActivity(new Intent(MainActivity.this, SettingsActivity.class));
        }

        if(fragment != null){
            fragmentService.load(fragment);
        }


        return true;
    }

    /*SEARCH BAR*/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);

        searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setOnQueryTextListener(new OnSearchListener(this));
        searchView.setOnQueryTextFocusChangeListener(new OnFocusChangeSearchListener());

        //floatingActionButton.setOnClickListener(new OnFloatIconListener(searchView));

        bottomNavigationView.setOnNavigationItemSelectedListener(
            new OnNavigationBottomClickListener(searchView)
        );

        return true;
    }
    /*SEARCH BAR*/

    public static MainActivity getActivity(){
        return mainActivity;
    }
}
