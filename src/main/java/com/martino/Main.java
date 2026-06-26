package com.martino;

public class Main {

    public static void main(String[] args) {

        try (
                CamillaDspRemote camilla =
                        new CamillaDspRemote(AppConfig.CAMILLA_DSP_WS_URL);

                WindowsKeyboardHook keyboard =
                        new WindowsKeyboardHook(
                                new WindowsKeyboardHook.KeyboardActionHandler() {

                                    @Override
                                    public void onVolumeUp() {
                                        camilla.changeGain(AppConfig.VOLUME_STEP_DB);
                                    }

                                    @Override
                                    public void onVolumeDown() {
                                        camilla.changeGain(-AppConfig.VOLUME_STEP_DB);
                                    }

                                    @Override
                                    public void onMute() {
                                        camilla.toggleMute();
                                    }
                                })
        ) {
            camilla.connect();

            System.out.println("Martino CamillaDSP controller started.");
            System.out.println("Volume Up   -> +1 dB");
            System.out.println("Volume Down -> -1 dB");
            System.out.println("Mute        -> toggle mute");
            System.out.println("ESC         -> exit");

            keyboard.startMessageLoop();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}