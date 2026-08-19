package cn.mcleng.mineleng.mcserverquery;

import android.app.Dialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.SeekBar;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.textfield.TextInputEditText;

public class ColorPickerDialog {

    public interface OnColorSelectedListener {
        void onColorSelected(int buttonColor, int backgroundColor, int cardColor, int textColor, int sliderColor, int inputRadius, int cardRadius, int buttonRadius, boolean dynamicColor);
    }

    private final Context context;
    private int buttonColor;
    private int backgroundColor;
    private int cardColor;
    private int textColor;
    private int sliderColor;
    private int inputCornerRadius;
    private int cardCornerRadius;
    private int buttonCornerRadius;
    private boolean dynamicColor;
    private boolean dynamicSupported;
    private OnColorSelectedListener listener;

    private int currentEditingType = 0;
    private int[] editingColor = new int[5];

    private View colorPreview;
    private SeekBar redSeek;
    private SeekBar greenSeek;
    private SeekBar blueSeek;
    private TextInputEditText hexInput;
    private MaterialSwitch dynamicSwitch;
    private SeekBar radiusSeek;
    private TextInputEditText radiusValue;
    private SeekBar cardRadiusSeek;
    private TextInputEditText cardRadiusValue;
    private SeekBar buttonRadiusSeek;
    private TextInputEditText buttonRadiusValue;

    private MaterialButton btnButton;
    private MaterialButton btnBackground;
    private MaterialButton btnCard;
    private MaterialButton btnText;
    private MaterialButton btnSlider;

    public ColorPickerDialog(Context context, int buttonColor, int backgroundColor, int cardColor, int textColor, int sliderColor, int inputRadius, int cardRadius, int buttonRadius, boolean dynamicColor) {
        this.context = context;
        this.buttonColor = buttonColor;
        this.backgroundColor = backgroundColor;
        this.cardColor = cardColor;
        this.textColor = textColor;
        this.sliderColor = sliderColor;
        this.inputCornerRadius = inputRadius;
        this.cardCornerRadius = cardRadius;
        this.buttonCornerRadius = buttonRadius;
        this.dynamicColor = dynamicColor;
        this.dynamicSupported = ColorManager.supportsDynamicColor(context);

        editingColor[0] = buttonColor;
        editingColor[1] = backgroundColor;
        editingColor[2] = cardColor;
        editingColor[3] = textColor;
        editingColor[4] = sliderColor;
    }

    public void setOnColorSelectedListener(OnColorSelectedListener listener) {
        this.listener = listener;
    }

    public void show() {
        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_color_picker);

        colorPreview = dialog.findViewById(R.id.color_preview);
        redSeek = dialog.findViewById(R.id.seek_red);
        greenSeek = dialog.findViewById(R.id.seek_green);
        blueSeek = dialog.findViewById(R.id.seek_blue);
        hexInput = dialog.findViewById(R.id.hex_input);
        dynamicSwitch = dialog.findViewById(R.id.switch_dynamic);
        radiusSeek = dialog.findViewById(R.id.seek_radius);
        radiusValue = dialog.findViewById(R.id.radius_value);
        cardRadiusSeek = dialog.findViewById(R.id.seek_card_radius);
        cardRadiusValue = dialog.findViewById(R.id.card_radius_value);
        buttonRadiusSeek = dialog.findViewById(R.id.seek_button_radius);
        buttonRadiusValue = dialog.findViewById(R.id.button_radius_value);
        MaterialButton confirmBtn = dialog.findViewById(R.id.btn_confirm);
        MaterialButton cancelBtn = dialog.findViewById(R.id.btn_cancel);

        btnButton = dialog.findViewById(R.id.btn_type_button);
        btnBackground = dialog.findViewById(R.id.btn_type_background);
        btnCard = dialog.findViewById(R.id.btn_type_card);
        btnText = dialog.findViewById(R.id.btn_type_text);
        btnSlider = dialog.findViewById(R.id.btn_type_slider);

        redSeek.setMax(255);
        greenSeek.setMax(255);
        blueSeek.setMax(255);
        radiusSeek.setMax(50);
        cardRadiusSeek.setMax(50);
        buttonRadiusSeek.setMax(50);

        radiusSeek.setProgress(Math.min(inputCornerRadius, 50));
        radiusValue.setText(String.valueOf(inputCornerRadius));
        cardRadiusSeek.setProgress(Math.min(cardCornerRadius, 50));
        cardRadiusValue.setText(String.valueOf(cardCornerRadius));
        buttonRadiusSeek.setProgress(Math.min(buttonCornerRadius, 50));
        buttonRadiusValue.setText(String.valueOf(buttonCornerRadius));

        styleTypeButtons();
        applySliderColor();
        loadColorToUI(editingColor[0]);
        highlightTypeButton(0);

        confirmBtn.setBackgroundTintList(ColorStateList.valueOf(buttonColor));
        confirmBtn.setTextColor(getContrastColor(buttonColor));
        cancelBtn.setTextColor(buttonColor);

        hexInput.setHintTextColor(ColorStateList.valueOf(buttonColor));

        SeekBar.OnSeekBarChangeListener seekListener = new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    int color = Color.rgb(redSeek.getProgress(), greenSeek.getProgress(), blueSeek.getProgress());
                    editingColor[currentEditingType] = color;
                    updatePreview();
                    updateHexInput();
                    if (dynamicSwitch.isChecked()) {
                        dynamicSwitch.setChecked(false);
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        };

        redSeek.setOnSeekBarChangeListener(seekListener);
        greenSeek.setOnSeekBarChangeListener(seekListener);
        blueSeek.setOnSeekBarChangeListener(seekListener);

        setupRadiusInput(radiusSeek, radiusValue, v -> inputCornerRadius = v);
        setupRadiusInput(cardRadiusSeek, cardRadiusValue, v -> cardCornerRadius = v);
        setupRadiusInput(buttonRadiusSeek, buttonRadiusValue, v -> buttonCornerRadius = v);

        hexInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String hex = s.toString().replace("#", "").trim();
                if (hex.length() == 6) {
                    try {
                        int color = Color.parseColor("#" + hex);
                        editingColor[currentEditingType] = color;
                        redSeek.setProgress(Color.red(color));
                        greenSeek.setProgress(Color.green(color));
                        blueSeek.setProgress(Color.blue(color));
                        updatePreview();
                        if (dynamicSwitch.isChecked()) {
                            dynamicSwitch.setChecked(false);
                        }
                    } catch (IllegalArgumentException ignored) {}
                }
            }
        });

        dynamicSwitch.setChecked(dynamicColor && dynamicSupported);
        dynamicSwitch.setEnabled(dynamicSupported);
        if (!dynamicSupported) {
            dynamicSwitch.setText(R.string.dynamic_color_unsupported);
        }

        dynamicSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            boolean disabled = isChecked;
            redSeek.setEnabled(!disabled);
            greenSeek.setEnabled(!disabled);
            blueSeek.setEnabled(!disabled);
            hexInput.setEnabled(!disabled);
            btnButton.setEnabled(!disabled);
            btnBackground.setEnabled(!disabled);
            btnCard.setEnabled(!disabled);
            btnText.setEnabled(!disabled);
            btnSlider.setEnabled(!disabled);
        });

        btnButton.setOnClickListener(v -> { currentEditingType = 0; loadColorToUI(editingColor[0]); highlightTypeButton(0); });
        btnBackground.setOnClickListener(v -> { currentEditingType = 1; loadColorToUI(editingColor[1]); highlightTypeButton(1); });
        btnCard.setOnClickListener(v -> { currentEditingType = 2; loadColorToUI(editingColor[2]); highlightTypeButton(2); });
        btnText.setOnClickListener(v -> { currentEditingType = 3; loadColorToUI(editingColor[3]); highlightTypeButton(3); });
        btnSlider.setOnClickListener(v -> { currentEditingType = 4; loadColorToUI(editingColor[4]); highlightTypeButton(4); });

        confirmBtn.setOnClickListener(v -> {
            if (listener != null) {
                listener.onColorSelected(
                        editingColor[0], editingColor[1], editingColor[2],
                        editingColor[3], editingColor[4],
                        inputCornerRadius, cardCornerRadius, buttonCornerRadius,
                        dynamicSwitch.isChecked() && dynamicSupported
                );
            }
            dialog.dismiss();
        });

        cancelBtn.setOnClickListener(v -> dialog.dismiss());

        Window window = dialog.getWindow();
        if (window != null) {
            window.setLayout(
                    (int) (context.getResources().getDisplayMetrics().widthPixels * 0.92),
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            window.setBackgroundDrawable(new GradientDrawable());
            GradientDrawable dialogBg = new GradientDrawable();
            dialogBg.setColor(backgroundColor);
            dialogBg.setCornerRadius(28 * context.getResources().getDisplayMetrics().density);
            window.setBackgroundDrawable(dialogBg);
        }

        dialog.show();
    }

    private void setupRadiusInput(SeekBar seekBar, TextInputEditText input, java.util.function.IntConsumer setter) {
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    setter.accept(progress);
                    input.setText(String.valueOf(progress));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String str = s.toString().trim();
                if (!str.isEmpty()) {
                    try {
                        int value = Integer.parseInt(str);
                        setter.accept(value);
                        seekBar.setProgress(Math.min(value, 50));
                    } catch (NumberFormatException ignored) {}
                }
            }
        });
    }

    private void applySliderColor() {
        ColorStateList tint = ColorStateList.valueOf(editingColor[4]);
        redSeek.setProgressTintList(tint);
        redSeek.setThumbTintList(tint);
        greenSeek.setProgressTintList(tint);
        greenSeek.setThumbTintList(tint);
        blueSeek.setProgressTintList(tint);
        blueSeek.setThumbTintList(tint);
        radiusSeek.setProgressTintList(tint);
        radiusSeek.setThumbTintList(tint);
        cardRadiusSeek.setProgressTintList(tint);
        cardRadiusSeek.setThumbTintList(tint);
        buttonRadiusSeek.setProgressTintList(tint);
        buttonRadiusSeek.setThumbTintList(tint);
    }

    private void styleTypeButtons() {
        float density = context.getResources().getDisplayMetrics().density;
        float r = buttonCornerRadius * density;
        MaterialButton[] btns = {btnButton, btnBackground, btnCard, btnText, btnSlider};
        for (MaterialButton btn : btns) {
            GradientDrawable bg = new GradientDrawable();
            bg.setShape(GradientDrawable.RECTANGLE);
            bg.setColor(buttonColor);
            bg.setCornerRadius(r);
            btn.setBackground(bg);
            btn.setTextColor(getContrastColor(buttonColor));
        }
    }

    private void highlightTypeButton(int index) {
        float density = context.getResources().getDisplayMetrics().density;
        float r = buttonCornerRadius * density;
        MaterialButton[] btns = {btnButton, btnBackground, btnCard, btnText, btnSlider};
        int[] colors = {editingColor[0], editingColor[1], editingColor[2], editingColor[3], editingColor[4]};

        for (int i = 0; i < btns.length; i++) {
            GradientDrawable bg = new GradientDrawable();
            bg.setShape(GradientDrawable.RECTANGLE);
            if (i == index) {
                bg.setColor(colors[i]);
                btns[i].setTextColor(getContrastColor(colors[i]));
                bg.setStroke((int) (2 * density), Color.WHITE);
            } else {
                bg.setColor(colors[i]);
                btns[i].setTextColor(getContrastColor(colors[i]));
                bg.setStroke((int) (1 * density), Color.LTGRAY);
            }
            bg.setCornerRadius(r);
            btns[i].setBackground(bg);
        }
    }

    private int getContrastColor(int color) {
        double luminance = (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255;
        return luminance > 0.5 ? Color.BLACK : Color.WHITE;
    }

    private void loadColorToUI(int color) {
        redSeek.setProgress(Color.red(color));
        greenSeek.setProgress(Color.green(color));
        blueSeek.setProgress(Color.blue(color));
        updatePreview();
        updateHexInput();
    }

    private void updatePreview() {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.RECTANGLE);
        drawable.setCornerRadius(16);
        drawable.setColor(editingColor[currentEditingType]);
        colorPreview.setBackground(drawable);
    }

    private void updateHexInput() {
        String hex = String.format("#%06X", editingColor[currentEditingType] & 0xFFFFFF);
        if (hexInput.getText() == null || !hexInput.getText().toString().equalsIgnoreCase(hex)) {
            hexInput.setText(hex);
            hexInput.setSelection(hex.length());
        }
    }
}
