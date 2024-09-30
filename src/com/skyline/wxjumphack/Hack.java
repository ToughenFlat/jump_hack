package com.skyline.wxjumphack;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Objects;
import java.util.Random;

public class Hack {
    static final String ADB_PATH = "E:/Android/Sdk/platform-tools/adb";

    static final int SCREEN_WIDTH = 720;

    /**
     * 弹跳类型
     * 1 - 小程序
     * 2 - 三国闲话
     */
    static final int JUMP_TYPE = 1;

    /**
     * 弹跳系数
     */
    static final double JUMP_RATIO = 1.995f;        // 1.950f 1.765f
    static final double JUMP_RATIO2 = 1.995f;       // 1.970f 1.785f
    static final double JUMP_RATIO3 = 2.045f;       // 1.970f 1.785f
    static final double JUMP_RATIO4 = 2.165f;       // 2.125f

    private static final Random RANDOM = new Random();

    public static void main(String... strings) {
        String root = Objects.requireNonNull(Hack.class.getResource("/")).getPath();
        File srcDir = new File(root, "imgs/input");
        boolean createDirFlag = srcDir.mkdirs();
        if (!createDirFlag) {
            System.err.println("Failed to create dir: " + srcDir.getAbsolutePath());
            return;
        } else {
            System.out.println("Source Directory: " + srcDir.getAbsolutePath());
        }

        MyPosFinder myPosFinder = new MyPosFinder();
        NextCenterFinder nextCenterFinder = new NextCenterFinder();
        WhitePointFinder whitePointFinder = new WhitePointFinder();

        int total = 0;
        int centerHit = 0;
        double jumpRatio = 0;
        for (int i = 0; i < 5000; i++) {
            System.out.println("************第" + (i + 1) + "次************");
            try {
                total++;

                // 截图
                File file = new File(srcDir, i + ".png");
                if (file.exists()) {
                    file.deleteOnExit();

                }
                Process process = Runtime.getRuntime().exec(ADB_PATH + " shell /system/bin/screencap -p /sdcard/screenshot.png");
                process.waitFor();
                process = Runtime.getRuntime().exec(ADB_PATH + " pull /sdcard/screenshot.png " + file.getAbsolutePath());
                process.waitFor();
                System.out.println("Screenshot file path: " + file.getAbsolutePath());

                // 加载图片, 设定弹跳比率
                BufferedImage image = ImgLoader.load(file.getAbsolutePath());
                if (jumpRatio == 0) {
                    jumpRatio = JUMP_RATIO * SCREEN_WIDTH / image.getWidth();
                }
                if (JUMP_TYPE == 1) {
                    if (i % 30 == 0) {
                        jumpRatio += 0.01;
                    }
                } else if (JUMP_TYPE == 2) {
                    if (i > 50) {
                        jumpRatio = JUMP_RATIO * SCREEN_WIDTH / image.getWidth();
                    }
                }

                // 位置识别
                int[] myPos = myPosFinder.find(image);
                if (myPos != null && myPos[0] != 0 && myPos[1] != 0) {
                    System.out.println("人物中心位置: (" + myPos[0] + ", " + myPos[1] + ")");

                    int[] nextCenter = null;
                    if (JUMP_TYPE == 1) {
                        nextCenter = nextCenterFinder.find(image, myPos);
                    } else if (JUMP_TYPE == 2) {
                        nextCenter = nextCenterFinder.findNextCenterCoordinate(image, myPos);
                    }
                    if (nextCenter == null || nextCenter[0] == 0) {
                        System.err.println("Failed to find the nextCenter");
                        break;
                    } else {
                        int centerX, centerY;
                        if (JUMP_TYPE == 1) {
                            int[] whitePoint = whitePointFinder.find(image, nextCenter[0] - 120, nextCenter[1], nextCenter[0] + 120, nextCenter[1] + 180);
                            if (whitePoint != null) {
                                centerX = whitePoint[0];
                                centerY = whitePoint[1];
                                centerHit++;
                                System.out.println("Successfully find whitePoint: (" + centerX + ", " + centerY + "), centerHit: " + centerHit + ", total: " + total);
                            } else {
                                if (nextCenter[2] != Integer.MAX_VALUE && nextCenter[4] != Integer.MIN_VALUE) {
                                    centerX = (nextCenter[2] + nextCenter[4]) / 2;
                                    centerY = (nextCenter[3] + nextCenter[5]) / 2;
                                } else {
                                    centerX = nextCenter[0];
                                    centerY = nextCenter[1] + 48;
                                }
                            }
                        } else if (JUMP_TYPE == 2) {
                            centerX = nextCenter[0];
                            centerY = nextCenter[1];
                        }
                        System.out.println("下个物体中心位置: (" + centerX + ", " + centerY + ")");

                        // 计算弹跳距离
                        int distance = (int) (Math.sqrt((centerX - myPos[0]) * (centerX - myPos[0]) + (centerY - myPos[1]) * (centerY - myPos[1])) * jumpRatio);
                        int pressX = 400 + RANDOM.nextInt(100);
                        int pressY = 500 + RANDOM.nextInt(100);

                        // 执行弹跳
                        String adbCommand = ADB_PATH + String.format(" shell input swipe %d %d %d %d %d", pressX, pressY, pressX, pressY, distance);
                        System.out.println(adbCommand);
                        Runtime.getRuntime().exec(adbCommand);
                    }
                } else {
                    System.err.println("Failed to find myPos");
                    break;
                }
            } catch (Exception e) {
                e.printStackTrace();
                break;
            }
            try {
                // sleep 随机时间, 防止上传不了成绩
                Thread.sleep(4_000 + RANDOM.nextInt(3000));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("Hit center count: " + centerHit);
        System.out.println("Total count: " + total);
    }

}
