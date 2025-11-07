package chihalu.building.support.village;

import com.mojang.datafixers.util.Pair;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.structure.Structure;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import chihalu.building.support.BuildingSupport;
import chihalu.building.support.config.BuildingSupportConfig;

public final class VillageSpawnManager {
	private static final VillageSpawnManager INSTANCE = new VillageSpawnManager();
	private static final int DEFAULT_SEARCH_DISTANCE = 640;
	private static final int WORLD_SPAWN_SEARCH_DISTANCE = 5000;
	private static final int MIN_SEARCH_DISTANCE = 128;
	private static final int MAX_SEARCH_DISTANCE = 16000;

	private VillageSpawnManager() {
	}

	public static VillageSpawnManager getInstance() {
		return INSTANCE;
	}

	public void initialize() {
		ServerLifecycleEvents.SERVER_STARTED.register(this::onServerStarted);
	}

	private void onServerStarted(MinecraftServer server) {
		ServerWorld overworld = server.getOverworld();
		if (overworld == null) {
			return;
		}

		BuildingSupportConfig config = BuildingSupportConfig.getInstance();
		if (!config.isVillageSpawnEnabled()) {
			return;
		}

		BuildingSupportConfig.VillageSpawnType desiredType = config.getVillageSpawnType();
		Optional<VillageLocation> location = findNthNearestVillage(overworld, BlockPos.ORIGIN, desiredType, 0, WORLD_SPAWN_SEARCH_DISTANCE);
		if (location.isEmpty()) {
			BuildingSupport.LOGGER.warn("村スポーンを {} で検索しましたが、近くに見つかりませんでした。既存のスポーン地点を保持します。", desiredType.id());
			return;
		}

		setWorldSpawn(server, overworld, location.get().spawnPos());
		BuildingSupport.LOGGER.info("村スポーン地点を {} の村 ({}) に設定しました。", desiredType.id(), location.get().spawnPos());
	}

	public Optional<VillageLocation> findNthNearestVillage(ServerWorld world, BlockPos origin, BuildingSupportConfig.VillageSpawnType type, int index) {
		return findNthNearestVillage(world, origin, type, index, DEFAULT_SEARCH_DISTANCE);
	}

	public Optional<VillageLocation> findNthNearestVillage(ServerWorld world, BlockPos origin, BuildingSupportConfig.VillageSpawnType type, int index, int maxDistance) {
		if (index < 0) {
			return Optional.empty();
		}
		List<VillageLocation> locations = findNearestVillages(world, origin, type, Math.max(index + 3, 3), maxDistance);
		if (locations.size() <= index) {
			return Optional.empty();
		}
		return Optional.of(locations.get(index));
	}

	public List<VillageLocation> findNearestVillages(ServerWorld world, BlockPos origin, BuildingSupportConfig.VillageSpawnType type, int requiredCount) {
		return findNearestVillages(world, origin, type, requiredCount, DEFAULT_SEARCH_DISTANCE);
	}

	public List<VillageLocation> findNearestVillages(ServerWorld world, BlockPos origin, BuildingSupportConfig.VillageSpawnType type, int requiredCount, int maxDistance) {
		int count = Math.max(requiredCount, 1);
		int cappedDistance = Math.max(MIN_SEARCH_DISTANCE, Math.min(maxDistance, MAX_SEARCH_DISTANCE));

		Optional<RegistryEntry<Structure>> structureEntry = resolveStructureEntry(world, type);
		if (structureEntry.isEmpty()) {
			return List.of();
		}

		RegistryEntryList<Structure> structures = RegistryEntryList.of(structureEntry.get());
		ChunkGenerator generator = world.getChunkManager().getChunkGenerator();
		Set<Long> visited = new HashSet<>();
		List<VillageLocation> candidates = new ArrayList<>();
		BlockPos[] centers = createSearchCenters(origin, cappedDistance);
		int[] radii = generateSearchRadii(cappedDistance);

		search:
		for (BlockPos center : centers) {
			for (int radius : radii) {
				Pair<BlockPos, RegistryEntry<Structure>> located = generator.locateStructure(world, structures, center, radius, false);
				if (located == null) {
					continue;
				}

				BlockPos structurePos = located.getFirst();
				ChunkPos chunkPos = new ChunkPos(structurePos);
				if (!visited.add(chunkPos.toLong())) {
					continue;
				}

				world.getChunk(chunkPos.x, chunkPos.z);
				BlockPos spawnPos = adjustSpawnPosition(world, structurePos);
				candidates.add(new VillageLocation(structurePos, spawnPos));

				if (candidates.size() >= count) {
					break search;
				}
			}
		}

		if (candidates.isEmpty()) {
			return List.of();
		}

		candidates.sort(Comparator.comparingDouble(candidate -> candidate.structurePos().getSquaredDistance(origin)));
		return candidates;
	}

	private Optional<RegistryEntry<Structure>> resolveStructureEntry(ServerWorld world, BuildingSupportConfig.VillageSpawnType type) {
		Optional<Registry<Structure>> registryOptional = world.getRegistryManager().getOptional(RegistryKeys.STRUCTURE);
		if (registryOptional.isEmpty()) {
			return Optional.empty();
		}
		Registry<Structure> registry = registryOptional.get();
		RegistryKey<Structure> structureKey = RegistryKey.of(RegistryKeys.STRUCTURE, type.structureId());
		return registry.getEntry(structureKey.getValue()).map(entry -> (RegistryEntry<Structure>) entry);
	}

	private BlockPos[] createSearchCenters(BlockPos origin, int maxDistance) {
		LinkedHashSet<BlockPos> centers = new LinkedHashSet<>();
		centers.add(origin);
		int[] axisOffsets = generateAxisOffsets(maxDistance);
		for (int x : axisOffsets) {
			for (int z : axisOffsets) {
				if (x == 0 && z == 0) {
					continue;
				}
				centers.add(origin.add(x, 0, z));
			}
		}
		return centers.toArray(new BlockPos[0]);
	}

	private int[] generateSearchRadii(int maxDistance) {
		LinkedHashSet<Integer> radii = new LinkedHashSet<>();
		int capped = Math.max(MIN_SEARCH_DISTANCE, maxDistance);
		int roughStep = Math.max(MIN_SEARCH_DISTANCE, capped / 8);
		int step = Math.max(MIN_SEARCH_DISTANCE, ((roughStep + 63) / 64) * 64);
		for (int radius = step; radius < capped; radius += step) {
			radii.add(radius);
		}
		radii.add(capped);
		return radii.stream().mapToInt(Integer::intValue).toArray();
	}

	private int[] generateAxisOffsets(int maxDistance) {
		LinkedHashSet<Integer> offsets = new LinkedHashSet<>();
		offsets.add(0);
		int capped = Math.max(0, maxDistance);
		int step = determineCenterStep(capped);
		if (step > 0) {
			for (int value = step; value < capped; value += step) {
				offsets.add(value);
				offsets.add(-value);
			}
		}
		if (capped > 0) {
			offsets.add(capped);
			offsets.add(-capped);
		}
		return offsets.stream().mapToInt(Integer::intValue).toArray();
	}

	private int determineCenterStep(int maxDistance) {
		if (maxDistance <= 0) {
			return 0;
		}
		if (maxDistance <= 512) {
			return 256;
		}
		if (maxDistance <= 1500) {
			return 512;
		}
		if (maxDistance <= 3000) {
			return 750;
		}
		if (maxDistance <= 5000) {
			return 1000;
		}
		return 1500;
	}

	private BlockPos adjustSpawnPosition(ServerWorld world, BlockPos structurePos) {
		BlockPos surface = world.getTopPosition(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, structurePos);
		return surface.up();
	}

	private void setWorldSpawn(MinecraftServer server, ServerWorld world, BlockPos pos) {
		world.getChunk(pos.getX() >> 4, pos.getZ() >> 4);
		String command = String.format("setworldspawn %d %d %d", pos.getX(), pos.getY(), pos.getZ());
		server.getCommandManager().executeWithPrefix(server.getCommandSource().withLevel(2), command);
	}

	public record VillageLocation(BlockPos structurePos, BlockPos spawnPos) {
		public ChunkPos chunkPos() {
			return new ChunkPos(structurePos);
		}
	}
}
