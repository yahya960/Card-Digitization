/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

// Wait for the deviceready event before using any of Cordova's device APIs.
// See https://cordova.apache.org/docs/en/latest/cordova/events/events.html#deviceready
var ThsCard;
var Cards;
var transaction_history;
var app = {
    //Application constructor
    initialize: function() {
        document.addEventListener('deviceready', this.onDeviceReady.bind(this), false);
    },

    onDeviceReady: function () {
        // Cordova is now initialized. Have fun!


        document.getElementById('deviceready').classList.add('ready');

        var success = function(message) {
            alert(message);
        }

        var failure = function() {
            alert("Error calling Hello Plugin");
        }

        ThsCard = window.cordova.plugins.THSCard;

        document.getElementById('addCard').addEventListener('click', this.addCard);
         document.getElementById('cardLoad').addEventListener('click', this.cardLoad);
//          document.getElementById('acceptterms').addEventListener('click', this.acceptterms);
           document.getElementById('getTransactionHistory').addEventListener('click', this.getTransactionHistory);
        document.getElementById('initMGSDK').addEventListener('click', this.initMDSDK);
        const cardNumber = document.getElementById('cardNumber');
        const expiry = document.getElementById('expiry');
        const cvv = document.getElementById('cvv');
        cardNumber.value = '4422941234636808';
        expiry.value = '1225';
        cvv.value = '123';
    },






    addCard: function(){
        //validate input
        const cardNumber = document.getElementById('cardNumber').value;
        const expiry = document.getElementById('expiry').value;
        const cvv = document.getElementById('cvv').value;

        if (cardNumber.length != 16) {
            alert('invalid card number')
            return;
        }
        if (expiry.length != 4) {
            alert('invalid expiry')
            return;
        }
        if (cvv.length != 3) {
            alert('invalid cvv')
            return;
        }
        var successCallback = function(res) {
            alert(`SUCCESS!!! `+ res)
        }
        var errorCallback = function(err) {
            alert('ERROR!!! ' + err)
        }
        console.log('1=-=->', cardNumber, expiry, cvv)
        ThsCard.addCard(cardNumber, expiry, cvv, successCallback, errorCallback);
    },

    initMDSDK: function(){
        var successCallback = function(res) {
            res == "Configured Already" ? alert(res) : alert(`SUCCESS!!! `+ res);
        }
        var errorCallback = function(err) {
            alert('ERROR!!! ' + err)
        }
        ThsCard.initMGSDK(successCallback, errorCallback);
    },

    cardLoad: function(){

      var successCallback = function(res) {
          Cards = JSON.parse(res)
            }
            var errorCallback = function(err) {
            alert("Error",err)
            }
            ThsCard.cardLoad(successCallback, errorCallback);
    },
     acceptterms: function(){

          var successCallback = function(res) {
                    res == "Configured Already" ? alert(res) : alert(`SUCCESS!!! `+ res);
                }
                var errorCallback = function(err) {
                }
                ThsCard.acceptterms(successCallback, errorCallback);
        },
        getTransactionHistory: function(){

               var successCallback = function(res) {
                         transaction_history = JSON.parse(res)
                     }
                     var errorCallback = function(err) {
                         alert('ERROR!!! ' + err)
                     }
                     ThsCard.getTransactionHistory(successCallback, errorCallback);
             },
}
app.initialize();
