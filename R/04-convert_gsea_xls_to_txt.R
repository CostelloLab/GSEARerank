# Convert xls files produced by GSEA to txt
all.xls.files = list.files('Results/GSEA_DSigDB', 
                           pattern = 'xls', recursive = T, full.names = T)

# Rename each file to be a txt file
new.txt.files = gsub('xls', 'txt', all.xls.files)
mapply(file.rename, all.xls.files, new.txt.files)

# Make a .txt copy of each file
if !(dir.exists('Results/GSEA_DSigDB/txt/')){ dir.create('Results/GSEA_DSigDB/txt/', recursive = T)}
local.files = paste0('Results/GSEA_DSigDB/txt/',
                   sapply(new.txt.files, function(x) strsplit(x, '/')[[1]][4]))
mapply(file.copy, new.txt.files, local.files)

# Make a file table
final.tab = data.frame(study = 'A', comparison = '1', group = '1', GSEA_id = '1',
                       pos_neg = 'A')
reports = grep('gsea_report', new.txt.files, value = T)
for (x in reports){
  s = strsplit(strsplit(x, '/')[[1]][3], '_')[[1]]
  final.tab = rbind(final.tab, 
                    data.frame(study = s[1], comparison = gsub('Expt', '', s[3]),
                               group = gsub('Group', '', s[6]),
                               GSEA_id = strsplit(s[7], '\\.')[[1]][4],
                               pos_neg = strsplit(x, '_')[[1]][12]))
}
final.tab = final.tab[-1,]
write.table(final.tab, 'Results/GSEA_DSigDB/txt/Comparison_Key.txt', 
            sep = '\t', row.names = F, quote = F)
