package com.agenthun.eseal.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.agenthun.eseal.App;
import com.agenthun.eseal.R;
import com.agenthun.eseal.bean.FreightInfosByToken;
import com.agenthun.eseal.bean.base.Detail;
import com.agenthun.eseal.connectivity.manager.RetrofitManager;
import com.agenthun.eseal.connectivity.service.PathType;
import com.agenthun.eseal.utils.ContainerNoSuggestion;
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
public class ScanDeviceFragment extends Fragment {

    private static final String TAG = "FreightTrackFragment";
    private static final String ARG_TYPE = "TYPE";
    private static final String ARG_CONTAINER_NO = "CONTAINER_NO";

    private String mType;
    private String mContainerNo;

    public static ScanDeviceFragment newInstance(String type, String containerNo) {
        Bundle args = new Bundle();
        args.putString(ARG_TYPE, type);
        args.putString(ARG_CONTAINER_NO, containerNo);
        ScanDeviceFragment fragment = new ScanDeviceFragment();
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
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_scan_device, container, false);
        return view;
    }
}
