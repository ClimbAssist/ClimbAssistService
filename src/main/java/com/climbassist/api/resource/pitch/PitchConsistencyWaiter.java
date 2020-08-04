package com.climbassist.api.resource.pitch;

import com.climbassist.api.user.UserData;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * This class handles waiting for consistency in the Pitches table so that the parent routes are updated properly.
 * This is a horrible ugly disgusting hacky workaround and we should migrate to a graph database ASAP.
 */
@Builder
@Slf4j
class PitchConsistencyWaiter {

    @AllArgsConstructor
    @Value
    private class PitchConsistencyRunnable implements Runnable {

        String routeId;
        Pitch expectedPitch;
        boolean shouldExist;
        CountDownLatch countDownLatch;
        int attempts;
        ScheduledExecutorService scheduledExecutorService;
        @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
        Optional<UserData> maybeUserData;

        @Override
        public void run() {
            boolean pitchExists = pitchesDao.getResources(routeId, maybeUserData)
                    .contains(expectedPitch);
            if (shouldExist == pitchExists) {
                countDownLatch.countDown();
            }
            else if (attempts < MAX_RETRIES) {
                scheduledExecutorService.schedule(
                        new PitchConsistencyRunnable(routeId, expectedPitch, shouldExist, countDownLatch, attempts + 1,
                                scheduledExecutorService, maybeUserData), 1, TimeUnit.SECONDS);
            }
        }
    }

    private static final int MAX_RETRIES = 10;

    @NonNull
    private final PitchesDao pitchesDao;

    public void waitForConsistency(String routeId, Pitch expectedPitch, boolean shouldExist,
                                   @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
                                           Optional<UserData> maybeUserData)
            throws PitchConsistencyException, InterruptedException {
        log.info(String.format("Waiting for consistency for pitch %s in route %s, which %s exist.",
                expectedPitch.getPitchId(), routeId, shouldExist ? "should" : "should not"));
        CountDownLatch countDownLatch = new CountDownLatch(1);
        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
        scheduledExecutorService.schedule(
                new PitchConsistencyRunnable(routeId, expectedPitch, shouldExist, countDownLatch, 0,
                        scheduledExecutorService, maybeUserData), 0, TimeUnit.SECONDS);
        if (!countDownLatch.await(10, TimeUnit.SECONDS)) {
            throw new PitchConsistencyException(expectedPitch.getPitchId());
        }
    }
}
