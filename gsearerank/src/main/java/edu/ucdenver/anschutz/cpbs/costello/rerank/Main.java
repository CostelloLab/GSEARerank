package edu.ucdenver.anschutz.cpbs.costello.rerank;

import edu.mit.broad.genome.Conf;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import xtools.api.AbstractTool;
import xtools.gsea.GseaPreranked;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.stream.Stream;

public class Main {

	public static void main(String[] args) throws IOException {
		Reader in = new FileReader("GSEA_input_files/gsea_rerank_sample_input_file.txt");
        Iterable<CSVRecord> exp_vars = CSVFormat.EXCEL.parse(in);

        in = new FileReader("GSEA_input_files/affy_anno_ALL.csv");
        Iterable<CSVRecord> meta_data = CSVFormat.EXCEL.parse(in);

        HashMap<String, Double> dist = new HashMap<>();



		for(CSVRecord record: meta_data){
		    try (Stream<String> lines = Files.lines(Paths.get(record.get("File name")), Charset.defaultCharset())) {
		        for (String line : (Iterable<String>) lines::iterator) {
		            String[] sep_line = line.split("\t");
                    Double previousValue = dist.get(sep_line[0]);
                    if(previousValue == null) previousValue = 0.0;
                    dist.put(sep_line[0], previousValue + Double.parseDouble(sep_line[1]));

                }
            }
        }


		GseaPreranked tool = new GseaPreranked(args);
        tool_main(tool);
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
