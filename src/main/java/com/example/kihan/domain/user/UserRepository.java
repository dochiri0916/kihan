package com.example.kihan.domain.user;

public interface UserRepository {

    User save(User user);

    User findByIdAndDeletedAtIsNull(Long id);

    User findByEmailAndDeletedAtIsNull(String email);

    boolean existsByEmailAndDeletedAtIsNull(String email);

}