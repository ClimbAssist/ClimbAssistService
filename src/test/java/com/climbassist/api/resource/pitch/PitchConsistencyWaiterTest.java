package com.climbassist.api.resource.pitch;

import com.google.common.collect.ImmutableSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PitchConsistencyWaiterTest {

    private static final Pitch PITCH_1 = Pitch.builder()
            .pitchId("pitch-1")
            .routeId("route-1")
            .description("Pitch 1")
            .grade(11)
            .gradeModifier("b/c")
            .danger("R")
            .anchors(Anchors.builder()
                    .x(1.0)
                    .y(1.0)
                    .z(1.0)
                    .fixed(true)
                    .build())
            .first(true)
            .next("pitch-2")
            .build();
    private static final Pitch PITCH_2 = Pitch.builder()
            .pitchId("pitch-2")
            .routeId("route-1")
            .description("Pitch 2")
            .grade(11)
            .gradeModifier("b/c")
            .danger("R")
            .anchors(Anchors.builder()
                    .x(2.0)
                    .y(2.0)
                    .z(2.0)
                    .fixed(true)
                    .build())
            .next("pitch-3")
            .build();
    private static final Pitch PITCH_3 = Pitch.builder()
            .pitchId("pitch-3")
            .routeId("route-1")
            .description("Pitch 3")
            .grade(11)
            .gradeModifier("b/c")
            .danger("R")
            .anchors(Anchors.builder()
                    .x(3.0)
                    .y(3.0)
                    .z(3.0)
                    .fixed(true)
                    .build())
            .build();
    private static final Set<Pitch> PITCHES_WITHOUT_EXPECTED = ImmutableSet.of(PITCH_2, PITCH_3);
    private static final Set<Pitch> PITCHES_WITH_EXPECTED = ImmutableSet.<Pitch>builder().addAll(
            PITCHES_WITHOUT_EXPECTED)
            .add(PITCH_1)
            .build();
    private static final int MAX_RETRIES = 5;

    @Mock
    private PitchesDao mockPitchesDao;

    private PitchConsistencyWaiter pitchConsistencyWaiter;

    @BeforeEach
    void setUp() {
        pitchConsistencyWaiter = PitchConsistencyWaiter.builder()
                .pitchesDao(mockPitchesDao)
                .build();
    }

    @Test
    void waitForConsistency_returns_whenPitchShouldExistAndFirstCallIsConsistent()
            throws InterruptedException, PitchConsistencyException {
        when(mockPitchesDao.getResources(any())).thenReturn(PITCHES_WITH_EXPECTED);
        pitchConsistencyWaiter.waitForConsistency(PITCH_1.getRouteId(), PITCH_1, true);
        verify(mockPitchesDao).getResources(PITCH_1.getRouteId());
    }

    @Test
    void waitForConsistency_returns_whenPitchShouldExistAndLastCallIsConsistent()
            throws InterruptedException, PitchConsistencyException {
        doReturn(PITCHES_WITHOUT_EXPECTED, PITCHES_WITHOUT_EXPECTED, PITCHES_WITHOUT_EXPECTED, PITCHES_WITHOUT_EXPECTED,
                PITCHES_WITH_EXPECTED).when(mockPitchesDao)
                .getResources(any());
        pitchConsistencyWaiter.waitForConsistency(PITCH_1.getRouteId(), PITCH_1, true);
        verify(mockPitchesDao, times(MAX_RETRIES)).getResources(PITCH_1.getRouteId());
    }

    @Test
    void waitForConsistency_throwPitchConsistencyException_whenPitchShouldExistAndNoCallsAreConsistent() {
        doReturn(PITCHES_WITHOUT_EXPECTED).when(mockPitchesDao)
                .getResources(any());
        PitchConsistencyException pitchConsistencyException = assertThrows(PitchConsistencyException.class,
                () -> pitchConsistencyWaiter.waitForConsistency(PITCH_1.getRouteId(), PITCH_1, true));
        assertThat(pitchConsistencyException.getMessage(), is(equalTo(String.format(
                "Consistency was not achieved while modifying pitch %s. Its parent route(s) may or may not have been " +
                        "updated.", PITCH_1.getPitchId()))));
        verify(mockPitchesDao, times(MAX_RETRIES)).getResources(PITCH_1.getRouteId());
    }

    @Test
    void waitForConsistency_returns_whenPitchShouldNotExistAndFirstCallIsConsistent()
            throws InterruptedException, PitchConsistencyException {
        when(mockPitchesDao.getResources(any())).thenReturn(PITCHES_WITHOUT_EXPECTED);
        pitchConsistencyWaiter.waitForConsistency(PITCH_1.getRouteId(), PITCH_1, false);
        verify(mockPitchesDao).getResources(PITCH_1.getRouteId());
    }

    @Test
    void waitForConsistency_returns_whenPitchShouldNotExistAndLastCallIsConsistent()
            throws InterruptedException, PitchConsistencyException {
        doReturn(PITCHES_WITH_EXPECTED, PITCHES_WITH_EXPECTED, PITCHES_WITH_EXPECTED, PITCHES_WITH_EXPECTED,
                PITCHES_WITHOUT_EXPECTED).when(mockPitchesDao)
                .getResources(any());
        pitchConsistencyWaiter.waitForConsistency(PITCH_1.getRouteId(), PITCH_1, false);
        verify(mockPitchesDao, times(MAX_RETRIES)).getResources(PITCH_1.getRouteId());
    }

    @Test
    void waitForConsistency_throwPitchConsistencyException_whenPitchShouldNotExistAndNoCallsAreConsistent() {
        doReturn(PITCHES_WITH_EXPECTED).when(mockPitchesDao)
                .getResources(any());
        PitchConsistencyException pitchConsistencyException = assertThrows(PitchConsistencyException.class,
                () -> pitchConsistencyWaiter.waitForConsistency(PITCH_1.getRouteId(), PITCH_1, false));
        assertThat(pitchConsistencyException.getMessage(), is(equalTo(String.format(
                "Consistency was not achieved while modifying pitch %s. Its parent route(s) may or may not have been " +
                        "updated.", PITCH_1.getPitchId()))));
        verify(mockPitchesDao, times(MAX_RETRIES)).getResources(PITCH_1.getRouteId());
    }
}
