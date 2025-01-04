package ru.Alerto.TgBot.DataBase.repo;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;
import ru.Alerto.TgBot.DataBase.models.DirectoryOrFile;

import java.util.Optional;

public interface FilesRepository extends CrudRepository<DirectoryOrFile, Long>{

    @Transactional
    Optional<DirectoryOrFile> findByPath(String path);

    @Transactional
    Optional<DirectoryOrFile> findById(Long id);

    @Transactional
    @Modifying
    @Query(value = "ALTER TABLE directory_or_file AUTO_INCREMENT = 1", nativeQuery = true)
    void resetAutoIncrement();

}
