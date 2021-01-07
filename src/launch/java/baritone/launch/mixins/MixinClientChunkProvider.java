/*
 * This file is part of Baritone.
 *
 * Baritone is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Baritone is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Baritone.  If not, see <https://www.gnu.org/licenses/>.
 */

package baritone.launch.mixins;

import baritone.utils.accessor.IChunkArray;
import baritone.utils.accessor.IClientChunkProvider;
import net.minecraft.client.world.ClientChunkManager;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.lang.reflect.Field;
import java.util.Arrays;

@Mixin(ClientChunkManager.class)
public class MixinClientChunkProvider implements IClientChunkProvider {

    @Final
    @Shadow
    private ClientWorld world;

    @Unique
    private Field chunkArrayField;

    @Override
    public ClientChunkManager createThreadSafeCopy() {
        IChunkArray arr = extractReferenceArray();
        ClientChunkManager result = new ClientChunkManager(world, arr.viewDistance() - 3); // -3 because its adds 3 for no reason lmao
        IChunkArray copyArr = ((IClientChunkProvider) result).extractReferenceArray();
        copyArr.copyFrom(arr);
        if (copyArr.viewDistance() != arr.viewDistance()) {
            throw new IllegalStateException(copyArr.viewDistance() + " " + arr.viewDistance());
        }
        return result;
    }

    @Override
    public IChunkArray extractReferenceArray() {
        if (chunkArrayField == null) {
            boolean flag = true;
            for (Field f : ClientChunkManager.class.getDeclaredFields()) {
                if (IChunkArray.class.isAssignableFrom(f.getType())) {
                    chunkArrayField = f;
                    flag = false;
                    break;
                }
            } //else
            if (flag) {
                throw new RuntimeException(Arrays.toString(ClientChunkManager.class.getDeclaredFields()));
            }
        }
        try {
            return (IChunkArray) chunkArrayField.get(this);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
