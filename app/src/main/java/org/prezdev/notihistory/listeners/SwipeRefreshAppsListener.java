package org.prezdev.notihistory.listeners;

import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.widget.ListView;

import org.prezdev.notihistory.R;
import org.prezdev.notihistory.adapter.AppAdapter;
import org.prezdev.notihistory.model.App;
import org.prezdev.notihistory.service.impl.NotificationServiceImpl;

import java.util.List;

public class SwipeRefreshAppsListener implements SwipeRefreshLayout.OnRefreshListener, Runnable {

    private View view;
    private NotificationServiceImpl notificationService;
    private SwipeRefreshLayout appsSwipeRefresh;
    private ListView lvApps;

    public SwipeRefreshAppsListener(View view) {
        this.view = view;
        this.appsSwipeRefresh = view.findViewById(R.id.appsSwipeRefresh);
        this.lvApps = view.findViewById(R.id.lvApps);
    }

    @Override
    public void onRefresh() {
        new Handler().postDelayed(this, 500);
    }

    @Override
    public void run() {
        notificationService = new NotificationServiceImpl(view.getContext());

        // Obteniendo las apps de la base de datos de notificaciones
        List<App> apps = notificationService.getApps();

        // Creando el adapter
        AppAdapter appAdapter = new AppAdapter(view.getContext(), apps);

        // Colocando las apps en el listView
        lvApps.setAdapter(appAdapter);

        // Parando la animación de load
        appsSwipeRefresh.setRefreshing(false);
    }
}
