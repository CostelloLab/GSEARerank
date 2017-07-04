library(GEOmetadb)
library(plyr)
if(!file.exists('GEOmetadb.sqlite')) getSQLiteFile()

# Look at GEOmetadb vignette for getting annotation db names too
# https://www.bioconductor.org/packages/release/bioc/vignettes/GEOmetadb/inst/doc/GEOmetadb.html#getting-the-geometadb-database

# Query on GEO website is
# cel[Supplementary Files] AND drug[All Fields] AND "Homo sapiens"[porgn]

# gse = series
# gsm = samples
# gds = geo data set
# gpl = platform

# Connect to GEO SQLite database
con = dbConnect(SQLite(),'GEOmetadb.sqlite')
# Fields available for query
fields = columnDescriptions(sqlite_db_name='GEOmetadb.sqlite')     # Actually no gse column in gsm table

# ----------------- GET TABLES FOR GSE, GSM, AND GSE-GSM --------------------- #

# Query GSE table to find all series of expression data
gse.tab = dbGetQuery(con,
                     "select title,gse,pubmed_id,summary,type,overall_design,repeats,repeats_sample_list,variable,variable_description,supplementary_file from gse")

# Query gse_gsm table to connect series to samples
gse.gsm.tab = dbGetQuery(con,
                         "select gse,gsm from gse_gsm")

# Query GSM table
gsm.tab = dbGetQuery(con,
                     paste("select ID,title,gsm,gpl,submission_date,type,organism_ch1,molecule_ch1,treatment_protocol_ch1,description,data_processing,data_row_count,supplementary_file",
                           "from gsm where supplementary_file like '%CEL.gz'"))

# ------------------- MERGE TABLES WITH RELEVANT INFO ------------------------ #

# Rename columns (except gsm and gse columns) to prevent issues merging tables
gse.idx = grep('^gse$', names(gse.tab))
gsm.idx = grep('^gsm$', names(gsm.tab))
names(gse.tab)[-gse.idx] = sapply(names(gse.tab)[-gse.idx], function(x) paste0('gse_', x))
names(gsm.tab)[-gsm.idx] = sapply(names(gsm.tab)[-gsm.idx], function(x) paste0('gsm_', x))

# Filter samples by Homo Sapiens
merged.tab = gsm.tab[gsm.tab$gsm_organism_ch1 == 'Homo sapiens',]

# Filter series by expression profiling with array
gse.tab = gse.tab[gse.tab$gse_type == 'Expression profiling by array',]

# Add GSM to GSE tab and join
gse.tab = join(gse.tab, gse.gsm.tab)
merged.tab = join(merged.tab, gse.tab, type = 'inner')

# Add sample counts to each experiment
samples.tab = as.data.frame(table(merged.tab$gse))
names(samples.tab) = c('gse', 'gse_total_n')
final.tab = join(merged.tab, samples.tab)

# Save this filtered table, 6,873 experiments total
if (!dir.exists('RData')){ dir.create('RData')}
saveRDS(final.tab, 'RData/full_human_expression_GEO.rds')

