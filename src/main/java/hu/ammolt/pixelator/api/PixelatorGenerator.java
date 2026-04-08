package hu.ammolt.pixelator.api;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.nio.file.Path;

public class PixelatorGenerator {
    // calc brightness from 3 values, Red Green Blue from RGB image
    private static int calculateBrightness(int R, int G, int B) {
        return ((int) (0.2126 * R + 0.7152 * G + 0.0722 * B));
    }

    private static int calculateBrightness(Color color) {
        return ((int) (0.2126 * color.getRed() + 0.7152 * color.getGreen() + 0.0722 * color.getBlue()));
    }

    // match brightness
    private static int getClosestNumberFromList(int[] list, int number) {
        int ranNumber;
        int smallestNumber = Integer.MAX_VALUE;
        for (int entry : list) {
            ranNumber = number - entry;
            if (ranNumber < smallestNumber) smallestNumber = ranNumber;
        }
        return smallestNumber;
    }

    private static Color getClosestColor(HashSet<Color> materialColors, Color newToolColor) {
        if (materialColors.isEmpty()) return Color.MAGENTA; // Error fallback

        int leastDifference = Integer.MAX_VALUE;
        Color lastColor = Color.PINK;

        java.util.List<Color> palette = new ArrayList<>(materialColors);

        palette.sort(Comparator.comparingInt(PixelatorGenerator::calculateBrightness));
        palette.remove(palette.getLast()); // Remove the lightest color, most likely white, which is not pure white though

        List<Color> generatedColors = new ArrayList<>();

        Color c1;
        Color c2;

        for (int i = 0; i < palette.size()-1; ++i) {
            c1 = palette.get(i);
            c2 = palette.get(i+1);

            int b1 = calculateBrightness(c1);
            int b2 = calculateBrightness(c2);

            if (Math.abs(b1 - b2) >= 9) {
                generatedColors.add(new Color(
                        ((c1.getRed() + c2.getRed()) / 4),
                        ((c1.getGreen() + c2.getGreen()) / 4),
                        ((c1.getBlue() + c2.getBlue()) / 4)
                ));
            }
        }

        palette.addAll(generatedColors);

        Color darkest = palette.getFirst();
        palette.add(new Color(darkest.getRed() / 2, darkest.getGreen() / 2, darkest.getBlue() / 2));

        palette.sort(Comparator.comparingInt(c -> calculateBrightness(c.getRed(),c.getGreen(),c.getBlue())));

        // new tool's selected pixel's brightness calculated from it's RGB color's
        int bee = calculateBrightness(newToolColor.getRed(),newToolColor.getGreen(),newToolColor.getBlue());

        for (Color currentMaterialColor : palette) {
            // currentBee is from currentMaterialColor
            int currentBee = calculateBrightness(currentMaterialColor.getRed(), currentMaterialColor.getGreen(), currentMaterialColor.getBlue());
            int conclusion = Math.abs(bee - currentBee);

            if (conclusion < leastDifference) {
                leastDifference = conclusion;
                lastColor = currentMaterialColor;
            }
        }
        return lastColor;
    }

    // ------- Folders names -------- //
    private static final String pixelatorFolderName = "pixelator";
    private static String modId = "pixelator"; // This should be changed to the mod's
    private static Path inputPath;
    private static Path outputPath;

    // To set which namespace the output should be
    public static void setupPaths(String newModId, Path sourceRoot, Path outputRoot) {
        modId = newModId;

        inputPath = sourceRoot.resolve("assets").resolve("pixelator").resolve("textures").resolve(pixelatorFolderName);

        outputPath = outputRoot.resolve("assets").resolve(modId).resolve("textures").resolve("item");
    }

    public static String getModId() {
        return modId;
    }

    private static Path getPathToMaterials() { return inputPath.resolve("materials"); }
    private static Path getPathToHandles() { return inputPath.resolve("handles"); }
    private static Path getPathToTemplates() { return inputPath.resolve("templates"); }
    // ----------------------------- //

    public static void generator() throws IOException {
        HashMap<String, HashSet<Color>> colors = new HashMap<>();
        HashMap<String, HashSet<Color>> handleColors = new HashMap<>();

        // ------- Folders and their contents -------- //
        if (inputPath == null || outputPath == null) {
            throw new IOException("PixelatorGenerator paths not initialized! Call setupPaths() first.");
        }

        Path pathToMaterials = getPathToMaterials();
        Path pathToHandles = getPathToHandles();
        Path pathToTemplates = getPathToTemplates();

        File materialsDirectory = pathToMaterials.toFile();
        if (!materialsDirectory.exists()) materialsDirectory.mkdirs();
        File[] materials = materialsDirectory.listFiles();
        if (materials == null) materials = new File[0];

        File handlesDirectory = pathToHandles.toFile();
        if (!handlesDirectory.exists()) handlesDirectory.mkdirs();
        File[] handles = handlesDirectory.listFiles();
        if (handles == null) handles = new File[0];

        File templatesDirectory = pathToTemplates.toFile();
        if (!templatesDirectory.exists()) templatesDirectory.mkdirs();
        File[] templates = templatesDirectory.listFiles();
        if (templates == null) templates = new File[0];
        // ------------------------------------------- //

        for (File material : materials) {
            BufferedImage rawTexture = ImageIO.read(material);
            BufferedImage texture = new BufferedImage(rawTexture.getWidth(), rawTexture.getHeight(), BufferedImage.TYPE_INT_ARGB);
            texture.getGraphics().drawImage(rawTexture, 0, 0, null);
            Raster textureRaster = texture.getRaster();
            if (textureRaster == null) {
            }

            String materialName = material.getName();
            materialName = materialName.replace("_ingot.png", "").replace(".png", "");

            colors.computeIfAbsent(materialName, k -> new HashSet<>());

            for (int y = 0; y < texture.getHeight(); ++y) {
                for (int x = 0; x < texture.getWidth(); ++x) {
                    int[] pixel = new int[4];
                    assert textureRaster != null;
                    textureRaster.getPixel(x, y, pixel);
                    if (pixel[3] != 0) {
                        Color newColor = new Color(pixel[0], pixel[1], pixel[2], pixel[3]);
//                        System.out.println(colors.containsKey(materialName));
                        colors.get(materialName).add(newColor);
                    }
                }
            }
        }

        for (File handle : handles) {
            BufferedImage texture = ImageIO.read(handle);
            Raster textureRaster = texture.getRaster();

            String handleName = handle.getName();
            handleName = handleName.replace("_ingot.png", "").replace(".png", "");

            handleColors.put(handleName, new HashSet<>());

            for (int y = 0; y < texture.getHeight(); ++y) {
                for (int x = 0; x < texture.getWidth(); ++x) {
                    int[] pixel = new int[4];
                    textureRaster.getPixel(x, y, pixel);
                    if (pixel[3] != 0) {
                        Color newColor = new Color(pixel[0], pixel[1], pixel[2], pixel[3]);
                        handleColors.get(handleName).add(newColor);
                    }
                }
            }
        }

        // Nested for loop hell and I'm loving it!
        for (File template : templates) {
            for (HashSet<Color> handleSet : handleColors.values()) {
                for (File material : materials) {

                    String materialName = material.getName();
                    materialName = materialName.replace("_ingot.png", "").replace(".png", "");

                    BufferedImage originalTexture = ImageIO.read(template);
                    BufferedImage newTexture = new BufferedImage(originalTexture.getWidth(), originalTexture.getHeight(), BufferedImage.TYPE_INT_ARGB);
                    newTexture.getGraphics().drawImage(originalTexture, 0, 0, null);
                    WritableRaster textureRaster = newTexture.getRaster();

                    for (int y = 0; y < newTexture.getHeight(); ++y) {
                        for (int x = 0; x < newTexture.getWidth(); ++x) {
                            int[] pixel = new int[4];
                            textureRaster.getPixel(x, y, pixel);
                            Color currentColor = new Color(pixel[0], pixel[1], pixel[2]);

                            if (pixel[3] == 0) {
                                continue;
                            }

                            if (handleSet.contains(currentColor)) {
                            } else {
                                currentColor = getClosestColor(colors.get(materialName), currentColor);
                                pixel[0] = currentColor.getRed();
                                pixel[1] = currentColor.getGreen();
                                pixel[2] = currentColor.getBlue();

                                textureRaster.setPixel(x, y, pixel);
                            }
                        }
                    }

                    String materialOutput = material.getName().replace("_ingot.png", "").replace(".png", "");
                    String outputFileName = materialOutput + "_" + template.getName();

                    // Create the final file path
                    File outputFile = outputPath.resolve(outputFileName).toFile();

                    // Ensure the 'item' folder exists
                    if (!outputFile.getParentFile().exists()) outputFile.getParentFile().mkdirs();

                    ImageIO.write(newTexture, "png", outputFile);
                }
            }
        }
    }
}
