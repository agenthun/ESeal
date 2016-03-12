package com.agenthun.eseal.activity;

import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.agenthun.eseal.App;
import com.agenthun.eseal.R;
import com.agenthun.eseal.adapter.SectionsPagerAdapter;
import com.agenthun.eseal.bean.AllDynamicDataByContainerId;
import com.agenthun.eseal.bean.base.Detail;
import com.agenthun.eseal.connectivity.manager.RetrofitManager;
import com.agenthun.eseal.connectivity.service.PathType;
import com.agenthun.eseal.view.BottomSheetDialogView;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "MainActivity";

    private SectionsPagerAdapter mSectionsPagerAdapter;

    private ViewPager mViewPager;
    private FloatingActionButton fab;

    private String token = null;
    private String mContainerNo = null;
    private String mContainerId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        token = getIntent().getStringExtra(RetrofitManager.TOKEN);

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        final TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String token = App.getToken();
                if (mContainerNo != null && mContainerId != null) {
                    showFreightDataListByBottomSheet(token, mContainerId, mContainerNo);
                } else {
                    Snackbar.make(view, getString(R.string.text_hint_freight_query), Snackbar.LENGTH_SHORT)
                            .setAction("Action", null).show();
                }
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        final ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        CoordinatorLayout coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);

        mSectionsPagerAdapter.setOnDataChangeListener(new SectionsPagerAdapter.OnDataChangeListener() {
            @Override
            public void onContainerDataChange(String containerNo, String containerId) {
                mContainerNo = containerNo;
                mContainerId = containerId;
            }
        });

/*        Log.d(TAG, "onPageChange() returned: " + position);
        if (position == 0) {
            fab.setVisibility(View.VISIBLE);
            ViewCompat.animate(fab).scaleX(1).scaleY(1)
                    .setInterpolator(new LinearOutSlowInInterpolator())
                    .setListener(new ViewPropertyAnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(View view) {
                            if (isFinishing() || (ApiLevelHelper.isAtLeast(Build.VERSION_CODES.JELLY_BEAN_MR1) && isDestroyed())) {
                                return;
                            }
                            fab.setVisibility(View.VISIBLE);
                        }
                    })
                    .start();
        } else {
            if (fab.isShown()) {
                ViewCompat.animate(fab).scaleX(0).scaleY(0)
                        .setInterpolator(new FastOutSlowInInterpolator())
                        .setStartDelay(100)
                        .setListener(new ViewPropertyAnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(View view) {
                                if (isFinishing() || (ApiLevelHelper.isAtLeast(Build.VERSION_CODES.JELLY_BEAN_MR1) && isDestroyed())) {
                                    return;
                                }
                                fab.setVisibility(View.GONE);
                            }
                        })
                        .start();
            }
        }*/
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void showFreightDataListByBottomSheet(String token, String containerId, final String containerNo) {
        if (token != null) {
            RetrofitManager.builder(PathType.BASE_WEB_SERVICE)
                    .getFreightDataListObservable(token, containerId, 1)
                    .enqueue(new Callback<AllDynamicDataByContainerId>() {
                        @Override
                        public void onResponse(Call<AllDynamicDataByContainerId> call, Response<AllDynamicDataByContainerId> response) {
                            List<Detail> details = response.body().getDetails();
                            BottomSheetDialogView.show(MainActivity.this, containerNo, details);
                        }

                        @Override
                        public void onFailure(Call<AllDynamicDataByContainerId> call, Throwable t) {
                            Log.d(TAG, "Response onFailure: " + t.getLocalizedMessage());
                        }
                    });
        }
    }
}
