package com.abhinav.moviebooking.util;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Stream;

public class MultiFileCopy {

    private static final Path MAIN_SRC = Paths.get("src/main/java");
    private static final Path TEST_SRC = Paths.get("src/test/java");

    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            System.out.println("Usage: java MultiFileCopy <outputFile> <package1> <package2> ...");
            return;
        }

        Path outputFile = Paths.get(args[0]);
        List<String> packages = Arrays.asList(Arrays.copyOfRange(args, 1, args.length));

        StringBuilder out = new StringBuilder();

        writeSection(out, "MAIN CODE", MAIN_SRC, packages);
        out.append("\n\n==================== TEST CODE ====================\n\n");
        writeSection(out, "TEST CODE", TEST_SRC, packages);

        Files.writeString(outputFile, out.toString());
        System.out.println("Written to: " + outputFile.toAbsolutePath());
    }

    private static void writeSection(StringBuilder out, String label, Path root, List<String> packages)
            throws IOException {

        List<Path> files = new ArrayList<>();

        for (String pkg : packages) {
            Path pkgPath = root.resolve(pkg.replace('.', '/'));
            if (!Files.exists(pkgPath)) continue;

            try (Stream<Path> stream = Files.walk(pkgPath)) {
                stream
                        .filter(Files::isRegularFile)
                        .filter(p -> p.toString().endsWith(".java"))
                        .filter(p -> !p.toString().contains("target"))
                        .filter(p -> !p.toString().contains(".idea"))
                        .forEach(files::add);
            }
        }

        files.sort(Comparator.comparing(Path::toString));

        for (Path file : files) {
            out.append("--- ").append(file.getFileName()).append(" ---\n");
            out.append(minify(Files.readString(file))).append("\n\n");
        }
    }

    // Removes comments + blank lines
    private static String minify(String code) {
        // Remove block & javadoc comments
        code = code.replaceAll("(?s)/\\*.*?\\*/", "");
        // Remove single-line comments
        code = code.replaceAll("(?m)//.*$", "");
        // Remove blank lines
        code = code.replaceAll("(?m)^\\s*$\\n", "");
        return code.trim();
    }
}
