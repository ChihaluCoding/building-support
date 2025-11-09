package chihalu.building.support;

import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;

/**
 * .minecraft 直下に配置する本Mod専用ストレージディレクトリを管理するユーティリティ。
 */
public final class BuildingSupportStorage {
	private static final Path BASE_DIR = FabricLoader.getInstance()
		.getGameDir()
		.resolve(BuildingSupport.MOD_ID);

	private BuildingSupportStorage() {
	}

	public static Path baseDir() {
		return BASE_DIR;
	}

	public static Path resolve(String first, String... more) {
		Path path = BASE_DIR.resolve(first);
		for (String segment : more) {
			path = path.resolve(segment);
		}
		return path;
	}
}
