package com.agenthun.eseal.utils;

import java.util.Locale;

/**
 * @project ESeal
 * @authors agenthun
 * @date 2016/10/31 12:26.
 */

public class LanguageUtil {
    private LanguageUtil() {
    }

    public static String getLanguage() {
        String lan = Locale.getDefault().getLanguage();
        if (lan.contains("zh")) {
            return "zh-CN";
        } else if (lan.contains("en")) {
            return "en-US";
        } else {
            return "en-US";
        }
    }
}
