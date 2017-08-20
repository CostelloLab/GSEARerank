library(gplots)
library(plyr)

# Load big table of human GEO expression sets
dat = readRDS('RData/full_human_expression_GEO.rds')

# Filter studies that used GPL570 platform
# 112,899 samples, Affymetrix Human Genome U133 Plus 2.0 Array
gpl570.dat = dat[dat$gsm_gpl == 'GPL570',]

# Add sample counts to each experiment
# 111,396 experiments have at least 6 samples
samples.tab = as.data.frame(table(gpl570.dat$gse))
names(samples.tab) = c('gse', 'gse_total_n')
gpl570.dat = join(gpl570.dat, samples.tab)
gpl570.dat = gpl570.dat[gpl570.dat$gse_total_n >= 6,]
row.names(gpl570.dat) = 1:nrow(gpl570.dat)

# Find studies where at least one sample annotated as vehicle, drug or treatment
QueryStudies = function(studies, query){
     studies.idx = sapply(studies, function(x){
          temp = gpl570.dat[gpl570.dat$gse == x,]
          any(apply(temp, 1, function(y){
               any(grepl(query, y))
          }))
     })
     return(studies[studies.idx])
}
all.studies = unique(gpl570.dat$gse)
query.studies = list(Vehicle = QueryStudies(all.studies, '[Vv]ehicle'),
                     Drug = QueryStudies(all.studies, '[Dd]rug'),
                     Treat = QueryStudies(all.studies, '[Tt]reat'),
                     siRNA = QueryStudies(all.studies, '[Ss][Ii][Rr][Nn][Aa]'),
                     Antibody = QueryStudies(all.studies, '[Aa]ntibod'))

# Annotate studies
gpl570.dat$query_vehicle = gpl570.dat$gse %in% query.studies$Vehicle
gpl570.dat$query_drug = gpl570.dat$gse %in% query.studies$Drug
gpl570.dat$query_treat = gpl570.dat$gse %in% query.studies$Treat
gpl570.dat$query_sirna = gpl570.dat$gse %in% query.studies$siRNA
gpl570.dat$query_antibody = gpl570.dat$gse %in% query.studies$Antibody

# Save some venn diagrams of study annotations
if (!dir.exists('Results/Plots/Venns/')){ dir.create('Results/Plots/Venns/', recursive = T)}
pdf('Results/Plots/Venns/vehicle_drug_treat.pdf')
v = venn(query.studies[c('Vehicle', 'Drug', 'Treat')])
dev.off()

pdf('Results/Plots/Venns/antibody_drug_treat.pdf')
v2 = venn(query.studies[c('Antibody', 'Drug', 'Treat')])
dev.off()

# Write out full table and a filtered table for annotating manually
if (!dir.exists('Data/GEO')){ dir.create('Data/GEO', recursive = T)}
write.table(gpl570.dat, 'Data/GEO/human_GPL570_n6_full.txt',
            sep = '`', row.names = F, quote = F)
write.table(gpl570.dat[,c('gse', 'gse_title', 'gse_summary', 'gsm', 'gsm_title', 
                          'gsm_description', grep('query', names(gpl570.dat), value = T))], 
            'Data/GEO/human_GPL570_n6_toAnnotate.txt',
            sep = '`', row.names = F, quote = F)

