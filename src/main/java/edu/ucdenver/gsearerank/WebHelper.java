package edu.ucdenver.gsearerank;

import edu.mit.broad.genome.XLogger;
import org.apache.log4j.Logger;

@SuppressWarnings("unused")
public class WebHelper {

    private Logger log = XLogger.getLogger(WebHelper.class);

    private String anno;
    private String filt;
    private String gmx;
    private String norm;
    private String nperm;
    private String rnk;
    private String scoring_scheme;
    private String make_sets;
    private String plot_top_x;
    private String rnd_seed;
    private String set_max;
    private String set_min;
    private String zip_report;
    private String out;
    private String gui;

    public void setRnkFileNames(String[] rnkFileNames) {
        this.rnkFileNames = rnkFileNames;
    }

    private String[] rnkFileNames;
    private String[] args;

    public WebHelper() {
        anno = "C:/Users/pielk/Google Drive/Costello Lab/GSEARerank/Data/Test/Input/test_anno.csv";
        filt = "C:/Users/pielk/Google Drive/Costello Lab/GSEARerank/Data/Test/Input/test_filter.csv";
        gmx = "C:/Users/pielk/Google Drive/Costello Lab/GSEARerank/Data/Test/Input/test.gmt";
        rnk = "C:/Users/pielk/Google Drive/Costello Lab/GSEARerank/Data/Test/Input/test.rnk";
        norm = "meandiv";
        nperm = "1000";
        scoring_scheme = "weighted";
        make_sets = "true";
        plot_top_x = "0";
        rnd_seed = "timestamp";
        set_max = "500";
        set_min = "15";
        zip_report = "false";
        out = "C:/Users/pielk/Google Drive/Costello Lab/GSEARerank/Data/TesOutput";
        gui = "false";
        args = new String[30];
        rnkFileNames = new String[0];
    }

    public String[] getArgs() {
        return args;
    }

    public String run(){

        int i = 0;
        if(rnkFileNames.length == 0) {
            args[i++] = "-anno";
            args[i++] = anno;
            args[i++] = "-filt";
            args[i++] = filt;
        }
        args[i++] = "-gmx";
        args[i++] = gmx;
        args[i++] = "-norm";
        args[i++] = norm;
        args[i++] = "-nperm";
        args[i++] = nperm;
        args[i++] = "-rnk";
        args[i++] = rnk;
        args[i++] = "-scoring_scheme";
        args[i++] = scoring_scheme;
        args[i++] = "-make_sets";
        args[i++] = make_sets;
        args[i++] = "-plot_top_x";
        args[i++] = plot_top_x;
        args[i++] = "-rnd_seed";
        args[i++] = rnd_seed;
        args[i++] = "-set_max";
        args[i++] = set_max;
        args[i++] = "-set_min";
        args[i++] = set_min;
        args[i++] = "-zip_report";
        args[i++] = zip_report;
//        args[i++] = "-out";
//        args[i++] = out;
        args[i++] = "-gui";
        args[i] = gui;

        if(rnkFileNames.length == 0) {
            log.warn("Using command line implementation");
            GseaPreranked_Rerank.main(args);
        } else {
            log.warn("Using web app implementation");
            GseaPreranked_Rerank.run(args, rnkFileNames);
        }
        return "Done";
    }


    public String getAnno() {
        return anno;
    }

    public void setAnno(String anno) {
        this.anno = anno;
    }

    public String getGmx() {
        return gmx;
    }

    public void setGmx(String gmx) {
        this.gmx = gmx;
    }

    public String getFilt() {
        return filt;
    }

    public void setFilt(String filt) {
        this.filt = filt;
    }

    public String getNorm() {
        return norm;
    }

    public void setNorm(String norm) {
        this.norm = norm;
    }

    public String getNperm() {
        return nperm;
    }

    public void setNperm(String nperm) {
        this.nperm = nperm;
    }

    public String getRnk() {
        return rnk;
    }

    public void setRnk(String rnk) {
        this.rnk = rnk;
    }

    public String getScoring_scheme() {
        return scoring_scheme;
    }

    public void setScoring_scheme(String scoring_scheme) {
        this.scoring_scheme = scoring_scheme;
    }

    public String getMake_sets() {
        return make_sets;
    }

    public void setMake_sets(String make_sets) {
        this.make_sets = make_sets;
    }

    public String getRnd_seed() {
        return rnd_seed;
    }

    public void setRnd_seed(String rnd_seed) {
        this.rnd_seed = rnd_seed;
    }

    public String getPlot_top_x() {
        return plot_top_x;
    }

    public void setPlot_top_x(String plot_top_x) {
        this.plot_top_x = plot_top_x;
    }

    public String getSet_max() {
        return set_max;
    }

    public void setSet_max(String set_max) {
        this.set_max = set_max;
    }

    public String getSet_min() {
        return set_min;
    }

    public void setSet_min(String set_min) {
        this.set_min = set_min;
    }

    public String getZip_report() {
        return zip_report;
    }

    public void setZip_report(String zip_report) {
        this.zip_report = zip_report;
    }

    public String getOut() {
        return out;
    }

    public void setOut(String out) {
        this.out = out;
    }

    public String getGui() {
        return gui;
    }

    public void setGui(String gui) {
        this.gui = gui;
    }

    public static void main(String[] args) {
        WebHelper helper = new WebHelper();
        helper.run();
    }
}
