package hu.ammolt.pixelator.dataGen;

import hu.ammolt.pixelator.api.PixelatorGenerator;
import hu.ammolt.pixelator.pixelator;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

// I used AI to generate this code, because I couldn't find a Wiki on how to make a library
// I don't care if it's called something else. Most call it library and I Googled library and couldn't find a tutorial about it.

public class PixelatorDataProvider implements DataProvider {
    private final PackOutput output;

    public PixelatorDataProvider(PackOutput output) {
        this.output = output;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        // 1. Get the Project Root by going up from the 'run' folder
        // '..' tells Java to step out of 'run' and into the main project folder
        Path projectRoot = Path.of("").toAbsolutePath().getParent();

        // 2. Now we point to the REAL source and the REAL generated output
        Path sourceRoot = projectRoot.resolve("src").resolve("main").resolve("resources");
        Path outputRoot = output.getOutputFolder(); // This is already handled by NeoForge

        try {
            // 3. Set the paths and run
            PixelatorGenerator.setupPaths(pixelator.MODID, sourceRoot, outputRoot);
            PixelatorGenerator.generator();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return CompletableFuture.completedFuture(null);
    }

    @Override
    public String getName() {
        return "Pixelator: " + PixelatorGenerator.getModId();
    }
}
