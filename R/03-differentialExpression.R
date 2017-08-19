library(GEOquery)
library(affy)
library(limma)
library(plyr)
library(hgu133plus2.db)

# Get annotated study data
all.studies.tab = read.table('Data/GEO/human_GPL570_n6_toAnnotate_completed.txt',
                             sep = '\t', header = T, as.is = T)
all.studies.tab = all.studies.tab[all.studies.tab$keep == 'TRUE',]
all.studies = unique(all.studies.tab$gse)

# Annotate probes
affy.map = read.table('Data/Annotation_files/HG-U133_Plus_2.txt', sep = '\t', stringsAsFactors = F, header = T)
# The probe set names never change, but they can give you an idea of what was known about the 
# sequence at the time of design.
# _at = all the probes hit one known transcript.
# _a = all probes in the set hit alternate transcripts from the same gene
# _s = all probes in the set hit transcripts from different genes
# _x = some probes hit transcripts from different genes
# For HG-U133, the _a designation was not used; an _s probe set on these arrays means the same 
# as an _a on any of the HG-U133 arrays.

# We want to remove all _x and _s probes, because they hit multiple genes
affy.map = affy.map[!grepl('_x|_s', affy.map$PROBEID),]

# Now there's two options - either use the same gene-probe maps across all experiments,
# or choose which probe to keep for each gene based on the gene expression results...
# Can start by joining on the entire affy.map and choose whether to keep at probe or gene 
# level later when doing GSEA.
#affy.map = affy.map[!duplicated(affy.map$HGNC.symbol) & !duplicated(affy.map$PROBEID),]

if (!dir.exists('Results/Tables/DEG_lists/rnk/')){ dir.create('Results/Tables/DEG_lists/rnk/', recursive = T)}
for (study in all.studies){
  
  cat('Opening study', study, '\n')
  
  # Get the extracted gene expression data
  e = readRDS(paste0('RData/GEO_Expression_Matrices/', study, '_expression_matrix.rds'))
  
  # Subset just the samples from desired study
  tab = all.studies.tab[all.studies.tab$gse == study,]
  tab = tab[tab$sample_group != 'X',]
  
  # Define the number of comparisons by the number of digits in the sample group
  comparison.groups = unique(tab$comparison)
  
  # Compare only the groups within that comparison group
  for (comparison in comparison.groups){
    samples.to.compare = tab[tab$comparison == comparison,]
    
    # Define groups to compare: C = control, 1+ = experimental
    groups = unique(samples.to.compare$sample_group)
    expt.groups = groups[!grepl('C', groups)]
    
    # Calculate diff expression for each expt group compared to its control
    for (expt.group in expt.groups){
      
      out.filename = paste0('Results/Tables/DEG_lists/', study, '_DEG_Expt', comparison, 
                            '_Control_vs_Group', expt.group, '.txt')
      out.rnk.gene = paste0('Results/Tables/DEG_lists/rnk/', study, '_DEG_Expt', comparison, 
                       '_Control_vs_Group', expt.group, '_gene.rnk')
      
      # Get CEL filenames from lookup table
      expt.samples = sapply(samples.to.compare[samples.to.compare$sample_group == expt.group,'gsm'], 
                            function(x) paste0(x, '.CEL'))
      control.samples = sapply(samples.to.compare[grepl('C', samples.to.compare$sample_group),'gsm'], 
                               function(x) paste0(x, '.CEL'))
      
      # Design matrix
      e.temp = e[,c(control.samples, expt.samples)]
      conditions = ifelse(colnames(e.temp) %in% control.samples, 'Control', 'Expt')
      design = model.matrix(~0+conditions)
      
      # Contrasts
      contrasts = makeContrasts(conditionsExpt - conditionsControl,
                                levels = design)
      
      # Differential expression
      fit = lmFit(e.temp, design = design)
      fit2 = contrasts.fit(fit, contrasts = contrasts)
      fit2 = eBayes(fit2)
      deg.tab = topTable(fit2, number = 100000, adjust = 'BH')
      deg.tab$PROBEID = row.names(deg.tab)
      suppressMessages(deg.tab <- join(deg.tab, affy.map, type = 'left'))
      
      # Keep only one probe per gene - the probe with highest average expression
      dup.genes = unique(deg.tab$HGNC.symbol[duplicated(deg.tab$HGNC.symbol)])
      max.probes = ddply(deg.tab, .(HGNC.symbol), function(x) x[which.max(x$AveExpr),])
      deg.tab = max.probes
      
      # Save DEG list as text file and ranked .rnk file
      deg.tab = deg.tab[order(deg.tab$logFC, decreasing = T),]
      write.table(deg.tab[,c(7:8,1:6)], out.filename, 
                  sep = '\t', row.names = F, quote = F)
      write.table(deg.tab[,c(8,1)], out.rnk.gene, 
                  sep = '\t', row.names = F, quote = F, col.names = F)
      
      cat('\tData successfully saved to', out.filename, '\n')
      
    }
  }  
  cat('Done analyzing', study, '\n')
}
