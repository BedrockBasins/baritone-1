/*
 * This file is part of Baritone.
 *
 * Baritone is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Baritone is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Baritone.  If not, see <https://www.gnu.org/licenses/>.
 */

package baritone;

import baritone.api.event.GameEventHandler;
import baritone.behavior.Behavior;
import baritone.behavior.impl.*;
import baritone.utils.InputOverrideHandler;
import baritone.utils.ToolSet;
import net.minecraft.client.Minecraft;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author Brady
 * @since 7/31/2018 10:50 PM
 */
public enum Baritone {

    /**
     * Singleton instance of this class
     */
    INSTANCE;

    /**
     * Whether or not {@link Baritone#init()} has been called yet
     */
    private boolean initialized;

    private GameEventHandler gameEventHandler;
    private InputOverrideHandler inputOverrideHandler;
    private Settings settings;
    private List<Behavior> behaviors;
    private File dir;

    /**
     * List of consumers to be called after Baritone has initialized
     */
    private List<Consumer<Baritone>> onInitConsumers;

    /**
     * Whether or not Baritone is active
     */
    private boolean active;

    Baritone() {
        this.onInitConsumers = new ArrayList<>();
    }

    public synchronized void init() {
        if (initialized) {
            return;
        }
        this.gameEventHandler = new GameEventHandler();
        this.inputOverrideHandler = new InputOverrideHandler();
        this.settings = new Settings();
        this.behaviors = new ArrayList<>();
        {
            registerBehavior(PathingBehavior.INSTANCE);
            registerBehavior(LookBehavior.INSTANCE);
            registerBehavior(MemoryBehavior.INSTANCE);
            registerBehavior(LocationTrackingBehavior.INSTANCE);
            registerBehavior(FollowBehavior.INSTANCE);
            registerBehavior(MineBehavior.INSTANCE);
            this.gameEventHandler.registerEventListener(ToolSet.INTERNAL_EVENT_LISTENER);
        }
        this.dir = new File(Minecraft.getMinecraft().gameDir, "baritone");
        if (!Files.exists(dir.toPath())) {
            try {
                Files.createDirectories(dir.toPath());
            } catch (IOException ignored) {}
        }

        this.active = true;
        this.initialized = true;

        this.onInitConsumers.forEach(consumer -> consumer.accept(this));
    }

    public final boolean isInitialized() {
        return this.initialized;
    }

    public final GameEventHandler getGameEventHandler() {
        return this.gameEventHandler;
    }

    public final InputOverrideHandler getInputOverrideHandler() {
        return this.inputOverrideHandler;
    }

    public final List<Behavior> getBehaviors() {
        return this.behaviors;
    }

    public void registerBehavior(Behavior behavior) {
        this.behaviors.add(behavior);
        this.gameEventHandler.registerEventListener(behavior);
    }

    public final boolean isActive() {
        return this.active;
    }

    public final Settings getSettings() {
        return this.settings;
    }

    public static Settings settings() {
        return Baritone.INSTANCE.settings; // yolo
    }

    public final File getDir() {
        return this.dir;
    }

    public final void registerInitListener(Consumer<Baritone> runnable) {
        this.onInitConsumers.add(runnable);
    }
}
