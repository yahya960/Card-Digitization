cordova.define('cordova/plugin_list', function(require, exports, module) {
  module.exports = [
    {
      "id": "com.obdx.meezan.THSCard",
      "file": "plugins/com.obdx.meezan/www/THSCard.js",
      "pluginId": "com.obdx.meezan",
      "clobbers": [
        "cordova.plugins.THSCard"
      ]
    }
  ];
  module.exports.metadata = {
    "cordova-plugin-whitelist": "1.3.5",
    "com.obdx.meezan": "1.0.0"
  };
});