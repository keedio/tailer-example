package com.keedio.tailer.listener;

import com.keedio.tailer.LRTailer;
import com.keedio.tailer.output.TailerOutputProcessor;
import com.keedio.tailer.rotation.RotationPolicy;
import com.keedio.tailer.validator.LineValidator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Luca Rosellini <lrosellini@keedio.com> on 9/2/16.
 */
@Component
@Scope("prototype")
public class KeedioFileEventListener implements FileEventListener {

    private final static Logger LOGGER = LogManager.getLogger(KeedioFileEventListener.class);

    @Autowired
    private TailerOutputProcessor tailerOutput;

    private LRTailer tailer;

    @Autowired
    private LineValidator lineValidator;

    private int countedLines = 0;

    @Autowired
    private RotationPolicy rotationPolicy;

    @Value("${file.monitoring.timeout}")
    private long timeoutDelay;

    private TailerTaskTimeoutHelper timeoutHelper;

    @Override
    public void init(LRTailer lrTailer) {
        this.tailer = lrTailer;

        timeoutHelper = new TailerTaskTimeoutHelper(tailer);

        timeoutHelper.resetTimer();

        LOGGER.info("Initializing tailer on file: " + tailer.getTailedFile().getAbsolutePath());
    }

    @Override
    public String rotated(long lastPosition, long currPosition) {
        String rotatedFile = rotationPolicy.rotatedFileName(
                tailer.getTailedFile().getParentFile().getAbsolutePath(),
                tailer.getTailedFile().getName());

        LOGGER.info("[" + tailer.getTailedFile().getAbsolutePath() + "], rotated to: " + rotatedFile);

        return rotatedFile;

    }

    @Override
    public void handle(String filename, String line) {
        countedLines++;
        LOGGER.info("[" + filename + "], lines: " + countedLines);

        tailerOutput.process(line);
        timeoutHelper.resetTimer();
    }

    @Override
    public void notExists() {
        LOGGER.info("File not found: " + tailer.getTailedFile().getAbsolutePath());
    }

    @Override
    public void handleException(Exception e) {
        LOGGER.error("Exception caught", e);
    }

    @Override
    public boolean isValid(String line) {
        return lineValidator.isValid(line);
    }

    class TailerTaskTimeoutHelper {
        private TimerTask tailerTimer;
        private Timer timer;
        private LRTailer instance;

        public TailerTaskTimeoutHelper(LRTailer instance) {
            this.instance = instance;
            timer = new Timer();
        }

        void resetTimer() {
            if (tailerTimer != null) {
                tailerTimer.cancel();
                timer.purge();
            }

            tailerTimer = new TimerTask() {
                @Override
                public void run() {
                    LOGGER.warn("Timeout expire for file: " + instance.getTailedFile().getAbsolutePath());
                    instance.stop();
                }
            };

            LOGGER.trace("Initializing timer for: " + instance.getTailedFile().getAbsolutePath());
            timer.schedule(tailerTimer, timeoutDelay);
        }
    }
}