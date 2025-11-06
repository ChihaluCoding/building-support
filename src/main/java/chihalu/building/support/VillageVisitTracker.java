package chihalu.building.support;

import com.mojang.serialization.Codec;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.Uuids;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateType;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class VillageVisitTracker extends PersistentState {
	private static final String STORAGE_KEY = BuildingSupport.MOD_ID + "_village_visits";
	private static final Codec<Set<String>> STRING_SET_CODEC = Codec.list(Codec.STRING)
		.xmap(values -> new HashSet<>(values), set -> List.copyOf(set));
	private static final Codec<Map<UUID, Set<String>>> MAP_CODEC = Codec.unboundedMap(Uuids.CODEC, STRING_SET_CODEC);
	private static final Codec<VillageVisitTracker> CODEC = MAP_CODEC.xmap(VillageVisitTracker::fromMap, VillageVisitTracker::toMap);
	private static final PersistentStateType<VillageVisitTracker> TYPE = new PersistentStateType<>(
		STORAGE_KEY,
		context -> new VillageVisitTracker(),
		context -> CODEC,
		DataFixTypes.SAVED_DATA_SCOREBOARD
	);

	private final Map<UUID, Set<String>> visitedVillages = new HashMap<>();

	private VillageVisitTracker() {
	}

	private VillageVisitTracker(Map<UUID, Set<String>> data) {
		data.forEach((uuid, entries) -> visitedVillages.put(uuid, new HashSet<>(entries)));
	}

	public static VillageVisitTracker get(ServerWorld world) {
		return world.getPersistentStateManager().getOrCreate(TYPE);
	}

	public boolean isVisited(UUID playerId, ServerWorld world, BuildingSupportConfig.VillageSpawnType type, VillageSpawnManager.VillageLocation location) {
		Set<String> entries = visitedVillages.get(playerId);
		if (entries == null) {
			return false;
		}
		return entries.contains(encodeKey(world, type, location));
	}

	public void markVisited(UUID playerId, ServerWorld world, BuildingSupportConfig.VillageSpawnType type, VillageSpawnManager.VillageLocation location) {
		String key = encodeKey(world, type, location);
		Set<String> entries = visitedVillages.computeIfAbsent(playerId, uuid -> new HashSet<>());
		if (entries.add(key)) {
			markDirty();
		}
	}

	private static VillageVisitTracker fromMap(Map<UUID, Set<String>> map) {
		return new VillageVisitTracker(map);
	}

	private Map<UUID, Set<String>> toMap() {
		Map<UUID, Set<String>> copy = new HashMap<>();
		visitedVillages.forEach((uuid, entries) -> copy.put(uuid, Set.copyOf(entries)));
		return copy;
	}

	private String encodeKey(ServerWorld world, BuildingSupportConfig.VillageSpawnType type, VillageSpawnManager.VillageLocation location) {
		Identifier worldId = world.getRegistryKey().getValue();
		ChunkPos chunkPos = location.chunkPos();
		return worldId + "|" + type.id() + "|" + chunkPos.x + "," + chunkPos.z;
	}
}
