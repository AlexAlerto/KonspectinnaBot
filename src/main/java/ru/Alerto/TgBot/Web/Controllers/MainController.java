package ru.Alerto.TgBot.Web.Controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.Alerto.TgBot.DataBase.models.DirectoryOrFile;
import ru.Alerto.TgBot.DataBase.repo.FilesRepository;

@RestController
public class MainController {

    @Autowired
    private FilesRepository fileRepository;

    @GetMapping("/main-html")
    public Model getForSearch(Model model){
        Iterable<DirectoryOrFile> directoryOrFiles = fileRepository.findAll();
        model.addAttribute("files", directoryOrFiles);
        return model;
    }
}
