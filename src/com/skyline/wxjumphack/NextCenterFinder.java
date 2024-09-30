package com.skyline.wxjumphack;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class NextCenterFinder {

    BottleFinder bottleFinder = new BottleFinder();

    /**
     * 在图像中查找下一个中心点坐标
     *
     * @param image 图像对象，代表需要搜索的图像
     * @param myPos 当前个人的位置，格式为[x, y]，用于确定搜索的上界
     * @return int[] 包含下一个中心点坐标的数组，格式为[x, y]；如果未找到，则返回null
     */
    public int[] findNextCenterCoordinate(BufferedImage image, int[] myPos) {
        if (image == null) {
            return null;
        }

        int width = image.getWidth();
        int height = image.getHeight();
        int pixel;

        int[] ret = new int[2];
        for (int j = height / 4; j < myPos[1]; j++) {
            for (int i = 0; i < width; i++) {
                pixel = image.getRGB(i, j);
                Color color = new Color(pixel);
                int r = color.getRed();
                int g = color.getGreen();
                int b = color.getBlue();

                if (ToleranceHelper.match(r, g, b, 239, 239, 239, 5)) {
                    System.out.println("The color value of the next center point: (" + r + ", " + g + ", " + b + ")");
                    int difference = Math.abs(myPos[0] - i);
                    if (difference < 60) {
                        continue;
                    }
                    ret[0] = i;
                    ret[1] = j;
                    return ret;
                }
            }

        }

        return ret;
    }

    public int[] find(BufferedImage image, int[] myPos) {
        if (image == null) {
            return null;
        }

        int width = image.getWidth();
        int height = image.getHeight();

        // 物体颜色列表
        List<Integer> itemColorList = null;

        // 获取背景像素值
        Set<Integer> pixelSet = new HashSet<>();
        for (int i = 1; i < 5; i++) {
            for (int j = 1; j < myPos[1]; j++) {
                int pixel = image.getRGB(i, j);
                pixelSet.add(pixel);
            }
        }
        List<Integer> pixelList = new ArrayList<>(pixelSet);

        int[] ret = new int[6];

        // 获取下个物体顶点的颜色值
        boolean found = false;
        int targetR = 0, targetG = 0, targetB = 0;
        int pixel;
        for (int j = height / 4; j < myPos[1]; j++) {
            for (int i = 1; i < width; i++) {
                pixel = image.getRGB(i, j);
                Color color = new Color(pixel);
                int r = color.getRed();
                int g = color.getRed();
                int b = color.getBlue();
                if (!ToleranceHelper.isPixelInRange(pixelList, pixel)) {
                    if (Math.abs(myPos[0] - i) < 100) {
                        continue;
                    }
                    System.out.println("Vertex position: (x: " + i + ", y: " + j + ")");
                    System.out.println("Vertex color: (r: " + r + ", g: " + g + ", b: " + b + ")");
                    j = j + 2;
                    itemColorList = new ArrayList<>();
                    itemColorList.add(pixel);
                    for (int k = 0; k < 50; k++) {
                        int pixelTmp = image.getRGB(i, j + k);
                        itemColorList.add(pixelTmp);
                    }

                    targetR = r;
                    targetG = g;
                    targetB = b;
                    ret[0] = i;
                    ret[1] = j;
                    found = true;
                    break;
                }
            }
            if (found) {
                break;
            }
        }
        if (itemColorList == null || itemColorList.size() == 0) {
            return ret;
        }

        // 确定是否为瓶子
        if (targetR == BottleFinder.TARGET && targetG == BottleFinder.TARGET && targetB == BottleFinder.TARGET) {
            return bottleFinder.find(image, ret[0], ret[1]);
        }

        // 获取下个物体的左右边界
        boolean[][] matchMap = new boolean[width][height];
        boolean[][] vMap = new boolean[width][height];
        ret[2] = Integer.MAX_VALUE;
        ret[3] = Integer.MAX_VALUE;
        ret[4] = Integer.MIN_VALUE;
        ret[5] = Integer.MAX_VALUE;

        Queue<int[]> queue = new ArrayDeque<>();
        queue.add(ret);
        while (!queue.isEmpty()) {
            int[] item = queue.poll();
            int i = item[0];
            int j = item[1];

            if (j >= myPos[1]) {
                continue;
            }

            if (i < Integer.max(ret[0] - 120, 0) || i >= Integer.min(ret[0] + 120, width) || j < Integer.max(0, ret[1] - 150) || j >= Integer.max(height, ret[1] + 150) || vMap[i][j]) {
                continue;
            }
            vMap[i][j] = true;
            pixel = image.getRGB(i, j);
            matchMap[i][j] = !ToleranceHelper.isPixelInRange(pixelList, pixel) && ToleranceHelper.isPixelInRange(itemColorList, pixel);

            if (matchMap[i][j]) {
                if (i < ret[2]) {
                    ret[2] = i;
                    ret[3] = j;
                } else if (i == ret[2] && j < ret[3]) {
                    ret[3] = j;
                }
                if (i > ret[4]) {
                    ret[4] = i;
                    ret[5] = j;
                } else if (i == ret[4] && j < ret[5]) {
                    ret[5] = j;
                }
                if (j < ret[1]) {
                    ret[0] = i;
                    ret[1] = j;
                }
                queue.add(buildArray(i - 1, j));
                queue.add(buildArray(i + 1, j));
                queue.add(buildArray(i, j - 1));
                queue.add(buildArray(i, j + 1));
            }
        }

        System.out.println("Left boundary: (x" + ret[2] + ", y: " + ret[3] + ")");
        System.out.println("Right boundary: (x" + ret[4] + ", y: " + ret[5] + ")");

        return ret;
    }

    public static int[] buildArray(int i, int j) {
        return new int[]{i, j};
    }

    public static void main(String... strings) throws IOException {
        //  int[] excepted = {0, 0};
        NextCenterFinder t = new NextCenterFinder();
        String root = t.getClass().getResource("/").getPath();
        System.out.println("root: " + root);
        String imgsSrc = root + "imgs/src";
        String imgsDesc = root + "imgs/next_center";
        File srcDir = new File(imgsSrc);
        System.out.println(srcDir);
        MyPosFinder myPosFinder = new MyPosFinder();
        long cost = 0;
        for (File file : srcDir.listFiles()) {
            System.out.println(file);
            BufferedImage img = ImgLoader.load(file.getAbsolutePath());
            long t1 = System.nanoTime();
            int[] myPos = myPosFinder.find(img);
            int[] pos = t.find(img, myPos);
            long t2 = System.nanoTime();
            cost += (t2 - t1);
            BufferedImage desc = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_RGB);
            Graphics g = desc.getGraphics();
            g.drawImage(img, 0, 0, img.getWidth(), img.getHeight(), null);
            g.setColor(Color.RED);
            g.fillRect(pos[0] - 5, pos[1] - 5, 10, 10);
            g.fillRect(pos[2] - 5, pos[3] - 5, 10, 10);
            g.fillRect(pos[4] - 5, pos[5] - 5, 10, 10);
            if (pos[2] != Integer.MAX_VALUE && pos[4] != Integer.MIN_VALUE) {
                g.fillRect((pos[2] + pos[4]) / 2 - 5, (pos[3] + pos[5]) / 2 - 5, 10, 10);
            } else {
                g.fillRect(pos[0], pos[1] + 36, 10, 10);
            }
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
