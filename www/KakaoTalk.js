// cordova.define("cordova-plugin-htjkakaotalk.KakaoTalk", function(require, exports, module) { var exec = require('cordova/exec');

var exec = require('cordova/exec');

var KakaoTalk = {
	login: function (successCallback, errorCallback) {
		exec(successCallback, errorCallback, "KakaoTalk", "login", []);
    },
	logout: function (successCallback, errorCallback) {
		exec(successCallback, errorCallback, 'KakaoTalk', 'logout', []);
	},
	share : function(options, successCallback, errorCallback) {

		for(var options_key in options){
			if(typeof options[options_key] === 'object'){
				console.log('object');
				for(var key in options[options_key]){
					options[options_key][key] = options[options_key][key] || '';
					console.log('options['+ options_key +']['+key+'] : ' + options[options_key][key]);
				};
			}else{
				console.log('NOT object');
				options[options_key] = options[options_key] || '';
				console.log('options['+ options_key +'] : ' + options[options_key]);
			}
		};

	    console.log('option : ' + JSON.stringify(options)); 
	    exec(successCallback, errorCallback, "KakaoTalk", "share", [options]);
	}
};

module.exports = KakaoTalk;

// });
