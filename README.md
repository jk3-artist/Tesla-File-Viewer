# Tesla-File-Viewer
This java based code allows users to view and delete files created by Tesla auto in the TeslaCam directories.
The executable jar file requires java version 1.8 to operate it.  
To use download the TeslaViewer.jar file to your platfrom. Ensure that java version 1.8 is installed.  Run the executable (TeslaViewer) it then pops a file dialog to allow thw operator navigate to a directory on the platform where the TeslaCam directory is present.  Once found  it opens TeslaCam directory and collects the directories created by the Tesla and generates a tree view of the clips.
In the directory tree there are two major directories RecentClips and SavedClips.  The RecentClips tree contains a single collection of Tesla clips.  The SavedClips tree has sub trees where the saved clips are organized by date.   Tesla places right center left camera video files in a directory, where the video length is about a min.  A directory can have about 11 video segments (33 files of a min or so in length).  The operator selects the RecentClips tree or one or more of the SavedClips sub trees and can view or delete them.  
The operator can view the files in real time or increase the play rate to 8 times.  The operator can skip foward and backward to clips in the select tree, the controls are VCR like.
The operator can also delete sets of clips, the deletion is the contents of the entire tree (directory).  Either all of the clips in  RecentClips or one or more SavedClips sub directory(ies)

Known problems  playback: At rates over 3x the right center left cameras get out of sync
                deletion: Sometimes files fail to be deleted in directories
