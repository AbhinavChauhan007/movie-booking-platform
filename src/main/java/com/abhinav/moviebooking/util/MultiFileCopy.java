package com.abhinav.moviebooking.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Stream;

public class MultiFileCopy {

    private static final Path SRC_ROOT = Paths.get("src");
//    private static final Path TARGET_ROOT = Paths.get("target");

    private static final Set<String> ALLOWED_EXTENSIONS =
            Set.of(".java", ".properties", ".yml", ".yaml", ".proto");

    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            System.out.println("Usage: java MultiFileCopy <outputFile> <package1> <package2> ...");
            return;
        }

        Path outputFile = Paths.get(args[0]);
        List<String> packages = Arrays.asList(Arrays.copyOfRange(args, 1, args.length));

        StringBuilder out = new StringBuilder();

        writeFromRoot(out, SRC_ROOT, packages);
//        writeFromRoot(out, TARGET_ROOT, packages);

        Files.writeString(outputFile, out.toString());
        System.out.println("Written to: " + outputFile.toAbsolutePath());
    }

    private static void writeFromRoot(StringBuilder out, Path root, List<String> packages)
            throws IOException {

        if (!Files.exists(root)) return;

        List<Path> files = new ArrayList<>();

        try (Stream<Path> stream = Files.walk(root)) {
            stream
                    .filter(Files::isRegularFile)
                    .filter(MultiFileCopy::hasAllowedExtension)
                    .filter(p -> !p.toString().contains(".idea"))
                    .filter(p -> !p.toString().contains("src/test"))
                    .filter(p -> !p.toString().endsWith(".java") || matchesPackage(p, packages))
                    .forEach(files::add);
        }


        files.sort(Comparator.comparing(Path::toString));

        for (Path file : files) {
            out.append("\n# ").append(file).append("\n");
            out.append(Files.readString(file));
        }
    }

    private static boolean hasAllowedExtension(Path p) {
        String name = p.getFileName().toString();
        for (String ext : ALLOWED_EXTENSIONS) {
            if (name.endsWith(ext)) return true;
        }
        return false;
    }

    private static boolean matchesPackage(Path file, List<String> packages) {
        String normalized = file.toString().replace(File.separatorChar, '/');
        for (String pkg : packages) {
            String pkgPath = pkg.replace('.', '/');
            if (normalized.contains(pkgPath)) return true;
        }
        return false;
    }

    private static String minify(String content) {
        // remove block & javadoc comments
        content = content.replaceAll("(?s)/\\*.*?\\*/", "");
        // remove single-line comments (safe for properties/yml/proto too)
        content = content.replaceAll("(?m)//.*$", "");
        // collapse whitespace
        content = content.replaceAll("\\s+", " ");
        return content.trim();
    }
}
