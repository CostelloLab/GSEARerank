my_dir=~/;

#####################################
# Set simple variables. These can mostly be left alone.
java_loc=${my_dir}/gsea2-2.2.3.jar; # .jar file that runs GSEA. Downloaded from http://software.broadinstitute.org/gsea/downloads.jsp
Xmx=xtools.gsea.GseaPreranked; 
collapse=false;
mode=Max_probe;
norm=meandiv;
nperm=1000;
scoring_scheme=weighted;
include_only_symbols=true;
make_sets=true;
plot_top_x=0;
rnd_seed=timestamp;
set_max=500;
set_min=15;
zip_report=false;
gui=false;

####################################
## Get lists of gene sets.
## all.all.gmt is a compilation of many collections of gene sets. Warning: takes a long time to run.

# gene_sets=$(ls -dm ${my_dir}/gene_sets/all/all.all.gmt | tr -d '\n')
# gene_sets=$(find ${my_dir}/gene_sets/good/ -type f | tr '\n' ',')
gene_sets=$(find ${my_dir}/gene_sets/bp/ -type f | tr '\n' ',')

####################################
## Make a new directory to output results to. This directory is called {my_dir}_run(i).
out_dir=${my_dir}/Results/GSEA;

## Make a new directory to output results to. This directory is called {my_dir}_run(i).
## I find this process helpful so I can run GSEA using different combinations of gene sets
## without losing my previous data.
i=0;
while [ -d ${out_dir}/run_${i} ];
do
	## If a file called gene_set_list exists in a previous run directory that contains the names of
	## the same gene set collections you've already run, it will use that directory again.
	if grep -s "$gene_sets" "${out_dir}/run_${i}/gene_set_list.txt"
	then
		break
	fi
	i=$((i+1))
done
out_dir=${out_dir}/run_${i}
if [ -d "$out_dir" ]; then
	echo "$out_dir already exists"
else
	mkdir $out_dir
	echo "Made $out_dir";
fi

## Write your gene set collection names to a file in the output directory
echo $gene_sets > "${out_dir}/gene_set_list.txt"

###########################################
## Determine how many processors can be used during multiprocessing. I leave one available so I can 
## still do stuff withoutmy computer grinding to a halt.
## Get number of processes and leave one available.
num_proc=$(cat /proc/cpuinfo | awk '/^processor/{print $3}' | tail -1)

## cd into the directory containing the .rnk files
cd ${my_dir}/Results/Tables/DEG_lists/rnk/gene/

## I am  working on getting this to check if GSEA has already been run on a particular rnk file in order
## to try to avoid rerunning things.
#existing_results=$(find "${out_dir}" -maxdepth 1 -type d | tr "\n" " | ")
#rnks=$(find *.rnk -type f -maxdepth 1 ! -name "$existing_results");
#echo "$existing_results"
#rnks | head -1 | xargs -I {} --max-procs=$num_proc java -cp $java_loc -Xmx6g $Xmx512m -gmx "$gene_sets" -collapse $collapse -mode $mode -norm $norm -nperm $nperm -rnk {} -scoring_scheme $scoring_scheme -rpt_label {} -include_only_symbols $include_only_symbols -make_sets $make_sets -plot_top_x $plot_top_x -rnd_seed $rnd_seed -set_max $set_max -set_min $set_min -zip_report $zip_report -out ${out_dir} -gui $gui

## Run paralellized GSEA.
## Explanation:
## ls feeds the .rnk files into xargs
## xargs uses the option --max-procs to parallelize java
## {} determines where the .rnk file is used in the script
##
## Notes:
## Change -Xmx4g (the actual option name, not the variable name) so that it suits your needs. 
## 4g is usually plenty. Remember that you need num_proc X 4g available memory.
ls *.rnk | xargs -I {} --max-procs=$num_proc java -cp $java_loc -Xmx6g $Xmx -gmx "$gene_sets" -collapse $collapse -mode $mode -norm $norm -nperm $nperm -rnk {} -scoring_scheme $scoring_scheme -rpt_label {} -include_only_symbols $include_only_symbols -make_sets $make_sets -plot_top_x $plot_top_x -rnd_seed $rnd_seed -set_max $set_max -set_min $set_min -zip_report $zip_report -out "$out_dir" -gui $gui
## Use this line to run just one file for testing putposes.
#ls *.rnk | head -1 | xargs -I {} --max-procs=$num_proc java -cp $java_loc -Xmx6g $Xmx -gmx "$gene_sets" -collapse $collapse -mode $mode -norm $norm -nperm $nperm -rnk {} -scoring_scheme $scoring_scheme -rpt_label {} -include_only_symbols $include_only_symbols -make_sets $make_sets -plot_top_x $plot_top_x -rnd_seed $rnd_seed -set_max $set_max -set_min $set_min -zip_report $zip_report -out "$out_dir" -gui $gui
