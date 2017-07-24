package edu.ucdenver.anschutz.cpbs.costello.rerank;

import edu.mit.broad.genome.Conf;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import xtools.api.AbstractTool;
import xtools.gsea.GseaPreranked;

import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.stream.Stream;

public class Rerank {

	public static void main(String[] args) throws IOException {
        String vars_file_name = "Input/gsea_rerank_sample_input_file.csv";
        String meta_data_file_name = "Input/test_anno.csv";

        Iterable<CSVRecord> exp_vars = CSVFormat.EXCEL.withHeader().parse(new FileReader(vars_file_name));
        Iterable<CSVRecord> meta_data = CSVFormat.EXCEL.withHeader().parse(new FileReader(meta_data_file_name));

        HashMap<String, Double> dist = get_dist(meta_data, exp_vars);

		GseaPreranked tool = new GseaPreranked(args);
//        tool_main(tool);
	}


	private static HashMap<String, Double> get_dist(Iterable<CSVRecord> meta_data, Iterable<CSVRecord> exp_vars) {
        HashMap<String, Double> dist = new HashMap<>();


        int num_rnk_files = 0;
        for(CSVRecord record: meta_data){
            if (check_filter(exp_vars, record)) {
                try (Stream<String> lines = Files.lines(Paths.get(record.get("File Name")), Charset.defaultCharset())) {
                    int rank = 1;
                    for (String line : (Iterable<String>) lines::iterator) {
                        String[] sep_line = line.split("\t");
                        Double previousValue = dist.get(sep_line[0]);
                        if (previousValue == null) previousValue = 0.0;
                        dist.put(sep_line[0], previousValue + rank);
                        rank++;
                    }
                    num_rnk_files++;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        for (String name: dist.keySet()){
            dist.put(name, dist.get(name)/num_rnk_files);
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


	private static void tool_main(final AbstractTool tool) {

        if (tool == null) {
            throw new IllegalArgumentException("Param tool cannot be null");
        }

        boolean was_error = false;
        try {

            tool.execute();

        } catch (Throwable t) {
            // if the rpt dir was made try to rename it so that easily identifiable
            was_error = true;
            t.printStackTrace();
        }

        if (was_error && tool.getReport() != null) {
            tool.getReport().setErroredOut();
        }

        if (tool.getParamSet().getGuiParam().isFalse()) {
            Conf.exitSystem(was_error);
        }
    }

}
