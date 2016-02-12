package com.keedio.tailer.listener;

import com.keedio.tailer.io.Tailer;
import com.keedio.tailer.rotation.RotationPolicy;

/**
 * Created by luca on 11/2/16.
 */
public class DummyKeedioTailListener implements KeedioTailerListener {
    @Override
    public void init(Tailer tailer) {

    }

    @Override
    public RotationPolicy getRotationPolicy() {
        return null;
    }

    @Override
    public void init(org.apache.commons.io.input.Tailer tailer) {

    }

    @Override
    public void fileNotFound() {

    }

    @Override
    public void fileRotated() {

    }

    @Override
    public void handle(String line) {

    }

    @Override
    public void handle(Exception ex) {

    }
}
