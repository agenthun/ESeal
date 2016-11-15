package com.agenthun.eseal.fragment;

import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.agenthun.eseal.App;
import com.agenthun.eseal.R;
import com.agenthun.eseal.bean.AllDynamicDataByContainerId;
import com.agenthun.eseal.bean.FreightInfosByToken;
import com.agenthun.eseal.bean.base.Detail;
import com.agenthun.eseal.connectivity.manager.RetrofitManager;
import com.agenthun.eseal.connectivity.service.PathType;
import com.agenthun.eseal.utils.ContainerNoSuggestion;
import com.agenthun.eseal.utils.UiUtil;
import com.agenthun.eseal.utils.baidumap.Utils;
import com.arlib.floatingsearchview.FloatingSearchView;
import com.arlib.floatingsearchview.suggestions.SearchSuggestionsAdapter;
import com.arlib.floatingsearchview.suggestions.model.SearchSuggestion;
import com.arlib.floatingsearchview.util.view.BodyTextView;
import com.arlib.floatingsearchview.util.view.IconImageView;
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
import com.baidu.mapapi.utils.CoordinateConverter;
import com.baidu.mapapi.utils.SpatialRelationUtil;

import java.io.IOException;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @project ESeal
 * @authors agenthun
 * @date 16/3/7 上午5:47.
 */
public class FreightTrackBaiduMapFragment extends Fragment {

    private static final String TAG = "FreightTrackFragment";

    // 通过设置间隔时间和距离可以控制速度和图标移动的距离
    private static final int TIME_INTERVAL = 80;
    private static final double DISTANCE = 0.0001;

    private static final String ARG_TYPE = "TYPE";
    private static final String ARG_CONTAINER_NO = "CONTAINER_NO";
    private static final int LOCATION_RADIUS = 50;

    private String mType;
    private String mContainerNo;
    private List<ContainerNoSuggestion> suggestionList = new ArrayList<>();

    private MapView bmapView;
    private BaiduMap mBaiduMap;
    private Polyline mVirtureRoad;
    private Marker mMoveMarker;
    private Handler mHandler;

    private Thread movingThread;
    private boolean isMoving = true;

    private FloatingSearchView floatingSearchView;
    private ImageView blurredMap;

    public static FreightTrackBaiduMapFragment newInstance(String type, String containerNo) {
        Bundle args = new Bundle();
        args.putString(ARG_TYPE, type);
        args.putString(ARG_CONTAINER_NO, containerNo);
        FreightTrackBaiduMapFragment fragment = new FreightTrackBaiduMapFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mType = getArguments().getString(ARG_TYPE);
            mContainerNo = getArguments().getString(ARG_CONTAINER_NO);
        }

        String token = App.getToken();
        if (token != null) {
//            RetrofitManager.builder(PathType.WEB_SERVICE_V2_TEST).getFreightListObservable(token).enqueue(new Callback<FreightInfosByToken>() {
            RetrofitManager.builder(PathType.BASE_WEB_SERVICE).getFreightListObservable(token).enqueue(new Callback<FreightInfosByToken>() {
                @Override
                public void onResponse(Call<FreightInfosByToken> call, Response<FreightInfosByToken> response) {
                    if (response.body() == null) return;
                    List<Detail> details = response.body().getDetails();
                    for (Detail detail :
                            details) {
                        Log.d(TAG, "onResponse() returned: " + detail.toString());
                        ContainerNoSuggestion containerNoSuggestion = new ContainerNoSuggestion(detail);
                        suggestionList.add(containerNoSuggestion);
                    }
                }

                @Override
                public void onFailure(Call<FreightInfosByToken> call, Throwable t) {
                    Log.d(TAG, "Response onFailure: " + t.getLocalizedMessage());
                }
            });
        }
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_freight_track_baidu_map, container, false);

        blurredMap = (ImageView) view.findViewById(R.id.blurredMap);
        bmapView = (MapView) view.findViewById(R.id.bmapView);
        bmapView.showZoomControls(false); //移除地图缩放控件
        bmapView.removeViewAt(1); //移除百度地图Logo

        mBaiduMap = bmapView.getMap();
        mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
        mBaiduMap.setMapStatus(MapStatusUpdateFactory.zoomTo(16));
//        int padding = (int) UiUtil.dipToPx(getContext(), 8);
//        mBaiduMap.setViewPadding(
//                padding,
//                0,
//                padding,
//                padding * 12);
        mBaiduMap.getUiSettings().setOverlookingGesturesEnabled(false); //取消俯视手势

        mHandler = new Handler();

        loadingMapState(false);

        floatingSearchView = (FloatingSearchView) view.findViewById(R.id.floatingSearchview);
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
            public void onBindSuggestion(IconImageView leftIcon, BodyTextView bodyText, SearchSuggestion item, int itemPosition) {
                ContainerNoSuggestion containerNoSuggestion = (ContainerNoSuggestion) item;
                if (containerNoSuggestion.getIsHistory()) {
                    leftIcon.setImageDrawable(leftIcon.getResources().getDrawable(R.drawable.ic_history_black_24dp));
                    leftIcon.setAlpha(.36f);
                } else {
                    Log.d(TAG, "onBindSuggestion() returned: " + containerNoSuggestion.getDetail().toString());
                }
            }
        });
        floatingSearchView.setOnSearchListener(new FloatingSearchView.OnSearchListener() {
            @Override
            public void onSuggestionClicked(SearchSuggestion searchSuggestion) {
                ((ContainerNoSuggestion) searchSuggestion).setIsHistory(true);

                final String containerId = ((ContainerNoSuggestion) searchSuggestion).getDetail().getContainerId();
                Log.d(TAG, "onSuggestionClicked() containerId = " + containerId);
                String containerNo = ((ContainerNoSuggestion) searchSuggestion).getDetail().getContainerNo();
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onItemClick(containerNo, containerId);
                }

                loadingMapState(true);


                clearRoadData();
                getLocationData(containerId);
//                initRoadData(containerId);
//                moveLooper();

//                List<LatLng> pts = new ArrayList<>();
//                for (int i = 0; i < 10; i++) {
//                    LatLng pt = new LatLng(31.041764, 121.465735 + Math.random() % 10);
//                    pts.add(pt);
//                }
//
////                BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.avator);
//                OverlayOptions options = new PolylineOptions().points(pts).width(10)
//                        .color(ContextCompat.getColor(getActivity(), R.color.colorAccentDark));
//                mBaiduMap.addOverlay(options);
//                mBaiduMap.setMapStatus(MapStatusUpdateFactory.);
            }

            @Override
            public void onSearchAction() {
                Log.d(TAG, "onSearchAction");
            }
        });
        return view;
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

    private void loadingMapState(boolean isLoading) {
        if (isLoading) {
            blurredMap.setVisibility(View.GONE);
            bmapView.setVisibility(View.VISIBLE);
        } else {
            blurredMap.setVisibility(View.VISIBLE);
            bmapView.setVisibility(View.GONE);
        }
    }

    private void clearRoadData() {
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
//            Log.d(TAG, "clearRoadData() returned: movingThread end");
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

    private void initRoadData(String containerId) {
        // init latlng data
        double centerLatitude = 31.041764;
        double centerLontitude = 121.465735;
        double deltaAngle = Math.PI / 180 * 5;
        double radius = 0.02;
        OverlayOptions polylineOptions;

        List<LatLng> polylines = new ArrayList<LatLng>();
        for (double i = 0; i < Math.PI * 1; i = i + deltaAngle) {
            float latitude = (float) (-Math.cos(i) * radius + centerLatitude);
            float longtitude = (float) (Math.sin(i) * radius + centerLontitude);
            polylines.add(new LatLng(latitude, longtitude));
            if (i > Math.PI) {
                deltaAngle = Math.PI / 180 * 30;
            }
        }

//        Random r = new Random();
//        for (int i = 0; i < 10; i++) {
//            int rlat = r.nextInt(370000);
//            int rlng = r.nextInt(370000);
//            int lat = (int) (centerLatitude * 1E6 + rlat);
//            int lng = (int) (centerLontitude * 1E6 + rlng);
//            LatLng ll = new LatLng(lat / 1E6, lng / 1E6);
//            polylines.add(ll);
//        }

//        polylines = getLocationData(containerId);

        //设置中心点
        LatLng pt = new LatLng(centerLatitude, centerLontitude);
        mBaiduMap.setMapStatus(MapStatusUpdateFactory.newLatLng(pt));

        polylineOptions = new PolylineOptions()
                .points(polylines)
                .width(8)
                .color(ContextCompat.getColor(getActivity(), R.color.colorAccentDark));

        mVirtureRoad = (Polyline) mBaiduMap.addOverlay(polylineOptions);
        OverlayOptions markerOptions = new MarkerOptions().flat(true).anchor(0.5f, 0.5f).icon(BitmapDescriptorFactory
                .fromResource(R.drawable.ic_car)).position(polylines.get(0)).rotate((float) getAngle(0));
        mMoveMarker = (Marker) mBaiduMap.addOverlay(markerOptions);
    }

    private void getLocationData(final String containerId) {
        String token = App.getToken();

        if (token != null) {
            Map<String, LatLng> map = new TreeMap<>();
            final SoftReference<Map<String, LatLng>> reference = new SoftReference<Map<String, LatLng>>(map);

            final WebPage webPage = new WebPage(1, Integer.MAX_VALUE, true);

/*            while (webPage.getCurrent() <= webPage.getTotal()) {

                int tempPage = webPage.getCurrent();
                RetrofitManager.builder(PathType.BASE_WEB_SERVICE)
                        .getFreightDataListObservable(token, containerId, tempPage)
                        .enqueue(new Callback<AllDynamicDataByContainerId>() {
                            @Override
                            public void onResponse(Call<AllDynamicDataByContainerId> call, Response<AllDynamicDataByContainerId> response) {
                                Result result = response.body().getResult().get(0);
                                if (result.getRESULT() == 0) {
                                    return;
                                }

                                if (webPage.isFirstRequest()) {
                                    webPage.setFirstRequest(false);
                                    webPage.setTotal(result.getCOUNTPAGES());
                                }

                                String time = "";
                                float latitude = 0;
                                float longtitude = 0;
                                reference.get().put(time, new LatLng(latitude, longtitude));

                                int currentPage = webPage.getCurrent();
                                webPage.setCurrent(++currentPage);
                            }

                            @Override
                            public void onFailure(Call<AllDynamicDataByContainerId> call, Throwable t) {
                                Log.d(TAG, "Response onFailure: " + t.getLocalizedMessage());
                            }
                        });

                int currentPage = webPage.getCurrent();
                webPage.setCurrent(++currentPage);
            }*/
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        AllDynamicDataByContainerId r = RetrofitManager.builder(PathType.BASE_WEB_SERVICE)
                                .getFreightDataListObservable(App.getToken(), containerId, 1)
                                .execute().body();

                        if (r.getResult().get(0).getRESULT() == 0) {
                            return;
                        }

                        //GPS坐标转百度地图坐标
                        CoordinateConverter converter = new CoordinateConverter();
                        converter.from(CoordinateConverter.CoordType.GPS);

                        List<Detail> details = r.getDetails();
                        for (Detail detail :
                                details) {
                            String time = detail.getCreateDatetime();
                            String[] location = detail.getCoordinate().split(",");
                            LatLng lng = new LatLng(
                                    Double.parseDouble(location[0]),
                                    Double.parseDouble(location[1])
                            );
                            converter.coord(lng);

                            reference.get().put(time, converter.convert());
                        }

                        int countInCircle = 0;

                        List<LatLng> polylines = new ArrayList<>();
                        Iterator iterator = reference.get().values().iterator();
                        while (iterator.hasNext()) {
                            LatLng lng = (LatLng) iterator.next();
                            polylines.add(lng);

                            if (polylines.size() > 1) {
                                if (SpatialRelationUtil.isCircleContainsPoint(polylines.get(0), LOCATION_RADIUS, lng)) {
                                    countInCircle++;
                                }
                            }
                        }

                        OverlayOptions polylineOptions = new PolylineOptions()
                                .points(polylines)
                                .width(8)
                                .color(ContextCompat.getColor(getActivity(), R.color.red_500));

                        mVirtureRoad = (Polyline) mBaiduMap.addOverlay(polylineOptions);
                        OverlayOptions markerOptions = new MarkerOptions().flat(true).anchor(0.5f, 0.5f).icon(BitmapDescriptorFactory
                                .fromResource(R.drawable.ic_car)).position(polylines.get(0)).rotate((float) getAngle(0));
                        mMoveMarker = (Marker) mBaiduMap.addOverlay(markerOptions);

                        //设置中心点
                        if (countInCircle < polylines.size() / 2) {
                            mBaiduMap.setMapStatus(MapStatusUpdateFactory.newLatLngZoom(polylines.get(0), 16));

//                            moveLooper();
                            movingThread = new Thread(new MyThread());
                            movingThread.start();
                        } else {
                            mBaiduMap.setMapStatus(MapStatusUpdateFactory.newLatLngZoom(polylines.get(0), 20));
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
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
            return DISTANCE;
        }
        return Math.abs((DISTANCE * slope) / Math.sqrt(1 + slope * slope));
    }

    /**
     * 循环进行移动逻辑
     */
    public void moveLooper() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (isMoving) {

                    for (int i = 0; isMoving && i < mVirtureRoad.getPoints().size() - 1; i++) {

                        final LatLng startPoint = mVirtureRoad.getPoints().get(i);
                        final LatLng endPoint = mVirtureRoad.getPoints().get(i + 1);
                        mMoveMarker.setPosition(startPoint);

                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                // refresh marker's rotate
                                if (bmapView == null) {
                                    return;
                                }
                                mMoveMarker.setRotate((float) getAngle(startPoint,
                                        endPoint));
                            }
                        });
                        double slope = getSlope(startPoint, endPoint);
                        //是不是正向的标示（向上设为正向）
                        boolean isReverse = (startPoint.latitude > endPoint.latitude);

                        double intercept = getInterception(slope, startPoint);

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
                                    if (bmapView == null) {
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
                }
            }
        }).start();
    }

    //itemClick interface
    public interface OnItemClickListener {
        void onItemClick(String containerNo, String containerId);
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

    private class WebPage {
        int current;
        int total;
        boolean isFirstRequest;

        public WebPage() {
        }

        public WebPage(int current, int total, boolean isFirstRequest) {
            this.current = current;
            this.total = total;
            this.isFirstRequest = isFirstRequest;
        }

        public int getCurrent() {
            return current;
        }

        public void setCurrent(int current) {
            this.current = current;
        }

        public int getTotal() {
            return total;
        }

        public void setTotal(int total) {
            this.total = total;
        }

        public boolean isFirstRequest() {
            return isFirstRequest;
        }

        public void setFirstRequest(boolean firstRequest) {
            isFirstRequest = firstRequest;
        }
    }
}
