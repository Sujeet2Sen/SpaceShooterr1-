package com.sujeet.spaceshooter.utils;

import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioTrack;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SoundManager {

    private static final int SAMPLE_RATE = 44100;
    public static boolean soundEnabled = true;

    private static final ExecutorService executor = Executors.newFixedThreadPool(2);

    private static byte[] shootPcm;
    private static byte[] explosionPcm;
    private static byte[] powerUpPcm;
    private static byte[] hitPcm;

    static {
        initAudioBuffers();
    }

    private static void initAudioBuffers() {
        try {
            // Shoot PCM (100ms)
            int shootSamples = (int) (SAMPLE_RATE * 0.1);
            shootPcm = new byte[2 * shootSamples];
            for (int i = 0; i < shootSamples; ++i) {
                double t = (double) i / SAMPLE_RATE;
                double freq = 1200.0 * Math.exp(-t * 15.0) + 200.0;
                double env = Math.max(0.0, 1.0 - (double) i / shootSamples);
                short sample = (short) (Math.sin(2.0 * Math.PI * freq * t) * 32767 * env);
                shootPcm[2 * i] = (byte) (sample & 0x00ff);
                shootPcm[2 * i + 1] = (byte) ((sample & 0xff00) >>> 8);
            }

            // Explosion PCM (400ms)
            int expSamples = (int) (SAMPLE_RATE * 0.4);
            explosionPcm = new byte[2 * expSamples];
            for (int i = 0; i < expSamples; ++i) {
                double t = (double) i / SAMPLE_RATE;
                double noise = (Math.random() * 2.0 - 1.0);
                double rumble = Math.sin(2.0 * Math.PI * 60.0 * t);
                double env = Math.max(0.0, 1.0 - (double) i / expSamples);
                short sample = (short) ((noise * 0.7 + rumble * 0.3) * 32767 * env);
                explosionPcm[2 * i] = (byte) (sample & 0x00ff);
                explosionPcm[2 * i + 1] = (byte) ((sample & 0xff00) >>> 8);
            }

            // PowerUp PCM (250ms)
            int pwrSamples = (int) (SAMPLE_RATE * 0.25);
            powerUpPcm = new byte[2 * pwrSamples];
            double[] freqs = {523.25, 659.25, 783.99};
            for (int i = 0; i < pwrSamples; ++i) {
                double t = (double) i / SAMPLE_RATE;
                int noteIndex = Math.min(2, (int) ((i / (double) pwrSamples) * 3));
                double freq = freqs[noteIndex];
                double env = Math.max(0.0, 1.0 - (double) i / pwrSamples);
                short sample = (short) (Math.sin(2.0 * Math.PI * freq * t) * 32767 * env);
                powerUpPcm[2 * i] = (byte) (sample & 0x00ff);
                powerUpPcm[2 * i + 1] = (byte) ((sample & 0xff00) >>> 8);
            }

            // Hit PCM (200ms)
            int hitSamples = (int) (SAMPLE_RATE * 0.2);
            hitPcm = new byte[2 * hitSamples];
            for (int i = 0; i < hitSamples; ++i) {
                double t = (double) i / SAMPLE_RATE;
                double freq = 150.0 * Math.exp(-t * 10.0);
                double env = Math.max(0.0, 1.0 - (double) i / hitSamples);
                short sample = (short) (Math.sin(2.0 * Math.PI * freq * t) * 32767 * env);
                hitPcm[2 * i] = (byte) (sample & 0x00ff);
                hitPcm[2 * i + 1] = (byte) ((sample & 0xff00) >>> 8);
            }
        } catch (Exception ignored) {
        }
    }

    public static void playShoot() {
        if (!soundEnabled || shootPcm == null) return;
        executor.execute(() -> playAudio(shootPcm));
    }

    public static void playExplosion() {
        if (!soundEnabled || explosionPcm == null) return;
        executor.execute(() -> playAudio(explosionPcm));
    }

    public static void playPowerUp() {
        if (!soundEnabled || powerUpPcm == null) return;
        executor.execute(() -> playAudio(powerUpPcm));
    }

    public static void playHit() {
        if (!soundEnabled || hitPcm == null) return;
        executor.execute(() -> playAudio(hitPcm));
    }

    private static void playAudio(byte[] pcmData) {
        if (!soundEnabled) return;
        try {
            AudioTrack audioTrack = new AudioTrack.Builder()
                    .setAudioAttributes(new AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build())
                    .setAudioFormat(new AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(SAMPLE_RATE)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build())
                    .setBufferSizeInBytes(pcmData.length)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build();

            audioTrack.write(pcmData, 0, pcmData.length);
            audioTrack.play();
        } catch (Exception ignored) {
        }
    }
}
