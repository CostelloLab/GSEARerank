# GSEA Rerank

Harrison Pielke-Lombardo, Rani Powers, Aik Choon Tan, James Costello

University of Colorado Anschutz Medical Campus

### R Scripts

#### R/00-queryGEO.R (possibly deprecated, see notes below)

Queries GEO for all human gene expression studies that have .CEL files available. Saves a table of the studies and their descriptions to `RData/full_human_expression_GEO.rds`. NOTE: The `GEOmetadb` package seems to have trouble connecting to the GEO database now. Rani has a local copy of the database downloaded previously (5 GB) that was used to generate `RData/full_human_expression_GEO.rds`. The analysis can be started with the `R/01-annotateStudies.R` script.

#### R/01-annotateStudies.R

Filters the big table of studies for those using Affymetrix Human Genome U133 Plus 2.0 Array with at least n = 6 samples. Annotate the studies for those that contain 'vehicle,' 'drug', 'treatment,' etc in the titles or descriptions. Write out table as a text file for manual annotation of control vs drug-treated samples.

Studies were manually annotated and saved in `Data/GEO/human_GPL570_n6_toAnnotate_withMetaData.xlsx`. Relevant columns from this table were saved to `Data/GEO/human_GPL570_n6_toAnnotate_completed.txt` for use in the next R script.

#### R/02-downloadStudies.R

For each of the annotated studies in `Data/GEO/human_GPL570_n6_toAnnotate_completed.txt`, this script downloads the CEL files from GEO. A directory for each study is created inside `Data/GEO/` and the CEL files are downloaded and unzipped.

#### R/02b-expressionMatrices.R

Compiles the set of CEL files from each experiment and extracts them into a single expression matrix saves in `Data/GEO/Expression_Matrices`.

#### R/03-differentialExpression.R

Creates a ranked differentially expression gene list for each treated-vs-control comparison in each study. Saves the .rnk files to `Results/Tables/DEG_lists/rnk/`.

#### R/04-convert_gsea_xls_to_txt.R

Once GSEA has been performed on all of the ranked lists from the previous step, this script converts all of the xls files produced by GSEA into txt files to `Results/GSEA_DSigDB/txt/`. Also saves a final Comparison_Key.txt describing the studies.
