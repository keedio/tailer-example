package com.keedio.tailer.listener;

import com.keedio.tailer.io.Tailer;
import com.keedio.tailer.rotation.RotationPolicy;
import org.apache.commons.io.input.TailerListenerAdapter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Luca Rosellini <lrosellini@keedio.com> on 9/2/16.
 */
@Component
public class KeedioTailerListenerAdapter extends TailerListenerAdapter implements KeedioTailerListener {
    private final static Logger LOGGER = LogManager.getLogger(KeedioTailerListenerAdapter.class);
    private final static Logger OUT_LOGGER = LogManager.getLogger("com.keedio.out");

    private Tailer instance;
    private TailerTaskTimeoutHelper timeoutHelper;

    private int countedLines = 0;

    @Value("${timeout.delay:10000}")
    private long timeoutDelay;

    @Autowired
    private RotationPolicy rotationPolicy;

    @Override
    public void handle(Exception ex) {
        super.handle(ex);
        LOGGER.error("Exception caught", ex);
    }

    public void init(Tailer tailer) {
        super.init(tailer);

        timeoutHelper = new TailerTaskTimeoutHelper(tailer);
        this.instance = tailer;

        timeoutHelper.resetTimer();

        LOGGER.info("Initializing tailer on file: " + tailer.getFile().getAbsolutePath());
    }

    @Override
    public RotationPolicy getRotationPolicy() {
        return rotationPolicy;
    }

    @Override
    public void fileNotFound() {
        super.fileNotFound();

        LOGGER.info("File not found: " + instance.getFile().getAbsolutePath());
    }

    @Override
    public void fileRotated() {
        super.fileRotated();

        long lastPosition = instance.getPosition();

        LOGGER.info("File rotated: " + instance.getFile().getAbsolutePath() + ", last position: " + lastPosition);

        handleRotatedFile();
    }

    private void handleRotatedFile() {
        long lastPosition = instance.getPosition();

        if (rotationPolicy == null) {
            return;
        }

        /**
         * Abro el fichero rotado, eg: filename.log.1, a la posición indicada por lastPosition
         */
        File rotatedFile = new File(rotationPolicy.rotatedFileName(
                instance.getFile().getParentFile().getAbsolutePath(),
                instance.getFile().getName()));

        try (BufferedReader buffer = new BufferedReader(new FileReader(rotatedFile))) {
            LOGGER.info("Opened rotated file: " + rotatedFile.getAbsolutePath());

            buffer.skip(lastPosition);

            String line = "";

            int readLines = 0;
            while ((line = buffer.readLine()) != null) {
                handle(line);

                readLines++;
            }

            LOGGER.info("Read " + readLines + " lines from rotated file");

        } catch (IOException e) {
            LOGGER.error("Unable to open rotated file: " + rotatedFile.getAbsolutePath(), e);
        }
    }

    @Override
    public void handle(String line) {
        super.handle(line);

        countedLines++;
        LOGGER.info("[" + instance.getFile().getAbsolutePath() + "], lines: " + countedLines);
        OUT_LOGGER.trace(line);

        timeoutHelper.resetTimer();
    }

    /**
     * Created by luca on 10/2/16.
     */
    class TailerTaskTimeoutHelper {
        private TimerTask tailerTimer;
        private Timer timer;
        private Tailer instance;

        public TailerTaskTimeoutHelper(Tailer instance) {
            this.instance = instance;
            timer = new Timer();
        }

        void resetTimer() {
            if (tailerTimer != null) {
                LOGGER.trace("Resetting timer for: " + instance.getFile().getAbsolutePath());
                tailerTimer.cancel();
                timer.purge();
            }

            tailerTimer = new TimerTask() {
                @Override
                public void run() {
                    LOGGER.warn("Timeout expire for file: " + instance.getFile().getAbsolutePath());
                    instance.stop();
                }
            };

            LOGGER.trace("Initializing timer for: " + instance.getFile().getAbsolutePath());
            timer.schedule(tailerTimer, timeoutDelay);
        }
    }
}