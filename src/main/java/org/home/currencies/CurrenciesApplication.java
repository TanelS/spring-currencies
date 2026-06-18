package org.home.currencies;

import io.github.cdimascio.dotenv.Dotenv;
import io.github.cdimascio.dotenv.DotenvEntry;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.system.ApplicationHome;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.nio.file.Path;

@SpringBootApplication
@RestController
public class CurrenciesApplication {

    public static void main(String[] args) {
        File projectRoot = findProjectRoot(new ApplicationHome(CurrenciesApplication.class).getDir());

        Dotenv dotenv = Dotenv.configure()
                .directory(projectRoot.getAbsolutePath())
                .filename(".env")
                .ignoreIfMalformed()
                .ignoreIfMissing()
                .load();

        for (DotenvEntry entry : dotenv.entries()) {
            System.setProperty(entry.getKey(), entry.getValue());
        }

        SpringApplication.run(CurrenciesApplication.class, args);
    }

    /**
     * Searches for the root directory of the project by looking for a directory containing a "build.gradle" file.
     * Traverses up the directory structure starting from the given directory.
     *
     * @param startDir the starting directory to begin the search
     * @return the directory containing the "build.gradle" file if found,
     *         or the starting directory if no such file exists in the directory hierarchy
     */
    private static File findProjectRoot(File startDir) {
        for (File dir = startDir; dir != null; dir = dir.getParentFile()) {
            if (new File(dir, "build.gradle").exists()) {
                return dir;
            }
        }
        return startDir;
    }

    @GetMapping("/hello")
    public String hello(@RequestParam(value = "name", defaultValue = "World") String name) {
        return String.format("Hello %s!", name);
    }

}
