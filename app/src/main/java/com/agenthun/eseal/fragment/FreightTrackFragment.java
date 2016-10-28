package com.agenthun.eseal.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageView;

import com.agenthun.eseal.App;
import com.agenthun.eseal.R;
import com.agenthun.eseal.bean.FreightInfosByToken;
import com.agenthun.eseal.bean.base.Detail;
import com.agenthun.eseal.connectivity.manager.RetrofitManager;
import com.agenthun.eseal.connectivity.service.Api;
import com.agenthun.eseal.connectivity.service.PathType;
import com.agenthun.eseal.utils.ContainerNoSuggestion;
import com.agenthun.eseal.utils.UiUtils;
import com.arlib.floatingsearchview.FloatingSearchView;
import com.arlib.floatingsearchview.suggestions.SearchSuggestionsAdapter;
import com.arlib.floatingsearchview.suggestions.model.SearchSuggestion;
import com.arlib.floatingsearchview.util.view.BodyTextView;
import com.arlib.floatingsearchview.util.view.IconImageView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * @project ESeal
 * @authors agenthun
 * @date 16/3/7 上午5:47.
 */
public class FreightTrackFragment extends Fragment {

    private static final String TAG = "FreightTrackFragment";
    private static final String ARG_TYPE = "TYPE";
    private static final String ARG_CONTAINER_NO = "CONTAINER_NO";

    private String mType;
    private String mContainerNo;
    private List<ContainerNoSuggestion> suggestionList = new ArrayList<>();

    private WebView webView;
    private FloatingSearchView floatingSearchView;
    private ImageView blurredMap;

    private int mScreenWidth;
    private int mScreenHeight;

    public static FreightTrackFragment newInstance(String type, String containerNo) {
        Bundle args = new Bundle();
        args.putString(ARG_TYPE, type);
        args.putString(ARG_CONTAINER_NO, containerNo);
        FreightTrackFragment fragment = new FreightTrackFragment();
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
            RetrofitManager.builder(PathType.WEB_SERVICE_V2_TEST).getFreightListObservable(token).enqueue(new Callback<FreightInfosByToken>() {
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

        mScreenWidth = UiUtils.getScreenWidthPixels(getActivity());
        mScreenWidth = UiUtils.pxToDip(getActivity(), mScreenWidth);
        mScreenHeight = UiUtils.getScreenHeightPixels(getActivity());
        mScreenHeight = UiUtils.pxToDip(getActivity(), mScreenHeight);
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_freight_track, container, false);

        blurredMap = (ImageView) view.findViewById(R.id.blurredMap);

/*        blurredMap.post(new Runnable() {
            @Override
            public void run() {
                mScreenWidth = UiUtils.pxToDip(getActivity(), blurredMap.getWidth());
                mScreenHeight = UiUtils.pxToDip(getActivity(), blurredMap.getHeight());
            }
        });*/

        webView = (WebView) view.findViewById(R.id.webView);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_INSET);
        WebSettings webSettings = webView.getSettings();
        webSettings.setAllowFileAccess(true);
        webSettings.setBuiltInZoomControls(true);

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

                webView.post(new Runnable() {
                    @Override
                    public void run() {
                        //webView.loadUrl(Api.AMAP_SERVICE_URL_STRING + "FreightTrackPath.aspx?token=" + App.getToken() + "&Type=0&ContainerId=" + containerId + "&language=l");
                        webView.loadUrl(Api.MAP_SERVICE_V2_URL_STRING
                                + "GFreightTrackPath_CN_Hu.aspx?token=" + App.getToken()
                                + "&containerId=" + containerId
                                + "&mobileWidth=" + mScreenWidth
                                + "&mobileHight=" + mScreenHeight
                                + "&language=zh-CN");
                    }
                });
            }

            @Override
            public void onSearchAction() {
                Log.d(TAG, "onSearchAction");
            }
        });
        return view;
    }

    private void loadingMapState(boolean isLoading) {
        if (isLoading) {
            blurredMap.setVisibility(View.GONE);
            webView.setVisibility(View.VISIBLE);
        } else {
            blurredMap.setVisibility(View.VISIBLE);
            webView.setVisibility(View.GONE);
        }
    }

    //itemClick interface
    public interface OnItemClickListener {
        void onItemClick(String containerNo, String containerId);
    }

    private OnItemClickListener mOnItemClickListener;

    public void setOnItemClickListener(OnItemClickListener mOnItemClickListener) {
        this.mOnItemClickListener = mOnItemClickListener;
    }
}
