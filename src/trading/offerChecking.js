const TradeOfferManager = require('steam-tradeoffer-manager');
const SteamUser = require('steam-user');
const SteamCommunity = require('steamcommunity');
const SteamTotp = require('steam-totp');
const ReadLine = require('readline');

const client = new SteamUser();
const community = new SteamCommunity();
const io = ReadLine.createInterface(process.stdin, process.stdout, null);
const manager = new TradeOfferManager({
  steam: client,
  community: community,
  language: 'en'
});

const config = {
  username: "***REMOVED***",
  password: "***REMOVED***",
  sharedSecret: "***REMOVED***",
  identitySecret: "***REMOVED***"
}

const logOnOptions = {
  accountName: config.username,
  password: config.password,
  twoFactorCode: SteamTotp.generateAuthCode(config.sharedSecret),
  rememberPassword: true
 };  

client.logOn(logOnOptions);

client.on('webSession', (sessionid, cookies) => {
  manager.setCookies(cookies);
  community.setCookies(cookies);
  community.startConfirmationChecker(2000, config.identitySecret);
});

client.on('loggedOn', () => {
  client.setPersona(SteamUser.EPersonaState.Online); //Online
  //client.gamesPlayed(440);
});

let offerQueue = [];

manager.on('newOffer', (offer) =>{
  offerQueue.push(offer);
});

setInterval(function(){
  if(offerQueue.length != 0){
    let offer = offerQueue[0];
    offerQueue.shift();
    evaluateOffer(offer);
  }
}, 60000)

function evaluateOffer(offer){
  offer.partner = offer.partner.getSteamID64();
    io.question(JSON.stringify(offer) + "\n", function(response){
      if(response == "ACCEPT"){
        acceptOffer(offer);
        //console.log("success");
      } else if(response == "DECLINE"){
        declineOffer(offer);
        //console.log("success");
      } else if(response == "HOLD"){
        console.log("success");
      }
    });
}

function acceptOffer(offer) {
  client.relog();
  setTimeout(function(){
    offer.accept((err) => {
      if(err) {
        console.log('There was an error accepting the offer.');
        console.log(err);
      } else {
        console.log('success'); 
      }
    });
  }, 15000);
}

function declineOffer(offer) {
  client.relog();
  setTimeout(function(){
    offer.decline((err) => {
      if(err) {
        console.log('There was an error declining the offer.');
        console.log(err);
      } else {
        console.log('success'); 
      }
    });
  }, 15000);
}
