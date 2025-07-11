package de.hysky.skyblocker.skyblock.profileviewer.utils;

import java.util.ArrayList;
import java.util.List;

public class LevelFinder {
    public static class LevelInfo {
        public long xp;
        public int level;
        public double fill;
        public long levelXP;
        public long nextLevelXP;

        public LevelInfo(long xp, int level) {
            this.xp = xp;
            this.level = level;
        }

        public LevelInfo(long xp, int level, double fill, double levelXP, double nextLevelXP) {
            this.xp = xp;
            this.level = level;
            this.fill = fill;
            this.levelXP = (long) levelXP;
            this.nextLevelXP = (long) nextLevelXP;
        }
    }

    private static final long CATA_XP_PER_LEVEL = 200_000_000;
    private static final List<LevelInfo> GENERIC_SKILL_BOUNDARIES = createGenericSkillBoundaries();
    private static final List<LevelInfo> CATACOMBS_SKILL_BOUNDARIES = createCatacombsSkillBoundaries();
    private static final List<LevelInfo> RUNECRAFT_SKILL_BOUNDARIES = createRunecraftSkillBoundaries();
    private static final List<LevelInfo> SOCIAL_SKILL_BOUNDARIES = createSocialSkillBoundaries();

    private static final List<LevelInfo> COMMON_PET_BOUNDARIES = createCommonPetBoundaries();
    private static final List<LevelInfo> UNCOMMON_PET_BOUNDARIES = createUncommonPetBoundaries();
    private static final List<LevelInfo> RARE_PET_BOUNDARIES = createRarePetBoundaries();
    private static final List<LevelInfo> EPIC_PET_BOUNDARIES = createEpicPetBoundaries();
    private static final List<LevelInfo> LEGENDARY_PET_BOUNDARIES = createLegendaryPetBoundaries();


    private static final List<LevelInfo> GENERIC_SLAYER_BOUNDARIES = createGenericSlayerBoundaries();
    private static final List<LevelInfo> VAMPIRE_SLAYER_BOUNDARIES = createVampireSlayerBoundaries();

    private static List<LevelInfo> createGenericSkillBoundaries() {
        List<LevelInfo> boundaries = new ArrayList<>();
        long[] cumulativeXp = {
                0L, 50L, 175L, 375L, 675L, 1175L, 1925L, 2925L, 4425L, 6425L,
                9925L, 14925L, 22425L, 32425L, 47425L, 67425L, 97425L, 147425L,
                222425L, 322425L, 522425L, 822425L, 1222425L, 1722425L, 2322425L,
                3022425L, 3822425L, 4722425L, 5722425L, 6822425L, 8022425L,
                9322425L, 10722425L, 12222425L, 13822425L, 15522425L, 17322425L,
                19222425L, 21222425L, 23322425L, 25522425L, 27822425L, 30222425L,
                32722425L, 35322425L, 38072425L, 40972425L, 44072425L, 47472425L,
                51172425L, 55172425L, 59472425L, 64072425L, 68972425L, 74172425L,
                79672425L, 85472425L, 91572425L, 97972425L, 104672425L, 111672425L
        };
        for (int i = 0; i < cumulativeXp.length; i++) {
            boundaries.add(new LevelInfo(cumulativeXp[i], i));
        }
        return boundaries;
    }

    private static List<LevelInfo> createCatacombsSkillBoundaries() {
        List<LevelInfo> boundaries = new ArrayList<>();
        long[] cumulativeXp = {
                0L, 50L, 125L, 235L, 395L, 625L, 955L, 1425L, 2095L, 3045L,
                4385L, 6275L, 8940L, 12700L, 17960L, 25340L, 35640L, 50040L,
                70040L, 97640L, 135640L, 188140L, 259640L, 356640L, 488640L,
                668640L, 911640L, 1239640L, 1684640L, 2284640L, 3084640L,
                4149640L, 5559640L, 7459640L, 9959640L, 13259640L, 17559640L,
                23159640L, 30359640L, 39359640L, 51359640L, 66359640L, 85359640L,
                109559640L, 139559640L, 177559640L, 225559640L, 295559640L,
                360559640L, 453559640L, 569809640L
        };
        for (int i = 0; i < cumulativeXp.length; i++) {
            boundaries.add(new LevelInfo(cumulativeXp[i], i));
        }
        return boundaries;
    }

    private static List<LevelInfo> createRunecraftSkillBoundaries() {
        List<LevelInfo> boundaries = new ArrayList<>();
        long[] cumulativeXp = {
                0L, 50L, 150L, 275L, 435L, 635L, 885L, 1200L, 1600L, 2100L,
                2725L, 3150L, 4510L, 5760L, 7325L, 9325L, 11825L, 14950L,
                18950L, 23950L, 30200L, 38050L, 47850L, 60100L, 75400L, 94500L
        };
        for (int i = 0; i < cumulativeXp.length; i++) {
            boundaries.add(new LevelInfo(cumulativeXp[i], i));
        }
        return boundaries;
    }

    private static List<LevelInfo> createSocialSkillBoundaries() {
        List<LevelInfo> boundaries = new ArrayList<>();
        long[] cumulativeXp = {
                0L, 50L, 150L, 300L, 550L, 1050L, 1800L, 2800L, 4050L, 5550L,
                7550L, 10050L, 13050L, 16800L, 21300L, 27300L, 35300L, 45300L,
                57800L, 72800L, 92800L, 117800L, 147800L, 182800L, 222800L,
                272800L
        };
        for (int i = 0; i < cumulativeXp.length; i++) {
            boundaries.add(new LevelInfo(cumulativeXp[i], i));
        }
        return boundaries;
    }

    private static List<LevelInfo> createGenericSlayerBoundaries() {
        List<LevelInfo> boundaries = new ArrayList<>();
        long[] cumulativeXp = {0L, 5L, 15L, 200L, 1000L, 5000L, 20000L, 100000L, 400000L, 1000000L};
        for (int i = 0; i < cumulativeXp.length; i++) {
            boundaries.add(new LevelInfo(cumulativeXp[i], i));
        }
        return boundaries;
    }

    private static List<LevelInfo> createVampireSlayerBoundaries() {
        List<LevelInfo> boundaries = new ArrayList<>();
        long[] cumulativeXp = {0L, 20L, 75L, 240L, 840L, 2400L};
        for (int i = 0; i < cumulativeXp.length; i++) {
            boundaries.add(new LevelInfo(cumulativeXp[i], i));
        }

        return boundaries;
    }

    private static List<LevelInfo> createCommonPetBoundaries() {
        List<LevelInfo> boundaries = new ArrayList<>();
        long[] cumulativeXp = {
                0L, 0L, 100L, 210L, 330L, 460L, 605L, 765L, 940L, 1130L, 1340L, 1570L, 1820L, 2095L,
                2395L, 2725L, 3085L, 3485L, 3925L, 4415L, 4955L, 5555L, 6215L, 6945L, 7745L,
                8625L, 9585L, 10635L, 11785L, 13045L, 14425L, 15935L, 17585L, 19385L, 21345L,
                23475L, 25785L, 28285L, 30985L, 33905L, 37065L, 40485L, 44185L, 48185L, 52535L,
                57285L, 62485L, 68185L, 74485L, 81485L, 89285L, 97985L, 107685L, 118485L, 130485L,
                143785L, 158485L, 174685L, 192485L, 211985L, 233285L, 256485L, 281685L, 309085L,
                338885L, 371285L, 406485L, 444685L, 486085L, 530885L, 579285L, 631485L, 687685L,
                748085L, 812885L, 882285L, 956485L, 1035685L, 1120385L, 1211085L, 1308285L,
                1412485L, 1524185L, 1643885L, 1772085L, 1909285L, 2055985L, 2212685L, 2380385L,
                2560085L, 2752785L, 2959485L, 3181185L, 3418885L, 3673585L, 3946285L, 4237985L,
                4549685L, 4883385L, 5241085L, 5624785L
        };

        for (int i = 0; i < cumulativeXp.length; i++) {
            boundaries.add(new LevelInfo(cumulativeXp[i], i));
        }

        return boundaries;
    }

    private static List<LevelInfo> createUncommonPetBoundaries() {
        List<LevelInfo> boundaries = new ArrayList<>();
        long[] cumulativeXp = {
                0L, 0L, 175L, 365L, 575L, 805L, 1055L, 1330L, 1630L, 1960L, 2320L, 2720L, 3160L,
                3650L, 4190L, 4790L, 5450L, 6180L, 6980L, 7860L, 8820L, 9870L, 11020L, 12280L,
                13660L, 15170L, 16820L, 18620L, 20580L, 22710L, 25020L, 27520L, 30220L, 33140L,
                36300L, 39720L, 43420L, 47420L, 51770L, 56520L, 61720L, 67420L, 73720L, 80720L,
                88520L, 97220L, 106920L, 117720L, 129720L, 143020L, 157720L, 173920L, 191720L,
                211220L, 232520L, 255720L, 280920L, 308320L, 338120L, 370520L, 405720L, 443920L,
                485320L, 530120L, 578520L, 630720L, 686920L, 747320L, 812120L, 881520L, 955720L,
                1034920L, 1119620L, 1210320L, 1307520L, 1411720L, 1523420L, 1643120L, 1771320L,
                1908520L, 2055220L, 2211920L, 2379620L, 2559320L, 2752020L, 2958720L, 3180420L,
                3418120L, 3672820L, 3945520L, 4237220L, 4548920L, 4882620L, 5240320L, 5624020L,
                6035720L, 6477420L, 6954120L, 7470820L, 8032520L, 8644220L
        };

        for (int i = 0; i < cumulativeXp.length; i++) {
            boundaries.add(new LevelInfo(cumulativeXp[i], i));
        }

        return boundaries;
    }

    private static List<LevelInfo> createRarePetBoundaries() {
        List<LevelInfo> boundaries = new ArrayList<>();
        long[] cumulativeXp = {
                0L, 0L, 275L, 575L, 905L, 1265L, 1665L, 2105L, 2595L, 3135L, 3735L, 4395L, 5125L,
                5925L, 6805L, 7765L, 8815L, 9965L, 11225L, 12605L, 14115L, 15765L, 17565L, 19525L,
                21655L, 23965L, 26465L, 29165L, 32085L, 35245L, 38665L, 42365L, 46365L, 50715L,
                55465L, 60665L, 66365L, 72665L, 79665L, 87465L, 96165L, 105865L, 116665L, 128665L,
                141965L, 156665L, 172865L, 190665L, 210165L, 231465L, 254665L, 279865L, 307265L,
                337065L, 369465L, 404665L, 442865L, 484265L, 529065L, 577465L, 629665L, 685865L,
                746265L, 811065L, 880465L, 954665L, 1033865L, 1118565L, 1209265L, 1306465L,
                1410665L, 1522365L, 1642065L, 1770265L, 1907465L, 2054165L, 2210865L, 2378565L,
                2558265L, 2750965L, 2957665L, 3179365L, 3417065L, 3671765L, 3944465L, 4236165L,
                4547865L, 4881565L, 5239265L, 5622965L, 6034665L, 6476365L, 6953065L, 7469765L,
                8031465L, 8643165L, 9309865L, 10036565L, 10828265L, 11689965L, 12626665L
        };

        for (int i = 0; i < cumulativeXp.length; i++) {
            boundaries.add(new LevelInfo(cumulativeXp[i], i));
        }

        return boundaries;
    }

    private static List<LevelInfo> createEpicPetBoundaries() {
        List<LevelInfo> boundaries = new ArrayList<>();
        long[] cumulativeXp = {
                0L, 0L, 440L, 930L, 1470L, 2070L, 2730L, 3460L, 4260L, 5140L, 6100L, 7150L, 8300L,
                9560L, 10940L, 12450L, 14100L, 15900L, 17860L, 19990L, 22300L, 24800L, 27500L, 30420L,
                33580L, 37000L, 40700L, 44700L, 49050L, 53800L, 59000L, 64700L, 71000L, 78000L, 85800L,
                94500L, 104200L, 115000L, 127000L, 140300L, 155000L, 171200L, 189000L, 208500L, 229800L,
                253000L, 278200L, 305600L, 335400L, 367800L, 403000L, 441200L, 482600L, 527400L, 575800L,
                628000L, 684200L, 744600L, 809400L, 878800L, 953000L, 1032200L, 1116900L, 1207600L, 1304800L,
                1409000L, 1520700L, 1640400L, 1768600L, 1905800L, 2052500L, 2209200L, 2376900L, 2556600L,
                2749300L, 2956000L, 3177700L, 3415400L, 3670100L, 3942800L, 4234500L, 4546200L, 4879900L,
                5237600L, 5621300L, 6033000L, 6474700L, 6951400L, 7468100L, 8029800L, 8641500L, 9308200L,
                10034900L, 10826600L, 11688300L, 12625000L, 13641700L, 14743400L, 15935100L, 17221800L,
                18608500L
        };

        for (int i = 0; i < cumulativeXp.length; i++) {
            boundaries.add(new LevelInfo(cumulativeXp[i], i));
        }

        return boundaries;
    }

    private static List<LevelInfo> createLegendaryPetBoundaries() {
        List<LevelInfo> boundaries = new ArrayList<>();
        Long[] cumulativeXp = {
                0L, 0L, 660L, 1390L, 2190L, 3070L, 4030L, 5080L, 6230L, 7490L,
                8870L, 10380L, 12030L, 13830L, 15790L, 17920L, 20230L, 22730L,
                25430L, 28350L, 31510L, 34930L, 38630L, 42630L, 46980L, 51730L,
                56930L, 62630L, 68930L, 75930L, 83730L, 92430L, 102130L, 112930L,
                124930L, 138230L, 152930L, 169130L, 186930L, 206430L, 227730L,
                250930L, 276130L, 303530L, 333330L, 365730L, 400930L, 439130L,
                480530L, 525330L, 573730L, 625930L, 682130L, 742530L, 807330L,
                876730L, 950930L, 1030130L, 1114830L, 1205530L, 1302730L, 1406930L,
                1518630L, 1638330L, 1766530L, 1903730L, 2050430L, 2207130L, 2374830L,
                2554530L, 2747230L, 2953930L, 3175630L, 3413330L, 3668030L, 3940730L,
                4232430L, 4544130L, 4877830L, 5235530L, 5619230L, 6030930L, 6472630L,
                6949330L, 7466030L, 8027730L, 8639430L, 9306130L, 10032830L, 10824530L,
                11686230L, 12622930L, 13639630L, 14741330L, 15933030L, 17219730L, 18606430L,
                20103130L, 21719830L, 23466530L, 25353230L, 25353230L, 25358785L, 27245485L,
                29132185L, 31018885L, 32905585L, 34792285L, 36678985L, 38565685L, 40452385L,
                42339085L, 44225785L, 46112485L, 47999185L, 49885885L, 51772585L, 53659285L,
                55545985L, 57432685L, 59319385L, 61206085L, 63092785L, 64979485L, 66866185L,
                68752885L, 70639585L, 72526285L, 74412985L, 76299685L, 78186385L, 80073085L,
                81959785L, 83846485L, 85733185L, 87619885L, 89506585L, 91393285L, 93279985L,
                95166685L, 97053385L, 98940085L, 100826785L, 102713485L, 104600185L, 106486885L,
                108373585L, 110260285L, 112146985L, 114033685L, 115920385L, 117807085L, 119693785L,
                121580485L, 123467185L, 125353885L, 127240585L, 129127285L, 131013985L, 132900685L,
                134787385L, 136674085L, 138560785L, 140447485L, 142334185L, 144220885L, 146107585L,
                147994285L, 149880985L, 151767685L, 153654385L, 155541085L, 157427785L, 159314485L,
                161201185L, 163087885L, 164974585L, 166861285L, 168747985L, 170634685L, 172521385L,
                174408085L, 176294785L, 178181485L, 180068185L, 181954885L, 183841585L, 185728285L,
                187614985L, 189501685L, 191388385L, 193275085L, 195161785L, 197048485L, 198935185L,
                200821885L, 202708585L, 204595285L, 206481985L, 208368685L, 210255385L
        };
        for (int i = 0; i < cumulativeXp.length; i++) {
            boundaries.add(new LevelInfo(cumulativeXp[i], i));
        }

        return boundaries;
    }

    public static LevelInfo getLevelInfo(String name, long xp) {
        List<LevelInfo> boundaries = getLevelBoundaries(name, xp);
        for (int i = boundaries.size() - 1; i >= 0; i--) {
            if (xp >= boundaries.get(i).xp) {
                double fill;
                double xpInCurrentLevel;
                double levelXPRange;
                if (i < boundaries.getLast().level) {
                    double currentLevelXP = boundaries.get(i).xp;
                    double nextLevelXP = boundaries.get(i + 1).xp;
                    levelXPRange = nextLevelXP - currentLevelXP;
                    xpInCurrentLevel = xp - currentLevelXP;
                    fill = xpInCurrentLevel / levelXPRange;
                } else {
                    fill = 1.0;
                    xpInCurrentLevel = xp - boundaries.getLast().xp;
                    levelXPRange = boundaries.getLast().xp - boundaries.get(boundaries.size()-2).xp;
                }
                return new LevelInfo(xp, boundaries.get(i).level, fill, xpInCurrentLevel, levelXPRange);
            }
        }
        return new LevelInfo(0L, 0);
    }


    private static List<LevelInfo> getLevelBoundaries(String levelName, long xp) {
        return switch (levelName) {
            case "Vampire" ->  VAMPIRE_SLAYER_BOUNDARIES;
            case "Zombie", "Spider", "Wolf", "Enderman", "Blaze" -> GENERIC_SLAYER_BOUNDARIES;
            case "PET_COMMON" -> COMMON_PET_BOUNDARIES;
            case "PET_UNCOMMON" -> UNCOMMON_PET_BOUNDARIES;
            case "PET_RARE" -> RARE_PET_BOUNDARIES;
            case "PET_EPIC" -> EPIC_PET_BOUNDARIES;
            case "PET_LEGENDARY", "PET_MYTHIC" -> LEGENDARY_PET_BOUNDARIES.subList(0, 101);
            case "PET_GREG" -> LEGENDARY_PET_BOUNDARIES;
            case "Social" -> SOCIAL_SKILL_BOUNDARIES;
            case "Runecraft" -> RUNECRAFT_SKILL_BOUNDARIES;
            case "Catacombs" -> calculateCatacombsSkillBoundaries(xp);
            default -> GENERIC_SKILL_BOUNDARIES;
        };
    }

    private static List<LevelInfo> calculateCatacombsSkillBoundaries(long xp) {
        if (xp >= CATACOMBS_SKILL_BOUNDARIES.getLast().xp) {
            int additionalLevels = (int) ((xp - CATACOMBS_SKILL_BOUNDARIES.getLast().xp) / CATA_XP_PER_LEVEL);

            List<LevelInfo> updatedBoundaries = new ArrayList<>(CATACOMBS_SKILL_BOUNDARIES);
            for (int i = 0; i <= additionalLevels; i++) {
                int level = CATACOMBS_SKILL_BOUNDARIES.getLast().level + i + 1;
                long nextLevelXP = updatedBoundaries.getLast().xp + CATA_XP_PER_LEVEL;
                updatedBoundaries.add(new LevelInfo(nextLevelXP, level));
            }

            return updatedBoundaries;
        }

        return CATACOMBS_SKILL_BOUNDARIES;
    }
}
