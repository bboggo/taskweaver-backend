package backend.taskweaver.domain.notification.controller;

import backend.taskweaver.domain.notification.dto.NotificationResponse;
import backend.taskweaver.domain.notification.service.NotificationService;
import backend.taskweaver.global.code.ApiResponse;
import backend.taskweaver.global.code.SuccessCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequestMapping("/v1")
@Tag(name = "알림")
@RequiredArgsConstructor
@Slf4j
@RestController
public class NotificationController {

    private final NotificationService notificationService;
    @Operation(summary =  "로그인한 유저의 알림 리스트 전체 조회")
    @GetMapping("/notification")
    public ResponseEntity<ApiResponse> AllNotification(@AuthenticationPrincipal User user) {
        ApiResponse apiResponse = ApiResponse.builder()
                .result(notificationService.getAllNotificationsForUser(Long.parseLong(user.getUsername())))
                .resultCode(SuccessCode.SELECT_SUCCESS.getStatus())
                .resultMsg(SuccessCode.SELECT_SUCCESS.getMessage())
                .build();
        return ResponseEntity.status(HttpStatus.OK).body(apiResponse);
    }

    @GetMapping("/has-unread")
    @Operation(summary = "유저의 읽지 않은 알림 존재 여부 확인")
    public ResponseEntity<ApiResponse> checkUnreadNotifications(@AuthenticationPrincipal User user) {
        Long memberId = Long.parseLong(user.getUsername());
        boolean hasUnread = notificationService.hasUnreadNotifications(memberId);

        ApiResponse apiResponse = ApiResponse.builder()
                .result(hasUnread)  // true or false 반환
                .resultCode(SuccessCode.SELECT_SUCCESS.getStatus())
                .resultMsg(SuccessCode.SELECT_SUCCESS.getMessage())
                .build();

        return ResponseEntity.status(HttpStatus.OK).body(apiResponse);
    }


}
