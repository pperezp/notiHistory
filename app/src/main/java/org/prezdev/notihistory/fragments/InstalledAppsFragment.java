package org.prezdev.notihistory.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.transition.Fade;
import android.transition.Slide;
import android.util.TimingLogger;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import org.prezdev.notihistory.MainActivity;
import org.prezdev.notihistory.R;
import org.prezdev.notihistory.adapter.InstalledAppAdapter;
import org.prezdev.notihistory.configuration.Config;
import org.prezdev.notihistory.configuration.Preferences;
import org.prezdev.notihistory.listeners.OnInstalledAppClickListener;
import org.prezdev.notihistory.listeners.OnInstalledAppLongClickListener;
import org.prezdev.notihistory.listeners.OnInstalledAppStateChangeListener;
import org.prezdev.notihistory.listeners.swiperefresh.SwipeRefreshAppsListener;
import org.prezdev.notihistory.listeners.swiperefresh.SwipeRefreshInstalledAppsListener;
import org.prezdev.notihistory.model.InstalledApp;
import org.prezdev.notihistory.permission.Permisions;
import org.prezdev.notihistory.service.AppService;
import org.prezdev.notihistory.service.impl.AppServiceImpl;

import java.util.ArrayList;
import java.util.List;


public class InstalledAppsFragment extends Fragment implements OnInstalledAppStateChangeListener {
    private ListView lvInstalledApps;
    private AppService appService;
    private Preferences preferences;
    private SwipeRefreshLayout appsSwipeRefresh;
    private List<InstalledApp> installedApps;


    public InstalledAppsFragment(){
        preferences = new Preferences();

        if(preferences.isFragmentTransition()){
            this.setExitTransition(new Fade());
            this.setEnterTransition(new Slide(Gravity.RIGHT).setDuration(300));
        }

        appService = new AppServiceImpl(MainActivity.getActivity());

        try{
            installedApps = appService.getInstalledApps();
        }catch (Exception ex){
            installedApps = new ArrayList();

            Permisions.checkAppPermissions(getActivity());
        }
    }

    @Override
    public View onCreateView(
        LayoutInflater inflater,
        ViewGroup container,
        Bundle savedInstanceState
    ) {
        View view = inflater.inflate(R.layout.fragment_installed_apps, container, false);
        Context context = view.getContext();

        lvInstalledApps = view.findViewById(R.id.lvInstalledApps);
        lvInstalledApps.setOnItemClickListener(new OnInstalledAppClickListener(this));
        lvInstalledApps.setOnItemLongClickListener(new OnInstalledAppLongClickListener());

        InstalledAppAdapter installedAppAdapter = new InstalledAppAdapter(context, installedApps, this);

        lvInstalledApps.setAdapter(installedAppAdapter);

        /*------------------------- Swipe Refresh -------------------------*/
        appsSwipeRefresh = view.findViewById(R.id.installedAppsSwipeRefresh);

        appsSwipeRefresh.setColorSchemeResources(
            R.color.orange,
            R.color.green,
            R.color.blue
        );

        appsSwipeRefresh.setOnRefreshListener(new SwipeRefreshInstalledAppsListener(view));
        /*------------------------- Swipe Refresh -------------------------*/

        return view;
    }


    @Override
    public void stateChange(InstalledApp installedApp) {
        if(installedApp.isSelected()){
            appService.save(installedApp);
        }else{
            appService.delete(installedApp.getPackageName());
        }
    }
}
