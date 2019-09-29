# VARpedia | Java / Bash

## This project was tested on the ECSE Lab Linux Image

## Project Description
JavaFX GUI application which lets the user search for a term on Wikipedia. 
They will be given text from the search, the user can select chunks of text 
and merge them into one audio file. Alongside this, the user can select images 
to be made into a video with the superimposed word. The video can then be played 
back to the user.

## Setting up and running the program
1. Ensure the jar file is in the same directory as the 'flickr-api-keys.txt'
2. Open the command prompt or terminal (macOS)
3. Type "java -jar VARpedia.jar"

## Using the program
On start-up, there will be the option between 'Viewing creations' and 'Creating creations'
# Viewing Creations:
  You will have the options to:
    Play            - Select a creation to play
    Delete          - Select a creation to delete
    Delete All      - Deletes all creations
    Return to Menu  - Returns to the main menu
    
    If you play a creation, you will be able to:
      Mute            - Removes all volume from the creation
      Change Volume   - Slider to alter the volume
      Play/Pause      - Allows us to play and pause the video
      Return to list  - Returns to the creation list
      Return to menu  - Returns to the main menu
      
# Creating Creations:
  1. Search for a term
  2. From the given text, highlight desired text.
  3. Choose a synthesiser (Festival or ESpeak)
    3a. Option: preview the selected text (program will speak the selected text)
    3b. Option: save the selected text (text saved in a text file)
  4. Select desired files to transfer to final creation to be merged 
  5. Select number of images to be included in the creation. Will be images of the search term.
  6. Name the creation, then 'Create new creation'
  
# Authors
Charles Paterson
Steven Ho
