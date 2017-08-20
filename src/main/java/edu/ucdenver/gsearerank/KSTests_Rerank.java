package edu.ucdenver.gsearerank;

import edu.mit.broad.genome.XLogger;
import edu.mit.broad.genome.alg.Metrics;
import edu.mit.broad.genome.alg.gsea.GeneSetCohort;
import edu.mit.broad.genome.alg.gsea.GeneSetCohortGenerator;
import edu.mit.broad.genome.alg.gsea.KSCore;
import edu.mit.broad.genome.alg.gsea.KSTests;
import edu.mit.broad.genome.math.*;
import edu.mit.broad.genome.objects.*;
import edu.mit.broad.genome.objects.esmatrix.db.*;
import org.apache.log4j.Logger;

import java.io.PrintStream;
import java.util.HashMap;

class KSTests_Rerank extends KSTests {

    private final Logger log = XLogger.getLogger(this.getClass());

    private final KSCore core;

    private static final int LOG_FREQ = 5;

    private PrintStream sout;

    KSTests_Rerank(final PrintStream os) {
        this.sout = os;
        this.core = new KSCore();
    }

    EnrichmentDb executeGsea(final RankedList rl_real,
                             final GeneSet[] gsets,
                             final int nperm,
                             final RandomSeedGenerator rst,
                             final GeneSetCohortGenerator gcohgen,
                             final HashMap<String, Integer[]> dist) throws Exception {

        log.debug("!!!! Executing for: " + rl_real.getName() + " # features: " + rl_real.getSize());

        EnrichmentResult[] results = shuffleGeneSet_precannedRankedList(nperm, rl_real, gsets, gcohgen, rst, dist);
        return new EnrichmentDbImpl_one_shared_rl(rl_real.getName(),
                rl_real, null, null, results, new LabelledVectorProcessors.None(),
                new Metrics.None(),
                new HashMap(),
                SortMode.REAL,
                Order.DESCENDING, nperm, null);
    }
    // this is the CORE method
    private EnrichmentResult[] shuffleGeneSet_precannedRankedList(final int nperm,
                                                                  final RankedList rlReal,
                                                                  final GeneSet[] gsetsReal,
                                                                  final GeneSetCohortGenerator gcohgen,
                                                                  final RandomSeedGenerator rst,
                                                                  final HashMap<String, Integer[]> dist) {

        final EnrichmentResult[] results = new EnrichmentResult[gsetsReal.length];
        final GeneSetCohort gcohReal = gcohgen.createGeneSetCohort(rlReal, gsetsReal, false, true);

        final EnrichmentScore[] real_scores = core.calculateKSScore(gcohReal, true, true); // @note ususally always store deep for the real one

        // The make rnd gene sets for every real one
        for (int g = 0; g < gsetsReal.length; g++) {
            if (g % LOG_FREQ == 0) {
                sout.println("shuffleGeneSet for GeneSet " + (g + 1) + "/" + gsetsReal.length + " nperm: " + nperm);
            }

            // now create random GeneSets and calc the ksscore for every rnd GeneSet
            //log.debug("started gsets");
            Vector rndEss;
            if (nperm > 0) {
                final GeneSet[] rndgsets = GeneSetGenerators_Rerank.createRandomGeneSetsFixedSize(nperm, rlReal, gsetsReal[g], rst, dist);
                final GeneSetCohort gcohRnd = gcohReal.clone(rndgsets, false);
                rndEss = new Vector(rndgsets.length);
                final EnrichmentScore[] rnds = core.calculateKSScore(gcohRnd, false); // never store deep for rnds
                for (int r = 0; r < rndgsets.length; r++) {
                    rndEss.setElement(r, rnds[r].getES());
                }
            } else {
                rndEss = new Vector(0);
            }

            results[g] = new EnrichmentResultImpl(rlReal, null,
                    gsetsReal[g], null, real_scores[g], rndEss);
        }

        return results;
    }
}
