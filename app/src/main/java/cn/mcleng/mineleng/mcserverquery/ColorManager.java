package cn.mcleng.mineleng.mcserverquery;

import android.app.WallpaperColors;
import android.app.WallpaperManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;

import org.json.JSONObject;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

public class ColorManager {

    private static final String COLOR_FILE = "color.json";
    private static final String DEFAULT_BUTTON = "0078D4";
    private static final String DEFAULT_BACKGROUND = "FFFFFF";
    private static final String DEFAULT_CARD = "F5F5F5";
    private static final String DEFAULT_TEXT = "212121";
    private static final String DEFAULT_SLIDER = "0078D4";
    private static final int DEFAULT_INPUT_RADIUS = 15;
    private static final int DEFAULT_CARD_RADIUS = 15;
    private static final int DEFAULT_BUTTON_RADIUS = 50;

    private boolean dynamicColor;
    private int buttonColor;
    private int backgroundColor;
    private int cardColor;
    private int textColor;
    private int sliderColor;
    private int inputCornerRadius;
    private int cardCornerRadius;
    private int buttonCornerRadius;

    public ColorManager(Context context) {
        load(context);
    }

    public boolean isDynamicColor() {
        return dynamicColor;
    }

    public void setDynamicColor(boolean dynamic) {
        this.dynamicColor = dynamic;
    }

    public int getButtonColor() {
        return buttonColor;
    }

    public void setButtonColor(int color) {
        this.buttonColor = color;
    }

    public int getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(int color) {
        this.backgroundColor = color;
    }

    public int getCardColor() {
        return cardColor;
    }

    public void setCardColor(int color) {
        this.cardColor = color;
    }

    public int getTextColor() {
        return textColor;
    }

    public void setTextColor(int color) {
        this.textColor = color;
    }

    public int getSliderColor() {
        return sliderColor;
    }

    public void setSliderColor(int color) {
        this.sliderColor = color;
    }

    public int getInputCornerRadius() {
        return inputCornerRadius;
    }

    public void setInputCornerRadius(int radius) {
        this.inputCornerRadius = radius;
    }

    public int getCardCornerRadius() {
        return cardCornerRadius;
    }

    public void setCardCornerRadius(int radius) {
        this.cardCornerRadius = radius;
    }

    public int getButtonCornerRadius() {
        return buttonCornerRadius;
    }

    public void setButtonCornerRadius(int radius) {
        this.buttonCornerRadius = radius;
    }

    public String getButtonColorHex() {
        return String.format("%06X", buttonColor & 0xFFFFFF);
    }

    public String getBackgroundColorHex() {
        return String.format("%06X", backgroundColor & 0xFFFFFF);
    }

    public String getCardColorHex() {
        return String.format("%06X", cardColor & 0xFFFFFF);
    }

    public String getTextColorHex() {
        return String.format("%06X", textColor & 0xFFFFFF);
    }

    public String getSliderColorHex() {
        return String.format("%06X", sliderColor & 0xFFFFFF);
    }

    public void load(Context context) {
        try {
            File file = new File(context.getFilesDir(), COLOR_FILE);
            if (file.exists()) {
                FileReader reader = new FileReader(file);
                StringBuilder sb = new StringBuilder();
                int c;
                while ((c = reader.read()) != -1) {
                    sb.append((char) c);
                }
                reader.close();

                JSONObject json = new JSONObject(sb.toString());
                dynamicColor = json.optBoolean("dynamic_color", false);
                buttonColor = Color.parseColor("#" + json.optString("button_color", DEFAULT_BUTTON));
                backgroundColor = Color.parseColor("#" + json.optString("background_color", DEFAULT_BACKGROUND));
                cardColor = Color.parseColor("#" + json.optString("card_color", DEFAULT_CARD));
                textColor = Color.parseColor("#" + json.optString("text_color", DEFAULT_TEXT));
                sliderColor = Color.parseColor("#" + json.optString("slider_color", DEFAULT_SLIDER));
                inputCornerRadius = json.optInt("input_corner_radius", DEFAULT_INPUT_RADIUS);
                cardCornerRadius = json.optInt("card_corner_radius", DEFAULT_CARD_RADIUS);
                buttonCornerRadius = json.optInt("button_corner_radius", DEFAULT_BUTTON_RADIUS);

                if (dynamicColor && !supportsDynamicColor(context)) {
                    dynamicColor = false;
                    save(context);
                }
            } else {
                resetToDefaults();
                save(context);
            }
        } catch (Exception e) {
            resetToDefaults();
        }
    }

    private void resetToDefaults() {
        dynamicColor = false;
        buttonColor = Color.parseColor("#" + DEFAULT_BUTTON);
        backgroundColor = Color.parseColor("#" + DEFAULT_BACKGROUND);
        cardColor = Color.parseColor("#" + DEFAULT_CARD);
        textColor = Color.parseColor("#" + DEFAULT_TEXT);
        sliderColor = Color.parseColor("#" + DEFAULT_SLIDER);
        inputCornerRadius = DEFAULT_INPUT_RADIUS;
        cardCornerRadius = DEFAULT_CARD_RADIUS;
        buttonCornerRadius = DEFAULT_BUTTON_RADIUS;
    }

    public void save(Context context) {
        try {
            File file = new File(context.getFilesDir(), COLOR_FILE);
            JSONObject json = new JSONObject();
            json.put("dynamic_color", dynamicColor);
            json.put("button_color", getButtonColorHex());
            json.put("background_color", getBackgroundColorHex());
            json.put("card_color", getCardColorHex());
            json.put("text_color", getTextColorHex());
            json.put("slider_color", getSliderColorHex());
            json.put("input_corner_radius", inputCornerRadius);
            json.put("card_corner_radius", cardCornerRadius);
            json.put("button_corner_radius", buttonCornerRadius);

            FileWriter writer = new FileWriter(file);
            writer.write(json.toString());
            writer.close();
        } catch (Exception e) {
            DebugLog.error("ColorManager", "Save failed", e);
        }
    }

    public static boolean supportsDynamicColor(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O_MR1) {
            return false;
        }
        try {
            WallpaperManager wm = WallpaperManager.getInstance(context);
            WallpaperColors colors = wm.getWallpaperColors(WallpaperManager.FLAG_SYSTEM);
            return colors != null;
        } catch (Exception e) {
            return false;
        }
    }

    public void applyDynamicColors(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O_MR1) {
            return;
        }
        try {
            WallpaperManager wm = WallpaperManager.getInstance(context);
            WallpaperColors wallpaperColors = wm.getWallpaperColors(WallpaperManager.FLAG_SYSTEM);
            if (wallpaperColors != null) {
                int primary = wallpaperColors.getPrimaryColor().toArgb();
                buttonColor = primary;

                float[] hsl = new float[3];
                Color.colorToHSV(primary, hsl);

                hsl[1] = 0.05f;
                hsl[2] = 0.98f;
                backgroundColor = Color.HSVToColor(hsl);

                hsl[1] = 0.08f;
                hsl[2] = 0.95f;
                cardColor = Color.HSVToColor(hsl);

                hsl[1] = 0.15f;
                hsl[2] = 0.15f;
                textColor = Color.HSVToColor(hsl);
            }
        } catch (Exception e) {
            DebugLog.error("ColorManager", "Apply dynamic colors failed", e);
        }
    }
}
