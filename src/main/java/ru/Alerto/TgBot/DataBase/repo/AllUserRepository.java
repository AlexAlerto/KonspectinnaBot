package ru.Alerto.TgBot.DataBase.repo;

import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;
import ru.Alerto.TgBot.DataBase.models.AllUser;

import java.util.Optional;

public interface AllUserRepository extends CrudRepository<AllUser, Long> {
    @Transactional
    Optional<AllUser> findByTgId(Long id);
}
