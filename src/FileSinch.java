import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class FileSinch {
    private static class ExitException extends Exception {
    }

    public static void main(String[] args) {
        Path src = null, dst = null;
        try {
            System.out.println("Введи эталонную директорию");
            src = getPath();
            System.out.println("Что синхронизировать/создать?");
            dst = getPathWhithoutCheck();
            try {
                sinch(src, dst);
            } catch (IOException e) {
                System.out.println("Ошибка:  "+e.getMessage());
            }
            System.out.println("Синхронизация прошла успешно");
        } catch (ExitException e) {
            System.out.println("Вы уже уходите? \"Баба з возу - кобыле легче \" ©");
        } catch (Exception e) {
            System.out.println("Атас! " + e.getMessage());
        }

    }

    private static void sinch(Path src, Path dst) throws IOException {
        final Path source = src;
        final Path target = dst;
        Files.walkFileTree(source, EnumSet.of(FileVisitOption.FOLLOW_LINKS), Integer.MAX_VALUE,
                new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
                            throws IOException {
                        Path targetdir = target.resolve(source.relativize(dir));
                        if (Files.exists(targetdir)) {
                            Set<String> listFilesSrc = getSet(dir);
                            Set<String> deleteFromDst = getSet(targetdir);
                            deleteFromDst.removeAll(listFilesSrc);
                            deleteAll(deleteFromDst, targetdir);
                        }
                        try {
                            Files.copy(dir, targetdir);
                        } catch (FileAlreadyExistsException e) {
                            if (!Files.isDirectory(targetdir))
                                throw e;
                        }
                        return CONTINUE;
                    }

                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                            throws IOException {
                        Path newFile = target.resolve(source.relativize(file));
                        if (false == Files.exists(newFile) || Files.getLastModifiedTime(file).compareTo(Files.getLastModifiedTime(newFile)) > 0) {
                            Files.copy(file, newFile, REPLACE_EXISTING);
                        }
                        return CONTINUE;
                    }
                });
    }

    private static Path getPath() throws ExitException {
        Path path;
        do {
            System.out.println("Директория должна существовать! Для выхода введи \"?\"");
            path = getPathWhithoutCheck();
        } while (false == path.toFile().isDirectory());
        return path;
    }

    private static Path getPathWhithoutCheck() throws ExitException {
        Scanner scanner = new Scanner(System.in);
        String pathStr = scanner.nextLine();
        if (pathStr.equals("?")) {
            throw new ExitException();
        }
        return Paths.get(pathStr);
    }

    private static Set<String> getSet(Path path) {

        Set<String> result = new TreeSet<>();
        if (path.toFile().list().length > 0) {
            Collections.addAll(result, path.toFile().list());
        }
        return result;
    }

    private static void deleteAll(Set<String> deletFromDst, Path dst) {
        for (String file : deletFromDst) {
            try {
                Files.delete(Paths.get(dst.toString(), file));
            } catch (IOException e) {
                System.out.println("Ошибка удаления в deleteAll " + e.getMessage());
            }
        }
    }
}
