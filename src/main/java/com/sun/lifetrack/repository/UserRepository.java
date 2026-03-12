package com.sun.lifetrack.repository;

import com.sun.lifetrack.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    //用户名查重
    Optional<User> findByUsername(String username);

    //增加忽略大小写的邮箱查询
    Optional<User> findByEmailIgnoreCase(String email);
}
