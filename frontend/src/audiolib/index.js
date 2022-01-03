import log from '../utils'

export default class AudioManager {
  constructor() {
    this.tracks = new Map();
    this.volumes = new Map();
    this.oldTimes = new Map();
    this.baseURL = "https://home.marcdif.com/music/"
    this.audio = new Audio();
  }

  startSong(songPath, startTime, songDuration) {
    let trackUrl = this.baseURL + songPath;
    if (this.audio != null) {
      this.audio.remove();
    }
    this.audio = new Audio(trackUrl);
    this.audio.id = songPath;
    this.audio.volume = 1;

    let currentTime = Date.now();
    let startingLate = currentTime > startTime;
    log("songPath: " + songPath + ", trackUrl: " + trackUrl + ", startTime: " + startTime + ", songDuration: " + songDuration + ", currentTime: " + currentTime + ", startingLate: " + startingLate)
    setTimeout(() => {
      if (startingLate) {
        let howLate = Date.now() - startTime;
        log("Starting " + howLate + "ms late...")
        if (howLate > songDuration) {
          // song is already over
          log('[ERROR] Song was over before we started playing.');
          return;
        }
        log('[DEBUG] Starting audio...');
        try {
          this.audio.play();
        } catch (e) {
          log("Failed to start song! Maybe it doesn't exist?")
          this.audio = null
          this.stopSong()
          return
        }
        this.audio.currentTime = (howLate / 1000);
        log('[DEBUG] Audio started! Current time: ' + this.audio.currentTime);
      } else {
        log("Starting music!")
        try {
          this.audio.play();
        } catch (e) {
          log("Failed to start song! Maybe it doesn't exist?")
          this.audio = null
          this.stopSong()
          return
        }
      }
      this.tracks.set(songPath, this.audio);
      let h = this;
      this.audio.onended = function () {
        h.tracks.delete(songPath);
      }
    }, startingLate ? 0 : (startTime - currentTime));
  }

  stopSong() {
    if (this.audio !== undefined) {
      this.audio.pause();
    }
  }
}

// packet start time: 1641410169330.0
//        start time: 1641410169272.5  Wednesday, January 5, 2022 2:16:09.272 PM
//      current time: 1641169757293.0  Sunday, January 2, 2022 7:29:17.293 PM
//                        240411979.5