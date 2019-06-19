package tesla;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;

import javafx.scene.control.SelectionMode;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

public class FileUtil {
	/** This class deals with organizing tesla's TeslaCam files
	 * Tesla creates files into the two main directories
	 * RecentClips 
	 * SavedClips
	 * 
	 * The RecentClips have mp4 video files whose names are
	 * YYYY-MM-DD_HH-MM-front.mp4
	 * YYYY-MM-DD_HH-MM-left_repeater.mp4
	 * YYYY-MM-DD-HH-MM-right_repeater.mp4
	 * 
	 * Where the YYYY-MM-DD_HH-MM whose names are the same compose a 
	 * front-left-right movie of the car in park or moving
	 * 
	 * The SavedClips directory is composed of directories where
	 * collections of front-left-right movies are saved.  Their 
	 * directory names have a similar format
	 * SavedClips
	 * 		YYYY-MM-DD_HH-MM-SS
	 * 			YYYY-MM-DD_HH-MM-front.mp4
	 * 			YYYY-MM-DD_HH-MM-left-repeater.mp4
	 * 			YYYY-MM-DD_HH-MM-right-repeater.mp4
	 * 
	 * 
	 */
	
	private static boolean DEBUG=false;
	/**
	 * This parses either a Tesla camera directory or mp4 file and get the date time
	 * @param current
	 * @return
	 */
	public static int[] getTeslaDateTimeFromFileName(File current){
		int value[]={0,0,0,0,0,0};
		String[] parts = current.getName().split("[_-]");
		if(parts.length>=6){
			try{
				value[0]=Integer.parseInt(parts[0],10);
				value[1]=Integer.parseInt(parts[1],10);
				value[2]=Integer.parseInt(parts[2],10);
				value[3]=Integer.parseInt(parts[3],10);
				value[4]=Integer.parseInt(parts[4],10);
				if(parts[5].compareTo("front.mp4")==0||
						parts[5].compareTo("left")==0||
						parts[5].compareTo("right")==0){
					//movie file no seconds added
				}
				else{
					value[5]=Integer.parseInt(parts[5],10);
				}
			}
			catch(Exception e){
				if(DEBUG){
					System.err.println("\n"+e);
					System.err.println("Decoding problem with:"+current.getName()+" ");
				}
				
				return(null);
			}
			
		}
		return(value);
		
	}
	/**
	 * This routine looks into a directory and gets the
	 * start and end dates of the Tesla files
	 * @param dir target directory
	 * @return localDateTime start then end 
	 */
	public static LocalDateTime[] getDateRange(File dir){
		LocalDateTime Start = null;
		LocalDateTime End=null;
		LocalDateTime []value=new LocalDateTime[2];
		File [] myfiles=dir.listFiles();
		for(int i=0; i<myfiles.length; i++){
			int []values=FileUtil.getTeslaDateTimeFromFileName(myfiles[i]);
			if(values==null)
				continue;
			LocalDateTime current=LocalDateTime.of(values[0],values[1],values[2],values[3],values[4],values[5]);
			
			if(Start!=null){
				if(current.isBefore(Start))
					Start=current;
				if(current.isAfter(End))
						End=current;
			}
			else{
				Start=current;
				End=current;
			}
		}
		value[0]=Start;
		value[1]=End;
		return(value);
		
	}
	
	public static int getClipsInDir(File clipDir[], 
			ArrayList<File>Left,ArrayList<File>Front,ArrayList<File>Right){
		int count=0;
		for(int i=0; i<clipDir.length; i++){
			File [] theFiles=clipDir[i].listFiles();
			for(int j=0; j<theFiles.length; j++){
				//Check to see that it is a multiple of three
				if(theFiles.length%3!=0){
					if(DEBUG)
						System.err.println("Warning Will Roberson imcomplete set "+theFiles.length);
					
				}
				String filename=theFiles[j].getName();
				File targetFile=theFiles[j];
				if(filename.contains("left")){
					Left.add(targetFile);
					count++;
				}
				else if(filename.contains("front")){
					Front.add(targetFile);
					count++;
				}
				else if(filename.contains("right")){
					Right.add(targetFile);
					count++;
				}
				else{
					if(DEBUG)
						System.err.println("Warning Will Robertson");
				}
				
			}
			
		}
		return(count);
	}
	
	/**
	 * This Utility will create a left center right array containing all files between
	 * a stated date time range
	 * @param savedClipDir normally either SaveClips or RecentClips
	 * @param Start LocalDateTime value that clips are AFTER
	 * @param End LocalDateTime value that clips are BEFORE
	 * @param Left ArrayList that contains all the left_repeater.mp4 files
	 * @param Front ArrayList that contains all the front.mp4 files
	 * @param Right ArrayList that contains all the right_repeater.mp4 files
	 * @return number of files discovered should be multiple of three
	 */
	public static int getClipsByDateTime(File savedClipDir, LocalDateTime Start, LocalDateTime End, 
			ArrayList<File>Left,ArrayList<File>Front,ArrayList<File>Right){
		//Get the date-hour for all the clips
		File [] myfiles=savedClipDir.listFiles();
		int count=0;
		for(int i=0; i<myfiles.length; i++){
			int []values=FileUtil.getTeslaDateTimeFromFileName(myfiles[i]);
			if(values==null)
				continue;
			LocalDateTime current=LocalDateTime.of(values[0],values[1],values[2],values[3],values[4],values[5]);
			if(myfiles[i].isDirectory()){
				if(current.isAfter(Start) && current.isBefore(End))
				count+=getClipsByDateTime(myfiles[i],Start,End,Left,Front,Right);
			}
			
			if(current.isAfter(Start) && current.isBefore(End) && myfiles[i].isFile()){
				String filename=myfiles[i].getName();
				File targetFile=myfiles[i];
				if(filename.contains("left")){
					Left.add(targetFile);
					count++;
				}
				else if(filename.contains("front")){
					Front.add(targetFile);
					count++;
				}
				else if(filename.contains("right")){
					Right.add(targetFile);
					count++;
				}
				else{
					if(DEBUG)
						System.err.println("Warning Will Robertson");
				}
			}
		}
		return(count);
	}
	static public int getMaxCount(ArrayList<File>Left,ArrayList<File>Front,ArrayList<File>Right){
		return(Math.max(Math.max(Left.size(), Front.size()),Right.size()));
	}

	static public int getMinCount(ArrayList<File>Left,ArrayList<File>Front,ArrayList<File>Right){
		return(Math.min(Math.min(Left.size(), Front.size()),Right.size()));
	}
	
	static public TreeView <File> getTreeViewOfTeslaCam(File baseline){
		TreeView <File>theTree= new TreeView<File>();
		theTree.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		File recentClipsDir=new File(baseline,"RecentClips");
		File savedClipsDir=new File(baseline,"SavedClips");
		TreeItem <File>recent= null;
		TreeItem <File>saved= null;
		
		if(recentClipsDir.exists()&recentClipsDir.isDirectory())
			recent= new TreeItem<File>(recentClipsDir);
		if(savedClipsDir.exists()&savedClipsDir.isDirectory())
		saved= new TreeItem<File>(savedClipsDir);

		TreeItem <File>bline=new TreeItem<File>(baseline);
		theTree.setRoot(bline);
		if(recent!=null)
			theTree.getRoot().getChildren().add(recent);
		if(saved!=null)
			theTree.getRoot().getChildren().add(saved);
		
//		theTree.setRoot(saved);	
		theTree.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		theTree.setShowRoot(true);
		File target=new File(baseline,"SavedClips");
		File []savedDirs=target.listFiles();
		for(int i=0; i<savedDirs.length; i++){
			if(savedDirs[i].isDirectory()){
				TreeItem <File> dir=new TreeItem<File>(savedDirs[i]);
				saved.getChildren().add(dir);
			}
		}
		return(theTree);
		
	}

}
