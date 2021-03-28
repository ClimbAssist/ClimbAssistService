package com.climbassist.api.v2;

import com.google.common.testing.NullPointerTester;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

class SentenceGeneratorTest {

    private static final String WORD_1 = "test1";
    private static final String WORD_2 = "test2";
    private static final String WORD_3 = "test3";

    @SuppressWarnings("UnstableApiUsage")
    @Test
    void parametersMarkedWithNonNull_throwNullPointerException_forNullValues() {
        NullPointerTester nullPointerTester = new NullPointerTester();
        nullPointerTester.testStaticMethods(SentenceGenerator.class, NullPointerTester.Visibility.PACKAGE);
    }

    @Test
    void toOrList_returnsEmptyString_whenInputIsEmpty() {
        assertThat(SentenceGenerator.toOrList(new String[0]), is(equalTo("")));
    }

    @Test
    void toOrList_returnsInputString_whenInputIsOneWord() {
        assertThat(SentenceGenerator.toOrList(new String[]{WORD_1}), is(equalTo(WORD_1)));
    }

    @Test
    void toOrList_returnsTwoWordsSeparatedByOr_whenInputIsTwoWords() {
        assertThat(SentenceGenerator.toOrList(new String[]{WORD_1, WORD_2}), is(equalTo(WORD_1 + " or " + WORD_2)));
    }

    @Test
    void toOrList_returnsWordsAsOrList_whenInputIsThreeWords() {
        assertThat(SentenceGenerator.toOrList(new String[]{WORD_1, WORD_2, WORD_3}),
                is(equalTo(WORD_1 + ", " + WORD_2 + ", or " + WORD_3)));
    }
}
