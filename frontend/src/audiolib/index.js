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
          log('Song was over before we started playing.');
          return;
        }
        this.audio.play();
        this.audio.currentTime = (howLate / 1000);
      } else {
        this.audio.play();
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

function log(msg) {
  var m = new Date();
  var dateString = m.getUTCFullYear() + "/" + (m.getUTCMonth() + 1) + "/" + m.getUTCDate() + " " + String(m.getUTCHours()).padStart(2, '0') + ":" + String(m.getUTCMinutes()).padStart(2, '0') + ":" + String(m.getUTCSeconds()).padStart(2, '0') + "." + String(m.getUTCMilliseconds()).padStart(3, '0');
  console.log(dateString + " | " + msg);
}
