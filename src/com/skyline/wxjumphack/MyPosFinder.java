package com.skyline.wxjumphack;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class MyPosFinder {
    public static final int R_TARGET = 54;          // 54 26

    public static final int G_TARGET = 60;          // 60 28

    public static final int B_TARGET = 102;          // 102 26

    public int[] find(BufferedImage image) {
        if (image == null) {
            return null;
        }
        int width = image.getWidth();
        int height = image.getHeight();

        int[] ret = {0, 0};
        for (int i = 0; i < width; i++) {
            for (int j = height / 4; j < height * 3 / 4; j++) {
                int pixel = image.getRGB(i, j);
                Color color = new Color(pixel);
                int r = color.getRed();
                int g = color.getGreen();
                int b = color.getBlue();
                if (ToleranceHelper.match(r, g, b, R_TARGET, G_TARGET, B_TARGET, 3)) {
                    if (isSurroundingMatch(image, i, j)) {
                        // 如果符合条件，直接返回当前坐标
                        if (j > ret[1]) {
                            ret[0] = i;
                            ret[1] = j;
                        }
                    }
                }
            }
        }
        return ret;
    }

    private boolean isSurroundingMatch(BufferedImage image, int i, int j) {
        int upPixel = image.getRGB(i, j - 1);
        int downPixel = image.getRGB(i, j + 1);
        int leftPixel = image.getRGB(i - 1, j);
        int rightPixel = image.getRGB(i + 1, j);
        Color upColor = new Color(upPixel);
        Color downColor = new Color(downPixel);
        Color leftColor = new Color(leftPixel);
        Color rightColor = new Color(rightPixel);
        return ToleranceHelper.match(upColor.getRed(), upColor.getGreen(), upColor.getBlue(), R_TARGET, G_TARGET, B_TARGET, 3)
                && ToleranceHelper.match(downColor.getRed(), downColor.getGreen(), downColor.getBlue(), R_TARGET, G_TARGET, B_TARGET, 3)
                && ToleranceHelper.match(leftColor.getRed(), leftColor.getGreen(), leftColor.getBlue(), R_TARGET, G_TARGET, B_TARGET, 3)
                && ToleranceHelper.match(rightColor.getRed(), rightColor.getGreen(), rightColor.getBlue(), R_TARGET, G_TARGET, B_TARGET, 3);
    }

    public static void main(String... strings) throws IOException {
        MyPosFinder t = new MyPosFinder();
        String root = Objects.requireNonNull(t.getClass().getResource("/")).getPath();
        System.out.println("root: " + root);
        String imgsSrc = root + "imgs/src";
        String imgsDesc = root + "imgs/my_pos";
        File srcDir = new File(imgsSrc);
        System.out.println(srcDir);
        long cost = 0;
        for (File file : Objects.requireNonNull(srcDir.listFiles())) {
            if (!file.getName().endsWith(".png")) {
                continue;
            }
            System.out.println(file);
            BufferedImage img = ImgLoader.load(file.getAbsolutePath());
            long t1 = System.nanoTime();
            int[] pos = t.find(img);
            long t2 = System.nanoTime();
            cost += (t2 - t1);
            BufferedImage desc = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_RGB);
            desc.getGraphics().drawImage(img, 0, 0, img.getWidth(), img.getHeight(), null); // 绘制缩小后的图
            desc.getGraphics().drawRect(pos[0] - 5, pos[1] - 5, 10, 10);
            File descFile = new File(imgsDesc, file.getName());
            if (!descFile.exists()) {
                descFile.mkdirs();
                descFile.createNewFile();
            }
            ImageIO.write(desc, "png", descFile);
        }
        System.out.println("avg time cost: " + (cost / srcDir.listFiles().length / 1_000_000));

    }
}
