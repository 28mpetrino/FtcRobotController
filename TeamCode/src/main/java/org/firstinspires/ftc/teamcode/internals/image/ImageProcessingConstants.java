package org.firstinspires.ftc.teamcode.internals.image;

import com.acmerobotics.dashboard.config.Config;

@Config
public class ImageProcessingConstants {
    public static int GRAY_MIN = 0;
    public static int GRAY_MAX = 255;

    public static double CROP_RATIO = 0.25;

    public static double RED_H_MIN = 0;
    public static double RED_S_MIN = 53;
    public static double RED_V_MIN = 73;

    public static double RED_H_MAX = 20;
    public static double RED_S_MAX = 255;
    public static double RED_V_MAX = 255;

    public static double BLUE_H_MIN = 100;
    public static double BLUE_S_MIN = 53;
    public static double BLUE_V_MIN = 73;

    public static double BLUE_H_MAX = 120;
    public static double BLUE_S_MAX = 255;
    public static double BLUE_V_MAX = 255;

    public static double GREEN_H_MIN = 40;
    public static double GREEN_S_MIN = 53;
    public static double GREEN_V_MIN = 73;

    public static double GREEN_H_MAX = 80;
    public static double GREEN_S_MAX = 255;
    public static double GREEN_V_MAX = 255;

    public static double GAUSSIAN_BLUR_SIZE = 5;

    public static boolean RETURN_GRAYSCALE = false;
}