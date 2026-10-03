package com.pantrick.backend.service

import com.pantrick.backend.models.Recipe
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.slf4j.LoggerFactory
import java.io.File
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets

data class DatasetLoadResult(
    val recipes: List<Recipe>,
    val totalProcessed: Int,
    val validCount: Int,
    val withImagesCount: Int,
    val withoutImagesCount: Int,
    val withIngredientsCount: Int,
    val invalidSkippedCount: Int,
    val duplicateTitlesCount: Int
)

object RecipeDatasetLoader {
    private val logger = LoggerFactory.getLogger(RecipeDatasetLoader::class.java)

    fun loadDataset(
        csvFile: File? = null,
        imagesDir: File? = null
    ): DatasetLoadResult {
        val actualCsvFile = csvFile ?: findDatasetFile()
        val actualImagesDir = imagesDir ?: findImagesDir()

        logger.info("Loading recipe dataset from CSV: {}", actualCsvFile?.absolutePath)
        logger.info("Checking recipe images from dir: {}", actualImagesDir?.absolutePath)

        if (actualCsvFile == null || !actualCsvFile.exists()) {
            val err = "Dataset CSV file not found! Checked working directory and relative paths."
            logger.error(err)
            throw IllegalStateException(err)
        }

        val availableImagesMap = mutableMapOf<String, String>()
        if (actualImagesDir != null && actualImagesDir.exists() && actualImagesDir.isDirectory) {
            actualImagesDir.listFiles()?.forEach { file ->
                if (file.isFile) {
                    val nameWithoutExt = file.nameWithoutExtension.lowercase()
                    availableImagesMap[nameWithoutExt] = file.name
                    availableImagesMap[file.name.lowercase()] = file.name
                }
            }
        }
        logger.info("Found {} image files in directory.", availableImagesMap.size)

        val recipes = mutableListOf<Recipe>()
        val seenTitles = mutableSetOf<String>()

        var totalProcessed = 0
        var validCount = 0
        var withImagesCount = 0
        var withoutImagesCount = 0
        var withIngredientsCount = 0
        var invalidSkippedCount = 0
        var duplicateTitlesCount = 0

        val format = CSVFormat.DEFAULT.builder()
            .setIgnoreHeaderCase(true)
            .setTrim(true)
            .build()

        InputStreamReader(actualCsvFile.inputStream(), StandardCharsets.UTF_8).use { reader ->
            CSVParser(reader, format).use { parser ->
                val records = parser.records
                if (records.isEmpty()) {
                    logger.warn("CSV file is empty")
                    return DatasetLoadResult(emptyList(), 0, 0, 0, 0, 0, 0, 0)
                }

                // Skip header line if first record contains header text
                val dataRecords = if (records[0].get(1).equals("Title", ignoreCase = true)) {
                    records.subList(1, records.size)
                } else {
                    records
                }

                for (record in dataRecords) {
                    totalProcessed++
                    if (record.size() < 4) {
                        logger.warn("Row {} has insufficient columns (size={}), skipping.", totalProcessed, record.size())
                        invalidSkippedCount++
                        continue
                    }

                    val rawIdx = record.get(0).trim()
                    val title = record.get(1).trim()
                    val rawIngredients = if (record.size() > 2) record.get(2).trim() else ""
                    val instructions = if (record.size() > 3) record.get(3).trim() else ""
                    val imageNameRaw = if (record.size() > 4) record.get(4).trim() else ""
                    val cleanedIngredientsRaw = if (record.size() > 5) record.get(5).trim() else ""

                    if (title.isBlank() || title == "#NAME?") {
                        logger.warn("Row {} skipping invalid/blank title: '{}'", totalProcessed, title)
                        invalidSkippedCount++
                        continue
                    }

                    if (seenTitles.contains(title.lowercase())) {
                        duplicateTitlesCount++
                    } else {
                        seenTitles.add(title.lowercase())
                    }

                    val ingredientsSource = if (cleanedIngredientsRaw.isNotBlank() && cleanedIngredientsRaw != "[]") {
                        cleanedIngredientsRaw
                    } else {
                        rawIngredients
                    }

                    val ingredients = IngredientParser.parseIngredientsList(ingredientsSource)
                    if (ingredients.isNotEmpty()) {
                        withIngredientsCount++
                    }

                    var imageName: String? = null
                    var hasImage = false

                    if (imageNameRaw.isNotBlank() && imageNameRaw != "#NAME?") {
                        imageName = imageNameRaw
                        val targetImageName = if (!imageNameRaw.endsWith(".jpg", ignoreCase = true)) {
                            "$imageNameRaw.jpg"
                        } else {
                            imageNameRaw
                        }

                        if (availableImagesMap.containsKey(imageNameRaw.lowercase()) ||
                            availableImagesMap.containsKey(targetImageName.lowercase())
                        ) {
                            hasImage = true
                            withImagesCount++
                        } else {
                            withoutImagesCount++
                        }
                    } else {
                        withoutImagesCount++
                    }

                    val recipeId = if (rawIdx.isNotBlank()) rawIdx else totalProcessed.toString()

                    val recipe = Recipe(
                        id = recipeId,
                        title = title,
                        instructions = instructions,
                        imageName = imageName,
                        hasImage = hasImage,
                        ingredients = ingredients
                    )

                    recipes.add(recipe)
                    validCount++
                }
            }
        }

        val result = DatasetLoadResult(
            recipes = recipes,
            totalProcessed = totalProcessed,
            validCount = validCount,
            withImagesCount = withImagesCount,
            withoutImagesCount = withoutImagesCount,
            withIngredientsCount = withIngredientsCount,
            invalidSkippedCount = invalidSkippedCount,
            duplicateTitlesCount = duplicateTitlesCount
        )

        logger.info(
            """
            ==================================================
            Recipe dataset loaded:
            Total rows processed    : {}
            Valid recipes           : {}
            With images             : {}
            Without images          : {}
            With ingredients        : {}
            Duplicate titles        : {}
            Invalid/skipped         : {}
            ==================================================
            """.trimIndent(),
            result.totalProcessed,
            result.validCount,
            result.withImagesCount,
            result.withoutImagesCount,
            result.withIngredientsCount,
            result.duplicateTitlesCount,
            result.invalidSkippedCount
        )

        return result
    }

    private fun findDatasetFile(): File? {
        val envPath = System.getenv("DATASET_CSV_PATH")
        if (!envPath.isNullOrBlank()) {
            val envFile = File(envPath)
            if (envFile.exists()) return envFile
        }

        val candidates = listOf(
            "Dataset_Resep/Food Ingredients and Recipe Dataset with Image Name Mapping.csv",
            "../Dataset_Resep/Food Ingredients and Recipe Dataset with Image Name Mapping.csv",
            "../../Dataset_Resep/Food Ingredients and Recipe Dataset with Image Name Mapping.csv"
        )

        for (path in candidates) {
            val file = File(path)
            if (file.exists()) return file
        }

        // Also check relative to current dir or user dir
        val userDir = System.getProperty("user.dir") ?: "."
        val fileInUserDir = File(userDir, "Dataset_Resep/Food Ingredients and Recipe Dataset with Image Name Mapping.csv")
        if (fileInUserDir.exists()) return fileInUserDir

        val parentFile = File(userDir, "../Dataset_Resep/Food Ingredients and Recipe Dataset with Image Name Mapping.csv")
        if (parentFile.exists()) return parentFile

        return null
    }

    private fun findImagesDir(): File? {
        val envPath = System.getenv("DATASET_IMAGES_DIR")
        if (!envPath.isNullOrBlank()) {
            val envDir = File(envPath)
            if (envDir.exists()) return envDir
        }

        val candidates = listOf(
            "Dataset_Resep/Food Images/Food Images",
            "../Dataset_Resep/Food Images/Food Images",
            "../../Dataset_Resep/Food Images/Food Images"
        )

        for (path in candidates) {
            val dir = File(path)
            if (dir.exists() && dir.isDirectory) return dir
        }

        val userDir = System.getProperty("user.dir") ?: "."
        val dirInUserDir = File(userDir, "Dataset_Resep/Food Images/Food Images")
        if (dirInUserDir.exists()) return dirInUserDir

        val parentDir = File(userDir, "../Dataset_Resep/Food Images/Food Images")
        if (parentDir.exists()) return parentDir

        return null
    }
}
