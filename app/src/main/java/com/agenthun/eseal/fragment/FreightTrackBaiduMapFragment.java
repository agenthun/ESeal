package com.agenthun.eseal.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;

import com.agenthun.eseal.App;
import com.agenthun.eseal.R;
import com.agenthun.eseal.bean.DeviceLocationInfos;
import com.agenthun.eseal.bean.base.DeviceLocation;
import com.agenthun.eseal.bean.base.LocationDetail;
import com.agenthun.eseal.connectivity.manager.RetrofitManager;
import com.agenthun.eseal.connectivity.service.PathType;
import com.agenthun.eseal.utils.DeviceSearchSuggestion;
import com.agenthun.eseal.utils.LanguageUtil;
import com.agenthun.eseal.utils.PreferencesHelper;
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
import com.umeng.analytics.MobclickAgent;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.Subscriber;
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

    private BaiduMap mBaiduMap;
    private Polyline mVirtureRoad;
    private Marker mMoveMarker;
    private Handler mHandler;

    private double moveDistance = 0.0001;
    private Thread movingThread;

    @Bind(R.id.bmapView)
    MapView bmapView;
    @Bind(R.id.webView)
    WebView webView;
    @Bind(R.id.blurredMap)
    ImageView blurredMap;

    private FloatingSearchView floatingSearchView;

    private boolean mUsingWebView = false;

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
        ButterKnife.bind(this, view);

        //根据当前系统语言设置加载不同的Map Ui
        mUsingWebView = "zh-CN".equals(LanguageUtil.getLanguage()) ? false : true;
//        mUsingWebView = true; //for test webviewMap
        setupMapUi(mUsingWebView, false);
        if (mUsingWebView) {
            setupWebView();
        }

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String token = PreferencesHelper.getTOKEN(getContext());
        if (token != null) {
            suggestionList.clear();
            //获取所有设备货物信息
            RetrofitManager.builder(PathType.WEB_SERVICE_V2_TEST)
                    .getAllDeviceFreightListObservable(token)
                    .subscribe(new Subscriber<List<DeviceSearchSuggestion>>() {
                        @Override
                        public void onCompleted() {

                        }

                        @Override
                        public void onError(Throwable e) {
                            Log.d(TAG, "onError: getAllDeviceFreightListObservable()");
                        }

                        @Override
                        public void onNext(List<DeviceSearchSuggestion> deviceSearchSuggestions) {
                            if (deviceSearchSuggestions != null && !deviceSearchSuggestions.isEmpty()) {
                                suggestionList.addAll(deviceSearchSuggestions);
                            }
                        }
                    });
        }

        setupBaiduMap();

        mHandler = new Handler();

        floatingSearchView = (FloatingSearchView) view.findViewById(R.id.floatingSearchview);
        setupFloatingSearch();
    }

    @Override
    public void onResume() {
        super.onResume();
        bmapView.onResume();
        MobclickAgent.onPageStart(TAG);
    }

    @Override
    public void onPause() {
        super.onPause();
        bmapView.onPause();
        MobclickAgent.onPageEnd(TAG);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        bmapView.onDestroy();
    }

    private void setupMapUi(boolean usingWebView) {
        if (usingWebView) {
            bmapView.setVisibility(View.GONE);
            webView.setVisibility(View.VISIBLE);
        } else {
            bmapView.setVisibility(View.VISIBLE);
            webView.setVisibility(View.GONE);
        }
    }

    /**
     * 更新地图显示状态
     */
    private void setupMapUi(boolean usingWebView, boolean enable) {
        if (enable) {
            if (usingWebView) {
                bmapView.setVisibility(View.GONE);
                webView.setVisibility(View.VISIBLE);
            } else {
                bmapView.setVisibility(View.VISIBLE);
                webView.setVisibility(View.GONE);
            }
            blurredMap.setVisibility(View.GONE);
        } else {
            bmapView.setVisibility(View.GONE);
            webView.setVisibility(View.GONE);
            blurredMap.setVisibility(View.VISIBLE);
        }
    }

    private void setupWebView() {
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_INSET);
        WebSettings webSettings = webView.getSettings();
        webSettings.setAllowFileAccess(true);
        webSettings.setBuiltInZoomControls(true);
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

                setupMapUi(mUsingWebView, true);
                if (!mUsingWebView) {
                    clearLocationData();
                }
                getLocationData(id, type, name);
                umengOnEvent(id, type, name); //友盟API
            }

            @Override
            public void onSearchAction(String currentQuery) {
                Log.d(TAG, "onSearchAction");
            }
        });
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
        String token = PreferencesHelper.getTOKEN(getContext());

        if (token != null) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    switch (type) {
                        case DeviceSearchSuggestion.DEVICE_BLE:
                            /**
                             * 获取蓝牙锁定位数据信息
                             */
                            RetrofitManager.builder(PathType.WEB_SERVICE_V2_TEST)
                                    .getBleAndBeidouNfcDeviceLocationObservable(PreferencesHelper.getTOKEN(getContext()), id)
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
                                            Log.d(TAG, "onError: getBleAndBeidouNfcDeviceLocationObservable()");
                                            if (!mUsingWebView) {
                                                clearLocationData();
                                            }
                                            setupMapUi(mUsingWebView, false);
                                        }

                                        @Override
                                        public void onNext(List<LocationDetail> locationDetails) {
                                            if (!mUsingWebView) {
                                                showBaiduMap(locationDetails);
                                            } else {
                                                showWebViewMap(locationDetails);
                                            }

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
                                            Log.d(TAG, "onError: getBeidouMasterDeviceLocationObservable()");
                                            if (!mUsingWebView) {
                                                clearLocationData();
                                            }
                                            setupMapUi(mUsingWebView, false);
                                        }

                                        @Override
                                        public void onNext(List<LocationDetail> locationDetails) {
                                            if (!mUsingWebView) {
                                                showBaiduMap(locationDetails);
                                            } else {
                                                showWebViewMap(locationDetails);
                                            }

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
                                    .getBleAndBeidouNfcDeviceLocationObservable(App.getToken(), id)
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
                                            Log.d(TAG, "onError: getBleAndBeidouNfcDeviceLocationObservable()");
                                            if (!mUsingWebView) {
                                                clearLocationData();
                                            }
                                            setupMapUi(mUsingWebView, false);
                                        }

                                        @Override
                                        public void onNext(List<LocationDetail> locationDetails) {
                                            if (mOnItemClickListener != null && locationDetails != null && locationDetails.size() != 0) {
                                                mOnItemClickListener.onItemClick(name, id, locationDetails);
                                            }

                                            if (!mUsingWebView) {
                                                showBaiduMap(locationDetails);
                                            } else {
                                                showWebViewMap(locationDetails);
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
     * 加载轨迹数据至WebView Google地图
     */
    private void showWebViewMap(List<LocationDetail> locationDetails) {
        if (locationDetails == null || locationDetails.size() == 0) return;

        final List<LatLng> polylines = new ArrayList<>();
        for (LocationDetail locationDetail :
                locationDetails) {
            if (locationDetail.isInvalid()) continue;

            LatLng lng = locationDetail.getLatLng();
            polylines.add(lng);
        }

        Collections.reverse(polylines); //按时间正序

        webView.post(new Runnable() {
            @Override
            public void run() {
                String data = buildHtmlMap(polylines);
//                String data = buildHtmlSample();

                //loadData不支持#、%、\、? 四种字符，用loadDataWithBaseURL
                webView.loadDataWithBaseURL(null, data, "text/html", "UTF-8", null);
            }
        });
    }

    private String buildHtmlSample() {
        return "<html><body><font color='red'>hello baidu!</font></body></html>";
    }

    private String buildHtmlMap(List<LatLng> polylines) {
        StringBuffer html = new StringBuffer();

        double minLat = polylines.get(0).latitude;
        double maxLat = polylines.get(0).latitude;
        double minLng = polylines.get(0).longitude;
        double maxLng = polylines.get(0).longitude;

        LatLng point;

        html.append("<!DOCTYPE html>");
        html.append("<head>");
        html.append("<meta charset='utf-8'>");
        html.append("<style>");
        html.append("#map {height: 100%;}");
        html.append("html, body {height: 100%;margin: 0;padding: 0;}");
        html.append("</style>");
        html.append("</head>");
        html.append("<body>");
        html.append("<div id='map'></div>");
        html.append("<script>");
        html.append("var neighborhoods = [");

        html.append("{lat: " + minLat + ", lng: " + minLng + "},");

        for (int i = 1; i < polylines.size(); i++) {
            point = polylines.get(i);
            if (point.latitude < minLat) minLat = point.latitude;
            if (point.latitude > maxLat) maxLat = point.latitude;
            if (point.longitude < minLng) minLng = point.longitude;
            if (point.longitude > maxLng) maxLng = point.longitude;

            html.append("{lat: " + point.latitude + ", lng: " + point.longitude + "},");
        }

        //设置中心点,以及缩放参数
        double centerLat = (maxLat + minLat) / 2;
        double centerLng = (maxLng + minLng) / 2;
        int zoom = getGoogleMapZoom(minLat, maxLat, minLng, maxLng);

        html.deleteCharAt(html.lastIndexOf(","));

        html.append("];");
        html.append("var markers = [];");
        html.append("var map;");
        html.append("function initMap() {");
        html.append("map = new google.maps.Map(document.getElementById('map'), {");
        html.append("zoom: " + zoom + ",");
        html.append("center: {lat: " + centerLat + ", lng: " + centerLng + "}");
        html.append("});");

        if (polylines.size() == 1) {
            html.append("drop();");
        }

        html.append("var flightPath = new google.maps.Polyline({");
        html.append("path: neighborhoods,");
        html.append("geodesic: true,");
        html.append("strokeColor: '#FF0000',");
        html.append("strokeOpacity: 1.0,");
        html.append("strokeWeight: 2");
        html.append("});");
        html.append("flightPath.setMap(map);");
        html.append("}");
        html.append("function drop() {");
        html.append("clearMarkers();");
        html.append("for (var i = 0; i < neighborhoods.length; i++) {");
        html.append("addMarkerWithTimeout(neighborhoods[i], i * 200);");
        html.append("}");
        html.append("}");
        html.append("function addMarkerWithTimeout(position, timeout) {");
        html.append("window.setTimeout(function() {");
        html.append("markers.push(new google.maps.Marker({");
        html.append("position: position,");
        html.append("map: map");
//        html.append("animation: google.maps.Animation.DROP");
        html.append("}));");
        html.append("}, timeout);");
        html.append("}");
        html.append("function clearMarkers() {");
        html.append("for (var i = 0; i < markers.length; i++) {");
        html.append("markers[i].setMap(null);");
        html.append("}");
        html.append("markers = [];");
        html.append("}");
        html.append("</script>");
        html.append("<script async defer src='https://maps.googleapis.com/maps/api/js?key=AIzaSyAp2aNol3FhJypghIA2IUZIOkNTwo6YPbY&callback=initMap'></script>");
        html.append("</body>");
        html.append("</html>");

        return html.toString();
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
     * 获取Google地图显示等级
     * 范围0-18级
     */
    private int getGoogleMapZoom(double minLat, double maxLat, double minLng, double maxLng) {
        LatLng minLatLng = new LatLng(minLat, minLng);
        LatLng maxLatLng = new LatLng(maxLat, maxLng);
        double distance = DistanceUtil.getDistance(minLatLng, maxLatLng);

        if (distance == 0.0d) {
            return 12;
        }
        if (distance > 0.0d && distance <= 100.0d) {
            return 18;
        }

        for (int i = 0; i < BAIDU_MAP_ZOOM.length; i++) {
            if (BAIDU_MAP_ZOOM[i] - distance > 0) {
                moveDistance = (BAIDU_MAP_ZOOM[i] - distance) / DISTANCE_RATIO;
                Log.d(TAG, "getZoom() moveDistance = " + moveDistance);
                return 18 - i;
            }
        }
        return 12;
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
        if (!mUsingWebView) {
            clearLocationData();
        }
        setupMapUi(mUsingWebView, false);
    }

    private void showMessage(String message) {
        Snackbar snackbar = Snackbar.make(getView(), message, Snackbar.LENGTH_LONG)
                .setAction("Action", null);
        ((TextView) (snackbar.getView().findViewById(R.id.snackbar_text)))
                .setTextColor(ContextCompat.getColor(getContext(), R.color.blue_grey_100));
        snackbar.show();
    }

    private void loadFreightLocation(@NonNull final String token, @NonNull String id,
                                     @Nullable String from, @Nullable String to) {
        //获取时间段内位置列表
/*        RetrofitManager.builder(PathType.WEB_SERVICE_V2_TEST).getFreightLocationListObservable(token, id, from, to)
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
                        mLocationDetailList = locationDetails;
                        if (!mUsingWebView) {
                            clearLocationData();
                            showBaiduMap(locationDetails);
                        } else {
                            showWebViewMap(locationDetails);
                        }
                    }
                });*/
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

    private void umengOnEvent(String id, Integer type, String name) {
        Map<String, String> map = new HashMap<>();
        map.put("deviceId", id);
        map.put("name", name);
        switch (type) {
            case DeviceSearchSuggestion.DEVICE_BLE:
                map.put("type", "BLE");
                break;
            case DeviceSearchSuggestion.DEVICE_BEIDOU_MASTER:
                map.put("type", "BEIDOU_MASTER");
                break;
            case DeviceSearchSuggestion.DEVICE_BEIDOU_NFC:
                map.put("type", "BEIDOU_NFC");
                break;
            default:
                break;
        }
        MobclickAgent.onEvent(getContext(), "getLocationData", map);
    }
}
