package com.dochiri.kihan.application.user.query;

import com.dochiri.kihan.application.user.dto.UserDetail;
import com.dochiri.kihan.domain.user.User;
import com.dochiri.kihan.domain.user.UserRole;
import com.dochiri.kihan.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserQueryService {

    private final UserRepository userRepository;

    public UserDetail getActiveUser(Long id) {
        return UserDetail.from(userRepository.findByIdAndDeletedAtIsNull(id));
    }

    public UserDetail getActiveUserWithAccess(Long requestUserId, UserRole requestUserRole, Long targetUserId) {
        User user = userRepository.findByIdAndDeletedAtIsNull(targetUserId);
        user.verifyAccessBy(requestUserId, requestUserRole);
        return UserDetail.from(user);
    }

}