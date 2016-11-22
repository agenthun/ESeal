package com.agenthun.eseal;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.agenthun.eseal.bean.FreightInfos;
import com.agenthun.eseal.bean.UserInfoByGetToken;
import com.agenthun.eseal.bean.base.Detail;
import com.agenthun.eseal.connectivity.manager.RetrofitManager;
import com.agenthun.eseal.connectivity.service.PathType;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import rx.functions.Action1;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ApplicationTest {
    private static final String TAG = "ApplicationTest";

    private String token = "a5b6fb3cad8644c2af46b89308196eea";

    @Test
    public void useAppContext() throws Exception {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();

        assertEquals("com.agenthun.eseal", appContext.getPackageName());
    }

    @Test
    public void doGetTokenObservable() {
        String name = "henghu";
        String password = "123456";

/*        RetrofitManager.builder(PathType.WEB_SERVICE_V2_TEST).getTokenObservable(name, password)
                .subscribe(new Action1<UserInfoByGetToken>() {
                    @Override
                    public void call(UserInfoByGetToken userInfoByGetToken) {
                        if (userInfoByGetToken == null) return;
                        token = userInfoByGetToken.getTOKEN();
                        System.out.println("token = " + token);
                    }
                });*/
    }

    @Test
    public void doGetBeidouMasterDeviceFreightListObservable() {
        RetrofitManager.builder(PathType.WEB_SERVICE_V2_TEST).getBeidouMasterDeviceFreightListObservable(token)
                .subscribe(new Action1<FreightInfos>() {
                    @Override
                    public void call(FreightInfos freightInfos) {
                        if (freightInfos == null) return;
//                        List<Detail> details = freightInfos.getDetails();
//                        for (Detail detail :
//                                details) {
//                            Log.d(TAG, "getBeidouMasterDeviceFreightListObservable() returned: " + detail.toString());
//                        }
                    }
                });
    }
}