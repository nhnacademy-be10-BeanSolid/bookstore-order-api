package com.nhnacademy.bookstoreorderapi.order.repository.impl;

import com.nhnacademy.bookstoreorderapi.order.domain.entity.*;
import com.nhnacademy.bookstoreorderapi.order.dto.response.OrderSummaryResponse;
import com.nhnacademy.bookstoreorderapi.order.dto.response.PurchaseVerificationResponse;
import com.nhnacademy.bookstoreorderapi.order.dto.response.UserOrderAmountResponse;
import com.nhnacademy.bookstoreorderapi.order.repository.CustomOrderRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@DataJpaTest
@Import({com.nhnacademy.bookstoreorderapi.order.common.config.QuerydslConfig.class})
class CustomOrderRepositoryImplTest {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private JPAQueryFactory jpaQueryFactory;

    private CustomOrderRepository customOrderRepository;

    @BeforeEach
    void setUp() {
        customOrderRepository = new CustomOrderRepositoryImpl(jpaQueryFactory);
        
        // 포장지 데이터 생성
        Wrapping basicWrapping = new Wrapping();
        basicWrapping.setName("기본 포장지");
        basicWrapping.setPrice(1000);
        basicWrapping.setIsActive(true);
        
        Wrapping premiumWrapping = new Wrapping();
        premiumWrapping.setName("프리미엄 포장지");
        premiumWrapping.setPrice(3000);
        premiumWrapping.setIsActive(true);
        
        entityManager.persist(basicWrapping);
        entityManager.persist(premiumWrapping);
        entityManager.flush();
        
        // 테스트 데이터 생성
        ShippingInfo shippingInfo1 = new ShippingInfo("홍길동", "01012345678", "서울특별시 강남구", LocalDate.now().plusDays(1), 3000);
        ShippingInfo shippingInfo2 = new ShippingInfo("김철수", "01087654321", "서울특별시 서초구", LocalDate.now().plusDays(2), 3000);
        ShippingInfo shippingInfo3 = new ShippingInfo("이영희", "01055559999", "서울특별시 종로구", LocalDate.now().plusDays(1), 0);
        ShippingInfo shippingInfo4 = new ShippingInfo("박민수", "01044447777", "서울특별시 마포구", LocalDate.now().plusDays(3), 3000);
        
        // 주문 생성
        Order order1 = Order.builder()
                .userNo(1L)
                .status(OrderStatus.PENDING)
                .orderDate(LocalDate.now().minusDays(10))
                .totalPrice(50000L)
                .shippingInfo(shippingInfo1)
                .build();
        
        Order order2 = Order.builder()
                .userNo(1L)
                .status(OrderStatus.SHIPPING)
                .orderDate(LocalDate.now().minusDays(20))
                .totalPrice(70000L)
                .shippingInfo(shippingInfo2)
                .build();
        
        Order order3 = Order.builder()
                .userNo(2L)
                .status(OrderStatus.COMPLETED)
                .orderDate(LocalDate.now().minusDays(30))
                .totalPrice(30000L)
                .shippingInfo(shippingInfo3)
                .build();
        
        Order order4 = Order.builder()
                .userNo(2L)
                .status(OrderStatus.PENDING)
                .orderDate(LocalDate.now().minusDays(40))
                .totalPrice(40000L)
                .shippingInfo(shippingInfo4)
                .build();
        
        Order oldOrder = Order.builder()
                .userNo(1L)
                .status(OrderStatus.COMPLETED)
                .orderDate(LocalDate.now().minusDays(100))
                .totalPrice(100000L)
                .shippingInfo(shippingInfo1)
                .build();
        
        Order canceledOrder = Order.builder()
                .userNo(1L)
                .status(OrderStatus.CANCELED)
                .orderDate(LocalDate.now().minusDays(5))
                .totalPrice(25000L)
                .shippingInfo(shippingInfo1)
                .build();
        
        entityManager.persist(order1);
        entityManager.persist(order2);
        entityManager.persist(order3);
        entityManager.persist(order4);
        entityManager.persist(oldOrder);
        entityManager.persist(canceledOrder);
        entityManager.flush();
        
        // 주문 아이템 생성
        OrderItem orderItem1 = OrderItem.builder()
                .bookId(1L)
                .unitPrice(20000)
                .quantity(2)
                .order(order1)
                .wrapping(basicWrapping)
                .build();
        
        OrderItem orderItem2 = OrderItem.builder()
                .bookId(2L)
                .unitPrice(30000)
                .quantity(2)
                .order(order2)
                .wrapping(premiumWrapping)
                .build();
        
        OrderItem orderItem3 = OrderItem.builder()
                .bookId(3L)
                .unitPrice(25000)
                .quantity(1)
                .order(order3)
                .wrapping(null)
                .build();
        
        OrderItem orderItem4 = OrderItem.builder()
                .bookId(4L)
                .unitPrice(35000)
                .quantity(1)
                .order(order4)
                .wrapping(basicWrapping)
                .build();
        
        OrderItem oldOrderItem = OrderItem.builder()
                .bookId(5L)
                .unitPrice(45000)
                .quantity(2)
                .order(oldOrder)
                .wrapping(premiumWrapping)
                .build();
        
        OrderItem canceledOrderItem = OrderItem.builder()
                .bookId(6L)
                .unitPrice(20000)
                .quantity(1)
                .order(canceledOrder)
                .wrapping(null)
                .build();
        
        entityManager.persist(orderItem1);
        entityManager.persist(orderItem2);
        entityManager.persist(orderItem3);
        entityManager.persist(orderItem4);
        entityManager.persist(oldOrderItem);
        entityManager.persist(canceledOrderItem);
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("회원별 주문 요약 조회 - 페이징 적용")
    void findOrderSummary_withPaging_success() {
        // given
        Long userNo = 1L;
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<OrderSummaryResponse> result = customOrderRepository.findOrderSummary(userNo, pageable);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(4);
        assertThat(result.getTotalElements()).isEqualTo(4);
        assertThat(result.getNumber()).isEqualTo(0);
        assertThat(result.getSize()).isEqualTo(10);
    }

    @Test
    @DisplayName("회원별 주문 요약 조회 - 페이징 제한")
    void findOrderSummary_withLimitedPaging() {
        // given
        Long userNo = 1L;
        Pageable pageable = PageRequest.of(0, 1);

        // when
        Page<OrderSummaryResponse> result = customOrderRepository.findOrderSummary(userNo, pageable);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(4);
        assertThat(result.hasNext()).isTrue();
    }

    @Test
    @DisplayName("존재하지 않는 회원의 주문 조회")
    void findOrderSummary_nonExistentUser() {
        // given
        Long userNo = 999L;
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<OrderSummaryResponse> result = customOrderRepository.findOrderSummary(userNo, pageable);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(0);
    }

    @Test
    @DisplayName("최근 3개월간 사용자별 주문 금액 조회에 성공한다")
    void findOrderAmountGroupByUserLastThreeMonths_success() {
        // when
        List<UserOrderAmountResponse> result = customOrderRepository.findOrderAmountGroupByUserLastThreeMonths();

        // then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        
        UserOrderAmountResponse user1Response = result.stream()
                .filter(response -> response.userNo().equals(1L))
                .findFirst()
                .orElse(null);
        assertThat(user1Response).isNotNull();
        assertThat(user1Response.pureOrderAmount()).isEqualTo(100000L);
        
        UserOrderAmountResponse user2Response = result.stream()
                .filter(response -> response.userNo().equals(2L))
                .findFirst()
                .orElse(null);
        assertThat(user2Response).isNotNull();
        assertThat(user2Response.pureOrderAmount()).isEqualTo(60000L);
    }

    @Test
    @DisplayName("최근 3개월간 주문이 없을 때 빈 리스트를 반환한다")
    void findOrderAmountGroupByUserLastThreeMonths_emptyResult() {
        // given - 모든 테스트 데이터 삭제
        entityManager.createQuery("DELETE FROM OrderItem").executeUpdate();
        entityManager.createQuery("DELETE FROM Order").executeUpdate();
        entityManager.flush();
        entityManager.clear();

        // when
        List<UserOrderAmountResponse> result = customOrderRepository.findOrderAmountGroupByUserLastThreeMonths();

        // then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("CANCELED 상태의 주문은 집계에서 제외된다")
    void findOrderAmountGroupByUserLastThreeMonths_excludesCanceledOrders() {
        // given - 추가 테스트 데이터 생성
        ShippingInfo shippingInfo = new ShippingInfo("테스트", "01011111111", "테스트 주소", LocalDate.now().plusDays(1), 3000);
        
        Order canceledOrder = Order.builder()
                .userNo(3L)
                .status(OrderStatus.CANCELED)
                .orderDate(LocalDate.now().minusDays(5))
                .totalPrice(50000L)
                .shippingInfo(shippingInfo)
                .build();
        
        entityManager.persist(canceledOrder);
        entityManager.flush();
        entityManager.clear();

        // when
        List<UserOrderAmountResponse> result = customOrderRepository.findOrderAmountGroupByUserLastThreeMonths();

        // then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2); // 사용자 3번은 포함되지 않음
        assertThat(result.stream().noneMatch(response -> response.userNo().equals(3L))).isTrue();
    }

    @Test
    @DisplayName("3개월 이전 주문은 집계에서 제외된다")
    void findOrderAmountGroupByUserLastThreeMonths_excludesOldOrders() {
        // given - 추가 테스트 데이터 생성
        ShippingInfo shippingInfo = new ShippingInfo("테스트", "01011111111", "테스트 주소", LocalDate.now().plusDays(1), 3000);
        
        Order veryOldOrder = Order.builder()
                .userNo(4L)
                .status(OrderStatus.COMPLETED)
                .orderDate(LocalDate.now().minusDays(200))
                .totalPrice(1000000L)
                .shippingInfo(shippingInfo)
                .build();
        
        entityManager.persist(veryOldOrder);
        entityManager.flush();
        entityManager.clear();

        // when
        List<UserOrderAmountResponse> result = customOrderRepository.findOrderAmountGroupByUserLastThreeMonths();

        // then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2); // 사용자 4번은 포함되지 않음
        assertThat(result.stream().noneMatch(response -> response.userNo().equals(4L))).isTrue();
    }

    @Test
    @DisplayName("단일 사용자의 주문 금액 조회에 성공한다")
    void findOrderAmountGroupByUserLastThreeMonths_singleUser() {
        // given - 기존 데이터 삭제하고 단일 사용자만 남김
        entityManager.createQuery("DELETE FROM OrderItem").executeUpdate();
        entityManager.createQuery("DELETE FROM Order").executeUpdate();
        
        ShippingInfo shippingInfo = new ShippingInfo("홍길동", "01012345678", "서울특별시 강남구", LocalDate.now().plusDays(1), 3000);
        
        Order singleOrder = Order.builder()
                .userNo(1L)
                .status(OrderStatus.COMPLETED)
                .orderDate(LocalDate.now().minusDays(10))
                .totalPrice(150000L)
                .shippingInfo(shippingInfo)
                .build();
        
        entityManager.persist(singleOrder);
        entityManager.flush();
        
        // 단일 사용자 테스트용 OrderItem 생성
        OrderItem singleOrderItem = OrderItem.builder()
                .bookId(1L)
                .unitPrice(75000)
                .quantity(2)
                .order(singleOrder)
                .wrapping(null)
                .build();
        
        entityManager.persist(singleOrderItem);
        entityManager.flush();
        entityManager.clear();

        // when
        List<UserOrderAmountResponse> result = customOrderRepository.findOrderAmountGroupByUserLastThreeMonths();

        // then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().userNo()).isEqualTo(1L);
        assertThat(result.getFirst().pureOrderAmount()).isEqualTo(150000L); // 75000 * 2 = 150000
    }

    // ========== 구매 확인 테스트 ==========

    @Test
    @DisplayName("구매 확인 성공 - COMPLETED 상태 주문에서 구매 이력 확인")
    void findByUserNoAndBookId_success_completedOrder() {
        // given
        Long userNo = 1L;
        Long bookId = 1L;

        // when
        PurchaseVerificationResponse result = customOrderRepository.findByUserNoAndBookId(userNo, bookId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getUserNo()).isEqualTo(userNo);
        assertThat(result.getBookId()).isEqualTo(bookId);
        assertThat(result.getIsValid()).isTrue();
    }

    @Test
    @DisplayName("구매 확인 성공 - PENDING 상태 주문에서 구매 이력 확인")
    void findByUserNoAndBookId_success_pendingOrder() {
        // given
        Long userNo = 1L;
        Long bookId = 1L;

        // when
        PurchaseVerificationResponse result = customOrderRepository.findByUserNoAndBookId(userNo, bookId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getUserNo()).isEqualTo(userNo);
        assertThat(result.getBookId()).isEqualTo(bookId);
        assertThat(result.getIsValid()).isTrue();
    }

    @Test
    @DisplayName("구매 확인 성공 - SHIPPING 상태 주문에서 구매 이력 확인")
    void findByUserNoAndBookId_success_shippingOrder() {
        // given
        Long userNo = 1L;
        Long bookId = 2L;

        // when
        PurchaseVerificationResponse result = customOrderRepository.findByUserNoAndBookId(userNo, bookId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getUserNo()).isEqualTo(userNo);
        assertThat(result.getBookId()).isEqualTo(bookId);
        assertThat(result.getIsValid()).isTrue();
    }

    @Test
    @DisplayName("구매 확인 실패 - 존재하지 않는 책")
    void findByUserNoAndBookId_fail_nonExistentBook() {
        // given
        Long userNo = 1L;
        Long bookId = 999L; // 존재하지 않는 책

        // when
        PurchaseVerificationResponse result = customOrderRepository.findByUserNoAndBookId(userNo, bookId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getUserNo()).isEqualTo(userNo);
        assertThat(result.getBookId()).isEqualTo(bookId);
        assertThat(result.getIsValid()).isFalse();
    }

    @Test
    @DisplayName("구매 확인 실패 - 존재하지 않는 사용자")
    void findByUserNoAndBookId_fail_nonExistentUser() {
        // given
        Long userNo = 999L; // 존재하지 않는 사용자
        Long bookId = 1L;

        // when
        PurchaseVerificationResponse result = customOrderRepository.findByUserNoAndBookId(userNo, bookId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getUserNo()).isEqualTo(userNo);
        assertThat(result.getBookId()).isEqualTo(bookId);
        assertThat(result.getIsValid()).isFalse();
    }

    @Test
    @DisplayName("구매 확인 실패 - 다른 사용자의 구매 이력")
    void findByUserNoAndBookId_fail_differentUser() {
        // given
        Long userNo = 1L;
        Long bookId = 3L; // 사용자 2번이 구매한 책

        // when
        PurchaseVerificationResponse result = customOrderRepository.findByUserNoAndBookId(userNo, bookId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getUserNo()).isEqualTo(userNo);
        assertThat(result.getBookId()).isEqualTo(bookId);
        assertThat(result.getIsValid()).isFalse();
    }

    @Test
    @DisplayName("구매 확인 실패 - CANCELED 상태 주문은 제외")
    void findByUserNoAndBookId_fail_canceledOrder() {
        // given
        Long userNo = 1L;
        Long bookId = 6L; // 취소된 주문의 책

        // when
        PurchaseVerificationResponse result = customOrderRepository.findByUserNoAndBookId(userNo, bookId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getUserNo()).isEqualTo(userNo);
        assertThat(result.getBookId()).isEqualTo(bookId);
        assertThat(result.getIsValid()).isFalse();
    }

    @Test
    @DisplayName("구매 확인 - 다른 사용자의 유효한 구매 이력")
    void findByUserNoAndBookId_success_differentUserValidPurchase() {
        // given
        Long userNo = 2L;
        Long bookId = 3L; // 사용자 2번이 실제로 구매한 책

        // when
        PurchaseVerificationResponse result = customOrderRepository.findByUserNoAndBookId(userNo, bookId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getUserNo()).isEqualTo(userNo);
        assertThat(result.getBookId()).isEqualTo(bookId);
        assertThat(result.getIsValid()).isTrue();
    }

    @Test
    @DisplayName("구매 확인 - 다양한 주문 상태가 혼재된 상황에서 유효한 상태만 확인")
    void findByUserNoAndBookId_multipleOrderStates() {
        // given - 동일한 사용자와 책에 대해 여러 상태의 주문 생성
        ShippingInfo additionalShippingInfo = new ShippingInfo("테스트", "01099999999", "테스트 주소", LocalDate.now().plusDays(1), 3000);
        Long userNo = 3L;
        Long bookId = 600L;
        
        // 취소된 주문
        Order canceledOrderForTest = Order.builder()
                .userNo(userNo)
                .status(OrderStatus.CANCELED)
                .orderDate(LocalDate.now().minusDays(10))
                .totalPrice(20000L)
                .shippingInfo(additionalShippingInfo)
                .build();
        
        // 완료된 주문
        Order completedOrderForTest = Order.builder()
                .userNo(userNo)
                .status(OrderStatus.COMPLETED)
                .orderDate(LocalDate.now().minusDays(5))
                .totalPrice(30000L)
                .shippingInfo(additionalShippingInfo)
                .build();
        
        entityManager.persist(canceledOrderForTest);
        entityManager.persist(completedOrderForTest);
        entityManager.flush();
        
        // 취소된 주문의 아이템
        OrderItem canceledItem = OrderItem.builder()
                .bookId(bookId)
                .unitPrice(20000)
                .quantity(1)
                .order(canceledOrderForTest)
                .wrapping(null)
                .build();
        
        // 완료된 주문의 아이템
        OrderItem completedItem = OrderItem.builder()
                .bookId(bookId)
                .unitPrice(30000)
                .quantity(1)
                .order(completedOrderForTest)
                .wrapping(null)
                .build();
        
        entityManager.persist(canceledItem);
        entityManager.persist(completedItem);
        entityManager.flush();
        entityManager.clear();

        // when
        PurchaseVerificationResponse result = customOrderRepository.findByUserNoAndBookId(userNo, bookId);

        // then - 취소된 주문이 있어도 완료된 주문이 있으면 true
        assertThat(result).isNotNull();
        assertThat(result.getUserNo()).isEqualTo(userNo);
        assertThat(result.getBookId()).isEqualTo(bookId);
        assertThat(result.getIsValid()).isTrue();
    }
}