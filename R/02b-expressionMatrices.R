# Calculate expression matrix first and save as RDS
library(affy)
library(limma)

# Get annotated study data
all.studies.tab = read.table('Data/GEO/human_GPL570_n6_toAnnotate_completed.txt',
                             sep = '\t', header = T, as.is = T)
all.studies.tab = all.studies.tab[all.studies.tab$keep == 'TRUE',]
all.studies = unique(all.studies.tab$gse)

if (!dir.exists('RData/GEO_Expression_Matrices')){ dir.create('RData/GEO_Expression_Matrices', recursive = T)}

for (study in all.studies){
  
  out.filename = paste0('RData/GEO_Expression_Matrices/', study, '_expression_matrix.rds')
  if (file.exists(out.filename)){
    cat('Expression matrix already exists for', study, '\n')
    next
  } else{
    cat('Opening study', study, '\n')
    
    # Extract gene expression data
    study.dir = paste0('Data/GEO/CELs/', study)
    cel = ReadAffy(celfile.path = study.dir)
    r = rma(cel)
    e = exprs(r)
    
    saveRDS(as.data.frame(e), out.filename)
    cat('Saved expression data for study', study, '\n')
  }
}