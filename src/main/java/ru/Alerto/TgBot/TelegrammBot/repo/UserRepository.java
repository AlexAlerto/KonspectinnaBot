package ru.Alerto.TgBot.TelegrammBot.repo;

import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;
import ru.Alerto.TgBot.TelegrammBot.models.User;

import java.util.Optional;

public interface UserRepository extends CrudRepository<User, Long>{
    @Transactional
    Optional<User> findByTgId(Long id);

    Iterable<User> findAllByDirection(String Direction);
}
