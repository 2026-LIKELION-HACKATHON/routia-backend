package com.routiaback.home.application;

import com.routiaback.auth.application.port.UserRepositoryPort;
import com.routiaback.auth.domain.User;
import com.routiaback.global.error.ApiException;
import com.routiaback.global.error.ErrorCode;
import com.routiaback.home.application.result.HomeResult;
import com.routiaback.home.application.result.HomeResult.TaskPreview;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class HomeService {

    private final UserRepositoryPort userRepository;

    // TODO: 루틴 서비스 생기면 아래 더미 데이터를 실제 조회 로직으로 교체

    public HomeService(UserRepositoryPort userRepository) {
        this.userRepository = userRepository;
    }

    public HomeResult getHome(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND));

        // TODO: 실제 오늘 루틴 진행률/할일 조회로 교체
        List<TaskPreview> dummyTasks = List.of(
                new TaskPreview(1L, "림프 마사지 5분", true),
                new TaskPreview(2L, "수분 크림과 자외선 차단제 바르기", true),
                new TaskPreview(3L, "단백질 위주의 아침식사", false)
        );

        return new HomeResult(
                user.name(),
                LocalDate.now(),
                80,
                4,
                7,
                dummyTasks
        );
    }
}