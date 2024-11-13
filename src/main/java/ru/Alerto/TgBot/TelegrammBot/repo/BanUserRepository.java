package ru.Alerto.TgBot.TelegrammBot.repo;

import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;
import ru.Alerto.TgBot.TelegrammBot.models.BanUser;

import java.util.Optional;

public interface BanUserRepository extends CrudRepository<BanUser, Long>{
    @Transactional
    Optional<BanUser> findByTgId(Long id);
}
