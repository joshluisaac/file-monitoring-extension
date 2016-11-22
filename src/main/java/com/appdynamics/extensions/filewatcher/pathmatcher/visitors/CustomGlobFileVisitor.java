package com.appdynamics.extensions.filewatcher.pathmatcher.visitors;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Map;

import org.apache.log4j.Logger;

import com.appdynamics.extensions.filewatcher.FileMetric;
import com.appdynamics.extensions.filewatcher.config.FileToProcess;
import com.appdynamics.extensions.filewatcher.pathmatcher.GlobPathMatcher;
import com.appdynamics.extensions.filewatcher.pathmatcher.helpers.DisplayNameHelper;

public class CustomGlobFileVisitor extends SimpleFileVisitor<Path>{

	protected static final Logger logger = Logger.getLogger(CustomGlobFileVisitor.class.getName());

	private FileToProcess file;

	private GlobPathMatcher matcher;

	private Map<String,FileMetric> fileMetricsMap;



	private String baseDir;

	public CustomGlobFileVisitor(FileToProcess file,GlobPathMatcher matcher,Map<String,FileMetric> fileMetricsMap,String baseDir){

		this.baseDir=baseDir;
		this.file=file;
		this.matcher=matcher;
		this.fileMetricsMap=fileMetricsMap;
	}

	@Override
	public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {

		if(file.getIgnoreHiddenFiles()  && path.toFile().isHidden()){
			logger.debug("Skipping file as is_ignore_hidden set true " + path.getFileName());
			return FileVisitResult.CONTINUE;
		}

		if(!file.getIsDirectoryDetailsRequired()){
			logger.debug("Skipping files as is_direcory_details_required set as false " + path.getFileName());
			return FileVisitResult.CONTINUE;
		}

		if (matcher.getMatcher().matches(path)) {
			logger.debug("Found match for entered path " + path);
			FileMetric metric = new FileMetric();
			if(attrs!=null){
				if(fileMetricsMap.containsKey(DisplayNameHelper.getFormattedDisplayName(file.getDisplayName(), path, baseDir))){
					metric = fileMetricsMap.get(DisplayNameHelper.getFormattedDisplayName(file.getDisplayName(), path, baseDir));
					if(!Long.valueOf(fileMetricsMap.get(DisplayNameHelper.getFormattedDisplayName(file.getDisplayName(), path, baseDir)).getTimeStamp())
							.equals(attrs.lastModifiedTime().toMillis())){
						metric.setChanged(true);
						metric.setTimeStamp(String.valueOf(attrs.lastModifiedTime().toMillis()));

					}
				}else{
					metric.setChanged(false);
					metric.setTimeStamp(String.valueOf(attrs.lastModifiedTime().toMillis()));
				}

				metric.setFileSize(String.valueOf(attrs.size()));
			}
			else{
				logger.debug("Couldnt find basic file attrs " + path.toString());
			}
			metric.setNumberOfFiles(-1);
			metric.setOldestFileAge(-1);
			fileMetricsMap.put(DisplayNameHelper.getFormattedDisplayName(file.getDisplayName(), path, baseDir),metric);
		}
		return FileVisitResult.CONTINUE;
	}

	@Override
	public FileVisitResult visitFileFailed(Path file, IOException exc)
			throws IOException
	{
		return FileVisitResult.CONTINUE;
	}

	@Override
	public FileVisitResult preVisitDirectory(Path path, BasicFileAttributes attrs)
			throws IOException
	{
		if(file.getIgnoreHiddenFiles() && path.toFile().isHidden()){
			logger.debug("Skipping directory as is_ignore_hidden set true " + path.getFileName());
			return FileVisitResult.CONTINUE;
		}
		if (matcher.getMatcher().matches(path)) {
			logger.debug("Found match for entered path " + path);

			FileMetric metric = new FileMetric();
			if(attrs!=null){
				if(fileMetricsMap.containsKey(DisplayNameHelper.getFormattedDisplayName(file.getDisplayName(), path, baseDir))){
					metric = fileMetricsMap.get(DisplayNameHelper.getFormattedDisplayName(file.getDisplayName(), path, baseDir));
					if(!Long.valueOf(fileMetricsMap.get(DisplayNameHelper.getFormattedDisplayName(file.getDisplayName(), path, baseDir)).getTimeStamp())
							.equals(attrs.lastModifiedTime().toMillis())){
						metric.setChanged(true);
						metric.setTimeStamp(String.valueOf(attrs.lastModifiedTime().toMillis()));

					}
				}else{
					metric.setChanged(false);
					metric.setTimeStamp(String.valueOf(attrs.lastModifiedTime().toMillis()));
				}

				metric.setFileSize(String.valueOf(attrs.size()));
			}
			else{
				logger.debug("Couldnt find basic file attrs " + path.toString());
			}
			int count  = 0;
			long oldestFile = 0l;
			File[] filesInDir= path.toFile().listFiles();
			if(filesInDir!=null && filesInDir.length>0){
				oldestFile = filesInDir[0].lastModified(); 

				for(File f : filesInDir){
					if(file.getIgnoreHiddenFiles()){
						if(!f.isHidden()){
							count++;
							if(f.lastModified() < oldestFile){
								oldestFile = f.lastModified();
							}
						}
					}
					else{
						count++;
						if(f.lastModified() < oldestFile){
							oldestFile = f.lastModified();
						}
					}
				}
			}
			metric.setNumberOfFiles(count);
	        long currentTimeInMillis = System.currentTimeMillis();
	        long oldestFileAge = -1;
	        if (oldestFile < currentTimeInMillis) {
	            oldestFileAge = (currentTimeInMillis - oldestFile) / 1000;
	        }
			
			metric.setOldestFileAge(oldestFileAge);
			fileMetricsMap.put(DisplayNameHelper.getFormattedDisplayName(file.getDisplayName(), path, baseDir),metric);
		}
		return FileVisitResult.CONTINUE;
	}

}
