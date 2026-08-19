package cn.mcleng.mineleng.mcserverquery;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private AutoCompleteTextView editionDropdown;
    private TextInputLayout addressLayout;
    private TextInputLayout editionDropdownLayout;
    private TextInputEditText addressInput;
    private TextInputLayout portLayout;
    private TextInputEditText portInput;
    private MaterialButton queryButton;
    private ProgressBar loadingIndicator;
    private MaterialCardView resultCard;
    private ImageView serverIcon;
    private TextView motdText;
    private TextView versionText;
    private TextView pingText;
    private TextView playersText;
    private MaterialButton paletteButton;
    private View rootView;
    private MaterialCardView inputCard;

    private ExecutorService executor;
    private boolean isJavaEdition = true;
    private ColorManager colorManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        colorManager = new ColorManager(this);

        initViews();
        applyThemeColor();
        setupEditionDropdown();
        setupQueryButton();
        setupPaletteButton();

        executor = Executors.newSingleThreadExecutor();
    }

    private void initViews() {
        rootView = findViewById(android.R.id.content);
        editionDropdownLayout = findViewById(R.id.edition_dropdown_layout);
        editionDropdown = findViewById(R.id.edition_dropdown);
        addressLayout = findViewById(R.id.address_layout);
        addressInput = findViewById(R.id.address_input);
        portLayout = findViewById(R.id.port_layout);
        portInput = findViewById(R.id.port_input);
        queryButton = findViewById(R.id.query_button);
        loadingIndicator = findViewById(R.id.loading_indicator);
        resultCard = findViewById(R.id.result_card);
        serverIcon = findViewById(R.id.server_icon);
        motdText = findViewById(R.id.motd_text);
        versionText = findViewById(R.id.version_text);
        pingText = findViewById(R.id.ping_text);
        playersText = findViewById(R.id.players_text);
        paletteButton = findViewById(R.id.btn_palette);

        inputCard = rootView.findViewById(R.id.input_card);
    }

    private void setupEditionDropdown() {
        String[] editions = {
                getString(R.string.java_edition),
                getString(R.string.bedrock_edition)
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                editions
        );
        editionDropdown.setAdapter(adapter);

        editionDropdown.setOnItemClickListener((parent, view, position, id) -> {
            isJavaEdition = (position == 0);
            updateInputFields();
        });
    }

    private void updateInputFields() {
        if (isJavaEdition) {
            addressLayout.setHint(getString(R.string.hint_address));
            portLayout.setVisibility(View.GONE);
        } else {
            addressLayout.setHint(getString(R.string.hint_domain));
            portLayout.setVisibility(View.VISIBLE);
        }
    }

    private void setupQueryButton() {
        queryButton.setOnClickListener(v -> performQuery());
    }

    private void setupPaletteButton() {
        paletteButton.setOnClickListener(v -> showColorPicker());
    }

    private void showColorPicker() {
        ColorPickerDialog dialog = new ColorPickerDialog(
                this,
                colorManager.getButtonColor(),
                colorManager.getBackgroundColor(),
                colorManager.getCardColor(),
                colorManager.getTextColor(),
                colorManager.getSliderColor(),
                colorManager.getInputCornerRadius(),
                colorManager.getCardCornerRadius(),
                colorManager.getButtonCornerRadius(),
                colorManager.isDynamicColor()
        );
        dialog.setOnColorSelectedListener((buttonColor, backgroundColor, cardColor, textColor, sliderColor, inputRadius, cardRadius, buttonRadius, dynamicColor) -> {
            colorManager.setButtonColor(buttonColor);
            colorManager.setBackgroundColor(backgroundColor);
            colorManager.setCardColor(cardColor);
            colorManager.setTextColor(textColor);
            colorManager.setSliderColor(sliderColor);
            colorManager.setInputCornerRadius(inputRadius);
            colorManager.setCardCornerRadius(cardRadius);
            colorManager.setButtonCornerRadius(buttonRadius);
            colorManager.setDynamicColor(dynamicColor);

            if (dynamicColor) {
                colorManager.applyDynamicColors(this);
            }

            colorManager.save(this);
            applyThemeColor();
        });
        dialog.show();
    }

    private void applyThemeColor() {
        if (colorManager.isDynamicColor()) {
            colorManager.applyDynamicColors(this);
        }

        int buttonColor = colorManager.getButtonColor();
        int backgroundColor = colorManager.getBackgroundColor();
        int cardColor = colorManager.getCardColor();
        int textColor = colorManager.getTextColor();
        int sliderColor = colorManager.getSliderColor();
        int inputRadius = colorManager.getInputCornerRadius();
        int cardRadius = colorManager.getCardCornerRadius();
        int buttonRadius = colorManager.getButtonCornerRadius();

        int onButtonColor = getContrastColor(buttonColor);

        rootView.setBackgroundColor(backgroundColor);

        queryButton.setBackgroundTintList(ColorStateList.valueOf(buttonColor));
        queryButton.setTextColor(onButtonColor);
        queryButton.setIconTint(ColorStateList.valueOf(onButtonColor));

        GradientDrawable buttonBg = new GradientDrawable();
        buttonBg.setShape(GradientDrawable.RECTANGLE);
        buttonBg.setColor(buttonColor);
        buttonBg.setCornerRadius(buttonRadius * getResources().getDisplayMetrics().density);
        queryButton.setBackground(buttonBg);

        loadingIndicator.setIndeterminateTintList(ColorStateList.valueOf(buttonColor));
        paletteButton.setIconTint(ColorStateList.valueOf(buttonColor));

        if (inputCard != null) {
            inputCard.setCardBackgroundColor(cardColor);
            inputCard.setRadius(cardRadius * getResources().getDisplayMetrics().density);
        }

        if (resultCard != null) {
            resultCard.setCardBackgroundColor(cardColor);
            resultCard.setRadius(cardRadius * getResources().getDisplayMetrics().density);
        }

        float r = inputRadius * getResources().getDisplayMetrics().density;

        if (editionDropdownLayout != null) {
            editionDropdownLayout.setBoxCornerRadii(r, r, r, r);
        }
        addressLayout.setBoxCornerRadii(r, r, r, r);
        portLayout.setBoxCornerRadii(r, r, r, r);

        motdText.setTextColor(textColor);
        versionText.setTextColor(textColor);
        pingText.setTextColor(textColor);
        playersText.setTextColor(textColor);

        Window window = getWindow();
        window.setStatusBarColor(backgroundColor);
        window.setNavigationBarColor(backgroundColor);

        if (editionDropdownLayout != null) {
            editionDropdownLayout.setBoxStrokeColor(buttonColor);
            editionDropdownLayout.setHintTextColor(ColorStateList.valueOf(buttonColor));
            editionDropdownLayout.setDefaultHintTextColor(ColorStateList.valueOf(textColor | 0x99000000));
        }
        addressLayout.setBoxStrokeColor(buttonColor);
        addressLayout.setHintTextColor(ColorStateList.valueOf(buttonColor));
        addressLayout.setDefaultHintTextColor(ColorStateList.valueOf(textColor | 0x99000000));
        portLayout.setBoxStrokeColor(buttonColor);
        portLayout.setHintTextColor(ColorStateList.valueOf(buttonColor));
        portLayout.setDefaultHintTextColor(ColorStateList.valueOf(textColor | 0x99000000));

        View toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            ((com.google.android.material.appbar.MaterialToolbar) toolbar).setTitleTextColor(textColor);
        }
    }

    private int getContrastColor(int color) {
        double luminance = (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255;
        return luminance > 0.5 ? Color.BLACK : Color.WHITE;
    }

    private void performQuery() {
        String address = addressInput.getText().toString().trim();
        if (address.isEmpty()) {
            Snackbar.make(queryButton, R.string.error_empty_address, Snackbar.LENGTH_SHORT).show();
            return;
        }

        setLoadingState(true);
        resultCard.setVisibility(View.GONE);

        executor.execute(() -> {
            ServerResult result;
            try {
                if (isJavaEdition) {
                    result = JavaEditionQuery.query(address);
                } else {
                    String portStr = portInput.getText().toString().trim();
                    int port = 19132;
                    if (!portStr.isEmpty()) {
                        try {
                            port = Integer.parseInt(portStr);
                        } catch (NumberFormatException e) {
                            port = 19132;
                        }
                    }
                    result = BedrockEditionQuery.query(address, port);
                }
            } catch (Exception e) {
                DebugLog.error("Main", "Executor exception", e);
                result = ServerResult.offline();
            }

            final ServerResult finalResult = result;
            runOnUiThread(() -> {
                setLoadingState(false);
                displayResult(finalResult);
            });
        });
    }

    private void setLoadingState(boolean loading) {
        loadingIndicator.setVisibility(loading ? View.VISIBLE : View.GONE);
        queryButton.setEnabled(!loading);
    }

    private void displayResult(ServerResult result) {
        if (!result.online) {
            Snackbar.make(queryButton, R.string.server_offline, Snackbar.LENGTH_LONG).show();
            return;
        }

        resultCard.setVisibility(View.VISIBLE);

        if (result.icon != null) {
            serverIcon.setImageBitmap(result.icon);
        } else {
            serverIcon.setImageResource(R.drawable.ic_server_placeholder);
        }

        motdText.setText(result.motd != null ? result.motd : "");
        versionText.setText(result.version != null ? result.version : "");
        pingText.setText(getString(R.string.ping_format, result.ping));
        playersText.setText(getString(R.string.players_format, result.onlinePlayers, result.maxPlayers));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executor != null) {
            executor.shutdown();
        }
    }
}
