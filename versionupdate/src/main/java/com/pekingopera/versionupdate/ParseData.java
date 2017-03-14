package com.pekingopera.versionupdate;

/**
 * ========================================
 * 
 * 版 权：dou361.com 版权所有 （C） 2015
 * 
 * 作 者：陈冠明
 * 
 * 个人网站：http://www.dou361.com
 * 
 * 版 本：1.0
 * 
 * 创建日期：2016/6/15 18:08
 * 
 * 描 述：数据解析
 * 
 * 
 * 修订历史：
 * 
 * ========================================
 */
public interface ParseData {
    <T> T parse(String httpResponse);
}
