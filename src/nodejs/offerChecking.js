//TODO:

const TradeOfferManager = require('steam-tradeoffer-manager');
const SteamUser = require('steam-user');
const SteamCommunity = require('steamcommunity');
const SteamTotp = require('steam-totp');
const ReadLine = require('readline');
const config = {
  username: process.argv[2],
  password: process.argv[3],
  sharedSecret: process.argv[4],
  identitySecret: process.argv[5],
  offerCheckTime: parseInt(process.argv[6], 10),
}

const client = new SteamUser();
const community = new SteamCommunity();
const io = ReadLine.createInterface(process.stdin, process.stdout, null);
const manager = new TradeOfferManager({
  steam: client,
  community: community,
  language: 'en'
});

const logOnOptions = {
  accountName: config.username,
  password: config.password,
  twoFactorCode: SteamTotp.generateAuthCode(config.sharedSecret),
  rememberPassword: true
 };  

const successMessage = 'success';
const offerCheckTime = config.offerCheckTime;
let offerQueue = [];
let nextOffer = null;
let nextOfferAction = null;

client.logOn(logOnOptions);

client.on('webSession', (sessionid, cookies) => {
  manager.setCookies(cookies);
  community.setCookies(cookies);
  community.startConfirmationChecker(2000, config.identitySecret);
});

client.on('loggedOn', () => {
  client.setPersona(SteamUser.EPersonaState.Online); //Online
  if(nextOffer !== null){
    setTimeout(() => {
      if(nextOfferAction === false){
        declineOffer(nextOffer);
      } else {
        acceptOffer(nextOffer);
      }
      nextOfferAction = null;
      nextOffer = null;
    }, 2500)
    
  }
  //client.gamesPlayed(440);
});

manager.on('newOffer', (offer) =>{
  offerQueue.push(offer);
});

setInterval(function(){
  if(offerQueue.length != 0 && nextOffer === null){
    let offer = offerQueue[0];
    offerQueue.shift();
    evaluateOffer(offer);
  }
}, offerCheckTime)

function evaluateOffer(offer){
  offer.partner = offer.partner.getSteamID64();
    io.question(JSON.stringify(offer) + "\n", function(response){
      if(response == "ACCEPT"){
        nextOffer = offer;
        nextOfferAction = true;
        client.relog();
      } else if(response == "DECLINE"){
        nextOffer = offer;
        nextOfferAction = false;
        client.relog();
      } else if(response == "HOLD"){
        console.log(successMessage);
      }
    });
}

function acceptOffer(offer) {
  offer.accept((err) => {
    if(err) {
      console.log(err);
    } else {
      console.log(successMessage); 
    }
  });
}

function declineOffer(offer) {
  offer.decline((err) => {
    if(err) {
      console.log(err);
    } else {
      console.log(successMessage); 
    }
  });
}
