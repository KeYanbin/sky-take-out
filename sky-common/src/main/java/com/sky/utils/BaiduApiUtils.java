package com.sky.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sky.exception.OrderBusinessException;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * 百度地图操作工具类
 */
@Slf4j
public class BaiduApiUtils {


    private static String COORDINATE_URL = "https://api.map.baidu.com/geocoding/v3";

    private static String DRIVING_URL = "https://api.map.baidu.com/directionlite/v1/driving";

    /**
     * 调用百度地图地理编码服务接口，根据地址获取坐标（经度、纬度）
     *
     * @param address 地址
     * @param AK      百度AK
     * @return
     */
    public static String getCoordinate(String address, String AK) {
        log.info("请求获取{}的经纬度坐标", address);
        Map map = new HashMap();
        map.put("address", address);
        map.put("output", "json");
        map.put("ak", AK);

        //获取店铺的经纬度坐标
        String shopCoordinate = HttpClientUtil.doGet(COORDINATE_URL, map);

        JSONObject jsonObject = JSON.parseObject(shopCoordinate);
        if (!jsonObject.getString("status").equals("0")) {
            throw new OrderBusinessException("地址解析失败");
        }
        //数据解析
        JSONObject location = jsonObject.getJSONObject("result").getJSONObject("location");
        String lat = location.getString("lat");
        String lng = location.getString("lng");
        //店铺经纬度坐标
        return lat + "," + lng;
    }

    /**
     * 调用百度地图驾车路线规划服务接口，根据商家地址和收件人地址坐标计算订单距离
     *
     * @param destination
     * @param AK
     * @return
     */
    public static Integer getDistance(String shopAddress, String destination, String AK) {
        log.info("计算 距离");
        Map map = new HashMap();
        map.put("origin", getCoordinate(shopAddress, AK));
        map.put("destination", destination);
        map.put("ak", AK);
        //路线规划
        String json = HttpClientUtil.doGet(DRIVING_URL, map);

        JSONObject jsonObject = JSON.parseObject(json);
        if (!jsonObject.getString("status").equals("0")) {
            throw new OrderBusinessException("配送路线规划失败");
        }

        //数据解析
        JSONObject result = jsonObject.getJSONObject("result");
        JSONArray jsonArray = (JSONArray) result.get("routes");
        // 获取距离
        Integer distance = (Integer) ((JSONObject) jsonArray.get(0)).get("distance");

        return distance;

    }


}
