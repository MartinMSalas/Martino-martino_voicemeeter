package com.martino;

import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinDef.HMODULE;
import com.sun.jna.platform.win32.WinDef.LRESULT;
import com.sun.jna.platform.win32.WinDef.WPARAM;
import com.sun.jna.platform.win32.WinUser;
import com.sun.jna.platform.win32.WinUser.HHOOK;
import com.sun.jna.platform.win32.WinUser.KBDLLHOOKSTRUCT;
import com.sun.jna.platform.win32.WinUser.MSG;
public final class WindowsKeyboardHook implements AutoCloseable {

    private static final int WH_KEYBOARD_LL = 13;

    private static final int WM_KEYDOWN = 0x0100;
    private static final int WM_SYSKEYDOWN = 0x0104;

    private static final int VK_VOLUME_MUTE = 0xAD;
    private static final int VK_VOLUME_DOWN = 0xAE;
    private static final int VK_VOLUME_UP = 0xAF;
    private static final int VK_ESCAPE = 0x1B;

    private final KeyboardActionHandler actionHandler;

    private HHOOK hook;
    private WinUser.LowLevelKeyboardProc keyboardProc;
    private volatile boolean running;

    public WindowsKeyboardHook(KeyboardActionHandler actionHandler) {
        this.actionHandler = actionHandler;
    }

    public void startMessageLoop() {
        HMODULE hMod = Kernel32.INSTANCE.GetModuleHandle(null);

        keyboardProc = (nCode, wParam, info) -> {
            if (nCode >= 0 && isKeyDown(wParam)) {
                handleVirtualKeyCode(info.vkCode);
            }

            WinDef.LPARAM lParam = new WinDef.LPARAM(Pointer.nativeValue(info.getPointer()));

            return User32.INSTANCE.CallNextHookEx(
                    hook,
                    nCode,
                    wParam,
                    lParam
            );
        };

        hook = User32.INSTANCE.SetWindowsHookEx(
                WH_KEYBOARD_LL,
                keyboardProc,
                hMod,
                0
        );

        if (hook == null) {
            throw new IllegalStateException("Could not install low-level keyboard hook.");
        }

        running = true;
        System.out.println("Native Windows keyboard hook installed.");

        MSG msg = new MSG();

        while (running && User32.INSTANCE.GetMessage(msg, null, 0, 0) != 0) {
            User32.INSTANCE.TranslateMessage(msg);
            User32.INSTANCE.DispatchMessage(msg);
        }
    }

    private boolean isKeyDown(WPARAM wParam) {
        int message = wParam.intValue();
        return message == WM_KEYDOWN || message == WM_SYSKEYDOWN;
    }

    private void handleVirtualKeyCode(int vkCode) {
        switch (vkCode) {
            case VK_VOLUME_UP -> actionHandler.onVolumeUp();
            case VK_VOLUME_DOWN -> actionHandler.onVolumeDown();
            case VK_VOLUME_MUTE -> actionHandler.onMute();
            case VK_ESCAPE -> {
                System.out.println("ESC pressed. Exiting Martino.");
                // stop();
            }
            default -> {
                // Ignore other keys.
            }
        }
    }

    public void stop() {
        running = false;
        User32.INSTANCE.PostQuitMessage(0);
    }

    @Override
    public void close() {
        stop();

        if (hook != null) {
            User32.INSTANCE.UnhookWindowsHookEx(hook);
            hook = null;
            System.out.println("Native Windows keyboard hook removed.");
        }
    }

    public interface KeyboardActionHandler {
        void onVolumeUp();

        void onVolumeDown();

        void onMute();
    }
}