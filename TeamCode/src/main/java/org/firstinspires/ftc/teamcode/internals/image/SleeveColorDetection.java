package org.firstinspires.ftc.teamcode.internals.image;

import org.firstinspires.ftc.teamcode.internals.hardware.Logging;
import org.opencv.core.*;
import org.opencv.features2d.SimpleBlobDetector;
import org.opencv.imgproc.Imgproc;
import org.openftc.easyopencv.OpenCvPipeline;

import static org.opencv.core.Core.*;
import static org.opencv.imgproc.Imgproc.*;

public class SleeveColorDetection extends OpenCvPipeline {
    Mat processedMat = new Mat();
    private int detection = 0;

    @Override
    public Mat processFrame(Mat input) {
        cvtColor(input, processedMat, Imgproc.COLOR_RGB2HSV);

        double cropRatio = ImageProcessingConstants.CROP_RATIO;

        // Crop part of the image to focus the center of the image
        Rect roi = new Rect(    new Point(processedMat.width()*cropRatio, processedMat.height()*cropRatio),
                                new Point(processedMat.width()*(1-cropRatio), processedMat.height()*(1-cropRatio)));

        processedMat = processedMat.submat(roi);


        // Define the colors that we want to detect
        Scalar redLowHSV = new Scalar(ImageProcessingConstants.RED_H_MIN, ImageProcessingConstants.RED_S_MIN, ImageProcessingConstants.RED_V_MIN);
        Scalar redHighHSV = new Scalar(ImageProcessingConstants.RED_H_MAX, ImageProcessingConstants.RED_S_MAX, ImageProcessingConstants.RED_V_MAX);

        Scalar blueLowHSV = new Scalar(ImageProcessingConstants.BLUE_H_MIN, ImageProcessingConstants.BLUE_S_MIN, ImageProcessingConstants.BLUE_V_MIN);
        Scalar blueHighHSV = new Scalar(ImageProcessingConstants.BLUE_H_MAX, ImageProcessingConstants.BLUE_S_MAX, ImageProcessingConstants.BLUE_V_MAX);

        Scalar greenLowHSV = new Scalar(ImageProcessingConstants.GREEN_H_MIN, ImageProcessingConstants.GREEN_S_MIN, ImageProcessingConstants.GREEN_V_MIN);
        Scalar greenHighHSV = new Scalar(ImageProcessingConstants.GREEN_H_MAX, ImageProcessingConstants.GREEN_S_MAX, ImageProcessingConstants.GREEN_V_MAX);


        // Convert to grayscale
        Mat grayScale = new Mat();
        cvtColor(processedMat, grayScale, Imgproc.COLOR_HSV2RGB); // There is no HSV to grayscale conversion, so we must convert to RGB first
        cvtColor(grayScale, grayScale, Imgproc.COLOR_RGB2GRAY);

        // Blur both the gray and color images to reduce noise
        Size blurSize = new Size(ImageProcessingConstants.GAUSSIAN_BLUR_SIZE, ImageProcessingConstants.GAUSSIAN_BLUR_SIZE);
        GaussianBlur(grayScale, grayScale, blurSize, 0);
        GaussianBlur(processedMat, processedMat, blurSize, 0);

        // Remove the background from the gray image and just get the cone to use as a mask
        inRange(grayScale, new Scalar(ImageProcessingConstants.GRAY_MIN), new Scalar(ImageProcessingConstants.GRAY_MAX), grayScale);
        // Set the GRAY_MIN and GRAY_MAX values to 0 and 255, respectively, to disable the feature

        // Use the grayscale image with the background removed to get rid of the background in the color image
        bitwise_and(processedMat, grayScale, processedMat);

        // Find the average colors of the original image
        double redAverage = processForColor(processedMat, redLowHSV, redHighHSV, "red");
        double blueAverage = processForColor(processedMat, blueLowHSV, blueHighHSV, "blue");
        double greenAverage = processForColor(processedMat, greenLowHSV, greenHighHSV, "green");

        double largestVal = Math.max(redAverage, Math.max(blueAverage, greenAverage));
        if (largestVal == redAverage) {
            Logging.logData("Sleeve Color", "Red");
            detection = 1;
        } else if (largestVal == blueAverage) {
            Logging.logData("Sleeve Color", "Blue");
            detection = 2;
        } else if (largestVal == greenAverage) {
            Logging.logData("Sleeve Color", "Green");
            detection = 3;
        } else {
            Logging.logData("Sleeve Color", "Unknown");
            detection = 0;
        }

        if (ImageProcessingConstants.RETURN_GRAYSCALE) {
            return grayScale; // Useful for tuning the background filtering
        } else {
            return processedMat; // Useful for previewing the final output
        }
    }

    public double processForColor(Mat input, Scalar lowHSV, Scalar highHSV, String name) {
        Mat mat = new Mat();
        inRange(input, lowHSV, highHSV, mat);

        double sum = sumElems(mat).val[0];
        double area = mat.rows() * mat.cols();

        double averageValue = sum / area / 255;

        // Log all values used in calculation for verification purposes
        Logging.logData(name+" - Average", averageValue * 100 + "%");
        Logging.updateLog();

        return averageValue;
    }

    public int getDetection() {
        return detection;
    }
}
