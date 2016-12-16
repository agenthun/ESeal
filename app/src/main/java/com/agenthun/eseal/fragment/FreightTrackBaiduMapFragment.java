package com.agenthun.eseal.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.agenthun.eseal.App;
import com.agenthun.eseal.R;
import com.agenthun.eseal.bean.BeidouMasterDeviceInfos;
import com.agenthun.eseal.bean.BeidouNfcDeviceInfos;
import com.agenthun.eseal.bean.BleDeviceInfos;
import com.agenthun.eseal.bean.DeviceLocationInfos;
import com.agenthun.eseal.bean.base.BeidouMasterDevice;
import com.agenthun.eseal.bean.base.BeidouNfcDevice;
import com.agenthun.eseal.bean.base.BleDevice;
import com.agenthun.eseal.bean.base.DeviceLocation;
import com.agenthun.eseal.bean.base.LocationDetail;
import com.agenthun.eseal.connectivity.manager.RetrofitManager;
import com.agenthun.eseal.connectivity.service.PathType;
import com.agenthun.eseal.utils.DeviceSearchSuggestion;
import com.arlib.floatingsearchview.FloatingSearchView;
import com.arlib.floatingsearchview.suggestions.SearchSuggestionsAdapter;
import com.arlib.floatingsearchview.suggestions.model.SearchSuggestion;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import rx.Subscriber;
import rx.functions.Action1;
import rx.functions.Func1;

/**
 * @project ESeal
 * @authors agenthun
 * @date 16/3/7 上午5:47.
 */
public class FreightTrackBaiduMapFragment extends Fragment {

    private static final String TAG = "FreightTrackFragment";

    // 通过设置间隔时间和距离可以控制速度和图标移动的距离
    private static final int TIME_INTERVAL = 80;
    private static final double DISTANCE_RATIO = 10000000.0D;
    private static final double MOVE_DISTANCE_MIN = 0.0001;
    private static final int LOCATION_RADIUS = 50;

    private static final double[] BAIDU_MAP_ZOOM = {
            50, 100, 200, 500, 1000, 2000, 5000, 10000, 20000, 25000,
            50000, 100000, 200000, 500000, 1000000, 2000000
    };

    private List<DeviceSearchSuggestion> suggestionList = new ArrayList<>();

    private MapView bmapView;

    private BaiduMap mBaiduMap;
    private Polyline mVirtureRoad;
    private Marker mMoveMarker;
    private Handler mHandler;

    private double moveDistance = 0.0001;
    private Thread movingThread;

    private FloatingSearchView floatingSearchView;
    private ImageView blurredMap;

    public static FreightTrackBaiduMapFragment newInstance() {
        FreightTrackBaiduMapFragment fragment = new FreightTrackBaiduMapFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_freight_track_baidu_map, container, false);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String token = App.getToken();
        if (token != null) {
            suggestionList.clear();
            /**
             * 获取蓝牙锁访问链路
             */
//            RetrofitManager.builder(PathType.BASE_WEB_SERVICE).getBleDeviceFreightListObservable(token)
            RetrofitManager.builder(PathType.WEB_SERVICE_V2_TEST).getBleDeviceFreightListObservable(token)
                    .subscribe(new Action1<BleDeviceInfos>() {
                        @Override
                        public void call(BleDeviceInfos bleDeviceInfos) {
                            if (bleDeviceInfos == null) return;
                            if (bleDeviceInfos.getResult().get(0).getRESULT() != 1) return;
                            List<BleDevice> details = bleDeviceInfos.getDetails();
                            for (BleDevice detail :
                                    details) {
                                Log.d(TAG, "getBleDevice(): " + detail.toString());
                                DeviceSearchSuggestion suggestion = new DeviceSearchSuggestion(detail);
                                suggestionList.add(suggestion);
                            }
                            Log.d(TAG, "from ble suggestionList.size(): " + suggestionList.size());
                        }
                    }, new Action1<Throwable>() {
                        @Override
                        public void call(Throwable throwable) {
                            Log.d(TAG, "getBleDevice() throwable: " + throwable.getLocalizedMessage());
                        }
                    });

/*            FreightTrackWebService service = RetrofitManager.builder(PathType.WEB_SERVICE_V2_TEST).getFreightTrackWebService();
            Observable.zip(
                    service.getBleDeviceFreightList(token, LanguageUtil.getLanguage()),
                    service.getBeidouMasterDeviceFreightList(token, LanguageUtil.getLanguage()),
                    service.getBeidouNfcDeviceFreightList(token, LanguageUtil.getLanguage()),
            );*/
            /**
             * 北斗终端帽访问链路
             */
            RetrofitManager.builder(PathType.WEB_SERVICE_V2_TEST).getBeidouMasterDeviceFreightListObservable(token)
                    .subscribe(new Action1<BeidouMasterDeviceInfos>() {
                        @Override
                        public void call(BeidouMasterDeviceInfos beidouMasterDeviceInfos) {
                            if (beidouMasterDeviceInfos == null) return;
                            if (beidouMasterDeviceInfos.getResult().get(0).getRESULT() != 1) return;
                            List<BeidouMasterDevice> details = beidouMasterDeviceInfos.getDetails();
                            for (BeidouMasterDevice detail :
                                    details) {
                                Log.d(TAG, "getBeidouMasterDevice(): " + detail.toString());
                                DeviceSearchSuggestion suggestion = new DeviceSearchSuggestion(detail);
                                suggestionList.add(suggestion);
                            }
                            Log.d(TAG, "from beiMaster suggestionList.size(): " + suggestionList.size());
                        }
                    }, new Action1<Throwable>() {
                        @Override
                        public void call(Throwable throwable) {
                            Log.d(TAG, "getBeidouMasterDevice() throwable: " + throwable.getLocalizedMessage());
                        }
                    });

            /**
             * 北斗终端NFC访问链路
             */
            RetrofitManager.builder(PathType.WEB_SERVICE_V2_TEST).getBeidouNfcDeviceFreightListObservable(token)
                    .subscribe(new Action1<BeidouNfcDeviceInfos>() {
                        @Override
                        public void call(BeidouNfcDeviceInfos beidouNfcDeviceInfos) {
                            if (beidouNfcDeviceInfos == null) return;
                            if (beidouNfcDeviceInfos.getResult().get(0).getRESULT() != 1) return;
                            List<BeidouNfcDevice> details = beidouNfcDeviceInfos.getDetails();
                            for (BeidouNfcDevice detail :
                                    details) {
                                Log.d(TAG, "getBeidouNfcDevice(): " + detail.toString());
                                DeviceSearchSuggestion suggestion = new DeviceSearchSuggestion(detail);
                                suggestionList.add(suggestion);
                            }
                            Log.d(TAG, "from beiNfc suggestionList.size(): " + suggestionList.size());
                        }
                    }, new Action1<Throwable>() {
                        @Override
                        public void call(Throwable throwable) {
                            Log.d(TAG, "getBeidouNfcDevice() throwable: " + throwable.getLocalizedMessage());
                        }
                    });
        }

        blurredMap = (ImageView) view.findViewById(R.id.blurredMap);
        bmapView = (MapView) view.findViewById(R.id.bmapView);
        setupBaiduMap();

        mHandler = new Handler();
        loadingMapState(false);

        floatingSearchView = (FloatingSearchView) view.findViewById(R.id.floatingSearchview);
        setupFloatingSearch();
    }

    @Override
    public void onResume() {
        super.onResume();
        bmapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        bmapView.onPause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        bmapView.onDestroy();
    }

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
     * 设置搜索框
     */
    private void setupFloatingSearch() {
        floatingSearchView.setOnQueryChangeListener(new FloatingSearchView.OnQueryChangeListener() {
            @Override
            public void onSearchTextChanged(String oldQuery, String newQuery) {
                if (!oldQuery.equals("") && newQuery.equals("")) {
                    floatingSearchView.clearSuggestions();
                } else {
                    floatingSearchView.showProgress();
                    floatingSearchView.swapSuggestions(suggestionList);
                    floatingSearchView.hideProgress();
                }
            }
        });

        floatingSearchView.setOnBindSuggestionCallback(new SearchSuggestionsAdapter.OnBindSuggestionCallback() {
            @Override
            public void onBindSuggestion(View suggestionView, ImageView leftIcon, TextView textView, SearchSuggestion item, int itemPosition) {
                DeviceSearchSuggestion suggestion = (DeviceSearchSuggestion) item;
                switch (suggestion.getType()) {
                    case DeviceSearchSuggestion.DEVICE_BLE:
                        leftIcon.setImageDrawable(leftIcon.getResources().getDrawable(R.drawable.ic_bluetooth_black_24dp));
                        break;
                    case DeviceSearchSuggestion.DEVICE_BEIDOU_MASTER:
                        leftIcon.setImageDrawable(leftIcon.getResources().getDrawable(R.drawable.ic_lock_black_24dp));
                        break;
                    case DeviceSearchSuggestion.DEVICE_BEIDOU_NFC:
                        leftIcon.setImageDrawable(leftIcon.getResources().getDrawable(R.drawable.ic_nfc_black_24dp));
                        break;
                }
            }
        });

        floatingSearchView.setOnSearchListener(new FloatingSearchView.OnSearchListener() {
            @Override
            public void onSuggestionClicked(SearchSuggestion searchSuggestion) {
                final DeviceSearchSuggestion suggestion = (DeviceSearchSuggestion) searchSuggestion;

                String id = suggestion.getId();
                Integer type = suggestion.getType();
                String name = suggestion.getName();
                Log.d(TAG, "onSuggestionClicked() id = " + id +
                        ", type = " + type +
                        ", name = " + name);

                loadingMapState(true);
                clearLocationData();
                getLocationData(id, type, name);
            }

            @Override
            public void onSearchAction(String currentQuery) {
                Log.d(TAG, "onSearchAction");
            }
        });
    }

    /**
     * 更新地图显示状态
     */
    private void loadingMapState(boolean isLoading) {
        if (isLoading) {
            blurredMap.setVisibility(View.GONE);
            bmapView.setVisibility(View.VISIBLE);
        } else {
            blurredMap.setVisibility(View.VISIBLE);
            bmapView.setVisibility(View.GONE);
        }
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

    /**
     * 访问定位数据信息
     */
    private void getLocationData(final String id, final Integer type, final String name) {
        String token = App.getToken();

        if (token != null) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    switch (type) {
                        case DeviceSearchSuggestion.DEVICE_BLE:
                            String idTset = "1143";//"718";
                            /**
                             * 获取蓝牙锁定位数据信息
                             */
                            RetrofitManager.builder(PathType.WEB_SERVICE_V2_TEST)
                                    .getBleDeviceLocationObservable(App.getToken(), idTset)
                                    .map(new Func1<DeviceLocationInfos, List<LocationDetail>>() {
                                        @Override
                                        public List<LocationDetail> call(DeviceLocationInfos locationInfos) {
                                            if (locationInfos == null ||
                                                    locationInfos.getResult().get(0).getRESULT() != 1)
                                                return new ArrayList<LocationDetail>();

                                            List<LocationDetail> res = locationInfosToLocationDetailList(locationInfos.getDetails());
                                            return res;
                                        }
                                    })
                                    .subscribe(new Subscriber<List<LocationDetail>>() {
                                        @Override
                                        public void onCompleted() {

                                        }

                                        @Override
                                        public void onError(Throwable e) {
                                            Log.d(TAG, "getBleDeviceLocationObservable Error()");
                                            clearLocationData();
                                            loadingMapState(false);
                                        }

                                        @Override
                                        public void onNext(List<LocationDetail> locationDetails) {
                                            showBaiduMap(locationDetails);

                                            if (mOnItemClickListener != null && locationDetails != null && locationDetails.size() != 0) {
                                                mOnItemClickListener.onItemClick(name, id, locationDetails);
                                            }
                                        }
                                    });
                            break;
                        case DeviceSearchSuggestion.DEVICE_BEIDOU_MASTER:
                            /**
                             * 北斗终端帽定位数据信息
                             */
                            RetrofitManager.builder(PathType.WEB_SERVICE_V2_TEST)
                                    .getBeidouMasterDeviceLocationObservable(App.getToken(), id)
                                    .map(new Func1<DeviceLocationInfos, List<LocationDetail>>() {
                                        @Override
                                        public List<LocationDetail> call(DeviceLocationInfos locationInfos) {
                                            if (locationInfos == null ||
                                                    locationInfos.getResult().get(0).getRESULT() != 1)
                                                return new ArrayList<LocationDetail>();

                                            List<LocationDetail> res = locationInfosToLocationDetailList(locationInfos.getDetails());
                                            return res;
                                        }
                                    })
                                    .subscribe(new Subscriber<List<LocationDetail>>() {
                                        @Override
                                        public void onCompleted() {

                                        }

                                        @Override
                                        public void onError(Throwable e) {
                                            Log.d(TAG, "getBeidouMasterDeviceLocationObservable Error()");
                                            clearLocationData();
                                            loadingMapState(false);
                                        }

                                        @Override
                                        public void onNext(List<LocationDetail> locationDetails) {
                                            showBaiduMap(locationDetails);

                                            if (mOnItemClickListener != null && locationDetails != null && locationDetails.size() != 0) {
                                                mOnItemClickListener.onItemClick(name, id, locationDetails);
                                            }
                                        }
                                    });
                            break;
                        case DeviceSearchSuggestion.DEVICE_BEIDOU_NFC:
                            /**
                             * 北斗终端NFC定位数据信息
                             */
                            RetrofitManager.builder(PathType.WEB_SERVICE_V2_TEST)
                                    .getBeidouNfcDeviceLocationObservable(App.getToken(), id)
                                    .map(new Func1<DeviceLocationInfos, List<LocationDetail>>() {
                                        @Override
                                        public List<LocationDetail> call(DeviceLocationInfos locationInfos) {
                                            if (locationInfos == null ||
                                                    locationInfos.getResult().get(0).getRESULT() != 1)
                                                return new ArrayList<LocationDetail>();

                                            List<LocationDetail> res = locationInfosToLocationDetailList(locationInfos.getDetails());
                                            return res;
                                        }
                                    })
                                    .subscribe(new Subscriber<List<LocationDetail>>() {
                                        @Override
                                        public void onCompleted() {

                                        }

                                        @Override
                                        public void onError(Throwable e) {
                                            Log.d(TAG, "getBeidouNfcDeviceLocationObservable Error()");
                                            clearLocationData();
                                            loadingMapState(false);
                                        }

                                        @Override
                                        public void onNext(List<LocationDetail> locationDetails) {
                                            showBaiduMap(locationDetails);

                                            if (mOnItemClickListener != null && locationDetails != null && locationDetails.size() != 0) {
                                                mOnItemClickListener.onItemClick(name, id, locationDetails);
                                            }
                                        }
                                    });
                            break;
                    }
                }
            }).start();
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
            String status = detail.getStatus();
            String[] location = detail.getBaiduCoordinate().split(",");
            LatLng lng = new LatLng(
                    Double.parseDouble(location[0]),
                    Double.parseDouble(location[1])
            );
//            converter.coord(lng);
//            lng = converter.convert();
            result.add(new LocationDetail(time, status, lng));
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

        OverlayOptions polylineOptions = new PolylineOptions()
                .points(polylines)
                .width(8)
                .color(ContextCompat.getColor(getActivity(), R.color.red_500));

        mVirtureRoad = (Polyline) mBaiduMap.addOverlay(polylineOptions);
        OverlayOptions markerOptions = new MarkerOptions().flat(true).anchor(0.5f, 0.5f).icon(BitmapDescriptorFactory
                .fromResource(R.drawable.ic_car)).position(polylines.get(0)).rotate((float) getAngle(0));
        mMoveMarker = (Marker) mBaiduMap.addOverlay(markerOptions);

        //设置中心点
        setBaiduMapAdaptedZoom(polylines);
        if (countInCircle < polylines.size() / 2) {
            movingThread = new Thread(new MyThread());
            movingThread.start();
        }

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


    //itemClick interface
    public interface OnItemClickListener {
        void onItemClick(String containerNo, String containerId, List<LocationDetail> details);
    }

    private OnItemClickListener mOnItemClickListener;

    public void setOnItemClickListener(OnItemClickListener mOnItemClickListener) {
        this.mOnItemClickListener = mOnItemClickListener;
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
