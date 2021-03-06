package ru.geekbrains.persist.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.geekbrains.persist.model.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findUserByLogin(String name);
}
