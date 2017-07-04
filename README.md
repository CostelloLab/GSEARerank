# GSEA Rerank

Harrison Pielke-Lombardo, Rani Powers, Aik Choon Tan, James Costello

University of Colorado Anschutz Medical Campus

### Scripts

#### R/00-queryGEO.R (possibly deprecated, see notes below)

Queries GEO for all human gene expression studies that have .CEL files available. Saves a table of the studies and their descriptions to `RData/full_human_expression_GEO.rds`. NOTE: The `GEOmetadb` package seems to have trouble connecting to the GEO database now. Rani has a local copy of the database downloaded previously (5 GB) that was used to generate `RData/full_human_expression_GEO.rds`. The analysis can be started with the `R/01-annotateStudies.R` script.

#### R/01-annotateStudies.R

Filters the big table of studies for those using Affymetrix Human Genome U133 Plus 2.0 Array with at least n = 6 samples. Annotate the studies for those that contain 'vehicle,' 'drug', 'treatment,' etc in the titles or descriptions. Write out table as a text file for manual annotation of control vs drug-treated samples.