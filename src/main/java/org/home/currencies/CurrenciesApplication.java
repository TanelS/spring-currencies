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

@SpringBootApplication
@RestController
public class CurrenciesApplication {

    public static void main(String[] args) {
        loadEnvFile();
        SpringApplication.run(CurrenciesApplication.class, args);
    }


    /**
     * Loads environment variables from a `.env` file located in the project's root directory.
     * The root directory is determined by traversing the directory structure upwards
     * from the directory of the running application, looking for a "build.gradle" file.
     *
     * If a `.env` file is present in the root directory, the key-value pairs defined in the file
     * are loaded as system properties. The method also gracefully handles cases
     * where the `.env` file is missing or malformed, ensuring the application can continue running.
     *
     * This method is useful for injecting environment-specific configuration at runtime
     * without explicitly specifying environment variables in the operating system.
     */
    public static void loadEnvFile() {
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
