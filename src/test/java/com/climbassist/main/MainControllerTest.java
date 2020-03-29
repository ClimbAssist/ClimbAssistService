package com.climbassist.main;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

class MainControllerTest {

    private MainController mainController;

    @BeforeEach
    void setUp() {
        mainController = new MainController();
    }

    @Test
    void index_forwardsToIndexDotHtml() {
        assertThat(mainController.index(), equalTo("forward:/static/index.html"));
    }
}
