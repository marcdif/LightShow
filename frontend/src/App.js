import './App.css';
import AudioManager from './audiolib/index'

const WebSocket = require('isomorphic-ws');

const socketURL = "wss://home.marcdif.com:3925"
var audioManager = new AudioManager();
let clientId = null
let ws = null
let synchronizing = false;
let sync_start_local_time = 0;
let sync_server_time_offset = 0;
let synchronized = false;

function makeid(length) {
  var result = '';
  var characters = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';
  var charactersLength = characters.length;
  for (var i = 0; i < length; i++) {
    result += characters.charAt(Math.floor(Math.random() *
      charactersLength));
  }
  return result;
}

function log(msg) {
  var m = new Date();
  var dateString = m.getUTCFullYear() + "/" + (m.getUTCMonth() + 1) + "/" + m.getUTCDate() + " " + String(m.getUTCHours()).padStart(2, '0') + ":" + String(m.getUTCMinutes()).padStart(2, '0') + ":" + String(m.getUTCSeconds()).padStart(2, '0') + "." + String(m.getUTCMilliseconds()).padStart(3, '0');
  console.log(dateString + " | " + msg);
}

function connectToAudio() {
  log("Connecting...")
  clientId = makeid(16)
  log("Using clientId " + clientId)
  let timer = setTimeout(() => socketServer(), 500)
  return () => {
    clearTimeout(timer)
  }
}

function socketServer() {
  log("Connecting to " + socketURL)

  if (ws != null) {
    if (ws.readyState === WebSocket.OPEN) {
      log("[WARN] Closing existing connection...")
      ws.close()
    }
    ws = null;
  }

  ws = new WebSocket(socketURL)

  ws.onopen = function open() {
    log('Starting time sync process...');
    synchronizing = true
    var GetTime = new Packets.GetTime();
    log('Sending this packet: ' + GetTime.asJSON());
    ws.send(GetTime.asJSON());
    sync_start_local_time = Date.now()
  };

  ws.onclose = function close() {
    log('disconnected');
    audioManager.stopSong();
  }

  ws.onmessage = function incoming(data) {
    let json = JSON.parse(data.data)
    log('received: ' + JSON.stringify(json));
    try {
      if (typeof json.id === 'undefined') throw log('Packet sent without ID: ' + json);
      let p = null;
      if (json.id === PacketID.GET_TIME) {
        if (sync_start_local_time === 0 || !synchronizing) {
          log("Not handling GET_TIME packet - haven't started a sync process!")
          return;
        }
        p = (new Packets.GetTime()).fromObject(json);
        var received_time = Date.now();
        var difference = received_time - sync_start_local_time;
        sync_server_time_offset = sync_start_local_time - (p.serverTime - (difference / 2));

        log('p.serverTime: ' + p.serverTime);
        log('difference: ' + difference);
        log('difference/2: ' + (difference / 2));
        log('sync_start_local_time: ' + sync_start_local_time);

        log('Response took ' + difference + 'ms... setting sync_server_time_offset to ' + sync_server_time_offset);

        var current_server_time = Date.now() - sync_server_time_offset;

        log('Responding with server time being ' + current_server_time);
        sync_start_local_time = 0;

        var GetTime = (new Packets.ConfirmSync()).set(current_server_time);
        log('Sending this packet: ' + GetTime.asJSON());
        ws.send(GetTime.asJSON());
      } else if (json.id === PacketID.CONFIRM_SYNC) {
        if (!synchronizing) {
          log("Not handling CONFIRM_SYNC Packet - haven't started a sync process!")
          return;
        }
        synchronizing = false;
        p = (new Packets.ConfirmSync()).fromObject(json);
        if (p.clientTime >= 0) {
          // accepted
          log('[INFO] Sync succeeded! ' + p.clientTime + 'ms offset');
          sync_start_local_time = 0;
          synchronized = true;
        } else {
          // failed
          log('[ERROR] Sync failed!');
          sync_server_time_offset = 0;
          sync_start_local_time = 0;
        }
      } else if (json.id === PacketID.START_SONG) {
        if (!synchronized) {
          log("[ERROR] Can't start a song, we aren't synchronized!");
          return;
        }
        p = (new Packets.StartSong()).fromObject(json);
        audioManager.startSong(p.songPath, p.startTime - sync_server_time_offset, p.songDuration);
      } else if (json.id === PacketID.STOP_SONG) {
        if (!synchronized) {
          log("[ERROR] Can't stop a song, we aren't synchronized!");
          return;
        }
        audioManager.stopSong();
      }
    } catch (a) {
      ws.close();
      log('An error occured: ' + a + '<br/>The sent data was ' + json + '. Line number #' + (a != null ? a.lineNumber : ""));
    }
  };
}

function App() {
  return (
    <div className="App">
      <header className="App-header">
        <h1>LightShow Control Panel</h1>
        <div className="buttons">
          <button className="button" onClick={() => connectToAudio()}>Listen to the Music</button><br />
          <a href="/showcontrol" className="button">Show Control</a><br />
          {/* <a href="#" className="button">Shutdown</a><br />
          <a href="#" className="button">Restart</a><br /> */}
        </div>
      </header>
    </div>
  );
}

var PacketID = {
  HEARTBEAT: 0,
  GET_TIME: 1,
  CONFIRM_SYNC: 2,
  CLIENT_CONNECT: 3,
  START_SONG: 4,
  STOP_SONG: 5
}
var Packets = {};
Packets.protocolVersion = 8;
Packets.Heartbeat = function () {
  this.message = '';
  this.reason = '';
  this.set = function () {
    return this;
  };
  this.fromObject = function (e) {
    return this;
  };
  this.asJSON = function () {
    return JSON.stringify({
      id: PacketID.HEARTBEAT
    })
  }
};
Packets.GetTime = function () {
  this.serverTime = 0;
  this.set = function (t) {
    this.serverTime = t;
    return this;
  };
  this.fromObject = function (e) {
    this.serverTime = e.serverTime;
    return this;
  };
  this.asJSON = function () {
    return JSON.stringify({
      id: PacketID.GET_TIME,
      serverTime: this.serverTime
    })
  };
}
Packets.ConfirmSync = function () {
  this.clientTime = 0;
  this.set = function (t) {
    this.clientTime = t;
    return this;
  };
  this.fromObject = function (e) {
    this.clientTime = e.clientTime;
    return this;
  };
  this.asJSON = function () {
    return JSON.stringify({
      id: PacketID.CONFIRM_SYNC,
      clientTime: this.clientTime
    })
  };
}
Packets.ClientConnect = function () {
  this.clientId = '';
  this.set = function (t) {
    this.clientId = t;
    return this;
  };
  this.fromObject = function (e) {
    this.clientId = e.clientId;
    return this;
  };
  this.asJSON = function () {
    return JSON.stringify({
      id: PacketID.CLIENT_CONNECT,
      clientId: this.clientId
    })
  };
};
Packets.StartSong = function () {
  this.songPath = '';
  this.startTime = 0;
  this.songDuration = 0;
  this.set = function (e, f, g) {
    this.songPath = e;
    this.startTime = f;
    this.songDuration = g;
    return this;
  };
  this.fromObject = function (e) {
    this.songPath = e.songPath;
    this.startTime = e.startTime;
    this.songDuration = e.songDuration;
    return this;
  };
  this.asJSON = function () {
    return JSON.stringify({
      id: PacketID.START_SONG,
      songPath: this.songPath,
      startTime: this.startTime,
      songDuration: this.songDuration
    })
  }
};

export default App;
