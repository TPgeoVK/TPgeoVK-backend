package ru.tpgeovk.back.util;

import java.util.ArrayList;
import java.util.List;

public class ModelUtil {

    public static Boolean isAgeSimilar(Integer actorAge, Integer comparingAge) {
        if ((actorAge == null) || (comparingAge == null)) {
            return Boolean.FALSE;
        }

        Integer youngerRadius = 0;
        Integer olderRadius = 0;

        if (actorAge < 12) {
            youngerRadius = 12;
            olderRadius = 5;
        }
        else if ((actorAge >= 12) && (actorAge < 17)) {
            youngerRadius = 4;
            olderRadius = 4;
        }
        else if ((actorAge >= 17) && (actorAge < 25)) {
            youngerRadius = 3;
            olderRadius = 7;
        }
        else if ((actorAge >= 25) && (actorAge < 31)) {
            youngerRadius = 5;
            olderRadius = 8;
        }
        else if ((actorAge >= 31) && (actorAge < 40)) {
            youngerRadius = 7;
            olderRadius = 12;
        }
        else {
            youngerRadius = 10;
            olderRadius = 40;
        }

        if (actorAge >= comparingAge) {
            return  actorAge - comparingAge <= youngerRadius;
        } else {
            return comparingAge - actorAge <= olderRadius;
        }
    }

    public static Integer countCommonGroups(List<String> actorGroups, List<String> comparingGroups) {
        List<Integer> comparingCopy = new ArrayList(comparingGroups);
        comparingCopy.retainAll(actorGroups);
        return comparingCopy.size();
    }
}
