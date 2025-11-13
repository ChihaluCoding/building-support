package chihalu.building.support.world;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.WorldProperties;

import chihalu.building.support.config.BuildingSupportConfig;
import chihalu.building.support.config.BuildingSupportConfig.WeatherMode;

/**
 * モジュール全体のワールド環境（天候・時間）を設定通りに維持する制御クラス。
 */
public final class WorldSettingsController {
	private WorldSettingsController() {
	}

	/**
	 * Fabricイベントへ登録し、毎ワールドTickで設定を反映する。
	 */
	public static void init() {
		ServerTickEvents.START_WORLD_TICK.register(WorldSettingsController::applyWorldSettings);
	}

	private static void applyWorldSettings(ServerWorld world) {
		BuildingSupportConfig config = BuildingSupportConfig.getInstance();
		if (config.isFixedWeatherEnabled()) {
			applyWeather(world, config.getFixedWeatherMode());
		}
		if (config.isFixedTimeEnabled()) {
			applyTime(world, config.getFixedTimeValue());
		}
	}

	/**
	 * 設定された天候と現在の天候が異なる場合のみ setWeather を呼び出す。
	 */
	private static void applyWeather(ServerWorld world, WeatherMode mode) {
		boolean raining = mode != WeatherMode.CLEAR;
		boolean thundering = mode == WeatherMode.THUNDER;
		if (world.isRaining() == raining && world.isThundering() == thundering) {
			return;
		}
		int clearDuration = raining ? 0 : 6000;
		int rainDuration = raining ? 6000 : 0;
		world.setWeather(clearDuration, rainDuration, raining, thundering);
	}

	/**
	 * 時刻固定が有効な場合、同じ日付の中で所定の時間帯になるよう補正する。
	 */
	private static void applyTime(ServerWorld world, int targetTicks) {
		WorldProperties properties = world.getLevelProperties();
		long currentTime = properties.getTimeOfDay();
		long currentRemainder = Math.floorMod(currentTime, 24000L);
		long normalized = Math.floorMod(targetTicks, 24000);
		if (currentRemainder == normalized) {
			return;
		}
		long base = currentTime - currentRemainder;
		world.setTimeOfDay(base + normalized);
	}
}
