package ru.Alerto.TgBot.DataBase.repo;

import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;
import ru.Alerto.TgBot.DataBase.models.BanUser;

import java.util.Optional;

public interface BanUserRepository extends CrudRepository<BanUser, Long>{
    @Transactional
    Optional<BanUser> findByTgId(Long id);
}
