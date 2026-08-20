// MQTT 5.0 CONNECT 原始报文构造测试 — username 带冒号
const net = require('net');

function vbi(n) {
  const out = [];
  do { let b = n % 128; n = Math.floor(n / 128); if (n > 0) b |= 0x80; out.push(b); } while (n > 0);
  return Buffer.from(out);
}
function utf8(s) { const b = Buffer.from(s, 'utf8'); return Buffer.concat([Buffer.from([b.length >> 8, b.length & 0xff]), b]); }

const username = 'PK-8CD4A2CA480E4DD0:DK-8BE3FE31FECF4DC5';
const password = 'aa53f01dc37b513af6f027e92e7de60d2d7fec4d9afa94dcd4ecf92d04bc29d2';

// Variable header: protocol name "MQTT" + level 5 + flags + keepalive + properties
const vh = Buffer.concat([
  utf8('MQTT'),
  Buffer.from([0x05]),                                  // protocol level = 5
  Buffer.from([0xC2]),                                  // username | password | clean session
  Buffer.from([0x00, 0x3C]),                            // keep alive 60
  Buffer.from([0x00]),                                  // CONNECT properties length = 0 (MQTTX 默认空 properties)
]);
const payload = Buffer.concat([
  utf8('probe-client'),                                 // client id
  utf8(username),                                       // username
  utf8(password),                                       // password
]);
const body = Buffer.concat([vh, payload]);
const packet = Buffer.concat([Buffer.from([0x10]), vbi(body.length), body]);

const sock = net.connect(18831, '127.0.0.1');
sock.on('connect', () => { sock.write(packet); });
sock.on('data', d => {
  console.log('CONNACK reply hex:', d.toString('hex'));
  sock.end();
});
sock.on('error', e => { console.log('error:', e.message); });
setTimeout(() => { sock.destroy(); process.exit(0); }, 3000);
