package chihalu.building.support.village;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.mojang.serialization.Codec;

import chihalu.building.support.BuildingSupport;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateType;

/**
 * 村スポーンの自動設定を各ワールドで一度だけ実行するための永続フラグ。
 */
public final class VillageSpawnState extends PersistentState {
	private static final String STORAGE_KEY = BuildingSupport.MOD_ID + "_village_spawn_state";
	private static final Codec<Set<String>> STRING_SET_CODEC = Codec.list(Codec.STRING)
		.xmap(HashSet::new, List::copyOf);
	private static final Codec<VillageSpawnState> CODEC = STRING_SET_CODEC
		.xmap(VillageSpawnState::fromSet, VillageSpawnState::toSet);
	private static final PersistentStateType<VillageSpawnState> TYPE = new PersistentStateType<>(
		STORAGE_KEY,
		context -> new VillageSpawnState(),
		context -> CODEC,
		DataFixTypes.SAVED_DATA_SCOREBOARD
	);

	private final Set<String> appliedTypes = new HashSet<>();

	private VillageSpawnState() {
	}

	private VillageSpawnState(Set<String> entries) {
		this.appliedTypes.addAll(entries);
	}

	private static VillageSpawnState fromSet(Set<String> entries) {
		return new VillageSpawnState(entries);
	}

	private Set<String> toSet() {
		return Set.copyOf(appliedTypes);
	}

	public static VillageSpawnState get(ServerWorld world) {
		return world.getPersistentStateManager().getOrCreate(TYPE);
	}

	public boolean hasApplied(String typeId) {
		return appliedTypes.contains(typeId);
	}

	public void markApplied(String typeId) {
		if (appliedTypes.add(typeId)) {
			markDirty();
		}
	}
}

