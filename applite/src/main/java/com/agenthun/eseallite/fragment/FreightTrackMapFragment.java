package com.agenthun.eseallite.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.agenthun.eseallite.R;
import com.agenthun.eseallite.activity.LoginActivity;
import com.agenthun.eseallite.activity.TimePickerActivity;
import com.agenthun.eseallite.bean.base.DeviceLocation;
import com.agenthun.eseallite.bean.base.LocationDetail;
import com.agenthun.eseallite.connectivity.manager.RetrofitManager2;
import com.agenthun.eseallite.connectivity.service.PathType;
import com.agenthun.eseallite.utils.PreferencesHelper;
import com.agenthun.eseallite.view.BottomSheetDialogView;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.Polyline;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.DistanceUtil;
import com.baidu.mapapi.utils.SpatialRelationUtil;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * @project ESeal
 * @authors agenthun
 * @date 2017/2/15 10:53.
 */

public class FreightTrackMapFragment extends Fragment {
    private static final String TAG = "FreightTrackMapFragment";

    private static final String ARGUMENT_FREIGHT_ID = "ARGUMENT_FREIGHT_ID";
    private static final String ARGUMENT_FREIGHT_NAME = "ARGUMENT_FREIGHT_NAME";

    // 通过设置间隔时间和距离可以控制速度和图标移动的距离
    private static final int TIME_INTERVAL = 80;
    private static final double DISTANCE_RATIO = 10000000.0D;
    private static final double MOVE_DISTANCE_MIN = 0.0001;
    private static final int LOCATION_RADIUS = 50;

    private static final double[] BAIDU_MAP_ZOOM = {
            50, 100, 200, 500, 1000, 2000, 5000, 10000, 20000, 25000,
            50000, 100000, 200000, 500000, 1000000, 2000000
    };

    private BaiduMap mBaiduMap;
    private Polyline mVirtureRoad;
    private Marker mMoveMarker;
    private Handler mHandler;

    private double moveDistance = 0.0001;
    private Thread movingThread;

    @Bind(R.id.bmapView)
    MapView bmapView;

    private String mFreightId = null;
    private String mFreightName = null;
    private boolean mIsFreightTrackMode = false;
    private LocationDetail mLocationDetail = null;
    private List<LocationDetail> mLocationDetailList = new ArrayList<>();

    public static FreightTrackMapFragment newInstance(String id, String name) {
        Bundle arguments = new Bundle();
        arguments.putString(ARGUMENT_FREIGHT_ID, id);
        arguments.putString(ARGUMENT_FREIGHT_NAME, name);
        FreightTrackMapFragment fragment = new FreightTrackMapFragment();
        fragment.setArguments(arguments);
        return fragment;
    }

    public FreightTrackMapFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_freight_track_map, container, false);
        ButterKnife.bind(this, view);

        mFreightId = getArguments().getString(ARGUMENT_FREIGHT_ID);
        mFreightName = getArguments().getString(ARGUMENT_FREIGHT_NAME);

        FloatingActionButton fab = (FloatingActionButton) getActivity().findViewById(R.id.fab);
        fab.setOnClickListener(mOnFabClickListener);

        mHandler = new Handler();
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        setupBaiduMap();
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        bmapView.onResume();

        loadFreightLocation(false, PreferencesHelper.getTOKEN(getActivity()), mFreightId, null, null);
    }

    @Override
    public void onPause() {
        super.onPause();
        bmapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        bmapView.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_track_search:
                mIsFreightTrackMode = true;
                startTimePickerActivity();
                return true;
            case R.id.action_location_search:
                mIsFreightTrackMode = false;
                loadFreightLocation(false, PreferencesHelper.getTOKEN(getActivity()), mFreightId, null, null);
                return true;
            case R.id.action_sign_out:
                signOut(true);
                return true;
            default:
                mIsFreightTrackMode = false;
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == TimePickerActivity.RESULT_PICK_TIME) {
            String from = data.getStringExtra(TimePickerActivity.PICK_TIME_FROM);
            String to = data.getStringExtra(TimePickerActivity.PICK_TIME_TO);
            Log.d(TAG, "from: " + from + ", to: " + to);

            loadFreightLocation(true, PreferencesHelper.getTOKEN(getActivity()),
                    mFreightId, from, to);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private View.OnClickListener mOnFabClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mIsFreightTrackMode) {
                if (mLocationDetailList != null && !mLocationDetailList.isEmpty()) {
                    showFreightDataListByBottomSheet(PreferencesHelper.getTOKEN(getActivity()),
                            mFreightId, mFreightName, mLocationDetailList);
                }
            } else {
                if (mLocationDetail != null) {
                    showFreightDataListByBottomSheet(PreferencesHelper.getTOKEN(getActivity()),
                            mFreightId, mFreightName, Arrays.asList(mLocationDetail));
                }
            }
        }
    };

    /**
     * 设置百度地图属性
     */
    private void setupBaiduMap() {
        bmapView.showZoomControls(false); //移除地图缩放控件
        bmapView.removeViewAt(1); //移除百度地图Logo

        mBaiduMap = bmapView.getMap();
        mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
        mBaiduMap.setMapStatus(MapStatusUpdateFactory.zoomTo(16));
        mBaiduMap.getUiSettings().setOverlookingGesturesEnabled(false); //取消俯视手势
    }

    /**
     * 清除百度地图覆盖物
     */
    private void clearLocationData() {
        mBaiduMap.clear();
        if (mVirtureRoad != null && mVirtureRoad.getPoints().size() > 0) {
            mVirtureRoad.remove();
            mVirtureRoad.getPoints().clear();
        }
        if (mMoveMarker != null) {
            mMoveMarker.remove();
        }
//        if (movingThread != null && movingThread.isAlive()) {
//            Thread.currentThread().interrupt();
//            mVirtureRoad = null;
//            mMoveMarker = null;
//            Log.d(TAG, "clearLocationData() returned: movingThread end");
//        }
        bmapView.getOverlay().clear();
        try {
            Thread.sleep(1000);
            if (movingThread != null) {
                movingThread.interrupt();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private List<LocationDetail> locationInfosToLocationDetailList(List<DeviceLocation> details) {
        List<LocationDetail> result = new ArrayList<LocationDetail>();
        if (details == null || details.size() == 0) return result;

        //GPS坐标转百度地图坐标
//        CoordinateConverter converter = new CoordinateConverter();
//        converter.from(CoordinateConverter.CoordType.GPS);
        for (DeviceLocation detail :
                details) {
            if (detail.isInvalid()) continue;

            String time = detail.getReportTime();
            String uploadType = detail.getUploadType();
            String securityLevel = detail.getSecurityLevel();
            String closedFlag = detail.getClosedFlag();
            if (uploadType.equals("0")) {
                time = utc2Local(time, "yyyy/MM/dd HH:mm:ss", "yyyy/MM/dd HH:mm:ss");
            }

            String[] location = detail.getBaiduCoordinate().split(",");
            LatLng lng = new LatLng(
                    Double.parseDouble(location[0]),
                    Double.parseDouble(location[1])
            );
//            converter.coord(lng);
//            lng = converter.convert();
            result.add(new LocationDetail(time, uploadType, securityLevel, closedFlag, lng));
        }

        return result;
    }

    /**
     * 加载轨迹数据至百度地图
     */
    private void showBaiduMap(List<LocationDetail> locationDetails) {
        if (locationDetails == null || locationDetails.size() == 0) return;

        int countInCircle = 0;

        List<LatLng> polylines = new ArrayList<>();
        for (LocationDetail locationDetail :
                locationDetails) {
            if (locationDetail.isInvalid()) continue;

            LatLng lng = locationDetail.getLatLng();
            polylines.add(lng);

            if (polylines.size() > 1) {
                if (SpatialRelationUtil.isCircleContainsPoint(polylines.get(0), LOCATION_RADIUS, lng)) {
                    countInCircle++;
                }
            }
        }

        Collections.reverse(polylines); //按时间正序

        OverlayOptions markerOptions = null;

        if (polylines.size() > 1) {
            OverlayOptions polylineOptions = new PolylineOptions()
                    .points(polylines)
                    .width(8)
                    .color(ContextCompat.getColor(getActivity(), R.color.red_500));
            mVirtureRoad = (Polyline) mBaiduMap.addOverlay(polylineOptions);
            markerOptions = new MarkerOptions().flat(true).anchor(0.5f, 0.5f).icon(BitmapDescriptorFactory
                    .fromResource(R.drawable.ic_car)).position(polylines.get(0)).rotate((float) getAngle(0));
        } else {
            markerOptions = new MarkerOptions().flat(true).anchor(0.5f, 0.5f).icon(BitmapDescriptorFactory
                    .fromResource(R.drawable.ic_car)).position(polylines.get(0));
        }
        mMoveMarker = (Marker) mBaiduMap.addOverlay(markerOptions);

        //设置中心点
        setBaiduMapAdaptedZoom(polylines);
        if (polylines.size() > 1 && countInCircle < polylines.size() / 2) {
            movingThread = new Thread(new MyThread());
            movingThread.start();
        }

    }

    private void showBaiduMap(LocationDetail locationDetail) {
        if (locationDetail == null || locationDetail.isInvalid()) {
            return;
        }
        LatLng lng = locationDetail.getLatLng();

        OverlayOptions markerOptions = new MarkerOptions().flat(true).icon(BitmapDescriptorFactory
                .fromResource(R.drawable.ic_location_on_white_48dp)).position(lng);
        mMoveMarker = (Marker) mBaiduMap.addOverlay(markerOptions);

        //设置中心点
        mBaiduMap.setMapStatus(MapStatusUpdateFactory.newLatLngZoom(lng, 16));
    }

    /**
     * 自适应百度地图显示大小
     */
    private void setBaiduMapAdaptedZoom(List<LatLng> polylines) {
        if (polylines == null || polylines.size() == 0) return;

        double minLat = polylines.get(0).latitude;
        double maxLat = polylines.get(0).latitude;
        double minLng = polylines.get(0).longitude;
        double maxLng = polylines.get(0).longitude;

        LatLng point;
        for (int i = 1; i < polylines.size(); i++) {
            point = polylines.get(i);
            if (point.latitude < minLat) minLat = point.latitude;
            if (point.latitude > maxLat) maxLat = point.latitude;
            if (point.longitude < minLng) minLng = point.longitude;
            if (point.longitude > maxLng) maxLng = point.longitude;
        }

        double centerLat = (maxLat + minLat) / 2;
        double centerLng = (maxLng + minLng) / 2;
        LatLng centerLatLng = new LatLng(centerLat, centerLng);

        float zoom = getZoom(minLat, maxLat, minLng, maxLng);
        mBaiduMap.setMapStatus(MapStatusUpdateFactory.newLatLngZoom(centerLatLng, zoom));
    }

    /**
     * 获取百度地图显示等级
     * 范围3-21级
     */
    private int getZoom(double minLat, double maxLat, double minLng, double maxLng) {
        LatLng minLatLng = new LatLng(minLat, minLng);
        LatLng maxLatLng = new LatLng(maxLat, maxLng);
        double distance = DistanceUtil.getDistance(minLatLng, maxLatLng);

        if (distance <= 100.0d) {
            return 16;
        }

        for (int i = 0; i < BAIDU_MAP_ZOOM.length; i++) {
            if (BAIDU_MAP_ZOOM[i] - distance > 0) {
                moveDistance = (BAIDU_MAP_ZOOM[i] - distance) / DISTANCE_RATIO;
                Log.d(TAG, "getZoom() moveDistance = " + moveDistance);
                return 19 - i + 3;
            }
        }
        return 16;
    }

    /**
     * 根据点获取图标转的角度
     */
    private double getAngle(int startIndex) {
        if ((startIndex + 1) >= mVirtureRoad.getPoints().size()) {
            throw new RuntimeException("index out of bonds");
        }
        LatLng startPoint = mVirtureRoad.getPoints().get(startIndex);
        LatLng endPoint = mVirtureRoad.getPoints().get(startIndex + 1);
        return getAngle(startPoint, endPoint);
    }

    /**
     * 根据两点算取图标转的角度
     */
    private double getAngle(LatLng fromPoint, LatLng toPoint) {
        double slope = getSlope(fromPoint, toPoint);
        if (slope == Double.MAX_VALUE) {
            if (toPoint.latitude > fromPoint.latitude) {
                return 0;
            } else {
                return 180;
            }
        }
        float deltAngle = 0;
        if ((toPoint.latitude - fromPoint.latitude) * slope < 0) {
            deltAngle = 180;
        }
        double radio = Math.atan(slope);
        double angle = 180 * (radio / Math.PI) + deltAngle - 90;
        return angle;
    }

    /**
     * 根据点和斜率算取截距
     */
    private double getInterception(double slope, LatLng point) {

        double interception = point.latitude - slope * point.longitude;
        return interception;
    }

    /**
     * 算取斜率
     */
    private double getSlope(int startIndex) {
        if ((startIndex + 1) >= mVirtureRoad.getPoints().size()) {
            throw new RuntimeException("index out of bonds");
        }
        LatLng startPoint = mVirtureRoad.getPoints().get(startIndex);
        LatLng endPoint = mVirtureRoad.getPoints().get(startIndex + 1);
        return getSlope(startPoint, endPoint);
    }

    /**
     * 算斜率
     */
    private double getSlope(LatLng fromPoint, LatLng toPoint) {
        if (toPoint.longitude == fromPoint.longitude) {
            return Double.MAX_VALUE;
        }
        double slope = ((toPoint.latitude - fromPoint.latitude) / (toPoint.longitude - fromPoint.longitude));
        return slope;
    }

    /**
     * 计算x方向每次移动的距离
     */
    private double getXMoveDistance(double slope) {
        if (slope == Double.MAX_VALUE) {
            return MOVE_DISTANCE_MIN;
        }
        return Math.abs((moveDistance * slope) / Math.sqrt(1 + slope * slope));
    }

    /**
     * UTC时间转本地时间
     */
    private String utc2Local(String utcTime, String utcTimePatten, String localTimePatten) {
        SimpleDateFormat utcFormater = new SimpleDateFormat(utcTimePatten);
        utcFormater.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date gpsUTCDate = null;
        try {
            gpsUTCDate = utcFormater.parse(utcTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        SimpleDateFormat localFormater = new SimpleDateFormat(localTimePatten);
        localFormater.setTimeZone(TimeZone.getDefault());
        String localTime = localFormater.format(gpsUTCDate.getTime());
        return localTime;
    }

    private void showLoadingFreightLocationError() {
        showMessage(getString(R.string.error_query_freight_location));
    }

    private void showMessage(String message) {
        Snackbar snackbar = Snackbar.make(getView(), message, Snackbar.LENGTH_LONG)
                .setAction("Action", null);
        ((TextView) (snackbar.getView().findViewById(R.id.snackbar_text)))
                .setTextColor(ContextCompat.getColor(getContext(), R.color.blue_grey_100));
        snackbar.show();
    }

    private void loadFreightLocation(final boolean isFreightTrackMode,
                                     @NonNull final String token, @NonNull String id,
                                     @Nullable String from, @Nullable String to) {
        if (isFreightTrackMode) {
            //获取时间段内位置列表
            RetrofitManager2.builder(PathType.WEB_SERVICE_V2_TEST).getFreightLocationListObservable(token, id, from, to)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .unsubscribeOn(Schedulers.io())
                    .subscribe(new Observer<List<LocationDetail>>() {
                        @Override
                        public void onCompleted() {

                        }

                        @Override
                        public void onError(Throwable e) {
                            showLoadingFreightLocationError();
                        }

                        @Override
                        public void onNext(List<LocationDetail> locationDetails) {
                            clearLocationData();
                            mLocationDetailList = locationDetails;
                            showBaiduMap(locationDetails);
                        }
                    });
        } else {
            //获取最新位置点
            RetrofitManager2.builder(PathType.WEB_SERVICE_V2_TEST).getFreightLocationObservable(token, id)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .unsubscribeOn(Schedulers.io())
                    .subscribe(new Observer<LocationDetail>() {
                        @Override
                        public void onCompleted() {

                        }

                        @Override
                        public void onError(Throwable e) {
                            showLoadingFreightLocationError();
                        }

                        @Override
                        public void onNext(LocationDetail locationDetail) {
                            clearLocationData();
                            mLocationDetail = locationDetail;
                            showBaiduMap(locationDetail);
                        }
                    });
        }
    }

    private void startTimePickerActivity() {
        Intent startIntent = TimePickerActivity.getStartIntent(getContext());
        startActivityForResult(startIntent, TimePickerActivity.REQUEST_PICK_TIME);
        /*ActivityCompat.startActivityForResult(getActivity(),
                startIntent,
                TimePickerActivity.REQUEST_PICK_TIME,
                ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity()).toBundle());*/
    }

    private void showFreightDataListByBottomSheet(String token, String containerId, final String containerNo, List<LocationDetail> details) {
        BottomSheetDialogView.show(getContext(), containerNo, details);
    }

    private void signOut(boolean isSave) {
        PreferencesHelper.signOut(getContext(), isSave);
        LoginActivity.start(getActivity(), isSave);
        ActivityCompat.finishAfterTransition(getActivity());
    }

    private class MyThread implements Runnable {

        @Override
        public void run() {
            Log.d(TAG, "run() returned: movingThread begin");

            while (!Thread.currentThread().isInterrupted()) {
                try {
                    if (mVirtureRoad == null || mVirtureRoad.getPoints() == null || mVirtureRoad.getPoints().isEmpty()) {
                        return;
                    }

                    for (int i = 0; i < mVirtureRoad.getPoints().size() - 1; i++) {

                        final LatLng startPoint = mVirtureRoad.getPoints().get(i);
                        final LatLng endPoint = mVirtureRoad.getPoints().get(i + 1);
                        mMoveMarker.setPosition(startPoint);

                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                // refresh marker's rotate
                                if (bmapView == null || mVirtureRoad == null || mMoveMarker == null || mVirtureRoad.getPoints().isEmpty()) {
                                    return;
                                }
                                mMoveMarker.setRotate((float) getAngle(startPoint,
                                        endPoint));
                            }
                        });
                        double slope = getSlope(startPoint, endPoint); //取斜率
                        //是不是正向的标示（向上设为正向）
                        boolean isReverse = (startPoint.latitude > endPoint.latitude); //取方向

                        double intercept = getInterception(slope, startPoint); //取阶矩

                        double xMoveDistance = isReverse ? getXMoveDistance(slope)
                                : -1 * getXMoveDistance(slope);


                        for (double j = startPoint.latitude;
                             !((j > endPoint.latitude) ^ isReverse);

                             j = j - xMoveDistance) {
                            LatLng latLng = null;
                            if (slope != Double.MAX_VALUE) {
                                latLng = new LatLng(j, (j - intercept) / slope);
                            } else {
                                latLng = new LatLng(j, startPoint.longitude);
                            }

                            final LatLng finalLatLng = latLng;
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    if (bmapView == null || mVirtureRoad == null || mMoveMarker == null || mVirtureRoad.getPoints().isEmpty()) {
                                        return;
                                    }
                                    // refresh marker's position
                                    mMoveMarker.setPosition(finalLatLng);
                                }
                            });
                            try {
                                Thread.sleep(TIME_INTERVAL);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                } catch (Exception e) {
                    Log.d(TAG, "moving thread error: " + e.getLocalizedMessage());
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
}
