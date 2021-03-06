package edu.ucdenver.gsearerank;

import edu.mit.broad.genome.math.RandomSeedGenerator;
import edu.mit.broad.genome.math.XMath;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

class XMath_Rerank {

    static int[] randomlySampleWithoutReplacement(final int numRndNeeded, final int highestrandomnumExclusive, final RandomSeedGenerator rsgen, HashMap<Integer, Integer[]> dist, Integer[] genesetInds) {
        return randomlySampleWithoutReplacement(numRndNeeded, highestrandomnumExclusive, rsgen.getRandom(), dist, genesetInds);
    }

    private static int[] randomlySampleWithoutReplacement(final int numRndNeeded, final int maxRndNumExclusive, final Random rnd, HashMap<Integer, Integer[]> dist, Integer[] genesetInds) {

        if (maxRndNumExclusive == numRndNeeded) { // no random picking needed, we have exactly as many as asked for
            return XMath.toIndices(maxRndNumExclusive, false);
        }

        if (numRndNeeded > maxRndNumExclusive) {
            throw new IllegalArgumentException("Cannot pick more numbers (no replacement) numRndNeeded: " + numRndNeeded + " than max possible number maxRndNumExclusive: " + maxRndNumExclusive);
        }

        List<Integer> seen = new ArrayList<>(numRndNeeded);
        int[] inds = new int[numRndNeeded];
        int cnt = 0;
        int gsInd;
        int min;
        int max;

        while(cnt < numRndNeeded) {
            gsInd = genesetInds[cnt];
            min = dist.get(gsInd)[0];
            max = dist.get(gsInd)[1];
            int r;
            if (max == min) {
                r = max;
            } else {
                r = rnd.nextInt(max-min) + min;
            }

            if (seen.contains(r)) {
                continue;
            }

            seen.add(r);

            inds[cnt++] = r;

            if (cnt == numRndNeeded) {
                break;
            }
        }

        return inds;
    }
}
