package ru.Alerto.TgBot.DataBase;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;
import ru.Alerto.TgBot.DataBase.models.DirectoryOrFile;
import ru.Alerto.TgBot.DataBase.repo.FilesRepository;
import ru.Alerto.TgBot.DataBase.repo.UserRepository;
import ru.Alerto.TgBot.TelegrammBot.bot.KonspectinnaBot;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class DataBase {

    private static DataBase instance;

    private final FilesRepository filesRepository;
    private final UserRepository userRepository;

    public DataBase(FilesRepository filesRepository, UserRepository userRepository) {
        this.filesRepository = filesRepository;
        this.userRepository = userRepository;
    }

    public static void userSaveOnDB(Long userId) {
        instance.userRepository.findByTgId(userId).ifPresent(user -> {
            user.setLastSeen(LocalDateTime.now());
            instance.userRepository.save(user);
        });
    }

    public static void searchAndSaveFilesOnDB() {
        instance.filesRepository.deleteAll();
        instance.filesRepository.resetAutoIncrement();

        Path rootDir = Paths.get("./files");
        List<DirectoryOrFile> directoryOrFiles = new ArrayList<>();

        try {
            Files.walkFileTree(rootDir, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                    directoryOrFiles.add(new DirectoryOrFile("./files/" + rootDir.relativize(dir).toString().replace("\\", "/")));
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    directoryOrFiles.add(new DirectoryOrFile("./files/" + rootDir.relativize(file).toString().replace("\\", "/")));
                    return FileVisitResult.CONTINUE;
                }
            });

            instance.filesRepository.saveAll(directoryOrFiles);

        } catch (IOException e) {
            KonspectinnaBot.LOG.error("Ошибка сканирования файлов и папок", e);
        }
    }

    @PostConstruct
    public void init() {
        instance = this;
    }
}
