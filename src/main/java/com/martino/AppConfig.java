package com.martino;

public final class AppConfig {

    private AppConfig() {
    }

    public static final String CAMILLA_DSP_WS_URL =
            "ws://127.0.0.1:1234";

    public static final float VOLUME_STEP_DB = 0.5f;

    public static final float MIN_GAIN_DB = -60.0f;
    public static final float MAX_GAIN_DB = 0.0f;
}
