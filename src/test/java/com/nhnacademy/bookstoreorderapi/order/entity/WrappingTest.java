package com.nhnacademy.bookstoreorderapi.order.entity;


import com.nhnacademy.bookstoreorderapi.order.domain.entity.Wrapping;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WrappingTest {

    @Test
    void builderSetsFieldsCorrectly() {
        Wrapping wrap = Wrapping.builder()
                .wrappingId(1L)
                .name("Test")
                .price(1000)
                .active(true)
                .build();

        assertThat(wrap.getWrappingId()).isEqualTo(1L);
        assertThat(wrap.getName()).isEqualTo("Test");
        assertThat(wrap.getPrice()).isEqualTo(1000);
        assertThat(wrap.isActive()).isTrue();
    }

    @Test
    void setterAndGetterWork() {
        Wrapping wrap = new Wrapping();
        wrap.setWrappingId(2L);
        wrap.setName("Sample");
        wrap.setPrice(500);
//        wrap.isActive();

        assertThat(wrap.getWrappingId()).isEqualTo(2L);
        assertThat(wrap.getName()).isEqualTo("Sample");
        assertThat(wrap.getPrice()).isEqualTo(500);
        assertThat(wrap.isActive()).isFalse();
    }
}