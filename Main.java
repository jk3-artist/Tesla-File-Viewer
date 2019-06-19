package application;
	
import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.TilePane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import tesla.FileUtil;


public class Main extends Application {
	
	static boolean DEBUG=false;
	
	//Style variables
	static String StopBackGroundColor="-fx-background-color: red;-fx-text-fill: yellow;";
	static String PlayBackGroundColor="-fx-background-color: green;-fx-text-fill: yellow;";
	static String FontText="-fx-font: normal bold 18px 'serif';";
	static String ControlStyle=FontText;

	//Screen layout variables
	Dimension screensize;
	int VIDEOSCREENSIZE=500;
	int VIEWGAP=4;
	int SCENEWIDTH=(VIDEOSCREENSIZE+VIEWGAP)*3;
	int CONTROLHEIGHT=100;
	int SCENEHEIGHT=VIDEOSCREENSIZE+(CONTROLHEIGHT*3);
	
	//Video support variables
	int FileIndex=0;
	int FileIndexLimit;
	int VideoEnd=2;
	boolean playing=false;
	ArrayList<File>Left= new ArrayList<File>();
	ArrayList<File>Center= new ArrayList<File>();
	ArrayList<File>Right= new ArrayList<File>();
	
	//These are the directories the user will select
	TreeView<File> theTeslaFileDir=null;
	File baseLineDir=null;
	File savedDir=null;
	File recentDir=null;
	String TESLADIR="TeslaCam";

	//Controls for panel
	Label rateLabel=new Label("Rate 1 X");
	Slider rateSlider= new Slider(1.0,8.0,1.0);
	Label fileLabel=new Label("Select file");
	Label countLabel= new Label("Playing 0/0");
	Button playControl= new Button(">");
	Button preClip= new Button("|<");
	Button nextClip= new Button(">|");
	Button selectVideo= new Button("Select Videos");
	Button deleteVideo=new Button("Delete Videos");
	Label statusLabel= new Label("No video clips selected");

	
	//	 MediaView mediaView=null;
	MediaView viewCenter=new MediaView();
	MediaView viewLeft=new MediaView();
	MediaView viewRight=new MediaView();
    MediaPlayer player =null;
     
    //Rate at which the video is played
    double Rate=1.0;
    //These define the logical states for the controls 
    static final int PLAY_ACTION=1;
    static final int PAUSE_ACTION=2;
    static final int DELETE_ACTION=3;
    static final int SELECT_ACTION=4;
    static final int INITIAL_ACTION=5;
    static final int DELETE_ONLY_ACTION=6;
    
     
	@Override
	public void start(Stage primaryStage) {
		try {
			//Locate TeslaCam directory....
			if(baseLineDir==null){
				getTeslaDir();
			}
			//If user does not select a directory or if it is not TeslaCam exit
			if(baseLineDir==null || baseLineDir.getName().compareToIgnoreCase(TESLADIR)!=0){
				System.out.println("Could not find the TeslaCam directory exiting...");
				System.exit(-1);
			}
			savedDir=new File(baseLineDir,"SavedClips");
			recentDir= new File(baseLineDir,"RecentClips");
			//Create the TreeView control and populate it
	        theTeslaFileDir=FileUtil.getTreeViewOfTeslaCam(baseLineDir);
	        theTeslaFileDir.setEditable(true);
	        theTeslaFileDir.setMinSize(100, CONTROLHEIGHT);
	        theTeslaFileDir.setMaxHeight(CONTROLHEIGHT*2);
	        theTeslaFileDir.setVisible(true);
	        theTeslaFileDir.getRoot().setExpanded(true);
	        
	        //Compute the screen size for the video display
	        screensize=Toolkit.getDefaultToolkit().getScreenSize();
	        if(DEBUG)
	        	System.out.println(screensize.getWidth()+" "+screensize.getHeight());
	        SCENEWIDTH=screensize.width-100;
	        VIDEOSCREENSIZE=SCENEWIDTH/3-VIEWGAP;
	        SCENEHEIGHT=VIDEOSCREENSIZE+CONTROLHEIGHT*2;
		
	        // We are using the border pane and set scene width and height
			BorderPane root = new BorderPane();
			Scene scene = new Scene(root,SCENEWIDTH,SCENEHEIGHT);
			
			//Install the 3 video screens in a tile pane
			TilePane tilePane = new TilePane();   	       
			//Setup Tile Pane 
			tilePane.setOrientation(Orientation.HORIZONTAL); 	       
			tilePane.setTileAlignment(Pos.CENTER_LEFT); 
			tilePane.setPrefColumns(3);
			tilePane.setHgap(VIEWGAP);
			//Set the dimensions for the viewers
	        viewLeft.setFitHeight(VIDEOSCREENSIZE);
	        viewLeft.setFitWidth(VIDEOSCREENSIZE);
	        viewCenter.setFitHeight(VIDEOSCREENSIZE);
	        viewCenter.setFitWidth(VIDEOSCREENSIZE);			
	        viewRight.setFitHeight(VIDEOSCREENSIZE);
	        viewRight.setFitWidth(VIDEOSCREENSIZE);
			//Add the media views to the tile pane
			ObservableList<Node> list = tilePane.getChildren(); 
		    list.add(viewLeft);
		    list.add(viewCenter);
		    list.add(viewRight);
			
			//Create Video control panel / info controls install in a  grid pane
		    GridPane gridPane = new GridPane();   
		    gridPane.setMinSize(SCENEWIDTH/2, CONTROLHEIGHT);
		    //Setting the padding and alignment  
		    gridPane.setPadding(new Insets(10, 10, 10, 10)); 
		    gridPane.setVgap(6); 
		    gridPane.setHgap(6);       
		    gridPane.setAlignment(Pos.BASELINE_LEFT);
		    gridPane.setMaxWidth((VIDEOSCREENSIZE*3));
			//Create viewer controls
		    //Video rate and label
			rateSlider.setShowTickLabels(true);
			rateSlider.setShowTickMarks(true);
	        rateSlider.setMajorTickUnit(1);
		    rateSlider.setSnapToTicks(true);
	        rateSlider.setBlockIncrement(1);
		    rateSlider.setVisible(true);
			rateSlider.setStyle(ControlStyle);
			rateLabel.setStyle(ControlStyle);
			//Player controls
			preClip.setStyle(ControlStyle);
			playControl.setStyle(ControlStyle+PlayBackGroundColor);
			nextClip.setStyle(ControlStyle);
			//Video selection and deletion
			selectVideo.setStyle(ControlStyle);
			deleteVideo.setStyle(ControlStyle);
			//Information and status
			fileLabel.setStyle(ControlStyle);
			countLabel.setStyle(ControlStyle);
			statusLabel.setStyle(ControlStyle);
		       
		    //Arranging all the nodes in the grid 
		    gridPane.add(preClip,     0, 0);       
		    gridPane.add(playControl, 1, 0);       
		    gridPane.add(nextClip,    2, 0);
		    
		    gridPane.add(rateLabel,   0, 1);
		    gridPane.add(rateSlider,  1, 1);
		    
		    gridPane.add(fileLabel,   0, 2);
		    gridPane.add(countLabel,  2, 2);
		    
		    gridPane.add(selectVideo, 0, 3);
		    gridPane.add(deleteVideo, 1, 3);
		    gridPane.add(statusLabel, 0, 4);

			//Set event handling for player controls 
			playControl.setOnMousePressed(new EventHandler<javafx.scene.input.MouseEvent>(){
				@Override
				public void handle(MouseEvent event) {
					updateStatus("");
					setVideo();
				}
				});
			preClip.setOnMousePressed(new EventHandler<javafx.scene.input.MouseEvent>(){
				@Override
				public void handle(MouseEvent event) {
					updateStatus("");
					FileIndex-=2;
					if(FileIndex<0)
						FileIndex=0;
					setClip();
				}
				});
			nextClip.setOnMousePressed(new EventHandler<javafx.scene.input.MouseEvent>(){
				@Override
				public void handle(MouseEvent event) {
					updateStatus("");
					setClip();
				}				
				});
			//Rate controls
			rateSlider.valueProperty().addListener(new ChangeListener<Number>() {
	            @Override
				public void changed(ObservableValue<? extends Number> ov,
	                    Number old_val, Number new_val) {
	                        Rate=(double)new_val;
	                        changeRate();
	                }
			});

			//Video Selection controls load up videos
			selectVideo.setOnMousePressed(new EventHandler<javafx.scene.input.MouseEvent>(){
				@Override
				public void handle(MouseEvent event) {
					ObservableList <TreeItem<File>>items = theTeslaFileDir.getSelectionModel().getSelectedItems();
					File selectedDirs[]= new File[items.size()];
					for(int i=0; i<items.size(); i++){
						selectedDirs[i]=items.get(i).getValue();
					}
					//Clear out existing viewing files and load new ones
					Left.removeAll(Left);
					Center.removeAll(Center);
					Right.removeAll(Right);
					updateStatus("");
					int count=FileUtil.getClipsInDir(selectedDirs, Left, Center, Right);
					if(count%3!=0){
						if(DEBUG){
							System.out.println("Bad count "+count);							
						}
						updateStatus("Bad number of files "+count);
						if(FileUtil.getMaxCount(Left, Center, Right)>0){
							setAction(DELETE_ONLY_ACTION);
							return;
						}
					}
					//Start with the first
					FileIndex=0;
					nextVideo();
					if(FileUtil.getMinCount(Left, Center, Right)>0)
						setAction(SELECT_ACTION);
				}});
			//Video deletion
			deleteVideo.setOnMousePressed(new EventHandler<javafx.scene.input.MouseEvent>(){
				@Override
				public void handle(MouseEvent event) {
					/**
					 * Stop the player if running
					 * Delete the current selection of files from the  players
					 * Grab the selected items from Tree view
					 * These are directories
					 * Ask the user if they want to delete that directory
					 * If so delete the underlining files them the directory then the treeview entry
					 */
					boolean deleted=false;
					pauseVideo();
					//if there are references to the files loaded it can 
					//stop a file deletion. So remove them from the media player as well as the 
					//existing selected files
					disposeMediaPlayer();
					Left.removeAll(Left);
					Center.removeAll(Center);
					Right.removeAll(Right);

					ObservableList <TreeItem<File>>items = theTeslaFileDir.getSelectionModel().getSelectedItems();
					@SuppressWarnings("unchecked")
					TreeItem<File> selectedTreeItems[]=  new TreeItem[items.size()];
					for(int i=0; i<items.size(); i++)
						selectedTreeItems[i]=items.get(i);
					for(int i=0; i<selectedTreeItems.length; i++){
						File thisdir=selectedTreeItems[i].getValue();
						String message="Delete "+thisdir.toString()+" ?";
						int retval = JOptionPane.showConfirmDialog(null,
					             message,"Delete directory", JOptionPane.YES_NO_OPTION);
						if(retval==JOptionPane.OK_OPTION){
							File files2Delete[]=selectedTreeItems[i].getValue().listFiles();
							for(int j=0; j<files2Delete.length; j++){
								String statusMessage=new String("File:"+files2Delete[j].getName()+" ");
								deleted=false;
								try{
									deleted=files2Delete[j].delete();
									if(deleted)
										statusMessage=statusMessage+"removed ";
									else
										statusMessage=statusMessage+"failed! ";
								}
								catch(Exception e){
									statusMessage=statusMessage+"failed exception "+e.toString();
								}
								updateStatus(statusMessage);
								if(!deleted)
									JOptionPane.showMessageDialog(null, statusMessage);									
							}
							String statusMessage=new String("Directory:"+thisdir.getName()+" ");
							deleted=false;
							try{
								deleted=thisdir.delete();
								if(deleted)
									statusMessage=statusMessage+"removed ";
								else
									statusMessage=statusMessage+"failed! ";
							}
							catch(Exception e){
								statusMessage=statusMessage+"failed exception "+e.toString();
							}
							updateStatus(statusMessage);
							if(!deleted)
								JOptionPane.showMessageDialog(null, statusMessage);									
							//If we failed to delete the directory DONOT remove it from treeview
							if(deleted)
								deleted=selectedTreeItems[i].getParent().getChildren().remove(selectedTreeItems[i]);
							if(DEBUG){
								if(deleted)
									System.out.println(thisdir.toString()+" removed from tree");
								else
									System.err.println(thisdir.toString()+" failed to be removed from tree");
							}
							theTeslaFileDir.refresh();
						}
					}
					if(deleted)
						setAction(DELETE_ACTION);
					theTeslaFileDir.getSelectionModel().clearSelection();
				}});
		      
			//Add the file tree view and controls
			//into a tile pane
			TilePane controlPane = new TilePane();   	       
			//Setup Tile Pane 
			controlPane.setOrientation(Orientation.HORIZONTAL); 	       
			controlPane.setTileAlignment(Pos.CENTER_LEFT); 
			controlPane.setPrefColumns(2);
			controlPane.setHgap(VIEWGAP);
			controlPane.setPrefTileWidth((VIDEOSCREENSIZE*3)/2);
			list=controlPane.getChildren();
			list.add(theTeslaFileDir);
			list.add(gridPane);
			//Add the media and control paned to the border pane
			root.setBottom(controlPane);
			root.setCenter(tilePane);
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			scene.setFill(Color.LIME);
			//set the scene and we are ready for the video
			primaryStage.setScene(scene);
			primaryStage.setTitle("Tesla Viewer");
			nextVideo();
			//Set the state of the controls
			setAction(INITIAL_ACTION);
			
			
			primaryStage.show();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	//Start and or Move to the next video
	private void nextVideo(){
		//Video's need to be  synchronous so there are two
		// techniques first when one is done move all videos to the next
		// The other is to count when two players done then move on this
		//is the technique I choose
		if(VideoEnd!=2 && playing){
			VideoEnd++;
			if(DEBUG){
				double lsec=viewLeft.getMediaPlayer().getCurrentTime().toSeconds();
				double csec=viewCenter.getMediaPlayer().getCurrentTime().toSeconds();
				double rsec=viewRight.getMediaPlayer().getCurrentTime().toSeconds();
				System.out.println("left "+lsec+" center "+csec+" right "+rsec);
			}
			return;
		}
		//Ensure that there is more to play
		FileIndexLimit=Math.min(Center.size(),Math.min(Left.size(), Right.size()));
		if(FileIndex+1>FileIndexLimit){
			if(DEBUG)
				System.out.println("Completed the show");
			updateStatus("End of selected clips");
			if(FileIndexLimit>0)
				pauseVideo();
			return;
		}
		//Create the date time stamp from the files
		updateFileLabels(FileIndex,FileIndexLimit);
	
		//Create the players clear error messages
		updateStatus("");
		MediaPlayer leftPlayer=new MediaPlayer(new Media(Left.get(FileIndex).toURI().toString()));
		MediaPlayer centerPlayer=new MediaPlayer(new Media(Center.get(FileIndex).toURI().toString()));
		MediaPlayer rightPlayer=new MediaPlayer(new Media(Right.get(FileIndex).toURI().toString()));
		leftPlayer.setOnError(new Runnable(){
			@Override
			public void run() {
				String message="Video error left ";
				updateStatus(message);
				nextVideo();				
			}});
		rightPlayer.setOnError(new Runnable(){
			@Override
			public void run() {
				String message="Video error right ";
				updateStatus(message);
				nextVideo();				
			}});
		centerPlayer.setOnError(new Runnable(){
			@Override
			public void run() {
				String message="Video error center ";
				updateStatus(message);
				nextVideo();				
			}});
		disposeMediaPlayer();
		//Install the new videos and set the current rate
		viewLeft.setMediaPlayer(leftPlayer);
		viewCenter.setMediaPlayer(centerPlayer);
		viewRight.setMediaPlayer(rightPlayer);
		changeRate();
		if(playing){
			leftPlayer.play();
			centerPlayer.play();
			rightPlayer.play();
//			FileIndex++;
		}
		FileIndex++;
		VideoEnd=0;
		if(DEBUG){
			System.out.println("Playing video "+FileIndex+" of "+FileIndexLimit);
		}

		//Setup for continues play
		leftPlayer.setOnEndOfMedia(new Runnable(){
			@Override
			public void run() {
				nextVideo();
				
			}});
		centerPlayer.setOnEndOfMedia(new Runnable(){
			@Override
			public void run() {
				nextVideo();
				
			}});
		
		rightPlayer.setOnEndOfMedia(new Runnable(){
			@Override
			public void run() {
				nextVideo();
				
			}});
	}
	/**
	 * When the user hits the rate slider we reset teh rates on all
	 * media players
	 */
	protected void changeRate(){		
		String rs=new String("Rate "+String.format("%.2f", Rate)+" X");
		rateLabel.setText(rs);
		MediaPlayer l=viewLeft.getMediaPlayer();
		MediaPlayer c=viewCenter.getMediaPlayer();
		MediaPlayer r=viewRight.getMediaPlayer();
		if(l!=null)
			l.setRate(Rate);
		if(c!=null)
			c.setRate(Rate);
		if(r!=null)
			r.setRate(Rate);
	}
	//Utility to stop playing when
	//we are playing with the tree view
	protected void pauseVideo(){
		playControl.setText("||");
		setVideo();
	}
	//Utility routine to load the next video
	//to be used when clip control is 
	//demands a backup or move forward 
	protected void setClip(){
		viewLeft.getMediaPlayer().stop();
		viewCenter.getMediaPlayer().stop();
		viewRight.getMediaPlayer().stop();
		VideoEnd=2;
		if(playing){
			setVideo();
			nextVideo();
			setVideo();
		}
		else{
			nextVideo();
		}
	}
	//Utility routine that clears out the media players
	protected void disposeMediaPlayer(){
		//Destroy the previous players
		if(viewLeft.getMediaPlayer()!=null){
			viewLeft.getMediaPlayer().dispose();
			viewLeft.setMediaPlayer(null);
		}
		if(viewCenter.getMediaPlayer()!=null){
			viewCenter.getMediaPlayer().dispose();
			viewCenter.setMediaPlayer(null);
		}
		if(viewRight.getMediaPlayer()!=null){
			viewRight.getMediaPlayer().dispose();
			viewRight.setMediaPlayer(null);
		}
		
	}
	//Utility function to stop and start the media 
	//players and keep the controls and variables
	//consistent 
	protected void setVideo() {
		String value=playControl.getText();
		if(value.compareTo(">")==0){
			//Logical state paused move to playing
			//If there is media selected
			if(viewLeft.getMediaPlayer()!=null &&
					viewCenter.getMediaPlayer()!=null&&
					viewRight.getMediaPlayer()!=null){
				viewLeft.getMediaPlayer().play();
				viewCenter.getMediaPlayer().play();
				viewRight.getMediaPlayer().play();
				playControl.setText("||");
				playControl.setStyle(ControlStyle+StopBackGroundColor);
				playing=true;
				setAction(PLAY_ACTION);
			}
		}
		else{
			//Logical state is playing we pause if 
			//There is clips loaded
			if(viewLeft.getMediaPlayer()!=null &&
					viewCenter.getMediaPlayer()!=null&&
					viewRight.getMediaPlayer()!=null){
				viewLeft.getMediaPlayer().pause();
				viewCenter.getMediaPlayer().pause();
				viewRight.getMediaPlayer().pause();
				playControl.setText(">");
				playControl.setStyle(ControlStyle+PlayBackGroundColor);
				playing=false;
				setAction(PAUSE_ACTION);
			}
		}
	}
	//This define the logical state of the controls
	//When an action has SUCCESSFULLY occurred  
	protected void setAction(int value){
		switch(value){
			case(INITIAL_ACTION):
			case(DELETE_ACTION):
				playControl.setDisable(true);
				preClip.setDisable(true);
				nextClip.setDisable(true);
				selectVideo.setDisable(false);
				deleteVideo.setDisable(true);
			break;
			case(DELETE_ONLY_ACTION):
				playControl.setDisable(true);
				preClip.setDisable(true);
				nextClip.setDisable(true);
				selectVideo.setDisable(false);
				deleteVideo.setDisable(false);
				break;
			case(SELECT_ACTION):
			case(PAUSE_ACTION):
				playControl.setDisable(false);
				preClip.setDisable(false);
				nextClip.setDisable(false);
				selectVideo.setDisable(false);
				deleteVideo.setDisable(false);
			break;
			case(PLAY_ACTION):
				playControl.setDisable(false);
				preClip.setDisable(false);
				nextClip.setDisable(false);
				selectVideo.setDisable(true);
				deleteVideo.setDisable(true);
			break;
		
		}
	}
	protected void updateFileLabels(int FileIndex, int FileIndexLimit){
		//Create the date time stamp from the files
		int[]xxx=FileUtil.getTeslaDateTimeFromFileName(Left.get(FileIndex));
		countLabel.setText(" "+(FileIndex+1)+" of "+FileIndexLimit);
		LocalDateTime currentFile=LocalDateTime.of(xxx[0],xxx[1],xxx[2],xxx[3],xxx[4]);
		fileLabel.setText(currentFile.toString());
/**	
		String message="";
		for(int i=0; i<FileIndexLimit; i++){
			if(i<FileIndex)
				message+="+";
			else
				message+=">";
		}
		updateStatus(message);
**/		
	}
	protected void updateStatus(String message){
		statusLabel.setText(message);
	}
	//Pop a file dialog and get the TeslaCam directory
	private void getTeslaDir(){		
		JFileChooser chooser = new JFileChooser();
		if(DEBUG)
			chooser.setCurrentDirectory(new File("c:\\"));
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		int returnVal = chooser.showOpenDialog(null);
		if(returnVal == JFileChooser.APPROVE_OPTION) {
			baseLineDir=chooser.getSelectedFile();
		}
	}

	
	/**
	 * 
	 */
	@Override
	public  void stop(){
		System.out.print("Elvis has left the building");
		Platform.exit();
	}
	public static void main(String[] args) {
		launch(args);
	}
}
