#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <windows.h>
#include <mmsystem.h>
#pragma comment(lib, "winmm.lib")

const char *MCI_ALIAS = "my_mp3_player"; 

enum PlaybackState { IDLE, PLAYING, PAUSED };
enum PlaybackState playerState = IDLE;

struct Node {
    char songName[100];
    struct Node *next;
    struct Node *prev;
};

struct Node *head = NULL;
struct Node *current = NULL;

struct Node* createNode(char *song) {
    struct Node *newNode = (struct Node*)malloc(sizeof(struct Node));
    strcpy(newNode->songName, song);
    newNode->next = newNode->prev = newNode; 
    return newNode;
}

void insertSong(char *song) {
    struct Node *newNode = createNode(song);
    if (head == NULL) {
        head = newNode;
        current = head;
    } else {
        struct Node *last = head->prev;
        last->next = newNode;
        newNode->prev = last;
        newNode->next = head;
        head->prev = newNode;
    }
}

// --- GEMINI: MCI Playback Functions (FIXED) ---

void mci_stop_and_close() {
    if (playerState != IDLE) {
        char command[64];
        
        // GEMINI: FIX: Use sprintf to correctly construct the command string
        sprintf(command, "stop %s", MCI_ALIAS);
        mciSendStringA(command, NULL, 0, NULL);
        
        sprintf(command, "close %s", MCI_ALIAS);
        mciSendStringA(command, NULL, 0, NULL);
        
        playerState = IDLE;
    }
}

int mci_open_song(const char *songName) {
    char command[256];
    
    mci_stop_and_close(); 

    sprintf(command, "open \"%s\" type mpegvideo alias %s", songName, MCI_ALIAS);
    if (mciSendStringA(command, NULL, 0, NULL) != 0) {
        printf("Error: Could not open song file: %s. Check if the file exists.\n", songName);
        return 0;
    }
    return 1;
}

void playSong() {
    if (current == NULL) {
        printf("No songs in the playlist.\n");
        return;
    }
    
    char command[64];

    if (playerState == IDLE) {
        if (mci_open_song(current->songName)) {
            // GEMINI: FIX: Use sprintf to correctly construct the command string
            sprintf(command, "play %s", MCI_ALIAS);
            mciSendStringA(command, NULL, 0, NULL);
            printf("Now Playing: %s\n", current->songName);
            playerState = PLAYING;
        }
    } else if (playerState == PAUSED) {

        // GEMINI: FIX: Use sprintf to correctly construct the command string
        sprintf(command, "play %s", MCI_ALIAS);
        mciSendStringA(command, NULL, 0, NULL);
        printf("Resuming: %s\n", current->songName);
        playerState = PLAYING;
    } else if (playerState == PLAYING) {
        printf("Already playing: %s\n", current->songName);
    }
}

void pauseSong() {
    if (playerState == PLAYING) {
        char command[64];
        // GEMINI: FIX: Use sprintf to correctly construct the command string
        sprintf(command, "pause %s", MCI_ALIAS);
        mciSendStringA(command, NULL, 0, NULL);
        
        printf("Pausing: %s. You can resume it with option 1.\n", current->songName);
        playerState = PAUSED;
    } else if (playerState == PAUSED) {
        printf("Song is already paused.\n");
    } else {
        printf("Nothing is currently playing to pause.\n");
    }
}

// GEMINI: Stops the current song (equivalent to the old stop, but clean)
void stopSong() {
    if (playerState != IDLE) {
        mci_stop_and_close();
        printf("Song stopped and device closed.\n");
    } else {
        printf("Nothing is currently playing to stop.\n");
    }
}

void nextSong() {
    if (current == NULL) {
        printf("No songs available.\n");
        return;
    }
    mci_stop_and_close();
    current = current->next;
    playSong();
}

void prevSong() {
    if (current == NULL) {
        printf("No songs available.\n");
        return;
    }
    mci_stop_and_close();
    current = current->prev;
    playSong();
}

int main() {
    int choice;

    insertSong("Perfect.mp3");
    insertSong("Rewrite_The_Stars.mp3");
    insertSong("I_Wanna_Be_Yours.mp3");

    while (1) {
        printf("\n=== Circular Linked List Music Player (MCI) ===\n");
        printf("1. Play/Resume Current Song\n");
        printf("2. Pause Song\n");
        printf("3. Stop and Reset Song\n");
        printf("4. Next Song\n");
        printf("5. Previous Song\n");
        printf("6. Exit\n");
        printf("Enter your choice: ");
        
        if (scanf("%d", &choice) != 1) {
            printf("Invalid input! Please enter a number.\n");
            while (getchar() != '\n'); // GEMINI: Clear buffer
            continue;
        }
        getchar(); // GEMINI: consume the newline
        system("cls");
        printf("\n");
        switch (choice) {
            case 1:
                playSong();
                break;
            case 2:
                pauseSong();
                break;
            case 3:
                stopSong();
                break;
            case 4:
                nextSong();
                break;
            case 5:
                prevSong();
                break;
            case 6:
                printf("Exiting player.\n");
                mci_stop_and_close();
                return 0;
            default:
                printf("Invalid choice! Try again.\n");
        }
    }

    return 0;
}

// GEMINI: Compile: "C:\MinGW\bin\gcc.exe" "c:\Users\Atharva\Documents\CRCE\III Sem\DS\mini_project\atharva\music.c" -o music.exe -lwinmm  