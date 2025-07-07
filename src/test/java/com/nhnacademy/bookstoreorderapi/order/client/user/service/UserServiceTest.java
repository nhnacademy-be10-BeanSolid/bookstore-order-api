package com.nhnacademy.bookstoreorderapi.order.client.user.service;

import com.nhnacademy.bookstoreorderapi.order.client.ExternalServiceException;
import com.nhnacademy.bookstoreorderapi.order.client.user.UserServiceClient;
import com.nhnacademy.bookstoreorderapi.order.client.user.dto.UserResponse;
import com.nhnacademy.bookstoreorderapi.order.client.user.exception.UserNotFoundException;
import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserServiceClient userServiceClient;

    @InjectMocks
    private UserService userService;

    private UserResponse mockUserResponse;

    @BeforeEach
    void setUp() {
        mockUserResponse = UserResponse.builder()
                .userNo(1L)
                .userId("testUser")
                .userPassword("password")
                .userName("테스트유저")
                .userPhoneNumber("010-1234-5678")
                .userEmail("test@example.com")
                .userBirth(LocalDate.of(1990, 1, 1))
                .userPoint(1000)
                .isAuth(true)
                .userStatus("ACTIVE")
                .createdAt(LocalDateTime.now())
                .lastLoginAt(LocalDateTime.now())
                .userGradeName("GOLD")
                .build();
    }

    @Test
    @DisplayName("사용자 정보 조회 성공")
    void getUserInfo_Success() {
        // Given
        String userId = "testUser";
        when(userServiceClient.getUserInfo(userId)).thenReturn(mockUserResponse);

        // When
        UserResponse result = userService.getUserInfo(userId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result)
                .extracting("userNo", "userId", "userName", "isAuth")
                .containsExactly(1L, "testUser", "테스트유저", true);
        assertThat(result.isAuth()).isTrue();
        verify(userServiceClient, times(1)).getUserInfo(userId);
    }

    @Test
    @DisplayName("사용자를 찾을 수 없는 경우 FeignException.NotFound 발생")
    void getUserInfo_UserNotFound_ThrowsFeignException() {
        // Given
        String userId = "nonExistentUser";
        FeignException.NotFound notFoundException = mock(FeignException.NotFound.class);
        when(userServiceClient.getUserInfo(userId)).thenThrow(notFoundException);

        // When & Then
        assertThatThrownBy(() -> userService.getUserInfo(userId))
                .isInstanceOf(FeignException.NotFound.class);
        verify(userServiceClient, times(1)).getUserInfo(userId);
    }

    @Test
    @DisplayName("외부 서비스 오류 시 FeignException 발생")
    void getUserInfo_ExternalServiceError_ThrowsFeignException() {
        // Given
        String userId = "testUser";
        FeignException serverError = mock(FeignException.class);
        when(userServiceClient.getUserInfo(userId)).thenThrow(serverError);

        // When & Then
        assertThatThrownBy(() -> userService.getUserInfo(userId))
                .isInstanceOf(FeignException.class);
        verify(userServiceClient, times(1)).getUserInfo(userId);
    }

    @Test
    @DisplayName("fallback 메서드 - 일반 예외 처리")
    void fallbackGetUserInfo_GeneralException_ThrowsExternalServiceException() {
        // Given
        String userId = "testUser";
        RuntimeException generalException = new RuntimeException("Service unavailable");

        // When & Then
        assertThatThrownBy(() -> userService.fallbackGetUserInfo(userId, generalException))
                .isInstanceOf(ExternalServiceException.class)
                .hasMessage("UserServiceClient 에러 발생")
                .hasCause(generalException);
    }

    @Test
    @DisplayName("fallback 메서드 - NotFound 예외 처리")
    void fallbackGetUserInfo_NotFound_ThrowsUserNotFoundException() {
        // Given
        String userId = "testUser";
        FeignException.NotFound notFoundException = mock(FeignException.NotFound.class);

        // When & Then
        assertThatThrownBy(() -> userService.fallbackGetUserInfo(userId, notFoundException))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("유저를 찾을 수 없습니다 - userId: " + userId);
    }


    @Test
    @DisplayName("fallback 메서드 - FeignException 처리")
    void fallbackGetUserInfo_FeignException_ThrowsExternalServiceException() {
        // Given
        String userId = "testUser";
        FeignException.ServiceUnavailable serviceUnavailableException = mock(FeignException.ServiceUnavailable.class);
        when(serviceUnavailableException.getMessage()).thenReturn("Service Unavailable");

        // When & Then
        assertThatThrownBy(() -> userService.fallbackGetUserInfo(userId, serviceUnavailableException))
                .isInstanceOf(ExternalServiceException.class)
                .hasMessage("UserServiceClient 에러 발생")
                .hasCause(serviceUnavailableException);
    }
}
