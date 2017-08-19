package edu.ucdenver.gsearerank;

import edu.mit.broad.genome.alg.gsea.GeneSetCohortGenerator;
import edu.mit.broad.genome.math.RandomSeedGenerator;
import edu.mit.broad.genome.math.RandomSeedGenerators;
import edu.mit.broad.genome.objects.FeatureAnnot;
import edu.mit.broad.genome.objects.FeatureAnnotImpl;
import edu.mit.broad.genome.objects.GeneSet;
import edu.mit.broad.genome.objects.RankedList;
import edu.mit.broad.genome.objects.esmatrix.db.EnrichmentDb;
import edu.mit.broad.genome.objects.strucs.CollapsedDetails;
import edu.mit.broad.genome.parsers.EdbFolderParser;
import edu.mit.broad.genome.reports.EnrichmentReports;
import edu.mit.broad.genome.reports.api.ReportIndexState;
import edu.mit.broad.genome.reports.pages.HtmlReportIndexPage;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import xtools.api.param.*;
import xtools.gsea.AbstractGseaTool;

import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Properties;
import java.util.stream.Stream;

@SuppressWarnings({"SpellCheckingInspection", "unused"})
public class GseaPreranked_Rerank extends AbstractGseaTool {
    private final RankedListReqdParam fRankedListParam = new RankedListReqdParam();
    private final IntegerParam fShowDetailsForTopXSetsParam;
    private final BooleanParam fMakeZippedReportParam;
    private final BooleanParam fMakeGeneSetReportsParam;
    private final BooleanParam fCreateSvgsParam;
    private final StringInputParam fAltDelimParam;
    private GeneSet[] fOrigGeneSets;

    private HashMap<String, Integer[]> dist;

    private GseaPreranked_Rerank(Properties properties) {
        this.fShowDetailsForTopXSetsParam = new IntegerParam("plot_top_x", "Plot graphs for the top sets of each phenotype", "Plot GSEA mountain and related plots for the top sets of each phenotype", 20, false, Param.ADVANCED);
        this.fMakeZippedReportParam = ParamFactory.createZipReportParam(false);
        this.fMakeGeneSetReportsParam = new BooleanParam("make_sets", "Make detailed gene set report", "Create detailed gene set reports (heat-map, mountain plot etc) for every enriched gene set", true, false, Param.ADVANCED);
        this.fCreateSvgsParam = new BooleanParam("create_svgs", "Create SVG plot images", "Create SVG plot images along with PNGs (GZ compressed to save space as these are very large)", false, false, Param.ADVANCED);
        this.fAltDelimParam = new StringInputParam("altDelim", "Alternate delimiter", "Optional alternate delimiter character for gene set names instead of comma", null, false, new char[]{';'}, Param.ADVANCED);
        super.init(properties);
    }

    private GseaPreranked_Rerank(String[] args, HashMap<String, Integer[]> dist) {
        this.dist = dist;
        this.fShowDetailsForTopXSetsParam = new IntegerParam("plot_top_x", "Plot graphs for the top sets of each phenotype", "Plot GSEA mountain and related plots for the top sets of each phenotype", 20, false, Param.ADVANCED);
        this.fMakeZippedReportParam = ParamFactory.createZipReportParam(false);
        this.fMakeGeneSetReportsParam = new BooleanParam("make_sets", "Make detailed gene set report", "Create detailed gene set reports (heat-map, mountain plot etc) for every enriched gene set", true, false, Param.ADVANCED);
        this.fCreateSvgsParam = new BooleanParam("create_svgs", "Create SVG plot images", "Create SVG plot images along with PNGs (GZ compressed to save space as these are very large)", false, false, Param.ADVANCED);
        this.fAltDelimParam = new StringInputParam("altDelim", "Alternate delimiter", "Optional alternate delimiter character for gene set names instead of comma", null, false, new char[]{';'}, Param.ADVANCED);
        super.init(args);
    }

    public GseaPreranked_Rerank() {
        this.fShowDetailsForTopXSetsParam = new IntegerParam("plot_top_x", "Plot graphs for the top sets of each phenotype", "Plot GSEA mountain and related plots for the top sets of each phenotype", 20, false, Param.ADVANCED);
        this.fMakeZippedReportParam = ParamFactory.createZipReportParam(false);
        this.fMakeGeneSetReportsParam = new BooleanParam("make_sets", "Make detailed gene set report", "Create detailed gene set reports (heat-map, mountain plot etc) for every enriched gene set", true, false, Param.ADVANCED);
        this.fCreateSvgsParam = new BooleanParam("create_svgs", "Create SVG plot images", "Create SVG plot images along with PNGs (GZ compressed to save space as these are very large)", false, false, Param.ADVANCED);
        this.fAltDelimParam = new StringInputParam("altDelim", "Alternate delimiter", "Optional alternate delimiter character for gene set names instead of comma", null, false, new char[]{';'}, Param.ADVANCED);
        this.declareParams();
    }

    public void execute() throws Exception {
        ReportIndexState state = new ReportIndexState(true, false, false, createHeader(this.fRankedListParam));
        this.startExec(state);
        RankedList fullRl = this.uniquize(this.fRankedListParam.getRankedList());
        if (fullRl.getSize() == 0) {
            throw new IllegalArgumentException("The chip and the ranked list did not match");
        } else {
            CollapsedDetails.Ranked cd = this.getRankedList(fullRl);
            if (this.fAltDelimParam.isSpecified() && StringUtils.isNotBlank(this.fAltDelimParam.getValue().toString())) {
                this.fGeneSetMatrixParam.setAlternateDelimiter(this.fAltDelimParam.getValue().toString());
            }

            this.fOrigGeneSets = this.fGeneSetMatrixParam.getGeneSetMatrixCombo(true).getGeneSets();
            GeneSet[] gsets = Helper.getGeneSets(cd.getRankedList(), this.fOrigGeneSets, this.fGeneSetMinSizeParam, this.fGeneSetMaxSizeParam);
            ParamFactory.checkAndBarfIfZeroSets(gsets);
            HtmlReportIndexPage htmlReportIndexPage = this.fReport.getIndexPage();
            this.execute_one(cd, gsets, htmlReportIndexPage);
            if (this.fMakeZippedReportParam.isTrue()) {
                this.fReport.closeReport(true);
                this.fReport.zipReport();
            }

            this.doneExec();
        }
    }

    private void execute_one(final CollapsedDetails fullRL,
                             final GeneSet[] gsets,
                             final HtmlReportIndexPage reportIndexPage) throws Exception {

        final int nperms = fNumPermParam.getIValue();
        final int topXSets = fShowDetailsForTopXSetsParam.getIValue();
        final RandomSeedGenerator rst = fRndSeedTypeParam.createSeed();
        final GeneSetCohortGenerator gcohgen = fGcohGenReqdParam.createGeneSetCohortGenerator(false);
        final int minSize = fGeneSetMinSizeParam.getIValue();
        final int maxSize = fGeneSetMaxSizeParam.getIValue();
        final boolean createSvgs = fCreateSvgsParam.isSpecified() && fCreateSvgsParam.isTrue();
        RankedList rl = ((CollapsedDetails.Ranked) fullRL).getRankedList();
        FeatureAnnot fann = new FeatureAnnotImpl(rl.getName(), rl.getRankedNames(), null);

        final KSTests_Rerank tests = new KSTests_Rerank(getOutputStream());
        
        // If we have a RandomSeedGenerator.Timestamp instance, save the timestamp for later reference
        if (rst instanceof RandomSeedGenerators.Timestamp) {
            fReport.addComment("Timestamp used as the random seed: " + 
                    ((RandomSeedGenerators.Timestamp)rst).getTimestamp());
        }

        EnrichmentDb edb = tests.executeGsea(
                rl,
                gsets,
                nperms,
                rst,
                null,
                gcohgen,
                dist
        );

        // Make the report
        EnrichmentReports.Ret ret = EnrichmentReports.createGseaLikeReport(edb, getOutputStream(),
                fullRL, reportIndexPage, false,
                fReport, topXSets, minSize, maxSize,
                fMakeGeneSetReportsParam.isTrue(), fMakeZippedReportParam.isTrue(),
                createSvgs, fOrigGeneSets, "PreRanked", fNormModeParam.getNormModeName(), fann);

        // Make an edb folder thing
        new EdbFolderParser().export(ret.edb, ret.savedInDir);

    }

    protected Param[] getAdditionalParams() {
        return new Param[]{this.fRankedListParam, this.fShowDetailsForTopXSetsParam, this.fMakeZippedReportParam, this.fMakeGeneSetReportsParam, this.fCreateSvgsParam, this.fAltDelimParam};
    }

    public String getDesc() {
        return "Run GSEA on a pre-ranked (with external tools) gene list";
    }

    private CollapsedDetails.Ranked getRankedList(RankedList origRL) throws Exception {
        CollapsedDetails.Ranked cd = new CollapsedDetails.Ranked();
        cd.orig = origRL;
        cd.wasCollapsed = false;
        cd.collapsed = origRL;
        return cd;
    }

    private static String createHeader(final RankedListReqdParam dsr) {
        try {
            if (dsr.isSpecified()) {
                RankedList rl = dsr.getRankedList();

                return "<div id=\"footer\" style=\"width: 905; height: 35\">\n" +
                        "<h3 style=\"text-align: left\"><font color=\"#808080\">GSEA Report for " +
                        "Dataset " + rl.getName() + "</font></h3>\n" + "</div>";
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }

        return null;
    }

    public static void main(String[] args) {
        String vars_file_name = "Input/gsea_rerank_sample_input_file.csv";
        String meta_data_file_name = "Input/test_anno.csv";


        Iterable<CSVRecord> exp_vars = null;
        try {
            exp_vars = CSVFormat.EXCEL.withHeader().parse(new FileReader(vars_file_name));
        } catch (IOException e) {
            e.printStackTrace();
        }
        Iterable<CSVRecord> meta_data = null;
        try {
            meta_data = CSVFormat.EXCEL.withHeader().parse(new FileReader(meta_data_file_name));
        } catch (IOException e) {
            e.printStackTrace();
        }

        HashMap<String, Integer[]> dist = get_dist(meta_data, exp_vars);

//        GseaPreranked tool_1 = new GseaPreranked(args);
        GseaPreranked_Rerank tool = new GseaPreranked_Rerank(args, dist);
        tool_main(tool);
    }

    private static HashMap<String, Integer[]> get_dist(Iterable<CSVRecord> meta_data, Iterable<CSVRecord> exp_vars) {
        HashMap<String, Integer[]> dist = new HashMap<>();


        for(CSVRecord record: meta_data){
            if (check_filter(exp_vars, record)) {
                try (Stream<String> lines = Files.lines(Paths.get(record.get("File Name")), Charset.defaultCharset())) {
                    int rank = 1;
                    for (String line : (Iterable<String>) lines::iterator) {
                        String[] sep_line = line.split("\t");
                        String gene_name = sep_line[0];
                        if (dist.get(gene_name) == null) {
                            Integer[] min_max = new Integer[2];
                            min_max[0] = Integer.MAX_VALUE;
                            min_max[1] = Integer.MIN_VALUE;
                            dist.put(gene_name, min_max);
                        }
                        if (dist.get(gene_name)[0] > rank){
                            dist.get(gene_name)[0] = rank;
                        }
                        if (dist.get(gene_name)[1] < rank){
                            dist.get(gene_name)[1] = rank;
                        }
                        rank++;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return dist;
    }


    private static Boolean check_filter(Iterable<CSVRecord> exp_vars, CSVRecord record){
        boolean get_file = true;
        for (CSVRecord var : exp_vars) {
            String var_name = var.get("Var_Name");
            String var_value = var.get("Var_Value");
            if (! record.get(var_name).equals(var_value)) {
                get_file = false;
            }
        }
        return get_file;
    }
}