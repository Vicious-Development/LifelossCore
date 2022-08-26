package com.vicious.lifelosscore.api;

import com.vicious.lifelosscore.common.teams.Team;
import com.vicious.viciouscore.common.data.implementations.attachable.SyncableGlobalData;
import com.vicious.viciouscore.common.data.state.IDataTable;

import java.util.UUID;

public interface ILLGlobalData {
    boolean kyraFailed();
    UUID getMaxHealthAttributeModUUID();
    void setKyraMode(boolean v);
    IDataTable<Team> getTeams();
    void addTeam(Team t);
    void removeTeam(Team t);
    static ILLGlobalData getInstance(){
        return (ILLGlobalData) SyncableGlobalData.getInstance();
    }
}
