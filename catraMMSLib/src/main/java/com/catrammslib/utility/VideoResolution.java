package com.catrammslib.utility;

public class VideoResolution {
    static public String getStandartDefinition(int width, int height) {
        if (width == 320 && height == 200)
            return "CGA 16:10";
        else if (width == 320 && height == 240)
            return "QVGA 4:3";
        else if (width == 640 && height == 480)
            return "VGA 4:3";
        else if (width == 768 && height == 576)
            return "PAL";
        else if (width == 800 && height == 480)
            return "WVGA";
        else if (width == 800 && height == 600)
            return "SVGA 4:3";
        else if (width == 854 && height == 480)
            return "FWVGA";
        else if (width == 1024 && height == 600)
            return "WSVGA";
        else if (width == 1024 && height == 768)
            return "XGA 4:3";
        else if (width == 1280 && height == 720)
            return "HD 720 16:9";
        else if (width == 1280 && height == 768)
            return "WXGA";
        else if (width == 1280 && height == 800)
            return "WXGA 16:9";
        else if (width == 1280 && height == 1024)
            return "SXGA";
        else if (width == 1400 && height == 1050)
            return "SXGA+ 4:3";
        else if (width == 1600 && height == 1200)
            return "UXGA 4:3";
        else if (width == 1680 && height == 1050)
            return "WSXGA+ 16:10";
        else if (width == 1920 && height == 1080)
            return "HD 1080 16:9";
        else if (width == 1920 && height == 1200)
            return "WUXGA 16:10";
        else if (width == 2048 && height == 1536)
            return "QXGA 4:3";
        else if (width == 2048 && height == 1080)
            return "2K 1,8962:1";
        else if (width == 2560 && height == 1600)
            return "WQXGA 16:10";
        else if (width == 2560 && height == 2048)
            return "QSXGA 5:4";
        else if (width == 3840 && height == 2160)
            return "SHD";
        else if (width == 4096 && height == 2160)
            return "4K 1,8962:1";
        else if (width == 7680 && height == 4320)
            return "8K 16:9";
        else
            return "";
    }
}
