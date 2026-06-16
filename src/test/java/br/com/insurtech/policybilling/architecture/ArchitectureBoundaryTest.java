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
    private static final List<String> FORBIDDEN_DOMAIN_IMPORTS = List.of(
            "import org.springframework.",
            "import jakarta.",
            "import org.springframework.amqp.",
            "import org.springframework.security."
    );

    @Test
    @DisplayName("domain layer should not depend on frameworks")
    void domainLayerShouldNotDependOnFrameworks() throws IOException {
        List<Path> domainFiles;
        try (var paths = Files.walk(DOMAIN_PATH)) {
            domainFiles = paths
                    .filter(path -> path.toString().endsWith(".java"))
                    .toList();
        }

        List<String> violations = domainFiles.stream()
                .flatMap(path -> forbiddenImportsIn(path).stream())
                .toList();

        assertThat(violations).isEmpty();
    }

    private static List<String> forbiddenImportsIn(Path path) {
        try {
            String source = Files.readString(path);
            return FORBIDDEN_DOMAIN_IMPORTS.stream()
                    .filter(source::contains)
                    .map(forbiddenImport -> path + " contains " + forbiddenImport)
                    .toList();
        } catch (IOException ex) {
            throw new IllegalStateException("Unable to inspect domain source file " + path, ex);
        }
    }
}
