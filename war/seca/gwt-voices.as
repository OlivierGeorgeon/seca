/*
 * Copyright 2007 Fred Sauer
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
import flash.external.ExternalInterface;

class Voices
{
  var sounds:Array = [];
  var stream:Array = [];
  
  function Voices() {
    Voices.log("Creating Voices '" + _root.id + "' ...");

    Voices.log("ExternalInterface.available = " + ExternalInterface.available);
    
    var wasSuccessful:Boolean;
    var result:Object;
    
    wasSuccessful = ExternalInterface.addCallback("createSound", this, createSound);
    Voices.log("addCallback(createSound) -> " + wasSuccessful);
    
    wasSuccessful = ExternalInterface.addCallback("playSound", this, playSound);
    Voices.log("addCallback(playSound) -> " + wasSuccessful);
    
    wasSuccessful = ExternalInterface.addCallback("stopSound", this, stopSound);
    Voices.log("addCallback(stopSound) -> " + wasSuccessful);
    
    wasSuccessful = ExternalInterface.addCallback("setVolume", this, setVolume);
    Voices.log("addCallback(setVolume) -> " + wasSuccessful);

    wasSuccessful = ExternalInterface.addCallback("setBalance", this, setBalance);
    Voices.log("addCallback(setBalance) -> " + wasSuccessful);

    Voices.log("Voices created.");

    // notify JavaScript that we are ready
    result = ExternalInterface.call("document.VoicesMovie['" + _root.id + "'].ready");
    Voices.log("document.VoicesMovie.ready() ... -> " + result);
  }
  
  function createSound(id:Number, url:String, streaming:Boolean):Void {
    Voices.log("createSound(id=" + id + ", url='" + url + "', streaming=" + streaming + ")...");
    stream[id] = streaming ? url : null;
    sounds[id] = new Sound();
    sounds[id].onLoad = function() {
      Voices.log("soundLoaded id=" + id);
      var result:Object = ExternalInterface.call("document.VoicesMovie['" + _root.id + "'].soundLoaded", id);
      Voices.log("document.VoicesMovie['" + _root.id + "'].soundLoaded(" + id + ") -> " + result);
    }
    sounds[id].onSoundComplete = function() {
      Voices.log("playbackCompleted id=" + id);
      var result:Object = ExternalInterface.call("document.VoicesMovie['" + _root.id + "'].playbackCompleted", id);
      Voices.log("document.VoicesMovie['" + _root.id + "'].playbackCompleted(" + id + ") -> " + result);
    }
    sounds[id].loadSound(url, streaming);
    Voices.log("...createSound(id=" + id + ", url='" + url + "', streaming=" + streaming + ")");
  }
  
  function playSound(id:Number):Void {
    Voices.log("playSound(id=" + id + ")");
    if (stream[id] != null) {
      sounds[id].loadSound(stream[id], true);
    }
    sounds[id].start();
  }
  
  function stopSound(id:Number):Void {
    Voices.log("stopSound(id=" + id + ")");
    sounds[id].stop();
    if (stream[id] != null) {
      sounds[id].loadSound("", true);
    }
  }
  
  function setVolume(id:Number, volume:Number):Void {
    Voices.log("setVolume(id=" + id + ", volume=" + volume + "%)");
    sounds[id].setVolume(volume);
  }
  
  function setBalance(id:Number, balance:Number):Void {
    Voices.log("setBalance(id=" + id + ", balance=" + balance + ")");
    sounds[id].setPan(balance);
  }
  
  static function log(text:String) {
//    ExternalInterface.call("document.VoicesMovie['" + _root.id + "'].log", text);
  }
  
  static function main(mc:MovieClip) {
    var app:Voices = new Voices();
  }
}