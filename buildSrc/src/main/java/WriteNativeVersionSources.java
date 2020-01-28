import static com.google.common.io.Files.asByteSource;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.FileVisitDetails;
import org.gradle.api.file.FileVisitor;
import org.gradle.api.file.RegularFile;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.CacheableTask;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.TaskAction;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.TreeMap;

@CacheableTask
public abstract class WriteNativeVersionSources extends DefaultTask {

    @PathSensitive(PathSensitivity.RELATIVE)
    @InputFiles
    public abstract ConfigurableFileCollection getNativeSources();

    @OutputDirectory
    public abstract DirectoryProperty getGeneratedNativeHeaderDirectory();

    @OutputDirectory
    public abstract DirectoryProperty getGeneratedJavaSourcesDir();

    @TaskAction
    public void generateVersionSources() throws IOException {
        HashCode combinedHash = hashNativeSources();
        long version = combinedHash.asLong();

        writeTextFile(
            createNativeHeaderContents(version),
            getGeneratedNativeHeaderDirectory().file("native_platform_version.h")
        );

        writeTextFile(
            createJavaInterfaceContents(version),
            getGeneratedJavaSourcesDir().file("net/rubygrapefruit/platform/internal/jni/NativeVersion.java")
        );
    }

    private HashCode hashNativeSources() {
        TreeMap<String, HashCode> sourceHashes = new TreeMap<>();
        getNativeSources().getAsFileTree().visit(new FileVisitor() {
            @Override
            public void visitDir(FileVisitDetails fileVisitDetails) {
            }

            @Override
            public void visitFile(FileVisitDetails fileVisitDetails) {
                HashCode fileHash = hashFile(fileVisitDetails);
                sourceHashes.put(fileVisitDetails.getPath(), fileHash);
            }
        });
        Hasher hasher = Hashing.sha256().newHasher();
        sourceHashes.forEach((relativePath, fileHash) -> {
            hasher.putString(relativePath, StandardCharsets.UTF_8);
            hasher.putBytes(fileHash.asBytes());
        });
        return hasher.hash();
    }

    private String createJavaInterfaceContents(long version) {
        return "/* DO NOT EDIT THIS FILE - it is machine generated */\n\n" +
            "package net.rubygrapefruit.platform.internal.jni;\n" +
            "public interface NativeVersion {\n" +
            "    long VERSION = " + version + "L;\n" +
            "}\n";
    }

    private void writeTextFile(String contents, Provider<RegularFile> targetLocation) throws IOException {
        Path path = targetLocation.get().getAsFile().toPath();
        Files.createDirectories(path.getParent());
        Files.write(path, contents.getBytes(StandardCharsets.UTF_8));
    }

    private String createNativeHeaderContents(long version) {

        return "/* DO NOT EDIT THIS FILE - it is machine generated */\n\n" +
            "#ifndef __INCLUDE_NATIVE_PLATFORM_VERSION_H__\n" +
            "#define __INCLUDE_NATIVE_PLATFORM_VERSION_H__\n" +
            "#ifdef __cplusplus\n" +
            "extern \"C\" {\n" +
            "#endif\n" +
            "#define NATIVE_VERSION " + version + "\n" +
            "#ifdef __cplusplus\n" +
            "}\n" +
            "#endif\n" +
            "#endif\n";
    }

    private HashCode hashFile(FileVisitDetails fileVisitDetails) {
        try {
            return asByteSource(fileVisitDetails.getFile()).hash(Hashing.sha256());
        } catch (IOException e) {
            throw new UncheckedIOException("Error hashing file " + fileVisitDetails.getFile().getAbsolutePath(), e);
        }
    }
}
