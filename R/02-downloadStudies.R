library(GEOquery)

# Read in the annotated studies
# Column names in this table are keep, drug, gse, gsm, sample_group
all.studies.tab = read.table('Data/GEO/human_GPL570_n6_toAnnotate_completed.txt',
                             sep = '\t', header = T, stringsAsFactors = F)
all.studies.tab = all.studies.tab[all.studies.tab$keep == 'TRUE',]
all.studies = unique(all.studies.tab$gse)

# Tally up how many comparisons we have total annotated
comparisons = 0
for (study in all.studies){
  tab = all.studies.tab[all.studies.tab$gse == study,]
  tab = tab[tab$comparison != 'X',]
  for (comparison in unique(tab$comparison))
    tab2 = tab[tab$comparison == comparison,]
    groups = unique(tab2$sample_group)
    groups = groups[groups != 'C']
    comparisons = comparisons + length(groups)
}

# For each study, download the samples if they haven't been downloaded yet
if (!dir.exists('Data/GEO/CELs')){ dir.create('Data/GEO/CELs', recursive = T)}
for (study in all.studies){
  
  cat('Opening study', study, '\n')
  
  # Subset just the samples from desired study
  tab = all.studies.tab[all.studies.tab$gse == study,]
  tab = tab[tab$sample_group != 'X',]
  
  # Download samples
  samples = tab$gsm
  study.dir = paste0('Data/GEO/CELs/', study)
  
  # If the folder for this study doesn't exist yet, create it and download all files
  if (!dir.exists(study.dir)){
    cat('\tDownloading and unzipping', length(samples), 'samples from study', 
        study, '\n')
    dir.create(study.dir)
    for (sample in samples){
      cel.name = paste0(study.dir, '/', sample, '.CEL.gz')
      getGEOSuppFiles(sample, makeDirectory = F, baseDir = study.dir) 
      
      # Rename file if needed
      if (!file.exists(cel.name)){
        to.rename = list.files(study.dir, pattern = '.CEL.gz', full.names = T, ignore.case = T)
        file.rename(to.rename, cel.name)
      }
      # Unzip
      gunzip(cel.name, overwrite = F, remove = T)
    }
    cat('\tStudy', study, 'download complete.\n')
    
  } else {
    cat('\tDirectory already exists for', study, '\n')
    # If the folder does exist, check the cel files inside it
    downloaded.cels = sort(gsub('\\.CEL', '', list.files(study.dir)))
    
    # If all samples are accounted for, move on
    if (identical(sort(downloaded.cels), sort(samples))){
      cat('\tAll CEL files already downloaded\n')
    } else{
      # Otherwise, download the missing files
      cat('\tDownloading the missing CEL files for', study, '\n')
      to.download = samples[!samples %in% downloaded.cels]
      
      for (new.sample in to.download){
        cel.name = paste0(study.dir, '/', new.sample, '.CEL.gz')
        getGEOSuppFiles(new.sample, makeDirectory = F, baseDir = study.dir)  
        
        # Rename file if needed
        if (!file.exists(cel.name)){
          to.rename = list.files(study.dir, pattern = '.CEL.gz', full.names = T, ignore.case = T)
          file.rename(to.rename, cel.name)
        }
        # Unzip
        gunzip(cel.name, overwrite = F, remove = T)
      }
      cat('\tFinished downloading new files for', study, '\n')
    }
  }
}