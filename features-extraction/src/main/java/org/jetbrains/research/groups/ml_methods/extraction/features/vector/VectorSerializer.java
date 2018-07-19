package org.jetbrains.research.groups.ml_methods.extraction.features.vector;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public final class VectorSerializer {
    private static final @NotNull VectorSerializer INSTANCE = new VectorSerializer();

    private static final @NotNull Logger LOGGER = Logger.getLogger(VectorSerializer.class);

    static {
        LOGGER.setLevel(Level.DEBUG);
        LOGGER.addAppender(new ConsoleAppender(new PatternLayout("%p [%c.%M] - %m%n")));
    }

    private static final @NotNull String fileExtension = ".ser";

    private VectorSerializer() {}

    public static @NotNull VectorSerializer getInstance() {
        return INSTANCE;
    }

    public void serialize(
        final @NotNull List<FeatureVector> vectors,
        final @NotNull Path path
    ) throws IOException {
        for (FeatureVector vector : vectors) {
            Path filePath = path.resolve(UUID.randomUUID().toString() + fileExtension);

            try (ObjectOutputStream out = new ObjectOutputStream(Files.newOutputStream(filePath))) {
                out.writeObject(vector);
            } catch (IOException e) {
                LOGGER.error("Failed to save feature on disk: " + e.getMessage());
                throw e;
            }
        }
    }

    public @NotNull List<FeatureVector> deserialize(
        final @NotNull Path path
    ) throws IOException, ClassNotFoundException {
        List<Path> filePaths = Files.walk(path).filter(Files::isRegularFile)
                                           .filter(it -> it.toString().endsWith(fileExtension))
                                           .collect(Collectors.toList());

        List<FeatureVector> vectors = new ArrayList<>();
        for (Path filePath : filePaths) {
            try (ObjectInputStream in = new ObjectInputStream(Files.newInputStream(filePath))) {
                FeatureVector vector = (FeatureVector) in.readObject();
                vectors.add(vector);
            } catch (ClassNotFoundException | IOException e) {
                LOGGER.error("Failed to load feature from disk: " + e.getMessage());
                throw e;
            }
        }

        return vectors;
    }
}
