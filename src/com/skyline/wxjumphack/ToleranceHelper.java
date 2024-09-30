package com.skyline.wxjumphack;

import java.awt.*;
import java.util.List;

public class ToleranceHelper {
    public static final int SHADOW_RED = 141;
    public static final int SHADOW_GREEN = 143;
    public static final int SHADOW_BLUE = 150;

    /**
     * 判断给定的RGB颜色值是否在目标颜色值的容差范围内
     *
     * @param r 红色分量值，范围0-255
     * @param g 绿色分量值，范围0-255
     * @param b 蓝色分量值，范围0-255
     * @param rt 目标颜色的红色分量值
     * @param gt 目标颜色的绿色分量值
     * @param bt 目标颜色的蓝色分量值
     * @param t 颜色值的容差，表示允许的颜色值偏差范围
     * @return 如果给定的RGB颜色值在目标颜色值的容差范围内，则返回true；否则返回false
     */
    public static boolean match(int r, int g, int b, int rt, int gt, int bt, int t) {
        return r > rt - t &&
                r < rt + t &&
                g > gt - t &&
                g < gt + t &&
                b > bt - t &&
                b < bt + t;
    }

    /**
     * 检查指定像素是否在给定的像素列表的范围内
     *
     * @param pixelList 像素列表，代表一系列颜色
     * @param pixel 待检查的像素值
     * @return 如果给定像素与列表中任意像素的RGB值相差超过阈值，则返回true；否则返回false
     */
    public static boolean isPixelInRange(List<Integer> pixelList, int pixel) {
        Color color = new Color(pixel);
        int red = color.getRed();
        int green = color.getGreen();
        int blue = color.getBlue();
        for (Integer targetPixel : pixelList) {
            Color targetColor = new Color(targetPixel);
            int redTarget = targetColor.getRed();
            int greenTarget = targetColor.getGreen();
            int blueTarget = targetColor.getBlue();
            if (match(red, green, blue, redTarget, greenTarget, blueTarget, 8)
                    || match(red, green, blue, SHADOW_RED, SHADOW_GREEN, SHADOW_BLUE, 8)) {
                return true;
            }
        }
        return false;
    }
}
