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
        try {
          this.audio.play();
        } catch (e) {
          log("Failed to start song! Maybe it doesn't exist?")
          this.audio = null
          this.stopSong()
          return
        }
        this.audio.currentTime = (howLate / 1000);
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
