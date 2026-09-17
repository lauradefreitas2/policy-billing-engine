package br.com.insurtech.policybilling.architecture;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ArchitectureBoundaryTest {

    private static final Path DOMAIN_PATH = Path.of("src/main/java/br/com/insurtech/policybilling/domain");
    private static final Path APPLICATION_PATH = Path.of("src/main/java/br/com/insurtech/policybilling/application");
    private static final List<String> FORBIDDEN_DOMAIN_IMPORTS = List.of(
            "import org.springframework.",
            "import jakarta.",
            "import org.springframework.amqp.",
            "import org.springframework.security."
    );
    private static final List<String> FORBIDDEN_APPLICATION_IMPORTS = List.of(
            "import br.com.insurtech.policybilling.infrastructure.",
            "import org.springframework.web.",
            "import org.springframework.data.",
            "import org.springframework.amqp.",
            "import org.springframework.security."
    );

    @Test
    @DisplayName("domain layer should not depend on frameworks")
    void domainLayerShouldNotDependOnFrameworks() throws IOException {
        List<Path> domainFiles = javaFilesIn(DOMAIN_PATH);

        List<String> violations = domainFiles.stream()
                .flatMap(path -> forbiddenImportsIn(path).stream())
                .toList();

        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("application layer should not depend on infrastructure adapters")
    void applicationLayerShouldNotDependOnInfrastructureAdapters() throws IOException {
        List<String> violations = javaFilesIn(APPLICATION_PATH).stream()
                .flatMap(path -> forbiddenImportsIn(path, FORBIDDEN_APPLICATION_IMPORTS).stream())
                .toList();

        assertThat(violations).isEmpty();
    }

    private static List<Path> javaFilesIn(Path root) throws IOException {
        try (var paths = Files.walk(root)) {
            return paths
                    .filter(path -> path.toString().endsWith(".java"))
                    .toList();
        }
    }

    private static List<String> forbiddenImportsIn(Path path) {
        return forbiddenImportsIn(path, FORBIDDEN_DOMAIN_IMPORTS);
    }

    private static List<String> forbiddenImportsIn(Path path, List<String> forbiddenImports) {
        try {
            String source = Files.readString(path);
            return forbiddenImports.stream()
                    .filter(source::contains)
                    .map(forbiddenImport -> path + " contains " + forbiddenImport)
                    .toList();
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to inspect domain source file " + path, ex);
        }
    }
}
