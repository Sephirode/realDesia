package desia.Character;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@ToString

public class Player {

    private String classes;
    private String id;
    private String description;

    private double maxHp;
    private double maxMp;
    private double atk;
    private double magic;
    private double spd;
    private double def;
    private double mdef;
    private int level;

    private double growthMaxHp;
    private double growthMaxMp;
    private double growthAtk;
    private double growthMagic;
    private double growthDef;
    private double growthMdef;
    private double growthSpd;
}
