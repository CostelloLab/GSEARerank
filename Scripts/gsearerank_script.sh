#!/usr/bin/env bash
gsea_loc=artifacts/GSEA_Rerank_jar/GSEA_Rerank.jar;
main_class=edu.ucdenver.gsearerank.GseaPreranked_Rerank;
gene_sets=Data/Test/Input/test.gmt;
ranked_list=Data/Test/Input/test.rnk;
out_dir=Data/Test/Output/;
annotation_file=Data/Test/Input/test_anno.csv;
filter_file=Data/Test/Input/test_filter.csv;

java -cp ${gsea_loc} -Xmx1g ${main_class} -gmx "$gene_sets" -norm meandiv -nperm 1000 -rnk "$ranked_list" -scoring_scheme weighted -make_sets true -plot_top_x 0 -rnd_seed timestamp -set_max 500 -set_min 15 -zip_report false -out "$out_dir" -gui false -anno ${annotation_file} -filt ${filter_file};