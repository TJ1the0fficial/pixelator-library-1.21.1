# Pixelator Library Mod
## Setup
To use this mod library, setup the data gen.

[![](https://jitpack.io/v/TJ1the0fficial/pixelator-library-1.21.1.svg)](https://jitpack.io/#TJ1the0fficial/pixelator-library-1.21.1)

Provider
```
// Doesn't include libraries or anything, just the classes
public class PixelatorDataProvider implements DataProvider {
    private final PackOutput output;

    public PixelatorDataProvider(PackOutput output) {
        this.output = output;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        // 1. Get the Project Root by going up from the 'run' folder
        // '..' tells Java to step out of 'run' and into the main project folder
        Path projectRoot = Path.of("..").toAbsolutePath().getParent();

//        // 2. Now we point to the REAL source and the REAL generated output
        Path sourceRoot = projectRoot.resolve("src").resolve("main").resolve("resources");
        Path outputRoot = output.getOutputFolder(); // This is already handled by NeoForge

        try {
            // 3. Set the paths and run
            PixelatorGenerator.setupPaths(pixelator.MODID, sourceRoot, outputRoot);
            PixelatorGenerator.generator(cache);
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
```

Optional DataGenerator (I assume you will use DataGenerator class)
```
@EventBusSubscriber(modid = your_awesome_mod.MODID)
public class DataGenerators {
    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();

        generator.addProvider(event.includeServer(), new PixelatorProvider(packOutput));

    }
}
```
## Use
Put the templates (iron_sword.png) into the templates folder under pixelator.<br>
Change name of template. (iron_sword.png -> sword.png (The program uses the image's name for full freedom with naming))<br>

Then put the materials (diamond.png) into the materials folder.<br>

Then put the handles (stuff you don't want to color, like the vanilla stick) into the handles folder.<br>

Run DataGen.<br>

You will see the outcome. template*material textures<br>


## How does this work though?

This mod checks every color in the template, then it checks the material's and the handle's pixels too.<br>
template - this software copies the image and then replaces the colors based on brightness<br>
material - the software uses the material's colors to paint over the copied template<br>
handle - a filter on what not to color on the template<br>

https://mcasset.cloud/26.1.1/assets/minecraft/textures<br>

template:<br>
<img width="160" height="160" alt="sword" src="https://github.com/user-attachments/assets/eccbbe27-58b8-4ecb-b0b1-d47e2a71d8f5" /><br>
material:<br>
<img width="160" height="160" alt="diamond" src="https://github.com/user-attachments/assets/d4c85211-4edd-42a1-af90-d07d3237fb0a" /><br>
handle:<br>
<img width="160" height="160" alt="stick" src="https://github.com/user-attachments/assets/1ec106cf-78db-423d-bbe6-5081ca4e915a" /><br>
generated diamond sword:<br>
<img width="160" height="160" alt="generated_diamond_sword" src="https://github.com/user-attachments/assets/d705c043-bb82-4e7e-8bcb-38b9a0cac9be" /><br>
original diamond sword:<br>
<img width="160" height="160" alt="original_diamond_sword" src="https://github.com/user-attachments/assets/7cdda573-3a95-4b25-9799-0b3ac9d48193" /><br>
