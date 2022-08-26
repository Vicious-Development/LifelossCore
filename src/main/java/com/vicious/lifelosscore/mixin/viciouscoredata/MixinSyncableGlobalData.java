package com.vicious.lifelosscore.mixin.viciouscoredata;

import com.vicious.lifelosscore.api.ILLGlobalData;
import com.vicious.lifelosscore.common.teams.Team;
import com.vicious.viciouscore.common.data.implementations.SyncableDataTable;
import com.vicious.viciouscore.common.data.implementations.attachable.SyncableGlobalData;
import com.vicious.viciouscore.common.data.state.IDataTable;
import com.vicious.viciouscore.common.data.structures.SyncablePrimitive;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import org.spongepowered.asm.mixin.Mixin;

import java.util.Locale;
import java.util.UUID;

@Mixin(SyncableGlobalData.class)
public class MixinSyncableGlobalData extends MixinSyncableAttachableCompound<ICapabilityProvider> implements ILLGlobalData {
    public SyncablePrimitive<UUID> maxHealthUUID = add(new SyncablePrimitive<>("maxhealthuuid",UUID.randomUUID()));
    public SyncableDataTable<Team> teams = add(new SyncableDataTable<>("teams",Team::new))
            .supports((t)->t.getName().toLowerCase(Locale.ROOT),String.class)
            .supports(Team::getUUID,UUID.class);
    public SyncablePrimitive<Boolean> kyramode = add(new SyncablePrimitive<>("kyramode", false));
    @Override
    public boolean kyraFailed() {
        return kyramode.value;
    }

    @Override
    public UUID getMaxHealthAttributeModUUID() {
        return maxHealthUUID.value;
    }

    @Override
    public void setKyraMode(boolean v) {
        kyramode.setValue(v);
    }

    @Override
    public IDataTable<Team> getTeams() {
        return teams.value;
    }

    @Override
    public void addTeam(Team t) {
        teams.value.add(t);
    }

    @Override
    public void removeTeam(Team t) {
        teams.value.remove(t);
    }
}
