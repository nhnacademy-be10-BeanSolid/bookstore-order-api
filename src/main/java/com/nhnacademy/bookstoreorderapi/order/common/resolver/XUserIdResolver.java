package com.nhnacademy.bookstoreorderapi.order.common.resolver;

import com.nhnacademy.bookstoreorderapi.order.client.user.dto.response.UserResponse;
import com.nhnacademy.bookstoreorderapi.order.client.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class XUserIdResolver {

    private final UserService userService;

    public Long resolveUserNo(String xUserId) {
        if (isGuest(xUserId)) {
            return null;
        }

        UserResponse userInfo = userService.getUserInfo(xUserId);
        log.debug("유저 정보를 가져왔습니다: userNo={}", userInfo.userNo());
        return userInfo.userNo();
    }

    public boolean isAdmin(String xUserId) {
        if (isGuest(xUserId)) {
            return false;
        }

        return userService.getUserInfo(xUserId).isAuth();
    }

    private boolean isGuest(String xUserId) {
        if (xUserId == null || xUserId.isBlank()) {
            log.debug("비회원입니다: X-USER-ID={}", xUserId);
            return true;
        }
        return false;
    }
}
