package com.nhnacademy.bookstoreorderapi.order.common.resolver;

import com.nhnacademy.bookstoreorderapi.order.client.user.dto.response.UserResponse;
import com.nhnacademy.bookstoreorderapi.order.client.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class XUserIdResolverTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private XUserIdResolver xUserIdResolver;

    private UserResponse userResponse;
    private UserResponse adminResponse;

    @BeforeEach
    void setUp() {
        String userId = "testUser";
        userResponse = new UserResponse(1L, userId, "", "", "", "", LocalDate.now(), 0, false, "", LocalDateTime.now(), LocalDateTime.now(), "");

        String adminId = "adminUser";
        adminResponse = new UserResponse(2L, adminId, "", "", "", "", LocalDate.now(), 0, true, "", LocalDateTime.now(), LocalDateTime.now(), "");
    }

    @Test
    @DisplayName("정상 회원 유저 번호 조회 - 성공")
    void resolveUserNo_ValidUser_ReturnsUserNo() {
        // given
        String xUserId = "validUser";
        given(userService.getUserInfo(xUserId)).willReturn(userResponse);

        // when
        Long result = xUserIdResolver.resolveUserNo(xUserId);

        // then
        assertEquals(1L, result);
        verify(userService).getUserInfo(xUserId);
    }

    @ParameterizedTest
    @DisplayName("비회원 유저 번호 조회 - null 반환")
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t", "\n"})
    void resolveUserNo_GuestUser_ReturnsNull(String xUserId) {
        // when
        Long result = xUserIdResolver.resolveUserNo(xUserId);

        // then
        assertNull(result);
        verify(userService, never()).getUserInfo(anyString());
    }

    @Test
    @DisplayName("관리자 권한 확인 - 관리자인 경우 true 반환")
    void isAdmin_AdminUser_ReturnsTrue() {
        // given
        String xUserId = "adminUser";
        given(userService.getUserInfo(xUserId)).willReturn(adminResponse);

        // when
        boolean result = xUserIdResolver.isAdmin(xUserId);

        // then
        assertTrue(result);
        verify(userService).getUserInfo(xUserId);
    }

    @Test
    @DisplayName("관리자 권한 확인 - 일반 회원인 경우 false 반환")
    void isAdmin_RegularUser_ReturnsFalse() {
        // given
        String xUserId = "regularUser";
        given(userService.getUserInfo(xUserId)).willReturn(userResponse);

        // when
        boolean result = xUserIdResolver.isAdmin(xUserId);

        // then
        assertFalse(result);
        verify(userService).getUserInfo(xUserId);
    }

    @ParameterizedTest
    @DisplayName("관리자 권한 확인 - 비회원인 경우 false 반환")
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t", "\n"})
    void isAdmin_GuestUser_ReturnsFalse(String xUserId) {
        // when
        boolean result = xUserIdResolver.isAdmin(xUserId);

        // then
        assertFalse(result);
        verify(userService, never()).getUserInfo(anyString());
    }

    @Test
    @DisplayName("다양한 회원 유저 번호 조회 테스트")
    void resolveUserNo_VariousValidUsers() {
        // given
        String userId2 = "user2";
        UserResponse user2Response = new UserResponse(200L, userId2, "", "", "", "", LocalDate.now(), 0, false, "", LocalDateTime.now(), LocalDateTime.now(), "");

        given(userService.getUserInfo(userResponse.userId())).willReturn(userResponse);
        given(userService.getUserInfo(userId2)).willReturn(user2Response);

        // when
        Long result1 = xUserIdResolver.resolveUserNo(userResponse.userId());
        Long result2 = xUserIdResolver.resolveUserNo(userId2);

        // then
        assertEquals(1L, result1);
        assertEquals(200L, result2);
        verify(userService).getUserInfo(userResponse.userId());
        verify(userService).getUserInfo(userId2);
    }

    @Test
    @DisplayName("다양한 관리자 권한 확인 테스트")
    void isAdmin_VariousUsers() {
        // given
        given(userService.getUserInfo(adminResponse.userId())).willReturn(adminResponse);
        given(userService.getUserInfo(userResponse.userId())).willReturn(userResponse);

        // when
        boolean adminResult = xUserIdResolver.isAdmin(adminResponse.userId());
        boolean userResult = xUserIdResolver.isAdmin(userResponse.userId());

        // then
        assertTrue(adminResult);
        assertFalse(userResult);
        verify(userService).getUserInfo(adminResponse.userId());
        verify(userService).getUserInfo(userResponse.userId());
    }
}