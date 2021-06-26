package com.cauldronjs.paper;

import com.cauldronjs.Cauldron;
import com.cauldronjs.config.PlatformConfig;
import org.graalvm.polyglot.Value;

import java.io.File;
import java.io.InputStream;

public class PaperCauldron implements Cauldron {

    @Override
    public boolean isRunning() {
        return false;
    }

    @Override
    public boolean isDebugging() {
        return false;
    }

    @Override
    public void log(System.Logger.Level level, String content, Object... args) {

    }

    @Override
    public PlatformConfig getPlatformConfig() {
        return null;
    }

    @Override
    public File getWorkingDirectory() {
        return null;
    }

    @Override
    public InputStream getResourceFile(String filename) {
        return null;
    }

    @Override
    public int scheduleRepeatingTask(Value fn, int interval, int timeout) {
        return 0;
    }

    @Override
    public int scheduleTask(Value fn, int timeout) {
        return 0;
    }

    @Override
    public int scheduleRepeatingTask(Runnable runnable, int interval, int timeout) {
        return 0;
    }

    @Override
    public int scheduleTask(Runnable runnable, int timeout) {
        return 0;
    }

    @Override
    public boolean cancelTask(int id) {
        return false;
    }
}
