cordova.define("com.obdx.meezan.THSCard", function(require, exports, module) {
var exec = require('cordova/exec');

exports.coolMethod = function (arg0, success, error) {
    exec(success, error, 'THSCard', 'coolMethod', [arg0]);
};

exports.addCard = function(cardNumber, expiry, cvv, aSuccessCallback, aErrorCallback) {
    var successCallback = aSuccessCallback || noop
    var errorCallback = aErrorCallback || noop

    if (!isFunction(successCallback)) {
        throw new TypeError("Missing or invalid argument, 'successCallback'. Function expected.")
    }

    if (!isFunction(errorCallback)) {
        throw new TypeError("Invalid argument, 'errorCallback'. Function expected.")
    }

    if (!isString(cardNumber) || !isString(expiry) || !isString(cvv)) {
        errorCallback(new TypeError("Missing or invalid argument, (cardNumber, expiry, cvv). String expected."))
        return
    }

    var onSuccess = function(res) {
        successCallback(res)
    }
    var onError = function(errMessage) {
        errorCallback(toError(errMessage))
    }
    console.log('2=-=->', cardNumber, expiry, cvv)
    exec(onSuccess, onError, 'THSCard', 'addCard', [this.name, cardNumber, expiry, cvv])
};

exports.initMGSDK = function(aSuccessCallback, aErrorCallback) {
    var successCallback = aSuccessCallback || noop
    var errorCallback = aErrorCallback || noop

    if (!isFunction(successCallback)) {
        throw new TypeError("Missing or invalid argument, 'successCallback'. Function expected.")
    }

    if (!isFunction(errorCallback)) {
        throw new TypeError("Invalid argument, 'errorCallback'. Function expected.")
    }

    var onSuccess = function(res) {
        successCallback(res)
    }
    var onError = function(errMessage) {
        errorCallback(toError(errMessage))
    }
    exec(onSuccess, onError, 'THSCard', 'initMGSDK', [this.name])
};

exports.cardLoad= function(aSuccessCallback, aErrorCallback) {
 var successCallback = aSuccessCallback || noop
    var errorCallback = aErrorCallback || noop

    if (!isFunction(successCallback)) {
        throw new TypeError("Missing or invalid argument, 'successCallback'. Function expected.")
    }

    if (!isFunction(errorCallback)) {
        throw new TypeError("Invalid argument, 'errorCallback'. Function expected.")
    }

    var onSuccess = function(res) {
        successCallback(res)
    }
    var onError = function(errMessage) {
        errorCallback(toError(errMessage))
    }
    exec(onSuccess, onError, 'THSCard', 'cardLoad', [this.name])
}
exports.acceptterms= function(aSuccessCallback, aErrorCallback) {
 var successCallback = aSuccessCallback || noop
    var errorCallback = aErrorCallback || noop

    if (!isFunction(successCallback)) {
        throw new TypeError("Missing or invalid argument, 'successCallback'. Function expected.")
    }

    if (!isFunction(errorCallback)) {
        throw new TypeError("Invalid argument, 'errorCallback'. Function expected.")
    }

    var onSuccess = function(res) {
        successCallback(res)
    }
    var onError = function(errMessage) {
        errorCallback(toError(errMessage))
    }
    exec(onSuccess, onError, 'THSCard', 'acceptterms', [this.name])
}

exports.getTransactionHistory = function(aSuccessCallback, aErrorCallback) {
 var successCallback = aSuccessCallback || noop
    var errorCallback = aErrorCallback || noop

    if (!isFunction(successCallback)) {
        throw new TypeError("Missing or invalid argument, 'successCallback'. Function expected.")
    }

    if (!isFunction(errorCallback)) {
        throw new TypeError("Invalid argument, 'errorCallback'. Function expected.")
    }

    var onSuccess = function(res) {
        successCallback(res)
    }
    var onError = function(errMessage) {
        errorCallback(toError(errMessage))
    }
    exec(onSuccess, onError, 'THSCard', 'getTransactionHistory', [this.name])
}

function isArray(value) {
    return /^\[object Array\]$/.test(Object.prototype.toString.call(value))
}

function identity(value) {
    return value
}

function isBoolean(value) {
    return typeof value === 'boolean'
}

function isFunction(value) {
    return typeof value === 'function'
}

function isNumber(value) {
    return typeof value === 'number'
}

function isString(value) {
    return typeof value === 'string'
}

function isUndefined(value) {
    return typeof value === 'undefined'
}

function noop() {}

function toError(errMessage) {
    return new Error(errMessage)
}

});
