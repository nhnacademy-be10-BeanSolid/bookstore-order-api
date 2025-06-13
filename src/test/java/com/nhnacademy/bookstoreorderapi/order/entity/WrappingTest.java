package com.nhnacademy.bookstoreorderapi.order.entity;


import com.nhnacademy.bookstoreorderapi.order.domain.entity.Wrapping;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WrappingTest {

    @Test
    void builderSetsFieldsCorrectly() {
        Wrapping wrap = Wrapping.builder()
                .id(1L)
                .name("Test")
                .price(1000)
                .active(true)
                .build();

        assertThat(wrap.getId()).isEqualTo(1L);
        assertThat(wrap.getName()).isEqualTo("Test");
        assertThat(wrap.getPrice()).isEqualTo(1000);
        assertThat(wrap.getActive()).isTrue();
    }

    @Test
    void setterAndGetterWork() {
        Wrapping wrap = new Wrapping();
        wrap.setId(2L);
        wrap.setName("Sample");
        wrap.setPrice(500);
        wrap.setActive(false);

        assertThat(wrap.getId()).isEqualTo(2L);
        assertThat(wrap.getName()).isEqualTo("Sample");
        assertThat(wrap.getPrice()).isEqualTo(500);
        assertThat(wrap.getActive()).isFalse();
    }
}