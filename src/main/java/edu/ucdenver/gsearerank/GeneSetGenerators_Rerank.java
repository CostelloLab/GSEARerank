package edu.ucdenver.gsearerank;

import edu.mit.broad.genome.NamingConventions;
import edu.mit.broad.genome.XLogger;
import edu.mit.broad.genome.math.RandomSeedGenerator;
import edu.mit.broad.genome.objects.FSet;
import edu.mit.broad.genome.objects.GeneSet;
import edu.mit.broad.genome.objects.RankedList;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

class GeneSetGenerators_Rerank {

    private static Logger klog = XLogger.getLogger(GeneSetGenerators_Rerank.class);


    static GeneSet[] createRandomGeneSetsFixedSize(final int numRndGeneSets,
                                                   final RankedList rl,
                                                   final GeneSet gset,
                                                   final RandomSeedGenerator rst,
                                                   final HashMap<String, Integer[]> dist) {

        String prefix = NamingConventions.removeExtension(gset);

        // Qualify as all members may not be in the dataset
        int nmembers = gset.getNumMembers(rl);

        GeneSet[] rndgsets = new GeneSet[numRndGeneSets];

        HashMap<Integer, Integer[]> numKey_dist = new HashMap<>();
        if (dist != null) {
            for (int i = 0; i < rl.getSize(); i++) {
                numKey_dist.put(i, dist.get(rl.getRankName(i)));
            }
        }
        else {
            numKey_dist = null;
        }

        for (int g = 0; g < numRndGeneSets; g++) {
            int[] randomrowindices;
            // IMP random from 0 to nrows not nmembers. duh!.

            Integer[] genesetInds = new Integer[nmembers];
            for (int i = 0; i < nmembers; i ++) {
                genesetInds[i] = rl.getRank(gset.getMember(i));
            }

            randomrowindices = XMath_Rerank.randomlySampleWithoutReplacement(nmembers, rl.getSize(), rst, numKey_dist, genesetInds);

            if (randomrowindices.length != nmembers) {
                throw new IllegalStateException("random indices generated: " + randomrowindices.length + " not equal to # members: " + nmembers);
            }

            Set<String> members = new HashSet<>();

            for (int i = 0; i < nmembers; i++) {
                members.add(rl.getRankName(randomrowindices[i]));
            }

            if (members.size() != nmembers) {
                klog.warn("Bad randomization -- repeated rnd members were made members: " + members.size() + " but wanted: " + nmembers);
            }

            rndgsets[g] = new FSet(prefix + "_" + g, members);
        }

        return rndgsets;
    }
}