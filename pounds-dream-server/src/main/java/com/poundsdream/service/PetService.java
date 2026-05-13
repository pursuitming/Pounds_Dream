package com.poundsdream.service;

import com.poundsdream.vo.PetLogVO;
import com.poundsdream.vo.PetVO;

import java.util.List;

public interface PetService {

    PetVO getPetInfo(Long userId);

    void renamePet(Long userId, String newName);

    PetVO interact(Long userId, String action);

    void changePetType(Long userId, String petType);

    List<PetLogVO> getPetLogs(Long userId);

    PetVO addWaterExp(Long userId);

    PetVO addSleepExp(Long userId);
}
